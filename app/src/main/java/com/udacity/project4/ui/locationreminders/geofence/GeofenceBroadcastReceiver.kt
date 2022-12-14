package com.udacity.project4.ui.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.udacity.project4.ui.locationreminders.geofence.GeofenceConstants.ACTION_GEOFENCE_EVENT

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT)
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
}

