package com.me.o_chat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Event(var eName: String = "",
            var eDescription: String = "",
            var eCode:String = "",
            var eOrganiser:String = "",
            var eUid: String = "",
            var eStartTime :Long = 0L,
            var eEndTime :Long = 0L

) : Parcelable  {
    constructor(): this("","","","","",0L,0L)
}