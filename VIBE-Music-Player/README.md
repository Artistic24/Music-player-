# VIBE Music Player

Android-only native music and video player built with Kotlin, Jetpack Compose and AndroidX Media3.

## Source
The complete Android Studio project is under `VIBE-Music-Player/`.

## Build
Open that folder in Android Studio (JDK 17, Android SDK Platform 35), sync Gradle, then run `assembleDebug`.

A GitHub Actions workflow builds a debug APK on pushes to main and on manual dispatch. Download it from the workflow run's Artifacts section.

## Platform scope
This is an Android application. It does not compile into a Windows `.exe`. A Windows desktop build requires a separate desktop app target (for example, Compose Multiplatform Desktop) and desktop-specific media integration.

## Current MVP limitations
- Offline conversion currently extracts AAC audio into M4A only.
- Album artwork is currently a placeholder.
- Equalizer/bass controls and full custom playlist editing are not yet implemented.
- Runtime behavior and APK build need validation on an Android SDK/device.
