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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
//import kotlin.collections.HashMap

class SignupActivity : AppCompatActivity() {

    val TAG: String = "Signup"
    var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRegister.setOnClickListener {
            //Register new user
            createNewUser()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            photoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btnSelectPhoto.setBackgroundDrawable(bitmapDrawable)

            circleViewPhoto.setImageBitmap(bitmap)
            btnSelectPhoto.alpha = 0f
        }
    }

    private fun createNewUser(){
        val username = etUsername.text.toString()
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

                    Log.d(TAG, "Successful login. User id = ${it.result?.user?.uid}")

                    saveUserToFirebase(username, email)
                }
                .addOnFailureListener {
                    Log.d(TAG, "Error: ${it.message}")
                    Snackbar.make(findViewById(R.id.mainConstraintLayout),"Couldn't create user: ${it.message}",
                            Snackbar.LENGTH_LONG)
                            .show()
                }
    }

    private fun saveUserToFirebase(username: String, email: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""

        if (photoUri == null) {
            val user = HashMap<String, String>()
            user.put("uid", uid)
            user.put("username", username)
            user.put("email", email)

            FirebaseFirestore.getInstance().collection("users")
                    .add(user as Map<String, Any>)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + it.getId())
                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error adding document", it)
                    }
        }else{
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            var photoUrl: String = ""

            ref.putFile(photoUri!!)
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                        ref.downloadUrl.addOnSuccessListener {
                            photoUrl = it.toString()
                            Log.d(TAG, "Download URL: $it")
                            val user = HashMap<String, String>()
                            user.put("uid", uid)
                            user.put("username", username)
                            user.put("email", email)
                            user.put("photourl", photoUrl)

                            FirebaseFirestore.getInstance().collection("users")
                                    .add(user as Map<String, Any>)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + it.getId())
                                    }
                                    .addOnFailureListener {
                                        Log.w(TAG, "Error adding document", it)
                                    }
                        }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Successfully uploaded image: ${it.message}")
                    }
        }
    }
}
