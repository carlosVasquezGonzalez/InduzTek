package com.example.induztek.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "direcciones",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.RESTRICT, // no se puede borrar un cliente con direcciones
            onUpdate = ForeignKey.CASCADE
        )
    ],
    // Una misma dirección no se repite para el mismo cliente
    indices = [Index(value = ["clienteId", "texto"], unique = true)]
)
data class Direccion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clienteId: Int,
    val texto: String
)

/** Resultado de un JOIN direcciones + clientes, para mostrar en listas. */
data class DireccionConCliente(
    val id: Int,
    val clienteId: Int,
    val clienteNombre: String,
    val texto: String
)
