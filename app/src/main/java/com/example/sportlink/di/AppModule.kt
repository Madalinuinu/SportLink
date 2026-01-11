package com.example.sportlink.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.sportlink.data.api.SportApi
import com.example.sportlink.data.repository.LobbyRepositoryImpl
import com.example.sportlink.data.repository.UserRepositoryImpl
import com.example.sportlink.domain.repository.LobbyRepository
import com.example.sportlink.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module for providing application-level dependencies.
 * This module provides DataStore and repository bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    
    /**
     * Binds LobbyRepository interface to its implementation.
     * This is how Hilt knows which implementation to use (10p DI).
     */
    @Binds
    @Singleton
    abstract fun bindLobbyRepository(impl: LobbyRepositoryImpl): LobbyRepository
    
    /**
     * Binds UserRepository interface to its implementation.
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    
    companion object {
        /**
         * Provides DataStore instance for user preferences.
         * Uses PreferenceDataStoreFactory to create a singleton DataStore.
         */
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { emptyPreferences() }
                ),
                produceFile = { context.preferencesDataStoreFile("user_preferences") }
            )
        }
        
        /**
         * Provides SportApi interface implementation from Retrofit.
         */
        @Provides
        @Singleton
        fun provideSportApi(retrofit: Retrofit): SportApi {
            return retrofit.create(SportApi::class.java)
        }
    }
}

