package com.example.eyetrainer.Data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    val allNotifications: Flow<List<NotificationData>> = notificationDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNotification(notificationData:NotificationData){
        notificationDao.insertNotification(notificationData)
    }
    @WorkerThread
    suspend fun deleteNotification(notificationData: NotificationData){
        notificationDao.deleteNotification(notificationData)
    }

}