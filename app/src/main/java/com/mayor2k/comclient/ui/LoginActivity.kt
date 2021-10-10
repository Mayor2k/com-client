package com.mayor2k.comclient.ui

import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mayor2k.comclient.R
import com.mayor2k.comclient.interfaces.Api
import com.mayor2k.comclient.interfaces.Constants.Companion.ALIAS
import com.mayor2k.comclient.interfaces.Constants.Companion.ANDROID_KEY_STORE
import com.mayor2k.comclient.interfaces.Constants.Companion.TAG
import com.mayor2k.comclient.interfaces.Constants.Companion.TRANSFORMATION
import com.mayor2k.comclient.models.LoginResponse
import com.mayor2k.comclient.utils.SaveTokenUtil.Companion.getToken
import com.mayor2k.comclient.utils.SaveTokenUtil.Companion.saveToken
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class LoginActivity : AppCompatActivity() {

    private lateinit var userField: EditText
    private lateinit var passwordField : EditText
    private lateinit var logInButton: Button
    private lateinit var errorTextView: TextView
    private val MIN_PASSWORD_LENGTH = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userField = findViewById(R.id.username)
        passwordField = findViewById(R.id.password)
        errorTextView = findViewById(R.id.error)
        logInButton = findViewById(R.id.login)
        passwordField.afterTextChanged {
            logInButton.isEnabled = passwordField.length() > MIN_PASSWORD_LENGTH
        }

        logInButton.setOnClickListener {
            authentication()
        }

    }

    private fun authentication(){
        val username = userField.text.toString()
        val password = passwordField.text.toString()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://mayor2k.pythonanywhere.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(Api::class.java)

        val authPayload = "$username:$password"
        val data = authPayload.toByteArray()
        val base64 = Base64.encodeToString(data, Base64.NO_WRAP)

        val call = service.logIn("Basic $base64".trim())

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.code() == 200) {
                    val loginResponse = response.body()!!
                    Log.i(TAG, loginResponse.token!!)

                    val keyGenerator: KeyGenerator = KeyGenerator
                            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)

                    keyGenerator.init(KeyGenParameterSpec.Builder(ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build())

                    val secretKey: SecretKey = keyGenerator.generateKey()
                    val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

                    val encryptedIv = cipher.iv
                    val tokenBytes = loginResponse.token!!.toByteArray(Charsets.UTF_8)
                    val encryptedTokenBytes = cipher.doFinal(tokenBytes)
                    val encryptedToken = Base64.encodeToString(encryptedTokenBytes, Base64.DEFAULT)

                    saveToken(this@LoginActivity, encryptedToken, Base64.encodeToString(encryptedIv, Base64.DEFAULT))
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()

                } else if (response.code() == 401){
                    val jsonError = JSONObject(response.errorBody()!!.string())
                    errorTextView.visibility = VISIBLE
                    errorTextView.text = jsonError.getString("detail")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.message?.let { Log.i("responses", it) }
            }
        })
    }
}

private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}