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

    val mutableSignedIn = mutableStateOf(false)
    var signedIn by mutableSignedIn

    init {
        if (user != null) {
            loadingState = LoadingState.LOGGED_IN
            signedIn = true
        }
    }

    fun signInWithCredential(credential: AuthCredential, onFinished: () -> Unit = {}) =
        viewModelScope.launch {
            loadingState = LoadingState.LOADING
            val authResult = auth.signInWithCredential(credential)
            authResult.addOnSuccessListener {
                loadingState = LoadingState.LOGGED_IN
                mutableSignedIn.value = true
                Log.d(TAG, "signInWithCredential: Finished!")
                onFinished()
            }.addOnFailureListener {
                loadingState = LoadingState.error(it.localizedMessage)
            }
        }

    fun signOut(onFinished: () -> Unit) {
        auth.signOut()
        loadingState = LoadingState.IDLE
        mutableSignedIn.value = false
        onFinished()
    }
}