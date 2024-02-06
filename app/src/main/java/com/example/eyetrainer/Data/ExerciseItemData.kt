package com.example.eyetrainer.Data

import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_NO_MIRROR

data class SingleExercise(
    val image: Int, val name: String,
    val points: ArrayList<Pair<Int, Int>> = arrayListOf(),
    val shouldDrawArrow: Boolean = true,
    val mirroring: Int = APP_EXERCISE_MIRRORING_NO_MIRROR
)

data class ExerciseItemData(
    val first: SingleExercise, val second: SingleExercise
)

