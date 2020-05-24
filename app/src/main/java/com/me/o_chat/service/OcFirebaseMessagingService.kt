package com.me.o_chat.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.me.o_chat.R
import com.me.o_chat.activities.FirstActivity
import com.me.o_chat.main.MainActivity



class OcFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Log.d("TCM","Notification token is ${p0}")
    }


    //Message received from Firebase Messaging Service
    // sending it to the post login screen FirstActivity
    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d("TCM","Notification message received")
        Log.d("TCM", "From: ${p0?.from}")
        p0?.data!!.isNotEmpty().let {
        Log.d("TCM", "Message data payload: " + p0?.data)
        Log.d("TCM", "Notification Message Body: " + p0?.notification?.body!!)
            val intent = Intent(this@OcFirebaseMessagingService, FirstActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("message", p0?.notification?.body!!)
            startActivity(intent)
      //  sendNotification(p0)
        }

    }


    private fun sendNotification(remoteMessage: RemoteMessage) {
        Log.d("TCM", "Send Notification")
        val intent = Intent(this, FirstActivity::class.java)
        //nstead of launching a new instance of that activity, all of the other activities
        // on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT)


            //intent,PendingIntent.FLAG_ONE_SHOT)

            //PendingIntent.FLAG_UPDATE_CURRENT)

            // PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationCompat.Builder(this)
           .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setSmallIcon(com.me.o_chat.R.drawable.cooked_egg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0/* ID of notification */, notificationBuilder.build())
    }






}