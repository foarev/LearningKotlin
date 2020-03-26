package com.example.chucknorrisjokes

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    val JOKE_LIST_KEY:String = "JOKE_LIST_KEY"
    val dataListSerializer = Joke.serializer().list
    lateinit var ad:JokeAdapter
    @UnstableDefault
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val llm = LinearLayoutManager(this)
        val jokeService: JokeApiService = JokeApiServiceFactory.factoryBuilder()

        val onClickShare: (Joke) -> Unit = {
            Log.wtf(TAG, it.id)
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, it.value)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        val onClickStar: (Joke, Boolean) -> Unit = {joke: Joke, starred: Boolean ->
            if(starred){
                val sharedPrefs = SharedPrefs()
                if(sharedPrefs.getFavorites(this)!=null)
                    sharedPrefs.addFavorite(this, joke)
                else {
                    val favJokes: MutableList<Joke> = mutableListOf()
                    favJokes.add(joke)
                    sharedPrefs.saveFavorites(this, favJokes)
                }
            } else {
                SharedPrefs().removeFavorite(this, joke)
            }
        }

        val addJoke: () -> Unit = {
            swipe_refresh_layout.isRefreshing = true
            cd.add(jokeService
                .giveMeAJoke()
                .subscribeOn(Schedulers.io())
                .delay(50, TimeUnit.MILLISECONDS)
                .repeat(10)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {swipe_refresh_layout.isRefreshing = false}
                .subscribeBy(
                    onError = { e -> Log.wtf(TAG, e) },
                    onNext = {joke ->
                        ad.models.add(JokeView.Model(joke, SharedPrefs().getFavorites(this)?.contains(joke)!!, onClickShare, onClickStar))
                    },
                    onComplete = {
                        ad.notifyDataSetChanged()
                    }
                )
            )
        }
        ad = JokeAdapter(addJoke)

        val onItemMoved: (Int, Int) -> Unit = { fromPosition:Int, toPosition: Int ->
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(ad.models, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(ad.models, i, i - 1)
                }
            }
            ad.notifyItemMoved(fromPosition, toPosition)
        }
        val onJokeRemoved: (Int, Int) -> Unit = { position: Int, _: Int ->
            Log.wtf(TAG, "onJokeRemoved")
            ad.models.removeAt(position)
            ad.notifyItemRemoved(position)
        }
        JokeTouchHelper(onItemMoved, onJokeRemoved, swipe_refresh_layout).attachToRecyclerView(my_recycler_view)

        my_recycler_view.layoutManager = llm
        my_recycler_view.adapter = ad

        if (savedInstanceState != null) {
            parse(dataListSerializer, savedInstanceState.getString(JOKE_LIST_KEY)).forEach{
                ad.models.add(JokeView.Model(it, SharedPrefs().getFavorites(this)?.contains(it)!!, onClickShare, onClickStar))
            }
        } else {
            SharedPrefs().getFavorites(this)?.forEach {
                if (it != null) {
                    ad.models.add(JokeView.Model(it, SharedPrefs().getFavorites(this)?.contains(it)!!, onClickShare, onClickStar))
                }
            }
            addJoke()
        }
        swipe_refresh_layout.setOnRefreshListener {
            ad.models.removeAll(ad.models)
            SharedPrefs().getFavorites(this)?.forEach {
                if (it != null) {
                    ad.models.add(JokeView.Model(it, SharedPrefs().getFavorites(this)?.contains(it)!!, onClickShare, onClickStar))
                }
            }
            addJoke()
        }

    }

    @UnstableDefault
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            val jokes:MutableList<Joke> = mutableListOf()
            ad.models.forEach {
                jokes.add(it.joke)
            }
            val s = stringify(dataListSerializer, jokes)
            putString(JOKE_LIST_KEY, s)
        }
        super.onSaveInstanceState(outState)
    }
}