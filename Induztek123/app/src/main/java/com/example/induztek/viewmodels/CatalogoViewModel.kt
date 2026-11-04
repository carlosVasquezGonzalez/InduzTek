package com.example.induztek.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.repository.data.AppDatabase
import com.example.induztek.repository.models.Cliente
import com.example.induztek.repository.models.ItemCatalogo
import com.example.induztek.repository.models.TipoEquipo
import com.example.induztek.repository.models.TipoPrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base de los catálogos simples (clientes, tipos de equipo, tipos de prueba).
 * Cada catálogo es una tabla Room con nombre único y CRUD completo.
 */
abstract class CatalogoViewModel<T : ItemCatalogo>(
    application: Application,
    private val singular: String // para los mensajes: "cliente", "tipo de equipo"...
) : AndroidViewModel(application) {

    protected abstract val flujo: Flow<List<T>>
    protected abstract fun crear(id: Int, nombre: String): T
    protected abstract suspend fun insertar(item: T)
    protected abstract suspend fun actualizar(item: T)
    protected abstract suspend fun eliminar(item: T)

    val items: StateFlow<List<T>> by lazy {
        flujo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    /** CREATE (id = 0) o UPDATE (id > 0). */
    fun save(id: Int, nombre: String, onSuccess: () -> Unit = {}) {
        val valor = nombre.trim()
        if (valor.isEmpty()) {
            _message.value = "Ingresa un nombre"
            return
        }
        if (valor.length > MAX_LARGO) {
            _message.value = "Máximo $MAX_LARGO caracteres"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val item = crear(id, valor)
                    if (id == 0) insertar(item) else actualizar(item)
                }
                _message.value = null
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _message.value = "Ya existe un $singular llamado \"$valor\""
            }
        }
    }

    /** DELETE. Falla si el registro está en uso (FK RESTRICT). */
    fun delete(item: T) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { eliminar(item) }
            } catch (e: SQLiteConstraintException) {
                _message.value = "No se puede eliminar \"${item.nombre}\": está en uso"
            }
        }
    }

    companion object {
        const val MAX_LARGO = 50
    }
}

class ClienteViewModel(application: Application) : CatalogoViewModel<Cliente>(application, "cliente") {
    private val dao = AppDatabase.getDatabase(application).clienteDao()
    override val flujo get() = dao.getAll()
    override fun crear(id: Int, nombre: String) = Cliente(id, nombre)
    override suspend fun insertar(item: Cliente) { dao.insert(item) }
    override suspend fun actualizar(item: Cliente) = dao.update(item)
    override suspend fun eliminar(item: Cliente) = dao.delete(item)
}

class TipoEquipoViewModel(application: Application) : CatalogoViewModel<TipoEquipo>(application, "tipo de equipo") {
    private val dao = AppDatabase.getDatabase(application).tipoEquipoDao()
    override val flujo get() = dao.getAll()
    override fun crear(id: Int, nombre: String) = TipoEquipo(id, nombre)
    override suspend fun insertar(item: TipoEquipo) { dao.insert(item) }
    override suspend fun actualizar(item: TipoEquipo) = dao.update(item)
    override suspend fun eliminar(item: TipoEquipo) = dao.delete(item)
}

class TipoPruebaViewModel(application: Application) : CatalogoViewModel<TipoPrueba>(application, "tipo de prueba") {
    private val dao = AppDatabase.getDatabase(application).tipoPruebaDao()
    override val flujo get() = dao.getAll()
    override fun crear(id: Int, nombre: String) = TipoPrueba(id, nombre)
    override suspend fun insertar(item: TipoPrueba) { dao.insert(item) }
    override suspend fun actualizar(item: TipoPrueba) = dao.update(item)
    override suspend fun eliminar(item: TipoPrueba) = dao.delete(item)
}
