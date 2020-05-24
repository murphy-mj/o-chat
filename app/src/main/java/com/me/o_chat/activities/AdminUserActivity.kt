package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.me.o_chat.R
import com.me.o_chat.UserAdapter
import com.me.o_chat.models.User
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.UserListener
import kotlinx.android.synthetic.main.activity_admin_user.*
import java.util.*
import kotlin.collections.ArrayList


class AdminUserActivity : AppCompatActivity(),UserListener {

    // What we are trying to do here is to allow the Organiser/Administrator to accept or reject
    // Participants who want to take part in the event.
    // An Organiser may be organising more than one event,
    // so Organiser selects the events from list of events and then reviews the participants
    //  either approve or deny

    lateinit var  userList : ArrayList<User>
    lateinit var currentUser: User
    lateinit var builder : AlertDialog.Builder
    lateinit var selectedList : ArrayList<Int>
    lateinit var items : ArrayList<String>
    var userId:String = ""
    lateinit var db: DatabaseReference
    lateinit var selectedStrings : ArrayList<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user)
        supportActionBar?.title = "Select Participant for Approval"
        recyclerviewUser.adapter
        userList = ArrayList<User>()
        selectedStrings = ArrayList<String>()
        currentUser = User()
        items = ArrayList<String>()
        selectedList = ArrayList<Int>()
        builder = AlertDialog.Builder(this)
        db = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()


        val layoutManager = LinearLayoutManager(this)
        recyclerviewUser.layoutManager = layoutManager as RecyclerView.LayoutManager

        recyclerviewUser.adapter = UserAdapter(userList, this)

        getEventList({ withMultiChoiceList({getCurrentUser({whichUsers()},userId)}) })
        recyclerviewUser.adapter = UserAdapter(userList, this)
    }





    override fun onUserClick(user: User){
        val intent = Intent(this,ApprovalActivity::class.java)
        intent.putExtra("Kuser",user )
        startActivity(intent)
       // finish()
    }




//.1 We need a list of 'Event Codes' associated with the logged in Organiser/Administrator
// these are the public code that the participants would register for

    private fun  getEventList(stationsReady: () -> Unit) {
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    var tst = it
                    tst.children.forEach {
                        Log.d("Event attribute key", it.key.toString())
                        Log.d("Event attribute value", it.value.toString())
                        if (it.key.toString() == "uEcode") {
                            var i = items.size
                            // items.set(i,it.value.toString())
                            items.add(i,it.value.toString())
                        }
                    }
                }
                stationsReady()
            }
        }
        db.child("/users/${userId}/events").addListenerForSingleValueEvent(stationListener)

    }



//.2 Once we have the Event Codes, the Organiser can select the Events that it wishes to deal with
// the Event codes stored in 'SelectedList' String Array
    fun withMultiChoiceList(stationsReady: () -> Unit) {

        val listP = arrayOfNulls<String>(items.size)
        items.toArray(listP)
        builder.setTitle("Please select Event Codes")
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
            Toast.makeText(applicationContext, "Events selected =: " + Arrays.toString(selectedStrings.toTypedArray()), Toast.LENGTH_SHORT).show()
            stationsReady()
        }
        // display multi choice dialog only if there is an event list otherwise just just the next function
        if(items.size > 0) {
            builder.show()
        } else {
            stationsReady()
        }

    }



//.3 We have the Events that Organiser wants to deal with.
// we need to get current user, to ensure that the type is correct, Admin or Participant

    private fun  getCurrentUser(stationsReady: () -> Unit,userId:String) {
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var user = p0.getValue(User::class.java)
                currentUser = user!!
                stationsReady()
            }
        }


        db.child("/users/${userId}").addListenerForSingleValueEvent(stationListener)
    }





//.4 Based on Logged in user determine way forward
    private fun whichUsers(){
        if (currentUser.uType == "Admin") {
            getUsers()
        } else {
            getUsersOther()
        }
    }

//4a. The current user is an Administrator, and we are seeking all participants that belong to the event selected
    private fun  getUsers() {
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userList.clear()
                p0.children.forEach {
                    var user = it.getValue(User::class.java)
                    // only interested in participants in the same evnt
                    ///only looking for approval that is pending, and users with an EventCode that is contained is the list of codes generate earlier
                    if (user != null && user.uType != "Admin" && user.uEvtApproval == "pending" && selectedStrings.contains(user.uOrgRef)) {
                        userList.add(user!!)
                    }
                }

                recyclerviewUser.adapter?.notifyDataSetChanged()
            }

        }

        db.child("/users").addListenerForSingleValueEvent(stationListener)

    }


//4ab The current user is a Participant, and we are Organisers of the event selected
    // Once we have the current User informtion we can seek the Administrators of the Event
    // the Particpants can only talk to Administrators of the Event that they are signed up for
    // so we interate through the Users that are classed as Admin, and iterate through there evenst
    // and once we find an uEcode that matches the current user, they are added to list
    // this should not be called

    private fun  getUsersOther() {
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userList.clear()
                p0.children.forEach {
                    var user = it.getValue(User::class.java)
                    // admin user has an "events object", that contains event info of each event
                    var avts = it.child("events")
                    avts.children.forEach {
                        var avts2 = it
                        avts2.children.forEach {
                            // we are looking for Admins that share are involved with same event
                            if (it.key.toString() == "uEcode" && currentUser.uOrgRef == it.value.toString()) {
                                if (user != null && user.uType == "Admin" && currentUser.uUid != user.uUid) {
                                    userList.add(user!!)
                                }
                            }

                        }
                    }
                    recyclerviewUser.adapter?.notifyDataSetChanged()

                }
            }

        }
        db.child("/users").addListenerForSingleValueEvent(stationListener)

    }

     // get Event associated with UOrgRef

//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
// /           override fun onCancelled(p0: DatabaseError) {
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                p0.children.forEach {
//                    Log.d("user", it.toString())
//                    var user = it.getValue(User::class.java)
//                    if(user != null) {
//                        userList.add(user!!)
//                        recyclerviewUser.adapter?.notifyDataSetChanged()
//                    }
//                }

//            }

    //recyclerviewNewMessage.adapter = UserAdapter(userList, this)
    //    recyclerviewNewMessage.adapter = UserAdapter(userList, this)


    // stationsReady()

    //  for(item in selectedList){
    //                    // var s = it.getValue(String::class.java)
    //                  //  var s = item.value
    //                    Log.d("selected List","${item}")
    //                }
// selectedStrings.forEach {
//                        Log.d("selected String element","$it")
//                    }

}