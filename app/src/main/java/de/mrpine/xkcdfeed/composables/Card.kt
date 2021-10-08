package de.mrpine.xkcdfeed.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mrpine.xkcdfeed.XKCDComic
import de.mrpine.xkcdfeed.ui.theme.Amber500
import de.mrpine.xkcdfeed.ui.theme.Gray400
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

private const val TAG = "Card"


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ComicCard(
    xkcdComic: XKCDComic,
    dateFormat: DateFormat,
    imageLoadedMap: MutableMap<Int, Boolean>,
    favoriteList: List<Int>,
    setFavorite: (XKCDComic) -> Unit,
    removeFavorite: (XKCDComic) -> Unit,
    onLongPress: suspend (XKCDComic) -> Unit
) {
    val scope = rememberCoroutineScope()
    Card(
        elevation = 5.dp,
        backgroundColor = if (MaterialTheme.colors.isLight) Color.White else Color.Black,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = { scope.launch { onLongPress(xkcdComic) } }) {}
    ) {
        MaterialTheme.colors.primarySurface
        Column(modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
                    Text(text = xkcdComic.title, fontWeight = FontWeight.Bold)
                    Text(
                        text = "(${xkcdComic.id})",
                        modifier = Modifier.padding(start = 4.dp, bottom = 1.5.dp),
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
                text = dateFormat.format(xkcdComic.pubDate.time),
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (imageLoadedMap[xkcdComic.id] == true) {
                val bitmap =
                    if (MaterialTheme.colors.isLight) xkcdComic.bitmapLight!! else xkcdComic.bitmapDark!!
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image of the comic",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()

                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(color = MaterialTheme.colors.primary.copy(alpha = 0.5F))
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            Text(
                text = xkcdComic.description,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview("Preview Card")
@Composable
fun PreviewCard() {
    val cal = Calendar.getInstance()
    cal.set(2021, 9, 4)
    XKCDFeedTheme(darkTheme = false) {
        ComicCard(
            xkcdComic = XKCDComic(
                "Comet Visitor",
                "https://imgs.xkcd.com/comics/comet_visitor.png",
                2524,
                cal,
                "this is a description I am too lazy to copy",
                rememberCoroutineScope(),
                {}), DateFormat.getDateInstance(), mutableMapOf(), listOf(2523), {}, {}, {}
        )
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