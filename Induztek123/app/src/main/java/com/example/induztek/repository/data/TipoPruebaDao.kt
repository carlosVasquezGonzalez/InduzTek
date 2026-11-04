package com.example.induztek.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.induztek.repository.models.TipoPrueba
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoPruebaDao {
    @Query("SELECT * FROM tipos_prueba ORDER BY nombre ASC")
    fun getAll(): Flow<List<TipoPrueba>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: TipoPrueba): Long

    @Update
    suspend fun update(item: TipoPrueba)

    @Delete
    suspend fun delete(item: TipoPrueba)
}
