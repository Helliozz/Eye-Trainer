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
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MAX_CELLS
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_X
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_Y
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_NO_MIRROR
import com.example.eyetrainer.R
import com.example.eyetrainer.ViewModel.ExerciseViewModel
import com.example.eyetrainer.databinding.FragmentCreateExerciseBinding
import java.lang.RuntimeException
import kotlin.math.round


@RequiresApi(Build.VERSION_CODES.S)
class CreateExerciseFragment : Fragment() {
    private lateinit var binding: FragmentCreateExerciseBinding
    private lateinit var gridLayout: GridLayout
    private val exerciseViewModel: ExerciseViewModel by activityViewModels()

    private lateinit var patternData: ArrayList<Pair<Int, Int>>
    private var patternArrow = false
    private var patternMirror = 0

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
        gridLayout.rowCount = APP_EXERCISE_MAX_CELLS
        gridLayout.columnCount = APP_EXERCISE_MAX_CELLS

        val displayMetrics = requireContext().resources.displayMetrics
        val pixelWidth = if (displayMetrics.widthPixels < displayMetrics.heightPixels) displayMetrics.widthPixels else displayMetrics.heightPixels

        for (i in 0 until APP_EXERCISE_MAX_CELLS) {
            for (j in 0 until APP_EXERCISE_MAX_CELLS) {
                val button = Button(context)
                button.layoutParams = ViewGroup.LayoutParams(
                    round(pixelWidth * 0.75 / APP_EXERCISE_MAX_CELLS).toInt(),
                    round(pixelWidth * 0.75 / APP_EXERCISE_MAX_CELLS).toInt()
                )
                //button.text = "$i/$j"
                button.gravity = Gravity.CENTER
                gridLayout.addView(button)

                button.setOnClickListener {
                    val saveAllowed = exerciseViewModel.exerciseCreationCellFunction(i, j, gridLayout, patternData, patternMirror)
                    updateByCellStates(saveAllowed)
                }
            }
        }
        binding.gridLayoutFrame.addView(gridLayout)


        binding.back.setOnClickListener {
            requireView().findNavController()
                .navigate(R.id.action_createExerciseFragment_to_exerciseFragment)
        }
        binding.save.setOnClickListener {
            exerciseViewModel.saveCreatedExercise(patternData, patternArrow, patternMirror, gridLayout)
            initialSetup(true)
        }
        binding.reverse.setOnClickListener {
            reverseAction()
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

            if (height > APP_EXERCISE_MAX_CELLS || width > APP_EXERCISE_MAX_CELLS) {
                throw(RuntimeException("Maximum exercise cell amount breached!"))
            }

            var offset_y = (APP_EXERCISE_MAX_CELLS - height) / 2
            var offset_x = (APP_EXERCISE_MAX_CELLS - width) / 2
            when (patternMirror) {
                APP_EXERCISE_MIRRORING_AXIS_Y -> {offset_x = APP_EXERCISE_MAX_CELLS / 2 - width}
                APP_EXERCISE_MIRRORING_AXIS_X -> {offset_y = APP_EXERCISE_MAX_CELLS / 2 - height}
            }

            patternData = arrayListOf()
            chosenExercise.points.forEach {
                patternData.add(Pair(it.first + offset_y, it.second + offset_x))
            }

            val saveAllowed = exerciseViewModel.exerciseCreationCellFunction(patternData.last().first, patternData.last().second, gridLayout, patternData, patternMirror)
            patternData.removeAt(patternData.lastIndex)
            updateByCellStates(saveAllowed)
        }
    }

    private fun resetMainFields() {
        patternData = arrayListOf()
        updateByCellStates(false)

        for (i in 0 until APP_EXERCISE_MAX_CELLS) {
            for (j in 0 until APP_EXERCISE_MAX_CELLS) {
                val cell = gridLayout.getChildAt(i * APP_EXERCISE_MAX_CELLS + j)
                if ( (patternMirror == APP_EXERCISE_MIRRORING_AXIS_X && i >= APP_EXERCISE_MAX_CELLS / 2) || (patternMirror == APP_EXERCISE_MIRRORING_AXIS_Y && j >= APP_EXERCISE_MAX_CELLS / 2) ) {
                    cell.setBackgroundResource(R.drawable.background_exercise_cell_nonactive)
                    cell.isEnabled = false
                } else {
                    cell.setBackgroundResource(R.drawable.background_exercise_cell_active)
                    cell.isEnabled = true
                }
            }
        }
    }

    private fun reverseAction() {
        patternData.removeAt(patternData.lastIndex)
        if (patternData.isEmpty()) {
            resetMainFields()
        } else {
            val saveAllowed = exerciseViewModel.exerciseCreationCellFunction(patternData.last().first, patternData.last().second, gridLayout, patternData)
            patternData.removeAt(patternData.lastIndex)
            updateByCellStates(saveAllowed)
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

    private fun updateByCellStates(saveAllowed: Boolean) {
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
            patternDataText += "(${it.first + 1}, ${it.second + 1})"
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