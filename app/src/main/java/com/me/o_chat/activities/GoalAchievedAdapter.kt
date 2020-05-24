package com.me.o_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.me.o_chat.models.User
import com.me.o_chat.models.Result
import com.me.o_chat.R
import com.me.o_chat.models.GoalAchObj
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_goal.view.*
import kotlinx.android.synthetic.main.card_leaderboard.view.*
//import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.card_user.view.*


interface GoalListener {
    fun onGoalClick(goal: Result)
}

class GoalAchievedAdapter constructor(var goals: ArrayList<Result>,
                                  private val listener: GoalListener)
    : RecyclerView.Adapter<GoalAchievedAdapter.MainHolder>() {



  //  var picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_leaderboard,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val goal = goals[holder.adapterPosition]
        holder.bind(goal,listener)
    }

    override fun getItemCount(): Int = goals.size

    fun removeAt(position: Int) {
        goals.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(goal: Result, listener: GoalListener) {
          //  itemView.tag = goal
            itemView.group_name.text = "Team Name :${goal.rName}"
            itemView.no_achieved.text = "Number of Stations completed : ${goal.rNumberAch}"
            itemView.time_taken.text = "Time taken : ${goal.rTimeTaken}"
           // itemView.setOnClickListener { listener.onGoalClick(goal) }
        }
    }
}