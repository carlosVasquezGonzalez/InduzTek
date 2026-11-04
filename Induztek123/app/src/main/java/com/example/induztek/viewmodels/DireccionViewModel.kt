package com.example.induztek.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.repository.data.AppDatabase
import com.example.induztek.repository.models.Direccion
import com.example.induztek.repository.models.DireccionConCliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DireccionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).direccionDao()

    val direcciones: StateFlow<List<DireccionConCliente>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    /** CREATE (id = 0) o UPDATE (id > 0). */
    fun save(id: Int, clienteId: Int, texto: String, onSuccess: () -> Unit = {}) {
        val limpio = texto.trim()
        if (clienteId == 0) {
            _message.value = "Selecciona un cliente"
            return
        }
        if (limpio.isEmpty()) {
            _message.value = "Ingresa la dirección"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val d = Direccion(id = id, clienteId = clienteId, texto = limpio)
                    if (id == 0) dao.insert(d) else dao.update(d)
                }
                _message.value = null
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "Esa dirección ya existe para el cliente"
            }
        }
    }

    /** DELETE. Falla si hay visitas en esa dirección (FK RESTRICT). */
    fun delete(d: DireccionConCliente) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dao.delete(Direccion(id = d.id, clienteId = d.clienteId, texto = d.texto))
                }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar: hay visitas registradas en esa dirección"
            }
        }
    }
}
