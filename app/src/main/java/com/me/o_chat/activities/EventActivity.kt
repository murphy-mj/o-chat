package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.*
import com.me.o_chat.R
import com.me.o_chat.models.Event
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.content_event.*


// this lists the all the Events associated with Current user

class EventActivity : AppCompatActivity(), EventListener {


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
        Log.d("UserId is",userId)
        getCurrentUser({getEvents()},userId)

        recyclerviewEvent.adapter = EventAdapter(eventList, this)
        recyclerviewEvent.scrollToPosition(recyclerviewEvent.adapter?.itemCount!!)

    }



    override fun onEventClick(event: Event) {
        Log.d("Click Me","in On Event Click")
        if (currentUser.uType == "Admin") {
            val intent = Intent(this, StationCreateActivity::class.java)
            intent.putExtra("Kevent", event)
            intent.putExtra("Kuser", currentUser)
            startActivity(intent)
        } else {
            // else the participant does to the Map
            Log.d("Click Me","in On Event Click Participant")
            val intent = Intent(this, MapUserSationsAllActivity::class.java)
            intent.putExtra("Kevent", event)
            startActivity(intent)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // only admin should have access to create an event

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                Log.d("FA menu", "in Menu")
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            R.id.menu_new_event -> {
                if (currentUser.uType == "Admin") {
                    val intent = Intent(this, EventCreateActivity::class.java)
                    Log.d("Event menu", "in Menu")
                    startActivity(intent)
                } else {
                    // Toast
                }
            }

            R.id.menu_manage_members -> {
                if (currentUser.uType == "Admin") {
                    val intent = Intent(this,AdminUserEventActivity::class.java)
                    Log.d("manage members", "in Menu create")
                    startActivity(intent)


                } else {
                    //Toast

                }
            }
        }
            return super.onOptionsItemSelected(item)
    }


    private fun getCurrentUser(stationsReady: () -> Unit,userId: String) {
        Log.d("in get Current User", "getting Current User Object")
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var user = p0.getValue(User::class.java)
                Log.d("Current User all", "${user!!.uEmail}")
                if(user != null) {
                    currentUser = user
                    Log.d("Current User", "${currentUser.uEmail}")
                    stationsReady()
                }
            }
        }
        db.child("/users/${userId}").addListenerForSingleValueEvent(stationListener)
    }


    private fun getEvents() {
        eventList.clear()
        val ref = db.child("events")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("events get to match ", currentUser.uOrgRef.toString())
                p0.children.forEach {
                    // Log.d("event", it.toString())
                    var event = it.getValue(Event::class.java)
                    var eCde: String = event!!.eCode.toString()
                    var eOrganiser: String = event!!.eOrganiser.toString()
                    Log.d("event ename", it.child("ename").value.toString())
                    Log.d("event OeCode", it.child("ecode").value.toString())
                    //   if (event != null && eOrganiser == userId) {
                    if (currentUser.uType != "Admin") {
                        if (event != null && eCde == currentUser.uOrgRef && eCde != null) {
                            eventList.add(event!!)
                        }
                    } else {
                        if (event != null && eOrganiser == userId) {
                            eventList.add(event!!)
                        }
                    }
                }
                Log.d("event list size", eventList.size.toString())
                recyclerviewEvent.adapter?.notifyDataSetChanged()
                ref.removeEventListener(this)

            }

        })
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
