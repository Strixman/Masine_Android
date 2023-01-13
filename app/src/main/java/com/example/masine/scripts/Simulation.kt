package com.example.masine.scripts

import android.location.Location
import android.util.Log
import com.github.pires.obd.commands.SpeedCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.turf.TurfMeasurement
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*

val MAPBOX_ACCESS_TOKEN = "pk.eyJ1Ijoic3RyaXhtYW4xMCIsImEiOiJjbGM1ZDVhMHU0cGpsM3drZWR3bGdib2VrIn0.J_EE1P7EpgcEOGT_EPXYvA"

abstract class Simulation(val vehicleName: String, private val startLocation: LatLng, private val endLocation: LatLng, val minSpeed: Float,val maxSpeed: Float,val minTemperature: Float,val maxTemperature: Float){
    var speed = minSpeed
    var temperature = minTemperature
    var rpm = speed * 60f
    var location = startLocation

    var simulating = false
    private lateinit var thread: Thread

    var UICallback : (() -> Unit)? = null
    var StopUICallback : (() -> Unit)? = null
    var StartUICallback : (() -> Unit)? = null

    fun start(){
        if(simulating) return
        simulating = true

        val client = MapboxDirections.builder().accessToken(MAPBOX_ACCESS_TOKEN).origin(Point.fromLngLat(startLocation.longitude, startLocation.latitude)).destination(Point.fromLngLat(endLocation.longitude, endLocation.latitude)).build()
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                thread = Thread{
                    onStart(location)
                    StartUICallback?.let { it() }

                    val rng = Random()
                    if(response.body()?.routes()?.size!! > 0) {
                        val route = response.body()!!.routes()[0]

                        val points = PolylineUtils.decode(route.geometry()!!, 6);

                        loop@ for(i in 0 until points.size - 1){
                            val firstPoint = points[i];
                            val nextPoint = points[i+1];

                            val dist = TurfMeasurement.distance(firstPoint, nextPoint);
                            val dirVec = Point.fromLngLat(firstPoint.longitude() - nextPoint.longitude(), firstPoint.latitude() - nextPoint.latitude());
                            val dirVec1 = Point.fromLngLat(dirVec.longitude() / dist, dirVec.latitude() / dist);

                            var d = 0.0;
                            while(simulating && d < dist){
                                location = LatLng(firstPoint.latitude() - dirVec1.latitude() * d,firstPoint.longitude() - dirVec1.longitude() * d)

                                if(speed < maxSpeed / 2f || rng.nextBoolean()){
                                    speed += rng.nextFloat() * 3f
                                }
                                else{
                                    speed -= rng.nextFloat() * 3f
                                }
                                if(speed < minSpeed) speed = minSpeed
                                else if(speed > maxSpeed) speed = maxSpeed

                                if(temperature < maxTemperature / 2f ||rng.nextBoolean()){
                                    temperature += rng.nextFloat()
                                }
                                else{
                                    temperature -= rng.nextFloat()
                                }
                                if(temperature < minTemperature) temperature = minTemperature
                                else if(temperature > maxTemperature) temperature = maxTemperature

                                rpm = speed * 60

                                onUpdate(LocalDateTime.now(), location, speed, temperature, rpm)
                                UICallback?.let { it() }

                                try{
                                    Thread.sleep(200)
                                }
                                catch (_:InterruptedException){
                                    break@loop
                                }

                                d += 0.01
                            }
                        }
                    }

                    onFinnish(location)
                    StopUICallback?.let { it() }
                    simulating = false

                }
                thread.start()
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                onFinnish(location)
                StopUICallback?.let { it() }
                simulating = false
            }
        })
    }
    fun stop(){
        if(!simulating) return
        thread.interrupt()
        thread.join()
    }

    abstract fun onUpdate(timestamp: LocalDateTime, location: LatLng, speed : Float, temperature: Float, rpm : Float)
    abstract fun onFinnish(location: LatLng)
    abstract fun onStart(location: LatLng)
}