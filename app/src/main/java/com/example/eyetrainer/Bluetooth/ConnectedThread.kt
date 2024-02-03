package com.example.eyetrainer.Bluetooth

import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(bluetoothSocket: BluetoothSocket) : Thread() {
    private val inputStream: InputStream?
    private val outputStream: OutputStream?

    init {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = bluetoothSocket.inputStream
            outputStream = bluetoothSocket.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }
        this.inputStream = inputStream
        this.outputStream = outputStream
    }

    override fun run() {}
    fun write(command: String) {
        val bytes = command.toByteArray()
        if (outputStream != null) {
            try {
                outputStream.write(bytes)
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun cancel() {
        try {
            inputStream!!.close()
            outputStream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}