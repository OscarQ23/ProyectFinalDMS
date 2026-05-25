package com.example.proyectofinaldms.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinaldms.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val currentUser get() = repository.currentUser

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.registerWithEmail(name, email, password)
            _authState.value = if (result.isSuccess)
                AuthState.Success(result.getOrThrow())
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error al registrar")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithEmail(email, password)
            _authState.value = if (result.isSuccess)
                AuthState.Success(result.getOrThrow())
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error al iniciar sesión")
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithGoogle(account)
            _authState.value = if (result.isSuccess)
                AuthState.Success(result.getOrThrow())
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error con Google")
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
