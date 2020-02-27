package com.example.chucknorrisjokes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.*
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*


class MainActivity : AppCompatActivity() {
    val TAG:String = "MAIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.wtf(TAG, Jokes.jokes.toString())
        val llm = LinearLayoutManager(this)
        val ad = JokeAdapter()
        ad.jokes = Jokes.jokes.map { Json(JsonConfiguration.Stable).parse(Joke.serializer(), it) }
        my_recycler_view.layoutManager = llm
        my_recycler_view.adapter = ad

        val joke_service:JokeApiService = JokeApiServiceFactory.factoryBuilder()
        joke_service
            .giveMeAJoke()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { e -> Log.wtf(TAG, e) },
                onSuccess = { joke -> Log.wtf(TAG, "$joke") }
            )

    }
}