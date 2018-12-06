package com.sourtime.www.firemessage

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    val TAG = "LatestMessagesActivity"
    var adapter = GroupAdapter<ViewHolder>()

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

//        setupDummyRose()
        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserLoggedIn()

    }

    private fun listenForLatestMessages() {
        val uid = FirebaseAuth.getInstance().uid
        val dbRef = FirebaseFirestore.getInstance().collection("latest_messages").document("$uid")
                .collection("")

//        dbRef.get()
//                .addOnSuccessListener { document ->
//                    if (document != null) {
//                        Log.d(TAG, "DocumentSnapshot data: " + document.data)
//                        document.reference.
//                    } else {
//                        Log.d(TAG, "No such document")
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.d(TAG, "get failed with ", exception)
//                }
//
//        val dbRef = FirebaseFirestore.getInstance().collection("latest_messages").whereEqualTo("id","$uid")
//                .whereEqualTo()

//        dbRef.collection("latest_messages/${uid}")
//                .get()
//                .addOnSuccessListener {
//                    for (doc in it){
//                        Log.d(TAG, doc.id + " => " + doc.data)
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.d(TAG, "Error getting documents: ", exception)
//                }


//        db.orderBy("datetime", Query.Direction.DESCENDING)
//                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                    if (firebaseFirestoreException != null){
//                        Log.e(TAG, "error: $firebaseFirestoreException")
//                        return@addSnapshotListener
//                    }
//                    adapter = GroupAdapter<ViewHolder>()
////                    adapter.add()
//
//                    for (doc in querySnapshot!!){
//
//                        Log.d(TAG, "doc id: ${doc.id}")
//                    }
//                }
    }

    private fun setupDummyRose() {

    }

    //*
    //
    // TRY TO USE CHATROOM APPROACH INSTEAD
    // https://firebase.google.com/docs/firestore/manage-data/structure-data
    //
    // *?
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener {
                    for (document in it) {
                        Log.d(TAG, "${document.get("username")}:  ${document.get("email")} uid: ${document.get("uid")}")
                        currentUser = document.toObject(User::class.java)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

    }

    private fun verifyUserLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, SignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messages_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_new_msg -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                verifyUserLoggedIn()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class LatestMessageRow: Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.item_latest_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

}
