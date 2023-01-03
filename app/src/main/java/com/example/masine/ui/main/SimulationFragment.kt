package com.example.masine.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.masine.databinding.FragmentSimulationBinding
import com.example.masine.scripts.Application
import com.mapbox.mapboxsdk.geometry.LatLng

class SimulationFragment : Fragment() {
    private lateinit var app: Application
    private lateinit var binding: FragmentSimulationBinding

    private val locations = mutableListOf(LatLng(), LatLng())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSimulationBinding.inflate(inflater, container, false);

        if(arguments?.containsKey("latitude") == true){
            val latitude = arguments?.getFloatArray("latitude")
            val longitude = arguments?.getFloatArray("longitude")

            if(latitude != null && longitude != null){
                locations[0] = LatLng(latitude[0].toDouble(), longitude[0].toDouble());
                locations[1] = LatLng(latitude[1].toDouble(), longitude[1].toDouble());
            }
        }

        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = SimulationRecyclerViewAdapter(requireActivity(), app.simulations)
        }

        binding.locations.setOnClickListener {
            val latitude = FloatArray(2) {
                locations[it].latitude.toFloat()
            }
            val longitude= FloatArray(2) {
                locations[it].longitude.toFloat()
            }

            val action = SimulationFragmentDirections.actionSimulationFragmentToMapsFragment(latitude,longitude);
            it.findNavController().navigate(action)
        }

        binding.addSimulationButton.setOnClickListener {
            if(binding.vehicleName.text.toString() == "" || (locations[0].latitude == 0.0 && locations[0].longitude == 0.0 && locations[1].latitude == 0.0 && locations[1].longitude == 0.0)) return@setOnClickListener
            val simulation = app.addSimulation(binding.vehicleName.text.toString(), locations[0], locations[1]);
            if(simulation != null){
                simulation.start()
                binding.list.adapter!!.notifyItemInserted(app.simulations.size - 1)
            }
        }

        return binding.root
    }
}