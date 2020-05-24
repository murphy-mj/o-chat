package com.me.o_chat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.me.o_chat.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.me.o_chat.models.Event
import com.me.o_chat.models.Station
import com.me.o_chat.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    var uType: String = ""
    lateinit var currentUser :User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        currentUser = User()
        logn_button.setOnClickListener {
            // val uName: String = logn_username.text.toString()
            val uPassword: String = logn_password.text.toString()
            val uEmail: String = logn_Email.text.toString()
            if (uPassword.isEmpty() || uEmail.isEmpty()) return@setOnClickListener
            letsLognCustomer(uPassword, uEmail)


        }
    }


    private fun letsLognCustomer(uPassword: String, uEmail: String) {
        auth.signInWithEmailAndPassword(uEmail, uPassword)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("Login", "login sucessful")
                val userId = auth.currentUser?.uid.toString()
                getType({ setUtype({topicSubscribe()}) }, userId)


            }.addOnFailureListener {
                Log.d("main", "failed to login, why  $it.message")
                Toast.makeText(this, "Authorisation Failed", Toast.LENGTH_SHORT).show()
            }

    }


    private fun getType2(userId: String): String {
      //  var currentUser: User = User()
        Log.d("Login", "in getType")
        val ref2 = FirebaseDatabase.getInstance().getReference("/users/${userId}")
        ref2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("Login p0", p0.toString())
                currentUser = p0.getValue(User::class.java)!! as User
                Log.d("Login current user", "${currentUser}")
            }
        })
        Log.d("Login User", currentUser.uName)
        return currentUser.uType
    }


    private fun getType(stationsReady: () -> Unit, userId: String) {

        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("Login p0", p0.toString())
                currentUser = p0.getValue(User::class.java)!! as User
                uType = currentUser.uType
                Log.d("Login current user", "${currentUser}")
                stationsReady()
            }
        }
        FirebaseDatabase.getInstance().getReference("/users/${userId}")
            .addListenerForSingleValueEvent(stationListener)

    }


    private fun setUtype(stationsReady: () -> Unit) {


        Log.d("Login", "in Set Uype")
        if (uType == "Admin") {
            Log.d("Login type", "utype ${uType}")
            startActivity(Intent(this, FirstAdminActivity::class.java))
        } else {
            startActivity(Intent(this, FirstActivity::class.java))
            // topic is only for participants
            stationsReady()
        }

    }

    private fun topicSubscribe() {
         FirebaseMessaging.getInstance().subscribeToTopic("${currentUser.uOrgRef}").addOnCompleteListener { task ->
       // var msg = getString(R.string.msg_subscribed)
        var msg = "subscribed to ${currentUser.uOrgRef}"

        if (!task.isSuccessful) {
          //  msg = getString(R.string.msg_subscribe_failed)
           msg = "subscribed to ${currentUser.uOrgRef} Failed"
        }
     //   Log.d(TAG, msg)
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }
   }

}