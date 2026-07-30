package com.google.jetstream.presentation

/**
 * Keeps the Android 12 splash visible until the first home catalog is ready for focus.
 */
object AppSplashGate {
    @Volatile
    var keepSplashScreen: Boolean = true
}
