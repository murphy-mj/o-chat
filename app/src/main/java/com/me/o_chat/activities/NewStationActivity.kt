package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_new_message.*
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.me.o_chat.*
import com.me.o_chat.models.Station
import kotlinx.android.synthetic.main.activity_new_station.*
import kotlinx.android.synthetic.main.activity_station_create_p1.*
import kotlinx.android.synthetic.main.activity_station_review.*
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay


class NewStationActivity : AppCompatActivity(), StationListener {

    lateinit var  stationList : ArrayList<Station>
    var eName: String = "wexford2020"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_station)
        supportActionBar?.title = "Select Station"
        recyclerviewNewStation.adapter
        stationList = ArrayList<Station>()
        // getUsers()
        Log.d("Stations", "been there done that ${stationList.size}")
        val layoutManager = LinearLayoutManager(this)
        recyclerviewNewStation.layoutManager = layoutManager as RecyclerView.LayoutManager
       // dummy data
      // var A_User = Station("12346", "Station One", "a wooden sculpture")
      //  var B_User = Station("7654321", "Station Two", "a metal sculpture")
      //  stationList.add(A_User)
      //  stationList.add(B_User)
        val user = FirebaseAuth.getInstance().currentUser?.uid.toString()

      //  val ref = FirebaseDatabase.getInstance().getReference("/stations")
        val ref = FirebaseDatabase.getInstance().getReference().child(user).child(eName).child("stations")


        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d("station", it.toString())
                    var station = it.getValue(Station::class.java)
                    if(station != null) {
                        stationList.add(station!!)
                        recyclerviewNewStation.adapter?.notifyDataSetChanged()
                    }
                }

            }

        })

        recyclerviewNewStation.adapter = StationAdapter(stationList, this)
        Log.d("at stationAdpter", "size =  ${stationList.size}")




        //recyclerviewNewMessage.adapter = UserAdapter(userList, this)
        //    recyclerviewNewMessage.adapter = UserAdapter(userList, this)
    }





    override fun onStationClick(station: Station){
        val intent = Intent(this,StationActivity::class.java)
        intent.putExtra("Kstation",station )
        startActivity(intent)
       // finish()
    }


    }