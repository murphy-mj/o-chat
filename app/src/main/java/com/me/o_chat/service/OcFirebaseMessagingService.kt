package com.me.o_chat.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OcFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Log.d("TCM","Notification token is ${p0}")
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d("TCM","Notification message received")
    }



}