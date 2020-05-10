package com.me.o_chat.main

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
import com.me.o_chat.activities.MainAdminUserRegActivity
import com.me.o_chat.activities.MainUserRegActivity
import com.me.o_chat.helpers.readImageFromPath
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_admin_or_user.*
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

class MainActivity : AppCompatActivity() {

    lateinit var pObj: PlacemarkFireStore
    lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_or_user)


        part_add_btn.setOnClickListener {
            val intent = Intent(this, MainUserRegActivity::class.java)
            startActivity(intent)
        }


        admin_add_btn2.setOnClickListener {
            val intent = Intent(this, MainAdminUserRegActivity::class.java)
            startActivity(intent)
        }


    }



    }
