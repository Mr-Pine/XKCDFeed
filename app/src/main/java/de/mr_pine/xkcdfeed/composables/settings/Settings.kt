package de.mr_pine.xkcdfeed.composables.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.mr_pine.xkcdfeed.*
import de.mr_pine.xkcdfeed.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray


private const val TAG = "Settings"

@Composable
fun SettingsComposable(
    context: Context,
    loginViewModel: LoginViewModel,
    mainViewModel: MainViewModel,
    onLoginChanged: () -> Unit,
    navigateBack: () -> Unit
) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                loginViewModel.signInWithCredential(credential, onLoginChanged)
            } catch (e: ApiException) {
                Log.e("TAG", "Google sign in failed", e)
            }
        }

    val token = stringResource(R.string.default_web_client_id)

    val localContext = LocalContext.current

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

        Settings(context/*, loginViewModel.signedIn*/) {
            settingsSection {

                settingsItem(
                    label = "Cloud synchronisation",
                    description = if (loginViewModel.signedIn) "Logged in as ${loginViewModel.user?.displayName}. Tap to log out" else "Tap to log in",
                    icon = SettingsIcon(
                        if (loginViewModel.signedIn) Icons.Outlined.Cloud else Icons.Outlined.CloudOff,
                        loginViewModel.loadingState == LoadingState.LOADING
                    )
                ) {
                    if (loginViewModel.signedIn) {
                        Log.d(TAG, "SettingsComposable: logged in")
                        loginViewModel.signOut(onLoginChanged)
                    } else {
                        Log.d(TAG, "SettingsComposable: Not signed in")
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(token)
                            .requestEmail()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                }
                if (loginViewModel.signedIn) {

                    settingsItem(
                        label = "Sync cloud -> local",
                        description = "Save the favorites from the cloud locally to be used when not logged in",
                        icon = Icons.Outlined.CloudDownload
                    ) {
                        val favoritesReference =
                            Firebase.firestore.collection("Users/${loginViewModel.user?.uid}/Favorites")

                        favoritesReference.get().addOnSuccessListener { collection ->
                            val documents = collection.documents
                            val listValues = documents.map { documentSnapshot ->
                                documentSnapshot.id.toInt()
                            }

                            Log.d(TAG, "initFavoriteList: hi cloud :) $listValues")

                            scope.launch {
                                context.userDataStore.edit { mutablePreferences ->
                                    val stringList =
                                        mutablePreferences[mainViewModel.favoriteListKey]
                                            ?: "[]"
                                    val mutableList =
                                        mainViewModel.generateListFromJSON(stringList)
                                    mutableList.addAll(listValues)
                                    mutablePreferences[mainViewModel.favoriteListKey] =
                                        JSONArray(
                                            mutableList.toTypedArray().distinct()
                                        ).toString()
                                }
                            }
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "initFavoriteList: $e user: ${loginViewModel.user?.uid}")
                        }
                    }

                    settingsItem(
                        label = "Sync local -> cloud",
                        description = "Save the favorites from the local storage to the cloud to be used when logged in",
                        icon = Icons.Outlined.CloudUpload
                    ) {
                        scope.launch {
                            context.userDataStore.data.first { preferences ->
                                // No type safety.
                                val stringList = preferences[mainViewModel.favoriteListKey] ?: "[]"
                                val localList = mainViewModel.generateListFromJSON(stringList)
                                Log.d(
                                    TAG,
                                    "initFavoriteList: hi local :) $localList"
                                )

                                val favoritesReference =
                                    Firebase.firestore.collection("Users/${loginViewModel.user?.uid}/Favorites")

                                for (number in localList) {
                                    favoritesReference.document(number.toString())
                                        .set(emptyMap<String, String>())
                                }

                                onLoginChanged()
                                false
                            }
                        }
                    }

                    settingsItem(
                        label = "Migrate data",
                        description = "Migrate favorites from the old app tho this version"
                    ) {
                        val oldReference =
                            Firebase.firestore.collection("Favourites/${loginViewModel.user?.uid}/ComicObjects")

                        oldReference.get().addOnSuccessListener { collection ->
                            val documents = collection.documents
                            val listValues = documents.map { documentSnapshot ->
                                documentSnapshot.id.toInt()
                            }

                            Log.d(TAG, "SettingsComposable: $listValues")

                            val favoritesReference =
                                Firebase.firestore.collection("Users/${loginViewModel.user?.uid}/Favorites")

                            for (number in listValues) {
                                favoritesReference.document(number.toString())
                                    .set(emptyMap<String, String>())
                            }

                            onLoginChanged()
                        }.addOnFailureListener { e ->
                            Log.e(
                                TAG,
                                "initFavoriteList: get old list $e user: ${loginViewModel.user?.uid}"
                            )
                        }
                    }
                }
            }
            settingsSection {
                radioSettingsItem("Theme", "theme", Icons.Filled.Tonality, Theme.SYSTEM)
                settingsItem(
                    "Notification settings",
                    icon = Icons.Outlined.Notifications
                ) {
                    val settingsIntent =
                        Intent(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS else android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) settingsIntent.putExtra(
                        android.provider.Settings.EXTRA_APP_PACKAGE,
                        context.packageName
                    )

                    context.startActivity(settingsIntent)
                }
                settingsItem("Show Open Source Licenses") {
                    localContext.startActivity(
                        Intent(
                            localContext,
                            OssLicensesMenuActivity::class.java
                        )
                    )
                }
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
fun Settings(context: Context, vararg rememberKeys: Any?, settingsContent: Settings.() -> Unit) {
    val settings by remember { mutableStateOf(Settings(context)) }

    LaunchedEffect(rememberKeys) {
        settings.sectionList.clear()
        settingsContent(settings)
    }

    SettingsList(settings.sectionList as List<List<@Composable () -> Unit>>)
}

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Settings(val context: Context) {
    var sectionList = mutableStateListOf<SnapshotStateList<@Composable () -> Unit>>()
    val scope = CoroutineScope(Dispatchers.IO)

    fun <T> editDataStore(key: Preferences.Key<T>, newValue: T) {
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
            icon: SettingsIcon? = null,
            onClick: () -> Unit = {}
        ) {
            this.list.add {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onClick)
                        .padding(end = 16.dp)
                ) {
                    ItemBoilerplate(icon = icon, label = label, description = description)
                }
            }
        }

        fun settingsItem(
            label: String,
            description: String = "",
            icon: ImageVector,
            onClick: () -> Unit = {}
        ) = settingsItem(
            label = label,
            description = description,
            icon = SettingsIcon(icon, false),
            onClick = onClick
        )

        inline fun <reified T : Enum<T>> radioSettingsItem(
            label: String,
            identifier: String,
            icon: SettingsIcon? = null,
            default: T
        ) {
            this.list.add {
                var openDialog by remember { mutableStateOf(false) }
                val currentState by this@Settings.context.settingsDataStore.data.map {
                    if (it[intPreferencesKey(identifier)] != null) enumValues<T>()[it[intPreferencesKey(
                        identifier
                    )]!!] else default
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
                                    editDataStore(
                                        intPreferencesKey(identifier),
                                        currentBuffer.ordinal
                                    )
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

        inline fun <reified T : Enum<T>> radioSettingsItem(
            label: String,
            identifier: String,
            icon: ImageVector,
            default: T
        ) = radioSettingsItem(
            label,
            identifier,
            icon = SettingsIcon(icon, false),
            default = default
        )

        @Composable
        fun ItemBoilerplate(icon: SettingsIcon?, label: String, description: String) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 50.dp)
                    .fillMaxWidth()
                //.border(2.dp, Color.Red)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(60.dp)
                ) { //icon
                    icon?.Show()
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

class SettingsIcon(var icon: ImageVector?, var loading: Boolean) {
    @Composable
    fun Show() {
        if (loading || icon == null) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
        } else {
            Icon(imageVector = icon!!, contentDescription = "icon")
        }
    }
}