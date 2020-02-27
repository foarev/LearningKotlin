package com.example.chucknorrisjokes

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class JokeAdapter() : RecyclerView.Adapter<JokeAdapter.JokeViewHolder>(){
    val TAG:String = "JokeAdapter"
    class JokeViewHolder(val v: TextView) : RecyclerView.ViewHolder(v)

    var jokes:List<Joke> = listOf()
        set(value){
            field = value
            notifyDataSetChanged()
            Log.wtf(TAG,"Data changed")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeViewHolder {
        val t = LayoutInflater.from(parent.context).inflate(R.layout.joke_layout, parent, false) as TextView
        return JokeViewHolder(t)
    }

    override fun getItemCount(): Int {
        return jokes.count()
    }

    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.v.text = jokes[position].value
    }
}