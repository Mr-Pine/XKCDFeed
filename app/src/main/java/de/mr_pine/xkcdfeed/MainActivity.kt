package de.mr_pine.xkcdfeed

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
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
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import de.mr_pine.xkcdfeed.composables.main.MainContent
import de.mr_pine.xkcdfeed.composables.single.SingleViewContentStateful
import de.mr_pine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

private const val TAG = "MainActivity"

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user-data")

@ExperimentalPagerApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    @ObsoleteCoroutinesApi
    @ExperimentalFoundationApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ${intent.data}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.new_comic_channel_name)
            val descriptionText = getString(R.string.new_comic_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(getString(R.string.new_comic_channel_id), name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        Firebase.messaging.subscribeToTopic("newComic")

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
                    singleComicViewModel::addToComicCache,
                    singleComicViewModel::setComicCacheImageLoaded
                )
            ).get(MainViewModel::class.java)

            if (mainViewModel.latestComicsList.isEmpty()) {
                mainViewModel.addLatestComics(4, this)

                scope.launch (Dispatchers.IO){
                    val favList = mainViewModel.favoriteListFlow.first()
                    for (i in favList) {
                        mainViewModel.addComicSync(i, this@MainActivity, MainViewModel.Tab.FAVORITES)
                    }
                    Log.d(TAG, "onCreate: ${Thread.currentThread().name}")
                }
            }


            XKCDFeedTheme {
                val rootUri = "xkcd.com"
                NavHost(navController = navController, startDestination = "mainView") {
                    composable("mainView", deepLinks = listOf(navDeepLink { uriPattern = rootUri })) {
                        MainContent(mainViewModel) {
                            navController.navigate("singleView/${it.id}")
                        }
                    }
                    composable("test") { Text("hello") }
                    composable(
                        route = "singleView/{number}",
                        arguments = listOf(navArgument("number") { type = NavType.IntType }),
                        deepLinks = listOf(navDeepLink { uriPattern = "$rootUri/{number}" })
                    ) { backStackEntry ->

                        val comicNumber = backStackEntry.arguments?.getInt("number")
                        singleComicViewModel.setComic(comicNumber ?: mainViewModel.latestComicNumber, this@MainActivity)
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