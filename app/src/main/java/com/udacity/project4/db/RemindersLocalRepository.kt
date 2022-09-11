package com.udacity.project4.db

import com.udacity.project4.db.dto.ReminderTable
import com.udacity.project4.db.dto.Resources
import com.udacity.project4.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of a data source as a db.
 *
 * The repository is implemented so that you can focus on only testing it.
 *
 * @param locationRemindersDao the dao that does the Room db operations
 * @param ioDispatcher a coroutine dispatcher to offload the blocking IO tasks
 */
class RemindersLocalRepository(
    private val locationRemindersDao: LocationRemindersDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReminderDataSource {

    /**
     * Get the reminders list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */
    override suspend fun getReminders(): Resources<List<ReminderTable>> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            return@withContext try {
                Resources.Success(locationRemindersDao.getReminders())
            } catch (ex: Exception) {
                Resources.Error(ex.localizedMessage)
            }
        }
    }

    /**
     * Insert a reminder in the db.
     * @param reminder the reminder to be inserted
     */
    override suspend fun saveReminder(reminder: ReminderTable) = wrapEspressoIdlingResource {
        withContext(ioDispatcher) {
            locationRemindersDao.saveReminder(reminder)
        }
    }

    /**
     * Get a reminder by its id
     * @param id to be used to get the reminder
     * @return Result the holds a Success object with the Reminder or an Error object with the error message
     */
    override suspend fun getReminder(id: String): Resources<ReminderTable> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            try {
                val reminder = locationRemindersDao.getReminderById(id)
                if (reminder != null) {
                    return@withContext Resources.Success(reminder)
                } else {
                    return@withContext Resources.Error("Reminder not found!")
                }
            } catch (e: Exception) {
                return@withContext Resources.Error(e.localizedMessage)
            }
        }
    }

    /**
     * Deletes all the reminders in the db
     */
    override suspend fun deleteAllReminders() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                locationRemindersDao.deleteAllReminders()
            }
        }
    }
}
