package com.example.eyetrainer.Bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException

class ConnectionThread(private val device: BluetoothDevice, private val failFun: ()->Unit, private val successFun: ()->Unit) : Thread() {
    private var bluetoothSocket: BluetoothSocket? = null
    private var success = false

    init {
        try {
            val method = device::class.members.single {it.name == "createRfcommSocket"}
            bluetoothSocket = method.call(device, 1) as BluetoothSocket
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    override fun run() {
        try {
            bluetoothSocket!!.connect()
            success = true
        } catch (e: IOException) {
            e.printStackTrace()
            /*UiThreadStatement.runOnUiThread(Runnable {
                Toast.makeText(
                    this@MainActivity,
                    "Не могу соединиться!",
                    Toast.LENGTH_SHORT
                ).show()
            })*/
            failFun()
            cancel()
        }
        if (success) {
            //connectedThread = ConnectedThread(bluetoothSocket)
            //connectedThread!!.start()
            successFun()
        }
    }

    val isConnected: Boolean
        get() = bluetoothSocket!!.isConnected

    fun cancel() {
        try {
            bluetoothSocket!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}