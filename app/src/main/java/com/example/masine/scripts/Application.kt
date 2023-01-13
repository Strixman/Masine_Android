package com.example.masine.scripts

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.getSystemService
import com.mapbox.mapboxsdk.geometry.LatLng
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.security.Provider
import java.time.LocalDateTime
import java.util.*

@SuppressLint("MissingPermission")
class Application : android.app.Application() {
    private val MQTT_SERVER_URL = "tcp://192.168.1.162:3030"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mqttClient : MqttAndroidClient

    lateinit var bluetoothManager : BluetoothManager
    lateinit var locationManager: LocationManager

    lateinit var obd: OBD

    val simulations = mutableListOf<Simulation>()

    lateinit var ID : String

    override fun onCreate() {
        super.onCreate();
        bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        sharedPreferences = getSharedPreferences(applicationInfo.name, Context.MODE_PRIVATE)
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
            override fun onSuccess(asyncActionToken: IMqttToken) {

            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        mqttClient.reconnect()
                    }
                }, 5000)
            }
        })

        obd = object : OBD("TEST1", locationManager){
            override fun onUpdate(time: LocalDateTime, location: Location, speed: Float, temperature: Float, rpm: Int) {
                if(mqttClient.isConnected){
                    val messageLocation = MqttMessage(("${vehicleName}$${time}$${location.latitude}$${location.longitude}").toByteArray())
                    mqttClient.publish("VehicleLocation", messageLocation, null, null)

                    val messageSpeed = MqttMessage(("${vehicleName}$${time}$${speed}").toByteArray())
                    mqttClient.publish("VehicleSpeed", messageSpeed, null, null)

                    val messageTemperature = MqttMessage(("${vehicleName}$${time}$${temperature}").toByteArray())
                    mqttClient.publish("VehicleTemperature", messageTemperature, null, null)

                    val messageRPM = MqttMessage(("${vehicleName}$${time}$${temperature}").toByteArray())
                    mqttClient.publish("VehicleRPM", messageRPM, null, null)
                }
            }

            override fun onStart(location: Location) {
                if(mqttClient.isConnected){
                    val message = MqttMessage(("${vehicleName}$${location.latitude}\$${location.longitude}").toByteArray())
                    mqttClient.publish("VehicleConnected", message, null, null)
                }
            }

            override fun onStop(location: Location) {
                if(mqttClient.isConnected){
                    val message = MqttMessage(("${vehicleName}$${location.latitude}\$${location.longitude}").toByteArray())
                    mqttClient.publish("VehicleDisconnected", message, null, null)
                }
            }
        }
    }

    fun onDestroy(){
        for(simulation in simulations) {
            simulation.stop()
        }
        simulations.clear()

        mqttClient.disconnect()
    }

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