package com.example.eyetrainer.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.eyetrainer.R
import com.example.eyetrainer.databinding.FragmentReminderBinding


class ReminderFragment : Fragment() {

    private lateinit var binding: FragmentReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.exercise.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_reminderFragment_to_exerciseFragment)
        }
    }
}

