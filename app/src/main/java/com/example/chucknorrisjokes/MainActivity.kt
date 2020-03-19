package com.example.chucknorrisjokes

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json.Companion.parse
import kotlinx.serialization.json.Json.Companion.stringify
import kotlinx.serialization.list
import java.util.*
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
        val onClickShare: (Joke) -> Unit = {
            Log.wtf(TAG, it.id)
        }
        val onClickStar: (Joke) -> Unit = {
            Log.wtf(TAG, it.id)
        }
        ad = JokeAdapter(addJoke, onClickShare, onClickStar)


        val onItemMoved: (Int, Int) -> Unit = { fromPosition:Int, toPosition: Int ->
            Log.wtf(TAG, "onItemMoved")
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(ad.jokes, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(ad.jokes, i, i - 1)
                }
            }
            ad.notifyItemMoved(fromPosition, toPosition)
        }
        val onJokeRemoved: (Int, Int) -> Unit = { position: Int, swipeDir: Int ->
            Log.wtf(TAG, "onJokeRemoved")
            ad.jokes.removeAt(position)
            ad.notifyItemRemoved(position)
        }
        JokeTouchHelper(onItemMoved, onJokeRemoved).attachToRecyclerView(my_recycler_view)

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