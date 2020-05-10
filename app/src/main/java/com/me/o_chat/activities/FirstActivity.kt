package com.me.o_chat.activities

import android.content.Intent
import android.net.sip.SipAudioCall
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

    lateinit var  messageList : ArrayList<Message>
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        supportActionBar?.title = "Welcome Participant"
        auth = FirebaseAuth.getInstance()
        val layoutManager = LinearLayoutManager(this)
        recyclerviewFirst.layoutManager = layoutManager as RecyclerView.LayoutManager
        var uid = FirebaseAuth.getInstance().uid
        Log.d("FA user uid", uid.toString())
        messageList = ArrayList<Message>()


       latestMessage()


        recyclerviewFirst.adapter =  AdminAdapter(messageList,this)


    }

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

            R.id.menu_list_events -> {
                val intent = Intent(this,EventActivity::class.java)
              //  intent.putExtra("Kuser",auth.currentUser )
              //  Log.d("Event List menu", "in Menu")
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





    override fun onMessageClick(message: Message){
        var tempMessage = Message()
        tempMessage = message
        tempMessage.uViewed = true
        var msgId = message.uUid
        var toM :String = message.uTo
        var fromM :String = message.uFrom
        Log.d("User Message clicked", message.uText.toString())
        Log.d("User Message clicke To",toM)
        Log.d("User Message clicked From", fromM)
        Log.d("User Message clicked tempM", tempMessage.toString())
        Log.d("User Msg Dit", FirebaseDatabase.getInstance().getReference("/user-messages/${toM}/${fromM}").child(msgId).toString())


        val refM = FirebaseDatabase.getInstance().getReference("/user-messages/${toM}/${fromM}").child(msgId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(dataSnapshot: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    Log.d("Clicked Message p0",p0!!.toString())
                    var msg: Message = p0!!.getValue(Message::class.java)!!
                    Log.d("Clicked Message","${message.uText}")
                    if (msg != null) {
                        FirebaseDatabase.getInstance().getReference("/user-messages/${toM}/${fromM}").child(msgId).setValue(tempMessage)
                        recyclerviewFirst.adapter?.notifyDataSetChanged()
                    }

                }
            })


    }

  private fun latestMessage() {
      val toM = FirebaseAuth.getInstance().currentUser?.uid.toString()
      // val  fromM = contact.uUid.toString()
      val refM = FirebaseDatabase.getInstance().getReference("/user-messages/${toM}")
          .addChildEventListener(object : ChildEventListener {
              override fun onCancelled(p0: DatabaseError) {
              }

              override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                  // each time messages db has a new entry, its add to the array list
                  messageList.clear()
                  Log.d("new message user p1", p1.toString())
                  Log.d("new message datasnap User", p0.toString())
                  Log.d("new message key User", p0.key.toString())
                  Log.d("new message child User", p0.child(p0.key.toString()).toString())
                  p0.children.forEach {
                      Log.d("on child added", it.toString())
                      var message = it.getValue(Message::class.java)
                      if(message != null && message.uViewed == false && message.uTo == toM) {
                          messageList.add(message!!)
                      }
                      Log.d("new message array",messageList.size.toString())

                  }
                  Collections.sort(messageList, compareByDescending({it.uTimeStamp}))

                  recyclerviewFirst.adapter?.notifyDataSetChanged()

                 // var message = p0.getValue(Message::class.java)
                //  Log.d("new message", message.toString())
                //  if (message != null) {
                //      messageList.add(message)
                //      recyclerviewFirst.adapter?.notifyDataSetChanged()
                //  }
              }

              override fun onChildMoved(p0: DataSnapshot, p1: String?) {
              }

              override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                  messageList.clear()
                  Log.d("new message user p1 changed", p1.toString())
                  Log.d("new message datasnap User changed", p0.toString())
                  Log.d("new message key User changed", p0.key.toString())
                  Log.d("new message child User changed ", p0.child(p0.key.toString()).toString())
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
      //  Log.d("at messageAdpter", "size =  ${messageList.size}")

  }


    // to be deleted
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

}




