package com.me.o_chat.models


import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
class Station3(var sUid: String = "",
              var sName: String = "",
              var sDescription:String = "",
              var sImage: String = "",
              var sProfilePic:String ="",
              var sVisit_date:String = "na",
              var sVisit_yn: Boolean = false,
              var sLocation : Location = Location()

) : Parcelable  {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
        "sUid" to sUid,
        "sName" to sName,
        "sDescription" to sDescription,
        "sImage" to sImage,
        "sProfilePic" to sProfilePic,
        "sVisit_date" to sVisit_date,
        "sVisit_yn" to sVisit_yn,
        "slocation" to sLocation
        )
    }
}

@Parcelize
data class Location3(var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable




