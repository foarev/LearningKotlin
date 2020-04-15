package com.example.chucknorrisjokes

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.joke_layout.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT

class JokeView @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0) :
                                    ConstraintLayout(context, attrs, defStyleAttr)
{
    val TAG: String = "JokeView"
    data class Model(val joke: Joke, val stared: Boolean, var onClickShare: (String) -> Unit, var onClickStar: (String) -> Unit, var onClickUnstar: (String) -> Unit)
    init {
        View.inflate(context, R.layout.joke_layout, this)
        this.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
    fun setupView(model: Model){
        joke_text_view.text = model.joke.value
        button_star.setOnClickListener {
            if(model.stared){
                model.onClickUnstar(model.joke.id)
                disableStar()
            }
            else {
                model.onClickStar(model.joke.id)
                enableStar()
            }
        }
        if(model.stared){
            enableStar()
        }
        else{
            disableStar()
        }
        button_share.setOnClickListener { model.onClickShare(model.joke.id) }
    }
    fun enableStar(){
        button_star.setImageResource(R.drawable.ic_star_black_24dp)
    }
    fun disableStar(){
        button_star.setImageResource(R.drawable.ic_star_border_black_24dp)
    }
}