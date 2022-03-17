package de.mr_pine.xkcdfeed.composables.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SettingsComposable(context: Context, navigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Back")
                    }
                }
            )
        }
    ) {
        Settings(context) {
            settingsSection {
                settingsItem("hi", icon = Icons.Outlined.Cloud, description = "Does a thing")
                settingsItem("test", description = "Does another Thing")
            }
            settingsSection("lol2") {
                settingsItem("hi2", "hi2", icon = Icons.Outlined.CloudOff)
                settingsItem("test2", "test2")
                radioSettingsItem("Theme", "theme", Icons.Filled.Tonality, Theme.SYSTEM)
            }
        }
    }
}

@Composable
fun SettingsList(items: List<List<@Composable () -> Unit>>) {
    LazyColumn {
        item {
            Row(modifier = Modifier.padding(top = 5.dp)) {}
        }
        itemsIndexed(items) { index, currentSection ->
            for (currentItem in currentSection) {
                currentItem()
            }
            if (index != items.lastIndex) {
                Divider(modifier = Modifier.padding(vertical = 10.dp))
            }
        }
    }
}

@Composable
fun Settings(context: Context, settingsContent: Settings.() -> Unit) {
    val settings by remember { mutableStateOf(Settings(context)) }

    LaunchedEffect(Unit) {
        settingsContent(settings)
    }

    SettingsList(settings.sectionList as List<List<@Composable () -> Unit>>)
}

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Settings(val context: Context) {
    var sectionList = mutableStateListOf<SnapshotStateList<@Composable () -> Unit>>()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun <T> editDataStore(key: Preferences.Key<T>, newValue: T){
        scope.launch {
            context.settingsDataStore.edit {
                it[key] = newValue
            }
        }
    }

    inner class SettingsSection {
        var list = mutableStateListOf<@Composable () -> Unit>()

        fun settingsItem(
            label: String,
            description: String = "",
            icon: ImageVector? = null,
            onClick: () -> Unit = {}
        ) {
            this.list.add {
                Row(modifier = Modifier.clickable(onClick = onClick)) {
                    ItemBoilerplate(icon = icon, label = label, description = description)
                }
            }
        }

        inline fun <reified T : Enum<T>> radioSettingsItem(
            label: String,
            identifier: String,
            icon: ImageVector? = null,
            default: T
        ) {
            this.list.add {
                var openDialog by remember { mutableStateOf(false) }
                val currentState by this@Settings.context.settingsDataStore.data.map {
                    if(it[intPreferencesKey(identifier)] != null) enumValues<T>()[it[intPreferencesKey(identifier)]!!] else default
                }.collectAsState(initial = default)
                var currentBuffer by remember { mutableStateOf(currentState) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            currentBuffer = currentState
                            openDialog = !openDialog
                        })
                ) {
                    if (openDialog) {
                        AlertDialog(
                            onDismissRequest = { openDialog = false },
                            title = { Text(text = label, fontSize = 22.sp) },
                            dismissButton = {
                                TextButton(onClick = {
                                    editDataStore(intPreferencesKey(identifier), currentBuffer.ordinal)
                                    openDialog = false
                                }) {
                                    Text(text = "Cancel")
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { openDialog = false }) {
                                    Text(text = "Confirm")
                                }
                            },
                            text = {
                                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                                    items(enumValues<T>()) { value ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(MaterialTheme.shapes.medium)
                                                .clickable {
                                                    editDataStore(
                                                        intPreferencesKey(
                                                            identifier
                                                        ), value.ordinal
                                                    )
                                                }
                                                .padding(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = currentState == value,
                                                onClick = null,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(text = value.toString(), fontSize = 18.sp)
                                        }
                                    }
                                }
                            }
                        )
                    }
                    ItemBoilerplate(
                        icon = icon,
                        label = label,
                        description = currentState.toString()
                    )
                }
            }
        }

        @Composable
        fun ItemBoilerplate(icon: ImageVector?, label: String, description: String) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 50.dp)
                    .fillMaxWidth()
                //.border(2.dp, Color.Red)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp)
                ) { //icon
                    if (icon != null) {
                        Icon(imageVector = icon, contentDescription = "icon")
                    }
                }
                Column(modifier = Modifier.padding(vertical = 15.dp)) {
                    Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.W500)
                    if (description.isNotEmpty()) Text(text = description)
                }
            }
        }
    }

    fun settingsSection(title: String? = null, sectionContent: SettingsSection.() -> Unit) {
        val section = SettingsSection()
        if (!title.isNullOrEmpty()) {
            section.list.add {
                Row {
                    Column(modifier = Modifier.width(60.dp)) {}
                    Text(text = title, fontWeight = FontWeight.W700)
                }
            }
        }
        sectionContent(section)
        sectionList.add(section.list)

    }
}

enum class Theme {
    LIGHT {
        override fun toString(): String {
            return "Always light mode"
        }
    },
    DARK {
        override fun toString(): String {
            return "Always dark mode"
        }
    },
    SYSTEM {
        override fun toString(): String {
            return "Use system setting"
        }
    }
}