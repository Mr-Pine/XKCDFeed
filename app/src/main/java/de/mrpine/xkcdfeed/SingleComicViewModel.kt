package de.mrpine.xkcdfeed

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

private const val TAG = "SingleComicViewModel"

class SingleComicViewModel: ViewModel() {
    val currentComic = mutableStateOf<XKCDComic?>(null)
    var currentNumber by mutableStateOf(-1)
    val imageLoaded = mutableStateOf(true)

    fun getCurrentSingleNumber(): Int {
        return currentNumber
    }

    fun setImageLoaded(loaded: Boolean){imageLoaded.value = loaded}

    fun setComic(number: Int, context: Context){
        imageLoaded.value = false
        currentNumber = number

        val cachedComic = comicCache[number]
        if(cachedComic == null) {
            XKCDComic.getComic(
                number = number,
                context = context,
                coroutineScope = viewModelScope,
                onImageLoaded = { imageLoaded.value = true }) {
                currentComic.value = it
                comicCache[number] = it
            }
        } else {
            Log.d(TAG, "setComic: got from cache")
            setComic(cachedComic)
        }
    }
    
    private val comicCache = mutableStateMapOf<Int, XKCDComic>()
    
    fun addToComicCache(comic: XKCDComic){
        comicCache[comic.id] = comic
    }

    fun setComic(comic: XKCDComic){
        imageLoaded.value = true
        currentNumber = comic.id
        currentComic.value = comic
    }
}