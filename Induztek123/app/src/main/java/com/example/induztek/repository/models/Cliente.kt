package com.example.induztek.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clientes", indices = [Index(value = ["nombre"], unique = true)])
data class Cliente(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val nombre: String
) : ItemCatalogo
