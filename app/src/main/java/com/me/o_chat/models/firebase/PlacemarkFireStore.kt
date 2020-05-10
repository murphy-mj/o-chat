package com.me.models.firebase

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.me.o_chat.models.Event
import com.me.o_chat.models.Station
import java.io.ByteArrayOutputStream
import java.io.File


class PlacemarkFireStore(val context: Context) {

  lateinit var db: DatabaseReference
  val stations = ArrayList<Station>()
  val events = ArrayList<Event>()



  fun findAll(): List<Station> {
    return stations
  }

  fun findStationById(id: String): Station? {
    val foundStation: Station? = stations.find { p -> p.sUid == id }
    return foundStation
  }

  fun findEventById(id: String): Event? {
    val foundEvent: Event? = events.find { p -> p.eUid == id }
    return foundEvent
  }


  fun fetchStations(stationsReady: () -> Unit,EventId:String) {
    val valueEventListener = object : ValueEventListener {
      override fun onCancelled(dataSnapshot: DatabaseError) {
      }
      override fun onDataChange(dataSnapshot: DataSnapshot) {
        dataSnapshot.children.mapNotNullTo(stations) { it.getValue<Station>(Station::class.java) as Station }
        stationsReady()
      }
    }

    db = FirebaseDatabase.getInstance().reference
    stations.clear()
      db.child("events").child(EventId).child("stations").addListenerForSingleValueEvent(valueEventListener)

    // userId = FirebaseAuth.getInstance().currentUser!!.uid
 //   db.child("hillforts").addListenerForSingleValueEvent(valueEventListener)
  }

}