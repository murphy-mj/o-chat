package com.me.o_chat.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize


class Message(var uText: String = "",
              var uImage: String = "",
              var uUid: String = "",
              var uFrom: String = "",
              var uTo:String = "",
              var uTimeStamp:Long = 0L,
              var uViewed:Boolean = false
) {
    constructor(): this("","","","","",0,false)
}