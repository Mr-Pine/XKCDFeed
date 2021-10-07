package de.mrpine.xkcdfeed

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.DateFormat

class MainViewModel(private val userDataStore: DataStore<Preferences>) : ViewModel() {
    private val TAG = "MainViewModel"
    var latestComicsList = mutableStateListOf<XKCDComic>()
    var latestImagesLoadedMap = mutableStateMapOf<Int, Boolean>()

    var favoriteComicsList = mutableStateListOf<XKCDComic>()
    var favoriteImagesLoadedMap = mutableStateMapOf<Int, Boolean>()

    private val FAVORITE_LIST = stringPreferencesKey("favorite_list")
    val favoriteListFlow: Flow<List<Int>> = userDataStore.data.map { preferences ->
        // No type safety.
        val stringList = preferences[FAVORITE_LIST] ?: "[]"
        return@map generateListFromJSON(stringList)
    }

    fun setFavoriteList(favoriteList: List<Int>) {
        viewModelScope.launch {
            userDataStore.edit { mutablePreferences ->
                mutablePreferences[FAVORITE_LIST] =
                    JSONArray(favoriteList.toTypedArray()).toString()
            }
        }
    }

    fun addFavorite(xkcdComic: XKCDComic, imageLoaded: Boolean = true){
        viewModelScope.launch {
            addToFavoriteList(xkcdComic.id)
            addToFavoriteComicList(xkcdComic, imageLoaded)
        }
    }

    fun removeFavorite(xkcdComic: XKCDComic){
        viewModelScope.launch {
            removeFromFavoriteList(xkcdComic.id)
            removeFromFavoriteComicList(xkcdComic)
        }
    }

    private suspend fun removeFromFavoriteList(id: Int) {
        userDataStore.edit { mutablePreferences ->
            val stringList = mutablePreferences[FAVORITE_LIST] ?: "[]"
            mutablePreferences[FAVORITE_LIST] =
                JSONArray(generateListFromJSON(stringList) { value, _ -> value != id }.toTypedArray()).toString()
        }
    }

    private suspend fun addToFavoriteList(id: Int) {
        userDataStore.edit { mutablePreferences ->
            val stringList = mutablePreferences[FAVORITE_LIST] ?: "[]"
            val mutableList = generateListFromJSON(stringList)
            mutableList.add(id)
            mutablePreferences[FAVORITE_LIST] =
                JSONArray(mutableList.toTypedArray()).toString()
        }
    }

    private fun generateListFromJSON(
        listString: String,
        condition: (Int, MutableList<Int>) -> Boolean = { value, list ->
            !list.contains(value)
        }
    ): MutableList<Int> {
        val jsonArray = JSONArray(listString)
        val mutableList = mutableListOf<Int>()
        for (i in 0 until jsonArray.length()) {
            if (condition(jsonArray.getInt(i), mutableList)) {
                mutableList.add(jsonArray.getInt(i))
            }
        }
        return mutableList
    }

    lateinit var dateFormat: DateFormat

    private fun addToLatestComicList(item: XKCDComic) {
        latestComicsList.add(item)
        latestComicsList.sortByDescending { it.id }
    }

    private fun addToFavoriteComicList(item: XKCDComic, imageLoaded: Boolean? = null) {
        favoriteComicsList.add(item)
        if(imageLoaded != null) favoriteImagesLoadedMap[item.id] = imageLoaded
        favoriteComicsList.sortByDescending { it.id }
    }

    private fun removeFromFavoriteComicList(item: XKCDComic) {
        favoriteComicsList.remove(favoriteComicsList.find { it.id == item.id })
        favoriteImagesLoadedMap.remove(item.id)
        favoriteComicsList.sortByDescending { it.id }
    }

    fun addComic(number: Int, context: Context, to: Tab) {
        viewModelScope.launch {
            addComicSync(number, context, to)
        }
    }

    private fun addComicSync(number: Int, context: Context, to: Tab) {
        (if (to == Tab.LATEST) latestImagesLoadedMap else favoriteImagesLoadedMap)[number] = false
        XKCDComic.getComic(
            number = number,
            coroutineScope = viewModelScope,
            context = context,
            onImageLoaded = {
                (if (to == Tab.LATEST) latestImagesLoadedMap else favoriteImagesLoadedMap)[number] =
                    true
            }) {
            if (to == Tab.LATEST) {
                addToLatestComicList(it)
            } else {
                addToFavoriteComicList(it)
            }
        }
    }

    fun addLatestComics(count: Int, context: Context) {
        viewModelScope.launch {
            getHttpJSON("https://xkcd.com/info.0.json", context) {
                val number = it.getInt("num")
                for (i in number downTo (number - count)) {
                    addComicSync(i, context, Tab.LATEST)
                }
            }
        }
    }

    enum class Tab {
        LATEST, FAVORITES
    }
}


class MainViewModelFactory(private val userDataStore: DataStore<Preferences>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(userDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}