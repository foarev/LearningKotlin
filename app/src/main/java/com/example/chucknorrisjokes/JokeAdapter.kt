package com.example.chucknorrisjokes

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.joke_layout.view.*


class JokeAdapter(onBottomReachedParam: () -> Unit, onClickShareParam: (Joke) -> Unit, onClickStarParam: (Joke) -> Unit) : RecyclerView.Adapter<JokeAdapter.JokeViewHolder>(){
    val TAG:String = "JokeAdapter"
    val onBottomReached: () -> Unit = onBottomReachedParam
    var onClickShare: (Joke) -> Unit = onClickShareParam
    var onClickStar: (Joke) -> Unit = onClickStarParam
    class JokeViewHolder(val v: JokeView) : RecyclerView.ViewHolder(v)

    var jokes:MutableList<Joke> = mutableListOf()
        set(value){
            field = value
            notifyDataSetChanged()
            Log.wtf(TAG,"Data changed")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeViewHolder {
        val v = JokeView(onClickShare, onClickStar, parent.context)
        return JokeViewHolder(v)
    }

    override fun getItemCount(): Int {
        return jokes.count()
    }

    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.v.setupView(JokeView.Model(jokes[position]))
        if (position >= jokes.count()-1) {
            onBottomReached()
        }
    }
}