package com.me.o_chat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable


class Event_S(var eName: String = "",
              var eDescription: String = "",
              var eUid: String = ""
) : Serializable  {
    constructor(): this("","","")
}