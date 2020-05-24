package com.me.o_chat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.me.models.firebase.PlacemarkFireStore
import com.me.o_chat.activities.LoginActivity
import com.me.o_chat.R
import com.me.o_chat.helpers.readImage
import com.me.o_chat.helpers.showImagePicker
import kotlinx.android.synthetic.main.activity_registration.*
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.me.o_chat.activities.FirstActivity
import com.me.o_chat.helpers.readImageFromPath
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_registration.reg_tv_note
import kotlinx.android.synthetic.main.activity_registration.select_photo
import kotlinx.android.synthetic.main.activity_registration2.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


import java.io.File
import java.util.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI

class MainAdminUserRegActivity : AppCompatActivity() {

  //  lateinit var pObj: PlacemarkFireStore
    lateinit var auth: FirebaseAuth
    var selectedPhoto: Uri? = null
    var storeLoc: String = ""
    lateinit var st: FirebaseStorage
    lateinit var db : FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_admin)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        st = FirebaseStorage.getInstance()




        reg_btn.setOnClickListener {
            val uName: String = reg_username.text.toString()
            val uPassword: String = reg_password.text.toString()
            val uEmail: String = reg_email.text.toString()
           // val uOrgRef:String = reg_org.text.toString()
            if (uPassword.isEmpty() || uEmail.isEmpty()) return@setOnClickListener
            Log.d("Reg", "hey logged in ${uName} and email ${uEmail}")
            letsRegisterCustomer(uPassword, uEmail,uName)
        }


        reg_tv_note.setOnClickListener {
            Log.d("Reg", "hey already have an Account")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }


    // will register User and save the users photo to the storage database
    // and the location will be save in the User Object
    private fun letsRegisterCustomer(uPassword: String, uEmail: String, uName:String){

        auth.createUserWithEmailAndPassword(uEmail, uPassword)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("Register", "Register successful")
                val uid = auth.currentUser!!.uid
                val name = uName
                val email = uEmail
                // val orgRef:String = uOrgRef
                val utype:String = "Admin"
                lateinit var image :String
               // image = letsStoreImageToStorage()
                image = storeLoc
                Log.d("msg", "this is the Image , in lets Reg ${image}")


                val user = User(name,email,image,uid,"","",utype)
                runBlocking {
                    saveUserToDataBase(user)
                }
                val intent =Intent(this, FirstAdminActivity::class.java)
                // clear off activities on Activity stack
                intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("main", "failed to create user $it.message")
            }


    }



    private fun saveUserToDataBase(user: User) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${user.uUid}")
        ref.setValue(user).addOnSuccessListener {
           // letsStoreImageToStorage(user)

        }

    }


    // leave for another time

    private fun letsStoreImageToStorage(user: User){
        //        //runBlocking {
            storeLoc = ""
            val fileName = UUID.randomUUID().toString()
            var store = st.getReference("/Image/${fileName}")

        val dtatab = db.getReference("/users/${user.uUid}")

           store.putFile(selectedPhoto!!).addOnSuccessListener {
                Log.d("msg", "Uploaded Image to storage ${it.metadata?.path}")
                store.downloadUrl.addOnSuccessListener {
                   Log.d("msg", "saving storage ref to DB ${it.toString()}")
                    // saveImageSorageRefToDataBase(it.toString())
                    storeLoc = it.toString()
                    val ref = FirebaseDatabase.getInstance().getReference("/users/${user.uUid}")
                    ref.child("uimage").setValue(storeLoc)


                }
            }
    }



 //   Future<String> uploadPic(File _image) async {
//
//        String fileName = basename(_image.path);
//        StorageReference firebaseStorageRef = FirebaseStorage.instance.ref().child(fileName);
//        StorageUploadTask uploadTask = firebaseStorageRef.putFile(_image);
//        var downloadURL = await(await uploadTask.onComplete).ref.getDownloadURL();
//        var url =downloadURL.toString()/;
//
 //       return url;
//
  //  }


    //select_photo.setOnClickListener {
    //            Log.d("main", "hey select photo")
    //
    //            // val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
    //
    //            // shows camera dir of images
    //            //  val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    //
    //            // this is for CAMERA
    //             val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    //
    //        //     intent.type = "image/*"
    //
    //      //      showImagePicker(this, 12)
    //
    //            startActivityForResult(intent,12)
    //        }



    ////    private fun updateImageToFB() {
    ////        val userId = FirebaseAuth.getInstance().currentUser!!.uid
    ////        val db = FirebaseDatabase.getInstance().reference
    ////        db.child("users").child(userId).setValue(userId)
    //        //  st = FirebaseStorage.getInstance().reference
    //        //  val fileName = File(selectedPhoto)
    //        //  val imageName = fileName.getName()
    //
    //        //  Log.d("Register Photo","loaded to storatge ${selectedPhoto}")
    //        // st.putFile(selectedPhoto!!).addOnSuccessListener {
    //        //  Log.d("Register Photo","loaded to storatge ${it.metadata?.path}")
    ////    }
    //
    //// storing updated image to St
    ////    private fun saveImageSorageRefToDataBase(profileImageUrl: String) {
    ////        val uid = FirebaseAuth.getInstance().currentUser!!.uid
    ////        val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
    ////        val user = User("jack",profileImageUrl,uid!!)
    ////        ref.setValue(user!!)
    ////    }



    /// this set the image or resets the image in the Button of the Register/ MainActivity
    //// the selectPhot is an object that will be available to all functions
    //
    //        if (requestCode == 12 && resultCode == Activity.RESULT_OK && data != null) {
    //            Log.d("ImageSelected", "here now")
    //            selectedPhoto = data.data
    //
    //            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhoto)
    //            select_photo.alpha = 0f
    //            profile_image.setImageBitmap(bitmap)
    //
    //           // val bitmapDrawable = BitmapDrawable(bitmap)
    //           // select_photo.setBackgroundDrawable(bitmapDrawable)
    //          //  letsStoreImageToStorage()
    //        }



    }
