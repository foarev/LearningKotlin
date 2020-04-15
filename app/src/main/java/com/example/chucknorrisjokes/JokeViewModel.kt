package com.example.chucknorrisjokes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.serialization.list
import kotlinx.serialization.json.Json.Companion.parse
import kotlinx.serialization.json.Json.Companion.stringify
import kotlinx.serialization.json.JsonConfiguration

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
    val JOKE_LIST_KEY:String = "JOKE_LIST_KEY"
    val dataListSerializer = Joke.serializer().list
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
    private val _stared = MutableLiveData<List<Boolean>>()


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
        onSavedJokesRestored()
        onNewJokesRequest()
    }

    fun onNewJokesRequest(jokeCount: Int = 10) {
        _jokesLoadingStatus.value=LoadingStatus.LOADING
        val jokes:MutableList<Joke> = mutableListOf()
        val stared:MutableList<Boolean> = mutableListOf()
        if(!_jokes.value.isNullOrEmpty() && !_stared.value.isNullOrEmpty()) {
            jokes.addAll(_jokes.value!!)
            stared.addAll(_stared.value!!)
        }
        composite.add(service
            .giveMeAJoke()
            .subscribeOn(Schedulers.io())
            .delay(50, TimeUnit.MILLISECONDS)
            .repeat(jokeCount.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {_jokesLoadingStatus.value=LoadingStatus.NOT_LOADING}
            .subscribeBy(
                onError = { e -> Log.wtf(TAG, e) },
                onNext = {joke ->
                    jokes.add(joke)
                    stared.add(false)
                },
                onComplete = {
                    _jokes.value = jokes
                    _stared.value = stared
                    _jokesSetChangedAction.value = ListAction.DataSetChangedAction
                }
            )
        )
    }

    fun onJokeRemovedAt(position: Int) {
        val jokes:MutableList<Joke> = mutableListOf()
        val stared:MutableList<Boolean> = mutableListOf()
        jokes.addAll(_jokes.value!!)
        stared.addAll(_stared.value!!)
        onJokeUnStared(jokes[position].id)
        jokes.removeAt(position)
        stared.removeAt(position)
        _jokes.value = jokes
        _stared.value = stared
        _jokesSetChangedAction.value = ListAction.ItemRemovedAction(position)
    }

    fun onJokesReset() {
        val jokes:MutableList<Joke> = mutableListOf()
        val stared:MutableList<Boolean> = mutableListOf()
        _jokes.value = jokes
        _stared.value = stared
        onSavedJokesRestored()
        onNewJokesRequest()
        _jokesSetChangedAction.value = ListAction.DataSetChangedAction
    }

    fun onJokePositionChanged(previous: Int, target: Int) {
        _jokes.value = _jokes.value!!.moveItem(previous, target)
        _stared.value = _stared.value!!.moveItem(previous, target)
        _jokesSetChangedAction.value = ListAction.ItemMovedAction(previous, target)
    }

    private fun onJokeStared(id: String) {
        val stared = mutableListOf<Boolean>()
        stared.addAll(_stared.value!!)
        _jokes.value?.forEachIndexed {index, joke ->
            if(joke.id==id){
                val sharedPrefs = SharedPrefs()
                if(sharedPrefs.getFavorites(context)!=null)
                    sharedPrefs.addFavorite(context, joke)
                else {
                    val favJokes: MutableList<Joke> = mutableListOf()
                    favJokes.add(joke)
                    sharedPrefs.saveFavorites(context, favJokes)
                }
                stared[index] = true
                _stared.value = stared
                _jokes.value = _jokes.value
                _jokesSetChangedAction.value = ListAction.ItemUpdatedAction(index)
            }
        }
    }

    private fun onJokeUnStared(id: String) {
        val stared = mutableListOf<Boolean>()
        stared.addAll(_stared.value!!)
        _jokes.value?.forEachIndexed {index, joke ->
            if(joke.id==id){
                SharedPrefs().removeFavorite(context, joke)
                stared[index] = false
                _stared.value = stared
                _jokes.value = _jokes.value
                _jokesSetChangedAction.value = ListAction.ItemUpdatedAction(index)
            }
        }
    }

    private fun onJokeShared(id: String) {
        _jokes.value?.forEach {joke ->
            if(joke.id==id){
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, joke.value)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        }
    }

    private fun onSavedJokesRestored() {
        val jokes = mutableListOf<Joke>()
        val stared = mutableListOf<Boolean>()
        SharedPrefs().getFavorites(context)?.forEach {joke ->
            if (joke != null) {
                jokes.add(joke)
                stared.add(true)
                _jokes.value = jokes
                _stared.value = stared
                _jokesSetChangedAction.value = ListAction.DataSetChangedAction
            }
        }
    }

    override fun onCleared() {
        composite.dispose()
        super.onCleared()
    }

    private fun List<Joke>.toJokesViewModel(): List<JokeView.Model> = mapIndexed { index, joke ->
        var s=false
        if(_jokes.value!=null && _stared.value!=null) {
            val stared = mutableListOf<Boolean>()
            stared.addAll(_stared.value!!)
            _jokes.value?.forEach {j ->
                if(j.id==joke.id && stared.size>index){
                    s = stared[index]
                }
            }
        }
        JokeView.Model(joke, s, {id -> onJokeShared(id)}, {id -> onJokeStared(id)}, {id -> onJokeUnStared(id)})
    }

    /** Convenient method to change an item position in a List */
    private inline fun <reified T> List<T>.moveItem(sourceIndex: Int, targetIndex: Int): List<T> =
        apply {
            if (sourceIndex <= targetIndex)
                Collections.rotate(subList(sourceIndex, targetIndex + 1), -1)
            else Collections.rotate(subList(targetIndex, sourceIndex + 1), 1)
        }

}