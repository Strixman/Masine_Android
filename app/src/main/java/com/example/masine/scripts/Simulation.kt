package com.example.masine.scripts

import android.util.Log
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

abstract class Simulation(val vehicleName: String,var startLocation: LatLng,var endLocation: LatLng){
    val vehicle = Vehicle(vehicleName, startLocation, 90.0, 50.0)

    var simulating = false
    private lateinit var thread: Thread
    var notifyOnChange : (() -> Unit)? = null
    var notifyOnFinnish : (() -> Unit)? = null

    fun start(){
        if(simulating) return
        simulating = true

        val client = MapboxDirections.builder().accessToken(MAPBOX_ACCESS_TOKEN).origin(Point.fromLngLat(startLocation.longitude, startLocation.latitude)).destination(Point.fromLngLat(endLocation.longitude, endLocation.latitude)).build()
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                thread = Thread{
                    onStart()

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
                                vehicle.location = LatLng(firstPoint.latitude() - dirVec1.latitude() * d,firstPoint.longitude() - dirVec1.longitude() * d)

                                val speed = 0.01 //TODO adjust with real speed
                                val num = rng.nextDouble() * 3
                                if(rng.nextBoolean()) vehicle.speed += num
                                else vehicle.speed -= num

                                d += speed

                                onUpdate(LocalDateTime.now())

                                try{
                                    Thread.sleep(200)
                                }
                                catch (_:InterruptedException){
                                    break@loop
                                }

                            }
                        }
                    }

                    onFinnish()
                    simulating = false

                }
                thread.start()
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                onFinnish()
                simulating = false
            }
        })
    }
    fun stop(){
        if(!simulating) return
        thread.interrupt()
        thread.join()
    }

    abstract fun onUpdate(timestamp: LocalDateTime)
    abstract fun onFinnish()
    abstract fun onStart()
}