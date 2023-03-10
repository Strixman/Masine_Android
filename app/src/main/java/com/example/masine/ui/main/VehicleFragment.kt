package com.example.masine.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.masine.scripts.Application
import com.example.masine.databinding.FragmentVehicleBinding
import com.example.masine.scripts.OBD
import com.google.android.material.snackbar.Snackbar
import me.ibrahimsn.lib.Speedometer


@SuppressLint("MissingPermission")
class VehicleFragment : Fragment() {
    private lateinit var app: Application
    private lateinit var binding: FragmentVehicleBinding
    private lateinit var obd : OBD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
        obd = app.obd
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVehicleBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.temperature.withTremble = false
        binding.rpm.withTremble = false
        binding.vehicleName.text = obd.vehicleName

        if(obd.connected){
            binding.obdButton.text = "Start"
        }
        else{
            binding.obdButton.text = "Connect"
        }

        binding.obdButton.setOnClickListener {
            //Start reading
            if(obd.connected && !obd.running) {
                if(!obd.start(this::onUpdateUI, this::onStopUI, this::onStartUI)){
                    binding.obdButton.text = "Start"
                    onError("Failed to start reading")
                }
                else binding.obdButton.text = "Starting"
            }
            //Stop reading
            else if(obd.connected && obd.running){
                if(!obd.stop()){
                    onError("Failed to stop reading")
                }
                else binding.obdButton.text = "START"
            }
            //Connect to device
            else{
                val devices = app.bluetoothManager.adapter.bondedDevices
                if(devices.size == 0) return@setOnClickListener

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Select device").setItems(Array(devices.size){devices.elementAt(it).name}){ dialog, index ->
                    val device = devices.elementAt(index)
                    dialog.dismiss()
                    if(!obd.connect(device)){
                        binding.obdButton.text = "Connect"
                        onError("Failed to connect to device")
                    }
                    else binding.obdButton.text = "Start"
                }.create()
                builder.setOnCancelListener {
                    binding.obdButton.text = "Connect"
                }
                builder.show()
                binding.obdButton.text = "Connecting"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(obd.running) obd.stop()
    }

    private fun onUpdateUI(speed : Float, temperature : Float, rpm: Int){
        requireActivity().runOnUiThread{
            binding.temperature.setSpeedAt(temperature)
            binding.rpm.setSpeedAt(rpm.toFloat())
            binding.speedometer.setSpeed(speed.toInt(), 1000)
        }
    }
    private fun onStopUI(){
        requireActivity().runOnUiThread {
            binding.temperature.setSpeedAt(0f)
            binding.rpm.setSpeedAt(0f)
            binding.speedometer.setSpeed(0, 1000)

            if (!obd.connected) binding.obdButton.text = "Connect"
            else binding.obdButton.text = "Start"
        }
    }
    private fun onStartUI(){
        requireActivity().runOnUiThread {
            binding.obdButton.text = "Stop"
        }
    }
    private fun onError(message: String){
        val error = Snackbar.make(binding.root, message, 1500)
        error.view.setBackgroundColor(Color.CYAN)
        error.show()
    }
}