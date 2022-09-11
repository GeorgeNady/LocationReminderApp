package com.udacity.project4.ui.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.db.LocationRemindersDatabase
import com.udacity.project4.db.RemindersLocalRepository
import com.udacity.project4.db.dto.ReminderTable
import com.udacity.project4.db.dto.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // TODO: Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var database: LocationRemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun getReminder(): ReminderTable {
        return ReminderTable(
                title = "title",
                description = "desc",
                location = "loc",
                latitude = 47.5456551,
                longitude = 122.0101731)
    }

    @Before
    fun setup() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
            LocationRemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.locationReminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = getReminder()
        repository.saveReminder(reminder)

        // WHEN  - reminder retrieved by ID.
        val result = repository.getReminder(reminder.id)

        // THEN - Same reminder is returned.
        assertThat(result is Resources.Success, `is`(true))
        result as Resources.Success


        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
        assertThat(result.data.location, `is`(reminder.location))
    }

    @Test
    fun deleteAllReminders_getReminderById() = runBlocking {
        val reminder = getReminder()
        repository.saveReminder(reminder)
        repository.deleteAllReminders()

        val result = repository.getReminder(reminder.id)


        assertThat(result is Resources.Error, `is`(true))
        result as Resources.Error
        assertThat(result.message, `is`("Reminder not found!"))

    }
}