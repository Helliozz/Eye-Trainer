package com.example.eyetrainer.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
class NotificationData(
    @PrimaryKey(autoGenerate = false) var id: Int,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "days") val days: Int,
    @ColumnInfo(name = "is_enabled")var isEnabled: Boolean
)