package com.example.induztek.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.induztek.repository.models.TipoEquipo
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoEquipoDao {
    @Query("SELECT * FROM tipos_equipo ORDER BY nombre ASC")
    fun getAll(): Flow<List<TipoEquipo>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: TipoEquipo): Long

    @Update
    suspend fun update(item: TipoEquipo)

    @Delete
    suspend fun delete(item: TipoEquipo)
}
