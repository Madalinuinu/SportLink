package com.example.sportlink

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for SportLink.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * This is REQUIRED for Hilt to work properly.
 */
@HiltAndroidApp
class SportLinkApplication : Application()

