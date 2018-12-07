package com.sourtime.www.firemessage.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val email: String, val photourl: String) : Parcelable {
    constructor() : this("","","","")
}