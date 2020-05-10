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




class AdminUserEventActivity : AppCompatActivity(), EventListener {


    lateinit var  eventList : ArrayList<Event>
    lateinit var event : Event
    lateinit var user :User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)


       // user object sent to this activity
      //  user = intent.getParcelableExtra("Kuser")

        supportActionBar?.title = "Events List"
        recyclerviewEvent.adapter

        eventList = ArrayList<Event>()

        // getUsers()
      //  Log.d("getEvents", "the number of m sent to this Station  ${messageList.size}")


        val layoutManager = LinearLayoutManager(this)
        recyclerviewEvent.layoutManager = layoutManager as RecyclerView.LayoutManager






        // as we are in this activity, we must be the admin
        // get of th events created and display them for selection
       // val admin = FirebaseAuth.getInstance().currentUser?.uid.toString()
       // val ref = FirebaseDatabase.getInstance().reference.child(admin).child("events")

        val ref = FirebaseDatabase.getInstance().reference.child("events")
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



        recyclerviewEvent.adapter = EventAdapter(eventList, this)
        recyclerviewEvent.scrollToPosition(recyclerviewEvent.adapter?.itemCount!!)



        //recyclerviewNewMessage.adapter = UserAdapter(userList, this)
        //    recyclerviewNewMessage.adapter = UserAdapter(userList, this)
    }







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

    // the event click allocates the User to the event
    override fun onEventClick(event: Event){
        FirebaseDatabase.getInstance().reference.child("users").child(user.uUid.toString())
            .child("events").child(event.eUid).setValue(event)
        }
       // val extras = Bundle()
       // val intent = Intent(this,StationCreateActivity::class.java)
       // intent.putExtra("Kevent",event )
      //  extras.putString("sUid", event.eUid)
      //  extras.putString("Event",event.eName)
      //  intent.putExtras(extras)
      //  startActivity(intent)



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this,NewMessageActivity::class.java)
                Log.d("FA menu", "in Menu")
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_new_event -> {
                val intent = Intent(this,EventCreateActivity::class.java)
                Log.d("Event menu", "in Menu")
                startActivity(intent)
            }
            R.id.menu_new_station -> {
                val intent = Intent(this,NewStationActivity::class.java)
                Log.d("FA menu", "in Menu")
                startActivity(intent)
            }
            R.id.menu_create_station -> {
                val intent = Intent(this,StationCreateActivity::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }
            R.id.menu_manage_members -> {
                val intent = Intent(this,AdminUserEventActivity::class.java)
                Log.d("manage members", "in Menu create")
                startActivity(intent)
            }


        }
        return super.onOptionsItemSelected(item)
    }

//to be deleted
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
}
