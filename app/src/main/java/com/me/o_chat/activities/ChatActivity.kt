package com.me.o_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.me.o_chat.MessageAdapter
import com.me.o_chat.MessageListener
import com.me.o_chat.R
import com.me.o_chat.UserAdapter
import com.me.o_chat.models.Message
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.activity_chatlog.view.*
import kotlinx.android.synthetic.main.activity_new_message.*

class ChatActivity : AppCompatActivity(), MessageListener {


    lateinit var  messageList : ArrayList<Message>
    lateinit var contact : User
    lateinit var recyclerviewChatLog :RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatrv)

        contact = intent.getParcelableExtra("Kuser")
        messageList = ArrayList<Message>()
        supportActionBar?.title = "Chat with ${contact.uName}"
        if(contact.uType == "Admin") {
            supportActionBar?.setIcon(R.mipmap.ic_message_in)
        } else {
            supportActionBar?.setIcon(R.mipmap.ic_message_out)

        }
        recyclerviewChatLog = findViewById<RecyclerView>(R.id.recyclerview_ChatLog)
        val msgAdapter = MessageAdapter(messageList, this, contact)

        val layoutManager = LinearLayoutManager(this)
        recyclerviewChatLog.layoutManager = layoutManager as RecyclerView.LayoutManager
        recyclerviewChatLog.adapter = msgAdapter

        MessageAllUpToDate()


        b_chatlog.setOnClickListener{
            Log.d("on Bln","Clicked CL Button")
            sendMessage()
          //  recyclerviewChatLog.adapter?.notifyDataSetChanged()
        }


     //   rv_chatlog.adapter = MessageAdapter(messageList, this)


    }


    // We store the same message twice, so that it can be retrieved by both the Admin and the Participant in the same chat log
    private fun sendMessage(){
        var mes = et_chatlog.text.toString()
        val fromM = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val  toM = contact.uUid.toString()
      //  val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val refFrom = FirebaseDatabase.getInstance().getReference("/user-messages/${fromM}/${toM}").push()
        val refIdFrom = refFrom.key.toString()
        val messMf:Message = Message(mes,"",refIdFrom,fromM,toM,System.currentTimeMillis()/1000,false)

        val refTo = FirebaseDatabase.getInstance().getReference("/user-messages/${toM}/${fromM}").push()
        val refIdTo = refTo.key.toString()
        val messMt:Message = Message(mes,"",refIdTo,fromM,toM,System.currentTimeMillis()/1000,false)

        refFrom.setValue(messMf)
        refTo.setValue(messMt).addOnSuccessListener {
        et_chatlog.text.clear()
        }
        recyclerviewChatLog.adapter?.notifyDataSetChanged()
        recyclerviewChatLog.scrollToPosition(recyclerviewChatLog.adapter?.itemCount!!)

    }


    private fun MessageAllUpToDate(){
        val fromM = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val  toM = contact.uUid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${fromM}/${toM}")
      //  ref.addListenerForSingleValueEvent(object : ValueEventListener {
            ref.addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {
            }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    Log.d("Chat act","on child added ${p0}")
                    // each time messages db has a new entry, its add to the array list
                    var message = p0.getValue(Message::class.java)
                    if(message != null) {
                        messageList.add(message)
                        recyclerviewChatLog.adapter?.notifyDataSetChanged()
                        recyclerviewChatLog.scrollToPosition(recyclerviewChatLog.adapter?.itemCount!!)
                    }

            }

             override fun onChildMoved(p0: DataSnapshot, p1: String?) {

             }

             override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                 Log.d("Chat act","on child changed ${p0}")
                 var message = p0.getValue(Message::class.java)
                 if(message != null) {
                     messageList.add(message)
                     recyclerviewChatLog.adapter?.notifyDataSetChanged()
                     recyclerviewChatLog.scrollToPosition(recyclerviewChatLog.adapter?.itemCount!!)
                 }

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
