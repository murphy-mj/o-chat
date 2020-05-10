package com.me.o_chat.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
class Station1(var uName: String = "",
               var uImage: String = "",
               var uUid: String = ""
) : Parcelable  {
    constructor(): this("","","")
}