package com.udacity.project4.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.udacity.project4.db.LocationRemindersDao
import com.udacity.project4.db.dto.ReminderTable

/**
 * The Room Database that contains the reminders table.
 */
@Database(entities = [ReminderTable::class], version = 1)
abstract class LocationRemindersDatabase : RoomDatabase() {
    abstract fun locationReminderDao(): LocationRemindersDao
}