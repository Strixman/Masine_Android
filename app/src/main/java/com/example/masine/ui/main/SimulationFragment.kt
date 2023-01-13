package com.example.masine.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.masine.R
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findNavController().popBackStack(R.id.SimulationAddFragment, true)

        binding.addSimulationButton.setOnClickListener {
            val action = SimulationFragmentDirections.actionSimulationFragmentToSimulationAddFragment();
            view.findNavController().navigate(action)
        }

        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = SimulationRecyclerViewAdapter(requireActivity(), app.simulations.simulations)
        }
    }
}