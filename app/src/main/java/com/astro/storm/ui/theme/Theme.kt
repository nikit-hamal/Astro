package com.astro.storm.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Complete Material 3 Dark Color Scheme for AstroStorm
 * Implements the full M3 color system with semantic color roles
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    // Secondary colors
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    // Tertiary colors
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    // Error colors
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    // Background colors
    background = Background,
    onBackground = OnBackground,

    // Surface colors with full elevation system
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,

    // Inverse colors for snackbars and special UI
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,

    // Outline colors
    outline = Outline,
    outlineVariant = OutlineVariant,

    // Scrim for modals
    scrim = Scrim,

    // Surface containers - Material 3 elevation system
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
)

/**
 * Light Color Scheme (optional - kept for future flexibility)
 * Currently astronomy app uses dark theme by default
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary
)

/**
 * AstroStorm Material 3 Theme
 *
 * Features:
 * - Complete Material 3 color system implementation
 * - Poppins font family throughout
 * - Professional, modern, clean aesthetic
 * - Full elevation system with surface containers
 * - Proper system bar styling
 *
 * @param darkTheme Whether to use dark theme (default: true for astronomy aesthetic)
 * @param dynamicColor Whether to use Android 12+ dynamic colors (default: false)
 * @param content Composable content to theme
 */
@Composable
fun AstroStormTheme(
    darkTheme: Boolean = true, // Always default to dark for astronomy app
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Always use dark theme for astronomy aesthetic
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to background color for seamless edge-to-edge
            window.statusBarColor = colorScheme.background.toArgb()
            // Set navigation bar color
            window.navigationBarColor = colorScheme.background.toArgb()
            // Dark status bar icons for professional look
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
