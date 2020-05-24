package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.*
import androidx.core.content.ContextCompat.startActivity

import androidx.recyclerview.widget.LinearLayoutManager
import com.me.o_chat.R
import com.me.o_chat.UserAdapter
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_new_message.*
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.UserListener
import com.me.o_chat.models.Event
import kotlinx.android.synthetic.main.content_event.*
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class NewMessageActivity : AppCompatActivity(),UserListener {

    lateinit var userList: ArrayList<User>
    lateinit var currentUser: User
//    lateinit var eventListCode: ArrayList<String>
    lateinit var builder :AlertDialog.Builder
    lateinit var selectedList : ArrayList<Int>
    lateinit var items : ArrayList<String>
  //  lateinit var items2 : Array<String>
    var userId:String = ""
    lateinit var db:DatabaseReference
    lateinit var selectedStrings : ArrayList<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"
        recyclerviewNewMessage.adapter
        userList = ArrayList<User>()
        selectedStrings = ArrayList<String>()
 //       eventListCode = ArrayList<String>()
        currentUser = User()

        items = ArrayList<String>()
        selectedList = ArrayList<Int>()
        builder = AlertDialog.Builder(this)
        db = FirebaseDatabase.getInstance().reference

        val layoutManager = LinearLayoutManager(this)
        recyclerviewNewMessage.layoutManager = layoutManager as RecyclerView.LayoutManager

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        getEventList({ withMultiChoiceList({getCurrentUser({whichUsers()},userId)}) })

        recyclerviewNewMessage.adapter = UserAdapter(userList, this)
        recyclerviewNewMessage.scrollToPosition(recyclerviewNewMessage.adapter?.itemCount!!)
        Log.d("at userAdpter", "size =  ${userList.size}")


    }

// we click the Participant or Organiser we need to send a message to
    override fun onUserClick(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("Kuser", user)
        startActivity(intent)
        finish()
    }


    // as the user can arrive from different Activities, we are using the limited menu, rather than back arrow
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.me.o_chat.R.menu.nav_limited, menu)
        return super.onCreateOptionsMenu(menu)
    }


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

//1. get the events that the Organiser has created or the participant has been accepted for
//  from these events we can get the public Event Codes
     private fun  getEventList(stationsReady: () -> Unit) {
        Log.d("in Get Event List", "getting a list of events associated with Admin/participant")
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("getevent it", it.toString())
                    var tst = it
                    tst.children.forEach {
                        Log.d("with getevntsList key", it.key.toString())
                        Log.d("with geteventList value", it.value.toString())
                        if (it.key.toString() == "uEcode" ||  it.key.toString() == "ecode") {
                            var i = items.size
                            // items.set(i,it.value.toString())
                            items.add(i,it.value.toString())
                        }
                    }
                }
                Log.d("with getEvent items size",items.size.toString())
                stationsReady()
            }
        }
        Log.d("Get Event list, c user","${userId}")
        db.child("/users/${userId}/events").addListenerForSingleValueEvent(stationListener)

    }




//2. Once we have the Eents codes, the current logged in person can select which Event they want
    fun withMultiChoiceList(stationsReady: () -> Unit) {
        Log.d("with multi choice list",items.size.toString())
        val listP = arrayOfNulls<String>(items.size)
        items.toArray(listP)
        builder.setTitle("Please select Events")
        builder.setMultiChoiceItems(listP, null
        ) { dialog, which, isChecked ->
            if (isChecked) {
                selectedList.add(which)
            } else if (selectedList.contains(which)) {
                selectedList.remove(Integer.valueOf(which))
            }
        }

        builder.setPositiveButton("DONE") { dialogInterface, i ->
            for (j in selectedList.indices) {
                selectedStrings.add(items[selectedList[j]])
            }
            Toast.makeText(applicationContext, "Items selected are: " + Arrays.toString(selectedStrings.toTypedArray()), Toast.LENGTH_SHORT).show()
            stationsReady()
        }
        // display multi choice dialog, if there is an event list otherwise just just the next function
        if(items.size > 0) {
            builder.show()
        } else {
            stationsReady()
        }

    }



//3. we need to get current user, before we gather a list of users
    private fun  getCurrentUser(stationsReady: () -> Unit,userId:String) {
        Log.d("in get Current User", "getting Current User Object")
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var user = p0.getValue(User::class.java)
                currentUser = user!!
                Log.d("Current User","${currentUser.uEmail}")
                stationsReady()
            }
        }


        db.child("/users/${userId}").addListenerForSingleValueEvent(stationListener)
    }





//4. based on the loggin in User type - determines
  private fun whichUsers(){
    if (currentUser.uType == "Admin") {
       getUsers()
    } else {
        if(currentUser.uEvtApproval == "approved") {
            getUsersOther()
        } else{
            Toast.makeText(this, "status of event access ${currentUser.uEvtApproval}", Toast.LENGTH_SHORT).show()
        }

    }
    }

//4.a If the current user is an Administrator, and we are seeking all participants that belong to the event selected
        private fun  getUsers() {
            val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userList.clear()

                p0.children.forEach {
                        var user = it.getValue(User::class.java)
                    // only interested in Participants in the same event, selectedStrings holds the public event codes
                        if (user != null && user.uType != "Admin" && selectedStrings.contains(user.uOrgRef)) {
                            userList.add(user!!)
                        }
                }

                recyclerviewNewMessage.adapter?.notifyDataSetChanged()
            }
        }

        db.child("/users").addListenerForSingleValueEvent(stationListener)

    }

//4.b Once we have the current User information we can seek the Administrators of the Event
    // the Participants can only talk to the Administrator/Organiser of the Event that they are approved up for
    // so we interate through the Users that are classed as Admin, and iterate through there events
    // and once we find an uEcode that matches the current user, they are added to list
    private fun  getUsersOther() {
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userList.clear()
                p0.children.forEach {
                    // we only need to Organisers that have set up events
                    // if the you have events associated with then, then they will have an "events" node
                    if (it.hasChild("events")) {
                      var user = it.getValue(User::class.java)
                      // admin user has an "events object", that contains event info of each event
                      var avts = it.child("events")
                      avts.children.forEach {
                        var avts2 = it
                        Log.d("inside the event events key avts2", avts2.toString())
                        avts2.children.forEach {
                            // we are looking for Admins that share are involved with same event
                            if (it.key.toString() == "uEcode" && currentUser.uOrgRef == it.value.toString()) {
                                if (user != null && user.uType == "Admin" && currentUser.uUid != user.uUid) {
                                    userList.add(user!!)
                                    recyclerviewNewMessage.adapter?.notifyDataSetChanged()
                                }
                            }

                        }
                      }
                    }
                }
            }

        }
        db.child("/users").addListenerForSingleValueEvent(stationListener)

    }




//  com.me.o_chat.R.id.menu_new_station -> {
//                val intent = Intent(this, NewStationActivity::class.java)
//                Log.d("FA menu", "in Menu")
//                startActivity(intent)
//            }
//            com.me.o_chat.R.id.menu_create_station -> {
//                val intent = Intent(this, StationCreateActivity::class.java)
//                Log.d("FA menu", "in Menu create")
//                startActivity(intent)



// for(item in selectedList){
//                            var s = it.value
//                            Log.d("selected List","${s}")
//                        }



    //selectedStrings.forEach {
    //                            Log.d("selected String element","$it")
    //
    //
    //                        // Log.d("inside the event events key", it.key.toString())
    //                            Log.d("inside the  event value", it.value.toString())}


    //Log.d("with admin events key", it.key.toString())
    // Log.d("with admin evenst value", it.value.toString())
}


