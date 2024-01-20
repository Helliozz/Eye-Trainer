package com.example.eyetrainer.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eyetrainer.Adapter.ChoiceRecyclerViewAdapter
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.ChoiceFragmentViewModel
import com.example.eyetrainer.databinding.FragmentChoiceBinding

class ChoiceFragment : Fragment() {

    private lateinit var itemFun: (SingleExercise)->Unit

    private lateinit var binding: FragmentChoiceBinding
    private val recyclerViewAdapter by lazy { ChoiceRecyclerViewAdapter(itemFun) }
    private val choiceFragmentViewModel: ChoiceFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        itemFun={
            choiceFragmentViewModel.saveExercise(it)
            requireView().findNavController().navigate(R.id.action_choiceFragment_to_currentExerciseFragment)
        }

        binding.reminder.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_choiceFragment_to_reminderFragment)
        }
        recyclerViewAdapter.differ.submitList(choiceFragmentViewModel.getExercises())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = recyclerViewAdapter
        }
    }

}