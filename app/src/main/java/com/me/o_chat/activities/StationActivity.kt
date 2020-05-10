package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.*
import com.me.o_chat.R
import com.me.o_chat.models.Message
import com.me.o_chat.models.Station
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.activity_station.*



// this lists the message activity on a selected Station
// and allows you to send a message to the Station

class StationActivity : AppCompatActivity(), MessageListener {


    lateinit var  messageList : ArrayList<Message>
    lateinit var station : Station


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station)

       // station object sent to this acticity
        station = intent.getParcelableExtra("Kstation")

        supportActionBar?.title = "Message with ${station.sName}"

       // rv_chatlog.adapter
        recyclerviewStation.adapter
        messageList = ArrayList<Message>()
        // getUsers()
        Log.d("getMessages", "the number of messages sent to this Station  ${messageList.size}")
        val layoutManager = LinearLayoutManager(this)

        recyclerviewStation.layoutManager = layoutManager as RecyclerView.LayoutManager

        MessagesAllUpToDate()

        st_button.setOnClickListener{
            Log.d("on Bln","Clicked Station Button")
            sendMessage()
            recyclerviewStation.adapter?.notifyDataSetChanged()
        }


    //    recyclerviewStation.adapter = MessageAdapter(messageList, this,null)




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


    }

    private fun sendMessage(){
        var mes = st_et.text.toString()
        val fromM = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val  toM = station.sUid.toString()
      //  val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/teams/${fromM}/${toM}").push()
        val refTo = FirebaseDatabase.getInstance().getReference("/stations/${toM}/${fromM}").push()
        val refId = ref.key.toString()
        val refIdTo = refTo.key.toString()
        val messM:Message = Message(mes,"",refId,fromM,toM,System.currentTimeMillis()/1000)
        ref.setValue(messM)
        refTo.setValue(messM).addOnSuccessListener {
            cl_et.text.clear()
        }
        recyclerviewStation.adapter?.notifyDataSetChanged()
        recyclerviewStation.scrollToPosition(recyclerviewStation.adapter?.itemCount!!)



    }


    private fun MessagesAllUpToDate(){
        val fromM = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val  toM = station.sUid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/stations/${toM}")
            ref.addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                    // each time messages db has a new entry, its add to the array list
                    var message = p0.getValue(Message::class.java)
                    if(message != null) {
                        messageList.add(message)
                        recyclerviewStation.adapter?.notifyDataSetChanged()
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
        Log.d("at messageAdpter", "size =  ${messageList.size}")


    }

    override fun onMessageClick(message: Message){

    //    val intent = Intent(this,NewMessageActivity::class.java)
    //    startActivity(intent)

    }
}
