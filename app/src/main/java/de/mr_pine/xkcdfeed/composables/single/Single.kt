package de.mr_pine.xkcdfeed.composables.single

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.google.accompanist.placeholder.material.placeholder
import de.mr_pine.xkcdfeed.MainViewModel
import de.mr_pine.xkcdfeed.SingleComicViewModel
import de.mr_pine.xkcdfeed.XKCDComic
import de.mr_pine.xkcdfeed.toColorMatrix
import de.mr_pine.xkcdfeed.ui.theme.Amber500
import de.mr_pine.xkcdfeed.ui.theme.Gray400
import de.mr_pine.zoomables.Zoomable
import de.mr_pine.zoomables.rememberZoomableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import kotlin.math.max
import kotlin.random.Random

private const val TAG = "Single"

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun SingleViewContent(
    comic: XKCDComic,
    isFavorite: Boolean,
    dateFormat: DateFormat,
    maxNumber: Int,
    setNumber: (Int) -> Unit,
    setFavorite: (XKCDComic) -> Unit,
    removeFavorite: (XKCDComic) -> Unit,
    colorMatrix: ColorMatrix,
    getNumber: () -> Int,
    navigateHome: () -> Unit,
    startActivity: (Intent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current).data(comic.imageURL)
            .size(Size.ORIGINAL) // Set the target size to load the image at.
            .build()
    )

    val orientation = LocalConfiguration.current.orientation
    LaunchedEffect(key1 = Unit, block = {
        if (orientation == Configuration.ORIENTATION_PORTRAIT && scaffoldState.bottomSheetState.isCollapsed) scaffoldState.bottomSheetState.expand()
    })

    var currentNumberString by remember(comic.id) { mutableStateOf(comic.id.toString()) }

    var parentSize by remember { mutableStateOf(IntSize(0, 0)) }
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
                    IconButton(onClick = navigateHome) {
                        Icon(Icons.Default.Close, "Close")
                    }
                    IconButton(
                        onClick = { setNumber(comic.id - 1) }, enabled = comic.id > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, "Previous Comic")
                    }
                    OutlinedTextField(
                        value = currentNumberString,
                        modifier = Modifier.width(100.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                        ),
                        onValueChange = {
                            if(it.isBlank()) {
                                currentNumberString = ""
                                return@OutlinedTextField
                            }
                            try {
                                currentNumberString = it.filter { digit -> digit.isDigit() }
                                    .takeIf { newString -> newString.toInt() <= maxNumber }
                                    ?: currentNumberString
                                if (currentNumberString.isNotBlank()) {
                                    val newNumber = currentNumberString.toInt()
                                    if (newNumber <= maxNumber) setNumber(newNumber)
                                }
                            } catch (err: Exception) {
                                Log.e(TAG, "$it not parsable")
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.White,
                            focusedBorderColor = Color.White,
                            cursorColor = Color.White,
                            backgroundColor = MaterialTheme.colors.primaryVariant
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()

                        })
                    )
                    IconButton(
                        onClick = { setNumber(comic.id + 1) }, enabled = comic.id < maxNumber
                    ) {
                        Icon(Icons.Default.ArrowForward, "Next Comic")
                    }
                    IconButton(onClick = { setNumber(Random.nextInt(maxNumber + 1)) }) {
                        Icon(Icons.Default.Shuffle, "Random Comic")
                    }

                }
            }
        },
        sheetPeekHeight = 77.dp,
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        ),
        sheetContent = bottomSheetContent(
            comic, dateFormat, isFavorite, setFavorite, removeFavorite, startActivity, scope
        ),
        modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
            parentSize = layoutCoordinates.size
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (MaterialTheme.colors.isLight) Color.White else Color.Black)
                .padding(bottom = with(LocalDensity.current) {
                    max(
                        0f, (parentSize.height - scaffoldState.bottomSheetState.requireOffset())
                    ).toDp()
                }), contentAlignment = Alignment.Center
        ) {
            val zoomableState = rememberZoomableState()
            if (painter.state is AsyncImagePainter.State.Success) {
                Zoomable(
                    coroutineScope = scope,
                    zoomableState = zoomableState,
                    onSwipeLeft = {
                        if (getNumber() < maxNumber) setNumber(getNumber() + 1)
                    },
                    onSwipeRight = {
                        if (getNumber() > 0) setNumber(getNumber() - 1)
                    },
                ) {
                    Image(
                        painter,
                        contentDescription = "Comic Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RectangleShape)
                            .padding(2.dp),
                        colorFilter = if (MaterialTheme.colors.isLight) null else ColorFilter.colorMatrix(
                            colorMatrix
                        )
                    )
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}


@Composable
fun bottomSheetContent(
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
                Row {
                    Column {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = comic.title ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .widthIn(min = if (comic.title == null) 200.dp else 0.dp)
                                    .placeholder(comic.title == null)
                            )
                            Text(
                                text = "(${comic.id})",
                                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Text(
                            text = comic.pubDate.let { if (it != null) dateFormat.format(it.time) else "00/00/0000" },
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            modifier = Modifier.placeholder(comic.pubDate == null)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()
                ) {
                    var icon = Icons.Outlined.StarOutline
                    var tint = Gray400

                    if (isFavorite) {
                        icon = Icons.Filled.Star
                        tint = Amber500
                    }
                    Icon(icon,
                        "Star",
                        tint = tint,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                (if (!isFavorite) setFavorite else removeFavorite)(
                                    comic
                                )
                            })
                }
            }
            Text(
                text = comic.description
                    ?: "I am a description text. If you see me, something isn't working as intended. Written at 2:36 a.m.",
                modifier = Modifier.placeholder(comic.description == null)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
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
                        icon, "Star", tint = tint
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
                        Icons.Default.Share, "Star"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun SingleViewContentStateful(
    singleViewModel: SingleComicViewModel, mainViewModel: MainViewModel, navigateHome: () -> Unit
) {
    val currentComic = singleViewModel.currentComic
    val favoriteList = mainViewModel.favoriteList
    if (currentComic != null) {
        SingleViewContent(
            comic = currentComic,
            isFavorite = favoriteList.contains(currentComic.id) && !mainViewModel.hideFavoritesList.contains(
                currentComic.id
            ),
            dateFormat = mainViewModel.dateFormat,
            setFavorite = mainViewModel::addFavorite,
            removeFavorite = mainViewModel::removeFavorite,
            setNumber = { singleViewModel.currentComic = mainViewModel.loadComic(it) },
            maxNumber = mainViewModel.latestComicNumber,
            colorMatrix = ColorMatrix(mainViewModel.matrix.toColorMatrix()),
            getNumber = singleViewModel::getNumber,
            startActivity = { mainViewModel.startActivity(it) },
            navigateHome = navigateHome
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}