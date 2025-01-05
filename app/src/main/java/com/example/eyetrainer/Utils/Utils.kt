package com.example.eyetrainer.Utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.Log
import android.widget.GridLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_GRID_MAX_CELL_COUNT
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_X
import com.example.eyetrainer.Data.Constants.APP_EXERCISE_MIRRORING_AXIS_Y
import com.example.eyetrainer.Data.SingleExercise
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

@RequiresApi(Build.VERSION_CODES.S)
object Utils {
    fun getBitmapFromGrid(pattern: ArrayList<Pair<Int, Int>>, mirroring: Int, gridWidth: Int, gridHeight: Int): Bitmap {
        var height = 0
        var width = 0
        var minHeight = APP_EXERCISE_GRID_MAX_CELL_COUNT
        var minWidth = APP_EXERCISE_GRID_MAX_CELL_COUNT
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

        var offset_x = (APP_EXERCISE_GRID_MAX_CELL_COUNT - width) * 0.5F
        var offset_y = (APP_EXERCISE_GRID_MAX_CELL_COUNT - height) * 0.5F
        when (mirroring) {
            APP_EXERCISE_MIRRORING_AXIS_Y -> {offset_x = APP_EXERCISE_GRID_MAX_CELL_COUNT * 0.5F - width}
            APP_EXERCISE_MIRRORING_AXIS_X -> {offset_y = APP_EXERCISE_GRID_MAX_CELL_COUNT * 0.5F - height}
        }

        val canvas_cell_width = gridWidth / APP_EXERCISE_GRID_MAX_CELL_COUNT
        val canvas_cell_height = gridHeight / APP_EXERCISE_GRID_MAX_CELL_COUNT

        val bitmap = Bitmap.createBitmap(gridWidth, gridHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (pattern.size <= 1)  {
            return bitmap
        }

        val paint = Paint()
        paint.color = "#3B2621".toColorInt()
        paint.strokeWidth = canvas_cell_width * 0.25F
        for (i in 0..<(pattern.size-1)) { // both the cell grid and canvas bitmap have "inverted" Y coordinates in comparison to what is expected by default (by regular users, not programmers)
            val start_x = pattern[i].second - minWidth + offset_x
            val start_y = APP_EXERCISE_GRID_MAX_CELL_COUNT - 1 - (pattern[i].first - minHeight) - offset_y
            val end_x = pattern[i + 1].second - minWidth + offset_x
            val end_y = APP_EXERCISE_GRID_MAX_CELL_COUNT - 1 - (pattern[i + 1].first - minHeight) - offset_y
            canvas.drawLine(canvas_cell_width * (0.5F + start_x), canvas_cell_height * (0.5F + start_y), canvas_cell_width * (0.5F + end_x), canvas_cell_height * (0.5F + end_y), paint)

            when (mirroring) {
                APP_EXERCISE_MIRRORING_AXIS_Y -> {canvas.drawLine(canvas_cell_width * (APP_EXERCISE_GRID_MAX_CELL_COUNT - start_x - 0.5F), canvas_cell_height * (0.5F + start_y), canvas_cell_width * (APP_EXERCISE_GRID_MAX_CELL_COUNT - end_x - 0.5F), canvas_cell_height * (0.5F + end_y), paint)}
                APP_EXERCISE_MIRRORING_AXIS_X -> {canvas.drawLine(canvas_cell_width * (start_x + 0.5F), canvas_cell_height * (APP_EXERCISE_GRID_MAX_CELL_COUNT - start_y - 0.5F), canvas_cell_width * (end_x + 0.5F), canvas_cell_height * (APP_EXERCISE_GRID_MAX_CELL_COUNT - end_y - 0.5F), paint)}
            }
        }
        val start_x = pattern.last().second - minWidth + offset_x
        val start_y = APP_EXERCISE_GRID_MAX_CELL_COUNT - 1 - (pattern.last().first - minHeight) - offset_y
        val end_x = pattern.first().second - minWidth + offset_x
        val end_y = APP_EXERCISE_GRID_MAX_CELL_COUNT - 1 - (pattern.first().first - minHeight) - offset_y
        canvas.drawLine(canvas_cell_width * (0.5F + start_x), canvas_cell_height * (0.5F + start_y), canvas_cell_width * (0.5F + end_x), canvas_cell_height * (0.5F + end_y), paint)

        when (mirroring) {
            APP_EXERCISE_MIRRORING_AXIS_Y -> {canvas.drawLine(canvas_cell_width * (APP_EXERCISE_GRID_MAX_CELL_COUNT - start_x - 0.5F), canvas_cell_height * (0.5F + start_y), canvas_cell_width * (APP_EXERCISE_GRID_MAX_CELL_COUNT - end_x - 0.5F), canvas_cell_height * (0.5F + end_y), paint)}
            APP_EXERCISE_MIRRORING_AXIS_X -> {canvas.drawLine(canvas_cell_width * (start_x + 0.5F), canvas_cell_height * (APP_EXERCISE_GRID_MAX_CELL_COUNT - start_y - 0.5F), canvas_cell_width * (end_x + 0.5F), canvas_cell_height * (APP_EXERCISE_GRID_MAX_CELL_COUNT - end_y - 0.5F), paint)}
        }

        paint.color = Color.BLACK
        paint.strokeWidth *= 0.5F
        when(mirroring) {
            APP_EXERCISE_MIRRORING_AXIS_Y -> {canvas.drawLine(gridWidth * 0.5F, 0F, gridWidth * 0.5F, gridHeight * 1F, paint)}
            APP_EXERCISE_MIRRORING_AXIS_X -> {canvas.drawLine(0F, gridHeight * 0.5F, gridWidth * 1F, gridHeight * 0.5F, paint)}
        }

        return bitmap
    }

    fun getNewExerciseFromData(pattern: ArrayList<Pair<Int, Int>>, arrow: Boolean, mirroring: Int, gridWidth: Int, gridHeight: Int): SingleExercise {
        var minHeight = APP_EXERCISE_GRID_MAX_CELL_COUNT
        var minWidth = APP_EXERCISE_GRID_MAX_CELL_COUNT
        pattern.forEach {
            if (it.first < minHeight) {
                minHeight = it.first
            }
            if (it.second < minWidth) {
                minWidth = it.second
            }
        }
        for (i in 0..<pattern.size) {
            pattern[i] = Pair(pattern[i].first - minHeight, pattern[i].second - minWidth)
        }

        return SingleExercise(-1, "custom", pattern, arrow, mirroring, getBitmapFromGrid(pattern, mirroring, gridWidth, gridHeight))
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