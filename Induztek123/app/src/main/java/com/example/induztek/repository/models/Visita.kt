package com.example.induztek.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Visita técnica: un técnico visita una dirección de un cliente y prueba varios equipos. */
@Entity(
    tableName = "visitas",
    foreignKeys = [
        ForeignKey(Cliente::class, ["id"], ["clienteId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Direccion::class, ["id"], ["direccionId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(User::class, ["id"], ["tecnicoId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("clienteId"), Index("direccionId"), Index("tecnicoId")]
)
data class Visita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clienteId: Int,
    val direccionId: Int,
    val tecnicoId: Int,
    val fecha: Long = System.currentTimeMillis()
)

/** Equipo probado durante una visita (con foto opcional tomada con CameraX). */
@Entity(
    tableName = "equipos_prueba",
    foreignKeys = [
        ForeignKey(Visita::class, ["id"], ["visitaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(TipoEquipo::class, ["id"], ["tipoEquipoId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(TipoPrueba::class, ["id"], ["tipoPruebaId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("visitaId"), Index("tipoEquipoId"), Index("tipoPruebaId")]
)
data class EquipoPrueba(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val visitaId: Int,
    val tipoEquipoId: Int,
    val tipoPruebaId: Int,
    val codigoEquipo: String,
    val resultado: String,
    val observaciones: String = "",
    val imageUri: String? = null // Foto del equipo tomada con CameraX
)

/** Resultados de JOIN para el historial. */
data class VisitaResumen(
    val id: Int,
    val fecha: Long,
    val clienteNombre: String,
    val direccionTexto: String,
    val tecnicoNombre: String,
    val cantidadEquipos: Int
)

data class EquipoDetalle(
    val id: Int,
    val codigoEquipo: String,
    val tipoEquipo: String,
    val tipoPrueba: String,
    val resultado: String,
    val observaciones: String,
    val imageUri: String?
)
