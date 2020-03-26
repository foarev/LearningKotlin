package com.example.chucknorrisjokes

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class JokeAdapter(onBottomReachedParam: () -> Unit) : RecyclerView.Adapter<JokeAdapter.JokeViewHolder>(){
    val TAG:String = "JokeAdapter"
    val onBottomReached: () -> Unit = onBottomReachedParam

    class JokeViewHolder(val v: JokeView) : RecyclerView.ViewHolder(v)

    var models:MutableList<JokeView.Model> = mutableListOf()
        set(value){
            field = value
            notifyDataSetChanged()
            Log.wtf(TAG,"Data changed")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeViewHolder {
        val v = JokeView(parent.context)
        return JokeViewHolder(v)
    }

    override fun getItemCount(): Int {
        return models.count()
    }

    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.v.setupView(models[position])
        if (position >= models.count()-1) {
            onBottomReached()
        }
    }
}