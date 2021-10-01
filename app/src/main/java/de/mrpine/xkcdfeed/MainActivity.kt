package de.mrpine.xkcdfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import de.mrpine.xkcdfeed.composeElemts.MainContent
import de.mrpine.xkcdfeed.composeElemts.sheetContent
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme

private const val TAG = "MainActivity"

@ExperimentalPagerApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            XKCDFeedTheme {
                NavHost(navController = navController, startDestination = "mainView") {
                    composable("mainView") { MainContent() }
                    composable("test") { Text("hello") }
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
        MainContent()
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