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

class Application : android.app.Application() {
    private val MAPBOX_ACCESS_TOKEN = "pk.eyJ1Ijoic3RyaXhtYW4xMCIsImEiOiJjbGM1ZDVhMHU0cGpsM3drZWR3bGdib2VrIn0.J_EE1P7EpgcEOGT_EPXYvA"
    private val MQTT_SERVER_URL = "tcp://192.168.1.132:3030"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mqttClient : MqttAndroidClient
    private var running = true;

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
    }

    fun onDestroy(){
        running = false;
    }

    fun simulateSpeed(vehicleName: String, startSpeed: Double, minSpeed: Double, maxSpeed: Double, durationMs: Long, onUpdateCallback: (Vehicle) -> Unit, onFinnishCallback: () -> Unit) {
        val vehicle = Vehicle(vehicleName, speed = startSpeed)

        var running = true
        val thread = Thread{
            val rnd = Random()
            while(running && this.running){
                val timestamp = LocalDateTime.now()

                val num = rnd.nextDouble() * 3
                if(rnd.nextBoolean()){
                    vehicle.speed += num

                    if(vehicle.speed > maxSpeed){
                        vehicle.speed -= num * (rnd.nextInt(10) + 2)
                    }
                }
                else{
                    vehicle.speed -= num

                    if(vehicle.speed < minSpeed){
                        vehicle.speed += num * (rnd.nextInt(10) + 2)
                    }
                }

                onUpdateCallback(vehicle);

                val message = MqttMessage(("${timestamp}$${vehicle.location.latitude}$${vehicle.location.longitude}$${vehicle.speed}").toByteArray())
                mqttClient.publish("VehicleSpeed", message, this, null)

                Thread.sleep(500)
            }
        }
        thread.start();

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                running = false
                thread.join()

                onFinnishCallback()
            }
        }, durationMs)
    }

    fun simulateTemperature(vehicleName: String, startTemperature: Double, minTemperature: Double, maxTemperature: Double, durationMs: Long, onUpdateCallback: (Vehicle) -> Unit, onFinnishCallback: () -> Unit) {
        val vehicle = Vehicle(vehicleName, temperature = startTemperature)

        var running = true
        val thread = Thread{
            val rnd = Random()
            while(running && this.running){
                val timestamp = LocalDateTime.now()

                val num = rnd.nextDouble() * 100
                if(rnd.nextBoolean()){
                    vehicle.speed += num

                    if(vehicle.speed > maxTemperature){
                        vehicle.speed -= num * (rnd.nextInt(10) + 2)
                    }
                }
                else{
                    vehicle.speed -= num

                    if(vehicle.speed < minTemperature){
                        vehicle.speed += num * (rnd.nextInt(10) + 2)
                    }
                }

                onUpdateCallback(vehicle);

                val message = MqttMessage(("${timestamp}$${vehicle.location.latitude}$${vehicle.location.longitude}$${vehicle.temperature}").toByteArray())
                mqttClient.publish("VehicleTemperature", message, this, null)

                Thread.sleep(500)
            }
        }
        thread.start();

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                running = false
                thread.join()

                onFinnishCallback()
            }
        }, durationMs)
    }

    fun simulateLocation(vehicleName: String, startLocation: LatLng, endLocation: LatLng, onUpdateCallback: (Vehicle) -> Unit, onFinnishCallback: () -> Unit) {
        val vehicle = Vehicle(vehicleName, location = startLocation)

        val client = MapboxDirections.builder().accessToken(MAPBOX_ACCESS_TOKEN).origin(Point.fromLngLat(startLocation.longitude, startLocation.latitude)).destination(Point.fromLngLat(endLocation.longitude, endLocation.latitude)).build()
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                Thread{
                    if(response.body()?.routes()?.size!! == 0) return@Thread;
                    val route = response.body()?.routes()?.get(0)
                    val geometry = route?.geometry()!!

                    val points = PolylineUtils.decode(geometry, 6);

                    for(i in 0 until points.size - 1){
                        val firstPoint = points[i];
                        val nextPoint = points[i+1];

                        val dist = TurfMeasurement.distance(firstPoint, nextPoint);
                        val dirVec = Point.fromLngLat(firstPoint.longitude() - nextPoint.longitude(), firstPoint.latitude() - nextPoint.latitude());
                        val dirVec1 = Point.fromLngLat(dirVec.longitude() / dist, dirVec.latitude() / dist);

                        var d = 0.0;
                        while(d < dist && running){
                            vehicle.location = LatLng(firstPoint.latitude() - dirVec1.latitude() * d,firstPoint.longitude() - dirVec1.longitude() * d)
                            val timestamp = LocalDateTime.now()

                            //onUpdateCallback(vehicle);

                            val message = MqttMessage(("${timestamp}$${vehicle.location.latitude}$${vehicle.location.longitude}").toByteArray())
                            mqttClient.publish("VehicleLocation", message, this, null)

                            d += 0.005;

                            Thread.sleep(100)
                        }
                    }
                    onFinnishCallback();
                }.start()
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}