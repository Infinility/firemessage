package com.sourtime.www.firemessage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sourtime.www.firemessage.NewMessageActivity.Companion.USER_KEY
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.item_chatlog_friends_message.view.*
import kotlinx.android.synthetic.main.item_chatlog_users_message.view.*

class ChatLogActivity : AppCompatActivity() {
    val TAG = "ChatLogActivity"
//    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        supportActionBar?.title = "Chat Log"

//        recyclerview_chatlog.adapter = adapter

        val user = intent.getParcelableExtra<User>(USER_KEY)

        supportActionBar?.title = user.username

        fetchChat()

        sendButton_chatlog.setOnClickListener {
            sendMessage()
        }

        listenForMessages()
    }

    private fun listenForMessages() {
        val friend = intent.getParcelableExtra<User>(USER_KEY)

        val db = FirebaseFirestore.getInstance().collection("messages")
//        db.orderBy("datetime", Query.Direction.ASCENDING)
        val userid = FirebaseAuth.getInstance().uid

        db.orderBy("datetime", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null){
                        Log.e(TAG, "error: $firebaseFirestoreException")
                        return@addSnapshotListener
                    }
                    val adapter = GroupAdapter<ViewHolder>()

                    for (doc in querySnapshot!!){
                        if ((doc.get("fromid")== userid) && (doc.get("toid") == friend.uid)){
                                    adapter.add(ChatUserItem(doc.get("message").toString()))
                            Log.d(TAG, "from user: $userid")

                        }

                        else if((doc.get("fromid")== friend.uid) && (doc.get("toid") == userid)){
                            adapter.add(ChatFriendItem(doc.get("message").toString()))
                            Log.d(TAG, "from friend: ${friend.uid}")

                        }
                        Log.d(TAG, "doc id: ${doc.id}")
                    }
                    recyclerview_chatlog.adapter = adapter

        }
    }


    private fun fetchChat() {
//        val adapter = GroupAdapter<ViewHolder>()
//
//
//        recyclerview_chatlog.adapter = adapter
    }

    private fun sendMessage() {
        val uid = FirebaseAuth.getInstance().uid ?: ""

        if (uid == "") return

        val user = intent.getParcelableExtra<User>(USER_KEY)

        val message = HashMap<String, Any>()
        Log.d(TAG, "fromid: $uid")
        Log.d(TAG, "toid: ${user.uid}")
        Log.d(TAG, "message: ${editText_chatlog.text.toString()}")
        Log.d(TAG, "datetime: ${System.currentTimeMillis()}")

        message.put("fromid", uid)
        message.put("toid", user.uid)
        message.put("message", editText_chatlog.text.toString())
        message.put("datetime", System.currentTimeMillis())

        FirebaseFirestore.getInstance().collection("messages")
                .add(message as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + it.getId())
                    editText_chatlog.setText("")
                }
                .addOnFailureListener {
                    Log.w(TAG, "Error adding document", it)

                }
    }
}


class ChatFriendItem(val msg: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_chatlog_friends_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chatlog_friends_message.text = msg
    }
}


class ChatUserItem(val msg: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_chatlog_users_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chatlog_user.text = msg
    }
}