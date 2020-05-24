package com.me.o_chat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Result(var rName: String = "",
             var rNumberAch: Int = 0,
             var rUid: String = "",
             var rTimeTaken :Long = 0L,
             var rUserId: String = "",
             var rEventId: String = ""
) : Parcelable{
    constructor(): this("",0,"",0,"","")
}