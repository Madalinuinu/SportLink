package com.example.sportlink.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sportlink.data.local.entity.LobbyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Lobby operations in Room database.
 * All functions are suspend functions to work with Coroutines (10p Management Asincron).
 */
@Dao
interface LobbyDao {
    /**
     * Gets all joined lobbies as a Flow for reactive updates.
     * Returns Flow<List<LobbyEntity>> for real-time updates.
     */
    @Query("SELECT * FROM joined_lobbies")
    fun getAllJoinedLobbies(): Flow<List<LobbyEntity>>
    
    /**
     * Gets a specific lobby by ID.
     */
    @Query("SELECT * FROM joined_lobbies WHERE id = :id")
    suspend fun getLobbyById(id: String): LobbyEntity?
    
    /**
     * Inserts a lobby into the database.
     * Uses REPLACE strategy to handle duplicates.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLobby(lobby: LobbyEntity)
    
    /**
     * Deletes a lobby from the database.
     */
    @Delete
    suspend fun deleteLobby(lobby: LobbyEntity)
    
    /**
     * Deletes a lobby by ID.
     */
    @Query("DELETE FROM joined_lobbies WHERE id = :id")
    suspend fun deleteLobbyById(id: String)
}

