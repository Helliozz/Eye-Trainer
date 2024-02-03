package com.example.eyetrainer.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.eyetrainer.R
import com.example.eyetrainer.databinding.FragmentStartBinding


class StartFragment : Fragment() {

private lateinit var binding: FragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.exercise.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_startFragment_to_exerciseFragment)
        }
        binding.reminder.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_startFragment_to_reminderFragment)
        }

    }
}