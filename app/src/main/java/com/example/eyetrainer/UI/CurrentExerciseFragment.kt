package com.example.eyetrainer.UI

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE
import com.example.eyetrainer.Data.Constants.APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND
import com.example.eyetrainer.R
import com.example.eyetrainer.Utils.Utils.applyImageFromExerciseItem
import com.example.eyetrainer.Utils.Utils.performTimerEvent
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.FragmentCurrentExerciseBinding


@RequiresApi(Build.VERSION_CODES.S)
@Suppress("DEPRECATION")
class CurrentExerciseFragment : Fragment() {

    private val exerciseViewModel: ExerciseViewModel by activityViewModels()
    private lateinit var binding: FragmentCurrentExerciseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentExercise = exerciseViewModel.getExercise()!!
        applyImageFromExerciseItem(binding.currentExerciseImage, currentExercise)
        binding.currentExerciseName.text = currentExercise.name

        binding.back.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_currentExerciseFragment_to_exerciseFragment)
        }
        binding.currentExerciseImage.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_currentExerciseFragment_to_createExerciseFragment)
        }
        binding.send.setOnClickListener {
            exerciseViewModel.dataCanBeSent.value = false
            performTimerEvent(
                { exerciseViewModel.dataCanBeSent.value = true },
                2500L
            )

            val dataWasSent = exerciseViewModel.uploadData()
            if (!dataWasSent) {
                exerciseViewModel.connectedThread.value = null
                Toast.makeText(requireActivity(), "$APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND\n$APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE", Toast.LENGTH_SHORT).show()
            }
        }
        exerciseViewModel.dataCanBeSent.observe(this) {
            binding.send.apply{
                isEnabled = it
                if (isEnabled) {
                    setBackgroundResource(R.drawable.background_button_active)
                } else {
                    setBackgroundResource(R.drawable.background_button_nonactive)
                }
            }
        }
    }

    override fun onDestroy() {
        exerciseViewModel.clearExercise()
        super.onDestroy()
    }
}