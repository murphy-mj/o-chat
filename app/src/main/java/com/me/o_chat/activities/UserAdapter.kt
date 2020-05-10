package com.me.o_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.me.o_chat.models.User
import com.me.o_chat.R
import com.squareup.picasso.Picasso
//import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.card_user.view.*


interface UserListener {
    fun onUserClick(user: User)
}

class UserAdapter constructor(var users: ArrayList<User>,
                                  private val listener: UserListener)
    : RecyclerView.Adapter<UserAdapter.MainHolder>() {



  //  var picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_user,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val user = users[holder.adapterPosition]
        holder.bind(user,listener)
    }

    override fun getItemCount(): Int = users.size

    fun removeAt(position: Int) {
        users.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(user: User, listener: UserListener) {
            itemView.tag = user
            itemView.userMessage.text = user.uName.toString()
            itemView.setOnClickListener { listener.onUserClick(user) }
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
            Glide.with(itemView.context).load(user.uImage).into(itemView.userIcon)
        }
    }
}