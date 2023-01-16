package com.example.masine.ui.main

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.masine.databinding.FragmentSimulationItemBinding
import com.example.masine.scripts.Simulation

class SimulationRecyclerViewAdapter(private val activity: Activity, private val simulations: MutableList<Simulation>) : RecyclerView.Adapter<SimulationRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FragmentSimulationItemBinding.inflate(LayoutInflater.from(parent.context), parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val simulation = simulations[position]

        simulation.UICallback = {
            activity.runOnUiThread{
                notifyItemChanged(position)
            }
        }
        simulation.StopUICallback = {
            activity.runOnUiThread{
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, simulations.size)
            }
        }

        holder.temperature.withTremble = false
        holder.rpm.withTremble = false

        holder.temperature.minSpeed = simulation.minTemperature
        holder.rpm.minSpeed = simulation.minSpeed * 60

        holder.temperature.maxSpeed = simulation.maxTemperature
        holder.rpm.maxSpeed = simulation.maxSpeed * 60

        holder.vehicleName.text = simulation.vehicleName
//        holder.location.text = "Location: ${String.format("%.5f", simulation.location.latitude)}, ${String.format("%.5f", simulation.location.longitude)}"
        holder.latitude.text = String.format("%.5f", simulation.location.latitude)
        holder.longitude.text = String.format("%.5f", simulation.location.longitude)
        holder.speed.setSpeed(simulation.speed.toInt(), 1000)
        holder.temperature.setSpeedAt(simulation.temperature)
        holder.rpm.setSpeedAt(simulation.rpm)

    }

    override fun getItemCount(): Int = simulations.size

    inner class ViewHolder(binding: FragmentSimulationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val latitude = binding.xValue
        val longitude = binding.yValue
        val speed = binding.speedometer
        val temperature = binding.temperature
        val rpm = binding.rpm
        val vehicleName = binding.name
    }

}