package com.me.o_chat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.me.o_chat.R
import com.me.o_chat.models.Station
import kotlinx.android.synthetic.main.activity_image_textureview.*
import kotlinx.android.synthetic.main.activity_station_create_p1.*
import java.io.File
import java.util.*

//private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File

class ImageCaptureActivity2 : AppCompatActivity() {


    private var imageview: ImageView? = null
    private var viewFinder: TextureView? = null
    lateinit var auth: FirebaseAuth
    lateinit var userImage: Uri
    var userId : String = ""
    var storeLoc: String = ""
    lateinit var fileName :String
    lateinit var st: FirebaseStorage
    lateinit var db : FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_textureview)
        imageview = findViewById<View>(R.id.imageView) as ImageView
        viewFinder = findViewById(R.id.view_finder) as TextureView
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        db = FirebaseDatabase.getInstance()
        st = FirebaseStorage.getInstance()




        select_btn.setOnClickListener {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(UUID.randomUUID().toString())
            val fileProvider = FileProvider.getUriForFile(this,"com.me.o_chat.fileprovider",photoFile)
            Log.d("msg", "In btn Listener  ${photoFile.absolutePath.toUri()}")

            letsStoreImageToStorage(photoFile.toString())
            Log.d("msg", "In btn Listener photo file  ${photoFile}")
            Log.d("msg", "In btn Listener file provider ${fileProvider}")


            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileProvider)
            if(takePictureIntent.resolveActivity(this.packageManager) != null){
                startActivityForResult(takePictureIntent,12)
            } else{
                Toast.makeText(this,"unable to opene Camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName,".jpg",storageDirectory)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK ) {
            val imageTaken = BitmapFactory.decodeFile(photoFile.absolutePath)
            Glide.with(this).load(imageTaken).into(imageView);
         //   imageView.setImageBitmap(imageTaken)
        } else{
            super.onActivityResult(requestCode, resultCode, data)
        }


    }


    private fun letsStoreImageToStorage(fileName: String){
        //runBlocking {
        storeLoc = ""
      //  val fileName = UUID.randomUUID().toString()
        var store = st.getReference("/Image/${fileName}")
        Log.d("msg", "In Lets store Image, just pFile ${photoFile}")
        Log.d("msg", "In Lets store Image ${photoFile.absolutePath.toUri()}")
        store.putFile(photoFile.absolutePath.toUri()).addOnSuccessListener {
            Log.d("msg", "Uploaded Image to storage ${it.metadata?.path}")
            store.downloadUrl.addOnSuccessListener {
                Log.d("msg", "saving storage ref to DB ${it.toString()}")
                // saveImageSorageRefToDataBase(it.toString())
                storeLoc = it.toString()

            //    val user = FirebaseAuth.getInstance().currentUser?.uid.toString()

            //    FirebaseDatabase.getInstance().getReference().child(user).child(eName).child("stations").child(currentStationID).addValueEventListener(object:
            //        ValueEventListener {
            //        override fun onDataChange(dataSnapshot: DataSnapshot) {
            //            val localSation = dataSnapshot.getValue(Station::class.java)
            //            Log.d("station snapshot",localSation.toString())
            //            dataSnapshot.getRef().child("simage").setValue(storeLoc!!)
            //        }
//
  //                  override fun onCancelled(databaseError: DatabaseError) {
    //                    Log.d("Station", "UpateSatation:onCancelled", databaseError.toException())
      //              }

         //       })
            }
        }
        //  }
        //  Log.d("msg", "This is the ref that is being returned ${storeLoc}")
        // return storeLoc
    }




}