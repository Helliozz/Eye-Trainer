package com.example.eyetrainer.ViewModel

import androidx.lifecycle.ViewModel
import com.example.eyetrainer.Data.ExerciseItemData
import com.example.eyetrainer.Data.SingleExercise
import com.example.eyetrainer.R

class ChoiceFragmentViewModel : ViewModel() {

    private lateinit var savedExercise: SingleExercise


    private val exercises: List<ExerciseItemData> = listOf(
        ExerciseItemData(
            SingleExercise(R.drawable.icon_left_right, "влево-вправо"),
            SingleExercise(R.drawable.icon_up_down, "вверх-вниз"),
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_circle_left, "круг влево"),
            SingleExercise(R.drawable.icon_circle_right, "круг вправо")
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_rombus, "ромб"),
            SingleExercise(R.drawable.icon_square, "квадрат")
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_rainbow_1, "радуга 1"),
            SingleExercise(R.drawable.icon_rainbow_2, "радуга 2")
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_butterfly_1, "бантик 1"),
            SingleExercise(R.drawable.icon_butterfly_2, "бантик 2")
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_eight, "восемь"),
            SingleExercise(R.drawable.icon_infinity, "бесконечность")
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_saw, "пила"),
            SingleExercise(R.drawable.icon_snake, "змея")
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_ellipse, "овал"),
            SingleExercise(R.drawable.icon_diagonal, "диагональ")
        )
    )

    fun getExercises(): List<ExerciseItemData> {
        return exercises
    }

    fun saveExercise(singleExercise: SingleExercise) {
        savedExercise = singleExercise
    }

    fun getExercise(): SingleExercise {
        return savedExercise
    }
}