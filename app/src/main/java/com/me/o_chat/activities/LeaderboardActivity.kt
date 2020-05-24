package com.me.o_chat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.me.o_chat.R
import com.me.o_chat.models.User
import com.me.o_chat.models.Event
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.GoalAchievedAdapter
import com.me.o_chat.GoalListener
import com.me.o_chat.models.GoalAchObj
import com.me.o_chat.models.Result
import kotlinx.android.synthetic.main.activity_admin_user.*
import kotlinx.android.synthetic.main.content_goal.*
import java.util.*
import kotlin.collections.ArrayList


class LeaderboardActivity : AppCompatActivity(),GoalListener {


  // To see a leaderboard, we fist of all need to determine the event
  // The Organiser may have more than one event, se we meed to have the Organiser/Participant select the event code

    lateinit var  eventList : ArrayList<Event>
    lateinit var currentUser: User
    lateinit var builder : AlertDialog.Builder
    lateinit var selectedList : ArrayList<Int>
    lateinit var items : ArrayList<String>
    var userId:String = ""
    lateinit var leadEvent : String
    lateinit var evt : Event
    lateinit var db: DatabaseReference
    lateinit var selectedStrings : ArrayList<String>
    lateinit var  resultList : ArrayList<Result>
    lateinit var eventInId : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal2)
        supportActionBar?.title = "Leaderboard : "
        recyclerviewGoal.adapter
        eventList = ArrayList<Event>()
        selectedStrings = ArrayList<String>()
        currentUser = User()
        items = ArrayList<String>()
        selectedList = ArrayList<Int>()
        builder = AlertDialog.Builder(this)
        db = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        resultList = ArrayList<Result>()
        val layoutManager = LinearLayoutManager(this)
        recyclerviewGoal.layoutManager = layoutManager as RecyclerView.LayoutManager

        // the order of functions listed below 1-6
        getEventList({ withSingleChoiceList({getCurrentUser({getEvent({fetchResults({resultStuff(resultList)},eventInId)})},userId)}) })

    }



//1. getting a list of Events approved for current user

    private fun  getEventList(stationsReady: () -> Unit) {
        Log.d("in Get Event List", "getting a list of events associated with Admin")
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                evt =  Event()
                p0.children.forEach {
                    evt = it.getValue(Event::class.java)!!
                    if(evt != null) {
                        eventList.add(evt)
                        Log.d("Event Added to eventList ", evt.toString())
                    }
                    var tst = it
                    tst.children.forEach {
                        Log.d("user events key", it.key.toString())
                        Log.d("user events value", it.value.toString())
                        if (it.key.toString() == "ecode") {
                            var i = items.size
                            items.add(i,it.value.toString())
                        }
                    }
                }
                Log.d("Finished getEvent, items size",items.size.toString())
                stationsReady()
            }
        }
        Log.d("In Get Events list, c user is ","${userId}")
        db.child("/users/${userId}/events").addListenerForSingleValueEvent(stationListener)

    }



//2. have to select event to see the leaderboard

    fun withSingleChoiceList(stationsReady: () -> Unit) {

        val listP = arrayOfNulls<String>(items.size)
        items.toArray(listP)
        builder.setTitle("Please select one Event Code")
        builder.setSingleChoiceItems(listP, -1) {
            dialog, i ->
            leadEvent = listP[i]!!

            dialog.dismiss()
            Log.d("LeaderBoard", "Event Code selected ${leadEvent}")
            Toast.makeText(applicationContext, "Event selected =: ${leadEvent} " , Toast.LENGTH_SHORT).show()
            stationsReady()
        }
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            leadEvent = ""
            dialogInterface.cancel()
        }

        builder.show()


    }





//3. we need to get current user
    private fun  getCurrentUser(stationsReady: () -> Unit, userId:String) {
        Log.d("in getCurrent User", "getting Current User Object")
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




//4. Get the event Id, based on the eventCode that Organiser/Participant selected
    private fun getEvent(stationsReady: () -> Unit):Unit{
    Log.d("Event Code used to get event", "${leadEvent}")
    Log.d("ListOf Events size", "${eventList.size}")
        var eventIn = eventList.find{p -> p.eCode == leadEvent }
    Log.d("EventIn id", "${eventIn!!.eUid}")
        if(eventIn != null) {
            eventInId = eventIn.eUid
            Log.d("Event Object ID Associated with Code", "${eventInId}")
            stationsReady()
        }

    }


//5. Once we have the EventId, we can gather any results associated with that Event
    fun fetchResults(stationsReady: () -> Unit, eventInId: String) {

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d("Results ", it.toString())
                    var resultt : Result = it.getValue(Result::class.java)!!
                    Log.d("Results round userid ", "${resultt!!.rUserId}")
                    if (resultt != null) {
                        resultList.add(resultt!!)
                        recyclerviewGoal.adapter?.notifyDataSetChanged()
                    }

                }
                Log.d("result List"," just filling up list ${resultList.size}")
                //sorting by the number of of stations completed
                Collections.sort(resultList, compareByDescending({it.rNumberAch}))
                stationsReady()
            }
        }

        resultList.clear()
    Log.d("result List"," Do we have an eventId ${eventInId}")
        val ref = FirebaseDatabase.getInstance().reference.child("Results").child("${eventInId}")
        ref.addListenerForSingleValueEvent(valueEventListener)

    }



    //6. Once we have the results we can present them
    fun resultStuff(resultList: ArrayList<Result>){
        Log.d("Result Stuff", "size =  ${resultList.size}")
        recyclerviewGoal.adapter = GoalAchievedAdapter(resultList, this)
        recyclerviewGoal.scrollToPosition(recyclerviewGoal.adapter?.itemCount!!)

    }





    override fun onGoalClick(goal: Result) {
        Log.d("Station List","Station List 1 ${goal} On Cliked")
    }




    // getting the Event id selected by Organiser or Participant
// we have eventList and the eventcode







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

}