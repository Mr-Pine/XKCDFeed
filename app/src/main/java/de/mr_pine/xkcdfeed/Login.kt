package de.mr_pine.xkcdfeed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "Login"

data class LoadingState private constructor(val status: Status, val msg: String? = null) {
    companion object {
        val IDLE = LoadingState(Status.IDLE)
        val LOADING = LoadingState(Status.RUNNING)
        val LOGGED_IN = LoadingState(Status.LOGGED_IN)
        fun error(msg: String?) = LoadingState(Status.FAILED, msg)
    }

    enum class Status {
        RUNNING,
        FAILED,
        IDLE,
        LOGGED_IN
    }
}

class LoginViewModel : ViewModel() {
    private val auth = Firebase.auth

    var user: FirebaseUser? by mutableStateOf(auth.currentUser)

    var loadingState by mutableStateOf(LoadingState.IDLE)

    var signedIn by mutableStateOf(false)

    init {
        if (user != null) {
            loadingState = LoadingState.LOGGED_IN
            signedIn = true
        }
    }

    fun signInWithCredential(credential: AuthCredential, onFinished: () -> Unit = {}) = viewModelScope.launch {
        try {
            loadingState = LoadingState.LOADING
            auth.signInWithCredential(credential)
            delay(1000)
            loadingState = LoadingState.LOGGED_IN
            signedIn = true
            Log.d(TAG, "signInWithCredential: Finished!")
            onFinished()
        } catch (e: Exception) {
            loadingState = LoadingState.error(e.localizedMessage)
        }
    }

    fun signOut(onFinished: () -> Unit) {
        auth.signOut()
        loadingState = LoadingState.IDLE
        signedIn = false
        onFinished()
    }
}