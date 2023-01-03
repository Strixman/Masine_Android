package com.example.masine.scripts

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import com.example.masine.BuildConfig
import com.example.masine.ui.main.MainFragment
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.turf.TurfMeasurement
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.Runnable
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import kotlin.time.Duration

class Application : android.app.Application() {
    private val MQTT_SERVER_URL = "tcp://192.168.1.162:3030"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mqttClient : MqttAndroidClient

    override fun onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences(applicationInfo.name, Context.MODE_PRIVATE)

        val ID : String;
        if(!sharedPreferences.contains("ID")){
            ID = UUID.randomUUID().toString()

            val editor = sharedPreferences.edit()
            editor.putString("ID", ID)
            editor.apply()
        }
        else{
            ID = sharedPreferences.getString("ID", "")!!;
        }

        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.isCleanSession = false

        mqttClient = MqttAndroidClient(this,  MQTT_SERVER_URL, ID);
        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {}

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        mqttClient.reconnect()
                    }
                }, 5000)
            }
        })
    }

    fun onDestroy(){
        for(simulation in simulations) {
            simulation.stop()
        }

        simulations.clear()
        mqttClient.disconnect()
    }

    val simulations = mutableListOf<Simulation>()

    fun addSimulation(vehicleName: String, startLocation: LatLng, endLocation: LatLng) : Simulation? {
        if(!mqttClient.isConnected) return null
        for(sim in simulations){
            if(sim.vehicleName == vehicleName) return null
        }

        val simulation = object: Simulation(vehicleName, startLocation, endLocation){
            override fun onUpdate(timestamp: LocalDateTime) {
                val messageLocation = MqttMessage(("${vehicleName}$${timestamp}$${vehicle.location.latitude}$${vehicle.location.longitude}").toByteArray())
                mqttClient.publish("VehicleLocation", messageLocation, null, null)

                val messageSpeed = MqttMessage(("${vehicleName}$${timestamp}$${vehicle.speed}").toByteArray())
                mqttClient.publish("VehicleSpeed", messageSpeed, null, null)

                val messageTemperature = MqttMessage(("${vehicleName}$${timestamp}$${vehicle.temperature}").toByteArray())
                mqttClient.publish("VehicleTemperature", messageTemperature, null, null)

                notifyOnChange?.let { it() }
            }

            override fun onFinnish() {
                val message = MqttMessage(("${vehicleName}$${vehicle.location.latitude}$${vehicle.location.longitude}").toByteArray())
                mqttClient.publish("VehicleDisconnected", message, null, null)

                simulations.remove(this)
                notifyOnFinnish?.let { it() }
            }

            override fun onStart() {
                val message = MqttMessage(("${vehicleName}$${vehicle.location.latitude}$${vehicle.location.longitude}").toByteArray())
                mqttClient.publish("VehicleConnected", message, null, null)
            }

        }

        simulations.add(simulation)
        return simulation
    }
}