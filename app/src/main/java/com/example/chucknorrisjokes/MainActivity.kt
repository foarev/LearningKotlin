package com.example.chucknorrisjokes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.*
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json.Companion.parse
import kotlinx.serialization.json.Json.Companion.stringify
import kotlinx.serialization.list
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    val TAG: String = "MAIN"
    val cd = CompositeDisposable()
    val jokes: MutableList<Joke> = mutableListOf()
    val JOKE_LIST_KEY:String = "JOKE_LIST_KEY"
    val dataListSerializer = Joke.serializer().list
    lateinit var ad:JokeAdapter
    @UnstableDefault
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val llm = LinearLayoutManager(this)
        val jokeService: JokeApiService = JokeApiServiceFactory.factoryBuilder()

        val addJoke: () -> Unit = {loader.visibility = View.VISIBLE
            cd.add(jokeService
                .giveMeAJoke()
                .subscribeOn(Schedulers.io())
                .delay(50, TimeUnit.MILLISECONDS)
                .repeat(10)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {loader.visibility = View.GONE}
                .subscribeBy(
                    onError = { e -> Log.wtf(TAG, e) },
                    onNext = {joke -> jokes.add(joke)},
                    onComplete = {ad.jokes=jokes}
                )
            )
        }
        ad = JokeAdapter(addJoke)

        my_recycler_view.layoutManager = llm
        my_recycler_view.adapter = ad

        if (savedInstanceState != null) {
            parse(dataListSerializer, savedInstanceState?.getString(JOKE_LIST_KEY)).forEach{jokes.add(it)}
            ad.jokes = jokes
        } else {addJoke()}

    }

    @UnstableDefault
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            val s = stringify(dataListSerializer, jokes)
            putString(JOKE_LIST_KEY, s)
        }
        super.onSaveInstanceState(outState)
    }
}