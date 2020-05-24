package com.me.o_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.me.o_chat.models.Message
import kotlinx.android.synthetic.main.card_message_admin.view.*
//import jp.wasabeef.picasso.transformations.CropCircleTransformation


interface MessageAdminListener {
    fun onMessageClick(message: Message)
}

// used in FirstActivity
// for messages from Organisers

class AdminAdapter constructor(var messages: ArrayList<Message>,
                                  private val listener: MessageAdminListener)
    : RecyclerView.Adapter<AdminAdapter.MainHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_message_admin,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val message = messages[holder.adapterPosition]
        holder.bind(message,listener)
    }

    override fun getItemCount(): Int = messages.size

    fun removeAt(position: Int) {
        messages.removeAt(position)
        notifyItemRemoved(position)
    }



    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message, listener: MessageAdminListener) {
            itemView.tag = message
            itemView.adminText.text = message.uText
            itemView.setOnClickListener { listener.onMessageClick(message) }

        }
    }




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

    //  Glide.with(itemView.context).load(message.uImage).into(itemView.userIcon)
}