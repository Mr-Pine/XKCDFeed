package de.mr_pine.xkcdfeed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

private const val TAG = "Login"

data class LoadingState private constructor(val status: Status, val msg: String? = null) {
    companion object {
        val LOADED = LoadingState(Status.SUCCESS)
        val IDLE = LoadingState(Status.IDLE)
        val LOADING = LoadingState(Status.RUNNING)
        val LOGGED_IN = LoadingState(Status.LOGGED_IN)
        fun error(msg: String?) = LoadingState(Status.FAILED, msg)
    }

    enum class Status {
        RUNNING,
        SUCCESS,
        FAILED,
        IDLE,
        LOGGED_IN
    }
}

class LoginViewModel: ViewModel() {
    var loadingState by mutableStateOf(LoadingState.IDLE)
    private val auth = Firebase.auth

    fun signInWithCredential(credential: AuthCredential) = viewModelScope.launch {
        try {
            loadingState = LoadingState.LOADING
            auth.signInWithCredential(credential)
            loadingState = LoadingState.LOADED
            Log.d(TAG, "signInWithCredential: Finished!")
        } catch (e: Exception) {
            loadingState = LoadingState.error(e.localizedMessage)
        }
    }
}