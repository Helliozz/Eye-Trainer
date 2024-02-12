package com.example.eyetrainer.Data

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.eyetrainer.R

@RequiresApi(Build.VERSION_CODES.S)
object Constants {
    const val APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE = "No data can be sent to a physical device."
    const val APP_TOAST_BLUETOOTH_MISSING = "Sadly, you have no Bluetooth support."
    const val APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND = "No valid device was found."
    const val APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL = "Successful connection!"
    const val APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED = "Failed to connect to the device."
    const val APP_TOAST_NOTIFICATION_SENDING_NOT_AVAILABLE = "You will not be able to create or receive notifications."
    const val APP_TOAST_NO_DAY_CHOSEN = "No day was chosen for this notification."
    const val APP_DEVICE_BLUETOOTH_ADDRESS = "00:20:10:08:0B:EF"

    const val APP_EXERCISE_MIRRORING_NO_MIRROR = 0
    const val APP_EXERCISE_MIRRORING_AXIS_X = 1
    const val APP_EXERCISE_MIRRORING_AXIS_Y = 2

    const val APP_KEY_DAY_CHECKSUM = "notification_checksum"
    const val APP_KEY_CHANNEL_ID = "notifications_channel_id_101"

    val APP_BLUETOOTH_PERMISSIONS_LIST = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val APP_EXERCISES_BASE_LIST = listOf(
        ExerciseItemData(
            SingleExercise(R.drawable.icon_left_right, "влево-вправо", arrayListOf(Pair(0,0), Pair(0,1), Pair(0,2), Pair(0,3), Pair(0,4), Pair(0,5), Pair(0,6), Pair(0,7), Pair(0,6), Pair(0,5), Pair(0,4), Pair(0,3), Pair(0,2), Pair(0,1))),
            SingleExercise(R.drawable.icon_up_down, "вверх-вниз", arrayListOf(Pair(0,0), Pair(1,0), Pair(2,0), Pair(3,0), Pair(4,0), Pair(5,0), Pair(6,0), Pair(7,0), Pair(6,0), Pair(5,0), Pair(4,0), Pair(3,0), Pair(2,0), Pair(1,0))),
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_circle_left, "круг влево", arrayListOf(Pair(0,1), Pair(1,0), Pair(2,0), Pair(3,1), Pair(3,2), Pair(2,3), Pair(1,3), Pair(0,2))),
            SingleExercise(R.drawable.icon_circle_right, "круг вправо", arrayListOf(Pair(0,2), Pair(1,3), Pair(2,3), Pair(3,2), Pair(3,1), Pair(2,0), Pair(1,0), Pair(0,1)))
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_rombus, "ромб", arrayListOf(Pair(0,1), Pair(1,0), Pair(2,1), Pair(1,2)), shouldDrawArrow = false),
            SingleExercise(R.drawable.icon_square, "квадрат", arrayListOf(Pair(0,0), Pair(1,0), Pair(1,1), Pair(0,1)))
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_rainbow_1, "радуга 1", arrayListOf(Pair(2,1), Pair(1,0), Pair(0,0), Pair(1,0)), mirroring = APP_EXERCISE_MIRRORING_AXIS_Y),
            SingleExercise(R.drawable.icon_rainbow_2, "радуга 2", arrayListOf(Pair(0,1), Pair(1,0), Pair(2,0), Pair(1,0)), mirroring = APP_EXERCISE_MIRRORING_AXIS_Y)
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_butterfly_1, "бантик 1", arrayListOf(Pair(0,0), Pair(1,0), Pair(2,0), Pair(1,1), Pair(0,2), Pair(1,2), Pair(2,2), Pair(1,1))),
            SingleExercise(R.drawable.icon_butterfly_2, "бантик 2", arrayListOf(Pair(0,0), Pair(1,1), Pair(2,2), Pair(2,1), Pair(2,0), Pair(1,1), Pair(0,2), Pair(0,1)))
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_eight, "восемь", arrayListOf(Pair(3,1), Pair(4,0), Pair(5,0), Pair(6,1), Pair(6,2), Pair(5,3), Pair(4,3), Pair(3,2), Pair(3,1), Pair(2,0), Pair(1,0), Pair(0,1), Pair(0,2), Pair(1,3), Pair(2,3), Pair(3,2))),
            SingleExercise(R.drawable.icon_infinity, "бесконечность", arrayListOf(Pair(1,3), Pair(0,4), Pair(0,5), Pair(1,6), Pair(2,6), Pair(3,5), Pair(3,4), Pair(2,3), Pair(1,3), Pair(0,2), Pair(0,1), Pair(1,0), Pair(2,0), Pair(3,1), Pair(3,2), Pair(2,3))),
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_saw, "пила", arrayListOf(Pair(1,5), Pair(0,4), Pair(1,3), Pair(0,2), Pair(1,1), Pair(0,0), Pair(1,0), Pair(0,1), Pair(1,2), Pair(0,3), Pair(1,4), Pair(0,5)), shouldDrawArrow = false),
            SingleExercise(R.drawable.icon_snake, "змея", arrayListOf(Pair(1,0), Pair(2,1), Pair(2,2), Pair(1,3), Pair(0,4), Pair(0,5), Pair(1,6), Pair(0,5), Pair(0,4), Pair(1,3), Pair(2,2), Pair(2,1)), shouldDrawArrow = false)
        ), ExerciseItemData(
            SingleExercise(R.drawable.icon_ellipse, "овал", arrayListOf(Pair(1,0), Pair(0,1), Pair(0,2), Pair(1,3), Pair(2,3), Pair(3,3), Pair(4,3), Pair(5,2), Pair(5,1), Pair(4,0), Pair(3,0), Pair(2,0))),
            SingleExercise(R.drawable.icon_diagonal, "диагональ", arrayListOf(Pair(0,0), Pair(1,1), Pair(2,2), Pair(1,1)))
        )
    )

    val APP_NOTIFICATION_POW_TRANSLATION = listOf(6,0,1,2,3,4,5)
}


