package com.me.o_chat.activities

import android.Manifest

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.me.o_chat.R
import com.me.o_chat.helpers.GeofencingConstants
import com.me.o_chat.helpers.createChannel
import com.me.o_chat.models.*
import com.me.o_chat.notify.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory


private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "MapUserSelected3Activity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1



class MapUserSationSelected3Activity : AppCompatActivity(), OnMapReadyCallback {

    // this activity just shows the participant current location and the station that the participant has seletcted
    // it sets the geolocation for that station
    // so when the participant arrives at the locacion a Notification will be received

    private lateinit var mMap: GoogleMap

    private lateinit var eventIn: Event
    lateinit var stationIn :Station
    private lateinit var stationMarker: Marker
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var myMarker: Marker
    private lateinit var ref1 : DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
//. For devices running Android Q (API 29) or later, you will have to ask for an additional background location permission.
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geofencingClient: GeofencingClient



    private val geofencePendingIntent: PendingIntent by lazy {

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps4)
        stationIn = Station()

        // when calling this Activity we also inluded the Event and Station Object
        // and based on that we create the database reference object
        if (intent.hasExtra("Kstation")) {
            stationIn = intent.getParcelableExtra("Kstation")
            eventIn = intent.getParcelableExtra("Kevent")
            ref1 = FirebaseDatabase.getInstance().reference.child("events").child(eventIn.eUid).child("stations").child(stationIn.sUid)

        }


        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MapUserSationSelected3Activity)

        geofencingClient = LocationServices.getGeofencingClient(this)
        createChannel(this )

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.getUiSettings().setZoomControlsEnabled(true)

        var post1: LatLng = LatLng(stationIn?.sLocation.lat,stationIn?.sLocation.lng)
        var optionsStation :MarkerOptions = MarkerOptions()
        optionsStation = optionsStation.title(stationIn?.sName).position(post1)
        stationMarker = mMap.addMarker(optionsStation)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(post1, 3f))

        setUpMap()
    }



// just checking that we are allowed access to current location informationm, Fine Location being more precise
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
       mMap.isMyLocationEnabled = true

    }


            // 2
    override fun onPause() {
                super.onPause()

            }

            // 3
    public override fun onResume() {
                super.onResume()
            }

    // Once we have selected a Station, we need to check the location permissions and then add a geofence to it
    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    // once we lave the activity, then there is no need to keep the geofence
    override fun onDestroy() {
        super.onDestroy()
        removeGeofences()
    }


    // A foreground service is a service, which works exactly same as a normal service (background service) and
    // the only difference is it has a notification attached to it in the notification tray of the device.
    //The main advantage of having a foreground service is its higher priority than its background version.

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }

    }


    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MapUserSationSelected3Activity,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                    //By giving a IntentSender to another application, you are granting it the right to perform the operation
                    // you have specified as if the other application was yourself (with the same permissions and identity).
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {


            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForStation()
            }
        }
    }


    companion object {
            private const val LOCATION_PERMISSION_REQUEST_CODE = 1
            // 3
            private const val REQUEST_CHECK_SETTINGS = 2

            internal const val ACTION_GEOFENCE_EVENT =
                "MapUserStationSelected3Activity.action.ACTION_GEOFENCE_EVENT"


        }
// using code from this source for geofencing
//https://codelabs.developers.google.com/codelabs/advanced-android-kotlin-training-geofencing
// hence the duplication on permission with setUpMap(), which is interested in the devices current location
// but it is needed for onMapReady call back

    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }


//resultCode. This code is different if the device is running Q (API 29) or later and determines whether you need to check
// for one permission (fine location) or multiple permissions (fine and background location) when the user returns from the permission request screen.
//Add a when statement to check the version running, and assign resultCode to
// REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE if the device is running Q (API 29) or later,
// and REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE, if not.


    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
            this@MapUserSationSelected3Activity,
            permissionsArray,
            resultCode
        )
    }


    /*
        * Adds a Geofence for the current station if needed, and removes any existing Geofence. This
        * method should be called after the user has granted the location permission.  I
        */
    private fun addGeofenceForStation() {

        val currentGeofenceData = stationIn

        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence. when the geofence radius is breached,
            // the geofence object will contain the EventId and the Station ID that we can extract
            .setRequestId("${currentGeofenceData.sUid}/${eventIn.eUid}")
            // Set the circular region of this geofence.
            .setCircularRegion(currentGeofenceData.sLocation.lat,
                currentGeofenceData.sLocation.lng,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry transitions.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        Log.d("Select3","just built geofence, for ${stationIn.sName}")
        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()
        Log.d("Select3","just built geofence Request ${geofencingRequest.describeContents()}")

        // First, remove any existing geofences that use our pending intent
        // geofencingClient is the location service
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        // Geofences added.
                        Toast.makeText(this@MapUserSationSelected3Activity, R.string.geofences_added,
                            Toast.LENGTH_SHORT).show()
                        Log.d("Geofence added", "${geofence.requestId}")

                    }
                    addOnFailureListener {
                        // Failed to add geofences.
                        Log.d("Failed to Add Geofence", "${geofence.requestId}")
                        Toast.makeText(this@MapUserSationSelected3Activity, R.string.geofences_not_added,
                            Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.w(TAG, it.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                Log.d(TAG, getString(R.string.geofences_removed))
                Toast.makeText(applicationContext, R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }




}





