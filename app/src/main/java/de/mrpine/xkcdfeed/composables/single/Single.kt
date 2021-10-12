package de.mrpine.xkcdfeed.composables.single

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mrpine.xkcdfeed.MainViewModel
import de.mrpine.xkcdfeed.SingleComicViewModel
import de.mrpine.xkcdfeed.XKCDComic
import de.mrpine.xkcdfeed.ui.theme.Amber500
import de.mrpine.xkcdfeed.ui.theme.Gray400
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "Single"

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun SingleViewContent(
    comic: XKCDComic,
    isFavorite: Boolean,
    dateFormat: DateFormat,
    imageLoaded: Boolean,
    currentNumber: Int,
    maxNumber: Int,
    setNumber: (Int) -> Unit,
    setFavorite: (XKCDComic) -> Unit,
    removeFavorite: (XKCDComic) -> Unit,
    startActivity: (Intent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val orientation = LocalConfiguration.current.orientation
    LaunchedEffect(key1 = Unit, block = {
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            scaffoldState.bottomSheetState.expand()
    })

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(contentColor = Color.White, contentPadding = PaddingValues(5.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { setNumber(0) }) {
                        Icon(Icons.Default.FirstPage, "First Comic")
                    }
                    IconButton(
                        onClick = { setNumber(currentNumber - 1) },
                        enabled = currentNumber > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, "Previous Comic")
                    }
                    OutlinedTextField(
                        value = currentNumber.toString(),
                        modifier = Modifier
                            .width(100.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        onValueChange = { setNumber(it.toInt()) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.White,
                            focusedBorderColor = Color.White,
                            cursorColor = Color.White,
                            backgroundColor = MaterialTheme.colors.primaryVariant
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                    IconButton(
                        onClick = { setNumber(currentNumber + 1) },
                        enabled = currentNumber < maxNumber
                    ) {
                        Icon(Icons.Default.ArrowForward, "Next Comic")
                    }
                    IconButton(onClick = { setNumber(maxNumber) }) {
                        Icon(Icons.Default.LastPage, "Latest Comic")
                    }

                }
            }
        },
        sheetPeekHeight = 77.dp,
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        ),
        sheetContent = BottomSheetContent(
            comic,
            dateFormat,
            isFavorite,
            setFavorite,
            removeFavorite,
            startActivity,
            scope
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 77.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageLoaded) {
                val bitmap =
                    if (MaterialTheme.colors.isLight) comic.bitmapLight else comic.bitmapDark
                if (bitmap != null) {
                    ZoomableImage(bitmap = bitmap.asImageBitmap())
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun ZoomableImage(bitmap: ImageBitmap) {
    val maxScale = 0.030F
    val minScale = 10F
    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    var centoid by remember { mutableStateOf(Offset(1f, 1f)) }

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var layerSize by remember { mutableStateOf(IntSize(1, 1)) }

    var isTransforming by remember { mutableStateOf(false) }

    Box(
        Modifier
            // apply pan offset state as a layout transformation before other modifiers
            // add transformable to listen to multitouch transformation events after offset
            //.transformable(state = state)
            // optional for example: add double click to zoom

            .pointerInput(Unit) {
                detectDragGestures(onDragStart = { dragOffset = Offset.Zero }, onDragEnd = {
                    if (scale == 1f) {
                        val offsetX = dragOffset.x
                        if (offsetX > 100) {
                            Log.d(TAG, "ZoomableImage: right")
                        } else if (offsetX < 100) {
                            Log.d(TAG, "ZoomableImage: left")
                        }
                    }
                }) { _, dragAmount ->
                    if (scale != 1f) {
                        offset += dragAmount
                        Log.d(TAG, "ZoomableImage: helo")
                    } else {
                        dragOffset += dragAmount
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale != 1f) {
                            scope.launch {
                                state.animateZoomBy(1 / scale)
                            }
                            offset = Offset.Zero
                            rotation = 0f
                        } else {
                            scope.launch {
                                state.animateZoomBy(2f)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { transformCentroid, pan, zoom, transformRotation ->
                    offset += pan
                    scale *= zoom
                    rotation += transformRotation

                    /*if (!isTransforming) {
                        //offset -= centoid - transformCentroid
                        centoid = transformCentroid
                    }
                    isTransforming = true*/

                    /*//TODO: Fix

                    val x0 = centoid.x - imageOffset.x
                    val y0 = centoid.y - imageOffset.y

                    val hyp0 = sqrt(x0 * x0 + y0 * y0)
                    val hyp1 = scale * hyp0

                    val alpha0 = atan(y0 / x0)

                    val alpha1 = alpha0 - transformRotation

                    val x1 = sin(alpha1) * hyp1
                    val y1 = cos(alpha1) * hyp1

                    offset = Offset(x1, y1)
*/
                    //Log.d(TAG, "ZoomableImage: $centoid")
                }
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = "Comic Image",
            modifier = Modifier
                .fillMaxSize()
                .clip(RectangleShape)
                /*.onGloballyPositioned { coordinates ->
                    layerSize = coordinates.size
                }*/
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .graphicsLayer(
                    scaleX = scale - 0.02f,
                    scaleY = scale - 0.02f,
                    /*transformOrigin = TransformOrigin(
                        centoid.x / layerSize.width,
                        centoid.y / layerSize.height
                    ),*/
                    rotationZ = rotation
                ),
            contentScale = ContentScale.Fit
        )/*
        Box(modifier = Modifier
            .size(5.dp)
            .absoluteOffset { IntOffset(centoid.x.toInt(), centoid.y.toInt()) }
            .background(Color.Red))*/
    }
}

@Composable
fun BottomSheetContent(
    comic: XKCDComic,
    dateFormat: DateFormat,
    isFavorite: Boolean,
    setFavorite: (XKCDComic) -> Unit,
    removeFavorite: (XKCDComic) -> Unit,
    startActivity: (Intent) -> Unit,
    scope: CoroutineScope
): @Composable ColumnScope.() -> Unit {
    return {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 0.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            MaterialTheme.colors.primary
                        )
                )

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row() {
                    Column() {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = comic.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                            Text(
                                text = "(${comic.id})",
                                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Text(
                            text = dateFormat.format(comic.pubDate.time),
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var icon = Icons.Outlined.StarOutline
                    var tint = Gray400

                    if (isFavorite) {
                        icon = Icons.Filled.Star
                        tint = Amber500
                    }
                    Icon(
                        icon,
                        "Star",
                        tint = tint,
                        modifier = Modifier.clickable {
                            (if (!isFavorite) setFavorite else removeFavorite)(
                                comic
                            )
                        }
                    )
                }
            }
            Text(text = comic.description)
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Button(onClick = {
                    (if (!isFavorite) setFavorite else removeFavorite)(
                        comic
                    )
                }, modifier = Modifier.width(140.dp)) {
                    var icon = Icons.Outlined.StarOutline
                    var tint = Gray400

                    if (isFavorite) {
                        icon = Icons.Filled.Star
                        tint = Amber500
                    }
                    Icon(
                        icon,
                        "Star",
                        tint = tint
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isFavorite) "Remove" else "Add")
                }
                Button(onClick = {
                    scope.launch {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "https://xkcd.com/${comic.id}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }
                }, modifier = Modifier.width(140.dp)) {
                    Icon(
                        Icons.Default.Share,
                        "Star"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun SingleViewContentStateful(
    singleViewModel: SingleComicViewModel,
    mainViewModel: MainViewModel,
    setComic: (Int) -> Unit
) {
    val currentComic = singleViewModel.currentComic.value
    val currentNumber = singleViewModel.currentNumber.value
    val favList = mainViewModel.favoriteListFlow.collectAsState(initial = listOf())
    if (currentComic != null) {
        val favoriteList = favList.value


        SingleViewContent(
            comic = currentComic,
            isFavorite = favoriteList.contains(currentComic.id),
            dateFormat = mainViewModel.dateFormat,
            imageLoaded = singleViewModel.imageLoaded.value,
            setFavorite = mainViewModel::addFavorite,
            removeFavorite = mainViewModel::removeFavorite,
            currentNumber = currentNumber,
            setNumber = setComic,
            maxNumber = mainViewModel.latestComicNumber,
            startActivity = { mainViewModel.startActivity(it) }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Preview
@Composable
fun SinglePreview() {
    XKCDFeedTheme(darkTheme = false) {
        val comic = XKCDComic(
            "Comet Visitor",
            "https://imgs.xkcd.com/comics/comet_visitor.png",
            2524,
            Calendar.getInstance(),
            "this is a description I am too lazy to copy",
            rememberCoroutineScope(),
            {}
        )
        SingleViewContent(
            comic = comic,
            isFavorite = true,
            DateFormat.getDateInstance(),
            false,
            2524,
            2526,
            {},
            {},
            {},
            {})

    }
}