package com.example.eyetrainer.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import com.example.eyetrainer.Data.Constants
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MAX_CELLS
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_NO_MIRROR
import com.example.eyetrainer.Data.SingleExercise
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

@RequiresApi(Build.VERSION_CODES.S)
object Utils {
    fun getBitmapFromViewUsingCanvas(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun getNewExerciseFromData(pattern: ArrayList<Pair<Int, Int>>, arrow: Boolean, mirroring: Int, grid: GridLayout): SingleExercise {
        var height = 0
        var width = 0
        var minHeight = APP_EXERCISE_MAX_CELLS
        var minWidth = APP_EXERCISE_MAX_CELLS
        pattern.forEach {
            if (it.first > height) {
                height = it.first
            }
            if (it.first < minHeight) {
                minHeight = it.first
            }
            if (it.second > width) {
                width = it.second
            }
            if (it.second < minWidth) {
                minWidth = it.second
            }
        }
        height = height - minHeight + 1
        width = width - minWidth + 1

        var offset_x = (APP_EXERCISE_MAX_CELLS - width) / 2
        var offset_y = (APP_EXERCISE_MAX_CELLS - height) / 2
        when (mirroring) {
            Constants.APP_EXERCISE_MIRRORING_AXIS_Y -> {offset_x = APP_EXERCISE_MAX_CELLS / 2 - width}
            Constants.APP_EXERCISE_MIRRORING_AXIS_X -> {offset_y = APP_EXERCISE_MAX_CELLS / 2 - height}
        }

        for (i in 0..<pattern.size) {
            pattern[i] = Pair(pattern[i].first - minHeight + offset_y, pattern[i].second - minWidth + offset_x)

            when (mirroring) {
                Constants.APP_EXERCISE_MIRRORING_AXIS_Y -> {pattern.add(Pair(pattern[i].first, APP_EXERCISE_MAX_CELLS - pattern[i].second - 1))}
                Constants.APP_EXERCISE_MIRRORING_AXIS_X -> {pattern.add(Pair(APP_EXERCISE_MAX_CELLS - pattern[i].first - 1, pattern[i].second))}
            }
        }

        for (i in 0..<APP_EXERCISE_MAX_CELLS) {
            for (j in 0..<APP_EXERCISE_MAX_CELLS) {
                val cell = grid.getChildAt(i * APP_EXERCISE_MAX_CELLS + j)
                if (pattern.contains(Pair(i, j))) {
                    cell.setBackgroundColor("#3B2621".toColorInt())
                } else {
                    cell.visibility = View.GONE
                }
            }
        }
        val bitmapImage = getBitmapFromViewUsingCanvas(grid)
        if (mirroring != APP_EXERCISE_MIRRORING_NO_MIRROR) {
            for (i in 0..<pattern.size/2) {
                pattern.removeAt(pattern.lastIndex)
            }
        }
        for (i in 0..<pattern.size) {
            pattern[i] = Pair(pattern[i].first - offset_y, pattern[i].second - offset_x)
        }
        val createdExercise = SingleExercise(-1, "custom", pattern, arrow, mirroring, bitmapImage)

        for (i in 0..<APP_EXERCISE_MAX_CELLS) {
            for (j in 0..<APP_EXERCISE_MAX_CELLS) {
                val cell = grid.getChildAt(i * APP_EXERCISE_MAX_CELLS + j)
                cell.visibility = View.VISIBLE
            }
        }

        return createdExercise
    }

    fun applyImageFromExerciseItem(view: ImageView, exerciseData: SingleExercise) {
        if (exerciseData.image != -1) {
            view.setImageResource(exerciseData.image)
        } else if (exerciseData.bitmap != null) {
            view.setImageBitmap(exerciseData.bitmap)
        } else {
            throw(RuntimeException("No available image was found for this item!"))
        }
    }

    fun getCheckedByte(command: Int): Byte {
        val bytes = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(command).array()
        if (bytes[0].toInt() != 0 || bytes[1].toInt() != 0 || bytes[2].toInt() != 0) {
            Log.d("APP_CHECKER", "Byte1 = ${bytes[0].toInt()}, Byte2 = ${bytes[1].toInt()},  Byte3 = ${bytes[2].toInt()}, Byte4 = ${bytes[3].toInt()}")
            throw(RuntimeException("This number is too big to send."))
        }
        return bytes[3]
    }

    fun performTimerEvent(timerFun: () -> Unit, time: Long) {
        val eventTimer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    timerFun()
                }
            }
        }
        eventTimer.schedule(timerTask, time)
    }
}