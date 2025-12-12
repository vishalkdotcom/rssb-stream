# RSSB Stream 🎵

<p align="center">
  <img src="assets/icon.png" alt="App Icon" width="128"/>
</p>

<p align="center">
  <img src="assets/screenshot1.jpg" alt="Screenshot 1" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot2.jpg" alt="Screenshot 2" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot3.jpg" alt="Screenshot 3" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot4.jpg" alt="Screenshot 4" width="200" style="border-radius:26px;"/>
</p>

RSSB Stream is a spiritual content streaming app for Android, forked from [PixelPlay](https://github.com/theovilardo/PixelPlay). Built with Kotlin and Jetpack Compose, it provides a beautiful and seamless experience for streaming RSSB content.

## ✨ Core Features

- **Streaming Playback**: Stream spiritual content from the cloud.
- **Background Playback**: Listen while the app is in the background, thanks to a foreground service and Media3.
- **Modern UI**: A beautiful and responsive UI built with Jetpack Compose and Material 3 Expressive, supporting Dynamic Color and dark/light themes.
- **Content Library**: Organize and browse content by categories, speakers, and albums.
- **Widget**: Control playback from the home screen with a Glance-based app widget.

## 🛠️ Tech Stack & Architecture

- **Language**: 100% [Kotlin](https://kotlinlang.org/)
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a declarative and modern UI.
- **Audio Playback**: [Media3 (ExoPlayer)](https://developer.android.com/guide/topics/media/media3) for robust audio playback.
- **Architecture**: MVVM (Model-View-ViewModel) with a reactive approach using StateFlow and SharedFlow.
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) for managing dependencies.
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for local database storage.
- **Background Processing**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for background tasks.
- **Asynchronous Operations**: [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-guide.html) for managing asynchronous operations.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) for making HTTP requests.
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) for loading and caching images.

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- Android Studio Iguana | 2023.2.1 or newer.
- Android SDK 29 or newer.

### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/vishalkdotcom/rssb-stream.git
   ```
2. Open the project in Android Studio.
3. Let Gradle sync and download the required dependencies.
4. Run the app on an emulator or a physical device.

## 📂 Project Structure

The project follows the standard Android app structure:

```
app/src/main/java/com/vishalk/rssbstream/
├── data
│   ├── local       # Room database, DAOs, and entities.
│   ├── remote      # Retrofit services for network calls.
│   ├── repository  # Repositories that abstract data sources.
│   └── service     # The MusicService for background playback.
├── di              # Hilt dependency injection modules.
├── domain          # Use cases and domain models.
├── presentation    # UI-related classes.
│   ├── components  # Reusable Jetpack Compose components.
│   ├── navigation  # Navigation graph and related utilities.
│   ├── screens     # Composable screens for different parts of the app.
│   └── viewmodel   # ViewModels for each screen.
├── ui
│   ├── glancewidget # Glance App Widget implementation.
│   └── theme       # App theme, colors, and typography.
└── utils           # Utility classes and extension functions.
```

## Credits

This project is forked from [PixelPlay](https://github.com/theovilardo/PixelPlay) by Theo Vilardo.
