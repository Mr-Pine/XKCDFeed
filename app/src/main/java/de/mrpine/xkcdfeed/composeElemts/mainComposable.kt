package de.mrpine.xkcdfeed.composeElemts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun MainContent() { //navRoute = mainView
    val favSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    fun showFavSheet() {
        scope.launch { favSheetState.show() }
    }

    SheetLayout(
        favSheetState,
        ::showFavSheet
    )
}


//<editor-fold desc="Bottom Sheet Composables">
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun SheetLayout(
    state: ModalBottomSheetState,
    showFavSheet: () -> Unit
) {
    ModalBottomSheetLayout(
        sheetContent = sheetContent(),
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetState = state,
        scrimColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.7f)
    ) {
        MainScaffold(showFavSheet)
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
fun MainScaffold(showFavSheet: () -> Unit) {

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = showFavSheet) {
            Icon(
                Icons.Default.Star,
                "favourite"
            )
        }
    }, topBar = { TopAppBar() }) {
        TabbedContent()
    }
}

@Composable
fun TopAppBar() {
    return TopAppBar(
        title = { Text(text = "XKCDFeed") },
        contentColor = Color.White,
        elevation = 0.dp
    )
}
//</editor-fold>

//<editor-fold desc="Tabs">
@ExperimentalPagerApi
@Composable
fun TabbedContent() {

    val tabPagerState = rememberPagerState(0)
    val scope = rememberCoroutineScope()

    Column {
        TabRow(
            selectedTabIndex = tabPagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(tabPagerState, tabPositions)
                )
            }
        ) {
            Tab(
                text = { Text("lol 1") },
                selected = tabPagerState.currentPage == 0,
                onClick = { scope.launch { tabPagerState.animateScrollToPage(0) } },
                selectedContentColor = Color(0xFFFFFFFF),
                unselectedContentColor = Color(0x80FFFFFF)
            )
            Tab(
                text = { Text("lol 2") },
                selected = tabPagerState.currentPage == 1,
                onClick = { scope.launch { tabPagerState.animateScrollToPage(1) } },
                selectedContentColor = Color(0xFFFFFFFF),
                unselectedContentColor = Color(0x80FFFFFF)
            )
        }
        HorizontalPager(count = 2, state = tabPagerState) { page ->
            TabContent(pagerState = tabPagerState, pagerScope = this, page)
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabContent(pagerState: PagerState, pagerScope: PagerScope, pageIndex: Int) {
    Column() {
        Text("lol Seite ${pageIndex + 1}")
    }
}
//</editor-fold>

class MainViewModel : ViewModel() {
}