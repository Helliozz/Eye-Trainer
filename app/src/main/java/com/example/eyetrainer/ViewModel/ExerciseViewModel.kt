package com.example.eyetrainer.ViewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eyetrainer.Bluetooth.ConnectedThread
import com.example.eyetrainer.Bluetooth.ConnectionThread
import com.example.eyetrainer.Data.Constants.APP_DEVICE_BLUETOOTH_ADDRESS
import com.example.eyetrainer.Data.Constants.APP_EXERCISES_BASE_LIST
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_DATAPACKAGE_TOTAL_SIZE
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_SECURITY_CODE_CONDENSED
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_SECURITY_CODE_LENGTH
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission")
class ExerciseViewModel : ViewModel() {
    private var isBluetoothAvailable: Boolean? = null

    private lateinit var savedExercise: SingleExercise
    private val exercises = ArrayList(APP_EXERCISES_BASE_LIST)

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothDevice: BluetoothDevice? = null

    var connectionThread: MutableLiveData<ConnectionThread?> = MutableLiveData(null)
    var connectedThread: MutableLiveData<ConnectedThread?> = MutableLiveData(null)

    val dataCanBeSent: MutableLiveData<Boolean> = MutableLiveData(true)

    fun setBluetoothAvailable(available: Boolean) {
        isBluetoothAvailable = available
    }

    fun isBluetoothAvailable(): Boolean? {
        return isBluetoothAvailable
    }

    fun updateBluetoothAdapter(context: Context) {
        val bluetoothManager: BluetoothManager? = ContextCompat.getSystemService(context, BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter
        setBluetoothAvailable(bluetoothAdapter != null && bluetoothAdapter!!.isEnabled)
    }

    fun enableSearch() {
        if (!bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.startDiscovery()
            Log.d("APP_CHECKER", "Discovery was started!")
        }
    }

    fun disableSearch() {
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
            Log.d("APP_CHECKER", "Discovery was canceled!")
        }
    }

    fun addDevice(device: BluetoothDevice) {
        if (device.address != APP_DEVICE_BLUETOOTH_ADDRESS) { throw(Exception("Attempt to save a device with an invalid address.")) }
        bluetoothDevice = device
        Log.d("APP_CHECKER", "Device added: ${device.name} (${device.address}).")
    }

    fun checkDeviceValidity(): Boolean {
        return (bluetoothDevice != null && bluetoothDevice?.address == APP_DEVICE_BLUETOOTH_ADDRESS)
    }

    fun connectToDevice(context: Context) {
        val connectedThreads: ArrayList<ConnectedThread> = arrayListOf()
        if (bluetoothDevice == null || bluetoothDevice?.address != APP_DEVICE_BLUETOOTH_ADDRESS) {
            throw(Exception("Attempt to connect to an invalid device."))
        }

        val currentConnectionThread = ConnectionThread(bluetoothDevice!!,
            successFun = { socket ->
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT).show()
                }

                val currentConnectedThread = ConnectedThread(socket)
                currentConnectedThread.start()
                connectedThreads.add(currentConnectedThread)

                connectedThread.value = currentConnectedThread
                Log.d("APP_CHECKER", "Connection to device ${bluetoothDevice!!.name} (${bluetoothDevice!!.bluetoothClass}) established.")
            },
            failFun = {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED, Toast.LENGTH_SHORT).show()
                }
                connectedThread.value = null
                Log.d("APP_CHECKER", "Connection to device ${bluetoothDevice!!.name} (${bluetoothDevice!!.bluetoothClass}) failed.")
            })
        currentConnectionThread.start()
        connectionThread.value = currentConnectionThread
    }

    fun uploadData(exercise: SingleExercise = savedExercise): Boolean {
        if (connectedThread.value != null && connectionThread.value != null && connectionThread.value!!.isConnected) {
            val dataPackage: ArrayList<Byte> = arrayListOf()

            var securityCodeDuplicate = APP_EXERCISE_SECURITY_CODE_CONDENSED
            for (i in 1..APP_EXERCISE_SECURITY_CODE_LENGTH) {
                dataPackage.add(getCheckedByte(securityCodeDuplicate % 2))
                securityCodeDuplicate /= 2
            }

            dataPackage.add(getCheckedByte(exercise.points.size))
            for (i in 0..63) {
                if (exercise.points.size > i) {
                    dataPackage.add(getCheckedByte(exercise.points[i].first))
                    dataPackage.add(getCheckedByte(exercise.points[i].second))
                } else {
                    dataPackage.add(0)
                    dataPackage.add(0)
                }
            }

            dataPackage.add(exercise.mirroring.toByte())
            dataPackage.add((when (exercise.shouldDrawArrow) {
                true -> 1
                false -> 0
            }).toByte())

            securityCodeDuplicate = APP_EXERCISE_SECURITY_CODE_CONDENSED
            for (i in 1..APP_EXERCISE_SECURITY_CODE_LENGTH) {
                dataPackage.add(0)
            }
            for (i in 1..APP_EXERCISE_SECURITY_CODE_LENGTH) {
                dataPackage[APP_EXERCISE_DATAPACKAGE_TOTAL_SIZE - i] = getCheckedByte(securityCodeDuplicate % 2)
                securityCodeDuplicate /= 2
            }

            return connectedThread.value!!.writePackage(dataPackage.toByteArray())
        } else {
            return false
        }
    }

    fun performTimerEvent(timerFun: () -> Unit, time: Long) {
        val eventTimer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    timerFun()
                }
            }
        }
        eventTimer.schedule(timerTask, time)
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

    private fun getCheckedByte(command: Int): Byte {
        val bytes = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(command).array()
        if (bytes[0].toInt() != 0 || bytes[1].toInt() != 0 || bytes[2].toInt() != 0) {
            Log.d("APP_CHECKER", "Byte1 = ${bytes[0].toInt()}, Byte2 = ${bytes[1].toInt()},  Byte3 = ${bytes[2].toInt()}, Byte4 = ${bytes[3].toInt()}")
            throw(RuntimeException("This number is too big to send."))
        }
        return bytes[3]
    }
}

