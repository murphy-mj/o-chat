package com.me.o_chat.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import org.jetbrains.anko.uiThread as uiThread
import org.jetbrains.anko.async


import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.me.o_chat.R
import com.me.o_chat.models.Event
import com.me.o_chat.models.Event_S
import com.me.o_chat.models.Message
import com.me.o_chat.models.Station
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.content_event.*
import kotlinx.android.synthetic.main.content_placemark_maps.*

class MapUserSationSelectedActivity : AppCompatActivity(),  com.google.android.gms.location.LocationListener,
    GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var myMarker: Marker
    private lateinit var stationMarker: Marker


    private var mLatitudeTextView: Long = 0L
    private var mLongitudeTextView: Long = 0L
    private lateinit var StationList2: ArrayList<Station>
    private lateinit var stationIn: Station
    private lateinit var mapFragment: SupportMapFragment
    private var currentLoc: LatLng = LatLng(0.0,0.0)

    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_map)


        if (intent.hasExtra("Kstation")) {
            stationIn = intent.getParcelableExtra("Kstation")
            Log.d("In Sation selected, Name","${stationIn?.sName}")
            Log.d("In Sation selected Desc","${stationIn?.sDescription}")

        }
//        Log.d("In Sation selected","${stationIn?.sDescription}")

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync {
            mMap = it
            mMap.setOnMarkerClickListener(this)
            mMap.getUiSettings().setZoomControlsEnabled(true)
         //   var optionsStation = MarkerOptions().title(stationIn?.sName).position(LatLng(stationIn?.sLocation.lat,stationIn?.sLocation.lng))
           // stationMarker = mMap.addMarker(optionsStation)
            var optionsMy = MarkerOptions().title("I am here").position(LatLng(0.0,0.0))
            myMarker = mMap.addMarker(optionsMy)

            var post1: LatLng = LatLng(stationIn?.sLocation.lat,stationIn?.sLocation.lng)
            var optionsStation :MarkerOptions = MarkerOptions()
            Log.d("latlng", "${stationIn?.sLocation.lat}  ${stationIn?.sLocation.lng} ")
            optionsStation = optionsStation.title(stationIn?.sName).position(post1)
            stationMarker = mMap.addMarker(optionsStation)
            setUpMap()
        }


      //  mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
      //  mapFragment.getMapAsync(this@MapUserSationSelectedActivity)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapUserSationSelectedActivity)

        locationCallback = object : LocationCallback() {
                 override fun onLocationResult(p0: LocationResult) {
                     super.onLocationResult(p0)

                     lastLocation = p0.lastLocation
                     placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
                 }
             }

         createLocationRequest()


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



    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }


        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))


            }

        }

    }


    private fun placeMarkerOnMap(location: LatLng) {
        var optionsMy = MarkerOptions().title("I am here").position(location)

        myMarker.remove()
        myMarker = mMap.addMarker(optionsMy)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }





        override fun onMarkerDragStart(marker: Marker) {
        }

        override fun onMarkerDrag(marker: Marker) {
        }

        override fun onMarkerDragEnd(marker: Marker) {

        }

        override fun onMarkerClick(marker: Marker): Boolean {



            return true
        }

        override fun onLocationChanged(location: Location) {

            val msg = "Updated Location: " +
                    java.lang.Double.toString(location.latitude) + "," +
                    java.lang.Double.toString(location.longitude)
            mLatitudeTextView = location.latitude.toLong()
            mLongitudeTextView = location.longitude.toLong()
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            // You can now create a LatLng Object for use with maps
            currentLoc = LatLng(location.latitude, location.longitude)
           // myMarker.position = currentLoc
            placeMarkerOnMap(currentLoc)
        }


        private fun startLocationUpdates() {
            //1
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return
            }
            //2
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */
            )
        }


            private fun createLocationRequest() {
                // 1
                locationRequest = LocationRequest()
                // 2
                locationRequest.interval = 10000
                // 3
                locationRequest.fastestInterval = 5000
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)

                // 4
                val client = LocationServices.getSettingsClient(this)
                val task = client.checkLocationSettings(builder.build())

                // 5
                task.addOnSuccessListener {
                    locationUpdateState = true
                    startLocationUpdates()
                }
                task.addOnFailureListener { e ->
                    // 6
                    if (e is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            e.startResolutionForResult(
                                this@MapUserSationSelectedActivity,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
            }


            // 1
            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                super.onActivityResult(requestCode, resultCode, data)
                if (requestCode == REQUEST_CHECK_SETTINGS) {
                    if (resultCode == Activity.RESULT_OK) {
                        locationUpdateState = true
                        startLocationUpdates()
                    }
                }
            }

            // 2
            override fun onPause() {
                super.onPause()
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            // 3
            public override fun onResume() {
                super.onResume()
                if (!locationUpdateState) {
                    startLocationUpdates()
                }
            }


        companion object {
            private const val LOCATION_PERMISSION_REQUEST_CODE = 1
            // 3
            private const val REQUEST_CHECK_SETTINGS = 2
        }




}





