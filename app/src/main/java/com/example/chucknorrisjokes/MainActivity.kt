package com.example.chucknorrisjokes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val TAG:String = "MAIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.wtf(TAG, Jokes.jokes.toString())
        val llm = LinearLayoutManager(this)
        val ad = JokeAdapter(Jokes)
        my_recycler_view.layoutManager = llm
        my_recycler_view.adapter = ad


    }
}