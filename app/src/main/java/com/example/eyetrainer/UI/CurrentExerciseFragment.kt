package com.example.eyetrainer.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
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
    }
}