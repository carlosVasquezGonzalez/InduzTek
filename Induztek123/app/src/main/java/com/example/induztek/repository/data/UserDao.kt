package com.example.induztek.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.induztek.repository.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // READ
    @Query("SELECT * FROM users ORDER BY nombre ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    // LOGIN
    @Query("SELECT * FROM users WHERE usuario = :usuario AND password = :password LIMIT 1")
    suspend fun login(usuario: String, password: String): User?

    // CREATE (ABORT: el usuario es único)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    // UPDATE
    @Update
    suspend fun updateUser(user: User)

    // DELETE
    @Delete
    suspend fun deleteUser(user: User)
}
