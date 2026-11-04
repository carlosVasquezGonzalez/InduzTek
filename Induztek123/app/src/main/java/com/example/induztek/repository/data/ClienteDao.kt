package com.example.induztek.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.induztek.repository.models.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun getAll(): Flow<List<Cliente>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: Cliente): Long

    @Update
    suspend fun update(item: Cliente)

    @Delete
    suspend fun delete(item: Cliente)
}
