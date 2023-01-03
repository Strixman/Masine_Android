package com.example.masine.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.masine.scripts.Application
import com.example.masine.databinding.FragmentMainBinding
import com.mapbox.mapboxsdk.geometry.LatLng

class MainFragment : Fragment() {
    private lateinit var app: Application
    private lateinit var binding: FragmentMainBinding

    private var simulating = false;
    private val locations = mutableListOf(LatLng(), LatLng())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false);

        if(arguments?.containsKey("latitude") == true){
            val latitude = arguments?.getFloatArray("latitude")!!
            val longitude = arguments?.getFloatArray("longitude")!!

            locations[0] = LatLng(latitude[0].toDouble(), longitude[0].toDouble());
            locations[1] = LatLng(latitude[1].toDouble(), longitude[1].toDouble());
        }

        /*binding.simulateButton.setOnClickListener {
            if(simulating) return@setOnClickListener;

            binding.simulateButton.isClickable = false;
            simulating = true;
            app.simulateSpeed("test1", binding.speedSlider.progress.toDouble(), 10.0, 100.0, 10000, { vehicle ->
                requireActivity().runOnUiThread {
                    binding.text3.setText("Speed:   " + String.format("%.2f", vehicle.speed));
                }
            }) {
                simulating = false;
                binding.simulateButton.isClickable = true;
            };
        }*/

        binding.locationButton.setOnClickListener {
            val latitude = FloatArray(2) {
                locations[it].latitude.toFloat()
            }
            val longitude= FloatArray(2) {
                locations[it].longitude.toFloat()
            }

            val action = MainFragmentDirections.actionMainFragmentToMapsFragment(latitude,longitude);
            it.findNavController().navigate(action)
        }

        /*binding.simulateButton.setOnClickListener {
            if(simulating) return@setOnClickListener;
            if(locations[0].latitude == 0.0 && locations[1].latitude == 0.0 && locations[0].longitude == 0.0 && locations[1].longitude == 0.0) return@setOnClickListener

            binding.simulateButton.isClickable = false;
            simulating = true;

            app.simulateLocation("test1", locations[0], locations[1], { vehicle ->
                Log.d("pub", vehicle.location.toString());
            }) {
                simulating = false;
                binding.simulateButton.isClickable = true;
            };
        }*/

        binding.simulateButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToSimulationFragment(null, null)
            it.findNavController().navigate(action)
        }

        return binding.root;
    }
}