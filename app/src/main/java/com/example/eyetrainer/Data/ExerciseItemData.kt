package com.example.eyetrainer.Data

data class ExerciseItemData(
    val firstImage: Int, val firstName: String, val secondImage: Int, val secondName: String
)

data class SingleExercise(
    val image: Int, val name: String
)
