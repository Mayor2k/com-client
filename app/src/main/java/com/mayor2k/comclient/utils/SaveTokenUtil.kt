package com.mayor2k.comclient.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.mayor2k.comclient.interfaces.Constants.Companion.ALIAS
import com.mayor2k.comclient.interfaces.Constants.Companion.ANDROID_KEY_STORE
import com.mayor2k.comclient.interfaces.Constants.Companion.TRANSFORMATION
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

class SaveTokenUtil {
    companion object{
        fun saveToken(context: Context, token: String, encryptedIv: String){
            val editor : SharedPreferences.Editor = context.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit()
            editor.putString("token", token)
            editor.apply()
            editor.putString("encryptedIv", encryptedIv)
            editor.apply()
        }

        fun getToken(context: Context) : String? {
            val sharedPreferences : SharedPreferences = context.getSharedPreferences("SP", Activity.MODE_PRIVATE)
            val encryptedToken = Base64.decode(sharedPreferences.getString("token", null), Base64.DEFAULT)
            val encryptedIv = Base64.decode(sharedPreferences.getString("encryptedIv", null), Base64.DEFAULT)

            val keyStore : KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val secretKey = keyStore.getKey(ALIAS, null) as? SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, encryptedIv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val tokenBytes = cipher.doFinal(encryptedToken)

            return String(tokenBytes, Charsets.UTF_8)
        }
    }
}