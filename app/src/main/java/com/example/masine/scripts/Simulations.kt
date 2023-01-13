package com.example.masine.scripts

class Simulations {
    val simulations = mutableListOf<Simulation>()

    fun add(simulation: Simulation) : Boolean {
        for(sim in simulations){
            if(sim.vehicleName == simulation.vehicleName) return false
        }
        simulations.add(simulation)
        simulation.start()
        return true
    }

    fun remove(simulation: Simulation) {
        simulations.remove(simulation)
    }

    fun clear(){
        for(simulation in simulations) {
            simulation.stop()
        }
        simulations.clear()
    }
}