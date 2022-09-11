package com.udacity.project4.di

import com.udacity.project4.db.ReminderDataSource
import com.udacity.project4.db.LocalDB
import com.udacity.project4.db.RemindersLocalRepository
import com.udacity.project4.ui.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.ui.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val myModule = module {
    //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
    viewModel {
        RemindersListViewModel(
            get(),
            get() as ReminderDataSource
        )
    }
    //Declare singleton definitions to be later injected using by inject()
    single {
        //This view model is declared singleton to be used across multiple fragments
        // viewModel { SaveReminderViewModel(get(), get() as ReminderDataSource) }
        SaveReminderViewModel(
            get(),
            get() as ReminderDataSource
        )
    }
    single { RemindersLocalRepository(get()) as ReminderDataSource }
    single { LocalDB.createRemindersDao(androidApplication()) }
}