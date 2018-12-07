package com.sourtime.www.firemessage.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sourtime.www.firemessage.R
import com.sourtime.www.firemessage.models.Message
import com.sourtime.www.firemessage.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.item_latest_message.view.*

class LatestMessagesActivity : AppCompatActivity() {

    val TAG = "LatestMessagesActivity"
    var adapter = GroupAdapter<ViewHolder>()

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserLoggedIn()
    }

    private fun listenForLatestMessages() {
        val uid = FirebaseAuth.getInstance().uid

        val dbRef = FirebaseFirestore.getInstance().collection("latest_messages")
                .document("$uid")

        dbRef.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.w(TAG, "Listen failed.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val latestmessages = documentSnapshot.data

                if (latestmessages == null) return@addSnapshotListener

                adapter = GroupAdapter<ViewHolder>()
                for((key,value) in latestmessages) {
                    val mapmsg = value as HashMap<String, Any>

                    val message = Message(mapmsg["fromid"].toString(), mapmsg["toid"].toString(), mapmsg["message"].toString(), mapmsg["timestamp"] as Long?)

                    var userid: String
                    if (mapmsg["fromid"] == uid){
                        userid = mapmsg["toid"].toString()
                        Log.d(TAG, "latest is current user: $userid")
                    }else{
                        userid = mapmsg["fromid"].toString()
                        Log.d(TAG, "latest is friend: $userid")
                    }

                    updateAdapter(userid, message)
                }
            }
            else {
                Log.d(TAG, "No such document")
            }
        }
    }

    private fun updateAdapter(userId: String, message: Message){
        var user: User = User()
        val ref = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid",userId)
        ref.get()
                .addOnSuccessListener {
                    for (document in it) {
                        Log.d(TAG, document.id + " => " + document.data)
                        val uid = document.data.getOrElse("uid"){""}
                        val username = document.data.getOrElse("username"){""}
                        val email = document.data.getOrElse("email"){""}
                        val photourl = document.data.getOrElse("photourl"){""}


                        user = User(uid.toString(),username.toString(),email.toString(),photourl.toString())
                        Log.d(TAG, "user's id: ${user.uid}")

                        adapter.add(LatestMessageRow(user, message))

                        recyclerView_latest_messages.adapter = adapter
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to get user: ${it}")
                }
    }


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

class LatestMessageRow(val user: User, val msg: Message): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.item_latest_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_latest_message_username.text = user.username
        viewHolder.itemView.tv_latest_messages_message.text = msg.message

        if (user.photourl != ""){
            Picasso.get().load(user.photourl).into(viewHolder.itemView.imageView_latest_messages_profile)
        }
    }

}
