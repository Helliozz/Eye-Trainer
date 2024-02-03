package com.example.eyetrainer.ViewModel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.eyetrainer.Bluetooth.ConnectedThread
import com.example.eyetrainer.Bluetooth.ConnectionThread
import com.example.eyetrainer.Data.Constants
import com.example.eyetrainer.Data.Constants.APP_EXERCISES_BASE_LIST
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise


class ExerciseViewModel : ViewModel() {
    private var isBluetoothAvailable: Boolean? = null

    private lateinit var savedExercise: SingleExercise
    private val exercises = ArrayList(APP_EXERCISES_BASE_LIST)

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val bluetoothDevices: ArrayList<BluetoothDevice> = arrayListOf()

    private val connectionThread: ConnectionThread? = null
    private val connectedThread: ConnectedThread? = null

    fun setBluetoothAvailable(available: Boolean) {
        isBluetoothAvailable = available
    }

    fun isBluetoothAvailable(): Boolean? {
        return isBluetoothAvailable
    }

    fun createBluetoothAdapter(context: Context) {
        val bluetoothManager: BluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)!!
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(context, Constants.APP_TOAST_BLUETOOTH_MISSING + "\n" + Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE, Toast.LENGTH_SHORT).show()
            setBluetoothAvailable(false)
        }
        setBluetoothAvailable(bluetoothAdapter != null)
    }

    @SuppressLint("MissingPermission")
    fun enableSearch(/*context: Context*/) {
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        } else {
            //accessLocationPermission(context)
            bluetoothAdapter!!.startDiscovery()
            Log.d("APP_CHECKER", "Discovery was started!")
        }
    }

    fun uploadData(command: String) {
        if (connectedThread != null && connectionThread!!.isConnected) {
            connectedThread.write(command)
        }
    }

    @SuppressLint("MissingPermission")
    fun addDevice(device: BluetoothDevice) {
        bluetoothDevices.add(device)
        Log.d("APP_CHECKER", "Device added.")
    }

    fun getExercises(): List<ExerciseItemData> {
        return exercises
    }

    fun saveExercise(singleExercise: SingleExercise) {
        savedExercise = singleExercise
    }

    fun getExercise(): SingleExercise {
        return savedExercise
    }



    /*private fun accessLocationPermission(context: Context) {
        val accessCoarseLocation: Int =
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val accessFineLocation: Int =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val listRequestPermission: MutableList<String> = ArrayList()
        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!listRequestPermission.isEmpty()) {
            val strRequestPermission = listRequestPermission.toTypedArray()
            (context as Activity).requestPermissions(strRequestPermission, MainActivity.REQUEST_CODE_LOC)
        }
    }*/
}

