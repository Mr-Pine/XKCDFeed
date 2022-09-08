package de.mr_pine.xkcdfeed.composables.main

import android.content.Intent
import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import de.mr_pine.xkcdfeed.MainViewModel
import de.mr_pine.xkcdfeed.XKCDComic
import de.mr_pine.xkcdfeed.ui.theme.Amber500
import de.mr_pine.xkcdfeed.ui.theme.Gray400
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    Scaffold(topBar = { TopAppBar { viewModel.navigateTo(it) } }) { paddingValues ->
        TabbedContent(viewModel, showSingleComic, paddingValues)
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
fun TabbedContent(viewModel: MainViewModel, showSingleComic: (XKCDComic) -> Unit, paddingValues: PaddingValues) {

    val tabPagerState = rememberPagerState(0)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(paddingValues)) {
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
    }) { paddingValues ->
        ComicList(
            list = viewModel.latestComicsList,
            currentTab = MainViewModel.Tab.LATEST,
            viewModel = viewModel,
            showSingleComic = showSingleComic,
            paddingValues = paddingValues
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
    }) { paddingValues ->
        ComicList(
            list = viewModel.favoriteComicsList,
            currentTab = MainViewModel.Tab.FAVORITES,
            viewModel = viewModel,
            showSingleComic = showSingleComic,
            state = viewModel.favListState,
            paddingValues = paddingValues
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun ComicList(
    list: List<XKCDComic>,
    viewModel: MainViewModel,
    currentTab: MainViewModel.Tab,
    showSingleComic: (XKCDComic) -> Unit,
    state: LazyListState? = null,
    paddingValues: PaddingValues
) {
    LazyColumn(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = state ?: rememberLazyListState()
    ) {
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
                viewModel.favoriteList - viewModel.hideFavoritesList,
                currentTab,
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