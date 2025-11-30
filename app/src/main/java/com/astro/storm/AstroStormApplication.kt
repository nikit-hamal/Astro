package com.astro.storm

import android.app.Application
import android.util.Log
import com.astro.storm.ephemeris.SwissEphemerisEngine
import java.io.IOException

/**
 * Application class for AstroStorm
 */
class AstroStormApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Copy ephemeris files once on app startup
        try {
            SwissEphemerisEngine(this).copyEphemerisFiles(this)
        } catch (e: IOException) {
            Log.e("AstroStormApplication", "Failed to copy ephemeris files", e)
        }
    }
}
