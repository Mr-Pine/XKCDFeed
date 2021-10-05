package de.mrpine.xkcdfeed.composables

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.*
import de.mrpine.xkcdfeed.XKCDComic
import de.mrpine.xkcdfeed.getHttpJSON
import kotlinx.coroutines.launch
import java.text.DateFormat

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
        elevation = 0.dp
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
        1 -> Tab2()
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
        ComicList(list = viewModel.comicList, viewModel = viewModel)
    }
}

@Composable
fun Tab2() {
    Text(text = "tab2")
}

@Composable
fun ComicList(list: List<XKCDComic>, viewModel: MainViewModel) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list) { item ->
            ComicCard(item, viewModel)
        }
    }
}
//</editor-fold>
//</editor-fold>


class MainViewModel : ViewModel() {
    var comicList = mutableStateListOf<XKCDComic>()
    var imageLoadedMap = mutableStateMapOf<Int, Boolean>()

    lateinit var dateFormat: DateFormat

    private fun addToComicList(item: XKCDComic) {
        comicList.add(item)
        comicList.sortByDescending { it.id }
    }

    fun addComic(number: Int, context: Context) {
        viewModelScope.launch {
            imageLoadedMap[number] = false
            XKCDComic.getComic(number = number, coroutineScope = viewModelScope, context = context, onImageLoaded = {imageLoadedMap[number] = true}) {
                addToComicList(it)
            }
        }
    }

    private fun addComicSync(number: Int, context: Context) {
        imageLoadedMap[number] = false
        XKCDComic.getComic(number = number, coroutineScope = viewModelScope, context = context, onImageLoaded = {imageLoadedMap[number] = true}) {
            addToComicList(it)
        }
    }

    fun addLatestComics(count: Int, context: Context) {
        viewModelScope.launch {
            getHttpJSON("https://xkcd.com/info.0.json", context){
                val number = it.getInt("num")
                for (i in number downTo (number - (count - 1))) {
                    addComicSync(i, context)
                }
            }
        }
    }
}