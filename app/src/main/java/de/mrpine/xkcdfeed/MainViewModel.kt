package de.mrpine.xkcdfeed

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.DateFormat

class MainViewModel : ViewModel() {
    var comicList = mutableStateListOf<XKCDComic>()
    var imageLoadedMap = mutableStateMapOf<Int, Boolean>()

    lateinit var dateFormat: DateFormat

    private fun addToComicList(item: XKCDComic) {
        comicList.add(item)
        comicList.sortByDescending { it.id }
    }

    fun addComic(number: Int, context: Context) {
        viewModelScope.launch {
            imageLoadedMap[number] = false
            XKCDComic.getComic(number = number, coroutineScope = viewModelScope, context = context, onImageLoaded = {imageLoadedMap[number] = true}) {
                addToComicList(it)
            }
        }
    }

    private fun addComicSync(number: Int, context: Context) {
        imageLoadedMap[number] = false
        XKCDComic.getComic(number = number, coroutineScope = viewModelScope, context = context, onImageLoaded = {imageLoadedMap[number] = true}) {
            addToComicList(it)
        }
    }

    fun addLatestComics(count: Int, context: Context) {
        viewModelScope.launch {
            getHttpJSON("https://xkcd.com/info.0.json", context){
                val number = it.getInt("num")
                for (i in number downTo (number - (count - 1))) {
                    addComicSync(i, context)
                }
            }
        }
    }
}