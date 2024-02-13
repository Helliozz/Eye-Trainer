package com.example.eyetrainer

import android.app.Application
import com.example.eyetrainer.Model.NotificationRepository
import com.example.eyetrainer.Model.NotificationRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class NotificationsApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { NotificationRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { NotificationRepository(database.notificationDao()) }
}