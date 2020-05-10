package com.me.o_chat.activities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentCallbacks
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import org.jetbrains.anko.uiThread as uiThread
import org.jetbrains.anko.async


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.me.o_chat.R
import com.me.o_chat.helpers.GeofencingConstants
import com.me.o_chat.helpers.createChannel
import com.me.o_chat.models.*
import com.me.o_chat.notify.GeofenceBroadcastReceiver
import kotlinx.android.synthetic.main.activity_admin_user.*
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.content_event.*
import java.lang.ref.Reference

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest


private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "MapUserSelected2Activity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1



class MapUserSationSelected3Activity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mLatitudeTextView: Long = 0L
    private var mLongitudeTextView: Long = 0L
    private var latoffset :Long= 0L
    private var lngoffset :Long= 0L

    private lateinit var StationList2: ArrayList<Station>
    private lateinit var eventIn: Event
    lateinit var stationOut :Station
    lateinit var stationIn :Station
    private lateinit var stationMarker: Marker

    private lateinit var mapFragment: SupportMapFragment
    private var currentLoc: LatLng = LatLng(0.0,0.0)
    private lateinit var myMarker: Marker
    private lateinit var ref1 : DatabaseReference
    private var LocStop  : Boolean = true

    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geofencingClient: GeofencingClient



    private val geofencePendingIntent: PendingIntent by lazy {
        Log.d("Select2","Im in geofencePendingIntent")
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    //val intent = Intent




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps4)
        StationList2 = ArrayList<Station>()
        stationIn = Station()

        if (intent.hasExtra("Kstation")) {
            stationIn = intent.getParcelableExtra("Kstation")
            eventIn = intent.getParcelableExtra("Kevent")
            Log.d("In Sation selected, Name","${stationIn?.sName}")
            Log.d("In Sation selected Desc","${stationIn?.sDescription}")
            ref1 = FirebaseDatabase.getInstance().reference.child("events").child(eventIn.eUid).child("stations").child(stationIn.sUid)

        }


        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MapUserSationSelected3Activity)

        geofencingClient = LocationServices.getGeofencingClient(this)
        createChannel(this )



        Log.d("station", "Hey I have arrived ${stationIn.sName}")

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("inside on Map, size sts lst", "${StationList2.size}")
        mMap = googleMap
        mMap.getUiSettings().setZoomControlsEnabled(true)
      //  mMap.setOnMarkerClickListener(this)

        var optionsMy = MarkerOptions().title("I am here").position(LatLng(0.0,0.0))
        myMarker = mMap.addMarker(optionsMy)
        var post1: LatLng = LatLng(stationIn?.sLocation.lat,stationIn?.sLocation.lng)
        var optionsStation :MarkerOptions = MarkerOptions()
        Log.d("latlng", "${stationIn?.sLocation.lat}  ${stationIn?.sLocation.lng} ")
        optionsStation = optionsStation.title(stationIn?.sName).position(post1)
        stationMarker = mMap.addMarker(optionsStation)

        setUpMap()
    }




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

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }


    override fun onDestroy() {
        super.onDestroy()
        removeGeofences()
    }

    private fun checkPermissionsAndStartGeofencing() {
        //if (viewModel.geofenceIsActive()) return
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
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {


            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForClue()
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
        * Adds a Geofence for the current clue if needed, and removes any existing Geofence. This
        * method should be called after the user has granted the location permission.  If there are
        * no more geofences, we remove the geofence and let the viewmodel know that the ending hint
        * is now "active."
        */
    private fun addGeofenceForClue() {

        val currentGeofenceData = stationIn

        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId("${currentGeofenceData.sUid}/${eventIn.eUid}")
            // Set the circular region of this geofence.
            .setCircularRegion(currentGeofenceData.sLocation.lat,
                currentGeofenceData.sLocation.lng,
                2000.0F
            )
              //  GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
           // )

            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        Log.d("Select2","just built geofence, for ${stationIn.sName}")
        Log.d("Select2"," circular region${geofence.toString()}")
        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()
        Log.d("Select2","just built geofence Request ${geofencingRequest.describeContents()}")

        // First, remove any existing geofences that use our pending intent
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        // Geofences added.
                        Log.d("Select2","deleted any  existing geofenses, and geofence request id ${geofence.requestId}")
                        Toast.makeText(this@MapUserSationSelected3Activity, R.string.geofences_added,
                            Toast.LENGTH_SHORT).show()
                        Log.d("Add Geofence", "${geofence.requestId}")
                        // Tell the viewmodel that we've reached the end of the game and
                        // activated the last "geofence" --- by removing the Geofence.
                       // viewModel.geofenceActivated()
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





