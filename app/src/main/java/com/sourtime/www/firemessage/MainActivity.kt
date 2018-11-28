package com.sourtime.www.firemessage

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val tag: String = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnRegister.setOnClickListener {
           registerNewUser()
        }


        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerNewUser(){
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        if (email.isEmpty()|| password.isEmpty()){
            Snackbar.make(findViewById(R.id.mainConstraintLayout),"Email or password empty", Snackbar.LENGTH_LONG)
                    .show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    Log.d(tag, "Successful login. User id = ${it.result?.user?.uid}")
                }
                .addOnFailureListener {
                    Log.d(tag, "Error: ${it.message}")
                    Snackbar.make(findViewById(R.id.mainConstraintLayout),"Couldn't create user: ${it.message}",
                            Snackbar.LENGTH_LONG)
                            .show()
                }
    }
}
