package com.example.eyetrainer.Data

data class SingleExercise(
    val  image: Int, val name:String
)

data class ExerciseItemData(
    val first: SingleExercise, val second: SingleExercise
)

