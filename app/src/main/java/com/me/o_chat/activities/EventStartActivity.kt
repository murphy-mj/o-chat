package com.me.o_chat.activities

import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.RemoteMessage
import com.me.o_chat.*
import com.me.o_chat.EventListener
import com.me.o_chat.models.Event
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.content_event.*
import kotlin.collections.ArrayList

import android.app.*
import com.me.o_chat.notify.NotificationHelper.createNotificationChannel


// this lists the all the Events associated with Current user
// if the current user is an Organiser/Administrator then the next activity will be add a Station
// if the current user is Participant, then the next activity will be a GoogleMap with all the Stations

// we have to use a menu, rather than using Back because this is activated from two sources

class EventStartActivity : AppCompatActivity(), EventListener {

    lateinit var eventList2: ArrayList<Event>
    lateinit var eventList: ArrayList<Event>
    lateinit var event: Event
    lateinit var currentUser: User
    lateinit var db:DatabaseReference
    lateinit var userId :String
    lateinit var items : ArrayList<String>
    lateinit var builderAlert : AlertDialog.Builder
    lateinit var selectedList : ArrayList<Int>
    lateinit var selectedStrings : ArrayList<String>
    lateinit var startEvent : String
    private var notificationManager: NotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.me.o_chat.R.layout.activity_event)
        db = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        items = ArrayList<String>()
        builderAlert = AlertDialog.Builder(this)
        selectedList = ArrayList<Int>()
        selectedStrings = ArrayList<String>()
        event = Event()


        supportActionBar?.title = "Events List - Select to Start"
        recyclerviewEvent.adapter
        val layoutManager = LinearLayoutManager(this)
        recyclerviewEvent.layoutManager = layoutManager as RecyclerView.LayoutManager
        eventList = ArrayList<Event>()
        eventList2 = ArrayList<Event>()

        Log.d("UserId is",userId)
        getCurrentUser2({getEvents()},userId)
      //  getCurrentUser2({getEvents({getEventList({ withSingleChoiceList({getCurrentUser({setStartTime({createNotificationChannel()})},userId)}) })})},userId)
       //getEventList({ withSingleChoiceList({getCurrentUser({setStartTime({createNotificationChannel()})},userId)}) })

        recyclerviewEvent.adapter = EventAdapter(eventList, this)
        recyclerviewEvent.scrollToPosition(recyclerviewEvent.adapter?.itemCount!!)

    }


// by Clicking the Event sets the start time on the event


    override fun onEventClick(event1: Event) {
        event = event1
        Log.d("Event is","${event.eName}")
        startEvent = event.eCode
        Log.d("Event Code is","${event.eCode}")
        setStartTime()
        Toast.makeText(applicationContext, "Satrt time has been set for  ${event.eName}",Toast.LENGTH_SHORT).show()
      //  setStartTime({createNotificationChannel()})
     //   val intent = Intent(this,FirstAdminActivity::class.java)
     //   startActivity(intent)




    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.me.o_chat.R.menu.nav_limited, menu)
        return super.onCreateOptionsMenu(menu)
    }


    // This Activity has only a limited menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {

            com.me.o_chat.R.id.menu_main -> {
                val intentA = Intent(this, FirstAdminActivity::class.java)
                val intentP = Intent(this, FirstActivity::class.java)
                if(currentUser.uType == "Admin"){
                startActivity(intentA)
                } else {
                    startActivity(intentP)
                }

            }
            com.me.o_chat.R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }


        }
            return super.onOptionsItemSelected(item)
    }

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
                            Toast.makeText(this@EventStartActivity, "status of event access ${currentUser.uEvtApproval}", Toast.LENGTH_SHORT).show()
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



    private fun getCurrentUser2(stationsReady: () -> Unit,userId: String) {
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


    // private fun setStartTime(stationsReady: () -> Unit) {
    private fun setStartTime() {
        Log.d("In Set Time","${event.eUid}")
        FirebaseDatabase.getInstance().reference.child("events").child("${event.eUid}")
            .child("estartTime").setValue(System.currentTimeMillis()/1000)
        //   stationsReady()

        //  sendNotification()

    }

}

//private const val NOTIFICATION_ID = 37
//private const val CHANNEL_ID = "com.me.o_chat"


    //  //    getEventList({ withSingleChoiceList({getCurrentUser({setStartTime({createNotificationChannel()})},userId)}) })
//
//    //    if (currentUser.uType == "Admin") {
//    //        val intent = Intent(this, StationCreateActivity::class.java)
//    //        intent.putExtra("Kevent", event)
//    //        intent.putExtra("Kuser", currentUser)
//    //        startActivity(intent)
//    //    } else {
//    //        // else the participant does to the Map
//    //        val intent = Intent(this, MapUserSationsAllActivity::class.java)
//    //        intent.putExtra("Kevent", event)
//    //        startActivity(intent)
//     //   }

   // private fun createNotificationChannel() {
    //        // Create the NotificationChannel, but only on API 26+ because
    //        // the NotificationChannel class is new and not in the support library
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    //            val name = startEvent
    //            //  val descriptionText = getString(com.me.o_chat.R.string.channel_description)
    //            val descriptionText = "Event Starting Notification"
    //            val importance = NotificationManager.IMPORTANCE_DEFAULT
    //            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
    //                description = descriptionText
    //            }
    //            // Register the channel with the system
    //
    //            notificationManager?.createNotificationChannel(channel)
    //        }
    //    }







    //1. get the events that the Organiser has created or the participant has been accepted for
//  from these events we can get the public Event Codes

  //  private fun  getEventList(stationsReady: () -> Unit) {
    //        eventList.clear()
    //        Log.d("in Get Event List", "getting a list of events associated with Admin/participant")
    //        val stationListener = object : ValueEventListener {
    //            override fun onCancelled(p0: DatabaseError) {
    //            }
    //
    //            override fun onDataChange(p0: DataSnapshot) {
    //
    //                p0.children.forEach {
    //                    Log.d("getevent it", it.toString())
    //                    var evt : Event = it.getValue(Event::class.java)!!
    //                    eventList.add(evt)
    //                    var tst = it
    //                    tst.children.forEach {
    //                        Log.d("with getevntsList key", it.key.toString())
    //                        Log.d("with geteventList value", it.value.toString())
    //                        if (it.key.toString() == "uEcode" ||  it.key.toString() == "ecode") {
    //                            var i = items.size
    //                            // items.set(i,it.value.toString())
    //                            items.add(i,it.value.toString())
    //
    //
    //                        }
    //                    }
    //                }
    //                stationsReady()
    //                Log.d("with getEvent items size",items.size.toString())
    //
    //            }
    //        }
    //        Log.d("Get Event list, c user","${userId}")
    //        db.child("/users/${userId}/events").addListenerForSingleValueEvent(stationListener)
    //
    //    }


    //2. Once we have the Eents codes, the current logged in person can select which Event they want


    //fun withSingleChoiceList(stationsReady: () -> Unit) {
    //
    //        val listP = arrayOfNulls<String>(items.size)
    //        items.toArray(listP)
    //        builderAlert.setTitle("Please select one Event Code")
    //        builderAlert.setSingleChoiceItems(listP, -1) {
    //                dialog, i -> startEvent = listP[i]!!
    //
    //            dialog.dismiss()
    //            Log.d("Event Start", "Event Code selected ${startEvent}")
    //            Toast.makeText(applicationContext, "Event selected =: ${startEvent} " , Toast.LENGTH_SHORT).show()
    //            stationsReady()
    //        }
    //        builderAlert.setNeutralButton("Cancel") { dialogInterface, which ->
    //            startEvent = ""
    //            dialogInterface.cancel()
    //        }
    //
    //        builderAlert.show()
    //
    //
    //    }










//3. first requirement is the current logged in User

    //private fun getCurrentUser(stationsReady: () -> Unit,userId: String) {
    //        val stationListener = object : ValueEventListener {
    //
    //            override fun onCancelled(p0: DatabaseError) {
    //            }
    //
    //            override fun onDataChange(p0: DataSnapshot) {
    //                var user = p0.getValue(User::class.java)
    //                if(user != null) {
    //                    currentUser = user
    //                 //   getUserApprovedEvents()
    //                    stationsReady()
    //                }
    //            }
    //        }
    //        db.child("/users/${userId}").addListenerForSingleValueEvent(stationListener)
    //    }

//4. Once we have the current user, and the type of user
//   we can add the events to the eventList Array


    // We have the Selected event we just need to sets its value an send a notification to Participants of Event





// getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//
//        val intent = Intent(this, FirstAdminActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
//
//        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(com.me.o_chat.R.drawable.rounded_btn)
//            .setContentTitle("My notification")
//            .setContentText("Much longer text that cannot fit one line...")
//            .setStyle(NotificationCompat.BigTextStyle()
//            .bigText("Much longer text that cannot fit one line..."))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
















//private fun sendNotification() {
//    val topic = "${startEvent}"
//
//// See documentation on defining a message payload.
//
//
//// Send a message to the devices subscribed to the provided topic.
//    val response = FirebaseMessaging.getInstance().send(message)
//// Response is a message ID string.
//    println("Successfully sent message: $response")
//
//
//    var remoteMessage :String = "The event has started, good luck"
//    Log.d("TCM", "Send Notification")
//    val intent = Intent(this, FirstAdminActivity::class.java)
//    //nstead of launching a new instance of that activity, all of the other activities
//    // on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//    val pendingIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT)
//
//
//    //intent,PendingIntent.FLAG_ONE_SHOT)
//
//    //PendingIntent.FLAG_UPDATE_CURRENT)
//
//    // PendingIntent.FLAG_ONE_SHOT)
//
//    val notificationBuilder = NotificationCompat.Builder(this)
//        .setContentText(remoteMessage)
//        .setAutoCancel(true)
//        .setSmallIcon(com.me.o_chat.R.mipmap.ic_launcher)
//        .setContentIntent(pendingIntent)
//    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//    notificationManager.notify(0/* ID of notification */, notificationBuilder.build())
//}
//
//
//private fun setChannelId() {
//    // 1
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//        // 2
//        val channelId = "${startEvent}"
//        val channel = NotificationChannel(channelId, "StartTime baby",)
//        channel.description = "Starting Event Channel"
//        channel.setShowBadge(showBadge)
//
//        // 3
//        val notificationManager = context.getSystemService(NotificationManager::class.java)
//        notificationManager.createNotificationChannel(channel)
//    }
//}


//    private fun getEvent() {
//        eventList.clear()
//        val ref = db.child("events")
//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//
//                p0.children.forEach {
//                    // Log.d("event", it.toString())
//                    var event = it.getValue(Event::class.java)
//                    var eCde: String = event!!.eCode.toString()
//                    var eOrganiser: String = event!!.eOrganiser.toString()
//                    Log.d("event ename", it.child("ename").value.toString())
//                    Log.d("event OeCode", it.child("ecode").value.toString())
//                    //   if (event != null && eOrganiser == userId) {
//                    if (currentUser.uType != "Admin") {
//                        // only allowing participant to be have access to the event when they have received approval from organiser
//                        if (event != null && eCde == currentUser.uOrgRef && eCde != null && currentUser.uEvtApproval == "approved") {
//                            eventList.add(event!!)
//                        } else{
//                         Toast.makeText(this@EventStartActivity, "status of event access ${currentUser.uEvtApproval}", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        if (event != null && eOrganiser == userId) {
//                            eventList.add(event!!)
//                        }
//                    }
//                }
//                recyclerviewEvent.adapter?.notifyDataSetChanged()
//                ref.removeEventListener(this)
//
//            }
//
//        })
//    }
//
//
//
//
//
//
//// for participants, displays a list events that they have been approved for
//// not going to use this now,  allowing for a time where Participants can do more than one event
//
//    private fun  getUserApprovedEvents() {
//        //The current user is an Administrator, and we are seeking all participants that belong to the event selected
//        Log.d("in getUserApproved Events", "in get users")
//        val stationListener = object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                Log.d("in GetUsers logged in user type is ", " none")
//                Log.d("in getUsers", "looking for participants of selected events")
//                eventList2.clear()
//
//                p0.children.forEach {
//                    var eventRef = it
//                    var avts = it.child("events")
//                    avts.children.forEach {
//                        Log.d("with admin events key", it.key.toString())
//                        Log.d("with admin evenst value", it.value.toString())
//                        var avts2 = it
//                        Log.d("inside the event events key avts2", avts2.toString())
//                        eventRef.children.forEach {
//                            Log.d("inside the event events key", it.key.toString())
//                            Log.d("inside the  event value", it.value.toString())
//
//                        }
//                    }
//
////                recyclerviewUser.adapter?.notifyDataSetChanged()
//                }
//            }
//        }
//        db.child("/users/${userId}/events").addListenerForSingleValueEvent(stationListener)
//
//    }






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


