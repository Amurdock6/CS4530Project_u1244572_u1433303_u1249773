# Style Streak (CS4530 Drawing Application)

Composable Android drawing app with local persistence, cloud backup/sharing via Firebase, Google Vision label suggestions, and a custom color picker.

## Project Layout
- `app/src/main/java/cs4530/u1433303/cs4530drawingapplication/MainActivity.kt` – bootstraps Room, repositories, and ViewModels; sets the Compose content/theme and hosts navigation for `splash → auth → main → editor`.
- UI layer (Compose): `DrawingAppScreen.kt`, `DrawingCanvas.kt`, `MainActivity.kt` (MainScreen/Auth/Splash composables), `ColorPickerDialog.kt`, theme files in `ui/theme/`.
- ViewModels: `MainViewModel.kt` (local list + delete), `DrawingViewModel.kt` (canvas state, save/import, Vision), `CloudSyncViewModel.kt` (Firebase sync), `AuthViewModel.kt` (Firebase Auth state), factory in `ViewModelFactory.kt`.
- Data/local: Room setup in `data/` (`DrawingEntity.kt`, `DrawingDao.kt`, `DrawingDatabase.kt`, `Converters.kt`, `DrawingRepository.kt`).
- Cloud: Firebase wrapper `data/CloudSyncRepository.kt` with models `CloudModels.kt`.
- Vision: `CloudVisionRepository.kt` + `CloudVisionService.kt` (Ktor client in `KtorClient.kt`).
- Android resources: layouts/drawables/values/xml under `app/src/main/res/` (notably `dialog_color_picker.xml` and `xml/file_paths.xml` for sharing).

## Key Features & Flows
- **Splash**: Gradient screen with logo (`SplashScreen()`) then auto-navigates to auth or main after ~1.7s.
- **Auth**: Email/password sign-in/up using Firebase Auth (`AuthViewModel.signIn/signUp`). Errors surface in UI; sign-out returns to auth.
- **Home / Library (`MainScreen`)**:
  - Shows local drawings (Room) with thumbnails; open, delete, backup to cloud, or share via email.
  - Cloud sections: “My cloud images”, “Shared by you”, “Shared with you” sourced from Firebase. Supports importing any remote image back into the canvas, and unsharing.
  - Cloud actions use `CloudSyncViewModel` to show busy state/messages and refresh data.
- **Editor (`DrawingScreen` / `DrawingAppScreen`)**:
  - Canvas (`DrawingCanvas`) captures drag strokes with brush color/size/shape (round/square); optional background image.
  - Tools: save locally, quick OS share (FileProvider), photo import (system Photo Picker), undo, clear, toggle Vision labels overlay, color picker dialog.
  - Google Vision labels: when importing or loading a drawing, `DrawingViewModel.analyzeImage` sends the bitmap to Cloud Vision; labels can overlay on the canvas.
- **Color Picker**: `ColorPickerDialog` uses skydoves ColorPickerView + custom safe touch wrappers (`SafeColorPickerView`, `SafeBrightnessSlideBar`, `SafeAlphaSlideBar`) to avoid accidental drags inside a ScrollView; supports HEX entry, brightness/alpha sliders, live preview.
- **Sharing/Export**: Quick share writes a temporary PNG to cache and exposes it via FileProvider (`res/xml/file_paths.xml`). Email sharing to other users uploads to Firebase (see below).

## Data & Storage
- **Local persistence (Room)**:
  - Entity `DrawingEntity` stores `id`, `name` (file name), `content` (Bitmap), `createdAt`.
  - `Converters` serialize Bitmaps to ByteArray.
  - `DrawingRepository` saves/updates PNGs in `context.filesDir` and inserts rows; `getAllDrawings()` returns a Flow for UI; destructive migration fallback on DB v3.
- **Cloud backup & sharing (Firebase)**:
  - Storage paths: backups under `user_drawings/{uid}/…`, shared images under `shared_drawings/{senderId}/…`.
  - Firestore collections:
    - `user_drawings`: `{ userId, imageUrl, timestamp, title }`
    - `shared_drawings`: `{ imageUrl, senderId, receiverEmail (lowercased), timestamp, title }`
  - `CloudSyncRepository` operations: upload backup, share to email, list your backups, list items shared with your email, list items you shared, unshare (delete Firestore doc), and download bitmaps (5 MB guardrail).
- **Authentication**: Firebase email/password via `AuthViewModel`; state mirrored into `AuthUiState.user`.
- **Vision**: Google Cloud Vision `images:annotate` with `LABEL_DETECTION` and `OBJECT_LOCALIZATION`; requests built in `CloudVisionService` and sent via Ktor; labels flow back into `DrawingViewModel.uiState.visionLabels`.

## External Integrations & Config
- **Firebase**: Requires `google-services.json` in `app/`. Uses Auth, Firestore, and Storage (BOM 33.4.0). Ensure Firestore/Storage rules allow authenticated access.
- **Google Cloud Vision**: Put `CLOUD_VISION_API_KEY=...` in `app/secrets.properties`. `build.gradle.kts` injects `BuildConfig.CLOUD_VISION_API_KEY`; when empty, Vision calls short-circuit.
- **Ktor**: CIO engine with JSON content negotiation for Vision API requests.
- **Coil**: Loads remote images in lists (cloud/shared sections).
- **Android Photo Picker**: `ActivityResultContracts.PickVisualMedia` used for background imports.
- **FileProvider**: Declared in `AndroidManifest.xml` with `@xml/file_paths` (cache directory) for sharing exported PNGs.

## Running & Development Notes
- Toolchain: Kotlin 2.0.21, AGP 8.13.0, Compose Material3, Room 2.6.1; minSdk 24 / targetSdk 36.
- Required files (not checked in): `app/google-services.json`, `app/secrets.properties` with a valid Vision API key. Without these, Firebase/Vision features will fail; the rest of the app (local drawing) still works, (however, there is a build error if the file for google-services can't be found at all).
- Start activity: `cs4530.u1433303.cs4530drawingapplication.MainActivity`.
- Network permission only (`INTERNET`). Backups/sharing/Vision need connectivity and signed-in user (for Firebase).
- Database migration: `fallbackToDestructiveMigration()` wipes local rows on schema changes; adjust before shipping if persistence matters.

## File Map (common touchpoints)
- Navigation + screens: `MainActivity.kt` (AuthScreen, SplashScreen, MainScreen, DrawingScreen).
- Canvas/Tools: `DrawingAppScreen.kt`, `DrawingCanvas.kt`, `ColorPickerDialog.kt`.
- State: `AuthViewModel.kt`, `MainViewModel.kt`, `DrawingViewModel.kt`, `CloudSyncViewModel.kt`.
- Data: `data/` (Room and cloud repositories/models).
- Networking: `CloudVisionService.kt`, `CloudVisionRepository.kt`, `KtorClient.kt`.
- UI theme: `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`.
- Resources: `res/layout/dialog_color_picker.xml`, `res/xml/file_paths.xml`, `res/drawable/splash_screen_image.png`, color picker drawables.
