package com.example.masine.ui.main

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.masine.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException


class MapsFragment : Fragment(), OnMapReadyCallback  {
    private lateinit var binding: FragmentMapsBinding

    private lateinit var searchView: SearchView;
    private lateinit var map: GoogleMap;

    private var currentMarker = 0;
    private var numOfMarkers = 0;
    private val locations = mutableListOf(LatLng(0.0,0.0), LatLng(0.0,0.0))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments?.containsKey("latitude") == true){
            val latitude = arguments?.getFloatArray("latitude")!!
            val longitude = arguments?.getFloatArray("longitude")!!

            locations[0] = LatLng(latitude[0].toDouble(), longitude[0].toDouble())
            locations[1] = LatLng(latitude[1].toDouble(), longitude[1].toDouble())
        }

        searchView = binding.searchLocation
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(numOfMarkers >= 2) return false
                val location: String = searchView.query.toString()
                val addressList : List<Address>?;

                if(location != ""){
                    val geocoder = Geocoder(requireContext())

                    try{
                        addressList = geocoder.getFromLocationName(location, 1);
                    }
                    catch (e : IOException){
                        e.printStackTrace()
                        return false
                    }

                    val address = addressList!![0]
                    val latLng = LatLng(address.latitude, address.longitude)

                    locations[currentMarker] = latLng

                    val marker = map.addMarker(MarkerOptions().position(latLng).title(location))
                    marker?.tag = currentMarker
                    if(currentMarker > 0) marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

                    currentMarker++
                    numOfMarkers++
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.clearButton.setOnClickListener {
            map.clear()
            currentMarker = 0
            numOfMarkers = 0

            locations[0] = LatLng(0.0,0.0)
            locations[1] = LatLng(0.0,0.0)
        }

        binding.saveButton.setOnClickListener {
            if(locations[0].latitude == 0.0 && locations[1].latitude == 0.0 && locations[0].longitude == 0.0 && locations[1].longitude == 0.0) return@setOnClickListener

            val latitude = FloatArray(2) {
                locations[it].latitude.toFloat()
            }
            val longitude= FloatArray(2) {
                locations[it].longitude.toFloat()
            }

            val action = MapsFragmentDirections.actionMapsFragmentToMainFragment(latitude, longitude);
            it.findNavController().navigate(action)
        }

        val mapFragment = childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        for(location in locations){
            if(location.latitude != 0.0 || location.longitude != 0.0) {
                val marker = map.addMarker(MarkerOptions().position(location).title(""))
                marker?.tag = currentMarker
                if(currentMarker > 0) marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))

                currentMarker++;
                numOfMarkers++
            }
        }

        map.setOnMapClickListener {
            if(numOfMarkers >= 2) return@setOnMapClickListener
            locations[currentMarker] = it;

            val marker = map.addMarker(MarkerOptions().position(it).title(""))
            marker?.tag = currentMarker
            if(currentMarker > 0) marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))

            currentMarker++;
            numOfMarkers++
        }

        map.setOnMarkerClickListener {
            numOfMarkers--;

            currentMarker = it.tag as Int;
            if(numOfMarkers == 0 && currentMarker == 1) currentMarker = 0;

            it.remove();
            return@setOnMarkerClickListener true
        }
    }
}