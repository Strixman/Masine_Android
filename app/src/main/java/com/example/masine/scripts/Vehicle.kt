package com.example.masine.scripts

import com.mapbox.mapboxsdk.geometry.LatLng

data class Vehicle(val name :String, var temperature: Double = 0.0, var speed: Double = 0.0, var location: LatLng = LatLng(0.0,0.0))