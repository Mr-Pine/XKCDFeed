package de.mr_pine.xkcdfeed.composables.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import de.mr_pine.xkcdfeed.XKCDComic
import de.mr_pine.xkcdfeed.toColorMatrix
import de.mr_pine.xkcdfeed.ui.theme.Amber500
import de.mr_pine.xkcdfeed.ui.theme.Gray400
import kotlinx.coroutines.launch
import java.text.DateFormat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "Card"

val angle = 180 * PI.toFloat() / 180
val sin = sin(angle)
val cos = cos(angle)
val sqrt2 = sqrt(2f)
val sqrt2I = 1 / sqrt2
val sqrt3 = sqrt(3f)
val sqrt3I = 1 / sqrt3
val sqrt6 = sqrt(6f)
val sqrt6I = 1 / sqrt6

/*val e = (sqrt3I * sqrt2 * (sqrt2 * wR - (wB + wG) * sqrt2I) / bgr + sqrt3I) * sqrt3I
val f = (sqrt2 * wR - (wB + wG) * sqrt2I) / bgr * sqrt3I
val h = sqrt2 * (wG - wB) / (3 * bgr)
val i =
    ((wG - wB) / bgr * sqrt3I + sqrt3I - (sqrt2 * wR - (wB + wG) * sqrt2I) / bgr * sqrt6I) * sqrt3I
val j = (sqrt2 * wR - (wB + wG) * sqrt2I)/ bgr * sqrt3I
val k = -cos * sqrt6I - sin * sqrt2I
val l = cos * sqrt2I - sin * sqrt6I
val m = 2/3 * sqrt3I * sin * (wG-wB) / bgr

val matrix = ColorMatrix(floatArrayOf(
    e + sqrt2 * sqrt3I * (sqrt2 * sqrt3I - f) * cos - m, e + sqrt2 * sqrt3I * (-sqrt6I - f) * cos - sqrt2 * sqrt3I * (sqrt2I - h) * sin, e + sqrt2 * sqrt3I * (-sqrt6I - f) * cos - sqrt2 * sqrt3I * (-sqrt2I - h) * sin, 0f, 0f,
    i + (sqrt2 * sqrt3I - j) * k - h*l, i + (-sqrt6I - j) * k -(sqrt2I - h)*l, i + (-sqrt6I - j) * k -(-sqrt2I - h)*l, 0f, 0f,
    i + (sqrt2 * sqrt3I - j) * k - h*l, i + (-sqrt6I - j) * k -(sqrt2I - h)*l, i + (-sqrt6I - j) * k -(-sqrt2I - h)*l, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
))*/

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ComicCard(
    xkcdComic: XKCDComic,
    dateFormat: DateFormat,
    favoriteList: List<Int>,
    setFavorite: (XKCDComic) -> Unit,
    removeFavorite: (XKCDComic) -> Unit,
    invertMatrix: Array<FloatArray>,
    onLongPress: suspend (XKCDComic) -> Unit,
    showSingle: (XKCDComic) -> Unit
) {
    val colorFilter = ColorFilter.colorMatrix(ColorMatrix(invertMatrix.toColorMatrix()))

    val scope = rememberCoroutineScope()
    Card(
        elevation = 5.dp,
        backgroundColor = if (MaterialTheme.colors.isLight) Color.White else Color.Black,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onLongClick = { scope.launch { onLongPress(xkcdComic) } },
                onClick = { showSingle(xkcdComic) }
            )
    ) {
        MaterialTheme.colors.primarySurface
        Column(modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
                    Text(
                        text = xkcdComic.title ?: "I am a title :)",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.placeholder(xkcdComic.title == null)
                    )
                    Text(
                        text = "(${xkcdComic.id})",
                        modifier = Modifier
                            .padding(start = 4.dp, bottom = 1.5.dp)
                            .placeholder(xkcdComic.title == null),
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.caption
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var icon = Icons.Outlined.StarOutline
                    var tint = Gray400

                    if (favoriteList.contains(xkcdComic.id)) {
                        icon = Icons.Filled.Star
                        tint = Amber500
                    }
                    Icon(
                        icon,
                        "Star",
                        tint = tint,
                        modifier = Modifier.clickable {
                            (if (!favoriteList.contains(xkcdComic.id)) setFavorite else removeFavorite)(
                                xkcdComic
                            )
                        })
                }
            }
            Text(
                text = xkcdComic.pubDate.let { if (it != null) dateFormat.format(it.time) else "00/00/0000" },
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .placeholder(xkcdComic.pubDate == null)
            )
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(xkcdComic.imageURL)
                    .size(Size.ORIGINAL) // Set the target size to load the image at.
                    .build()
            )

            Image(
                painter = painter,
                //bitmap = bitmap.asImageBitmap(),
                contentDescription = "Image of the comic",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (painter.state is AsyncImagePainter.State.Success) Modifier else Modifier.sizeIn(
                            minHeight = 200.dp
                        )
                    )
                    .placeholder(
                        painter.state !is AsyncImagePainter.State.Success,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                colorFilter = if (MaterialTheme.colors.isLight) null else colorFilter
            )
            Text(
                text = xkcdComic.description
                    ?: "I am a description text. If you see me, something isn't working as intended. Written at 2:36 a.m.",
                style = MaterialTheme.typography.caption,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .placeholder(xkcdComic.description == null)
            )
        }
    }
}

/* is code den ich ungerne wieder 5 Stunden lang zusammensuchen will aber der hier irgendwie auch keine Verwendung hat ¯\_(ツ)_/¯

                        var scale by remember { mutableStateOf(1f) }
                        var rotation by remember { mutableStateOf(0f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                            scale *= zoomChange
                            rotation += rotationChange
                            offset += offsetChange
                        }


                        Modifier.graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                        .pointerInput(Unit) {
                            forEachGesture {
                                awaitPointerEventScope {
                                    awaitFirstDown()
                                    do {
                                        val event = awaitPointerEvent()
                                    } while (event.changes.any { it.pressed })
                                    scale = 1F
                                    offset = Offset.Zero
                                    rotation = 0F
                                }
                            }
                        }

Macht halt so pinch to zoom aber mit zurücksnappen aber halt auch nur innerhalb der zwei parent composables

 */