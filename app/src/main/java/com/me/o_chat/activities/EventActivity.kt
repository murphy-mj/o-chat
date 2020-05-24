package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.*
import com.me.o_chat.R
import com.me.o_chat.models.Event
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_admin_user.*
import kotlinx.android.synthetic.main.content_event.*


// this lists the all the Events associated with Current user
// if the current user is an Organiser/Administrator then the next activity will be add a Station
// if the current user is Participant, then the next activity will be a GoogleMap with all the Stations

// we have to use a menu, rather than using Back because this is activated from two sources

class EventActivity : AppCompatActivity(), EventListener {

    lateinit var eventList2: ArrayList<Event>
    lateinit var eventList: ArrayList<Event>
    lateinit var event: Event
    lateinit var currentUser: User
    lateinit var db:DatabaseReference
    lateinit var userId :String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        db = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        supportActionBar?.title = "Events List"
        recyclerviewEvent.adapter
        val layoutManager = LinearLayoutManager(this)
        recyclerviewEvent.layoutManager = layoutManager as RecyclerView.LayoutManager
        eventList = ArrayList<Event>()
        eventList2 = ArrayList<Event>()

        Log.d("UserId is",userId)
        getCurrentUser({getEvents()},userId)

        recyclerviewEvent.adapter = EventAdapter(eventList, this)
        recyclerviewEvent.scrollToPosition(recyclerviewEvent.adapter?.itemCount!!)

    }

// if the Current User is an Organiser, the by Clicking the Events allows Stations to be added
// The Station Created requires the Event Information and the current users information
// if Participant, Googlemap onlt requires the Event's Details

    override fun onEventClick(event: Event) {
        if (currentUser.uType == "Admin") {
            val intent = Intent(this, StationCreateActivity::class.java)
            intent.putExtra("Kevent", event)
            intent.putExtra("Kuser", currentUser)
            startActivity(intent)
        } else {
            // else the participant does to the Map
            val intent = Intent(this, MapUserSationsAllActivity::class.java)
            intent.putExtra("Kevent", event)
            startActivity(intent)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_limited, menu)
        return super.onCreateOptionsMenu(menu)
    }


    // This Activity has only a limited menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {

            R.id.menu_main -> {
                val intentA = Intent(this, FirstAdminActivity::class.java)
                val intentP = Intent(this, FirstActivity::class.java)
                if(currentUser.uType == "Admin"){
                startActivity(intentA)
                } else {
                    startActivity(intentP)
                }

            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }


        }
            return super.onOptionsItemSelected(item)
    }

//1. first requirement is the current logged in User

    private fun getCurrentUser(stationsReady: () -> Unit,userId: String) {
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var user = p0.getValue(User::class.java)
                if(user != null) {
                    currentUser = user
                 //   getUserApprovedEvents()
                    stationsReady()
                }
            }
        }
        db.child("/users/${userId}").addListenerForSingleValueEvent(stationListener)
    }

//2. Once we have the current user, and the type of user
//   we can add the events to the eventList Array

    private fun getEvents() {
        eventList.clear()
        val ref = db.child("events")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    // Log.d("event", it.toString())
                    var event = it.getValue(Event::class.java)
                    var eCde: String = event!!.eCode.toString()
                    var eOrganiser: String = event!!.eOrganiser.toString()
                    Log.d("event ename", it.child("ename").value.toString())
                    Log.d("event OeCode", it.child("ecode").value.toString())
                    //   if (event != null && eOrganiser == userId) {
                    if (currentUser.uType != "Admin") {
                        // only allowing participant to be have access to the event when they have received approval from organiser
                        if (event != null && eCde == currentUser.uOrgRef && eCde != null && currentUser.uEvtApproval == "approved") {
                            eventList.add(event!!)
                        } else{
                         Toast.makeText(this@EventActivity, "status of event access ${currentUser.uEvtApproval}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (event != null && eOrganiser == userId) {
                            eventList.add(event!!)
                        }
                    }
                }
                recyclerviewEvent.adapter?.notifyDataSetChanged()
                ref.removeEventListener(this)

            }

        })
    }



// for participants, displays a list events that they have been approved for
// not going to use this now,  allowing for a time where Participants can do more than one event

    private fun  getUserApprovedEvents() {
        //The current user is an Administrator, and we are seeking all participants that belong to the event selected
        Log.d("in getUserApproved Events", "in get users")
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("in GetUsers logged in user type is ", " none")
                Log.d("in getUsers", "looking for participants of selected events")
                eventList2.clear()

                p0.children.forEach {
                    var eventRef = it
                    var avts = it.child("events")
                    avts.children.forEach {
                        Log.d("with admin events key", it.key.toString())
                        Log.d("with admin evenst value", it.value.toString())
                        var avts2 = it
                        Log.d("inside the event events key avts2", avts2.toString())
                        eventRef.children.forEach {
                            Log.d("inside the event events key", it.key.toString())
                            Log.d("inside the  event value", it.value.toString())

                        }
                    }

//                recyclerviewUser.adapter?.notifyDataSetChanged()
                }
            }
        }
        db.child("/users/${userId}/events").addListenerForSingleValueEvent(stationListener)

    }







        //  to be deleted

        //  extras.putString("sUid", event.eUid)
        //  extras.putString("Event",event.eName)
        //  intent.putExtras(extras)
        // val extras = Bundle()


        //    if (intent.hasExtra("Kevent")) {
        //         user = intent.getParcelableExtra("Kevent")
        //     }

        // station object sent to this acticity
        //      event = intent.getParcelableExtra("Kevent")

        // EventsAllUpToDate()

        //   st_button.setOnClickListener{
        //       Log.d("on Bln","Clicked Station Button")
        //       sendMessage()
        //       recyclerviewStation.adapter?.notifyDataSetChanged()
        //   }

        //  recyclerviewEvent.adapter = EventAdapter(eventList, this)
        //  recyclerviewEvent.scrollToPosition(recyclerviewEvent.adapter?.itemCount!!)

        // val ref = FirebaseDatabase.getInstance().getReference("/messages")
        // ref.addListenerForSingleValueEvent(object : ValueEventListener {
        //     override fun onCancelled(p0: DatabaseError) {
        //     }

        //     override fun onDataChange(p0: DataSnapshot) {
        //         p0.children.forEach {
        //             Log.d("message", it.toString())
        //             var message = it.getValue(Message::class.java)
        //             if(message != null) {
        //                 messageList.add(message!!)
        //                 recyclerviewChatLog.adapter?.notifyDataSetChanged()
        //                // rv_chatlog.adapter?.notifyDataSetChanged()
        //             }
        //         }
        //     }
//
        //      })
        //   Log.d("at userAdpter", "size =  ${messageList.size}")
        //   Log.d("at userAdpter", "size =  ${messageList[0].uText}")


        //  private fun sendMessage(){
        //      var mes = st_et.text.toString()
        //      val fromM = FirebaseAuth.getInstance().currentUser?.uid.toString()
        //      val  toM = station.sUid.toString()
        //    //  val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        //      val ref = FirebaseDatabase.getInstance().getReference("/teams/${fromM}/${toM}").push()
        //      val refTo = FirebaseDatabase.getInstance().getReference("/stations/${toM}/${fromM}").push()
        //      val refId = ref.key.toString()
        //      val refIdTo = refTo.key.toString()
        //      val messM:Message = Message(mes,"",refId,fromM,toM,System.currentTimeMillis()/1000)
        //      ref.setValue(messM)
        //      refTo.setValue(messM).addOnSuccessListener {
        //          cl_et.text.clear()
        //      }
        //      recyclerviewStation.adapter?.notifyDataSetChanged()
        //      recyclerviewStation.scrollToPosition(recyclerviewStation.adapter?.itemCount!!)
        //   }

//these are all the events stored in Event database
        //
        // val ref = FirebaseDatabase.getInstance().reference.child("users").child(adminId).child("events")


//        Log.d("at userAdpter", "size =  ${eventList.size}")


        //
        //  ref.addChildEventListener(object : ChildEventListener {

        //    override fun onCancelled(p0: DatabaseError) {
        //      }

        //      override fun onChildAdded(p0: DataSnapshot, p1: String?) {

        // each time messages db has a new entry, its add to the array list
        //            var event = p0.getValue(Event::class.java)
        //            if (event != null) {
        //                eventList.add(event)
        //                recyclerviewEvent.adapter?.notifyDataSetChanged()
        //            }
        //            ref.removeEventListener(this)

        //   }

        //    override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        //      }

        //        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        //       }

        //      override fun onChildRemoved(p0: DataSnapshot) {
//
        //         }
        //      })


        //   R.id.menu_new_station -> {
        //       val intent = Intent(this, NewStationActivity::class.java)
        //       Log.d("FA menu", "in Menu")
        //       startActivity(intent)
        //   }
        //    R.id.menu_create_station -> {
        //        val intent = Intent(this, StationCreateActivity::class.java)
        //        Log.d("FA menu", "in Menu create")
        //        startActivity(intent)
        //    }


        //  private fun EventsAllUpToDate() {
        //      val admin = FirebaseAuth.getInstance().currentUser?.uid.toString()
        //  val  eventId = event.eUid.toString()
        // val ref = FirebaseDatabase.getInstance().getReference("${admin}/events")
        //  val ref = FirebaseDatabase.getInstance().reference.child(admin).child("events")
        //     val ref = db.child("events")
        // /   ref.addChildEventListener(object : ChildEventListener {

        //        override fun onCancelled(p0: DatabaseError) {
        //        }

        //        override fun onChildAdded(p0: DataSnapshot, p1: String?) {

        // each time messages db has a new entry, its add to the array list
        //          var event = p0.getValue(Event::class.java)
        //          if (event != null) {
        //              eventList.add(event)
        //              recyclerviewEvent.adapter?.notifyDataSetChanged()
        //          }
        //          ref.removeEventListener(this)
        //       }

        //       override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        //       }

        //     override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        //     }

        //     override fun onChildRemoved(p0: DataSnapshot) {

        //     }
        //  })
        //  Log.d("at EventAdpter", "size =  ${eventList.size}")


        // }


        //recyclerviewNewMessage.adapter = UserAdapter(userList, this)
        //    recyclerviewNewMessage.adapter = UserAdapter(userList, this)

    }
