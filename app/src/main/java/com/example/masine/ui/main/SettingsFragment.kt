package com.example.masine.ui.main

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.masine.MainActivity
import com.example.masine.R
import com.example.masine.databinding.FragmentMainBinding
import com.example.masine.databinding.FragmentSettingsBinding
import com.example.masine.scripts.Application
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {
    private lateinit var app: Application
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = app.getStorage("vehicleName")
        binding.vehicleNameSettingsInput.setText(name)

        val address = app.getStorage("address")
        binding.mqttAddressInput.setText(address)

        binding.saveButton.setOnClickListener {
            if((binding.vehicleNameSettingsInput.text.toString() == "" || binding.vehicleNameSettingsInput.text.toString() == name) && (binding.mqttAddressInput.text.toString() == "") || binding.mqttAddressInput.text.toString() == address) {
                onError("Invalid or same")
                return@setOnClickListener
            }

            app.setStorage("address", binding.mqttAddressInput.text.toString())
            app.setStorage("vehicleName", binding.vehicleNameSettingsInput.text.toString())

            (activity as MainActivity?)!!.replaceFragment(SettingsFragment())
        }
    }

    private fun onError(message: String){
        val error = Snackbar.make(binding.root, message, 1500)
        error.view.setBackgroundColor(Color.CYAN)
        error.show()
    }
}