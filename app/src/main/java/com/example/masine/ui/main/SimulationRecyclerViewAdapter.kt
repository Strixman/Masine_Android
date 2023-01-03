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

        simulation.notifyOnChange = {
            activity.runOnUiThread{
                notifyItemChanged(position)
            }
        }
        simulation.notifyOnFinnish = {
            activity.runOnUiThread{
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, simulations.size)
            }
        }

        holder.location.text = simulation.vehicle.location.toString()
        holder.speed.text = simulation.vehicle.speed.toString()
        holder.temperature.text = simulation.vehicle.temperature.toString()

        holder.removeButton.setOnClickListener {
            simulation.stop()
        }
    }

    override fun getItemCount(): Int = simulations.size

    inner class ViewHolder(binding: FragmentSimulationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val location: TextView = binding.location
        val speed: TextView = binding.speed
        val temperature: TextView = binding.temperature
        val removeButton = binding.removeButton
    }

}