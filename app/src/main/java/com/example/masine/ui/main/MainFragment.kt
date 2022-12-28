package com.example.masine.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.masine.scripts.Application
import com.example.masine.databinding.FragmentMainBinding
import com.mapbox.mapboxsdk.geometry.LatLng

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var app: Application
    private lateinit var binding: FragmentMainBinding

    private var simulating = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false);

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

        binding.simulateButton.setOnClickListener {
            if(simulating) return@setOnClickListener;

            binding.simulateButton.isClickable = false;
            simulating = true;
            app.simulateLocation("test1", LatLng(46.557392, 15.646352), LatLng(46.558410, 15.651098), 10000, { vehicle ->
                Log.d("pub", vehicle.location.toString());
            }) {
                simulating = false;
                binding.simulateButton.isClickable = true;
            };
        }

        return binding.root;
    }
}