package com.example.eyetrainer.UI

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
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.ActivityMainBinding

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val exerciseViewModel: ExerciseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bluetooth_menu, menu)

        exerciseViewModel.performTimerEvent({
            exerciseViewModel.bluetoothConnection.observe(this) { thread ->
                val bluetoothItem: View? = findViewById(R.id.bluetooth)
                Log.d("APP_DEBUGGER", "Attempt to set the color.")
                bluetoothItem?.apply {
                    Log.d("APP_DEBUGGER", "Color set called.")
                    this.setBackgroundColor(
                        getColor(when(thread != null && thread.checkConnection()) {
                            true -> R.color.green
                            false -> R.color.red
                        }))
                }
            }
        }, 100L)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bluetooth -> {
                if (exerciseViewModel.isBluetoothAvailable() != null) {
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

    companion object {
        const val REQUEST_CODE_LOC_BLUETOOTH = 1
        const val REQUEST_CODE_LOC_NOTIFICATION = 2
    }
}