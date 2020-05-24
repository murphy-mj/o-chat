package com.me.o_chat.activities

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.me.o_chat.R
import com.me.o_chat.R.layout.activity_maps3
import com.me.o_chat.R.layout.content_station_maps
import com.me.o_chat.models.Station
import kotlinx.android.synthetic.main.content_station_maps.*

class MapsActivity3 : AppCompatActivity(), OnMapReadyCallback, com.google.android.gms.location.LocationListener,
    GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private var mLatitudeTextView: Long  = 0L
    private var mLongitudeTextView: Long = 0L
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentStationID: String
    private lateinit var currentStation: Station
    private lateinit var eId: String
    private lateinit var eName: String
    private lateinit var marker1: Marker
    //var location : Location = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(activity_maps3)
        if (intent.hasExtra("sUid")) {
            currentStationID = intent.extras?.getString("sUid")!!
            eId = intent.extras?.getString("EventId")!!
            eName = intent.extras?.getString("EventN")!!
        }
       // if (intent.hasExtra("EventName")) {
       //     eName = intent.extras?.getString("EventName")!!
       // }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
      val mapFragment = supportFragmentManager
         .findFragmentById(R.id.map) as SupportMapFragment
         mapFragment.getMapAsync(this)
   //    mapView.getMapAsync(this)


        //once the Admin has moved the marker to the desire location,
        // this will return to list events
        map_btn.setOnClickListener() {
            val intent: Intent = Intent(this, EventActivity::class.java)

                startActivity(intent)
            }
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
        mMap = googleMap
        mMap.getUiSettings().setZoomControlsEnabled(true)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney").draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        Log.d("station", "On Map Ready")
        //.marker.position.latitude.toString())

    }



    override fun onMarkerDragStart(marker: Marker) {
    }

    override fun onMarkerDrag(marker: Marker) {


    }

    // sets the location of the newly created Station by moving the marker on the map.

    override fun onMarkerDragEnd(marker: Marker) {
        Log.d("station on Marker Drag End",marker.toString())
        marker1 = marker
        val admin = FirebaseAuth.getInstance().currentUser?.uid.toString()

      //  FirebaseDatabase.getInstance().getReference().child(admin).child(eId).child("stations")
        FirebaseDatabase.getInstance().getReference().child("events").child(eId).child("stations")
            .child(currentStationID).child("slocation").addValueEventListener(object:ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
             //   val localSation = dataSnapshot.getValue(Station::class.java)
             //   Log.d("station snapshot",localSation.toString())
                Log.d("station Snapshot Location", dataSnapshot.getRef().child("lat").toString())
                dataSnapshot.getRef().child("lat").setValue(marker1.position.latitude!!)
                dataSnapshot.getRef().child("lng").setValue(marker1.position.longitude!!)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Station", "UpateSatation:onCancelled", databaseError.toException())
            }

    })

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
       // val latLng = LatLng(location.latitude, location.longitude)


    }

    // map_btn.setOnClickListener() {
    //            Log.d("Station","Going to Image Act3 from MapsActivity3")
    //            val extras = Bundle()
    //            val intent: Intent = Intent(this, ImageCaptureActivity::class.java)
    //                extras.putString("sUid", currentStationID)
    //                extras.putString("EventN",eName)
    //                extras.putString("EventId",eId)
    //                intent.putExtras(extras)
    //                startActivity(intent)
    //            }
    //        }

}

