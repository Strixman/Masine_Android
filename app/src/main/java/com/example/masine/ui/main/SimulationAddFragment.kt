package com.example.masine.ui.main

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.masine.MainActivity
import com.example.masine.databinding.FragmentSimulationAddBinding
import com.example.masine.scripts.Application
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import java.io.IOException


class SimulationAddFragment : Fragment(), OnMapReadyCallback  {
    private lateinit var app: Application
    private lateinit var binding: FragmentSimulationAddBinding

    private lateinit var searchView: SearchView;
    private lateinit var map: GoogleMap;

    private var currentMarker = 0
    private var numOfMarkers = 0

    private val locations = arrayOf(LatLng(0.0,0.0), LatLng(0.0,0.0))
    private val speed = floatArrayOf(-1.0f, -1.0f)
    private val temperature = floatArrayOf(-1.0f, -1.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSimulationAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = binding.searchLocation
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(numOfMarkers >= 2) return false
                val location: String = searchView.query.toString()

                if(location != ""){
                    val addressList : List<Address>?
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

        binding.searchButton.setOnClickListener {
            searchView.setQuery(searchView.query, true)
        }

        binding.clearButton.setOnClickListener {
            map.clear()
            currentMarker = 0
            numOfMarkers = 0

            locations[0] = LatLng(0.0,0.0)
            locations[1] = LatLng(0.0,0.0)
        }

        binding.minSpeed.addTextChangedListener {
            if(binding.minSpeed.text.toString() == "") speed[0] = -1f
            else speed[0] = binding.minSpeed.text.toString().toFloat()
        }
        binding.maxSpeed.addTextChangedListener {
            if(binding.maxSpeed.text.toString() == "") speed[1] = -1f
            else speed[1] = binding.maxSpeed.text.toString().toFloat()
        }

        binding.minTemperature.addTextChangedListener {
            if(binding.minTemperature.text.toString() == "") temperature[0] = -1f
            else temperature[0] = binding.minTemperature.text.toString().toFloat()
        }
        binding.maxTemperature.addTextChangedListener {
            if(binding.maxTemperature.text.toString() == "") temperature[1] = -1f
            else temperature[1] = binding.maxTemperature.text.toString().toFloat()
        }

        binding.saveButton.setOnClickListener {
            Log.d("test",temperature[0].toString())
            if(locations[0].latitude == 0.0 && locations[1].latitude == 0.0 && locations[0].longitude == 0.0 && locations[1].longitude == 0.0) {
                onError("Location not set")
                return@setOnClickListener
            }
            if(speed[0] < 0 || speed[1] < 0 || temperature[0] < 0 || temperature[1] < 0 || speed[1] < speed[0] || temperature[1] < temperature[0]) {
                onError("Speed or temperature invalid")
                return@setOnClickListener
            }

            if(binding.vehicleNameInput.text.toString() == ""){
                onError("Invalid vehicle name")
                return@setOnClickListener
            }

            if(!app.addSimulation(binding.vehicleNameInput.text.toString(), com.mapbox.mapboxsdk.geometry.LatLng(locations[0].latitude, locations[0].longitude),  com.mapbox.mapboxsdk.geometry.LatLng(locations[1].latitude, locations[1].longitude), speed[0], speed[1], temperature[0], temperature[1])){
                onError("Error creating simulation")
                return@setOnClickListener
            }

        }

        val mapFragment = childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val latLng = LatLng(46.554997, 15.645930)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))

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

    private fun onError(message: String){
        val error = Snackbar.make(binding.root, message, 1500)
        error.view.setBackgroundColor(Color.CYAN)
        error.show()
    }
}