package de.mr_pine.xkcdfeed

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
import de.mr_pine.xkcdfeed.composables.settings.SettingsComposable
import de.mr_pine.xkcdfeed.composables.settings.Theme
import de.mr_pine.xkcdfeed.composables.settings.settingsDataStore
import de.mr_pine.xkcdfeed.composables.single.SingleViewContentStateful
import de.mr_pine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user-data")

class MainActivity : ComponentActivity() {

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "hi")
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ${navController.handleDeepLink(intent)}")
    }

    private lateinit var navController: NavHostController

    @OptIn(
        ExperimentalPagerApi::class,
        androidx.compose.ui.ExperimentalComposeUiApi::class,
        androidx.compose.foundation.ExperimentalFoundationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class,
        kotlinx.coroutines.ObsoleteCoroutinesApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ${intent.data}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.new_comic_channel_name)
            val descriptionText = getString(R.string.new_comic_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel =
                NotificationChannel(getString(R.string.new_comic_channel_id), name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        Firebase.messaging.subscribeToTopic("newComic")

        val themeSettingFlow = this.settingsDataStore.data.map {
            if (it[intPreferencesKey("theme")] != null) enumValues<Theme>()[it[intPreferencesKey("theme")]!!] else Theme.SYSTEM
        }

        setContent {
            val scope = rememberCoroutineScope()
            navController = rememberNavController()

            val loginViewModel: LoginViewModel = viewModel()

            val singleComicViewModel: SingleComicViewModel = viewModel()

            val mainViewModel: MainViewModel = ViewModelProvider(
                this,
                MainViewModelFactory(
                    userDataStore,
                    DateFormat.getDateFormat(this),
                    this::startActivity,
                    navController::navigate,
                    this.baseContext,
                    loginViewModel,
                    singleComicViewModel::addToComicCache,
                    singleComicViewModel::setComicCacheImageLoaded
                )
            )[MainViewModel::class.java]

            if (!mainViewModel.latestComicsInitialized) {
                mainViewModel.addLatestComics(4, this)
            }
            mainViewModel.initFavoriteList(this)

            Log.d(TAG, "onCreate/AUTH: ${loginViewModel.loadingState}")

            var lastDestination by remember { mutableStateOf("null") }

            val themeSetting by themeSettingFlow.collectAsState(initial = Theme.SYSTEM)

            XKCDFeedTheme(
                darkTheme = when (themeSetting) {
                    Theme.LIGHT -> false
                    Theme.DARK -> true
                    Theme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {

                val rootUri = "xkcd.com"
                NavHost(navController = navController, startDestination = "mainView") {
                    composable(
                        "mainView", deepLinks = listOf(
                            navDeepLink { uriPattern = rootUri },
                            navDeepLink { uriPattern = "$rootUri/" },
                            navDeepLink { uriPattern = "www.$rootUri" },
                            navDeepLink { uriPattern = "www.$rootUri/" },
                        )
                    ) {
                        MainContent(mainViewModel) {
                            navController.navigate("singleView/${it.id}")
                        }
                        lastDestination = "mainView"
                    }
                    composable("test") { Text("hello") }
                    composable(
                        route = "singleView/{number}",
                        arguments = listOf(navArgument("number") { type = NavType.IntType }),
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "$rootUri/{number}/" },
                            navDeepLink { uriPattern = "$rootUri/{number}" },
                            navDeepLink { uriPattern = "www.$rootUri/{number}/" },
                            navDeepLink { uriPattern = "www.$rootUri/{number}" },
                        )
                    ) { backStackEntry ->
                        val comicNumber = backStackEntry.arguments?.getInt("number")

                        if (lastDestination != "singleView") singleComicViewModel.currentComic =
                            comicNumber?.let { mainViewModel.loadComic(it) }

                        SingleViewContentStateful(
                            mainViewModel = mainViewModel,
                            singleViewModel = singleComicViewModel,
                            setComic = { singleComicViewModel.setComic(it, this@MainActivity) },
                            navigate = navController::navigate
                        )
                        lastDestination = "singleView"
                    }
                    composable(
                        route = "singleView"
                    ) {
                        if (singleComicViewModel.currentComic == null) {
                            singleComicViewModel.currentComic =
                                mainViewModel.loadComic(mainViewModel.latestComicNumber)
                        }
                        SingleViewContentStateful(
                            mainViewModel = mainViewModel,
                            singleViewModel = singleComicViewModel,
                            setComic = { singleComicViewModel.setComic(it, this@MainActivity) },
                            navigate = navController::navigate
                        )

                        lastDestination = "singleView"
                    }
                    composable(route = "settings") {
                        SettingsComposable(
                            navigateBack = { navController.navigateUp() },
                            loginViewModel = loginViewModel,
                            mainViewModel = mainViewModel,
                            context = this@MainActivity.baseContext,
                            onLoginChanged = {
                                scope.launch {
                                    mainViewModel.initFavoriteList(
                                        this@MainActivity,
                                        true,
                                        if (loginViewModel.signedIn) MainViewModel.ClearType.FIREBASE else MainViewModel.ClearType.LOCAL
                                    )
                                }
                            }
                        )

                        lastDestination = "settings"
                    }
                }
            }
        }
    }
}