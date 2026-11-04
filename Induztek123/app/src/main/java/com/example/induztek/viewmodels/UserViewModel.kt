package com.example.induztek.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.repository.data.AppDatabase
import com.example.induztek.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    // READ
    val users: StateFlow<List<User>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensajes de error para la vista (ej: usuario duplicado)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    suspend fun getUser(id: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    // CREATE / UPDATE. onSuccess se ejecuta en el hilo principal.
    fun saveUser(
        id: Int,
        nombre: String,
        usuario: String,
        password: String,
        imageUri: String?,
        tipoUsuarioId: Int,
        onSuccess: () -> Unit
    ) {
        // Validación: User.nombre solo admite letras (mayúsculas/minúsculas) y espacios entre palabras
        if (!Validaciones.esSoloLetras(nombre)) {
            _message.value = "El nombre solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val user = User(
                        id = id, nombre = nombre, usuario = usuario, password = password,
                        profileImageUri = imageUri, tipoUsuarioId = tipoUsuarioId
                    )
                    if (id == 0) userDao.insertUser(user) else userDao.updateUser(user)
                }
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "Ya existe un usuario con el nombre de usuario \"$usuario\""
            }
        }
    }

    // DELETE (falla si el usuario tiene visitas registradas: FK RESTRICT)
    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { userDao.deleteUser(user) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar a \"${user.nombre}\": tiene visitas registradas"
            }
        }
    }
}
