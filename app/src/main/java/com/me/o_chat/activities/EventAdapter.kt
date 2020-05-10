package com.me.o_chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.me.o_chat.helpers.readImageFromPath
import com.me.o_chat.models.Event
import com.me.o_chat.models.Message
import com.me.o_chat.models.Station
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_event_create_p1.view.*
import kotlinx.android.synthetic.main.card_event.view.*
//import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_message_to.view.*
import kotlinx.android.synthetic.main.card_station.view.*

//import jp.wasabeef.picasso.transformations.CropCircleTransformation


interface EventListener {
    fun onEventClick(event: Event)
}

class EventAdapter constructor(var events: ArrayList<Event>,
                               private val listener: EventListener)
    : RecyclerView.Adapter<EventAdapter.MainHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_event,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val event = events[holder.adapterPosition]
        holder.bind(event,listener)
    }

    override fun getItemCount(): Int = events.size

    fun removeAt(position: Int) {
        events.removeAt(position)
        notifyItemRemoved(position)
    }



    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(event: Event, listener: EventListener) {
            itemView.tag = event
         //  itemView.userText.text = message.uText.toString()

            itemView.eDescription.text = event.eDescription.toString()

       //     Picasso.get().load(station.sImage.toUri())
       //                   //.resize(180, 180)
       //                // .transform(CropCircleTransformation())
       //               .into(itemView.stationIcon)

          //  itemView.stationIcon.setImageBitmap(readImageFromPath(itemView.context, station.sImage))
            itemView.setOnClickListener { listener.onEventClick(event) }
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
    }
}