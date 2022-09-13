package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.db.LocalDB
import com.udacity.project4.db.ReminderDataSource
import com.udacity.project4.db.RemindersLocalRepository
import com.udacity.project4.ui.authentication.AuthenticationViewModel
import com.udacity.project4.ui.locationreminders.RemindersActivity
import com.udacity.project4.ui.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.ui.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.ToastShower
import com.udacity.project4.utils.ToastShowerImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private val TAG = "RemindersActivityTest"
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val authViewModel: AuthenticationViewModel by inject()

    private var auth: FirebaseAuth

    //private lateinit var device: UiDevice

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val backgroundPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    init{
        //sign out
        auth = FirebaseAuth.getInstance()
        auth.signOut()
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                AuthenticationViewModel()
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            androidContext(getApplicationContext())
            loadKoinModules(myModule)
        }


        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun setUpIdlingResources() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Before
    fun login() = runBlocking {
        Log.d(TAG, "login running")
        auth.signInWithEmailAndPassword("tet@test.com", "123456789")
        delay(4000)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }


    // TODO: add End to End testing to the app
    @Test
    fun addReminder() = runBlocking {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Title1"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Desc1"), closeSoftKeyboard())
        onView(withId(R.id.selectLocation)).perform(click())

        delay(1000)

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.map)).perform(click())

        delay(1000)

        onView(withId(R.id.save_button)).perform(click())

        delay(1000)

        onView(withId(R.id.reminderTitle)).check(matches(withText("Title1")))
        onView(withId(R.id.reminderDescription)).check(matches(withText("Desc1")))

        activityScenario.close()
    }

    @Test
    fun saveReminder_showToastMessage_WorkingFine() = runBlocking {
        // GIVEN ::
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // WHEN ::
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("test title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("test description"))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.save_button)).perform(click())

        delay(3000)
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN ::
        onView(withText(R.string.reminder_saved))
            .inRoot(withDecorView(not(`is`(getActivity(activityScenario).window.decorView))))
            .check(matches(isDisplayed()))

        /*onView(withText(R.string.reminder_saved)).inRoot(ToastMatcher())
            .check(matches(isDisplayed()))*/

        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }



}

class ToastMatcher : TypeSafeMatcher<Root?>() {

    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    override fun matchesSafely(root: Root?): Boolean {
        root?.let {
            val type: Int = root.windowLayoutParams.get().type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken: IBinder = root.decorView.windowToken
                val appToken: IBinder = root.decorView.applicationWindowToken
                if (windowToken === appToken) {
                    //means this window isn't contained by any other windows.
                }
            }
        }
        return false
    }
}
