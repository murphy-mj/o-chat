package com.me.o_chat.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.me.o_chat.models.Event
import kotlinx.android.synthetic.main.activity_event_create_p1.*




class EventCreateActivity : AppCompatActivity() {
    var event = Event()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.me.o_chat.R.layout.activity_event_create_p1)

        event_add_btn.setOnClickListener() {
            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

            event.eName = eventNameC.text.toString()
            event.eDescription = eventDescription.text.toString()
            event.eOrganiser = userId
            event.eCode = publicEventCode.text.toString()

            val ref = FirebaseDatabase.getInstance().getReference("events").push()
            val refId = ref.getKey().toString()
            event.eUid = refId

            // The event is stored in two places in the database
            // The "events" directory and in the "users" in the evnts section.
            //store in events first to get the refId and then in the users
            // The user's event info is limited, primary the Event Code.
            // The evnt code is required to limit the number of participants that will be displayed for the event Admin

            FirebaseDatabase.getInstance().getReference("users/${userId}/events/${refId}").child("uEcode").setValue(event.eCode)
            FirebaseDatabase.getInstance().getReference("users/${userId}/events/${refId}").child("uEdescription").setValue(event.eDescription)

            ref.setValue(event).addOnSuccessListener {
                eventNameC.text.clear()
                eventDescription.text.clear()
                val intent:Intent = Intent(this, EventActivity::class.java)
                startActivity(intent)
            }

            // Once the event has be stored, they the are brought to a ist of Available events, Event Activity

        }
    }


    // to be deleted
    //  extras.putString("eUid", refId)
    //  extras.putString("Event",EventName)
    //  intent.putExtras(extras)
    //  Log.d("StationCreate","station add just been pressed off to MapsActivity3")

    //  recyclerviewEvent.adapter?.notifyDataSetChanged()

   // val extras = Bundle()
   // val EventName  = eventNameC.text.toString()

    //  lateinit var eventIn : Event

}