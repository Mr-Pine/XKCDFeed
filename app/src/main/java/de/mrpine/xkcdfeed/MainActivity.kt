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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import de.mrpine.xkcdfeed.ui.theme.Shapes
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MainContent(mainViewModel: MainViewModel = MainViewModel()) {
    val favSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope();

    fun showFavSheet() {
        scope.launch { favSheetState.show() }
    }

    XKCDFeedTheme {
        Surface {
            SheetLayout(favSheetState, ::showFavSheet)
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MainScaffold(showFavSheet: () -> Unit) {

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = showFavSheet) {
            Icon(Icons.Default.Star, "favourite")
        }
    }) {
        Text(text = "hallöchen")
    }
}

@ExperimentalMaterialApi
@Composable
fun SheetLayout(state: ModalBottomSheetState, showFavSheet: () -> Unit) {
    ModalBottomSheetLayout(
        sheetContent = sheetContent(),
        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        sheetState = state
    ) {
        MainScaffold(showFavSheet)
    }
}

@Composable
fun sheetContent(): @Composable() (ColumnScope.() -> Unit) {
    return {
        Surface(modifier = Modifier.padding(8.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .padding(end = 6.dp)) {
                        Icon(Icons.Default.Star, "Star")
                    }
                    Text("Fav")
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .padding(end = 6.dp)) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    Text(text = "Share")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@ExperimentalMaterialApi
@Preview(showBackground = true, name = "Main Preview")
@Composable
fun DefaultPreview() {
    XKCDFeedTheme(darkTheme = true) {
        MainContent()
    }
}

@ExperimentalMaterialApi
@Preview(name = "Sheet Preview", showBackground = true)
@Composable
fun SheetPreview() {
    XKCDFeedTheme {
        Column(content = sheetContent())
    }
}

@ExperimentalMaterialApi
class MainViewModel : ViewModel() {
}