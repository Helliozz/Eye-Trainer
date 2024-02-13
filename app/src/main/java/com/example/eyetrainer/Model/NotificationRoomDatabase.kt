package com.example.eyetrainer.Model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [NotificationData::class], version = 1, exportSchema = false)
abstract class NotificationRoomDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    private class NotificationDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database -> scope.launch { populateDatabase(database.notificationDao()) } }
        }

        suspend fun populateDatabase(notificationDao: NotificationDao) {
            notificationDao.deleteAll()
            val timeSet = Calendar.getInstance()
            timeSet.set(Calendar.HOUR_OF_DAY, 12)
            timeSet.set(Calendar.MINUTE,0)

            notificationDao.insertNotification(
                NotificationData(
                    id = 10, time = timeSet.timeInMillis, days = 127, isEnabled = false
                )
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NotificationRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NotificationRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationRoomDatabase::class.java,
                    "notification_database"
                ).addCallback(NotificationDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }
}