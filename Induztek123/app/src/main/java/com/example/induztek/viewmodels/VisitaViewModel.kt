package com.example.induztek.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek.repository.data.AppDatabase
import com.example.induztek.repository.models.Direccion
import com.example.induztek.repository.models.EquipoDetalle
import com.example.induztek.repository.models.EquipoPrueba
import com.example.induztek.repository.models.Visita
import com.example.induztek.repository.models.VisitaResumen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Equipo agregado en el formulario, aún sin guardar en la BD. */
data class EquipoBorrador(
    val tipoEquipoId: Int,
    val tipoEquipoNombre: String,
    val codigoEquipo: String,
    val tipoPruebaId: Int,
    val tipoPruebaNombre: String,
    val resultado: String,
    val observaciones: String,
    val imageUri: String?
)

data class NuevaVisitaErrores(
    val cliente: String? = null,
    val direccion: String? = null,
    val tipoEquipo: String? = null,
    val codigoEquipo: String? = null,
    val tipoPrueba: String? = null,
    val resultado: String? = null,
    val equipos: String? = null
)

data class NuevaVisitaUiState(
    val clienteId: Int = 0,
    val direccionId: Int = 0,
    val tipoEquipoId: Int = 0,
    val tipoEquipoNombre: String = "",
    val codigoEquipo: String = "",
    val tipoPruebaId: Int = 0,
    val tipoPruebaNombre: String = "",
    val resultado: String = "",
    val observaciones: String = "",
    val imageUri: String? = null,
    val equipos: List<EquipoBorrador> = emptyList(),
    val errores: NuevaVisitaErrores = NuevaVisitaErrores(),
    val guardada: Boolean = false
)

class VisitaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val visitaDao = db.visitaDao()
    private val direccionDao = db.direccionDao()

    // ---------------------------------------------------------------- Formulario "Nueva visita"
    private val _estado = MutableStateFlow(NuevaVisitaUiState())
    val estado: StateFlow<NuevaVisitaUiState> = _estado.asStateFlow()

    // Direcciones del cliente seleccionado (cambian al elegir otro cliente)
    @OptIn(ExperimentalCoroutinesApi::class)
    val direccionesDelCliente: StateFlow<List<Direccion>> = _estado
        .map { it.clienteId }
        .distinctUntilChanged()
        .flatMapLatest { id -> if (id == 0) flowOf(emptyList()) else direccionDao.getByCliente(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onClienteChange(id: Int) = _estado.update {
        it.copy(clienteId = id, direccionId = 0, errores = it.errores.copy(cliente = null, direccion = null))
    }

    fun onDireccionChange(id: Int) = _estado.update {
        it.copy(direccionId = id, errores = it.errores.copy(direccion = null))
    }

    fun onTipoEquipoChange(id: Int, nombre: String) = _estado.update {
        it.copy(tipoEquipoId = id, tipoEquipoNombre = nombre, errores = it.errores.copy(tipoEquipo = null))
    }

    fun onCodigoEquipoChange(valor: String) = _estado.update {
        it.copy(codigoEquipo = valor, errores = it.errores.copy(codigoEquipo = null))
    }

    fun onTipoPruebaChange(id: Int, nombre: String) = _estado.update {
        it.copy(tipoPruebaId = id, tipoPruebaNombre = nombre, errores = it.errores.copy(tipoPrueba = null))
    }

    fun onResultadoChange(valor: String) = _estado.update {
        it.copy(resultado = valor, errores = it.errores.copy(resultado = null))
    }

    fun onObservacionesChange(valor: String) = _estado.update { it.copy(observaciones = valor) }

    fun onFotoChange(uri: String?) = _estado.update { it.copy(imageUri = uri) }

    // Valida solo los campos del equipo y, si está bien, lo agrega a la lista
    fun agregarEquipo(): Boolean {
        val a = _estado.value
        val errorTipo = if (a.tipoEquipoId == 0) "Indique el tipo de equipo" else null
        val errorCodigo = when {
            a.codigoEquipo.isBlank() -> "El código es obligatorio"
            a.codigoEquipo.trim().length < 3 -> "El código debe tener al menos 3 caracteres"
            else -> null
        }
        val errorPrueba = if (a.tipoPruebaId == 0) "Indique el tipo de prueba" else null
        val errorResultado = if (a.resultado.isBlank()) "Ingrese el resultado" else null

        if (errorTipo != null || errorCodigo != null || errorPrueba != null || errorResultado != null) {
            _estado.update {
                it.copy(
                    errores = it.errores.copy(
                        tipoEquipo = errorTipo, codigoEquipo = errorCodigo,
                        tipoPrueba = errorPrueba, resultado = errorResultado
                    )
                )
            }
            return false
        }

        val nuevo = EquipoBorrador(
            tipoEquipoId = a.tipoEquipoId, tipoEquipoNombre = a.tipoEquipoNombre,
            codigoEquipo = a.codigoEquipo.trim(),
            tipoPruebaId = a.tipoPruebaId, tipoPruebaNombre = a.tipoPruebaNombre,
            resultado = a.resultado.trim(), observaciones = a.observaciones.trim(),
            imageUri = a.imageUri
        )
        _estado.update {
            it.copy(
                equipos = it.equipos + nuevo,
                tipoEquipoId = 0, tipoEquipoNombre = "", codigoEquipo = "",
                tipoPruebaId = 0, tipoPruebaNombre = "", resultado = "",
                observaciones = "", imageUri = null,
                errores = it.errores.copy(
                    tipoEquipo = null, codigoEquipo = null, tipoPrueba = null,
                    resultado = null, equipos = null
                )
            )
        }
        return true
    }

    fun quitarEquipo(indice: Int) = _estado.update {
        it.copy(equipos = it.equipos.filterIndexed { i, _ -> i != indice })
    }

    // Valida la visita completa y la guarda en Room (visita + equipos en una transacción)
    fun guardarVisita(tecnicoId: Int) {
        val e = _estado.value
        // Si dejó un equipo a medio llenar, se intenta agregar antes de guardar
        val pendiente = e.tipoEquipoId != 0 || e.codigoEquipo.isNotBlank() || e.tipoPruebaId != 0 ||
            e.resultado.isNotBlank() || e.observaciones.isNotBlank()
        val equipoOk = if (pendiente) agregarEquipo() else true

        val a = _estado.value
        val errorCliente = if (a.clienteId == 0) "Seleccione un cliente" else null
        val errorDireccion = if (a.direccionId == 0) "Seleccione la dirección o instalación" else null
        val errorEquipos = if (a.equipos.isEmpty()) "Agregue al menos un equipo" else null
        _estado.update {
            it.copy(errores = it.errores.copy(cliente = errorCliente, direccion = errorDireccion, equipos = errorEquipos))
        }
        if (!equipoOk || errorCliente != null || errorDireccion != null || errorEquipos != null) return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val visita = Visita(clienteId = a.clienteId, direccionId = a.direccionId, tecnicoId = tecnicoId)
                    val equipos = a.equipos.map {
                        EquipoPrueba(
                            visitaId = 0, tipoEquipoId = it.tipoEquipoId, tipoPruebaId = it.tipoPruebaId,
                            codigoEquipo = it.codigoEquipo, resultado = it.resultado,
                            observaciones = it.observaciones, imageUri = it.imageUri
                        )
                    }
                    visitaDao.guardarVisita(visita, equipos)
                }
                _estado.value = NuevaVisitaUiState(guardada = true)
            } catch (ex: SQLiteConstraintException) {
                _estado.update {
                    it.copy(errores = it.errores.copy(equipos = "No se pudo guardar: algún dato ya no existe"))
                }
            }
        }
    }

    fun limpiarFormulario() { _estado.value = NuevaVisitaUiState() }

    // ---------------------------------------------------------------- Historial
    // READ: el gerente ve todas las visitas; el técnico solo las suyas
    fun historial(tecnicoId: Int?): StateFlow<List<VisitaResumen>> {
        val flujo = if (tecnicoId == null) visitaDao.getResumenes() else visitaDao.getResumenesPorTecnico(tecnicoId)
        return flujo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    suspend fun getEquipos(visitaId: Int): List<EquipoDetalle> = withContext(Dispatchers.IO) {
        visitaDao.getEquipos(visitaId)
    }

    // DELETE (los equipos se borran en cascada)
    fun eliminarVisita(visitaId: Int) {
        viewModelScope.launch(Dispatchers.IO) { visitaDao.deleteVisita(visitaId) }
    }
}
