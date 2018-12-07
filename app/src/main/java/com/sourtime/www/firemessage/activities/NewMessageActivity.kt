package com.sourtime.www.firemessage.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.sourtime.www.firemessage.R
import com.sourtime.www.firemessage.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.item_user_row.view.*

class NewMessageActivity : AppCompatActivity() {

    val TAG: String = "NewMessageActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.setTitle("Select Contact")

        val adapter = GroupAdapter<ViewHolder>()

        recyclerViewNewMessage.adapter = adapter

        fetchUsers()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    val adapter = GroupAdapter<ViewHolder>()
                    for (document in result) {
                        Log.d(TAG, document.id + " => " + document.data)
                        val uid = document.data.getOrElse("uid"){""}
                        val username = document.data.getOrElse("username"){""}
                        val email = document.data.getOrElse("email"){""}
                        val photourl = document.data.getOrElse("photourl"){""}

                        val user = User(uid.toString(),username.toString(),email.toString(),photourl.toString())

                        Log.d(TAG, "userid: ${user.uid} useremail: ${user.email} username: ${user.username} userphoto: ${user.photourl}")

                        adapter.add(UserItem(user))
                    }
                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem

                        val intent = Intent(view.context, ChatLogActivity::class.java)
                        intent.putExtra(USER_KEY,userItem.user)

                        startActivity(intent)

                        finish()
                    }

                    recyclerViewNewMessage.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
    }
}

class UserItem(val user: User) : Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.item_user_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_username_new_message.text = user.username

        if (user.photourl != "") {
            Picasso.get().load(user.photourl).into(viewHolder.itemView.circleimageiew_profile_new_message)
        }
    }
}