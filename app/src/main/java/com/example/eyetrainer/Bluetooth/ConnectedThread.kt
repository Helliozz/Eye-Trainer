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

    fun write(number: Int) {
        if (outputStream != null) {
            val byteArray: ArrayList<Byte> = arrayListOf()
            byteArray.add(number.toByte())

            try {
                outputStream.write(byteArray.toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                throw(RuntimeException("Failed attempt to write to the device."))
            }
        }
    }

    fun writePackage(dataPackage: ByteArray) {
        if (outputStream != null) {
            try {
                outputStream.write(dataPackage)
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                throw(RuntimeException("Failed attempt to write to the device."))
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