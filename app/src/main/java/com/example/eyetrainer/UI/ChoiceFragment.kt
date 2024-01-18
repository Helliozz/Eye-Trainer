package com.example.eyetrainer.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eyetrainer.Adapter.ChoiceRecyclerViewAdapter
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.R
import com.example.eyetrainer.databinding.FragmentChoiceBinding

class ChoiceFragment : Fragment() {

    private lateinit var binding: FragmentChoiceBinding
    private val recyclerViewAdapter by lazy { ChoiceRecyclerViewAdapter() }
    private val exercises: List<ExerciseItemData> = listOf(
        ExerciseItemData(
            R.drawable.icon_left_right, "влево-вправо", R.drawable.icon_up_down, "вверх-вниз"
        ), ExerciseItemData(
            R.drawable.icon_circle_left, "круг влево", R.drawable.icon_circle_right, "круг вправо"
        ), ExerciseItemData(
            R.drawable.icon_rombus, "ромб", R.drawable.icon_square, "квадрат"
        ), ExerciseItemData(
            R.drawable.icon_rainbow_1, "радуга 1", R.drawable.icon_rainbow_2, "радуга 2"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.reminder.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_choiceFragment_to_remiderFragment)
        }
        recyclerViewAdapter.differ.submitList(exercises)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter=recyclerViewAdapter
        }
    }
}