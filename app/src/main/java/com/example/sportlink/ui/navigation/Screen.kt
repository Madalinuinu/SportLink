package com.example.sportlink.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Sealed class defining all navigation routes in the SportLink app.
 * Used for type-safe navigation throughout the application.
 */
sealed class Screen(val route: String) {
    /**
     * Login screen route.
     */
    object Login : Screen("login")
    
    /**
     * Register screen route.
     */
    object Register : Screen("register")
    
    /**
     * Verify email screen route with email, password, and nickname parameters.
     */
    data class VerifyEmail(
        val email: String,
        val password: String,
        val nickname: String
    ) : Screen("verify_email/{email}/{password}/{nickname}") {
        /**
         * Creates the route with the actual parameters.
         */
        fun createRoute(email: String, password: String, nickname: String): String {
            val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
            val encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString())
            val encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8.toString())
            return "verify_email/$encodedEmail/$encodedPassword/$encodedNickname"
        }
    }
    
    /**
     * Forgot password screen route.
     */
    object ForgotPassword : Screen("forgot_password")
    
    /**
     * Reset password screen route with email parameter.
     */
    data class ResetPassword(val email: String) : Screen("reset_password/{email}") {
        /**
         * Creates the route with the actual email.
         */
        fun createRoute(email: String): String {
            val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
            return "reset_password/$encodedEmail"
        }
    }
    
    /**
     * Home screen route.
     */
    object Home : Screen("home")
    
    /**
     * Details screen route with lobby ID parameter.
     */
    data class Details(val lobbyId: String) : Screen("details/{lobbyId}") {
        /**
         * Creates the route with the actual lobby ID.
         */
        fun createRoute(lobbyId: String) = "details/$lobbyId"
    }
    
    /**
     * Create lobby screen route.
     */
    object CreateLobby : Screen("create_lobby")
    
    /**
     * Profile screen route.
     */
    object Profile : Screen("profile")
}

