package com.example.chucknorrisjokes

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.joke_layout.view.*

class JokeView @JvmOverloads constructor(onClickShareParam: (Joke) -> Unit,
                                         onClickStarParam: (Joke, Boolean) -> Unit,
                                         context: Context,
                                         attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0) :
                                    ConstraintLayout(context, attrs, defStyleAttr)
{
    val TAG: String = "JokeView"
    var onClickShare: (Joke) -> Unit = onClickShareParam
    var onClickStar: (Joke, Boolean) -> Unit = onClickStarParam
    init {
        View.inflate(context, R.layout.joke_layout, this)
    }
    data class Model(val joke: Joke, val starred: Boolean)
    fun setupView(model: Model){
        joke_text_view.text = model.joke.value
        button_star_border.setOnClickListener { onClickStar(model.joke, true)
            enableStar()
        }
        button_star_full.setOnClickListener { onClickStar(model.joke, false)
            disableStar()
        }
        if(model.starred)
            enableStar()
        else
            disableStar()
        button_share.setOnClickListener { onClickShare(model.joke) }
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