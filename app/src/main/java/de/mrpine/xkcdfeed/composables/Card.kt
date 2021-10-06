package de.mrpine.xkcdfeed.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import java.text.DateFormat
import java.util.*

private const val TAG = "Card"

@Composable
fun ComicCard(xkcdComic: XKCDComic, dateFormat: DateFormat, imageLoadedMap: MutableMap<Int, Boolean>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 5.dp, backgroundColor = if(MaterialTheme.colors.isLight) Color.White else Color.Black, shape = MaterialTheme.shapes.medium) {
        MaterialTheme.colors.primarySurface
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.Bottom){
                Text(text = xkcdComic.title, fontWeight = FontWeight.Bold)
                Text(text = "(${xkcdComic.id})", modifier = Modifier.padding(start = 4.dp, bottom = 1.5.dp), fontStyle = FontStyle.Italic, style = MaterialTheme.typography.caption)
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
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(color = MaterialTheme.colors.primary.copy(alpha = 0.5F))) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            Text(text = xkcdComic.description, style = MaterialTheme.typography.caption, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Preview("Preview Card")
@Composable
fun PreviewCard(){
    val cal = Calendar.getInstance()
    cal.set(2021, 9, 4)
    XKCDFeedTheme(darkTheme = false) {
        ComicCard(xkcdComic = XKCDComic("Comet Visitor", "https://imgs.xkcd.com/comics/comet_visitor.png", 2524, cal, "this is a description I am too lazy to copy", rememberCoroutineScope(), {}), DateFormat.getDateInstance(), mutableMapOf())
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