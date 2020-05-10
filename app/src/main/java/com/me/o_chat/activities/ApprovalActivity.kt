package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.*
import com.me.o_chat.R
import com.me.o_chat.models.Event
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_approval.*
import kotlinx.android.synthetic.main.content_event.*

class ApprovalActivity : AppCompatActivity() {

    lateinit var userIn : User
    lateinit var eventIn : Event
    lateinit var db: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approval)
        db = FirebaseDatabase.getInstance().reference
        var eventRef :String = ""

        if (intent.hasExtra("Kuser")) {
            userIn = intent.getParcelableExtra("Kuser")
            getEvent(userIn.uOrgRef)

        }



        btn_approve.setOnClickListener() {
            FirebaseDatabase.getInstance().reference.child("users").child(userIn.uUid.toString())
                .child("events").child(eventIn.eUid).setValue(eventIn).addOnSuccessListener{
                    val intent = Intent(this,AdminUserActivity::class.java)
                    startActivity(intent)
                }

        }

        btn_deny.setOnClickListener() {
            FirebaseDatabase.getInstance().reference.child("users").child(userIn.uUid.toString())
                .child("uOrgRef").setValue("denied").addOnSuccessListener {
                    val intent = Intent(this,AdminUserActivity::class.java)
                    startActivity(intent)
                }


        }


    }


        private fun  getEvent(eventRef:String) {
            val stationListener = object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        Log.d("event", it.toString())
                        var event = it.getValue(Event::class.java)
                        if (event != null && event.eCode == eventRef) {
                            eventIn = event
                        }
                    }

                }

            }

                db.child("/events").addListenerForSingleValueEvent(stationListener)
        }


}
