package com.me.o_chat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.me.o_chat.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.me.o_chat.models.Event
import com.me.o_chat.models.Station
import com.me.o_chat.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    var uType :String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        logn_button.setOnClickListener {
            // val uName: String = logn_username.text.toString()
            val uPassword: String = logn_password.text.toString()
            val uEmail: String = logn_Email.text.toString()
            if(uPassword.isEmpty() || uEmail.isEmpty()) return@setOnClickListener
               letsLognCustomer(uPassword, uEmail)


        }
    }


    private fun letsLognCustomer(uPassword:String, uEmail:String){
        auth.signInWithEmailAndPassword(uEmail, uPassword)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("Login", "login sucessful")
                val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

                getType({ setUtype() }, userId)

                Log.d("Login type post gettype", "utype ${uType}")


            }.addOnFailureListener {
                Log.d("main","failed to login, why  $it.message")
            }

    }


    private fun getType2(userId:String) : String {
        var  currentUser : User = User()
        Log.d("Login","in getType")
        val ref2 = FirebaseDatabase.getInstance().getReference("/users/${userId}")
        ref2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                Log.d("Login p0",p0.toString())
                var currentUser: User = p0.getValue(User::class.java)!! as User
                Log.d("Login current user","${currentUser}")
            }
        })
         Log.d("Login User",currentUser.uName)
        return currentUser.uType
    }


    private fun getType(stationsReady: () -> Unit, userId: String) {

        val stationListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                Log.d("Login p0", p0.toString())
                var currentUser: User = p0.getValue(User::class.java)!! as User
                uType = currentUser.uType
                Log.d("Login current user", "${currentUser}")
                stationsReady()
            }
        }
        FirebaseDatabase.getInstance().getReference("/users/${userId}").addListenerForSingleValueEvent(stationListener)

    }


        private fun setUtype(){
            Log.d("Login","in Set Uype")
            if (uType == "Admin") {
            Log.d("Login type", "utype ${uType}")
            startActivity(Intent(this, FirstAdminActivity::class.java))
            } else {
                 startActivity(Intent(this, FirstActivity::class.java))
               }

}

}