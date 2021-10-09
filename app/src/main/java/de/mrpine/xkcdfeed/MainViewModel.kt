package de.mrpine.xkcdfeed

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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


class MainViewModel(
    private val userDataStore: DataStore<Preferences>,
    val dateFormat: DateFormat,
    val startActivity: (Intent) -> Unit,
    val navigateTo: (String) -> Unit
) : ViewModel() {
    private val TAG = "MainViewModel"

    var latestComicNumber = -1

    //<editor-fold desc="BottomSheet State">
    @ExperimentalMaterialApi
    var modalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var currentBottomSheetXKCDComic = mutableStateOf<XKCDComic?>(null)

    @ExperimentalMaterialApi
    suspend fun showBottomSheet(xkcdComic: XKCDComic) {
        modalBottomSheetState.show()
        currentBottomSheetXKCDComic.value = xkcdComic
    }

    @ExperimentalMaterialApi
    suspend fun hideBottomSheet() {
        modalBottomSheetState.hide()
        currentBottomSheetXKCDComic.value = null
    }
    //</editor-fold>


    //<editor-fold desc="Favorite List">
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

    private suspend fun addToFavoriteList(id: Int) {
        userDataStore.edit { mutablePreferences ->
            val stringList = mutablePreferences[FAVORITE_LIST] ?: "[]"
            val mutableList = generateListFromJSON(stringList)
            mutableList.add(id)
            mutablePreferences[FAVORITE_LIST] =
                JSONArray(mutableList.toTypedArray()).toString()
        }
    }

    private suspend fun removeFromFavoriteList(id: Int) {
        userDataStore.edit { mutablePreferences ->
            val stringList = mutablePreferences[FAVORITE_LIST] ?: "[]"
            mutablePreferences[FAVORITE_LIST] =
                JSONArray(generateListFromJSON(stringList) { value, _ -> value != id }.toTypedArray()).toString()
        }
    }

    //Helper to parse string stored in DataStore
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
    //</editor-fold>


    //<editor-fold desc="Functions to add or delete a comic to/from the favorites">
    fun addFavorite(xkcdComic: XKCDComic, imageLoaded: Boolean = true) {
        viewModelScope.launch {
            addToFavoriteList(xkcdComic.id)
            addToFavoriteComicList(xkcdComic, imageLoaded)
        }
    }

    fun removeFavorite(xkcdComic: XKCDComic) {
        viewModelScope.launch {
            removeFromFavoriteList(xkcdComic.id)
            removeFromFavoriteComicList(xkcdComic)
        }
    }
    //</editor-fold>


    //<editor-fold desc="Functions to add and remove Comics to/from the lists">
    var latestComicsList = mutableStateListOf<XKCDComic>()
    var latestImagesLoadedMap = mutableStateMapOf<Int, Boolean>()

    var favoriteComicsList = mutableStateListOf<XKCDComic>()
    var favoriteImagesLoadedMap = mutableStateMapOf<Int, Boolean>()

    private fun addToLatestComicList(item: XKCDComic) {
        latestComicsList.add(item)
        latestComicsList.sortByDescending { it.id }
    }

    private fun addToFavoriteComicList(item: XKCDComic, imageLoaded: Boolean? = null) {
        favoriteComicsList.add(item)
        if (imageLoaded != null) favoriteImagesLoadedMap[item.id] = imageLoaded
        favoriteComicsList.sortByDescending { it.id }
    }

    private fun removeFromFavoriteComicList(item: XKCDComic) {
        favoriteComicsList.remove(favoriteComicsList.find { it.id == item.id })
        favoriteImagesLoadedMap.remove(item.id)
        favoriteComicsList.sortByDescending { it.id }
    }
    //</editor-fold>


    //<editor-fold desc="Functions to generate new comics and add them to their lists">
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
    //</editor-fold>


    //<editor-fold desc="Helper function to load all latest comics into the list">
    fun addLatestComics(count: Int, context: Context) {
        viewModelScope.launch {
            getHttpJSON("https://xkcd.com/info.0.json", context) {
                val number = it.getInt("num")
                latestComicNumber = number
                for (i in number downTo (number - (count - 1))) {
                    addComicSync(i, context, Tab.LATEST)
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Fav List State">
    val favListState: LazyListState = LazyListState()

    suspend fun scrollToFavItem(index: Int){
            favListState.animateScrollToItem(index = index)
    }
    //</editor-fold>

    enum class Tab {
        LATEST, FAVORITES
    }
}


class MainViewModelFactory(
    private val userDataStore: DataStore<Preferences>,
    private val dateFormat: DateFormat,
    private val startActivity: (Intent) -> Unit,
    private val navigateTo: (String) -> Unit
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(userDataStore, dateFormat, startActivity, navigateTo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}