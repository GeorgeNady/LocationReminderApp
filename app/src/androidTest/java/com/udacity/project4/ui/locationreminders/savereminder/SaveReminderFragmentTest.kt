package com.udacity.project4.ui.locationreminders.savereminder

import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_TOAST
import androidx.annotation.IntRange
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.db.LocalDB
import com.udacity.project4.db.ReminderDataSource
import com.udacity.project4.db.RemindersLocalRepository
import com.udacity.project4.ui.locationreminders.RemindersActivity
import com.udacity.project4.ui.locationreminders.reminderslist.ReminderDataDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var decorView: View

    private val giza = LatLng(21.000, 20.000)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var activityRule = activityScenarioRule<RemindersActivity>()

    @Before
    fun initRepository() {

        stopKoin()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }

        activityRule.scenario.onActivity {
            decorView = it.window.decorView
        }


        saveReminderViewModel = GlobalContext.get().koin.get()

    }


    @Test
    fun noTitle_fails() {
        val navController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)



        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        onView(withId(R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

    }

    private fun getReminder(): ReminderDataDomain {
        return ReminderDataDomain(
            title = "title",
            description = "desc",
            location = "loc",
            latitude = 47.5456551,
            longitude = 122.0101731
        )
    }

    @Test
    fun saveReminder_succeeds()  = runBlockingTest {
        val reminder = ReminderDataDomain(
            "Title",
            "Description",
            "Giza",
            giza.latitude,
            giza.longitude
        )

        val navController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(reminder.title))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(reminder.description))

        closeSoftKeyboard()

        runBlocking {
            saveReminderViewModel.saveReminder(reminder)
        }

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))

        onToast("Reminder Saved !").check(matches(isDisplayed()))

        /*onView(withText("Reminder Saved !"))
            .inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))*/


    }

    @Test
    fun saveReminder_failed()  = runBlockingTest {
        val reminder = ReminderDataDomain(
            null,
            "Description",
            "Giza",
            giza.latitude,
            giza.longitude
        )

        val navController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(reminder.description))

        closeSoftKeyboard()

        runBlocking {
            saveReminderViewModel.saveReminder(reminder)
        }

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))

        // onToast("Reminder Saved !").check(matches(isDisplayed()))

        onView(withText("Reminder Saved !"))
            .inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))


    }

}

fun onToast(
    text: Int,
    @IntRange(from = 1) maximumRetries: Int = 5
): ViewInteraction = onView(withText(text)).inRoot(ToastMatcher(maximumRetries))

fun onToast(
    text: String,
    @IntRange(from = 1) maximumRetries: Int = 5
): ViewInteraction = onView(withText(text)).inRoot(ToastMatcher(maximumRetries))

class ToastMatcher(private val maximumRetries: Int) : TypeSafeMatcher<Root>() {

    private var currentFailures: Int = 0

    override fun describeTo(description: Description?) {
        description?.appendText("no toast found after")
    }

    override fun matchesSafely(item: Root?): Boolean {
        val type: Int? = item?.windowLayoutParams?.get()?.type

        if(TYPE_TOAST == type || TYPE_APPLICATION_OVERLAY == type) {
            val windowToken = item.decorView.windowToken
            val appToken = item.decorView.applicationWindowToken

            if(windowToken == appToken) {
                return true
            }
        }

        return ++currentFailures >= maximumRetries
    }
}