package com.udacity.project4.ui.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.ui.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.properties.Delegates

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val baseViewModel: SaveReminderViewModel by inject()

    private val binding by lazy { FragmentSelectLocationBinding.inflate(layoutInflater) }

    private lateinit var map: GoogleMap

    companion object {
        private const val FINE_LOCATION_ACCESS_REQUEST_CODE = 1
    }

    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var locationName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        with(binding) {
            viewModel = baseViewModel
            lifecycleOwner = this@SelectLocationFragment

            setHasOptionsMenu(true)
            setDisplayHomeAsUpEnabled(true)
        }


        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        //Default location
        val myLocation = LatLng(-34.0, 151.0)
        val zoomLevel = 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoomLevel))
        map.uiSettings.isZoomControlsEnabled = true

        // Map Styling added #done
        setMapStyle(map)

        enableUserLocation()

        // user can select a location or POI #done
        setLocationClick(map)
        setPoiClickListener(map)

        onLocationSelected()

    }

    // Map Styling added #done
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(this::class.simpleName, "Style parsing failed.")
            }
        } catch (e:Exception) {
            Log.e(this::class.simpleName, "Can't find style. Error: ", e)
        }
    }

    private fun setPoiClickListener(map: GoogleMap) {

        map.setOnPoiClickListener { poi ->

            map.clear()
            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude
            locationName = poi.name

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            map.addCircle(
                CircleOptions()
                    .center(poi.latLng)
                    .radius(200.0)
                    .strokeColor(Color.argb(255, 255, 0, 0))
                    .fillColor(Color.argb(64, 255, 0, 0)).strokeWidth(4F)

            )

            poiMarker.showInfoWindow()


        }
    }

    // user can select a location or POI #done
    private fun setLocationClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            map.clear()
            // set lat
            latitude = latLng.latitude
            // set long
            longitude = latLng.longitude
            // set name
            locationName = "random place"
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )

            map.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(200.0)
                    .strokeColor(Color.argb(255, 255, 0, 0))
                    .fillColor(Color.argb(64, 255, 0, 0)).strokeWidth(4F)

            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

            )
        }
    }

    private fun onLocationSelected() {

        binding.saveButton.setOnClickListener {
            if (
                latitude != 0.0 && longitude != 0.0 && this::locationName.isInitialized
            ) {
                with(baseViewModel) {
                    latitude.value = this@SelectLocationFragment.latitude
                    longitude.value = this@SelectLocationFragment.longitude
                    reminderSelectedLocationStr.value = locationName
                    navigationCommand.value =
                        NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
                }

            } else {
                Toast.makeText(context, "Please select a location", Toast.LENGTH_LONG).show()
            }

        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableUserLocation() {

        when {
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                map.isMyLocationEnabled = true

                Toast.makeText(context, "Location permission is granted.", Toast.LENGTH_LONG).show()
            }
            (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }

            else ->
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            FINE_LOCATION_ACCESS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) enableUserLocation()
                else Toast.makeText(
                    context,
                    "Location permission was not granted.",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

    }


}



