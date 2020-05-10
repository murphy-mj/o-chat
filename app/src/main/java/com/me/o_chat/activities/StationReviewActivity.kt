package com.me.o_chat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.me.o_chat.activities.ImageActivity3
import com.me.o_chat.models.Station
import kotlinx.android.synthetic.main.activity_station_review.*
import kotlinx.android.synthetic.main.content_station_maps.*

class StationReviewActivity: AppCompatActivity() {
    private lateinit var currentStationID: String
    var eName: String = "wexford2020"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.me.o_chat.R.layout.activity_station_review)

        if (intent.hasExtra("sUid")) {
            currentStationID = intent.extras?.getString("sUid")!!
        }

        val user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        FirebaseDatabase.getInstance().getReference().child(user).child(eName).child("stations").child(currentStationID).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot == null) return
               var localStation = dataSnapshot.getValue(Station::class.java)
                Log.d("station snapshot",localStation.toString())
                stationRname.setText(localStation?.sName.toString())
                stationRdescription.setText(localStation?.sDescription.toString())






            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Station", "UpateSatation:onCancelled", databaseError.toException())
            }

        })


        station_ok_button.setOnClickListener({

            Log.d("Station","Going to Station Act")
            val extras = Bundle()
            val EventName  = eName
            val sUId = currentStationID
            val intent: Intent = Intent(this, NewStationActivity::class.java)
            extras.putString("sUid", sUId)
            extras.putString("Event",EventName)
            intent.putExtras(extras)
            startActivity(intent)
        })

     //  map_btn.setOnClickListener() {
     //       Log.d("Station","Going to Image Act")
     //       val extras = Bundle()
     //       val EventName  = eName
     //       val sUId = currentStationID
     //       val intent: Intent = Intent(this, ImageActivity3::class.java)
     //       extras.putString("sUid", sUId)
     //       extras.putString("Event",EventName)
     //       intent.putExtras(extras)
     //       startActivity(intent)
     //   }



    }

}