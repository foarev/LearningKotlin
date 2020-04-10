package com.example.chucknorrisjokes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chucknorrisjokes.JokesViewModel.LoadingStatus
import com.example.chucknorrisjokes.JokesViewModel.ListAction
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.joke_layout.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val SHARED_PREFS = "SHARED_PREFS"
    }
    /**
     * Our ViewModel instance, built with our Factory
     *
     * @see androidx.activity.viewModels
     */
    private val viewModel: JokesViewModel by viewModels {
        JokesViewModelFactory(
            this,
            getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        )
    }

    private val jokeAdapter: JokeAdapter = JokeAdapter{viewModel.onNewJokesRequest()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        JokeTouchHelper(
            { fromPosition, toPosition -> viewModel.onJokePositionChanged(fromPosition, toPosition) },
            { position -> viewModel.onJokeRemovedAt(position) },
            swipe_refresh_layout
        ).attachToRecyclerView(my_recycler_view)

        swipe_refresh_layout.setOnRefreshListener { viewModel.onJokesReset() }

        my_recycler_view.layoutManager = LinearLayoutManager(this)
        my_recycler_view.adapter = jokeAdapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.jokeModels.observe(
            this,
            Observer { jokes: List<JokeView.Model> ->
                jokeAdapter.models = jokes
            })

        viewModel.jokesSetChangedAction.observe(
            this,
            Observer { listAction: ListAction ->
                when(listAction) {
                    is ListAction.ItemUpdatedAction ->
                        jokeAdapter.notifyItemChanged(listAction.position)
                    is ListAction.ItemRemovedAction->
                        jokeAdapter.notifyItemRemoved(listAction.position)
                    is ListAction.ItemInsertedAction->
                        jokeAdapter.notifyItemInserted(listAction.position)
                    is ListAction.ItemMovedAction->
                        jokeAdapter.notifyItemMoved(listAction.fromPosition, listAction.toPosition)
                    is ListAction.DataSetChangedAction->
                        jokeAdapter.notifyDataSetChanged()
                }
            })

        viewModel.jokesLoadingStatus.observe(
            this,
            Observer { loadingStatus: LoadingStatus ->
                swipe_refresh_layout.isRefreshing = loadingStatus == LoadingStatus.LOADING
            })
    }


    /**
     * Convenient class used to build the instance of our JokeViewModel,
     * passing some params to its constructor.
     *
     * @see androidx.lifecycle.ViewModelProvider
     */
    private class JokesViewModelFactory(
        private val context: Context,
        private val sharedPrefs: SharedPreferences
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            JokesViewModel(context, sharedPrefs) as T
    }

}