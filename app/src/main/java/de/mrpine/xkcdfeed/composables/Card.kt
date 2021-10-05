package de.mrpine.xkcdfeed.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import de.mrpine.xkcdfeed.XKCDComic

private const val TAG = "Card"

@Composable
fun ComicCard(xkcdComic: XKCDComic, viewModel: MainViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 5.dp) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = xkcdComic.title)
            Text(
                text = viewModel.dateFormat.format(xkcdComic.pubDate.time),
                fontStyle = FontStyle.Italic
            )
            if (viewModel.imageLoadedMap[xkcdComic.id] == true) {



                val bitmap =
                    if (MaterialTheme.colors.isLight) xkcdComic.bitmapLight!! else xkcdComic.bitmapDark!!
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image of the comic",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()

                )
            }
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