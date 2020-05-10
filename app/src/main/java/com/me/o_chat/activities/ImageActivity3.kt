package com.me.o_chat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.me.o_chat.R
import com.me.o_chat.activities.NewStationActivity
import com.me.o_chat.helpers.showImagePicker
import com.me.o_chat.models.Station
import kotlinx.android.synthetic.main.activity_station_create_p1.*
import kotlinx.android.synthetic.main.content_station_image.*
import java.util.*


class ImageActivity3 : AppCompatActivity(){

    private lateinit var currentStationID: String
    lateinit var auth: FirebaseAuth
    var selectedPhoto: Uri? = null
    var storeLoc: String = ""
    lateinit var st: FirebaseStorage
    lateinit var db : FirebaseDatabase
    var eName: String = "wexford2020"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.content_station_image)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        st = FirebaseStorage.getInstance()

        if (intent.hasExtra("sUid")) {
            currentStationID = intent.extras?.getString("sUid")!!
        }


        imageView.setOnClickListener {
            Log.d("main", "hey select photo")

            // val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

            // shows camera dir of images
            //  val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            // this is for CAMERA
            //  val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // intent.type = "image/*"

            showImagePicker(this, 12)

            //   startActivityForResult(intent,12)
        }

        image_btn.setOnClickListener {
            val intent: Intent = Intent(this, NewStationActivity::class.java)
            startActivity(intent)

        }

    }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

// this set the image or resets the image in the Button of the Register/ MainActivity
// the selectPhot is an object that will be available to all functions

            if (requestCode == 12 && resultCode == Activity.RESULT_OK && data != null) {
                Log.d("ImageSelected", "here now")
                selectedPhoto = data.data

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhoto)
               // imageView.alpha = 0f  0f mokes it invisible
                imageView.setImageBitmap(bitmap)

             //    val bitmapDrawable = BitmapDrawable(bitmap)
             //   imageView.setBackgroundDrawable(bitmapDrawable)
                letsStoreImageToStorage()
            }

        }


        private fun letsStoreImageToStorage(){
            //runBlocking {
            storeLoc = ""
            val fileName = UUID.randomUUID().toString()
            var store = st.getReference("/Image/${fileName}")

            store.putFile(selectedPhoto!!).addOnSuccessListener {
                Log.d("msg", "Uploaded Image to storage ${it.metadata?.path}")
                store.downloadUrl.addOnSuccessListener {
                    Log.d("msg", "saving storage ref to DB ${it.toString()}")
                    // saveImageSorageRefToDataBase(it.toString())
                    storeLoc = it.toString()

                    val admin = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    FirebaseDatabase.getInstance().getReference().child(admin).child(eName).child("stations")
                        .child(currentStationID).addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                         //   val localSation = dataSnapshot.getValue(Station::class.java)
                         //   Log.d("station snapshot",localSation.toString())
                            dataSnapshot.getRef().child("simage").setValue(storeLoc!!)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.d("Station", "UpateSatation:onCancelled", databaseError.toException())
                        }

                    })
                }
            }
            //  }
            //  Log.d("msg", "This is the ref that is being returned ${storeLoc}")
            // return storeLoc
        }

}

