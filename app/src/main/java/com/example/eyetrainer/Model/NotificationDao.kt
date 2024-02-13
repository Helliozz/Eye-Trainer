package com.example.eyetrainer.Model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_table")
    fun getAll(): Flow<List<NotificationData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(vararg notification: NotificationData)

    @Delete
    suspend fun deleteNotification(notification: NotificationData)

    @Query("DELETE FROM notification_table")
    suspend fun deleteAll()

    @Update
    suspend fun updateNotification(notification: NotificationData)
}