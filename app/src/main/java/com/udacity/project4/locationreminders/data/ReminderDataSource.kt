package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderTable
import com.udacity.project4.locationreminders.data.dto.Resources

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Resources<List<ReminderTable>>
    suspend fun saveReminder(reminder: ReminderTable)
    suspend fun getReminder(id: String): Resources<ReminderTable>
    suspend fun deleteAllReminders()
}