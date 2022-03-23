package de.mr_pine.xkcdfeed

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


private const val TAG = "XKCDComic"

class XKCDComic(
    val id: Int,
    private val coroutineScope: CoroutineScope,
    context: Context,
    onImageLoaded: () -> Unit
) {
    var title: String? by mutableStateOf(null)
    var pubDate: Calendar? by mutableStateOf(null)
    var imageURL by mutableStateOf("")
    var description: String? by mutableStateOf(null)
    var bitmapLight: Bitmap? by mutableStateOf(null)
    var bitmapDark: Bitmap? by mutableStateOf(null)

    private val TAG = "XKCDComic"

    init {
        getHttpJSON("https://xkcd.com/$id/info.0.json", context, coroutineScope) { json ->
            title = json.getString("title")
            imageURL = json.getString("img")
            pubDate = Calendar.getInstance()
            pubDate!!.clear()
            pubDate!!.set(
                json.getInt("year"),
                json.getInt("month") - 1,
                json.getInt("day")
            )
            description = json.getString("alt")


        }
    }

    /*private fun convertToDarkImage(lightBitmap: Bitmap?, onFinish: () -> Unit): Bitmap? {
        if (lightBitmap == null) return null

        val darkBitmap = lightBitmap.copy(lightBitmap.config, true)
        for (y in 0 until darkBitmap.height) {
            for (x in 0 until darkBitmap.width) {
                val pixelColor = darkBitmap.getPixel(x, y)
                val invertedPixelColor = Color.rgb(
                    255 - Color.red(pixelColor),
                    255 - Color.green(pixelColor),
                    255 - Color.blue(pixelColor)
                )
                val hsv = FloatArray(3)
                Color.colorToHSV(invertedPixelColor, hsv)
                darkBitmap[x, y] =
                    Color.HSVToColor(floatArrayOf((hsv[0] + 180) % 360, hsv[1], hsv[2]))
            }
        }
        onFinish()
        return darkBitmap
    }*/
}

fun getHttpJSON(
    getURL: String,
    context: Context,
    coroutineScope: CoroutineScope,
    onError: (VolleyError) -> Unit = {
        Log.e(
            "XKCDComic.kt",
            "getHttpJson: ${it.message}",

            )
    },
    returnFunction: (JSONObject) -> Unit
) {

    // Instantiate the RequestQueue with the cache and network. Start the queue.
    coroutineScope.launch(Dispatchers.IO) {
        val requestQueue = Volley.newRequestQueue(context)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, getURL, null,
            returnFunction,
            onError
        )

        requestQueue.add(jsonObjectRequest)
    }

}

fun getBitmapFromURL(src: String): Bitmap? {
    return try {
        val url = URL(src)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        null
    }
}

