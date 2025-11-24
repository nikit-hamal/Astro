# AstroStorm - Ultra-Precision Vedic Astrology

[![Android CI](https://github.com/yourusername/astrostorm/actions/workflows/android.yml/badge.svg)](https://github.com/yourusername/astrostorm/actions/workflows/android.yml)

AstroStorm is a production-grade Android application for generating ultra-accurate Vedic natal charts using the Swiss Ephemeris engine in JPL mode.

## Features

- **Maximum Precision Calculations**: Uses Swiss Ephemeris with JPL ephemeris files for planetary positions
- **Complete Vedic Astrology**: Sidereal zodiac with Lahiri ayanamsa
- **Detailed Chart Information**:
  - Precise planetary longitudes with retrograde detection
  - House cusps using multiple house systems
  - Nakshatra details with pada information
  - Julian Day and astronomical metadata
- **High-Quality Chart Rendering**: South Indian diamond-style chart visualization
- **Export Capabilities**:
  - Save chart as high-resolution PNG image
  - Copy plaintext chart data to clipboard (LLM-friendly format)
- **Local Storage**: Save and manage multiple charts using Room database
- **Modern UI**: Material 3 design with Jetpack Compose
- **Clean Architecture**: Separated layers for easy extension

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room
- **Astronomical Engine**: Swiss Ephemeris (JPL mode)
- **Build System**: Gradle with Kotlin DSL

## Building

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Swiss Ephemeris Library
This project requires the Swiss Ephemeris Java library. Download it from:
- [Swiss Ephemeris](https://www.astro.com/swisseph/)
- Place `swisseph-2.10.03.jar` in `app/libs/`

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/yourusername/astrostorm.git
cd astrostorm

# Build the APK
./gradlew assembleRelease
```

The signed APK will be available at `app/build/outputs/apk/release/`

## CI/CD

The project includes GitHub Actions workflow for automated builds. The workflow:
- Automatically generates the keystore for signing
- Downloads required dependencies
- Builds and signs the release APK
- Uploads artifacts with commit hash in filename

## Keystore Information

**Note**: This is an open-source application. The keystore credentials are publicly available:
- Keystore: `keystore/release.jks`
- Alias: `astrostorm`
- Store Password: `astrostorm2024`
- Key Password: `astrostorm2024`

## Architecture

```
app/
├── data/
│   ├── local/          # Room database entities and DAOs
│   ├── model/          # Data models (Planet, ZodiacSign, VedicChart, etc.)
│   └── repository/     # Repository layer
├── ephemeris/          # Swiss Ephemeris integration
├── ui/
│   ├── chart/          # Chart rendering engine
│   ├── navigation/     # Navigation setup
│   ├── screen/         # Compose screens
│   ├── theme/          # Material 3 theme
│   └── viewmodel/      # ViewModels
└── util/               # Utility classes (export, etc.)
```

## Future Enhancements

- Divisional charts (D9, D10, etc.)
- Dasha calculations
- Transit analysis
- Planetary aspects
- LLM-powered chart interpretations
- Multiple ayanamsa support
- North Indian chart style

## License

This project is open source and available for educational purposes.

## Acknowledgments

- Swiss Ephemeris by Astrodienst
- Material Design 3 by Google
- Jetpack Compose team

## Disclaimer

This application is for educational and entertainment purposes. Astrological interpretations should not be used as a substitute for professional advice.