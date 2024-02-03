package com.example.eyetrainer.Data

import com.example.eyetrainer.R

object Constants {
    val APP_EXERCISES_BASE_LIST = listOf(
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

    const val APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE = "No data can be sent to any physical device."
    const val APP_TOAST_BLUETOOTH_MISSING = "Sadly, you have no Bluetooth support."
    const val APP_DEVICE_BLUETOOTH_ADDRESS = "00:20:10:08:0B:EF"
}


