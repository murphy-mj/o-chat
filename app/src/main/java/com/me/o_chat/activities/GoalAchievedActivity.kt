package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.me.o_chat.*
import com.me.o_chat.R
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.activity_event.*
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.activity_station.*
import kotlinx.android.synthetic.main.activity_station_create_p1.*
import kotlinx.android.synthetic.main.content_event.*
import com.me.o_chat.models.*
import kotlinx.android.synthetic.main.content_goal.*
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


// this lists the message activity on a selected Station
// and allows you to send a message to the Station

class GoalAchievedActivity : AppCompatActivity(), GoalListener {


    lateinit var  goalList : ArrayList<GoalAchObj>
    private lateinit var StationList: ArrayList<Station>
    lateinit var  eventList : ArrayList<Event>
    lateinit var  userList : ArrayList<User>
    lateinit var  resultList : ArrayList<Result>
    lateinit var event : Event
    lateinit var eventIn : Event
    lateinit var station : Station
    lateinit var goal : GoalAchObj
    lateinit var user : User
    lateinit var result : Result
    lateinit var currentUser: User
    lateinit var db:DatabaseReference
    lateinit var userId :String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.me.o_chat.R.layout.activity_goal)

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        StationList = ArrayList<Station>()
        eventList = ArrayList<Event>()
        goalList = ArrayList<GoalAchObj>()
        userList = ArrayList<User>()
        resultList = ArrayList<Result>()
        station = Station()
        goal = GoalAchObj()
        event = Event()
        eventIn = Event()
        user = User()
        result = Result()
        db = FirebaseDatabase.getInstance().reference

        getCurrentUser({getEvents()},userId)

        var eventInId :String = "-M3Nkz3a-u-6Pg51hg2e"



        if (intent.hasExtra("Kevent")) {

            // station object sent to this acticity
            eventIn = intent.getParcelableExtra("Kevent")
        }



        supportActionBar?.title = "Leaderboard"
        recyclerviewGoal.adapter


        val layoutManager = LinearLayoutManager(this)
        recyclerviewGoal.layoutManager = layoutManager as RecyclerView.LayoutManager


        Log.d("Goal", "In Goal Achieved Act")
//these are all the events assocated with the current user

        fetchGoals({adapterSuff(goalList)},eventInId)
        fetchUsers({userStuff(userList)},eventInId)
        fetchResults({resultStuff(resultList)},eventInId)


        val refEvent = FirebaseDatabase.getInstance().reference.child("events")
        refEvent.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d("event", it.toString())
                    var event = it.getValue(Event::class.java)
                    if(event != null) {
                        eventList.add(event!!)
                       // recyclerviewGoal.adapter?.notifyDataSetChanged()
                    }
                }
                refEvent.removeEventListener(this)

            }

        })




    }


/////////// End of




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

   override fun onGoalClick(goal: Result){

       Log.d("Goal Achieved"," OnGoalClick no action required ")

    }

    fun packingActivity (goal: GoalAchObj){
        val extras = Bundle()
        val intent = Intent(this,MapUserSationsAllActivity::class.java)
        intent.putExtra("Kevent",event )
        Log.d("Station List","Station List 1 ${StationList.size} From ONCliked")
        Log.d("Station List","Station List 1.uid ${StationList.get(0).sUid} From ONCliked uid")
        extras.putParcelableArrayList("stations",StationList)
        intent.putExtras(extras)
        startActivity(intent)
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

            com.me.o_chat.R.id.menu_manage_members -> {
                val intent = Intent(this,HomeFragments::class.java)
                Log.d("FA menu", "in Menu create")
                startActivity(intent)
            }




        }
        return super.onOptionsItemSelected(item)
    }





    fun fetchGoals(stationsReady: () -> Unit, eventInId: String) {

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                goalList.clear()
                Log.d("Goal without without  ", p0.toString())
                p0.children.forEach {
                    Log.d("Goal without ", it.toString())
                    it.children.forEach {
                        Log.d("Goal within ", it.toString())
                        var goal = it.getValue(GoalAchObj::class.java)
                        if (goal != null) {
                            goalList.add(goal!!)
                         //   recyclerviewGoal.adapter?.notifyDataSetChanged()
                        }
                    }
                }

                goalList.forEach {
                    Log.d("Goal List obj Tname","${it.gTeam}")
                }
                Log.d("Goal List"," just filling up list ${goalList.size}")
                stationsReady()
            }
        }

        goalList.clear()
        //https://console.firebase.google.com/project/o-chat-25812/database/o-chat-25812/data/GoalAchieved/-M3Nkz3a-u-6Pg51hg2e/Stations/-M3Sw3AgHMs6f5K_7WmP
        val ref = FirebaseDatabase.getInstance().reference.child("GoalAchieved").child(eventInId).child("Stations")
       Log.d("db rEf",ref.toString())
        ref.addListenerForSingleValueEvent(valueEventListener)

    }

    fun adapterSuff (goalList: ArrayList<GoalAchObj>){
        Log.d("Goal Achieved Activity", "size =  ${goalList.size}")
      //  recyclerviewGoal.adapter = GoalAchievedAdapter(goalList, this)
      //  recyclerviewGoal.scrollToPosition(recyclerviewGoal.adapter?.itemCount!!)

    }


    fun fetchUsers(stationsReady: () -> Unit, eventInId: String) {

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                        Log.d("Users ", it.toString())
                        var user = it.getValue(User::class.java)
                        if (user != null) {
                            userList.add(user!!)
                           // recyclerviewGoal.adapter?.notifyDataSetChanged()
                        }

                    }
                Log.d("User List"," just filling up list ${userList.size}")
                stationsReady()
            }
        }

        userList.clear()

        val ref = FirebaseDatabase.getInstance().reference.child("users")
        ref.addListenerForSingleValueEvent(valueEventListener)

    }

    fun userStuff(userList: ArrayList<User>){
        Log.d("User Stuff", "size =  ${userList.size}")


    }



    fun fetchResults(stationsReady: () -> Unit, eventInId: String) {

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d("Results ", it.toString())
                    var resultt :Result = it.getValue(Result::class.java)!!
                    Log.d("Results round userid ", "${resultt!!.rUserId}")
                    if (resultt != null) {
                        resultList.add(resultt!!)
                        recyclerviewGoal.adapter?.notifyDataSetChanged()
                    }

                }
                Log.d("result List"," just filling up list ${resultList.size}")
                Collections.sort(resultList, compareByDescending({it.rNumberAch}))
              //  Collections.sort(resultList,kotlin.Comparator { t, t2 -> compareValues(
              //      return@Comparator t.rNumberAch.compareTo(t2.rNumberAch)
              //  ) })
                stationsReady()
            }
        }

        resultList.clear()

        val ref = FirebaseDatabase.getInstance().reference.child("Results").child("${eventInId}")
        ref.addListenerForSingleValueEvent(valueEventListener)

    }

    fun resultStuff(resultList: ArrayList<Result>){
        Log.d("Result Stuff", "size =  ${resultList.size}")
        recyclerviewGoal.adapter = GoalAchievedAdapter(resultList, this)
        recyclerviewGoal.scrollToPosition(recyclerviewGoal.adapter?.itemCount!!)

    }




    private fun getCurrentUser(stationsReady: () -> Unit,userId: String) {
        Log.d("in get Current User", "getting Current User Object")
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var user = p0.getValue(User::class.java)
                Log.d("Current User all", "${user!!.uEmail}")
                if(user != null) {
                    currentUser = user
                    Log.d("Current User", "${currentUser.uEmail}")
                 //  getUserApprovedEvents()
                    stationsReady()
                }
            }
        }
        db.child("/users/${userId}").addListenerForSingleValueEvent(stationListener)
    }


    private fun getEvents() {
        eventList.clear()
        val ref = db.child("events")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("events get to match ", currentUser.uOrgRef.toString())
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
                          //  Toast.makeText(this@EventActivity, "status of event access ${currentUser.uEvtApproval}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (event != null && eOrganiser == userId) {
                            eventList.add(event!!)
                        }
                    }
                }
                Log.d("event list size", eventList.size.toString())
                recyclerviewEvent.adapter?.notifyDataSetChanged()
                ref.removeEventListener(this)

            }

        })
    }


// com.me.o_chat.R.id.menu_new_station -> {
//                val intent = Intent(this,NewStationActivity::class.java)
//                Log.d("FA menu", "in Menu")
//                startActivity(intent)
//            }
//            com.me.o_chat.R.id.menu_create_station -> {
//                val intent = Intent(this,StationCreateActivity::class.java)
//                Log.d("FA menu", "in Menu create")
//                startActivity(intent)
//            }

    // for each user




    // val ref = FirebaseDatabase.getInstance().reference.child("users").child(adminId).child("events")
    //  val ref = FirebaseDatabase.getInstance().reference.child("GoalAchieved")


    //  ref.addListenerForSingleValueEvent(object : ValueEventListener {
    //     override fun onCancelled(p0: DatabaseError) {
    //     }

    //           override fun onDataChange(p0: DataSnapshot) {
    //            p0.children.forEach {
    //                 Log.d("Station", it.toString())
//
    //                  it.children.forEach {
    //                    Log.d("Goal", it.toString())
    //                  var goal = it.getValue(GoalAchObj::class.java)
    //                if (goal != null) {
    //                  goalList.add(goal!!)
    //                recyclerviewGoal.adapter?.notifyDataSetChanged()
    //          }

    //    }


    //  }
    //  ref.removeEventListener(this)

    // }

    // })


//    private fun convEvent(eventL :ArrayList<Event>): ArrayList<Event_S>{
    //     event_sList = ArrayList<Event_S>()
    //    eventL.forEach {
    //         var Ev_s:Event_S = Event_S(it.eUid,it.eName,it.eDescription)
    //         event_sList.add(Ev_s)
    //     }
    //    return  event_sList

    //  }


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



    /// removed adapter stuff



    //recyclerviewNewMessage.adapter = UserAdapter(userList, this)
    //    recyclerviewNewMessage.adapter = UserAdapter(userList, this)





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


    //          fetchPlacemarks2({packingActivity(goal)},event)
    //  uiThread {
    //      Log.d("Station List","Station List 1 $StationList.size} On Cliked")
    //      extras.putParcelableArrayList("stations",StationList)
    //     intent.putExtras(extras)
    //     startActivity(intent)
    // }}

    // private fun fetchPlacemarks2(stationsReady: () -> Unit, eventIn: Event) {
    //        val stationListener = object : ValueEventListener {
    //            override fun onDataChange(dataSnapshot: DataSnapshot) {
    //                StationList.clear()
    //                //dataSnapshot.children.mapNotNullTo(StationList) {
    //                //    it.getValue<Station>(Station::class.java)
    //                //}
    //                dataSnapshot.children.forEach {
    //                    var Sat: Station? = it.getValue<Station>(Station::class.java)
    //                    if(Sat != null) {
    //                        StationList.add(Sat)
    //                    }
    //                    Log.d("Station List item ", " station des ${Sat?.sUid}")
    //                }
    //                stationsReady()
    //            }
    //
    //            override fun onCancelled(databaseError: DatabaseError) {
    //                println("loadPost:onCancelled ${databaseError.toException()}")
    //            }
    //        }
    //        FirebaseDatabase.getInstance().reference.child("events").child(eventIn.eUid).child("stations").addListenerForSingleValueEvent(stationListener)
    //    }
}


