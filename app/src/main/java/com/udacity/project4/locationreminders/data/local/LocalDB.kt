package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.room.Room

object LocalDB {

    fun createRemindersDao(context: Context): LocationRemindersDao {
        return Room.databaseBuilder(
            context.applicationContext,
            LocationRemindersDatabase::class.java, "reminders.db"
        ).build().locationReminderDao()
    }

}