package com.me.o_chat.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList

@Parcelize
class User(var uName: String = "",
           var uEmail:String = "",
           var uImage: String = "",
           var uUid: String = "",
           var uOrgRef :String = "",
           var uType:String = ""


) : Parcelable  {
    constructor(): this("","","","","","")
}