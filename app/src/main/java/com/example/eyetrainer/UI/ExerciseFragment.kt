package com.example.eyetrainer.UI

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eyetrainer.Adapter.ChoiceRecyclerViewAdapter
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_MISSING
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.FragmentExerciseBinding

@RequiresApi(Build.VERSION_CODES.S)
class ExerciseFragment : Fragment() {

    private lateinit var itemFun: (SingleExercise)->Unit
    private lateinit var binding: FragmentExerciseBinding
    private val recyclerViewAdapter by lazy { ChoiceRecyclerViewAdapter(itemFun) }
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        itemFun = {
            exerciseViewModel.saveExercise(it)
            requireView().findNavController().navigate(R.id.action_exerciseFragment_to_currentExerciseFragment)
        }

        binding.reminder.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_exerciseFragment_to_reminderFragment)
        }
        recyclerViewAdapter.differ.submitList(exerciseViewModel.getExercises())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = recyclerViewAdapter
        }

        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  ) {
            this.requestPermissions(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                MainActivity.REQUEST_CODE_LOC)
        } else {
            initiateBluetoothSetup()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MainActivity.REQUEST_CODE_LOC -> if (grantResults.isNotEmpty()) {
                for (gr in grantResults) {
                    // Check if request is granted or not
                    if (gr != PackageManager.PERMISSION_GRANTED) {
                        exerciseViewModel.setBluetoothAvailable(false)
                        Toast.makeText(activity!!, APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE, Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                initiateBluetoothSetup()
            }
            else -> return
        }
    }

    private fun initiateBluetoothSetup() {
        if (exerciseViewModel.isBluetoothAvailable() != null) { return }
        exerciseViewModel.createBluetoothAdapter(activity!!)

        if (exerciseViewModel.isBluetoothAvailable()!!) {
            val filter = IntentFilter()
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            registerReceiver(activity!!, receiver, filter, RECEIVER_EXPORTED)
        }
        exerciseViewModel.enableSearch()
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("APP_CHECKER", "Receive event was called:")
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    //btnEnableSearch.setText(android.R.string.stop_search)
                    //pbProgress!!.visibility = View.VISIBLE
                    //setListAdapter(MainActivity.BT_SEARCH)
                    Log.d("APP_CHECKER", "Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    //btnEnableSearch.setText(android.R.string.start_search)
                    //pbProgress!!.visibility = View.GONE
                    Log.d("APP_CHECKER", "Discovery finished")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        exerciseViewModel.addDevice(device)
                        //listAdapter.notifyDataSetChanged()
                    }
                    Log.d("APP_CHECKER", "Device found")
                }
            }
        }
    }

    override fun onDestroy() {
        activity!!.unregisterReceiver(receiver)
        super.onDestroy()
    }
}