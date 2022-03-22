package de.mr_pine.xkcdfeed.composables.main

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.set
import com.google.accompanist.pager.*
import de.mr_pine.xkcdfeed.MainViewModel
import de.mr_pine.xkcdfeed.R
import de.mr_pine.xkcdfeed.XKCDComic
import de.mr_pine.xkcdfeed.composables.*
import de.mr_pine.xkcdfeed.ui.theme.Amber500
import de.mr_pine.xkcdfeed.ui.theme.Gray400
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.random.Random

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun MainContent(
    viewModel: MainViewModel,
    showSingleComic: (XKCDComic) -> Unit
) { //navRoute = mainView
    val favSheetState = viewModel.modalBottomSheetState
    val scope = rememberCoroutineScope()

    SheetLayout(favSheetState, viewModel, scope, showSingleComic)
}


//<editor-fold desc="Bottom Sheet Composables">
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun SheetLayout(
    state: ModalBottomSheetState,
    viewModel: MainViewModel,
    scope: CoroutineScope,
    showSingleComic: (XKCDComic) -> Unit
) {
    val favoriteList = viewModel.favoriteList
    val currentComic = viewModel.currentBottomSheetXKCDComic.value
    val isFav = if (currentComic != null) favoriteList.contains(currentComic.id) else false
    ModalBottomSheetLayout(
        sheetContent = sheetContent(
            currentComic = currentComic,
            isFav = isFav,
            onClickFav = {
                scope.launch {
                    if (currentComic != null) {
                        if (!favoriteList.contains(currentComic.id)) viewModel.addFavorite(
                            currentComic
                        ) else viewModel.removeFavorite(currentComic)
                    }
                    viewModel.hideBottomSheet()
                }
            },
            onClickShare = {
                scope.launch {
                    if (currentComic != null) {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "https://xkcd.com/${currentComic.id}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        viewModel.startActivity(shareIntent)
                    }
                    viewModel.hideBottomSheet()
                }
            }),
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetState = state,
        scrimColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.7f)
    ) {
        MainScaffold(viewModel, showSingleComic)
    }
}

@Composable
fun sheetContent(
    isFav: Boolean,
    onClickFav: (xkcdComic: XKCDComic) -> Unit,
    onClickShare: (xkcdComic: XKCDComic) -> Unit,
    currentComic: XKCDComic?
): @Composable (ColumnScope.() -> Unit) {
    return {
        Surface(modifier = Modifier.padding(8.dp)) {
            Column {
                val iconDimensions = 30.dp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = {
                            if (currentComic != null) {
                                onClickFav(currentComic)
                            }
                        })
                        .fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier
                            .width(iconDimensions)
                            .height(iconDimensions)
                            .padding(end = 6.dp)
                    ) {
                        var icon = Icons.Outlined.StarOutline
                        var tint = Gray400

                        if (isFav) {
                            icon = Icons.Filled.Star
                            tint = Amber500
                        }
                        Icon(
                            icon,
                            "Star",
                            tint = tint
                        )
                    }
                    Text("Fav")
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = {
                            if (currentComic != null) {
                                onClickShare(currentComic)
                            }
                        })
                        .fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier
                            .width(iconDimensions)
                            .height(iconDimensions)
                            .padding(end = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    Text(text = "Share")
                }
            }
        }
    }
}
//</editor-fold>

//<editor-fold desc="Main Scaffold with Top Bar">
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun MainScaffold(viewModel: MainViewModel, showSingleComic: (XKCDComic) -> Unit) {

    Scaffold(topBar = { TopAppBar { viewModel.navigateTo(it) } }) {
        TabbedContent(viewModel, showSingleComic)
    }
}


@Composable
fun TopAppBar(navigate: (String) -> Unit) {
    return TopAppBar(
        title = { Text(text = "XKCDFeed") },
        contentColor = Color.White,
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { navigate("settings") }) {
                Icon(Icons.Default.Settings, "Settings")
            }
        }
    )
}
//</editor-fold>

//<editor-fold desc="Tabs">
//<editor-fold desc="Tab Main Layout">
@ExperimentalPagerApi
@Composable
fun TabbedContent(viewModel: MainViewModel, showSingleComic: (XKCDComic) -> Unit) {

    val tabPagerState = rememberPagerState(0)
    val scope = rememberCoroutineScope()

    Column() {
        TabRow(
            selectedTabIndex = tabPagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(tabPagerState, tabPositions)
                )
            },
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Tab(
                text = { Text("Latest") },
                selected = tabPagerState.currentPage == 0,
                onClick = { scope.launch { tabPagerState.animateScrollToPage(0) } },
                selectedContentColor = Color(0xFFFFFFFF),
                unselectedContentColor = Color(0x80FFFFFF)
            )
            Tab(
                text = { Text("Favorites") },
                selected = tabPagerState.currentPage == 1,
                onClick = { scope.launch { tabPagerState.animateScrollToPage(1) } },
                selectedContentColor = Color(0xFFFFFFFF),
                unselectedContentColor = Color(0x80FFFFFF)
            )
        }
        HorizontalPager(count = 2, state = tabPagerState) { page ->
            TabContent(
                pagerState = tabPagerState,
                pagerScope = this,
                page,
                viewModel,
                scope = scope,
                showSingleComic
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabContent(
    pagerState: PagerState,
    pagerScope: PagerScope,
    pageIndex: Int,
    viewModel: MainViewModel,
    scope: CoroutineScope,
    showSingleComic: (XKCDComic) -> Unit
) {
    when (pageIndex) {
        0 -> Tab1(viewModel, showSingleComic)
        1 -> Tab2(viewModel, scope, showSingleComic)
        else -> Text(text = "error occurred")
    }
}
//</editor-fold>

private const val TAG = "mainComposable"

//<editor-fold desc="Tab Pages">
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Tab1(viewModel: MainViewModel, showSingleComic: (XKCDComic) -> Unit) {

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            Log.d(
                TAG,
                "Tab1: ${viewModel.favoriteImagesLoadedMap.keys}"
            )
            viewModel.navigateTo("singleView")
        }) {
            Icon(Icons.Default.History, "History")
        }
    }) {
        ComicList(
            list = viewModel.latestComicsList,
            viewModel = viewModel,
            showSingleComic = showSingleComic
        )
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Tab2(viewModel: MainViewModel, scope: CoroutineScope, showSingleComic: (XKCDComic) -> Unit) {
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            val randomIndex = Random.nextInt(viewModel.favListState.layoutInfo.totalItemsCount)
            scope.launch {
                viewModel.scrollToFavItem(randomIndex)
            }
        }) {
            Icon(Icons.Default.Shuffle, "Shuffle")
        }
    }) {
        ComicList(
            list = viewModel.favoriteComicsList,
            viewModel = viewModel,
            showSingleComic = showSingleComic,
            state = viewModel.favListState
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun ComicList(
    list: List<XKCDComic>,
    viewModel: MainViewModel,
    showSingleComic: (XKCDComic) -> Unit,
    state: LazyListState? = null
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = state ?: rememberLazyListState()
    ) {
        item {

            val lightBitmap =
                ImageBitmap.imageResource(id = R.drawable.jji_colorchart).asAndroidBitmap()

            var darkBitmap by remember {
                mutableStateOf(lightBitmap.copy(lightBitmap.config, true))
            }

            var wR by remember { mutableStateOf(50f/*0.6612f*//*3.311f*/) }
            var wG by remember { mutableStateOf(10f/*0.4018f*//*0.733f*/) }
            var wB by remember { mutableStateOf(1f/*0.8680f*//*4.554f*/) }

            val matrix by remember(wR, wG, wB){
                var matrix = identityMatrix(4,4)
                matrix = matrix.matrixMultiply(arrayOf(
                    floatArrayOf(-1f, 0f, 0f, 0f),
                    floatArrayOf(0f, -1f, 0f, 0f),
                    floatArrayOf(0f, 0f, -1f, 0f),
                    floatArrayOf(0f, 0f, 0f, -1f),
                ))
                matrix = matrix.matrixAdd(arrayOf(
                    floatArrayOf(0f, 0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f, 0f),
                    floatArrayOf(0f, 0f, 0f, 0f),
                    floatArrayOf(255f, 255f, 255f, 0f),
                ))

                //HSV 180 rotation
                matrix = matrix.matrixMultiply(xRotation(cos = 1/ sqrt(2f), sin = 1/ sqrt(2f)))
                Log.d(TAG, "\n${matrix.matrixToString()}")
                matrix = matrix.matrixMultiply(yRotation(cos = sqrt(2/3f), sin = -sqrt(1/3f)))
                val transformedWeights = arrayOf(floatArrayOf(wR, wG, wB)).matrixMultiply(matrix.cutTo(3,3)).matrixMultiply(matrix.cutTo(3,3))
                val shearX = (transformedWeights[0][0]/transformedWeights[0][2])
                val shearY = (transformedWeights[0][1]/transformedWeights[0][2])
                matrix = matrix.matrixMultiply(shearZ(shearX, shearY))
                matrix = matrix.matrixMultiply(zRotation(PI.toFloat()))
                matrix = matrix.matrixMultiply(shearZ(-shearX, -shearY))
                matrix = matrix.matrixMultiply(yRotation(cos = sqrt(2/3f), sin = sqrt(1/3f)))
                matrix = matrix.matrixMultiply(xRotation(cos = 1/ sqrt(2f), sin = -1/ sqrt(2f)))

                return@remember mutableStateOf(ColorMatrix(matrix.toColorMatrix()))
            }

            LaunchedEffect(key1 = Unit) {
                val dark = darkBitmap
                for (y in 0 until dark.height) {
                    for (x in 0 until dark.width) {
                        val pixelColor = dark.getPixel(x, y)
                        val invertedPixelColor = android.graphics.Color.rgb(
                            255 - android.graphics.Color.red(pixelColor),
                            255 - android.graphics.Color.green(pixelColor),
                            255 - android.graphics.Color.blue(pixelColor)
                        )
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(invertedPixelColor, hsv)
                        dark[x, y] =
                            android.graphics.Color.HSVToColor(
                                floatArrayOf(
                                    (hsv[0] + 180) % 360,
                                    hsv[1],
                                    hsv[2]
                                )
                            )
                    }
                }
                darkBitmap = dark
            }

            Image(
                bitmap = darkBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Slider(value = wR, onValueChange = {wR = it}, valueRange = -3f .. 10f)
            Slider(value = wG, onValueChange = {wG = it}, valueRange = -3f .. 10f)
            Slider(value = wB, onValueChange = {wB = it}, valueRange = -3f .. 10f)
            Text(text = "hi $wR, $wG, $wB")
            Image(
                painter = painterResource(id = R.drawable.jji_colorchart),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    matrix
                )
            )
        }

        items(list.let {
            try {
                it.sortedBy { item -> -item.id }
            } catch (e: Exception) {
                it
            }
        }) { item ->
            ComicCard(
                item,
                viewModel.dateFormat,
                viewModel.favoriteList,
                viewModel::addFavorite,
                viewModel::removeFavorite,
                viewModel.matrix,
                viewModel::showBottomSheet,
                showSingleComic
            )
        }
    }
}
//</editor-fold>
//</editor-fold>