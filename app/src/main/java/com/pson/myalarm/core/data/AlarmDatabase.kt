package com.pson.myalarm.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pson.myalarm.core.data.dao.AlarmDao
import com.pson.myalarm.core.data.model.Alarm
import com.pson.myalarm.core.data.model.WeeklySchedule

@Database(
    entities = [Alarm::class, WeeklySchedule::class],
    version = 1,
)
@TypeConverters(LocalTimeConverter::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AlarmDatabase::class.java,
                "alarm-db"
            )
                .build()
            INSTANCE = instance
            return instance
        }
    }
}