package com.astro.storm.ui.theme

import androidx.compose.ui.graphics.Color

// ===========================
// AstroStorm Material 3 Color System
// Modern, Professional, Clean Palette
// ===========================

// Primary Colors - Deep Cosmic Blues with high contrast
val Primary = Color(0xFF5E7CE2)           // Vibrant cosmic blue - main brand color
val OnPrimary = Color(0xFFFFFFFF)         // Pure white for text on primary
val PrimaryContainer = Color(0xFF3D5AFE)  // Deeper blue for containers
val OnPrimaryContainer = Color(0xFFE8EEFF) // Light blue-white for text on containers

// Secondary Colors - Elegant Purple Accents
val Secondary = Color(0xFF8B7FFF)         // Soft purple for accents
val OnSecondary = Color(0xFFFFFFFF)       // White for text on secondary
val SecondaryContainer = Color(0xFF6C5CE7) // Rich purple for containers
val OnSecondaryContainer = Color(0xFFF0EDFF) // Light purple tint for text

// Tertiary Colors - Gold Highlights for important elements
val Tertiary = Color(0xFFFFB84D)          // Warm gold - for special highlights
val OnTertiary = Color(0xFF1A1B2E)        // Dark for text on gold
val TertiaryContainer = Color(0xFFFF9500) // Vibrant orange-gold
val OnTertiaryContainer = Color(0xFF2B2D42) // Dark blue-grey for text

// Error Colors - Professional error states
val Error = Color(0xFFFF5555)             // Clear red for errors
val OnError = Color(0xFFFFFFFF)           // White for text on error
val ErrorContainer = Color(0xFFFF8A80)    // Light red for error containers
val OnErrorContainer = Color(0xFF410002)  // Dark red for text

// Background Colors - Deep space aesthetic
val Background = Color(0xFF0F1014)        // Very dark background - professional depth
val OnBackground = Color(0xFFE6E8F0)      // Light grey-blue for main text
val BackgroundVariant = Color(0xFF161821) // Slightly lighter for variation

// Surface Colors - Layered elevation system
val Surface = Color(0xFF1E2030)           // Primary surface layer
val SurfaceVariant = Color(0xFF2A2D3E)    // Secondary surface layer
val SurfaceTint = Color(0xFF5E7CE2)       // Tint matches primary
val OnSurface = Color(0xFFE6E8F0)         // Light text on surface
val OnSurfaceVariant = Color(0xFFB8BCCC) // Muted text for less important content

// Outline Colors - Borders and dividers
val Outline = Color(0xFF3F4456)           // Subtle outlines
val OutlineVariant = Color(0xFF2E3142)    // Even more subtle outlines

// Surface Container Hierarchy - Material 3 elevation system
val SurfaceContainerLowest = Color(0xFF181A26)   // Lowest elevation
val SurfaceContainerLow = Color(0xFF1E2030)      // Low elevation
val SurfaceContainer = Color(0xFF242837)         // Standard elevation
val SurfaceContainerHigh = Color(0xFF2A2D3E)     // High elevation
val SurfaceContainerHighest = Color(0xFF303446)  // Highest elevation

// Inverse Colors - For snackbars and special UI
val InverseSurface = Color(0xFFE6E8F0)    // Light surface for contrast
val InverseOnSurface = Color(0xFF1A1C2E)  // Dark text on light surface
val InversePrimary = Color(0xFF3D5AFE)    // Primary color for inverse theme

// Scrim - For modals and overlays
val Scrim = Color(0x88000000)             // Semi-transparent black

// Semantic Colors - Astrology-specific
val PlanetColor = Color(0xFFFFB84D)       // Gold for planets
val AscendantColor = Color(0xFFFF5E94)    // Pink for ascendant
val HouseColor = Color(0xFF8B7FFF)        // Purple for houses
val RetrogradColor = Color(0xFFFF5555)    // Red for retrograde
val BeneficColor = Color(0xFF4CAF50)      // Green for benefic planets
val MaleficColor = Color(0xFFFF5555)      // Red for malefic planets

// Chart Colors - For visualization
val ChartBorder = Color(0xFF5E7CE2)       // Primary color for chart borders
val ChartDivider = Color(0xFF3F4456)      // Outline color for internal dividers
val ChartBackground = Color(0xFF0F1014)   // Background color
val ChartText = Color(0xFFE6E8F0)         // Text color for readability
val ChartHighlight = Color(0xFFFFB84D)    // Gold for highlighted elements

// Legacy Colors (for gradual migration)
@Deprecated("Use Primary instead", ReplaceWith("Primary"))
val CosmicPurple = Primary
@Deprecated("Use Tertiary instead", ReplaceWith("Tertiary"))
val StarGold = Tertiary
@Deprecated("Use Background instead", ReplaceWith("Background"))
val DeepSpace = Background
