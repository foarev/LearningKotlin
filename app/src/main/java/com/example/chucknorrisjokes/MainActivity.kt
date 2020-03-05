package com.example.chucknorrisjokes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.*
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    val TAG: String = "MAIN"
    val cd = CompositeDisposable()
    val jokes: MutableList<Joke> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val llm = LinearLayoutManager(this)
        val ad = JokeAdapter()
        val jokeService: JokeApiService = JokeApiServiceFactory.factoryBuilder()

        loader.visibility = View.VISIBLE
        cd.add(jokeService
            .giveMeAJoke()
            .subscribeOn(Schedulers.io())
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {loader.visibility = View.GONE}
            .subscribeBy(
                onError = { e -> Log.wtf(TAG, e) },
                onSuccess = {
                    joke -> jokes.add(joke)
                    ad.jokes=jokes
                }
            )
        )

        my_recycler_view.layoutManager = llm
        my_recycler_view.adapter = ad

        addJokeButton.setOnClickListener {
            loader.visibility = View.VISIBLE
            cd.add(jokeService
                .giveMeAJoke()
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {loader.visibility = View.GONE}
                .subscribeBy(
                    onError = { e -> Log.wtf(TAG, e) },
                    onSuccess = {
                            joke -> jokes.add(joke)
                        ad.jokes=jokes
                    }
                )
            )
        }
    }
}