package de.mrpine.xkcdfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

@ExperimentalPagerApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            XKCDFeedTheme() {
                NavHost(navController = navController, startDestination = "mainView") {
                    composable("mainView") { mainContent() }
                    composable("test") { Text("hello") }
                }
            }
        }
    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun mainContent(mainViewModel: MainViewModel = viewModel()) {
    val favSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope();

    fun showFavSheet() {
        scope.launch { favSheetState.show() }
    }

    val tabIndex: Int by mainViewModel.tabIndex.observeAsState(1)

    sheetLayout(
        favSheetState,
        ::showFavSheet
    )
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun mainScaffold(showFavSheet: () -> Unit) {

    val tabPagerState = rememberPagerState(0)
    val scope = rememberCoroutineScope()

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = showFavSheet) {
            Icon(
                Icons.Default.Star,
                "favourite"
            )
        }
    }, topBar = { topAppBar() }) {
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
                Text("lol Seite $page")
            }
        }
    }
}

@Composable
fun topAppBar() {
    return TopAppBar(
        title = { Text(text = "XKCDFeed") },
        contentColor = Color.White,
        elevation = 0.dp
    )
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun sheetLayout(
    state: ModalBottomSheetState,
    showFavSheet: () -> Unit
) {
    ModalBottomSheetLayout(
        sheetContent = sheetContent(),
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetState = state,
        scrimColor = MaterialTheme.colors.primaryVariant.copy(alpha = 0.7f)
    ) {
        mainScaffold(showFavSheet)
    }
}

@Composable
fun sheetContent(): @Composable() (ColumnScope.() -> Unit) {
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

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Preview(showBackground = true, name = "Main Preview")
@Composable
fun DefaultPreview() {
    XKCDFeedTheme(darkTheme = false) {
        mainContent()
    }
}

@ExperimentalMaterialApi
@Preview(name = "Sheet Preview", showBackground = true)
@Composable
fun SheetPreview() {
    XKCDFeedTheme(darkTheme = false) {
        Column(content = sheetContent())
    }
}

@ExperimentalMaterialApi
class MainViewModel : ViewModel() {
    private val _tabIndex = MutableLiveData(0)
    val tabIndex: LiveData<Int> = _tabIndex

    fun setTabIndex(index: Int) {
        _tabIndex.value = index
    }
}