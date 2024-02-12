package com.example.eyetrainer.ViewModel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.eyetrainer.Data.Constants.APP_KEY_CHANNEL_ID
import com.example.eyetrainer.Data.Constants.APP_KEY_DAY_CHECKSUM
import com.example.eyetrainer.Data.Constants.APP_NOTIFICATION_POW_TRANSLATION
import com.example.eyetrainer.R
import com.example.eyetrainer.UI.MainActivity
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
class AlarmReceiver : BroadcastReceiver() {

    private companion object {
        private const val CHANNEL_ID = APP_KEY_CHANNEL_ID
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Notification", "onReceive")
        val checkSum = intent.getIntExtra(APP_KEY_DAY_CHECKSUM, -1)
        val calendar = Calendar.getInstance()
        val pow = APP_NOTIFICATION_POW_TRANSLATION[calendar.get(Calendar.DAY_OF_WEEK)]
        val sum = Math.pow(2.0, pow.toDouble()).toInt()
        if ((checkSum and sum) != 0) {
            sendNotification(context, intent)
        }
    }

    private fun sendNotification(context: Context, intent: Intent) {
//        val textTime = intent.getStringExtra("notification_time")
//        val textId = intent.getIntExtra("notification_key", -1)

        Log.d("Notification", "Notification sent")
        val name: CharSequence = "MyNotification"
        val description = "My notification chanel description"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
        notificationChannel.description = description
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val date = Date()
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.US).format(date).toInt()

        val mainIntent = Intent(context, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val mainPendingIntent =
            PendingIntent.getActivity(context, 1, mainIntent, PendingIntent.FLAG_MUTABLE)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.icon_notification)
        notificationBuilder.setContentTitle("Вы сегодня размяли глаза?")
//        notificationBuilder.setContentTitle("time: $textTime, id = $textId")
        notificationBuilder.setContentText("Самое время зайти в приложение и повторить одно из полезных для глаз упражнений")
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH


        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setContentIntent(mainPendingIntent)


        val notificationManagerCompat = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManagerCompat.notify(notificationId, notificationBuilder.build())

    }
}