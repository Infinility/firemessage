package com.sourtime.www.firemessage

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {

    val tag: String = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            login()
        }

        tvBackToSignup.setOnClickListener {
            finish()
        }

    }

    private fun login(){
        val email = etLoginEmail.text.toString()
        val password = etLoginPassword.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    Log.d(tag, "Login successful")
                    val intent = Intent(this,LatestMessagesActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d(tag, "Login failed")
                }
    }

}
