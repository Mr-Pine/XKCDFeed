package de.mrpine.xkcdfeed.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import de.mrpine.xkcdfeed.MainViewModel
import de.mrpine.xkcdfeed.XKCDComic
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun MainContent(viewModel: MainViewModel) { //navRoute = mainView
    val favSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    SheetLayout(favSheetState, viewModel)
}


//<editor-fold desc="Bottom Sheet Composables">
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun SheetLayout(state: ModalBottomSheetState, viewModel: MainViewModel) {
    ModalBottomSheetLayout(
        sheetContent = sheetContent(),
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetState = state,
        scrimColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.7f)
    ) {
        MainScaffold(viewModel)
    }
}

@Composable
fun sheetContent(): @Composable (ColumnScope.() -> Unit) {
    return {
        Surface(modifier = Modifier.padding(8.dp)) {
            Column {
                val iconDimensions = 30.dp
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .width(iconDimensions)
                            .height(iconDimensions)
                            .padding(end = 6.dp)
                    ) {
                        Icon(Icons.Default.Star, "Star")
                    }
                    Text("Fav")
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
fun MainScaffold(viewModel: MainViewModel) {

    Scaffold(topBar = { TopAppBar() }) {
        TabbedContent(viewModel)
    }
}


@Composable
fun TopAppBar() {
    return TopAppBar(
        title = { Text(text = "XKCDFeed") },
        contentColor = Color.White,
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.primary
    )
}
//</editor-fold>

//<editor-fold desc="Tabs">
//<editor-fold desc="Tab Main Layout">
@ExperimentalPagerApi
@Composable
fun TabbedContent(viewModel: MainViewModel) {

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
            TabContent(pagerState = tabPagerState, pagerScope = this, page, viewModel)
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabContent(
    pagerState: PagerState,
    pagerScope: PagerScope,
    pageIndex: Int,
    viewModel: MainViewModel
) {
    when (pageIndex) {
        0 -> Tab1(viewModel)
        1 -> Tab2(viewModel)
        else -> Text(text = "error occurred")
    }
}
//</editor-fold>

private const val TAG = "mainComposable"

//<editor-fold desc="Tab Pages">
@Composable
fun Tab1(viewModel: MainViewModel) {

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {}) {
            Icon(Icons.Default.Star, "Star")
        }
    }) {
        ComicList(list = viewModel.latestComicsList, imagesLoadedMap = viewModel.latestImagesLoadedMap, viewModel = viewModel)
    }

}

@Composable
fun Tab2(viewModel: MainViewModel) {
    ComicList(list = viewModel.favoriteComicsList, imagesLoadedMap = viewModel.favoriteImagesLoadedMap, viewModel = viewModel)
}

@Composable
fun ComicList(list: List<XKCDComic>, imagesLoadedMap: MutableMap<Int, Boolean>, viewModel: MainViewModel) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list) { item ->
            ComicCard(
                item,
                viewModel.dateFormat,
                imagesLoadedMap,
                viewModel.favoriteListFlow.collectAsState(initial = mutableListOf()).value,
                viewModel::addFavorite,
                viewModel::removeFavorite
            )
        }
    }
}
//</editor-fold>
//</editor-fold>