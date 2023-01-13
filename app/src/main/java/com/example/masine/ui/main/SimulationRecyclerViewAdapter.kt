package com.example.masine.ui.main

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.masine.databinding.FragmentSimulationItemBinding
import com.example.masine.scripts.Simulation
import java.util.*

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

        holder.speed.withTremble = false
        holder.temperature.withTremble = false
        holder.rpm.withTremble = false


        //holder.location.text = simulation.location.toString()
        holder.location.text = "Location: ${String.format("%.5f", simulation.location.latitude)}, ${String.format("%.5f", simulation.location.longitude)}"
        holder.speed.setSpeedAt(simulation.speed)
        holder.temperature.setSpeedAt(simulation.temperature)
        holder.rpm.setSpeedAt(simulation.rpm)

    }

    override fun getItemCount(): Int = simulations.size

    inner class ViewHolder(binding: FragmentSimulationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val location = binding.location
        val speed = binding.speed
        val temperature = binding.temperature
        val rpm = binding.rpm
    }

}