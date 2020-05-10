package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.R
import com.me.o_chat.models.Event
import com.me.o_chat.models.GoalAchObj
import com.me.o_chat.models.Station
import com.me.o_chat.models.User
import com.me.o_chat.models.Result
import kotlinx.android.synthetic.main.activity_here_and_now.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.nav_header_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

private lateinit var ref1 : DatabaseReference
private  lateinit var InPt: String

class HereAndNowActivity : AppCompatActivity() {

    // here and Now, creates an GoalAchObj

    private val KEY_TEXT_REPLY = "key_text_reply"
    private var stn = ""
    private var evt = ""
    private lateinit var goalAch: GoalAchObj
    private var inputString: String = ""
    private lateinit var eventOut: Event
    private lateinit var EvtResult: Result
    private var numberAchieved: Int = 0;
    private var timeTaken: Int = 0;
    var stationAch: Boolean = false
    private lateinit var goalInHand: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_here_and_now)
        ref1 = FirebaseDatabase.getInstance().reference
        eventOut = Event()
        goalAch = GoalAchObj()
        EvtResult = Result()
        goalInHand = ArrayList<String>()
        /**
         *
         *
         */

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val intent = this.intent

        val remoteInput = androidx.core.app.RemoteInput.getResultsFromIntent(intent)
        Log.d("Her and NOw intent extra ", intent.extras.toString())

        if (remoteInput != null) {
            inputString = remoteInput.getCharSequence(
                KEY_TEXT_REPLY
            ).toString()
            tv_handn.text = inputString
        }

        Log.d("Her and NOw inputString  ", inputString)

        InPt = intent.getStringExtra("STATION_ID")
        Log.d("Her and NOw inPt ", InPt)

        if (InPt != null) {
            stn = InPt.substringBefore('/', InPt)
            evt = InPt.substringAfter('/', InPt)
            // eventOut = getEventObject(evt)
            getEventObject(evt)
            Log.d("Her and NOw stn ", stn)
            Log.d("Her and NOw evt ", evt)
            goalAchieved({ hasGoalAchievedAlready(evt, stn, userId) }, evt, stn, userId)
        }


        runBlocking {
            delay(2000L)  // ... delay for 2 seconds to keep JVM alive
        }



        btn_handn.setOnClickListener {
            Log.d("Event Out", "id ${eventOut.eUid}")
            Log.d("Event Out", "id ${eventOut.toString()}")
            val intent = Intent(applicationContext, MapUserSationsAllActivity::class.java)
            intent.putExtra("Kevent", eventOut)
            startActivity(intent)
        }


    }

    // end of on Create

    private fun getEventObject(evt: String): Unit {
        var myEvent: Event = Event()
        ref1!!.child("events").child(evt!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    myEvent = dataSnapshot!!.getValue(Event::class.java)!!
                    Log.d("Her and NOw evt ", myEvent.toString())
                    if (myEvent != null) {
                        eventOut = myEvent
                    }
                    //    ref1.removeEventListener(this)
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }


    private fun getResults(stationsReady: () -> Unit, evt: String, userId: String) {

        Log.d("Results to Date", "Geting no avhived and time taken")

        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var reslt = p0.getValue(Result::class.java)
                if (reslt != null) {
                    numberAchieved = reslt.rNumberAch
                    timeTaken = reslt.rTimeTaken
                }
                stationsReady()
                //removeEventListener(this)
            }
        }
        ref1.child("Results").child("${evt}").child("${userId}")
            .addListenerForSingleValueEvent(stationListener)

    }


    private fun updateResult(evt: String, userId: String) {
        Log.d("in update result", "having arrived at new station")
        val stationListener = object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("Results", "In value listener on data change")
                if (dataSnapshot.hasChild("${userId}")) {
                    var myResult: Result =
                        dataSnapshot!!.child("${userId}").getValue<Result>(Result::class.java)!!
                    Log.d("Result exits", "${myResult.rUserId.toString()}")
                    timeTaken = timeTaken + 34
                    numberAchieved = numberAchieved + 1
                    myResult.rTimeTaken = timeTaken
                    myResult.rNumberAch = numberAchieved

                    ref1!!.child("Results").child("${evt}").child("${userId!!}").setValue(myResult)
                    // ref1.getReference("Results/${evt}/${userId}")
                    //    ref1!!.child("Results").child("${evt}").child("${userId!!}").child("rTimeTaken").setValue({timeTaken})
                    //    ref1!!.child("Results").child("${evt}").child("${userId!!}").child("rNumberAch").setValue({numberAchieved})
                } else {
                    Log.d("user Result", "not existing")
                    val refRes = ref1.child("Results").child("${evt}").child("${userId!!}")
                    val refResId = refRes.getKey().toString()
                    timeTaken = timeTaken + 34
                    numberAchieved = numberAchieved + 1
                    EvtResult.rNumberAch = numberAchieved
                    EvtResult.rTimeTaken = timeTaken
                    EvtResult.rEventId = evt
                    EvtResult.rUserId = userId
                    refRes.setValue(EvtResult)
                }

                ref1.removeEventListener(this)
            }
        }

        ref1.child("Results").child("${evt}").addListenerForSingleValueEvent(stationListener)
    }


    private fun newGoalAchieved(evt: String, stn: String, userId: String) {
        val ref = ref1.child("GoalAchieved/${evt}/Stations/${stn}").push()
        val refId = ref.getKey().toString()
        goalAch.gMessage = inputString
        goalAch.gStationUid = stn.toString()
        goalAch.gEventUid = evt
        goalAch.gTeam = userId
        goalAch.gTime = System.currentTimeMillis() / 1000

        Log.d("Goal Achieved", "${goalAch.toString()}")
        Log.d("Goal Achieved shall we post", "${stationAch.toString()}")
        if (stationAch == false) {
            ref.setValue(goalAch)
            getResults({ updateResult(evt, userId) }, evt, userId)
        }
    }

    // test is gaol has been achieved already
    private fun hasGoalAchievedAlready(evt: String, stn: String, userId: String) {
        if (stationAch == false)
            newGoalAchieved(evt, stn, userId)
    }



    private fun  goalAchieved(stationsReady: () -> Unit,evt:String,stn:String,userId:String) {
        // Once we have the current User informtion we can seek the Administrators of the Event
        // the Particpants can only talk to Administrators of the Event that they are signed up for
        // so we interate through the Users that are classed as Admin, and iterate through there evenst
        // and once we find an uEcode that matches the current user, they are added to list
        Log.d("in has goal achieved already", "getting closer")
        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    var goalA = it.getValue(GoalAchObj::class.java)
                    if (goalA!!.gTeam == userId && goalA != null && goalA.gStationUid == stn) {
                        Log.d("Goal Achieved", "Goal Achieved already")
                        Log.d("Goal Achieved", goalA.gTeam)
                        goalInHand.add(goalA.gStationUid)
                        stationAch = true
                    }
                    Log.d("Goal Achieved BY someelse ", goalA.gTeam)

                }
                stationsReady()
                Log.d("Goal Achieved Array size", goalInHand.size.toString())
        }

        }
        ref1.child("GoalAchieved/${evt}/Stations/${stn}").addListenerForSingleValueEvent(stationListener)

    }









// delete
//val refResults = FirebaseDatabase.getInstance().reference.child("Results").child("${evt}").child("${userId}")
//        refResults.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                    var reslt = p0.getValue(Result::class.java)
//                    if(reslt != null) {
//                        numberAchieved = reslt.rNumberAch
//                        timeTaken = reslt.rTimeTaken
//                    }
//                refResults.removeEventListener(this)
//                }
//
//
//        })



    // ref1.child("Results").child("${evt}").addListenerForSingleValueEvent(object :ValueEventListener {
    //
    //            override fun onDataChange(dataSnapshot: DataSnapshot) {
    //                Log.d("Results","In value listener on data change")
    //             if(dataSnapshot.hasChild("${userId}")) {
    //                    var myResult: Result = dataSnapshot!!.child("${userId}").getValue<Result>(Result::class.java)!!
    //                    Log.d("Result exits", "${myResult.rUserId.toString()}")
    //                    timeTaken = timeTaken + 34
    //                    numberAchieved = numberAchieved +1
    //                    myResult.rTimeTaken = timeTaken
    //                    myResult.rNumberAch = numberAchieved
    //
    //                    ref1!!.child("Results").child("${evt}").child("${userId!!}").setValue(myResult)
    //                // ref1.getReference("Results/${evt}/${userId}")
    //                //    ref1!!.child("Results").child("${evt}").child("${userId!!}").child("rTimeTaken").setValue({timeTaken})
    //                //    ref1!!.child("Results").child("${evt}").child("${userId!!}").child("rNumberAch").setValue({numberAchieved})
    //                } else {
    //                    Log.d("user Result", "not existing")
    //                    val refRes = ref1.child("Results").child("${evt}").child("${userId!!}")
    //                    val refResId = refRes.getKey().toString()
    //                    timeTaken = timeTaken + 34
    //                    numberAchieved = numberAchieved +1
    //                    EvtResult.rNumberAch = numberAchieved
    //                    EvtResult.rTimeTaken = timeTaken
    //                    EvtResult.rEventId = evt
    //                    EvtResult.rUserId = userId
    //                    refRes.setValue(EvtResult)
    //                }
    //
    //                ref1.removeEventListener(this)
    //            }
    //
    //            override fun onCancelled(p0: DatabaseError) {
    //                Log.d("Results","In value listener cancelled")
    //            }
    //
    //        })



    //
    //        // val ref = FirebaseDatabase.getInstance().getReference("/${admin}/${EventId}/stations").push()
    //
    //
    //        //if(hasChild("${ref1.child("Results").child("${evt}").child("${userId}")}")) {
    //        //    Log.d("Results", "has child is true")
    //        ///} else {
    //        //    Log.d("Results", "has child is false")
    //       // }
    //
    //      // ref1.child("Results").child("${evt}").child("${userId}").addValueEventListener(object :ValueEventListener {
    //        //   override fun onDataChange(dataSnapshot: DataSnapshot) {
    //        //       if (dataSnapshot.exists()) {
    //        //           var myResult: Result = dataSnapshot!!.getValue(Result::class.java)!!
    //        //           Log.d("Result exits", "${myResult!!.toString()}")
    //        //           ref1!!.child("Results").child("${evt}").child("${userId!!}").child("rTimeTaken").setValue(timeTaken + 34)
    //       //            ref1!!.child("Results").child("${evt}").child("${userId!!}").child("rNumberAch").setValue(numberAchieved + 1)
    //       //            Log.d("Updated Result", "possibly")
    //       //        } else {
    //       //            Log.d("dded Result", "possibly")
    //      //             val refRes = ref1.child("Results").child("${evt}").child("${userId!!}").push()
    //     ///              val refResId = refRes.getKey().toString()
    //     //              EvtResult.rNumberAch = numberAchieved + 1
    //    //               EvtResult.rTimeTaken = timeTaken + 34
    //    //               EvtResult.rEventId = evt
    //    //               EvtResult.rUserId = userId
    //     //              refRes.setValue(EvtResult)
    //    //           }
    //
    //
    //  //         }
    //
    ////           override fun onCancelled(p0: DatabaseError) {
    ////           }
    ////
    ////       })
}
