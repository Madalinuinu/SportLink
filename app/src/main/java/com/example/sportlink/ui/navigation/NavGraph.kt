package com.example.sportlink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sportlink.ui.screens.home.HomeScreen
import com.example.sportlink.ui.screens.login.LoginScreen

/**
 * Navigation graph for SportLink application.
 * Defines all navigation routes and their destinations.
 * 
 * @param navController The NavController for navigation (defaults to rememberNavController)
 * @param startDestination The starting destination route (defaults to Login)
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        // Clear back stack so user can't go back to login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToDetails = { lobbyId ->
                    navController.navigate(Screen.Details(lobbyId).createRoute(lobbyId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateLobby.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        // Details Screen with lobby ID argument
        composable(
            route = Screen.Details("").route,
            arguments = listOf(
                navArgument("lobbyId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val lobbyId = backStackEntry.arguments?.getString("lobbyId") ?: ""
            // DetailsScreen will be implemented in ETAPA 4
            // For now, just navigate back
            navController.popBackStack()
        }
        
        // Create Lobby Screen
        composable(route = Screen.CreateLobby.route) {
            // CreateLobbyScreen will be implemented in ETAPA 4
            // For now, just navigate back
            navController.popBackStack()
        }
        
        // Profile Screen
        composable(route = Screen.Profile.route) {
            // ProfileScreen will be implemented in ETAPA 4
            // For now, just navigate back
            navController.popBackStack()
        }
    }
}

