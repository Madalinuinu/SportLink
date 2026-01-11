package com.example.sportlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sportlink.ui.navigation.NavGraph
import com.example.sportlink.ui.theme.SportLinkTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for SportLink application.
 * Annotated with @AndroidEntryPoint for Hilt dependency injection.
 * This is REQUIRED for Hilt to inject dependencies into the Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportLinkTheme {
                NavGraph()
            }
        }
    }
}

