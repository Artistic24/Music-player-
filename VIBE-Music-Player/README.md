# VIBE Player 1.1.0

Native Android music and video player built with Kotlin, Jetpack Compose and AndroidX Media3.

## Design
VIBE opens directly into its magenta/black music library, with For You, Songs, Playlist and Video sections. The app includes a vinyl-inspired now-playing screen, swipe-access lyrics, player-style previews, a multi-theme gallery, and an animated headphone dog while music is playing.

## Music features
- Local audio and video library with search, favourites, playlists and play counts.
- Lyrics lookup with on-device caching and timed-line highlighting when LRC data is available.
- Editable song title and artist overrides with undo.
- Hide and rescan controls for the local library.
- Offline video-to-audio extraction to M4A.

## Build an APK
Open `VIBE-Music-Player/` in Android Studio with JDK 17 and Android SDK Platform 35, then run `gradle assembleDebug assembleRelease`.

GitHub Actions runs unit tests and builds both debug and release APKs. Download the release artifact named **VIBE-Player-release-apk** from the successful workflow run. The attached release APK is named **VIBE Player.apk**.

## Platform scope
This repository currently builds Android APKs. It does not produce a Windows `.exe`; a Windows desktop version requires a separate desktop app target and platform-specific media integration.
