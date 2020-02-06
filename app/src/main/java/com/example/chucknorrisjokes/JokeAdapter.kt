package com.example.chucknorrisjokes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class JokeAdapter(var j:Jokes) : RecyclerView.Adapter<JokeAdapter.JokeViewHolder>(){
    class JokeViewHolder(val v: TextView) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeViewHolder {
        val t = LayoutInflater.from(parent.context).inflate(R.layout.joke_layout, parent, false) as TextView
        return JokeViewHolder(t)
    }

    override fun getItemCount(): Int {
        return Jokes.jokes.count()
    }

    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.v.text = Jokes.jokes[position]
    }
}