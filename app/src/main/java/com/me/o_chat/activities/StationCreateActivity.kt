package com.me.o_chat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.me.o_chat.activities.MapsActivity3
import com.me.o_chat.R
import com.me.o_chat.models.Event
import com.me.o_chat.models.Station
import kotlinx.android.synthetic.main.activity_chatlog.*

import kotlinx.android.synthetic.main.activity_station_create_p1.*



class StationCreateActivity : AppCompatActivity() {

    var station: Station = Station()
    lateinit var eventIn : Event
    val IMAGE_REQUEST = 5
    val LOCATION_REQUEST = 3
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.me.o_chat.R.layout.activity_station_create_p1)
        // setSupportActionBar(toolbar)

        if (intent.hasExtra("Kevent")) {
            //eventIn = intent.getParcelableExtra("Kevent")
            eventIn = intent.getParcelableExtra("Kevent")
            eNameC.setText(eventIn.eName.toString())

        }




        //station_location_btn.setOnClickListener() {
        //    val intent:Intent = Intent(this, MapsActivity3::class.java)
           // intent.putExtra("name", station.sName)
        //    startActivity(intent)
           // startActivity (intentFor<MapsActivity>().putExtra("name", station.sName))
        //}

        station_add_btn.setOnClickListener() {
            val extras = Bundle()
            val EventName  = eNameC.text.toString()
            val EventId  = eventIn.eUid.toString()
            station.sName = stationCname.text.toString()
            station.sDescription = stationCdescription.text.toString()
           station.sEvent = EventId
            val admin = FirebaseAuth.getInstance().currentUser?.uid.toString()
           // val ref = FirebaseDatabase.getInstance().getReference("/${admin}/${EventId}/stations").push()
            val ref = FirebaseDatabase.getInstance().getReference("events/${EventId}/stations").push()
            val refId = ref.getKey().toString()
            station.sUid = refId
            ref.setValue(station).addOnSuccessListener {
                eNameC.text.clear()
                stationCname.text.clear()
                stationCdescription.text.clear()

                val intent:Intent = Intent(this, MapsActivity3::class.java)
                extras.putString("sUid", refId)
                extras.putString("EventN",eventIn.eName)
                extras.putString("EventId",eventIn.eUid)
                intent.putExtras(extras)
                Log.d("StationCreate","station add just been pressed off to MapsActivity3")
                startActivity(intent)
            }
           // recyclerviewChatLog.adapter?.notifyDataSetChanged()
           // recyclerviewChatLog.scrollToPosition(recyclerviewChatLog.adapter?.itemCount!!)
        }
    }
}