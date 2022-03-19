package de.mr_pine.xkcdfeed

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

private const val TAG = "SingleComicViewModel"

class SingleComicViewModel: ViewModel() {
    var currentComic by mutableStateOf<XKCDComic?>(null)
    var currentNumber by mutableStateOf(-1)
    var imageLoaded by mutableStateOf(true)

    fun getCurrentSingleNumber(): Int {
        return currentNumber
    }

    fun setComic(number: Int, context: Context){
        currentNumber = number

        val cachedComic = comicCache[number]
        /*if(cachedComic == null) {
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
        }*/
    }
    
    private val comicCache = mutableStateMapOf<Int, XKCDCacheObject>()
    
    fun addToComicCache(comic: XKCDComic, imageLoaded: Boolean){
        comicCache[comic.id] = XKCDCacheObject(comic, imageLoaded)
    }

    fun setComicCacheImageLoaded(number: Int, imageLoaded: Boolean){
        comicCache[number]!!.imageLoaded = imageLoaded
    }
}

data class XKCDCacheObject(val comic: XKCDComic, var imageLoaded: Boolean)