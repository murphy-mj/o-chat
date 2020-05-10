package com.me.o_chat.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList

@Parcelize
class AdminUser(var uName: String = "",
                var uImage: String = "",
                var uUid: String = "",
                var uOrgRefAdmin :String = ""

) : Parcelable  {
    constructor(): this("","","","")
}