package com.example.sportlink.di

import android.content.Context
import androidx.room.Room
import com.example.sportlink.data.local.database.SportLinkDatabase
import com.example.sportlink.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 * Provides Room Database instance as a singleton.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides SportLinkDatabase instance using Room.
     * This is a singleton that will be used throughout the app.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SportLinkDatabase {
        return Room.databaseBuilder(
            context,
            SportLinkDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development only - remove in production
            .build()
    }
    
    /**
     * Provides LobbyDao instance from the database.
     */
    @Provides
    @Singleton
    fun provideLobbyDao(database: SportLinkDatabase) = database.lobbyDao()
}

