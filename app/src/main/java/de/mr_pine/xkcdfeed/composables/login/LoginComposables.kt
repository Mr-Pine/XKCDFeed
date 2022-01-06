package de.mr_pine.xkcdfeed.composables.login

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import de.mr_pine.xkcdfeed.LoadingState
import de.mr_pine.xkcdfeed.LoadingState.Companion.LOADING
import de.mr_pine.xkcdfeed.R

@Composable
fun Login(state: LoadingState, signWithCredential: (AuthCredential) -> Unit, context: Context) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                signWithCredential(credential)
            } catch (e: ApiException) {
                Log.e("TAG", "Google sign in failed", e)
            }
        }

    val token = stringResource(R.string.default_web_client_id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(border = ButtonDefaults.outlinedBorder.copy(width = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(token)
                    .requestEmail()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (state == LOADING) Arrangement.Center else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state == LOADING) {
                    CircularProgressIndicator()
                } else {
                    Icon(
                        painterResource(id = R.drawable.googleg_standard_color_18),
                        null
                    )
                    Text(text = "Sign in with Google")
                    Icon(
                        tint = Color.Transparent,
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = null
                    )
                }
            }
        }
        when (state.status) {
            LoadingState.Status.SUCCESS -> Text(text = "Success")
            LoadingState.Status.FAILED -> Text(text = state.msg ?: "Error")
            LoadingState.Status.LOGGED_IN -> Text(text = "Success")
            else -> {}
        }
    }
}