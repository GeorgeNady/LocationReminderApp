package com.udacity.project4.ui.locationreminders.reminderslist

import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: ((ReminderDataDomain) -> Unit)? = null) :
    BaseRecyclerViewAdapter<ReminderDataDomain>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}