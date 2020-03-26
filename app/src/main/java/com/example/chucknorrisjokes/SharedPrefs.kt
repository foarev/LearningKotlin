package com.example.chucknorrisjokes
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.*


class SharedPrefs {
    // This four methods are used for maintaining favorites.
    fun saveFavorites(
        context: Context,
        favorites: List<Joke?>?
    ) {
        val settings: SharedPreferences
        val editor: SharedPreferences.Editor
        settings = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = settings.edit()
        val gson = Gson()
        val jsonFavorites: String = gson.toJson(favorites)
        editor.putString(FAVORITES, jsonFavorites)
        editor.apply()
    }

    fun addFavorite(context: Context, Joke: Joke?) {
        var favorites: MutableList<Joke?>? = getFavorites(context)
        if (favorites == null) favorites = ArrayList<Joke?>()
        favorites.add(Joke)
        saveFavorites(context, favorites)
    }

    fun removeFavorite(context: Context, Joke: Joke?) {
        val favorites: ArrayList<Joke?>? = getFavorites(context)
        if (favorites != null) {
            favorites.remove(Joke)
            saveFavorites(context, favorites)
        }
    }

    fun getFavorites(context: Context): ArrayList<Joke?>? {
        val settings: SharedPreferences
        var favorites: List<Joke?>?
        settings = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        if (settings.contains(FAVORITES)) {
            val jsonFavorites =
                settings.getString(FAVORITES, null)
            val gson = Gson()
            val favoriteItems: Array<Joke> = gson.fromJson(
                jsonFavorites,
                Array<Joke>::class.java
            )
            favorites = Arrays.asList(*favoriteItems)
            favorites = ArrayList<Joke>(favorites)
        } else return null
        return favorites as ArrayList<Joke?>?
    }

    companion object {
        const val PREFS_NAME = "Joke_APP"
        const val FAVORITES = "Joke_Favorite"
    }
}