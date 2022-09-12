package com.udacity.project4.ui.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.db.ReminderDataSource
import com.udacity.project4.db.dto.ReminderTable
import com.udacity.project4.ui.locationreminders.reminderslist.ReminderDataDomain
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()

    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()

    val selectedPOI = MutableLiveData<PointOfInterest>()

    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()


    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    fun validateAndSaveReminder(reminderData: ReminderDataDomain): Boolean {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            return true
        }
        return false
    }


    fun saveReminder(reminderData: ReminderDataDomain) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderTable(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
        }
    }

    private fun validateEnteredData(reminderData: ReminderDataDomain): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

}