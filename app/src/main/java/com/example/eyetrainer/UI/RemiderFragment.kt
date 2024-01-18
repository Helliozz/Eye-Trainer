package com.example.eyetrainer.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.eyetrainer.R
import com.example.eyetrainer.databinding.FragmentRemiderBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class RemiderFragment : Fragment() {

    private lateinit var binding: FragmentRemiderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRemiderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.exercise.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_remiderFragment_to_choiceFragment)
        }
    }
}