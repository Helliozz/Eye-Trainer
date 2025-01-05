package com.example.eyetrainer.ViewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
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
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_CELL_STATE_ACTIVE
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_CELL_STATE_CHOSEN
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_CELL_STATE_VITAL
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_DATAPACKAGE_TOTAL_SIZE
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_GRID_MAX_CELL_COUNT
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_X
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_Y
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_PATTERN_DATA_LENGTH
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_SECURITY_CODE_CONDENSED
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_SECURITY_CODE_LENGTH
import com.example.eyetrainer.Data.Constants.APP_PREFERENCES
import com.example.eyetrainer.Data.Constants.APP_PREFERENCES_SAVED_EXERCISE
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.R
import com.example.eyetrainer.Utils.Utils.getCheckedByte
import com.example.eyetrainer.Utils.Utils.getNewExerciseFromData
import com.google.gson.Gson
import kotlin.math.abs
import kotlin.math.round


@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission")
class ExerciseViewModel : ViewModel() {
    private var isBluetoothAvailable: Boolean? = null

    private var chosenExercise: SingleExercise? = null
    val exercises = MutableLiveData(ArrayList(APP_EXERCISES_BASE_LIST))

    private lateinit var createdExerciseData: SingleExercise

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
        if (canCreateAdditionalExercises()) {
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

    fun getExerciseGridSideLength(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val pixelWidth = if (displayMetrics.widthPixels < displayMetrics.heightPixels) displayMetrics.widthPixels else displayMetrics.heightPixels
        return round(pixelWidth * 0.75 / APP_EXERCISE_GRID_MAX_CELL_COUNT).toInt() * APP_EXERCISE_GRID_MAX_CELL_COUNT
    }

    fun updateExerciseCellGrid(grid: GridLayout, pattern: ArrayList<Pair<Int, Int>>, patternMirroring: Int, setActive: (cell: View, state: Int)->Unit) {
        if (pattern.isEmpty()) {
            throw(RuntimeException("Unable to update the grid because it's empty."))
        }

        val selectedRow = pattern.last().first
        val selectedCol = pattern.last().second
        val selectedCell = grid.getChildAt(selectedRow * APP_EXERCISE_GRID_MAX_CELL_COUNT + selectedCol)
        setActive(selectedCell, APP_EXERCISE_CELL_STATE_CHOSEN + APP_EXERCISE_CELL_STATE_VITAL)

        for (y in 0..< APP_EXERCISE_GRID_MAX_CELL_COUNT) {
            for (x in 0..< APP_EXERCISE_GRID_MAX_CELL_COUNT) {
                if (y == selectedRow && x == selectedCol) continue

                var cellState = APP_EXERCISE_CELL_STATE_ACTIVE
                if ( pattern.size > APP_EXERCISE_PATTERN_DATA_LENGTH || (abs(y - selectedRow) >= 2 || abs(x - selectedCol) >= 2) || Pair(y, x) == pattern.last() || (patternMirroring == APP_EXERCISE_MIRRORING_AXIS_X && y >= APP_EXERCISE_GRID_MAX_CELL_COUNT / 2) || (patternMirroring == APP_EXERCISE_MIRRORING_AXIS_Y && x >= APP_EXERCISE_GRID_MAX_CELL_COUNT / 2) ) {
                    cellState -= APP_EXERCISE_CELL_STATE_ACTIVE
                }
                if (pattern.contains(Pair(y, x))) {
                    cellState += APP_EXERCISE_CELL_STATE_CHOSEN
                    if (Pair(y, x) == pattern.first() || Pair(y, x) == pattern.last()) {
                        cellState += APP_EXERCISE_CELL_STATE_VITAL
                    }
                }

                val cell = grid.getChildAt(y * APP_EXERCISE_GRID_MAX_CELL_COUNT + x)
                setActive(cell, cellState)
            }
        }
    }

    fun canCreateAdditionalExercises(): Boolean {
        // Currently only one custom exercise is allowed
        return exercises.value!!.size <= APP_EXERCISES_BASE_LIST.size
    }

    fun saveCreatedExercise(context: Context, pattern: ArrayList<Pair<Int, Int>>, arrow: Boolean, mirroring: Int, grid: GridLayout) {
        createdExerciseData = getNewExerciseFromData(pattern, arrow, mirroring, grid.width, grid.height)

        val exercisesVal = exercises.value!!
        if (canCreateAdditionalExercises()) {
            exercisesVal.add(createdExerciseData)
        } else {
            exercisesVal[exercisesVal.lastIndex] = createdExerciseData
        }
        exercises.postValue(exercisesVal)

        val sPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val resultString: String = Gson().toJson(SingleExercise(createdExerciseData.image, createdExerciseData.name, createdExerciseData.points, createdExerciseData.shouldDrawArrow, createdExerciseData.mirroring, null))
        sPreferences.edit().putString(APP_PREFERENCES_SAVED_EXERCISE, resultString).apply()
        Log.d("APP_DEBUGGER_JSON", resultString)
    }

    fun deleteCreatedExercise(context: Context) {
        val exercisesVal = exercises.value!!
        if (!canCreateAdditionalExercises()) {
            exercisesVal.removeAt(exercisesVal.lastIndex)
            exercises.postValue(exercisesVal)

            val sPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            sPreferences.edit().remove(APP_PREFERENCES_SAVED_EXERCISE).apply()
        } else {
            Log.d("APP_DEBUGGER", "Delete-exercise-call despite its unnecessity.")
        }
    }

    fun restoreCreatedExercise(context: Context) {
        if (!canCreateAdditionalExercises()) {
            Log.d("APP_DEBUGGER", "Restore-exercise-call despite its unnecessity.")
        } else {
            val sPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            val resultString = sPreferences.getString(APP_PREFERENCES_SAVED_EXERCISE, null)
            Log.d("APP_DEBUGGER_JSON", "Exercise restore string: $resultString")

            if (resultString != null) {
                val exerciseData = Gson().fromJson(resultString, SingleExercise::class.java)
                createdExerciseData = if (exerciseData.image == -1) {
                    val gridSideLength = getExerciseGridSideLength(context)
                    getNewExerciseFromData(exerciseData.points, exerciseData.shouldDrawArrow, exerciseData.mirroring, gridSideLength, gridSideLength)
                } else {
                    exerciseData
                }

                val exercisesVal = exercises.value!!
                exercisesVal.add(createdExerciseData)
                exercises.postValue(exercisesVal)
            }
        }
    }
}