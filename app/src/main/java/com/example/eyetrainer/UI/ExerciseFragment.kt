package com.example.eyetrainer.UI

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eyetrainer.Adapter.ChoiceRecyclerViewAdapter
import com.example.eyetrainer.Data.Constants.APP_BLUETOOTH_PERMISSIONS_LIST
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_NOT_AVAILABLE
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.R
import com.example.eyetrainer.Utils.Utils.performTimerEvent
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.FragmentExerciseBinding

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.S)
class ExerciseFragment : Fragment() {
    private lateinit var itemFun: (SingleExercise)->Unit
    private lateinit var createFun: ()->Unit
    private lateinit var binding: FragmentExerciseBinding
    private val recyclerViewAdapter by lazy { ChoiceRecyclerViewAdapter(itemFun, createFun) }
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        itemFun = {
            exerciseViewModel.chooseExercise(it)
            requireView().findNavController().navigate(R.id.action_exerciseFragment_to_currentExerciseFragment)
        }
        createFun = {
            requireView().findNavController().navigate(R.id.action_exerciseFragment_to_createExerciseFragment)
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

        exerciseViewModel.exercises.observe(this) {
            recyclerViewAdapter.differ.submitList(exerciseViewModel.getExercises())
            performTimerEvent({
                recyclerViewAdapter.notifyDataSetChanged()
            }, 50L)
        }

        for (permission: String in APP_BLUETOOTH_PERMISSIONS_LIST) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(APP_BLUETOOTH_PERMISSIONS_LIST.toTypedArray(), MainActivity.REQUEST_CODE_LOC_BLUETOOTH)
                return
            }
        }
        initiateBluetoothSetup()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MainActivity.REQUEST_CODE_LOC_BLUETOOTH -> if (grantResults.isNotEmpty()) {
                for (gr in grantResults) {
                    // Check if request is granted or not
                    if (gr != PackageManager.PERMISSION_GRANTED) {
                        exerciseViewModel.setBluetoothAvailable(false)
                        Toast.makeText(requireActivity(), APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE, Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                initiateBluetoothSetup()
            }
            else -> return
        }
    }

    private fun initiateBluetoothSetup() {
        exerciseViewModel.updateBluetoothAdapter(requireActivity())

        if (exerciseViewModel.isBluetoothAvailable()!!) {
            exerciseViewModel.enableSearch()
        } else {
            Toast.makeText(requireActivity(), "$APP_TOAST_BLUETOOTH_NOT_AVAILABLE\n$APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (exerciseViewModel.isBluetoothAvailable() == true) {
            exerciseViewModel.disableSearch()
        }
        exerciseViewModel.exercises.removeObservers(this)
        super.onDestroy()
    }
}