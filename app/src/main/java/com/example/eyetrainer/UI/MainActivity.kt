package com.example.eyetrainer.UI

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.eyetrainer.Data.Constants.APP_DEVICE_BLUETOOTH_ADDRESS
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND
import com.example.eyetrainer.R
import com.example.eyetrainer.Utils.Utils.performTimerEvent
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val exerciseViewModel: ExerciseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(receiver)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bluetooth_menu, menu)

        performTimerEvent({
            (findViewById<View>(R.id.bluetooth)).apply {
                this.setBackgroundColor(getColor(R.color.red))
            }

            exerciseViewModel.connectedThread.observe(this) {
                editBluetoothIcon()
            }
            exerciseViewModel.connectionThread.observe(this) { thread ->
                editBluetoothIcon()
            }
        }, 100L)

        return super.onCreateOptionsMenu(menu)
    }

    private fun editBluetoothIcon() {
        (findViewById<View>(R.id.bluetooth)).apply {
            Log.d("APP_DEBUGGER", "Color set called.")
            this.setBackgroundColor(
                getColor(when(exerciseViewModel.connectionThread.value != null && exerciseViewModel.connectionThread.value!!.isConnected && exerciseViewModel.connectedThread.value != null && exerciseViewModel.connectedThread.value!!.checkConnection()) {
                    true -> R.color.green
                    false -> R.color.red
                }))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bluetooth -> {
                val currFragment = supportFragmentManager.primaryNavigationFragment!!.childFragmentManager.fragments.last()
                if (currFragment::class.java == ExerciseFragment::class.java || currFragment::class.java == CurrentExerciseFragment::class.java) {
                    exerciseViewModel.updateBluetoothAdapter(this)
                    if (exerciseViewModel.isBluetoothAvailable()!!) {
                        exerciseViewModel.enableSearch()
                    } else {
                        Toast.makeText(this, APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("APP_CHECKER", "Receive event was called:")
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("APP_CHECKER", "Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (!exerciseViewModel.checkDeviceValidity()) {
                        Toast.makeText(this@MainActivity, APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND, Toast.LENGTH_SHORT).show()
                    }
                    Log.d("APP_CHECKER", "Discovery finished")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.address == APP_DEVICE_BLUETOOTH_ADDRESS) {
                        exerciseViewModel.addDevice(device)
                        exerciseViewModel.connectToDevice(this@MainActivity)
                        exerciseViewModel.disableSearch()
                        Log.d("APP_CHECKER", "Device found")
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_LOC_NOTIFICATION_MAIN_THREAD = 1
        const val REQUEST_CODE_LOC_NOTIFICATION = 2
        const val REQUEST_CODE_LOC_BLUETOOTH = 4
    }
}