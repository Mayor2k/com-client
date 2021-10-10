package com.mayor2k.comclient.interfaces

import com.mayor2k.comclient.models.LoginResponse
import retrofit2.Call
import retrofit2.http.*

public interface Api {
    @GET("/api/auth/")
    fun logIn(@Header("Authorization") authorization: String): Call<LoginResponse>
}