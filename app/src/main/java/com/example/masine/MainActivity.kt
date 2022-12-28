package com.example.masine

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.masine.databinding.ActivityMainBinding
import com.example.masine.scripts.Application
import com.example.masine.ui.main.MainFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding;
    private lateinit var app: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as Application;

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container, MainFragment.newInstance()).commitNow()
        }

        /*val mqttClient = MqttAndroidClient(this, "tcp://192.168.1.132:3030", "car1")

        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.isCleanSession = false

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.d("test", "success");
                requestRoute(ACCESS_TOKEN, Point.fromLngLat(15.645925,46.557301), Point.fromLngLat(15.641511, 46.558276)) { points: List<Point> ->
                    Thread{
                        for(point in points){

                            val message = MqttMessage(point.coordinates().toString().toByteArray())
                            mqttClient.publish("position", message, this, null);
                            Thread.sleep(100);
                        }
                    }.start()
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Log.d("test", exception.message.toString());
            }
        })*/
    }

    override fun onDestroy() {
        app.onDestroy();
        super.onDestroy()
    }
    /*fun requestRoute(
        accessToken: String,
        start: Point,
        end: Point,
        callback: (List<Point>) -> Unit
    ) {
        val client = MapboxDirections.builder()
            .accessToken(accessToken)
            .origin(start)
            .destination(end)
            .build()
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                val route = response.body()?.routes()?.get(0)
                val geometry = route?.geometry()!!

                val points = PolylineUtils.decode(geometry, 6);

                val newPoints = mutableListOf<Point>()
                for(i in 0 until points.size - 1){
                    val firstPoint = points[i];
                    val nextPoint = points[i+1];

                    val dist = TurfMeasurement.distance(firstPoint, nextPoint);
                    val dirVec = Point.fromLngLat(firstPoint.longitude() - nextPoint.longitude(), firstPoint.latitude() - nextPoint.latitude());
                    val dirVec1 = Point.fromLngLat(dirVec.longitude() / dist, dirVec.latitude() / dist);

                    var d = 0.0;
                    while(d < dist){
                        newPoints.add(Point.fromLngLat(firstPoint.longitude() - dirVec1.longitude() * d,firstPoint.latitude() - dirVec1.latitude() * d))
                        d += 0.005;
                    }
                }

                callback(newPoints);
            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                // Handle failure
            }
        })
    }*/
}

