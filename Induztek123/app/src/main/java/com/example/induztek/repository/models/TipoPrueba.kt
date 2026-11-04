package com.example.induztek.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_prueba", indices = [Index(value = ["nombre"], unique = true)])
data class TipoPrueba(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val nombre: String
) : ItemCatalogo
