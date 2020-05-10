package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.me.o_chat.R
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.activity_event.*
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.activity_station.*
import kotlinx.android.synthetic.main.activity_station_create_p1.*
import kotlinx.android.synthetic.main.content_event.*
import com.me.o_chat.EventAdapter
import com.me.o_chat.EventListener
import com.me.o_chat.models.*
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.io.Serializable


// this lists the message activity on a selected Station
// and allows you to send a message to the Station

class UserEventActivity : AppCompatActivity(), EventListener {


    lateinit var  eventList : ArrayList<Event>
    private lateinit var StationList: ArrayList<Station>
    lateinit var  event_sList : ArrayList<Event_S>
    lateinit var event : Event
    lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.me.o_chat.R.layout.activity_event)
        StationList = ArrayList<Station>()
   //     if (intent.hasExtra("Kuser")) {
          //  user = intent.getParcelableExtra("Kevent")
    //    }

       // station object sent to this acticity
  //      event = intent.getParcelableExtra("Kevent")

        supportActionBar?.title = "Events List"
        recyclerviewEvent.adapter

       // rv_chatlog.adapter

         eventList = ArrayList<Event>()

        // getUsers()
      //  Log.d("getEvents", "the number of m sent to this Station  ${messageList.size}")


        val layoutManager = LinearLayoutManager(this)
        recyclerviewEvent.layoutManager = layoutManager as RecyclerView.LayoutManager


        Log.d("event", "In UserEventAct")
//these are all the events assocated with the current user
        val adminId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(adminId).child("events")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d("event", it.toString())
                    var event = it.getValue(Event::class.java)
                    if(event != null) {
                        eventList.add(event!!)
                        recyclerviewEvent.adapter?.notifyDataSetChanged()
                    }
                }
                ref.removeEventListener(this)

            }

        })

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


        recyclerviewEvent.adapter = EventAdapter(eventList, this)
        recyclerviewEvent.scrollToPosition(recyclerviewEvent.adapter?.itemCount!!)



        //recyclerviewNewMessage.adapter = UserAdapter(userList, this)
        //    recyclerviewNewMessage.adapter = UserAdapter(userList, this)
    }




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


    private fun EventsAllUpToDate(){
        val admin = FirebaseAuth.getInstance().currentUser?.uid.toString()
      //  val  eventId = event.eUid.toString()
       // val ref = FirebaseDatabase.getInstance().getReference("${admin}/events")
        val ref = FirebaseDatabase.getInstance().reference.child(admin).child("events")
            ref.addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                    // each time messages db has a new entry, its add to the array list
                    var event = p0.getValue(Event::class.java)
                    if(event != null) {
                        eventList.add(event)
                        recyclerviewEvent.adapter?.notifyDataSetChanged()
                    }
                    ref.removeEventListener(this)
            }

             override fun onChildMoved(p0: DataSnapshot, p1: String?) {

             }

             override fun onChildChanged(p0: DataSnapshot, p1: String?) {
             }

             override fun onChildRemoved(p0: DataSnapshot) {

             }
      })
        Log.d("at EventAdpter", "size =  ${eventList.size}")


    }

   override fun onEventClick(event: Event){
      //  val extras = Bundle()
      //  val intent = Intent(this,MapUserSationsAllActivity::class.java)
      // intent.putExtra("Kevent",event )

           //(extras.putParcelableArrayList("stations",StationList))},event)
      // extras.putParcelableArrayList("stations",StationList)
      //  extras.putString("sUid", event.eUid)
      //  extras.putString("Event",event.eName)

      //  extras.putSerializable("events", convEvent(eventList) as Serializable)
     //  extras.putParcelableArrayList("events",eventList)
     //  Log.d(" I UserE SList size","${StationList.size}")
     //  async{
       Log.d("Station List","Station List 1 ${StationList.size} On Cliked, sending to PackingActivity")
           fetchPlacemarks2({packingActivity(event)},event)
     //  uiThread {
     //      Log.d("Station List","Station List 1 $StationList.size} On Cliked")
     //      extras.putParcelableArrayList("stations",StationList)
      //     intent.putExtras(extras)
      //     startActivity(intent)
      // }}
    }

    fun packingActivity (event: Event){
        val extras = Bundle()
        val intent = Intent(this,MapUserSationsAllActivity::class.java)
        intent.putExtra("Kevent",event )
        Log.d("Station List","Station List 1 ${StationList.size} From ONCliked")
        Log.d("Station List","Station List 1.uid ${StationList.get(0).sUid} From ONCliked uid")
        extras.putParcelableArrayList("stations",StationList)
        intent.putExtras(extras)
        startActivity(intent)
    }





    fun fetchPlacemarks(stationsReady: () -> Unit, eventIn: Event) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dataSnapshot.children.mapNotNullTo(StationList) {
                    it.getValue<Station>(Station::class.java) as Station

                }
                StationList.forEach {
                    Log.d("Station List obj mame","${it.sName}")
                }
                Log.d("Station List"," just filling up list ${StationList.size}")
                stationsReady()
            }
        }
        StationList.clear()

        val ref = FirebaseDatabase.getInstance().reference.child("events").child(eventIn.eUid).child("stations")
        ref.addListenerForSingleValueEvent(valueEventListener)

    }


    private fun fetchPlacemarks2(stationsReady: () -> Unit, eventIn: Event) {
        val stationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                StationList.clear()
                //dataSnapshot.children.mapNotNullTo(StationList) {
                //    it.getValue<Station>(Station::class.java)
                //}
                dataSnapshot.children.forEach {
                    var Sat: Station? = it.getValue<Station>(Station::class.java)
                    if(Sat != null) {
                        StationList.add(Sat)
                    }
                    Log.d("Station List item ", " station des ${Sat?.sUid}")
                }
                stationsReady()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        FirebaseDatabase.getInstance().reference.child("events").child(eventIn.eUid).child("stations").addListenerForSingleValueEvent(stationListener)
    }













    private fun convEvent(eventL :ArrayList<Event>): ArrayList<Event_S>{
        event_sList = ArrayList<Event_S>()
        eventL.forEach {
            var Ev_s:Event_S = Event_S(it.eUid,it.eName,it.eDescription)
            event_sList.add(Ev_s)
        }
        return  event_sList

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.me.o_chat.R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            com.me.o_chat.R.id.menu_new_message -> {
                val intent = Intent(this,NewMessageActivity::class.java)
                Log.d("FA menu", "in Menu")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_new_event -> {
                val intent = Intent(this,EventCreateActivity::class.java)
                Log.d("Event menu", "in Menu")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_new_station -> {
                val intent = Intent(this,NewStationActivity::class.java)
                Log.d("FA menu", "in Menu")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_create_station -> {
                val intent = Intent(this,StationCreateActivity::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_manage_members -> {
                val intent = Intent(this,HomeFragments::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }
            R.id.menu_leaderboard -> {
            val intent = Intent(this,GoalAchievedActivity::class.java)
            Log.d("FA menu", "in Menu create")
            startActivity(intent)
        }



        }
        return super.onOptionsItemSelected(item)
    }
}
