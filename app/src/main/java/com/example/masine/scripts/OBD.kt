package com.example.masine.scripts

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.github.pires.obd.commands.SpeedCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.github.pires.obd.commands.protocol.EchoOffCommand
import com.github.pires.obd.commands.protocol.LineFeedOffCommand
import com.github.pires.obd.commands.protocol.SelectProtocolCommand
import com.github.pires.obd.commands.protocol.TimeoutCommand
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand
import com.github.pires.obd.enums.ObdProtocols
import java.io.IOException
import java.time.LocalDateTime
import java.util.*


@SuppressLint("MissingPermission")
abstract class OBD(var vehicleName : String, locationManager: LocationManager) {
    private val DEVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"

    private var socket: BluetoothSocket? = null
    var connected = false

    private var thread : Thread? = null
    var running = false

    private val speedCommand = SpeedCommand()
    private val temperatureCommand = EngineCoolantTemperatureCommand()
    private val rpmCommand = RPMCommand()
    private var location : Location

    init {
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,0F){
            location = it
        }

        speedCommand.useImperialUnits()
    }

    fun connect(device: BluetoothDevice) : Boolean {
        if(connected) return false
        try{
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(DEVICE_UUID))
            socket!!.connect()
        }
        catch (_ : Exception){
            return false
        }
        connected = true
        return true
    }

    fun start(UICallback: (Float, Float, Int) -> Unit, StopUICallback: () -> Unit, StartUICallback : () -> Unit) : Boolean {
        if(!connected || running || socket == null || !socket!!.isConnected) return false
        thread = Thread{
            onStart(location)
            StartUICallback()
            try{
                EchoOffCommand().run(socket!!.inputStream, socket!!.outputStream)
                LineFeedOffCommand().run(socket!!.inputStream, socket!!.outputStream)
                TimeoutCommand(125).run(socket!!.inputStream, socket!!.outputStream)
                SelectProtocolCommand(ObdProtocols.AUTO).run(socket!!.inputStream, socket!!.outputStream)
            }
            catch (_: Exception){
                connected = false
                running = false
                StopUICallback()
                return@Thread
            }
            while(running && socket!!.isConnected){
                try{
                    speedCommand.run(socket!!.inputStream, socket!!.outputStream)
                    temperatureCommand.run(socket!!.inputStream, socket!!.outputStream)
                    rpmCommand.run(socket!!.inputStream, socket!!.outputStream)
                }
                catch (_ : IOException){
                    connected = false
                    running = false
                    break;
                }
                catch (_ : Exception){
                    connected = socket!!.isConnected
                    running = false
                    break;
                }

                onUpdate(LocalDateTime.now(), location, speedCommand.imperialSpeed * 1.60934f, temperatureCommand.temperature, rpmCommand.rpm)
                UICallback(speedCommand.imperialSpeed * 1.60934f, temperatureCommand.temperature, rpmCommand.rpm)

                try{
                    Thread.sleep(500)
                }
                catch (_ : InterruptedException){
                    break
                }
            }
            StopUICallback()
            onStop(location)
        }

        running = true
        thread!!.start()

        return true
    }
    fun stop() : Boolean {
        if(!connected || !running || thread == null) return false

        running = false
        thread!!.interrupt()
        thread!!.join()

        return true
    }

    abstract fun onUpdate(time: LocalDateTime, location: Location, speed : Float, temperature: Float, rpm : Int)
    abstract fun onStart(location: Location)
    abstract fun onStop(location: Location)

    fun setName(vehicleName: String){
        this.vehicleName = vehicleName
    }
}