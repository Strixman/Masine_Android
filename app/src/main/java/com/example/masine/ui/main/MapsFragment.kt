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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException


class MapsFragment : Fragment(), OnMapReadyCallback  {
    private lateinit var binding: FragmentMapsBinding

    private lateinit var searchView: SearchView;
    private lateinit var map: GoogleMap;
    private var latLng: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = binding.searchLocation
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

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
                    latLng = LatLng(address.latitude, address.longitude)

                    map.addMarker(MarkerOptions().position(latLng!!).title(location))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng!!, 16f))
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.saveButton.setOnClickListener {
            if(latLng == null) return@setOnClickListener
            val latitude : FloatArray;
            val longitude : FloatArray;

            if(arguments?.getInt("location") == 0){
                latitude = FloatArray(2) {
                    if(it == 0) latLng!!.latitude.toFloat()
                    else arguments?.getFloatArray("latitude")?.get(1)!!
                };

                longitude = FloatArray(2) {
                    if(it == 0) latLng!!.longitude.toFloat()
                    else arguments?.getFloatArray("longitude")?.get(1)!!
                };
            }
            else{
                latitude = FloatArray(2) {
                    if(it == 0) arguments?.getFloatArray("latitude")?.get(0)!!
                    else latLng!!.latitude.toFloat()
                };

                longitude = FloatArray(2) {
                    if(it == 0) arguments?.getFloatArray("longitude")?.get(0)!!
                    else latLng!!.longitude.toFloat()
                };
            }

            val action = MapsFragmentDirections.actionMapsFragmentToMainFragment(latitude, longitude);
            it.findNavController().navigate(action)
        }

        val mapFragment = childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener {
            latLng = it;
            map.clear()
            map.addMarker(MarkerOptions().position(it).title(""))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }
}