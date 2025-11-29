package com.astro.storm

import android.app.Application
import com.astro.storm.ephemeris.SwissEphemerisEngine

/**
 * Application class for AstroStorm
 */
class AstroStormApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Copy ephemeris files once on app startup
        SwissEphemerisEngine(this).copyEphemerisFiles(this)
    }
}
