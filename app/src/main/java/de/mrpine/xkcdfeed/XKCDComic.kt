package de.mrpine.xkcdfeed

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.StrictMode
import android.util.Log
import androidx.core.graphics.set
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


private const val TAG = "XKCDComic"

class XKCDComic(
    val title: String,
    val imageURL: String,
    val id: Int,
    val pubDate: Calendar,
    val description: String,
    val coroutineScope: CoroutineScope,
    val onImageLoaded: () -> Unit
) {
    var bitmapLight: Bitmap? = null
    var bitmapDark: Bitmap? = null

    private val TAG = "XKCDComic"

    init {
        coroutineScope.launch {
            bitmapLight = getBitmapFromURL(imageURL)
            bitmapDark = convertToDarkImage(bitmapLight, onImageLoaded)
        }
    }

    private fun convertToDarkImage(lightBitmap: Bitmap?, onFinish: () -> Unit): Bitmap? {
        if (lightBitmap == null) return null

        val darkBitmap = lightBitmap.copy(lightBitmap.config, true)
        for (y in 0 until darkBitmap.height) {
            for (x in 0 until darkBitmap.width) {
                val pixelColor = darkBitmap.getPixel(x, y)
                val invertedPixelColor = Color.rgb(255 - Color.red(pixelColor), 255 - Color.green(pixelColor), 255 - Color.blue(pixelColor))
                val hsv = FloatArray(3)
                Color.colorToHSV(invertedPixelColor, hsv)
                darkBitmap[x, y] = Color.HSVToColor(floatArrayOf((hsv[0] + 180) % 360, hsv[1], hsv[2]))
            }
        }
        onFinish()
        Log.d(TAG, "convertToDarkImage: $id")
        return darkBitmap
    }

    companion object {
        fun getComic(
            number: Int,
            context: Context,
            coroutineScope: CoroutineScope,
            onImageLoaded: () -> Unit,
            saveComic: (XKCDComic) -> Unit
        ) {
            getHttpJSON("https://xkcd.com/$number/info.0.json", context) {
                generateComic(
                    it,
                    coroutineScope,
                    onImageLoaded,
                    saveComic
                )
            }
        }

        private fun generateComic(
            jsonObject: JSONObject,
            coroutineScope: CoroutineScope,
            onImageLoaded: () -> Unit,
            saveComic: (XKCDComic) -> Unit
        ) {
            val title = jsonObject.getString("title")
            val imageURL = jsonObject.getString("img")
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(
                jsonObject.getInt("year"),
                jsonObject.getInt("month") - 1,
                jsonObject.getInt("day")
            )
            val description = jsonObject.getString("alt")
            val number = jsonObject.getInt("num")
            val comic = XKCDComic(
                title = title,
                imageURL = imageURL,
                id = number,
                pubDate = calendar,
                description = description,
                coroutineScope = coroutineScope,
                onImageLoaded = onImageLoaded
            )
            saveComic(comic)
        }
    }
}

fun getHttpJSON(
    getURL: String, context: Context, onError: (VolleyError) -> Unit = {
        Log.e(
            "XKCDComic.kt",
            "getHttpJson: ${it.message}",

            )
    }, returnFunction: (JSONObject) -> Unit
) {

    // Instantiate the RequestQueue with the cache and network. Start the queue.
    val requestQueue = Volley.newRequestQueue(context)
    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)
    val jsonObjectRequest = JsonObjectRequest(
        Request.Method.GET, getURL, null,
        { response ->
            Log.d(TAG, "getHttpJSON: ${response.getInt("num")}")
            returnFunction(response)
        },
        { error ->
            onError(error)
        }
    )

    requestQueue.add(jsonObjectRequest)

}

fun getBitmapFromURL(src: String?): Bitmap? {
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