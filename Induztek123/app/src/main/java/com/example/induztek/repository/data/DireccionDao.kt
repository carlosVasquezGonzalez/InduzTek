package com.example.induztek.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.induztek.repository.models.Direccion
import com.example.induztek.repository.models.DireccionConCliente
import kotlinx.coroutines.flow.Flow

@Dao
interface DireccionDao {
    @Query(
        "SELECT d.id AS id, d.clienteId AS clienteId, c.nombre AS clienteNombre, d.texto AS texto " +
                "FROM direcciones d INNER JOIN clientes c ON c.id = d.clienteId " +
                "ORDER BY c.nombre ASC, d.texto ASC"
    )
    fun getAll(): Flow<List<DireccionConCliente>>

    // Direcciones de un cliente (selector del formulario de visita)
    @Query("SELECT * FROM direcciones WHERE clienteId = :clienteId ORDER BY texto ASC")
    fun getByCliente(clienteId: Int): Flow<List<Direccion>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(direccion: Direccion): Long

    @Update
    suspend fun update(direccion: Direccion)

    @Delete
    suspend fun delete(direccion: Direccion)
}
