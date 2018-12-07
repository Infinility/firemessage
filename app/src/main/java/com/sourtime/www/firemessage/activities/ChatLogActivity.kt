package com.sourtime.www.firemessage.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sourtime.www.firemessage.activities.NewMessageActivity.Companion.USER_KEY
import com.sourtime.www.firemessage.R
import com.sourtime.www.firemessage.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.item_chatlog_friends_message.view.*
import kotlinx.android.synthetic.main.item_chatlog_users_message.view.*

class ChatLogActivity : AppCompatActivity() {
    val TAG = "ChatLogActivity"
    var adapter = GroupAdapter<ViewHolder>()
    var friend: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        supportActionBar?.title = "Chat Log"

//        recyclerview_chatlog.adapter = adapter

        friend = intent.getParcelableExtra<User>(USER_KEY)

        supportActionBar?.title = friend?.username

        sendButton_chatlog.setOnClickListener {
            sendMessage()
        }

        listenForMessages()
    }

    private fun listenForMessages() {
        val userid = FirebaseAuth.getInstance().uid

        val db = FirebaseFirestore.getInstance().collection("user_messages/${userid}/${friend!!.uid}")
//        db.orderBy

        db.orderBy("datetime", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null){
                        Log.e(TAG, "error: $firebaseFirestoreException")
                        return@addSnapshotListener
                    }
                    adapter = GroupAdapter<ViewHolder>()
//                    adapter.add()

                    for (doc in querySnapshot!!){
                        if ((doc.get("fromid")== userid) && (doc.get("toid") == friend?.uid)){
                            val user = LatestMessagesActivity.currentUser
                                    adapter.add(ChatUserItem(doc.get("message").toString(), user))
                            Log.d(TAG, "from user: $userid")

                        }

                        else if((doc.get("fromid")== friend?.uid) && (doc.get("toid") == userid)){
                            adapter.add(ChatFriendItem(doc.get("message").toString(), friend))
                            Log.d(TAG, "from friend: ${friend?.uid}")

                        }
                        Log.d(TAG, "doc id: ${doc.id}")
                    }
                    recyclerview_chatlog.adapter = adapter

        }
    }


    private fun sendMessage() {
        val uid = FirebaseAuth.getInstance().uid ?: ""

        if (uid == "") return

        val message = HashMap<String, Any>()
        Log.d(TAG, "fromid: $uid")
        Log.d(TAG, "toid: ${friend?.uid}")
        Log.d(TAG, "message: ${editText_chatlog.text.toString()}")
        Log.d(TAG, "datetime: ${System.currentTimeMillis()}")

        message.put("fromid", uid)
        message.put("toid", friend!!.uid)
        message.put("message", editText_chatlog.text.toString())
        message.put("datetime", System.currentTimeMillis())

        FirebaseFirestore.getInstance().collection("user_messages/${uid}/${friend!!.uid}")
                .add(message as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + it.getId())
                }
                .addOnFailureListener {
                    Log.w(TAG, "Error adding document", it)
                }

        FirebaseFirestore.getInstance().collection("user_messages/${friend!!.uid}/${uid}")
                .add(message as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + it.getId())
                    editText_chatlog.setText("")
                    recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
                }
                .addOnFailureListener {
                    Log.w(TAG, "Error adding document", it)

                }

        val latestMsgForUser = HashMap<String, Any>()
        latestMsgForUser["${friend!!.uid}"] = message
        FirebaseFirestore.getInstance().collection("latest_messages").document("${uid}")
                .set(latestMsgForUser, SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

        val latestMsgForFriend = HashMap<String, Any>()
        latestMsgForFriend["${uid}"] = message
        FirebaseFirestore.getInstance().collection("latest_messages").document("${friend!!.uid}")
                .set(latestMsgForFriend, SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}


class ChatFriendItem(val msg: String, val user: User?): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_chatlog_friends_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chatlog_friends_message.text = msg

        val uri = user?.photourl ?: ""

        if (uri != ""){
            Picasso.get().load(uri).into(viewHolder.itemView.imageView_chatlog_friend)
        }
    }
}


class ChatUserItem(val msg: String, val user: User?): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_chatlog_users_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chatlog_user.text = msg

        val uri = user?.photourl ?: ""
        if (uri != ""){
            Picasso.get().load(uri).into(viewHolder.itemView.imageView_chatlog_user)
        }
    }
}