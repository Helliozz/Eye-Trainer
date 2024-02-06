package com.example.eyetrainer.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.eyetrainer.Data.Constants
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.FragmentCurrentExerciseBinding


class CurrentExerciseFragment : Fragment() {

    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private lateinit var binding: FragmentCurrentExerciseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentExercise = exerciseViewModel.getExercise()
        binding.currentExerciseImage.setImageResource(currentExercise.image)
        binding.currentExerciseName.text = currentExercise.name

        binding.back.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_currentExerciseFragment_to_exerciseFragment)
        }
        binding.send.setOnClickListener {
            if (exerciseViewModel.isBluetoothAvailable()!! && exerciseViewModel.checkDeviceValidity()) {
                exerciseViewModel.uploadData()
            } else {
                Toast.makeText(activity!!, "$APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND\n$APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE", Toast.LENGTH_SHORT).show()
            }
        }
    }
}