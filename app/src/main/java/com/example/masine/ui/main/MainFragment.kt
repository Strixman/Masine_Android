package com.example.masine.ui.main

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.masine.databinding.FragmentMainBinding
import com.example.masine.scripts.Application
import com.mapbox.mapboxsdk.geometry.LatLng

class MainFragment : Fragment() {
    private lateinit var app: Application
    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false);

        binding.locationButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToVehicleFragment();
            it.findNavController().navigate(action)
        }

        binding.simulateButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToSimulationFragment(null, null)
            it.findNavController().navigate(action)
        }
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestMultiplePermissions.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
}