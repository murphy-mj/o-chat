package com.me.o_chat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class GoalAchObj(var gUid: String = "",
                 var gEventUid: String = "",
                 var gStationUid: String = "",
                  var gImage: String = "",
                  var gMessage:String ="",
                 var gTime:Long =0,
                 var gTeam:String =""

    ) : Parcelable {
        constructor() : this("","","","","",0,"")
    }

