package com.example.eyetrainer.ViewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.GridLayout
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
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MAX_CELLS
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_X
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_Y
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_NO_MIRROR
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_PATTERN_DATA_LENGTH
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_SECURITY_CODE_CONDENSED
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_SECURITY_CODE_LENGTH
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.R
import com.example.eyetrainer.Utils.Utils.getCheckedByte
import com.example.eyetrainer.Utils.Utils.getNewExerciseFromData
import kotlin.math.abs


@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission")
class ExerciseViewModel : ViewModel() {
    private var isBluetoothAvailable: Boolean? = null

    private var chosenExercise: SingleExercise? = null
    val exercises = MutableLiveData(ArrayList(APP_EXERCISES_BASE_LIST))

    private lateinit var createdExerciseData: SingleExercise
    private var createdExerciseSaved = false

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

    fun uploadData(exercise: SingleExercise = chosenExercise!!): Boolean {
        if (connectedThread.value != null && connectionThread.value != null && connectionThread.value!!.isConnected) {
            val dataPackage: ArrayList<Byte> = arrayListOf()

            var securityCodeDuplicate = APP_EXERCISE_SECURITY_CODE_CONDENSED
            for (i in 1..APP_EXERCISE_SECURITY_CODE_LENGTH) {
                dataPackage.add(getCheckedByte(securityCodeDuplicate % 2))
                securityCodeDuplicate /= 2
            }

            dataPackage.add(getCheckedByte(exercise.points.size))
            for (i in 0..<APP_EXERCISE_PATTERN_DATA_LENGTH) {
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

    fun getExercises(): ArrayList<ExerciseItemData> {
        val exercisesList = ArrayList(exercises.value!!)
        if (!createdExerciseSaved) {
            val creationItem = SingleExercise(R.drawable.icon_addition, "создать своё")
            exercisesList.add(creationItem)
        }

        val exerciseRecyclerList = arrayListOf<ExerciseItemData>()
        for (i in 0..<exercisesList.size) {
            if (exerciseRecyclerList.isNotEmpty() && exerciseRecyclerList.last().second == exercisesList[i]) {
                continue
            }

            if ((exercisesList.size-1) - i >= 1) {
                exerciseRecyclerList.add(ExerciseItemData(exerciseRecyclerList.size, exercisesList[i], exercisesList[i+1]))
            } else {
                exerciseRecyclerList.add(ExerciseItemData(exerciseRecyclerList.size, exercisesList[i]))
            }
        }
        return exerciseRecyclerList
    }

    fun chooseExercise(singleExercise: SingleExercise) {
        chosenExercise = singleExercise
    }

    fun clearExercise() {
        chosenExercise = null
    }

    fun getExercise(): SingleExercise? {
        return chosenExercise
    }

    fun exerciseCreationCellFunction(row: Int, col: Int, grid: GridLayout, pattern: ArrayList<Pair<Int, Int>>, patternMirroring: Int = APP_EXERCISE_MIRRORING_NO_MIRROR): Boolean {
        pattern.add(Pair(row, col))
        updateExerciseCellByPosition(row, col, grid, pattern)

        for (i in 0..< APP_EXERCISE_MAX_CELLS) {
            for (j in 0..< APP_EXERCISE_MAX_CELLS) {
                if (i == row && j == col) continue
                updateExerciseCellByPosition(i, j, grid, pattern, patternMirroring)
            }
        }
        return grid.getChildAt(pattern[0].first * APP_EXERCISE_MAX_CELLS + pattern[0].second).isEnabled
    }

    private fun updateExerciseCellByPosition(row: Int, col: Int, grid: GridLayout, pattern: ArrayList<Pair<Int, Int>>, patternMirroring: Int = APP_EXERCISE_MIRRORING_NO_MIRROR) {
        val cell = grid.getChildAt(row * APP_EXERCISE_MAX_CELLS + col)
        val selectedRow = pattern.last().first
        val selectedCol = pattern.last().second

        if ( (abs(row - selectedRow) >= 2 || abs(col - selectedCol) >= 2) || Pair(row, col) == pattern.last() || pattern.size >= APP_EXERCISE_PATTERN_DATA_LENGTH || (patternMirroring == APP_EXERCISE_MIRRORING_AXIS_X && row >= APP_EXERCISE_MAX_CELLS / 2) || (patternMirroring == APP_EXERCISE_MIRRORING_AXIS_Y && col >= APP_EXERCISE_MAX_CELLS / 2) ) {
            cell.setBackgroundResource( when(pattern.contains(Pair(row, col))) {
                true -> if (Pair(row, col) == pattern[0]) R.drawable.background_exercise_cell_first_nonactive else R.drawable.background_exercise_cell_chosen_nonactive
                false -> R.drawable.background_exercise_cell_nonactive
            })
            cell.isEnabled = false
        } else {
            cell.setBackgroundResource( when(pattern.contains(Pair(row, col))) {
                true -> if (Pair(row, col) == pattern[0]) R.drawable.background_exercise_cell_first_active else R.drawable.background_exercise_cell_chosen_active
                false -> R.drawable.background_exercise_cell_active
            })
            cell.isEnabled = true
        }
    }

    fun saveCreatedExercise(pattern: ArrayList<Pair<Int, Int>>, arrow: Boolean, mirroring: Int, grid: GridLayout) {
        createdExerciseData = getNewExerciseFromData(pattern, arrow, mirroring, grid)

        // Currently only one custom exercise is allowed
        val exercisesVal = exercises.value!!
        if (!createdExerciseSaved) {
            exercisesVal.add(createdExerciseData)
            createdExerciseSaved = true
        } else {
            exercisesVal[exercisesVal.lastIndex] = createdExerciseData
        }
        exercises.postValue(exercises.value)
    }
}

