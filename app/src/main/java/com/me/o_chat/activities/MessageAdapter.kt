package com.me.o_chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.me.o_chat.models.Message
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.card_message.view.*
//import jp.wasabeef.picasso.transformations.CropCircleTransformation


interface MessageListener {
    fun onMessageClick(message: Message)
}

class MessageAdapter constructor(var messages: ArrayList<Message>,
                                  private val listener: MessageListener,uUser:User)
    : RecyclerView.Adapter<MessageAdapter.MainHolder>() {

    var cUser = uUser


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {

        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_message,
                parent,
                false
            ),cUser
        )

    }
        override fun onBindViewHolder(holder: MainHolder, position: Int) {
            val message = messages[holder.adapterPosition]
            holder.bind(message, listener)
        }

        override fun getItemCount(): Int = messages.size


        fun removeAt(position: Int) {
            messages.removeAt(position)
            notifyItemRemoved(position)
        }





    // moified to accept 2 Views on for MessageTo and another for MessageFrom
    // so we can have different cards depending on the its the current logged in user.

    class MainHolder constructor(itemView: View,mUser:User) : RecyclerView.ViewHolder(itemView) {
     //   val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
      val currentUser = mUser

        fun bind(message: Message, listener: MessageListener) {
            Log.d("message Adapter"," Message${message.uTo}")
            //

                itemView.tag = message
                //  itemView.userText.text = message.uText.toString()
                itemView.mText.text = message.uText.toString()
               if(currentUser.uType == "Admin" && message.uFrom == currentUser.uUid || currentUser.uType != "Admin" &&  message.uFrom != currentUser.uUid ) {
                   itemView.mIcon.setImageResource(R.mipmap.ic_message_in)
               } else{
                   itemView.mIcon.setImageResource(R.mipmap.ic_message_out)
               }

            //    itemView.mIcon.setImageResource(R.mipmap.ic_message_in)
          //  itemView.mIcon.setImageDrawable(R.drawable.common_full_open_on_phone)
                itemView.setOnClickListener { listener.onMessageClick(message) }
                // if(!reportAll)
                //    itemView.setOnClickListener { listener.onPlacemarkClick(placemark) }
//
                //          if(!placemark.profilepic.isEmpty()) {
                ///            Picasso.get().load(placemark.profilepic.toUri())
                //              //.resize(180, 180)
                //            .transform(CropCircleTransformation())
                //          .into(itemView.imageIcon)
                //}
                //    else
                //        itemView.imageIcon.setImageResource(R.mipmap.ic_launcher_homer_round)
                // }
          //  }

          //  Glide.with(itemView.context).load(message.uImage).into(itemView.userIcon)
        }
    }
}