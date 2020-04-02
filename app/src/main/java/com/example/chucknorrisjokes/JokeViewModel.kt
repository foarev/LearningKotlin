package com.example.chucknorrisjokes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.activity_main.*
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json.Companion.parse
import kotlinx.serialization.json.Json.Companion.stringify
import kotlinx.serialization.list

/**
 * @param context, helpful for sharing joke
 * @param sharedPreferences, helpful for saving jokes
 *
 * @see androidx.lifecycle.ViewModel
 */
class JokesViewModel(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val TAG: String = "JokeViewModel"
    private val service: JokeApiService = JokeApiServiceFactory.factoryBuilder()
    private val composite: CompositeDisposable = CompositeDisposable()

    enum class LoadingStatus { LOADING, NOT_LOADING }

    /** Used as a "dynamic enum" to notify Adapter with correct action. */
    sealed class ListAction {
        data class ItemUpdatedAction(val position: Int) : ListAction()
        data class ItemInsertedAction(val position: Int) : ListAction()
        data class ItemRemovedAction(val position: Int) : ListAction()
        data class ItemMovedAction(val fromPosition: Int, val toPosition: Int) : ListAction()
        object DataSetChangedAction : ListAction()
    }

    /**
     * Private members of type MutableLiveData.
     * You can update a MutableLiveData value using setValue() (or postValue() if not main Thread).
     * Belong private because only the ViewModel should be able to update its liveData.
     *
     * @see androidx.lifecycle.MutableLiveData
     * @see androidx.lifecycle.LiveData#setValue()
     * @see androidx.lifecycle.LiveData#postValue()
     */
    private val _jokesLoadingStatus = MutableLiveData<LoadingStatus>()
    private val _jokesSetChangedAction = MutableLiveData<ListAction>()
    private val _jokes = MutableLiveData<List<Joke>>()


    /**
     * Public members of type LiveData.
     * This is what UI will observe and use to update views.
     * They are built with private MutableLiveData above.
     *
     * @see androidx.lifecycle.LiveData
     * @see androidx.lifecycle.Transformations
     */
    val jokesLoadingStatus: LiveData<LoadingStatus> = _jokesLoadingStatus
    val jokesSetChangedAction: LiveData<ListAction> = _jokesSetChangedAction
    val jokeModels: LiveData<List<JokeView.Model>> = Transformations.map(_jokes) {
        it.toJokesViewModel()
    }

    init {
        TODO("Restore saved joke, and fetch new others.")
    }

    fun onNewJokesRequest(jokeCount: Int = 10) {
        //swipe_refresh_layout.isRefreshing = true
        val jokes = mutableListOf<Joke>()
        jokes.addAll(_jokes.value!!)
        composite.add(service
            .giveMeAJoke()
            .subscribeOn(Schedulers.io())
            .delay(50, TimeUnit.MILLISECONDS)
            .repeat(jokeCount.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {/*swipe_refresh_layout.isRefreshing = false*/}
            .subscribeBy(
                onError = { e -> Log.wtf(TAG, e) },
                onNext = {joke ->
                    jokes.add(joke)
                    _jokesSetChangedAction.value = ListAction.ItemInsertedAction(jokes.lastIndex)
                },
                onComplete = {
                    _jokes.postValue(jokes)
                }
            )
        )
    }

    fun onJokeRemovedAt(position: Int) {
        val jokes = mutableListOf<Joke>()
        jokes.addAll(_jokes.value!!)
        jokes.removeAt(position)
        _jokes.postValue(jokes)
        _jokesSetChangedAction.value = ListAction.ItemRemovedAction(position)
    }

    fun onJokePositionChanged(previous: Int, target: Int) {
        val jokes = mutableListOf<Joke>()
        jokes.addAll(_jokes.value!!)
        if (previous < target) {
            for (i in previous until target) {
                Collections.swap(jokes, i, i + 1)
            }
        } else {
            for (i in previous downTo target + 1) {
                Collections.swap(jokes, i, i - 1)
            }
        }
        _jokes.postValue(jokes)
        _jokesSetChangedAction.value = ListAction.ItemMovedAction(previous, target)
    }

    private fun onJokeStared(id: String) {
        var i=0
        _jokes.value?.forEach {joke -> if(joke.id==id){
                val sharedPrefs = SharedPrefs()
                if(sharedPrefs.getFavorites(context)!=null)
                    sharedPrefs.addFavorite(context, joke)
                else {
                    val favJokes: MutableList<Joke> = mutableListOf()
                    favJokes.add(joke)
                    sharedPrefs.saveFavorites(context, favJokes)
                }
                _jokesSetChangedAction.value = ListAction.ItemUpdatedAction(i)
            } else i++
        }
    }

    private fun onJokeUnStared(id: String) {
        var i=0
        _jokes.value?.forEach {joke ->
            if(joke.id==id){
                SharedPrefs().removeFavorite(context, joke)
                _jokesSetChangedAction.value = ListAction.ItemUpdatedAction(i)
            } else i++
        }
    }

    private fun onJokeShared(id: String) {
        var i=0
        _jokes.value?.forEach {joke ->
            if(joke.id==id){
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, joke.value)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            } else i++
        }
    }

    private fun onSavedJokesRestored() {
        val jokes = mutableListOf<Joke>()
        jokes.addAll(_jokes.value!!)
        SharedPrefs().getFavorites(context)?.forEach {
            if (it != null) {
                jokes.add(it)
                _jokesSetChangedAction.value = ListAction.ItemInsertedAction(jokes.lastIndex)
            }
        }
        _jokes.value = jokes
    }

    override fun onCleared() {
        TODO("What to do here ? See method documentation.")
    }

    private fun List<Joke>.toJokesViewModel(): List<JokeView.Model> = map { joke ->
        TODO("Build a Model instance using $joke")
    }

    /** Convenient method to change an item position in a List */
    private inline fun <reified T> List<T>.moveItem(sourceIndex: Int, targetIndex: Int): List<T> =
        apply {
            if (sourceIndex <= targetIndex)
                Collections.rotate(subList(sourceIndex, targetIndex + 1), -1)
            else Collections.rotate(subList(targetIndex, sourceIndex + 1), 1)
        }

}