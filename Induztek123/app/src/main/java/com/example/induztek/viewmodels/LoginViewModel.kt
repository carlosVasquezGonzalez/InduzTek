package com.example.induztek.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.repository.data.AppDatabase
import com.example.induztek.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val errorUsuario: String? = null,
    val errorPassword: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val loggedUser: User? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(value: String) =
        _uiState.update { it.copy(usuario = value, errorUsuario = null, error = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorPassword = null, error = null) }

    fun login() {
        val state = _uiState.value
        val errorUsuario = if (state.usuario.isBlank()) "Campo obligatorio" else null
        val errorPassword = if (state.password.isBlank()) "Campo obligatorio" else null
        if (errorUsuario != null || errorPassword != null) {
            _uiState.update { it.copy(errorUsuario = errorUsuario, errorPassword = errorPassword) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600) // pequeña pausa para mostrar el indicador de carga
            val user = withContext(Dispatchers.IO) {
                userDao.login(state.usuario.trim(), state.password)
            }
            _uiState.update {
                if (user != null) it.copy(isLoading = false, loggedUser = user, password = "")
                else it.copy(isLoading = false, error = "Credenciales inválidas")
            }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}
