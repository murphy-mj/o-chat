package com.me.o_chat.activities

import android.content.Intent
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
import com.me.o_chat.models.Result
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_chatlog.*

import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.content_first.*
import kotlinx.android.synthetic.main.content_goal.*
import kotlinx.android.synthetic.main.home.*
import java.util.*
import kotlin.collections.ArrayList

class FirstAdminActivity : AppCompatActivity(), MessageAdminListener {

    lateinit var  adminMessageList : ArrayList<Message>
    lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_admin)
        supportActionBar?.title = "Welcome Administrator"
        auth = FirebaseAuth.getInstance()

        val layoutManager = LinearLayoutManager(this)
        recyclerviewFirst.layoutManager = layoutManager as RecyclerView.LayoutManager

        var uid = FirebaseAuth.getInstance().uid
        Log.d("FA user uid", uid.toString())
        adminMessageList = ArrayList<Message>()

        latestMessage()
        recyclerviewFirst.adapter =  AdminAdapter(adminMessageList,this)



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_admin,menu)
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
            R.id.menu_list_events -> {
                val intent = Intent(this,EventActivity::class.java)
              //  intent.putExtra("Kuser",auth.currentUser )
             //   Log.d("Event List menu", "in Menu")
                startActivity(intent)
            }

            R.id.menu_manage_members -> {
                //val intent = Intent(this,HomeFragments::class.java)
                val intent = Intent(this,AdminUserActivity::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }

            R.id.menu_start_event -> {
                //val intent = Intent(this,HomeFragments::class.java)
                val intent = Intent(this,EventStartActivity::class.java)
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
        Log.d("Admin Message clicked", message.uText.toString())
        Log.d("Admin Message clicke To",toM)
        Log.d("Admin Message clicked From", fromM)
        Log.d("Admin Message clicked tempM", tempMessage.toString())
Log.d("Admin Msg Dit", FirebaseDatabase.getInstance().getReference("/user-messages/${toM}/${fromM}").child(msgId).toString())


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
        // looking at alll message that were sent to logged in User
        // toM will have children, which will be users that that have sent message to loggin user
        val refM = FirebaseDatabase.getInstance().getReference("/user-messages/${toM}").addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    // each time messages db has a new entry, its add to the array list
                    adminMessageList.clear()
                    Log.d("new message Admin p1", p1.toString())
                    Log.d("new message datasnap Admin", p0.toString())
                    Log.d("new message key Admin", p0.key.toString())
                    Log.d("new message child Admin", p0.child(p0.key.toString()).toString())

                    p0.children.forEach {
                        Log.d("Message From", it.toString())
                        var message = it.getValue(Message::class.java)
                        if(message != null && message.uViewed == false && message.uTo == toM) {
                            adminMessageList.add(message!!)
                        }
                        Log.d("new message array",adminMessageList.size.toString())

                    }

                    Collections.sort(adminMessageList, compareByDescending({it.uTimeStamp}))
                    recyclerviewFirst.adapter?.notifyDataSetChanged()
                //    var message = p0.getValue(Message::class.java)
                //    Log.d("new message", message.toString())
                //    if (message != null) {
                //        adminMessageList.add(message)
                //        recyclerviewFirst.adapter?.notifyDataSetChanged()
                //    }
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    adminMessageList.clear()
                    Log.d("new message Admin child Changed p1", p1.toString())
                    Log.d("new message datasnap Admin Changed ", p0.toString())
                    Log.d("new message key Admin Changed ", p0.key.toString())
                    Log.d("new message child Admin Changed ", p0.child(p0.key.toString()).toString())
                    p0.children.forEach {
                        Log.d("Message From", it.toString())
                        var message = it.getValue(Message::class.java)
                        if(message != null && message.uViewed == false && message.uTo == toM) {
                            adminMessageList.add(message!!)
                        }
                        Log.d("new message array",adminMessageList.size.toString())

                    }

                    Collections.sort(adminMessageList, compareByDescending({it.uTimeStamp}))
                    recyclerviewFirst.adapter?.notifyDataSetChanged()
                }

                override fun onChildRemoved(p0: DataSnapshot) {

                }
            })
        //  Log.d("at messageAdpter", "size =  ${messageList.size}")

    }

    // to be deleted

    //     if(uid == null){
    //         // in not logged in  then go to
    //         val intent =Intent(this, MainActivity::class.java)
    // clear off activities on Activity stack
    //         intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
    //         startActivity(intent)
    //     }

    //  setSupportActionBar(toolbar)

    //    fab.setOnClickListener { view ->
    //        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
    //            .setAction("Action", null).show()
    //    }

  //  R.id.menu_new_station -> {
  //      val intent = Intent(this,NewStationActivity::class.java)
  //      Log.d("FA menu", "in Menu")
  //      startActivity(intent)
  //  }
  //  R.id.menu_create_station -> {
  //      val intent = Intent(this,StationCreateActivity::class.java)
  //      Log.d("FA menu", "in Menu create")
  //      startActivity(intent)
  //  }

    //  var M1 :Message = Message("Test one Admin","","","","",0L)
    //   adminList.add(M1)



    // val PERMISSION_ID = 42
    //    lateinit var mFusedLocationClient: FusedLocationProviderClient
}
