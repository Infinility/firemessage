package com.sourtime.www.firemessage

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class SignupActivity : AppCompatActivity() {

    val tag: String = "Signup"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnRegister.setOnClickListener {
            //Register new user
           registerNewUser()
        }


        tvLogin.setOnClickListener {
            //Go to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnSelectPhoto.setOnClickListener {
            //Show photo selector

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var photoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            photoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)

            val bitmapDrawable = BitmapDrawable(bitmap)
            btnSelectPhoto.setBackgroundDrawable(bitmapDrawable)
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

                    uploadImageToFirebase()
                }
                .addOnFailureListener {
                    Log.d(tag, "Error: ${it.message}")
                    Snackbar.make(findViewById(R.id.mainConstraintLayout),"Couldn't create user: ${it.message}",
                            Snackbar.LENGTH_LONG)
                            .show()
                }
    }

    private fun uploadImageToFirebase(){
        if (photoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(photoUri!!)
                .addOnSuccessListener {
                    Log.d(tag, "Successfully uploaded image: ${it.metadata.path}")
                }
    }
}
