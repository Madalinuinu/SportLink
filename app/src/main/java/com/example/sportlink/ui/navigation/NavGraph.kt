package com.example.sportlink.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sportlink.ui.screens.create.CreateLobbyScreen
import com.example.sportlink.ui.screens.details.DetailsScreen
import com.example.sportlink.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.sportlink.ui.screens.home.HomeScreen
import com.example.sportlink.ui.screens.login.LoginScreen
import com.example.sportlink.ui.screens.profile.ProfileScreen
import com.example.sportlink.ui.screens.register.RegisterScreen
import com.example.sportlink.ui.screens.resetpassword.ResetPasswordScreen
import com.example.sportlink.ui.screens.verifyemail.VerifyEmailScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        
        // Register Screen
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { email, password, nickname ->
                    // Navigate to verify email screen
                    navController.navigate(Screen.VerifyEmail(email, password, nickname).createRoute(email, password, nickname))
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        // Verify Email Screen
        composable(
            route = Screen.VerifyEmail("", "", "").route,
            arguments = listOf(
                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("password") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("nickname") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val email = URLDecoder.decode(backStackEntry.arguments?.getString("email") ?: "", StandardCharsets.UTF_8.toString())
            val password = URLDecoder.decode(backStackEntry.arguments?.getString("password") ?: "", StandardCharsets.UTF_8.toString())
            val nickname = URLDecoder.decode(backStackEntry.arguments?.getString("nickname") ?: "", StandardCharsets.UTF_8.toString())
            
            VerifyEmailScreen(
                email = email,
                password = password,
                nickname = nickname,
                onVerificationSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Forgot Password Screen
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCodeSent = { email ->
                    navController.navigate(Screen.ResetPassword(email).createRoute(email)) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Reset Password Screen with email argument
        composable(
            route = Screen.ResetPassword("").route,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val email = URLDecoder.decode(backStackEntry.arguments?.getString("email") ?: "", StandardCharsets.UTF_8.toString())
            ResetPasswordScreen(
                email = email,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPasswordResetSuccess = {
                    navController.navigate(Screen.Login.route) {
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
            DetailsScreen(
                lobbyId = lobbyId,
                navController = navController
            )
        }
        
        // Create Lobby Screen
        composable(route = Screen.CreateLobby.route) {
            CreateLobbyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Profile Screen
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

