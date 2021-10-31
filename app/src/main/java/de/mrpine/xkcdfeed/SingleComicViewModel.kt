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
                onImageLoaded = { imageLoaded.value = true; comicCache[number]!!.imageLoaded = true}) {
                currentComic.value = it
                comicCache[number] = XKCDCacheObject(it, false)
            }
        } else {
            Log.d(TAG, "setComic: got from cache")
            setComic(cachedComic.comic)
            imageLoaded.value = cachedComic.imageLoaded
        }
    }
    
    private val comicCache = mutableStateMapOf<Int, XKCDCacheObject>()
    
    fun addToComicCache(comic: XKCDComic, imageLoaded: Boolean){
        comicCache[comic.id] = XKCDCacheObject(comic, imageLoaded)
    }

    fun setComicCacheImageLoaded(number: Int, imageLoaded: Boolean){
        comicCache[number]!!.imageLoaded = imageLoaded
    }

    fun setComic(comic: XKCDComic){
        imageLoaded.value = true
        currentNumber = comic.id
        currentComic.value = comic
    }
}

data class XKCDCacheObject(val comic: XKCDComic, var imageLoaded: Boolean)