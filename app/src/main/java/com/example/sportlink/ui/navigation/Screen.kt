package com.example.sportlink.ui.navigation

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

