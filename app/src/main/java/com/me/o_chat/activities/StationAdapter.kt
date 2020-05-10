package com.me.o_chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.me.o_chat.helpers.readImageFromPath
import com.me.o_chat.models.Message
import com.me.o_chat.models.Station
import com.squareup.picasso.Picasso
//import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_message_to.view.*
import kotlinx.android.synthetic.main.card_station.view.*

//import jp.wasabeef.picasso.transformations.CropCircleTransformation


interface StationListener {
    fun onStationClick(station: Station)
}

class StationAdapter constructor(var stations: ArrayList<Station>,
                                 private val listener: StationListener)
    : RecyclerView.Adapter<StationAdapter.MainHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_station,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val station = stations[holder.adapterPosition]
        holder.bind(station!!,listener)
    }

    override fun getItemCount(): Int = stations.size

    fun removeAt(position: Int) {
        stations.removeAt(position)
        notifyItemRemoved(position)
    }



    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(station: Station, listener: StationListener) {
            itemView.tag = station
         //  itemView.userText.text = message.uText.toString()
            itemView.stationDescription.text = station.sDescription.toString()


            Picasso.get().load(station.sImage.toUri())
                          //.resize(180, 180)
                       // .transform(CropCircleTransformation())
                      .into(itemView.stationIcon)

          //  itemView.stationIcon.setImageBitmap(readImageFromPath(itemView.context, station.sImage))
            itemView.setOnClickListener { listener.onStationClick(station) }
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