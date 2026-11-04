package com.example.induztek.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.induztek.repository.models.EquipoDetalle
import com.example.induztek.repository.models.EquipoPrueba
import com.example.induztek.repository.models.Visita
import com.example.induztek.repository.models.VisitaResumen
import kotlinx.coroutines.flow.Flow

private const val SELECT_RESUMEN =
    "SELECT v.id AS id, v.fecha AS fecha, c.nombre AS clienteNombre, d.texto AS direccionTexto, " +
        "u.nombre AS tecnicoNombre, " +
        "(SELECT COUNT(*) FROM equipos_prueba e WHERE e.visitaId = v.id) AS cantidadEquipos " +
        "FROM visitas v " +
        "INNER JOIN clientes c ON c.id = v.clienteId " +
        "INNER JOIN direcciones d ON d.id = v.direccionId " +
        "INNER JOIN users u ON u.id = v.tecnicoId "

/** Clase abstracta (no interfaz) para poder tener un método @Transaction con cuerpo. */
@Dao
abstract class VisitaDao {

    @Insert
    abstract suspend fun insertVisita(visita: Visita): Long

    @Insert
    abstract suspend fun insertEquipos(equipos: List<EquipoPrueba>)

    /** CREATE: guarda la visita y todos sus equipos en una sola transacción. */
    @Transaction
    open suspend fun guardarVisita(visita: Visita, equipos: List<EquipoPrueba>) {
        val visitaId = insertVisita(visita).toInt()
        insertEquipos(equipos.map { it.copy(visitaId = visitaId) })
    }

    // READ: historial general (gerente)
    @Query(SELECT_RESUMEN + "ORDER BY v.fecha DESC")
    abstract fun getResumenes(): Flow<List<VisitaResumen>>

    // READ: historial de un técnico
    @Query(SELECT_RESUMEN + "WHERE v.tecnicoId = :tecnicoId ORDER BY v.fecha DESC")
    abstract fun getResumenesPorTecnico(tecnicoId: Int): Flow<List<VisitaResumen>>

    // READ: equipos de una visita
    @Query(
        "SELECT e.id AS id, e.codigoEquipo AS codigoEquipo, te.nombre AS tipoEquipo, tp.nombre AS tipoPrueba, " +
            "e.resultado AS resultado, e.observaciones AS observaciones, e.imageUri AS imageUri " +
            "FROM equipos_prueba e " +
            "INNER JOIN tipos_equipo te ON te.id = e.tipoEquipoId " +
            "INNER JOIN tipos_prueba tp ON tp.id = e.tipoPruebaId " +
            "WHERE e.visitaId = :visitaId ORDER BY e.id ASC"
    )
    abstract suspend fun getEquipos(visitaId: Int): List<EquipoDetalle>

    // DELETE (los equipos se borran en cascada)
    @Query("DELETE FROM visitas WHERE id = :visitaId")
    abstract suspend fun deleteVisita(visitaId: Int)
}
