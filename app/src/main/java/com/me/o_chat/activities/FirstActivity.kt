package com.me.o_chat.activities

import android.content.Intent
import android.net.sip.SipAudioCall
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ReportFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.AdminAdapter
import com.me.o_chat.MessageAdminListener
import com.me.o_chat.R
import com.me.o_chat.fragments.AboutUsFragment
import com.me.o_chat.main.MainActivity
import com.me.o_chat.models.Message
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_chatlog.*

import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.content_first.*
import kotlinx.android.synthetic.main.home.*
import java.util.*
import kotlin.collections.ArrayList

class FirstActivity : AppCompatActivity(), MessageAdminListener {

    // This is the first screen after login for the Participant
    // It wil display any messages that the Participant has not viewed
    // and will the launching platform for the menu items


    lateinit var  messageList : ArrayList<Message>
    lateinit var auth: FirebaseAuth
    lateinit var uid:String
    lateinit var MessTo:String
    lateinit var MessFrom:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        supportActionBar?.title = "Welcome Participant"
        auth = FirebaseAuth.getInstance()
        val layoutManager = LinearLayoutManager(this)
        recyclerviewFirst.layoutManager = layoutManager as RecyclerView.LayoutManager
        uid = FirebaseAuth.getInstance().uid!!
        messageList = ArrayList<Message>()


       latestMessage()
       recyclerviewFirst.adapter =  AdminAdapter(messageList,this)

        // this should be activated if a TCM message is received in OcFirebaseMessagingService
        val intent = intent
        val message = intent.getStringExtra("message")
        Log.d("TCM 2","in FirstActivity Message : ${message}")
        if(!message.isNullOrEmpty()) {
            if(message == "Please Begin Event"){

            }
            AlertDialog.Builder(this)
                .setTitle("Event has Begun")
                .setMessage(message)
                .setPositiveButton("Ok", { dialog, which -> }).show()
        }




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.menu_new_message -> {
              val intent = Intent(this,NewMessageActivity::class.java)
               startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            R.id.menu_list_events -> {
                val intent = Intent(this,EventActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_leaderboard -> {
                val intent = Intent(this,LeaderboardActivity::class.java)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }




// on clicking the message it will be flagged as viewed and removed from view
// we are recreating the message data and updating a field uViewed
// then re-saving the message again, its one way :)

    override fun onMessageClick(message: Message){
        var tempMessage = Message()
        tempMessage = message
        tempMessage.uViewed = true
        var msgId = message.uUid
        var toM :String = message.uTo
        var fromM :String = message.uFrom

        val refM = FirebaseDatabase.getInstance().getReference("/user-messages/${toM}/${fromM}").child(msgId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(dataSnapshot: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    var msg: Message = p0!!.getValue(Message::class.java)!!
                    Log.d("Clicked Message","${message.uText}")
                    if (msg != null) {
                        FirebaseDatabase.getInstance().getReference("/user-messages/${toM}/${fromM}").child(msgId).setValue(tempMessage)
                            .addOnSuccessListener {getUserTo({getUserFrom(toM,fromM)},toM,fromM)  }
                         // this call to getUserTo and FRom results in a Toat message, which works fine for participant
                        recyclerviewFirst.adapter?.notifyDataSetChanged()
                    }
                }
            })


        val refM2 = FirebaseDatabase.getInstance().getReference("/user-messages/${fromM}/${toM}").child(msgId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(dataSnapshot: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    var msg: Message = p0!!.getValue(Message::class.java)!!
                    Log.d("Clicked Message","${message.uText}")
                    if (msg != null) {
                        FirebaseDatabase.getInstance().getReference("/user-messages/${fromM}/${toM}").child(msgId).setValue(tempMessage)
                            .addOnSuccessListener {getUserTo({getUserFrom(toM,fromM)},toM,fromM)  }
                        // this call to getUserTo and FRom results in a Toat message, which works fine for participant
                        recyclerviewFirst.adapter?.notifyDataSetChanged()
                    }
                }
            })

    }









    // we are going through all the Participants messages to see which one has not been viewed
    // and adding these to the MessageList Array, for use in the Message Adapter
    // the "toM" will always be the current logging in person
    // once we have the list these are sorted before send to the Adapter for display

    private fun latestMessage() {
      val toM = FirebaseAuth.getInstance().currentUser?.uid.toString()

      val refM = FirebaseDatabase.getInstance().getReference("/user-messages/${toM}")
          .addChildEventListener(object : ChildEventListener {
              override fun onCancelled(p0: DatabaseError) {
              }

              override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                  // each time messages db has a new entry, its add to the array list
                  messageList.clear()
                  p0.children.forEach {
                      var message = it.getValue(Message::class.java)
                      if(message != null && message.uViewed == false && message.uTo == toM) {
                          messageList.add(message!!)
                      }
                      Log.d("new message array",messageList.size.toString())

                  }
                  Collections.sort(messageList, compareByDescending({it.uTimeStamp}))
                  recyclerviewFirst.adapter?.notifyDataSetChanged()


              }

              override fun onChildMoved(p0: DataSnapshot, p1: String?) {
              }

              override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                  messageList.clear()
                  p0.children.forEach {
                      Log.d("on Child Changed", it.toString())
                      var message = it.getValue(Message::class.java)
                      if(message != null && message.uViewed == false && message.uTo == toM) {
                          messageList.add(message!!)
                      }
                      Log.d("new message array",messageList.size.toString())

                  }
                  Collections.sort(messageList, compareByDescending({it.uTimeStamp}))
                  recyclerviewFirst.adapter?.notifyDataSetChanged()
              }


              override fun onChildRemoved(p0: DataSnapshot) {

              }
          })


    }


    private fun  getUserTo(stationsReady: () -> Unit,toM :String,fromM:String) {
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                MessTo = ""
                var TUser = p0.getValue(User::class.java)
                var toName = TUser?.uName
                stationsReady( )
            }
        }


        FirebaseDatabase.getInstance().getReference().child("/users/${toM}").addListenerForSingleValueEvent(stationListener)
    }

    private fun  getUserFrom(toM :String,fromM:String) {
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                MessFrom = ""
                var FrmuUser = p0.getValue(User::class.java)
                var FromName = FrmuUser?.uName
                Toast.makeText(applicationContext, "Mesage from ${FromName}",Toast.LENGTH_SHORT).show()
            }
        }


        FirebaseDatabase.getInstance().getReference().child("/users/${fromM}").addListenerForSingleValueEvent(stationListener)
    }





    // to be deleted

//  //  Log.d("at messageAdpter", "size =  ${messageList.size}")

    //R.id.menu_new_station -> {
    //                val intent = Intent(this,NewStationActivity::class.java)
    //                Log.d("FA menu", "in Menu")
    //                startActivity(intent)
    //            }
    //            R.id.menu_create_station -> {
    //                val intent = Intent(this,StationCreateActivity::class.java)
    //                Log.d("FA menu", "in Menu create")
    //                startActivity(intent)
    //            }

//  R.id.menu_manage_members -> {
//                val intent = Intent(this,HomeFragments::class.java)
//                Log.d("FA menu", "in Menu create")
//                startActivity(intent)
//            }


    // R.id.menu_image -> {
    //                val intent = Intent(this,ImageCaptureActivity::class.java)
    //                Log.d("image menu", "in Menu create")
    //                intent.putExtra("Kparticpant",uid )
    //                startActivity(intent)
    //                        }

    // <item android:id="@+id/menu_image"
    //        android:title="Image capture">
    //    </item>


    // var message = p0.getValue(Message::class.java)
    //  Log.d("new message", message.toString())
    //  if (message != null) {
    //      messageList.add(message)
    //      recyclerviewFirst.adapter?.notifyDataSetChanged()
    //  }

    // // val PERMISSION_ID = 42
    //   // lateinit var mFusedLocationClient: FusedLocationProviderClient
}




