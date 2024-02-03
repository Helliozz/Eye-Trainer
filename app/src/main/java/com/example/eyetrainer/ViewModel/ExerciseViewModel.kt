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
import com.example.eyetrainer.Data.Constants.APP_DEVICE_BLUETOOTH_ADDRESS
import com.example.eyetrainer.Data.Constants.APP_EXERCISES_BASE_LIST
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise


@SuppressLint("MissingPermission")
class ExerciseViewModel : ViewModel() {
    private var isBluetoothAvailable: Boolean? = null

    private lateinit var savedExercise: SingleExercise
    private val exercises = ArrayList(APP_EXERCISES_BASE_LIST)

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val bluetoothDevices: ArrayList<BluetoothDevice> = arrayListOf()

    private var connectionThread: ConnectionThread? = null
    private var connectedThread: ConnectedThread? = null

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
            setBluetoothAvailable(false)
        }
        setBluetoothAvailable(bluetoothAdapter != null)
    }

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
        if (connectedThread != null && connectionThread != null && connectionThread!!.isConnected) {
            connectedThread!!.write(command)
        }
    }

    fun addDevice(device: BluetoothDevice) {
        if (bluetoothDevices.contains(device)) { return }
        bluetoothDevices.add(device)
        Log.d("APP_CHECKER", "Device added: ${device.name} (${device.address}).")
    }

    fun findRelevantDevice() {
        val connectedThreads: ArrayList<ConnectedThread> = arrayListOf()
        bluetoothDevices.forEach {
            if (it.address == APP_DEVICE_BLUETOOTH_ADDRESS) {
                val currentConnectionThread = ConnectionThread(it,
                    successFun = { socket ->
                        val currentConnectedThread = ConnectedThread(socket)
                        currentConnectedThread.start()
                        connectedThreads.add(currentConnectedThread)

                        connectedThread = currentConnectedThread
                        Log.d("APP_CHECKER", "Connection to device ${it.name} (${it.bluetoothClass}) established.")
                    },
                    failFun = {
                        Log.d("APP_CHECKER", "Connection to device ${it.name} (${it.bluetoothClass}) failed.")
                    })
                currentConnectionThread.start()
                connectionThread = currentConnectionThread
            }
        }
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

