package com.udacity.project4.db

import com.udacity.project4.db.dto.ReminderTable
import com.udacity.project4.db.dto.Resources

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Resources<List<ReminderTable>>
    suspend fun saveReminder(reminder: ReminderTable)
    suspend fun getReminder(id: String): Resources<ReminderTable>
    suspend fun deleteAllReminders()
}