package com.example.chucknorrisjokes

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.joke_layout.view.*

class JokeView @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0) :
                                    ConstraintLayout(context, attrs, defStyleAttr)
{
    val TAG: String = "JokeView"
    data class Model(val joke: Joke, val starred: Boolean, var onClickShare: (String) -> Unit, var onClickStar: (String) -> Unit, var onClickUnstar: (String) -> Unit)
    fun setupView(model: Model){
        View.inflate(context, R.layout.joke_layout, this)
        joke_text_view.text = model.joke.value
        button_star_border.setOnClickListener { model.onClickStar(model.joke.id)
            enableStar()
        }
        button_star_full.setOnClickListener { model.onClickUnstar(model.joke.id)
            disableStar()
        }
        if(model.starred)
            enableStar()
        else
            disableStar()
        button_share.setOnClickListener { model.onClickShare(model.joke.id) }
    }
    fun enableStar(){
        button_star_border.visibility = View.INVISIBLE
        button_star_full.visibility = View.VISIBLE
    }
    fun disableStar(){
        button_star_border.visibility = View.VISIBLE
        button_star_full.visibility = View.INVISIBLE
    }
}