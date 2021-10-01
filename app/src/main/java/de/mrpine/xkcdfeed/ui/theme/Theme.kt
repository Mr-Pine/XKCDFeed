package de.mrpine.xkcdfeed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    /*primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,*/

    primary = Gray900,
    primaryVariant = Gray900Dark,
    secondary = Gray500,
    onPrimary = Color.White,
    onSecondary = Color.Black,

)

private val LightColorPalette = lightColors(
    /*primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200*/


    primary = Gray500,
    primaryVariant = Gray500Dark,
    secondary = Gray700,
    onPrimary = Color.Black,
    onSecondary = Color.White

    /* Other default colors to override
background = Color.White,
surface = Color.White,
onPrimary = Color.White,
onSecondary = Color.Black,
onBackground = Color.Black,
onSurface = Color.Black,
*/
)


@Composable
fun XKCDFeedTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {


    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(colors.primaryVariant)

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}