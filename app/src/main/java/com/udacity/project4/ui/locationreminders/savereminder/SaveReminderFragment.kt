package com.udacity.project4.ui.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.ui.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.ui.locationreminders.geofence.GeofenceConstants
import com.udacity.project4.ui.locationreminders.reminderslist.ReminderDataDomain
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


// Done: update it to the request permissions with registerForActivityResult()
class SaveReminderFragment : BaseFragment() {

    companion object {
        private const val TAG = "SaveReminderFragment"
    }

    override val baseViewModel: SaveReminderViewModel by inject()
    private val binding by lazy { FragmentSaveReminderBinding.inflate(layoutInflater) }
    private val runningQOrLater = SDK_INT >= Q
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderData: ReminderDataDomain

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceConstants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) checkDeviceLocationSettingsAndStartGeofence()
            else Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show()
        }

    private val locationSettingLauncher =
        registerForActivityResult(StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) checkDeviceLocationSettingsAndStartGeofence()
        }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////// INITIALIZATION
    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setDisplayHomeAsUpEnabled(true)
        binding.apply {
            viewModel = baseViewModel
            lifecycleOwner = this@SaveReminderFragment
            geofencingClient = LocationServices.getGeofencingClient(requireContext())
        }
        return binding.root
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////// LISTENERS
    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            /**
             * on select location
             */
            selectLocation.setOnClickListener {
                baseViewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            }

            /**
             * on save reminder listener
             */
            saveReminder.setOnClickListener {

                val title = baseViewModel.reminderTitle.value
                val description = baseViewModel.reminderDescription.value
                val locationName = baseViewModel.reminderSelectedLocationStr.value
                val latitude = baseViewModel.latitude.value
                val longitude = baseViewModel.longitude.value

                reminderData =
                    ReminderDataDomain(title, description, locationName, latitude, longitude)

                if (baseViewModel.validateAndSaveReminder(reminderData)) {
                    //addGeofence(reminderData)
                    checkPermissionsAndStartGeofencing()
                }

            }
        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////// FINISHER
    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        baseViewModel.onClear()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////// PERMISSION
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun isForegroundPermissionsGranted() =
        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)

    private fun isBackgroundPermissionsGranted() =
        if (runningQOrLater)
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        else true


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////// GEOFENCING
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * check if these below permissions is granted or not
     * * ACCESS_FINE_LOCATION
     * * ACCESS_BACKGROUND_LOCATION
     */
    private fun checkPermissionsAndStartGeofencing() {
        val foregroundLocationApproved = isForegroundPermissionsGranted()
        val backgroundPermissionApproved = isBackgroundPermissionsGranted()

        val permission = if (!foregroundLocationApproved)
            Manifest.permission.ACCESS_FINE_LOCATION
        else if (!backgroundPermissionApproved && runningQOrLater)
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        else
            null

        if (foregroundLocationApproved && backgroundPermissionApproved)
            checkDeviceLocationSettingsAndStartGeofence()
        else permission?.let { requestPermissionLauncher.launch(it) }

    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask
            .addOnCompleteListener {
                if (it.isSuccessful) addNewGeofence()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException && resolve) {
                    try {
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingLauncher.launch(intentSenderRequest)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Error getting location settings resolution:", e)
                    }
                } else {
                    Toast.makeText(requireContext(), "location required ", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun addNewGeofence() {
        if (this::reminderData.isInitialized) {

            val geofence = Geofence.Builder()
                .setRequestId(reminderData.id)
                .setCircularRegion(
                    reminderData.latitude!!,
                    reminderData.longitude!!,
                    GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()


            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) return

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    Log.d(TAG, "Location added!!!")
                    baseViewModel.validateAndSaveReminder(reminderData)
                }
                addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add location!!! Try again later!", Toast.LENGTH_SHORT).show()
                    if (it.message != null) {
                        Log.i(TAG, it.message.toString())
                    }
                }
            }
        }
    }


}
