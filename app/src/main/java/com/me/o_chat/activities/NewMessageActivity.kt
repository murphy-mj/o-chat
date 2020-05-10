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





       // getEventList({ withMultiChoiceList() },currentUser)
        getEventList({ withMultiChoiceList({getCurrentUser({whichUsers()},userId)}) })

        recyclerviewNewMessage.adapter = UserAdapter(userList, this)
        recyclerviewNewMessage.scrollToPosition(recyclerviewNewMessage.adapter?.itemCount!!)
        Log.d("at userAdpter", "size =  ${userList.size}")


    }


    override fun onUserClick(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("Kuser", user)
        startActivity(intent)
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.me.o_chat.R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            com.me.o_chat.R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                Log.d("FA menu", "in Menu")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_new_event -> {
                val intent = Intent(this, EventCreateActivity::class.java)
                Log.d("Event menu", "in Menu")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_new_station -> {
                val intent = Intent(this, NewStationActivity::class.java)
                Log.d("FA menu", "in Menu")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_create_station -> {
                val intent = Intent(this, StationCreateActivity::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }
            com.me.o_chat.R.id.menu_manage_members -> {
                val intent = Intent(this, HomeFragments::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }
            R.id.menu_leaderboard -> {
                val intent = Intent(this, GoalAchievedActivity::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }


        }
        return super.onOptionsItemSelected(item)
    }


    private fun  getEventList(stationsReady: () -> Unit) {
        Log.d("in Get Event List", "getting a list of events associated with Admin")
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
                        if (it.key.toString() == "uEcode") {
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



    // we need to get current user, before we gather a list of users
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






  private fun whichUsers(){
    if (currentUser.uType == "Admin") {
       getUsers()
    } else {
        getUsersOther()
    }
    }


        private fun  getUsers() {
          //The current user is an Administrator, and we are seeking all participants that belong to the event selected
            Log.d("in getUsers", "in get users")
            val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("in GetUsers logged in user type is "," ${currentUser.uType}")
                Log.d("in getUsers", "looking for participants of selected events")
                userList.clear()

                p0.children.forEach {
                        var user = it.getValue(User::class.java)


                        for(item in selectedList){
                           // var s = it.getValue(String::class.java)
                            var s = it.value
                            Log.d("selected List","${s}")
                        }

                    // only interested in participants in the same evnt
                        selectedStrings.forEach {
                            Log.d("selected String element","$it")
                        }

                        if (user != null && user.uType != "Admin" && selectedStrings.contains(user.uOrgRef)) {
                            userList.add(user!!)
                        }
                }

                recyclerviewNewMessage.adapter?.notifyDataSetChanged()
            }
        }

        db.child("/users").addListenerForSingleValueEvent(stationListener)

    }


    private fun  getUsersOther() {
        // Once we have the current User informtion we can seek the Administrators of the Event
        // the Particpants can only talk to Administrators of the Event that they are signed up for
        // so we interate through the Users that are classed as Admin, and iterate through there evenst
        // and once we find an uEcode that matches the current user, they are added to list
        Log.d("logged in User Type is ", " ${currentUser.uType}")
        Log.d("in get Other Users", "looking for admin of Event")
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userList.clear()
                p0.children.forEach {
                    var user = it.getValue(User::class.java)

                    // admin user has an "events object", that contains event info of ecah event
                    var avts = it.child("events")
                    avts.children.forEach {
                        Log.d("with admin events key", it.key.toString())
                        Log.d("with admin evenst value", it.value.toString())
                        var avts2 = it
                        Log.d("inside the event events key avts2", avts2.toString())
                        avts2.children.forEach {
                            Log.d("inside the event events key", it.key.toString())
                            Log.d("inside the  event value", it.value.toString())
                            // we are looking for Admins that share are involved with same event
                            if (it.key.toString() == "uEcode" && currentUser.uOrgRef == it.value.toString()) {
                                if (user != null && user.uType == "Admin" && currentUser.uUid != user.uUid) {
                                    userList.add(user!!)
                                }
                            }

                        }
                    }
                    recyclerviewNewMessage.adapter?.notifyDataSetChanged()
                    // stationsReady()
                }
            }

        }
        db.child("/users").addListenerForSingleValueEvent(stationListener)

    }











}


