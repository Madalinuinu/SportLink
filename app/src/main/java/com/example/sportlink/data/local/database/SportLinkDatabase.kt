package com.example.sportlink.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sportlink.data.local.dao.LobbyDao
import com.example.sportlink.data.local.entity.LobbyEntity

/**
 * Room Database for SportLink application.
 * This is the main database class that Room uses to create the database.
 */
@Database(
    entities = [LobbyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SportLinkDatabase : RoomDatabase() {
    /**
     * Provides access to LobbyDao for lobby operations.
     */
    abstract fun lobbyDao(): LobbyDao
}

