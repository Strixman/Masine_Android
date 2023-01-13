package com.example.masine.scripts

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import com.mapbox.mapboxsdk.geometry.LatLng
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
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
    val simulations = Simulations()

    //val simulations = mutableListOf<Simulation>()

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
        simulations.clear()
        mqttClient.disconnect()
    }

    fun addSimulation(vehicleName: String, startLocation: LatLng, endLocation: LatLng, minSpeed: Float, maxSpeed: Float, minTemperature: Float, maxTemperature: Float) : Boolean {
        val simulation = object: Simulation(vehicleName, startLocation, endLocation, minSpeed, maxSpeed, minTemperature, maxTemperature){
            override fun onUpdate(time: LocalDateTime, location: LatLng, speed : Float, temperature: Float, rpm : Float) {
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

            override fun onFinnish(location: LatLng) {
                if(mqttClient.isConnected){
                    val message = MqttMessage(("${vehicleName}$${location.latitude}$${location.longitude}").toByteArray())
                    mqttClient.publish("VehicleDisconnected", message, null, null)
                }

                simulations.remove(this)
            }

            override fun onStart(location: LatLng) {
                if(mqttClient.isConnected) {
                    val message = MqttMessage(("${vehicleName}$${location.latitude}$${location.longitude}").toByteArray())
                    mqttClient.publish("VehicleConnected", message, null, null)
                }
            }
        }

        return simulations.add(simulation)
    }
}