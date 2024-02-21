package com.example.eyetrainer.ViewModel


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.eyetrainer.Data.Constants
import com.example.eyetrainer.Data.Constants.APP_KEY_DAY_CHECKSUM
import com.example.eyetrainer.Model.NotificationData
import com.example.eyetrainer.Model.NotificationRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*


@RequiresApi(Build.VERSION_CODES.S)
class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {
    val notifications: LiveData<List<NotificationData>> = repository.allNotifications.asLiveData()

    private var savedNotification: NotificationData? = null

    fun getSavedNotification(): NotificationData? {
        return savedNotification
    }

    fun saveNotification(notification: NotificationData) {
        savedNotification = notification
    }

    fun clearSavedNotification() {
        savedNotification = null
    }

    fun insert(notificationData: NotificationData) = viewModelScope.launch {
        repository.insertNotification(notificationData)
    }

    fun delete(notificationData: NotificationData) = viewModelScope.launch {
        repository.deleteNotification(notificationData)
    }

    fun update(notificationData: NotificationData)=viewModelScope.launch {
        repository.updateNotification(notificationData)
    }

    fun getPossibleId(): Int {
        val currentIds: ArrayList<Int> = arrayListOf()
        notifications.value?.forEach {
            currentIds.add(it.id)
        }
        currentIds.sort()

        var newId = 10
        for (e: Int in currentIds) {
            if (newId == e) {
                newId += 1
            }
        }
        return newId
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

    fun setNewExactAlarm(
        notification: NotificationData,
        context: Context?,
        alarmManager: AlarmManager,
    ) {
        val alarmIntent: PendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            Log.d("NotificationSample", "days = ${notification.days}, identifier = ${notification.id}.")
            intent.putExtra(APP_KEY_DAY_CHECKSUM, notification.days)

            PendingIntent.getBroadcast(
                context, notification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_MUTABLE
            )
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, notification.time, AlarmManager.INTERVAL_DAY, alarmIntent
        )
    }

    fun activateNotification(notification: NotificationData, context: Context?, alarmManager: AlarmManager) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            Log.d("NotificationSample", "days = ${notification.days}, identifier = ${notification.id}.")
            intent.putExtra(APP_KEY_DAY_CHECKSUM, notification.days)
            PendingIntent.getBroadcast(
                context, /*Идентификационный номер, должен быть уникальным (можно заменить id)*/
                notification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_MUTABLE
            )
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, notification.time, AlarmManager.INTERVAL_DAY, alarmIntent
        )
    }

    fun cancelNotification(notification: NotificationData, context: Context?, alarmManager: AlarmManager) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                context, /*Идентификационный номер, должен быть уникальным (можно заменить id)*/
                notification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_MUTABLE
            )
        }
        alarmManager.cancel(alarmIntent)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
class NotificationViewModelFactory(private val repository: NotificationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return NotificationViewModel(repository) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}