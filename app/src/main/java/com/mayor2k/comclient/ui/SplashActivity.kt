package com.mayor2k.comclient.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences : SharedPreferences = this.getSharedPreferences("SP", Activity.MODE_PRIVATE)

        if (sharedPreferences.contains("token")){
            startActivity(Intent(this, DashboardActivity::class.java))
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}