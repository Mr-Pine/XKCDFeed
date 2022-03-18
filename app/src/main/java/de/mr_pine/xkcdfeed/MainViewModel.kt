package de.mr_pine.xkcdfeed

import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.text.DateFormat


class MainViewModel(
    private val userDataStore: DataStore<Preferences>,
    val dateFormat: DateFormat,
    val startActivity: (Intent) -> Unit,
    val navigateTo: (String) -> Unit,
    private val loginViewModel: LoginViewModel,
    private val addToComicCache: (XKCDComic, Boolean) -> Unit,
    private val setComicCacheImageLoaded: (Int, Boolean) -> Unit
) : ViewModel() {
    private val TAG = "MainViewModel"

    private val db = Firebase.firestore

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
    val favoriteListKey = stringPreferencesKey("favorite_list")

    var favoriteListInitialized = false
    var lastClearType = ClearType.UNDEFINED
    var favoriteList = mutableStateListOf<Int>()

    enum class ClearType {
        LOCAL, FIREBASE, UNDEFINED
    }

    fun initFavoriteList(context: Context, clear: Boolean = false, clearType: ClearType? = null) {
        if (!favoriteListInitialized || (clear && lastClearType != clearType)) {
            favoriteListInitialized = true
            favoriteList.clear()
            if (clearType != null) lastClearType = clearType
            viewModelScope.launch {
                if (!loginViewModel.signedIn) {
                    userDataStore.data.first { preferences ->
                        // No type safety.
                        Log.d(TAG, "initFavoriteList: hi :)")
                        val stringList = preferences[favoriteListKey] ?: "[]"
                        favoriteList.addAll(generateListFromJSON(stringList))
                        addFromFavoritesList(context, clear)
                        false
                    }
                } else {
                    val favoritesReference =
                        db.collection("Users/${loginViewModel.user?.uid}/Favorites")
                    favoritesReference.get().addOnSuccessListener { collection ->
                        val documents = collection.documents
                        val listValues = documents.map { documentSnapshot ->
                            documentSnapshot.id.toInt()
                        }
                        Log.d(TAG, "initFavoriteList: hi :) $listValues")
                        favoriteList.addAll(listValues)
                        addFromFavoritesList(context, clear)
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "initFavoriteList: $e user: ${loginViewModel.user?.uid}")
                    }
                }
            }
        }
    }

    private fun addFromFavoritesList(context: Context, clear: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (clear) favoriteComicsList.clear()
            Log.d(TAG, "addFromFavoritesList: ${favoriteList.toTypedArray()}")
            for (i in favoriteList) {
                addComicSync(
                    i,
                    context,
                    Tab.FAVORITES
                )
            }
        }
    }

    private suspend fun addToFavoriteList(id: Int) {
        favoriteList.add(id)
        if (!loginViewModel.signedIn) {
            userDataStore.edit { mutablePreferences ->
                val stringList = mutablePreferences[favoriteListKey] ?: "[]"
                val mutableList = generateListFromJSON(stringList)
                mutableList.add(id)
                mutablePreferences[favoriteListKey] =
                    JSONArray(mutableList.toTypedArray()).toString()
            }
        } else {
            db.collection("Users/${loginViewModel.user?.uid}/Favorites").document(id.toString())
                .set(emptyMap<String, String>())
        }
    }

    private suspend fun removeFromFavoriteList(id: Int) {
        favoriteList.remove(id)
        if (!loginViewModel.signedIn) {
            userDataStore.edit { mutablePreferences ->
                val stringList = mutablePreferences[favoriteListKey] ?: "[]"
                mutablePreferences[favoriteListKey] =
                    JSONArray(generateListFromJSON(stringList) { value, _ -> value != id }.toTypedArray()).toString()
            }
        } else {
            db.collection("Users/${loginViewModel.user?.uid}/Favorites").document(id.toString())
                .delete()
        }
    }

    //Helper to parse string stored in DataStore
    fun generateListFromJSON(
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
    var latestComicsInitialized = false
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
        try {
            favoriteComicsList.sortByDescending { it.id }
        } catch (e: Exception) {
            Log.e(TAG, "addToFavoriteComicList: Error occurred: $e", )
        }
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

    private suspend fun addComicSync(number: Int, context: Context, to: Tab) {
        withContext(Dispatchers.Main) {
            (if (to == Tab.LATEST) latestImagesLoadedMap else favoriteImagesLoadedMap)[number] =
                false
        }
        XKCDComic.getComic(
            number = number,
            coroutineScope = viewModelScope,
            context = context,
            onImageLoaded = {
                (if (to == Tab.LATEST) latestImagesLoadedMap else favoriteImagesLoadedMap)[number] =
                    true
                setComicCacheImageLoaded(number, true)
            }) {
            if (to == Tab.LATEST) {
                addToLatestComicList(it)
            } else {
                addToFavoriteComicList(it)
            }
            addToComicCache(it, false)
        }
    }
    //</editor-fold>


    //<editor-fold desc="Helper function to load all latest comics into the list">
    @ObsoleteCoroutinesApi
    fun addLatestComics(count: Int, context: Context) {
        latestComicsInitialized = true
        getHttpJSON("https://xkcd.com/info.0.json", context, viewModelScope) {
            viewModelScope.launch(newSingleThreadContext("yes hello")) {
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

    suspend fun scrollToFavItem(index: Int) {
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
    private val navigateTo: (String) -> Unit,
    private val loginViewModel: LoginViewModel,
    private val addToComicCache: (XKCDComic, Boolean) -> Unit,
    private val setComicCacheImageLoaded: (Int, Boolean) -> Unit
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                userDataStore,
                dateFormat,
                startActivity,
                navigateTo,
                loginViewModel,
                addToComicCache,
                setComicCacheImageLoaded
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}