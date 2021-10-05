package de.mrpine.xkcdfeed

import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import de.mrpine.xkcdfeed.composables.MainContent
import de.mrpine.xkcdfeed.composables.MainViewModel
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme

private const val TAG = "MainActivity"

@ExperimentalPagerApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            val viewModel: MainViewModel = viewModel()
            viewModel.dateFormat = DateFormat.getDateFormat(this)
            if (viewModel.comicList.isEmpty()) {
                viewModel.addComic(202, this)
                viewModel.addLatestComics(4, this)
            }

            XKCDFeedTheme {
                NavHost(navController = navController, startDestination = "mainView") {
                    composable("mainView") { MainContent(viewModel) }
                    composable("test") { Text("hello") }
                }
            }
        }


    }
}