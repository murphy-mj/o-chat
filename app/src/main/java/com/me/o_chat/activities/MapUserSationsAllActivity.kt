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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.me.o_chat.R
import com.me.o_chat.models.*
import kotlinx.android.synthetic.main.activity_admin_user.*
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.content_event.*
import java.lang.ref.Reference

class MapUserSationsAllActivity : AppCompatActivity(), OnMapReadyCallback,  com.google.android.gms.location.LocationListener,
    GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private var mLatitudeTextView: Long = 0L
    private var mLongitudeTextView: Long = 0L
    private lateinit var StationList2: ArrayList<Station>
    private lateinit var eventIn: Event
    lateinit var stationOut :Station
    lateinit var stationOut2 :Station
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)

        Log.d(" in MapUserStation All","in Map User Station All act")

        StationList2 = ArrayList<Station>()
        stationOut = Station()

        if (intent.hasExtra("Kevent")) {
            eventIn = intent.getParcelableExtra("Kevent")
            Log.d(" in MapUserStation All event in","${eventIn.eUid}")
            ref1 = FirebaseDatabase.getInstance().reference.child("events").child(eventIn.eUid).child("stations")
        }




        if(intent.hasExtra("stations")){
            StationList2 = intent.getParcelableArrayListExtra("stations")
        Log.d("Station List","in Maps SList parcel size, Sation 2 ${StationList2.size}")
            Log.d(" in Maps SList parcel size","${StationList2.size}")
        }

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MapUserSationsAllActivity)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapUserSationsAllActivity)
      //  async{
        fetchPlacemarks({ mapFragment.getMapAsync(this@MapUserSationsAllActivity) }, eventIn)
     //
     //    uiThread {
             Log.d("event", "Hey I have arrived ${eventIn.eName}")

             locationCallback = object : LocationCallback() {
                 override fun onLocationResult(p0: LocationResult) {
                     super.onLocationResult(p0)

                     lastLocation = p0.lastLocation
                     placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
                 }
             }
          //   if(LocStop == false) {
             createLocationRequest()
          //   }
       //  }}

    }



    fun fetchPlacemarks(stationsReady: () -> Unit, eventIn: Event) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.mapNotNullTo(StationList2) { it.getValue<Station>(Station::class.java) as Station }
                stationsReady()
            }
        }
        StationList2.clear()

        val ref = FirebaseDatabase.getInstance().reference.child("events").child(eventIn.eUid)
            .child("stations")
        ref.addListenerForSingleValueEvent(valueEventListener)

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
        mMap.setOnMarkerClickListener(this)
        var optionsMy = MarkerOptions().title("I am here").position(LatLng(0.0,0.0))
        myMarker = mMap.addMarker(optionsMy)

        setUpMap()
    }


    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        StationList2.forEach {
            Log.d("Station obj","${it.toString()}")
            var post1: LatLng = LatLng(0.0,0.0)
            var options :MarkerOptions = MarkerOptions()
            Log.d("latlng", "${it.sLocation.lat}  ${it.sLocation.lng}  ${it.sUid}")
            post1 = LatLng(it.sLocation.lat, it.sLocation.lng)
            options = options.title(it.sUid).position(post1)
            mMap.addMarker(options)
         //   mMap.addMarker(options).tag = it
            Log.d("adding markers","from list ${options.title}")
                //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(post1, 3f))
        }

       mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
              //  var optionsMy = MarkerOptions().title("I am here").position(currentLatLng)

              //  if (myMarker == null) {
              //      myMarker = mMap.addMarker(optionsMy)
              //  }
              //  myMarker.position = currentLatLng
                placeMarkerOnMap(currentLatLng)
          //      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 8f))

            }

        }

    }


    private fun placeMarkerOnMap(location: LatLng) {
        var optionsMy = MarkerOptions().title("I am here").position(location)

        myMarker.remove()
        myMarker = mMap.addMarker(optionsMy)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 5f))
    }









        fun doPopulateMap(map: GoogleMap, stations: List<Station>) {
            map.uiSettings.setZoomControlsEnabled(true)
            mMap = map
            stations.forEach {
                val loc = LatLng(it.sLocation.lat, it.sLocation.lng)
                val options = MarkerOptions().title(it.sName).position(loc)
                map.addMarker(options).tag = it
                // map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, it.location.zoom))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 3f))
            }

        }


        override fun onMarkerDragStart(marker: Marker) {
        }

        override fun onMarkerDrag(marker: Marker) {
        }

        override fun onMarkerDragEnd(marker: Marker) {

        }

        override fun onMarkerClick(marker: Marker): Boolean {
            Log.d("On Marker Clicked - before","${marker?.title}")
          //  packingActivity(marker)
            fetchPlacemarks2({PackingStationActivity()},marker.title)
         //  stationOut = ref1.child(marker.title)

            Log.d("On Marker Clicked - return","${marker.title}")
                return false
            // camera does not zoom in on marker selected if return is true
        }

    fun packingActivity (marker: Marker){
        Log.d("On Marker Clicked - pack","${marker?.title}")
     //   lateinit var stationOut :Station
      //  val id = marker.title
        async{
        ref1.child(marker.title).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                stationOut = p0.getValue(Station::class.java)!!
                Log.d("On Marker Clicked - desc","${stationOut?.sDescription}")
            }
        })
            ref1.child(marker.title).removeEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                }
            })


        uiThread {

            Log.d("Packing Station - desc", "${stationOut?.sDescription}")
            if (stationOut != null) {
                val intent = Intent(this@MapUserSationsAllActivity,MapUserSationSelected2Activity::class.java)
                intent.putExtra("Kstation", stationOut)
                startActivity(intent)
            }
        }}
    }


    private fun fetchPlacemarks2(stationsReady: () -> Unit, idIn: String) {
        val stationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                stationOut2 = dataSnapshot.getValue(Station::class.java) as Station

                //dataSnapshot.children.forEach {
                //    Log.d("Station id ", "${idIn}")
                //    stationOut2  = it.getValue<Station>(Station::class.java)!! as Station
                //    Log.d("Station selectd ", " station des ${stationOut2?.sDescription}")
               // }
                stationsReady()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        ref1.child(idIn).addListenerForSingleValueEvent(stationListener)
    }





    private fun PackingStationActivity() {
        if(stationOut2 !=null) {
        val intent = Intent(this,MapUserSationSelected3Activity::class.java)
        intent.putExtra("Kstation",stationOut2 )
        intent.putExtra("Kevent",eventIn )
        Log.d("Station List","PACKING Station ${stationOut2.sUid}")
        startActivity(intent)
        }
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
            myMarker.position = currentLoc
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
                                this@MapUserSationsAllActivity,
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





