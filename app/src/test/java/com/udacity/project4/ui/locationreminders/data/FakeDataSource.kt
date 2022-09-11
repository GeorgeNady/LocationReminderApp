package com.udacity.project4.ui.locationreminders.data

import com.udacity.project4.db.ReminderDataSource
import com.udacity.project4.db.dto.ReminderTable
import com.udacity.project4.db.dto.Resources


class FakeDataSource(var reminders: MutableList<ReminderTable> = mutableListOf()): ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }

    override suspend fun saveReminder(reminder: ReminderTable) {
        reminders.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    override suspend fun getReminders(): Resources<List<ReminderTable>> {

        if (shouldReturnError){
            return Resources.Error("Reminders not found", 404)
        }else{
            return return Resources.Success(ArrayList(reminders))
        }

    }


    override suspend fun getReminder(id: String): Resources<ReminderTable> {

        if(shouldReturnError){

            return Resources.Error("Error")

        }else{

            val reminder = reminders.find { it.id == id }

            if (reminder != null) {
                 return Resources.Success(reminder)
            } else {
                return Resources.Error("Reminder not found", 404)
            }

        }


    }




}