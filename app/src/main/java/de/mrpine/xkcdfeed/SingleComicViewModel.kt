package de.mrpine.xkcdfeed

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class SingleComicViewModel: ViewModel() {
    val currentComic = mutableStateOf<XKCDComic?>(null)
    val currentNumber = mutableStateOf(-1)
    val imageLoaded = mutableStateOf(true)

    fun setImageLoaded(loaded: Boolean){imageLoaded.value = loaded}

    fun setComic(number: Int, context: Context){
        imageLoaded.value = false
        currentNumber.value = number

        XKCDComic.getComic(number = number, context = context, coroutineScope = viewModelScope, onImageLoaded = {imageLoaded.value = true}){
            currentComic.value = it
        }
    }

    fun setComic(comic: XKCDComic){
        imageLoaded.value = true
        currentNumber.value = comic.id
        currentComic.value = comic
    }
}