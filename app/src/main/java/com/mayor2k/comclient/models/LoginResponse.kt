package com.mayor2k.comclient.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginResponse {
    @SerializedName("token")
    var token: String? = null
}