package com.example.induztek.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.induztek.repository.models.Cliente
import com.example.induztek.repository.models.Direccion
import com.example.induztek.repository.models.EquipoPrueba
import com.example.induztek.repository.models.TipoEquipo
import com.example.induztek.repository.models.TipoPrueba
import com.example.induztek.repository.models.TipoUsuario
import com.example.induztek.repository.models.User
import com.example.induztek.repository.models.Visita

@Database(
    entities = [
        User::class, TipoUsuario::class,
        Cliente::class, Direccion::class, TipoEquipo::class, TipoPrueba::class,
        Visita::class, EquipoPrueba::class
    ],
    version = 2, // v2: Room completo (v1 solo tenía la tabla usuarios)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tipoUsuarioDao(): TipoUsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun direccionDao(): DireccionDao
    abstract fun tipoEquipoDao(): TipoEquipoDao
    abstract fun tipoPruebaDao(): TipoPruebaDao
    abstract fun visitaDao(): VisitaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "induztek.db"
                )
                    // Proyecto académico: al cambiar la versión se recrea la BD
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Datos iniciales. Se ejecuta en cada apertura con INSERT OR IGNORE,
        // así también se cargan después de una migración destructiva.
        private val seedCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL(
                    "INSERT OR IGNORE INTO tipo_usuarios (id, tipo_usuario) VALUES " +
                            "(1, 'Gerente'), (2, 'Técnico')"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO users (nombre, usuario, password, profileImageUri, tipoUsuarioId) VALUES " +
                            "('Carlos Gerente', 'gerente', '1234', NULL, 1), " +
                            "('Juan Técnico', 'tecnico', '1234', NULL, 2)"
                )
            }
        }
    }
}
