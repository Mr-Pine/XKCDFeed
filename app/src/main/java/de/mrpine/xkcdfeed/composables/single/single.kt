package de.mrpine.xkcdfeed.composables.single

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mrpine.xkcdfeed.XKCDComic
import de.mrpine.xkcdfeed.ui.theme.Amber500
import de.mrpine.xkcdfeed.ui.theme.Gray400
import de.mrpine.xkcdfeed.ui.theme.XKCDFeedTheme
import java.text.DateFormat
import java.util.*

@ExperimentalMaterialApi
@Composable
fun SingleViewContent(
    comic: XKCDComic,
    isFavorite: Boolean,
    dateFormat: DateFormat,
    setFavorite: (XKCDComic) -> Unit,
    removeFavorite: (XKCDComic) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(contentColor = Color.White, contentPadding = PaddingValues(5.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.FirstPage, "First Comic")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.ArrowBack, "Previous Comic")
                    }
                    OutlinedTextField(
                        value = comic.id.toString(),
                        modifier = Modifier
                            .width(100.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        onValueChange = {},
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.White,
                            focusedBorderColor = Color.White,
                            cursorColor = Color.White,
                            backgroundColor = MaterialTheme.colors.primaryVariant
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.ArrowForward, "Next Comic")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.LastPage, "Latest Comic")
                    }

                }
            }
        },
        sheetPeekHeight = 77.dp,
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        ),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp, 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                MaterialTheme.colors.primary
                            )
                    )

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row() {
                        Column() {
                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = comic.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "(${comic.id})",
                                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            Text(
                                text = dateFormat.format(comic.pubDate.time),
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var icon = Icons.Outlined.StarOutline
                        var tint = Gray400

                        if (isFavorite) {
                            icon = Icons.Filled.Star
                            tint = Amber500
                        }
                        Icon(
                            icon,
                            "Star",
                            tint = tint,
                            modifier = Modifier.clickable {
                                (if (!isFavorite) setFavorite else removeFavorite)(
                                    comic
                                )
                            }
                        )
                    }
                }
                Text(text = comic.description)
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Button(onClick = { /*TODO*/ }, modifier = Modifier.width(140.dp)) {
                        var icon = Icons.Outlined.StarOutline
                        var tint = Gray400

                        if (isFavorite) {
                            icon = Icons.Filled.Star
                            tint = Amber500
                        }
                        Icon(
                            icon,
                            "Star",
                            tint = tint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFavorite) "Remove" else "Add")
                    }
                    Button(onClick = { /*TODO*/ }, modifier = Modifier.width(140.dp)) {
                        Icon(
                            Icons.Default.Share,
                            "Star"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                }
            }
        },
    ) {
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
fun SinglePreview() {
    XKCDFeedTheme(darkTheme = false) {
        val comic = XKCDComic(
            "Comet Visitor",
            "https://imgs.xkcd.com/comics/comet_visitor.png",
            2524,
            Calendar.getInstance(),
            "this is a description I am too lazy to copy",
            rememberCoroutineScope(),
            {}
        )
        SingleViewContent(comic = comic, isFavorite = true, DateFormat.getDateInstance(), {}, {})

    }
}