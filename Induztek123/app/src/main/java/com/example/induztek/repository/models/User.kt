package com.example.induztek.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = TipoUsuario::class,
            parentColumns = ["id"],
            childColumns = ["tipoUsuarioId"],
            onDelete = ForeignKey.RESTRICT, // no se puede borrar un tipo en uso
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuario"], unique = true),
        Index(value = ["tipoUsuarioId"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val usuario: String,                // nombre de usuario para el login (único)
    val password: String,               // Solo para fines académicos (sin hash)
    val profileImageUri: String? = null, // Ruta local de la foto de perfil
    val tipoUsuarioId: Int              // FK -> tipo_usuarios.id
)
