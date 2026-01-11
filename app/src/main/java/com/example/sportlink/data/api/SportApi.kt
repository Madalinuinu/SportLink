package com.example.sportlink.data.api

import com.example.sportlink.data.dto.CreateLobbyRequest
import com.example.sportlink.data.dto.LobbyDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API interface for SportLink.
 * All functions are suspend functions to work with Coroutines (10p Management Asincron).
 */
interface SportApi {
    /**
     * Fetches all lobbies from the API.
     * GET /lobbies
     */
    @GET("lobbies")
    suspend fun getAllLobbies(): List<LobbyDto>
    
    /**
     * Fetches a specific lobby by ID.
     * GET /lobbies/{id}
     */
    @GET("lobbies/{id}")
    suspend fun getLobbyById(@Path("id") id: String): LobbyDto
    
    /**
     * Creates a new lobby.
     * POST /lobbies
     */
    @POST("lobbies")
    suspend fun createLobby(@Body request: CreateLobbyRequest): LobbyDto
}

