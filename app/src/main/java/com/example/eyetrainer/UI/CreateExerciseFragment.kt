package com.example.eyetrainer.UI

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.eyetrainer.Data.Constants.APP_EXERCISES_BASE_LIST
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_CELL_STATE_ACTIVE
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_CELL_STATE_CHOSEN
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_CELL_STATE_VITAL
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_GRID_MAX_CELL_COUNT
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_X
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_Y
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_NO_MIRROR
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.FragmentCreateExerciseBinding
import java.lang.RuntimeException
import kotlin.math.abs


@RequiresApi(Build.VERSION_CODES.S)
class CreateExerciseFragment : Fragment() {
    private lateinit var binding: FragmentCreateExerciseBinding
    private lateinit var gridLayout: GridLayout
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()

    private lateinit var patternData: ArrayList<Pair<Int, Int>>
    private var patternArrow = false
    private var patternMirror = 0

    private lateinit var setCellActive: (View, Int)->Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gridLayout = GridLayout(context)
        gridLayout.layoutParams = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        )
        gridLayout.rowCount = APP_EXERCISE_GRID_MAX_CELL_COUNT
        gridLayout.columnCount = APP_EXERCISE_GRID_MAX_CELL_COUNT

        val buttonSideLength = exerciseViewModel.getExerciseGridSideLength(requireContext()) / APP_EXERCISE_GRID_MAX_CELL_COUNT
        for (y in 0 until APP_EXERCISE_GRID_MAX_CELL_COUNT) {
            for (x in 0 until APP_EXERCISE_GRID_MAX_CELL_COUNT) {
                val button = Button(context)
                button.layoutParams = ViewGroup.LayoutParams(buttonSideLength, buttonSideLength)
                //button.text = "$x/$y"
                button.gravity = Gravity.CENTER
                gridLayout.addView(button)

                button.setOnClickListener {
                    patternData.add(Pair(y, x))
                    exerciseViewModel.updateExerciseCellGrid(gridLayout, patternData, patternMirror, setCellActive)
                    updateByCellStates()
                }
            }
        }
        binding.gridLayoutFrame.addView(gridLayout)

        setCellActive = {cell, state ->
            val active = ((state and APP_EXERCISE_CELL_STATE_ACTIVE) != 0)
            val chosen = ((state and APP_EXERCISE_CELL_STATE_CHOSEN) != 0)
            val vital = ((state and APP_EXERCISE_CELL_STATE_VITAL) != 0)

            if (!active) {
                cell.setBackgroundResource( when(chosen) {
                    true -> if (vital) R.drawable.background_exercise_cell_first_nonactive else R.drawable.background_exercise_cell_chosen_nonactive
                    false -> R.drawable.background_exercise_cell_nonactive
                })
                cell.isEnabled = false
            } else {
                cell.setBackgroundResource( when(chosen) {
                    true -> if (vital) R.drawable.background_exercise_cell_first_active else R.drawable.background_exercise_cell_chosen_active
                    false -> R.drawable.background_exercise_cell_active
                })
                cell.isEnabled = true
            }
        }

        binding.save.setOnClickListener {
            if (patternData.isEmpty()) {
                exerciseViewModel.deleteCreatedExercise(requireContext())
            } else {
                val invertedDuplicate = ArrayList(patternData)
                for (i in 0..<invertedDuplicate.size) { // inverting the pattern on the Y axis before saving (because of the way the grid is built)
                    invertedDuplicate[i] = Pair(APP_EXERCISE_GRID_MAX_CELL_COUNT - patternData[i].first - 1, patternData[i].second)
                }
                exerciseViewModel.saveCreatedExercise(requireContext(), invertedDuplicate, patternArrow, patternMirror, gridLayout)
            }
            initialSetup(true)
        }
        binding.back.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_createExerciseFragment_to_exerciseFragment)
        }

        binding.reverse.setOnClickListener {
            patternData.removeAt(patternData.lastIndex)
            if (patternData.isEmpty()) {
                resetMainFields()
            } else {
                exerciseViewModel.updateExerciseCellGrid(gridLayout, patternData, patternMirror, setCellActive)
                updateByCellStates()
            }
        }
        binding.reverse.setOnLongClickListener {
            resetMainFields()
            true
        }
        binding.arrow.setOnClickListener {
            updateArrowValue()
        }
        binding.mirroring.setOnClickListener {
            updateMirrorValue()
        }

        initialSetup()
    }

    private fun initialSetup(clear: Boolean = false) {
        val chosenExercise = exerciseViewModel.getExercise()

        if (clear || chosenExercise == null) {
            updateArrowValue(true)
            updateMirrorValue(0)
            resetMainFields()
        } else {
            updateArrowValue(chosenExercise.shouldDrawArrow)
            updateMirrorValue(chosenExercise.mirroring)

            var height = 0; var width = 0
            chosenExercise.points.forEach {
                if (it.first > height) {
                    height = it.first
                }
                if (it.second > width) {
                    width = it.second
                }
            }
            height++; width++

            if (height > APP_EXERCISE_GRID_MAX_CELL_COUNT || width > APP_EXERCISE_GRID_MAX_CELL_COUNT) {
                throw(RuntimeException("Maximum exercise cell amount breached!"))
            }

            var offset_y = (APP_EXERCISE_GRID_MAX_CELL_COUNT - height) / 2
            var offset_x = (APP_EXERCISE_GRID_MAX_CELL_COUNT - width) / 2
            when (patternMirror) {
                APP_EXERCISE_MIRRORING_AXIS_Y -> {offset_x = APP_EXERCISE_GRID_MAX_CELL_COUNT / 2 - width}
                APP_EXERCISE_MIRRORING_AXIS_X -> {offset_y = APP_EXERCISE_GRID_MAX_CELL_COUNT / 2 - height}
            }

            patternData = arrayListOf()
            chosenExercise.points.forEach {// inverting the pattern before applying it
                patternData.add(Pair(APP_EXERCISE_GRID_MAX_CELL_COUNT - 1 - it.first - offset_y, it.second + offset_x))
            }

            exerciseViewModel.updateExerciseCellGrid(gridLayout, patternData, patternMirror, setCellActive)
            updateByCellStates()
        }
    }

    private fun resetMainFields() {
        patternData = arrayListOf()
        updateByCellStates()

        for (y in 0 until APP_EXERCISE_GRID_MAX_CELL_COUNT) {
            for (x in 0 until APP_EXERCISE_GRID_MAX_CELL_COUNT) {
                val cell = gridLayout.getChildAt(y * APP_EXERCISE_GRID_MAX_CELL_COUNT + x)
                if ( (patternMirror == APP_EXERCISE_MIRRORING_AXIS_X && y >= APP_EXERCISE_GRID_MAX_CELL_COUNT / 2) || (patternMirror == APP_EXERCISE_MIRRORING_AXIS_Y && x >= APP_EXERCISE_GRID_MAX_CELL_COUNT / 2) ) {
                    setCellActive(cell, 0)
                } else {
                    setCellActive(cell, APP_EXERCISE_CELL_STATE_ACTIVE)
                }
            }
        }
    }

    private fun updateArrowValue(value: Boolean? = null) {
        patternArrow = value ?: !patternArrow

        binding.arrow.setBackgroundResource(when(patternArrow) {
            true -> R.drawable.background_button_active
            else -> R.drawable.background_button_nonactive
        })
    }

    private fun updateMirrorValue(value: Int? = null) {
        patternMirror = value ?: ((patternMirror + 1) % 3)
        when(patternMirror) {
            APP_EXERCISE_MIRRORING_NO_MIRROR -> {
                binding.mirroring.text = "Ø"
            }
            APP_EXERCISE_MIRRORING_AXIS_X -> {
                binding.mirroring.text = "X"
            }
            APP_EXERCISE_MIRRORING_AXIS_Y -> {
                binding.mirroring.text = "Y"
            }
        }
        resetMainFields()
    }

    private fun updateByCellStates(forcedSave: Boolean? = null) {
        val savePressable: Boolean = if (patternData.isEmpty()) {
            exerciseViewModel.exercises.value!!.size > APP_EXERCISES_BASE_LIST.size
        } else {
            abs(patternData.last().first - patternData.first().first) <= 1 && abs(patternData.last().second - patternData.first().second) <= 1 && (patternData.last().first != patternData.first().first || patternData.last().second != patternData.first().second)
        }
        val saveAllowed = forcedSave ?: savePressable

        if (saveAllowed) {
            binding.save.isEnabled = true
            binding.save.setBackgroundResource(R.drawable.background_button_active)
        } else {
            binding.save.isEnabled = false
            binding.save.setBackgroundResource(R.drawable.background_button_nonactive)
        }

        var patternDataText = ""
        patternData.forEach {
            if (patternDataText.isNotEmpty()) {
                patternDataText += " -> "
            }
            patternDataText += "(${it.second + 1}, ${APP_EXERCISE_GRID_MAX_CELL_COUNT - it.first})"
        }
        if (patternDataText.isEmpty()) {
            patternDataText = "Нажимайте на клетки поля для создания упражнения!"
        }
        binding.patternText.text = patternDataText

        if (patternData.isEmpty()) {
            binding.reverse.isEnabled = false
            binding.reverse.setBackgroundResource(R.drawable.background_button_nonactive)

            binding.mirroring.setBackgroundResource(R.drawable.background_button_active)
            binding.mirroring.isEnabled = true
        } else {
            binding.reverse.isEnabled = true
            binding.reverse.setBackgroundResource(R.drawable.background_button_active)

            binding.mirroring.setBackgroundResource(R.drawable.background_button_nonactive)
            binding.mirroring.isEnabled = false
        }
    }
}