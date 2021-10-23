package de.mrpine.xkcdfeed

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import de.mrpine.xkcdfeed.composables.main.MainContent
import de.mrpine.xkcdfeed.composables.single.SingleViewContentStateful
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user-data")

@ExperimentalPagerApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    @ExperimentalComposeUiApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()

            val singleComicViewModel: SingleComicViewModel = viewModel()

            val mainViewModel: MainViewModel = ViewModelProvider(
                this,
                MainViewModelFactory(
                    userDataStore,
                    DateFormat.getDateFormat(this),
                    this::startActivity,
                    navController::navigate,
                    singleComicViewModel::addToComicCache
                )
            ).get(MainViewModel::class.java)

            if (mainViewModel.latestComicsList.isEmpty()) {
                mainViewModel.addLatestComics(4, this)

                scope.launch {
                    val favList = mainViewModel.favoriteListFlow.first()
                    for (i in favList) {
                        mainViewModel.addComic(i, this@MainActivity, MainViewModel.Tab.FAVORITES)
                    }
                }
            }


            XKCDFeedTheme {
                NavHost(navController = navController, startDestination = "mainView") {
                    composable("mainView") {
                        MainContent(mainViewModel) {
                            singleComicViewModel.setComic(it)
                            navController.navigate("singleView/${it.id}")
                        }
                    }
                    composable("test") { Text("hello") }
                    composable(
                        route = "singleView/{number}",
                        arguments = listOf(navArgument("number") { type = NavType.IntType })
                    ) { backStackEntry ->

                        val comicNumber = backStackEntry.arguments?.getInt("number")
                        SingleViewContentStateful(
                            mainViewModel = mainViewModel,
                            singleViewModel = singleComicViewModel,
                            setComic = { singleComicViewModel.setComic(it, this@MainActivity)},
                            navigate = navController::navigate
                        )
                    }
                    composable(
                        route = "singleView"
                    ) {
                        if (singleComicViewModel.currentComic.value == null) {
                            singleComicViewModel.setComic(
                                mainViewModel.latestComicNumber,
                                this@MainActivity
                            )
                        }
                        SingleViewContentStateful(
                            mainViewModel = mainViewModel,
                            singleViewModel = singleComicViewModel,
                            setComic = { singleComicViewModel.setComic(it, this@MainActivity) },
                            navigate = navController::navigate
                        )
                    }
                }
            }
        }
    }
}