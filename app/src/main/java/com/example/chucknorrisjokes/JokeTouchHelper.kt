package com.example.chucknorrisjokes

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class JokeTouchHelper(
    private val onItemMoved: (Int, Int) -> Unit ,
    private val onJokeRemoved: (Int, Int) -> Unit,
    private val swiperefreshLayout:SwipeRefreshLayout
) : ItemTouchHelper(
    object : ItemTouchHelper.SimpleCallback(
        UP or DOWN,
        LEFT or RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder
        ): Boolean {
            onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, swipeDir: Int) =
            onJokeRemoved(viewHolder.adapterPosition, swipeDir)

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            swiperefreshLayout.isEnabled = actionState != ACTION_STATE_DRAG
        }
    }

)