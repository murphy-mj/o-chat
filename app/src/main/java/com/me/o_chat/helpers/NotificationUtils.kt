/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.me.o_chat.helpers

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.RemoteViews
import androidx.camera.camera2.impl.Camera2CaptureRequestBuilder.build
import androidx.core.app.NotificationCompat
import com.me.o_chat.R
import com.me.o_chat.activities.HereAndNowActivity

/*
 * We need to create a NotificationChannel associated with our CHANNEL_ID before sending a
 * notification.
 */
fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }
        // notification badges (also known as notification dots) appear on a launcher icon when the associated app has an active notification
        // setting it to false, setShowBadge

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(false)
        notificationChannel.description = context.getString(R.string.notification_channel_description)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}


/*
 * A Kotlin extension function for AndroidX's NotificationCompat that sends our Geofence
 * entered notification.  It sends a custom notification based on the name string associated
 * with the LANDMARK_DATA from GeofencingConstatns in the GeofenceUtils file.
 */
fun NotificationManager.sendGeofenceEnteredNotification(context: Context, foundIndex: String) {

    val contentView = RemoteViews("com.me.o_chat", R.layout.notification_layout)
    contentView.setTextViewText(R.id.tv_title,"How about this then")
    contentView.setTextViewText(R.id.tv_content,"How about this then and then and now")

    val replyLabel = "Enter your reply here"
    val remoteInput = androidx.core.app.RemoteInput.Builder(KEY_TEXT_REPLY)
        .setLabel(replyLabel)
        .build()


    //  val contentIntent = Intent(context, MapUserSationSelected3Activity::class.java)
    val contentIntent = Intent(context, HereAndNowActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }


    val mapImage = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.map_small
    )





    contentIntent.putExtra(GeofencingConstants.EXTRA_GEOFENCE_INDEX, foundIndex)
   // contentIntent.putExtra("EvtID", foundIndex)

    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val action = NotificationCompat.Action.Builder(R.drawable.rounded_btn, "Open", contentPendingIntent)



    val replyAction = NotificationCompat.Action.Builder(R.drawable.rounded_btn, "Send Message", contentPendingIntent)
        .addRemoteInput(remoteInput)
        .build()


    val replyAction2 = NotificationCompat.Action(R.drawable.rounded_btn, "Open", contentPendingIntent)


    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(mapImage)
        .bigLargeIcon(null)

    // We use the name resource ID from the LANDMARK_DATA along with content_text to create
    // a custom message when a Geofence triggers.
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(contentIntent.getStringExtra("STATION_ID"))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .setSmallIcon(R.drawable.map_small)
        .addAction(replyAction2)
        .addAction(replyAction)
        .setGroup(GROUP_KEY_STATION_ONE)
     //   .build()

    notify(NOTIFICATION_ID, builder.build())

    //.setContentText(context.getString(R.string.content_text,
    //  context.getString(GeofencingConstants.LANDMARK_DATA[foundIndex].name)))

}




val GROUP_KEY_STATION_ONE = "GROUP KEY STATION ONE"
private const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "com.me.o_chat"
private val KEY_TEXT_REPLY = "key_text_reply"

