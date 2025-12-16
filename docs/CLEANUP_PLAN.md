# Cleanup Plan: Transition to Remote-Only Architecture

## Goal
Remove all code related to local file scanning, local library management, and legacy local-content screens. The app will focus exclusively on remote content (RSSB).

## Checklist

### 1. Navigation & UI
- [ ] **AppNavigation.kt**: Remove legacy composables:
    - `HomeScreen`, `SearchScreen`, `LibraryScreen` (Local versions)
    - `SettingsScreen` (Keep, but modify)
    - `DailyMixScreen`, `StatsScreen`, `PlaylistDetailScreen`, `GenreDetailScreen`
    - `AlbumDetailScreen`, `ArtistDetailScreen`, `MashupScreen`
    - `NavBarCornerRadiusScreen`, `EditTransitionScreen`
- [ ] **MainActivity.kt**: Remove references to `Screen.kt` objects. Update `routesWithHiddenNavigationBar`.
- [ ] **SettingsScreen.kt**:
    - Remove "Music Management" section.
    - Remove "Developer Options" (Daily Mix).
    - Remove `FileExplorerBottomSheet` integration.
- [ ] **Screen.kt**: Delete this file. Migrate `Settings` and `About` to `RssbScreen`.

### 2. ViewModels
- [ ] **SettingsViewModel**: Remove `loadDirectory`, `refreshLibrary`, `allowedDirectories`.
- [ ] **PlayerViewModel**:
    - Remove `MusicRepository` injection.
    - Remove references to `MusicDao` / `SongEntity` calls for local files.
    - Ensure `play(song)` works for remote content.
    - Refactor to use `RemoteContentRepository` (or equivalent) for initial data loading.
    - Remove local scanning, WiFi/Bluetooth scanning logic if unused for remote cast.
- [ ] **PlaylistViewModel**: Delete this file.
- [ ] **LibraryViewModel**: Delete if exists/unused.

### 3. Data Layer
- [ ] **Repositories**: Delete `MusicRepository.kt` and `MusicRepositoryImpl.kt`.
- [ ] **Entities**: Delete `SongEntity.kt`, `AlbumEntity.kt`, `ArtistEntity.kt`.
- [ ] **DAOs**: Delete `MusicDao.kt`.
- [ ] **Database**: Update `RssbStreamDatabase.kt` to remove above entities/DAOs and increment version.
- [ ] **Models**: Delete `MusicFolder.kt`, `DirectoryItem.kt`.
- [ ] **Services/Managers**:
    - Delete `SyncManager.kt`
    - Delete `DailyMixManager.kt`
    - Delete `SongMetadataEditor.kt`
    - Delete `MediaFileHttpServerService.kt`
    - Delete `AiPlaylistGenerator.kt`

### 4. Legacy Files
- [ ] Delete `HomeScreen.kt` (local version)
- [ ] Delete `LibraryScreen.kt` (local version)
- [ ] Delete `SearchScreen.kt` (local version)
- [ ] Delete `DailyMixScreen.kt`, `StatsScreen.kt`
- [ ] Delete `PlaylistDetailScreen.kt`, `GenreDetailScreen.kt`, `AlbumDetailScreen.kt`, `ArtistDetailScreen.kt`
- [ ] Delete `MashupScreen.kt`
- [ ] Delete `NavBarCornerRadiusScreen.kt`, `EditTransitionScreen.kt`
- [ ] Delete `FileExplorerBottomSheet.kt`

## Execution Strategy
Work on these items in separate batches (Navigation, ViewModels, Data) to ensure stability.
