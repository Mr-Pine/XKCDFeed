package de.mr_pine.xkcdfeed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(

    primary = Gray900Light,
    primaryVariant = Gray900,
    secondary = Gray500,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    surface = Gray900,
    onSurface = Color.White
)

private val LightColorPalette = lightColors(


    primary = Gray500,
    primaryVariant = Gray500Dark,
    secondary = Gray700,
    onPrimary = Color.White,
    onSecondary = Color.White,
    surface = Gray400Light

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