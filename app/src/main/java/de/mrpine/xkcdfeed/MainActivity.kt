package de.mrpine.xkcdfeed

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import de.mrpine.xkcdfeed.composables.main.MainContent
import de.mrpine.xkcdfeed.composables.single.SinglePreview
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user-data")

@ExperimentalPagerApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()

            val viewModel: MainViewModel = ViewModelProvider(
                this,
                MainViewModelFactory(
                    userDataStore,
                    DateFormat.getDateFormat(this),
                    this::startActivity,
                    navController::navigate
                )
            ).get(MainViewModel::class.java)

            if (viewModel.latestComicsList.isEmpty()) {
                viewModel.addLatestComics(4, this)

                scope.launch {
                    val favList = viewModel.favoriteListFlow.first()
                    for (i in favList) {
                        viewModel.addComic(i, this@MainActivity, MainViewModel.Tab.FAVORITES)
                    }
                }
            }

            XKCDFeedTheme {
                NavHost(navController = navController, startDestination = "mainView") {
                    composable("mainView") { MainContent(viewModel) }
                    composable("test") { Text("hello") }
                    composable(
                        route = "singleView/{number}",
                        arguments = listOf(navArgument("number") { type = NavType.IntType })
                    ) { backStackEntry ->

                        val comicNumber = backStackEntry.arguments?.getInt("number")
                        if (comicNumber != null) {
                            SinglePreview()
                        }else{
                            Text(text = "An Error occured", color = Color.Red, modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }


    }
}