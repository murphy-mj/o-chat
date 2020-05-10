package com.me.o_chat.models


import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
class Station2(var sUid: String = "",
              var sName: String = "",
              var sDescription:String = "",
              var sImage: String = "",
              var sProfilePic:String ="",
              var sVisit_date:String = "na",
              var sVisit_yn: Boolean = false,
              var sLocation : Location = Location()

) : Parcelable  {
   constructor() : this("","","","","","",false,Location())
}

@Parcelize
data class Location2(var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable
{
    constructor() :this(0.0,0.0,0f)
}




