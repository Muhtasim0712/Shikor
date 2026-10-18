# Shikor (শিকড়) — Android Family Heritage & Lineage Archive

[![Platform](https://img.shields.io/badge/Platform-Android%2024%2B-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-100%25%20Java-orange.svg)](https://www.java.com)
[![UI](https://img.shields.io/badge/UI-Material%203%20Design-blue.svg)](https://m3.material.io)
[![Database](https://img.shields.io/badge/Database-Room%202.6.1-red.svg)](https://developer.android.com/training/data-storage/room)
[![Backend](https://img.shields.io/badge/Cloud-Firebase%20Firestore%20%2B%20Auth-amber.svg)](https://firebase.google.com)

**Shikor** (meaning *“Root”* in Bengali) is an authentic, offline-first Android application designed to preserve family heritage, ancestral roots, oral histories, and generational lineage. It combines custom hardware-accelerated 2D Canvas tree visualizations, elder audio recordings, daily remembrance alarms, relationship discovery, and cloud synchronization—crafted strictly in **100% Java and XML** adhering to modern Android architecture principles.

---

## Table of Contents
1. [Project Overview & Key Features](#project-overview--key-features)
2. [Setup & Installation Guide](#setup--installation-guide)
3. [Exhaustive Syllabus Concept Mapping](#exhaustive-syllabus-concept-mapping)
4. [Component Communication Diagram](#component-communication-diagram)
5. [Activity & Component Lifecycle Log Guide](#activity--component-lifecycle-log-guide)
6. [Permissions Architecture & Rationale](#permissions-architecture--rationale)
7. [Known Limitations & Design Considerations](#known-limitations--design-considerations)

---

## Project Overview & Key Features

- **Interactive Canvas Family Tree (`FamilyTreeView`)**: Dual-mode rendering offering both a living, organic botanical tree (ancestors rooted at trunk, generations growing upward with organic branch sway) and a classic organizational genealogy chart. Supports pinch-to-zoom (0.4x to 3.0x), flinging, smooth pan, and instant node tap hit-testing.
- **Family Member Directory (`MembersFragment`, `PersonDetailActivity`)**: Full profile management with living/deceased indicators, parental/spousal relations, hometowns, contact numbers, and instant Islamic Hijri date calculations via offline-first Room caching (`HijriCacheEntity`).
- **Kinship Terminology & Relationship Finder (`RelationshipFinderActivity`, `KinshipResolver`)**: Graph BFS algorithm that analyzes family relationships against an XML taxonomy (`kinship_terms.xml`) to calculate connection paths and display authentic Bengali kinship terms (e.g., চাচা, মামা, খালা, ফুফু, দাদা, নানা, ভাতিজা).
- **Elder Audio & Story Archive (`AddStoryActivity`, `AudioRecordingService`, `StoryDetailActivity`)**: Foreground audio recording service with persistent microphone notifications, started/bound service communication, dynamic state broadcasts, and custom `MediaPlayer` playback with scrubbing `SeekBar`.
- **Photo Timeline with Milestones (`TimelineFragment`)**: Chronological memory stream sectioned by year headers with multi-member face tagging.
- **Reminders & Remembrance Alarms (`AlarmScheduler`, `ReminderReceiver`)**: Daily exact alarms for birthdays and death anniversaries with heads-up notifications, action buttons ("Call", "Snooze"), and phone reboot auto-rescheduling (`BootReceiver`).
- **Data Export & Safe Sharing (`TreeExporter`, `ExportActivity`, `FamilyProvider`)**: Export full graphical family trees as high-resolution PNG images or multi-page archival PDF reports (`PdfDocument`), plus vCard (.vcf) and CSV export/import via `FileProvider`.
- **Cloud Synchronization & Status Indicator (`SyncManager`, `MainActivity`)**: Automatic background synchronization between Room and Firebase Firestore with connectivity callbacks and a reactive toolbar status chip.
- **Tablet Optimization (`sw600dp`) & Dual Language**: Adaptive tablet layouts with Material 3 `NavigationRailView`, side-by-side selectors, and comprehensive English / Bengali localization.

---

## Setup & Installation Guide

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or later recommended.
- **Java Development Kit (JDK)**: JDK 17 (embedded with Android Studio).
- **Android SDK**: `compileSdk = 37`, `minSdk = 24`, `targetSdk = 34`.
- **Physical Device or Android Emulator**: Running Android 7.0 (API 24) or higher (Android 13+ recommended to verify runtime notification permissions).

### Building and Running
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Muhtasim0712/Shikor.git
   cd Shikor
   ```
2. **Firebase Configuration**:
   - Ensure `google-services.json` is placed in the `app/` directory (`app/google-services.json`).
3. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug --no-daemon
   ```
4. **Install to Connected Device / Emulator**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Exhaustive Syllabus Concept Mapping

Every single Android development syllabus concept is implemented in production code without placeholders or shortcuts:

| Category | Syllabus Concept | Exact Class / File Implementation | Description |
| :--- | :--- | :--- | :--- |
| **Components** | Application Class | [`ShikorApp.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ShikorApp.java) | App-wide singleton initialization, notification channels creation, and theme preference restoration. |
| | Base Activity & Lifecycle Logging | [`BaseActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/base/BaseActivity.java) | Centralized lifecycle callback logging (`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`) and edge-to-edge window insets. |
| | Activities | [`SplashActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/auth/SplashActivity.java)<br>[`LoginActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/auth/LoginActivity.java)<br>[`RegisterActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/auth/RegisterActivity.java)<br>[`FamilySetupActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/auth/FamilySetupActivity.java)<br>[`MainActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/main/MainActivity.java)<br>[`PersonDetailActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/person/PersonDetailActivity.java)<br>[`AddEditPersonActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/person/AddEditPersonActivity.java)<br>[`AddStoryActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/story/AddStoryActivity.java)<br>[`StoryDetailActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/story/StoryDetailActivity.java)<br>[`RelationshipFinderActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/tools/RelationshipFinderActivity.java)<br>[`ExportActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/tools/ExportActivity.java) | Distinct activities with explicit tasks, back stack management, and intent handling. |
| | Fragments & FragmentManager | [`TreeFragment.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/main/TreeFragment.java)<br>[`MembersFragment.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/main/MembersFragment.java)<br>[`TimelineFragment.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/main/TimelineFragment.java)<br>[`SettingsFragment.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/main/SettingsFragment.java) | Modular dashboard screens swapped dynamically via `getSupportFragmentManager().beginTransaction().replace()`. |
| | Foreground Service (Started & Bound) | [`AudioRecordingService.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/service/AudioRecordingService.java) | Background elder voice recording with persistent notification (`foregroundServiceType="microphone"`), `IBinder` binding, and amplitude sampling. |
| | Static Broadcast Receivers | [`ReminderReceiver.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/receiver/ReminderReceiver.java)<br>[`ReminderActionReceiver.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/receiver/ReminderActionReceiver.java)<br>[`BootReceiver.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/receiver/BootReceiver.java) | Manifest-declared receivers for scheduled alarm firing, notification actions ("Call", "Snooze"), and boot restoration. |
| | Dynamic Broadcast Receiver | [`AddStoryActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/story/AddStoryActivity.java) (`RecordingStateReceiver`) | Context-registered receiver listening to `AudioRecordingService.ACTION_RECORDING_STATE` for real-time timer and waveform updates. |
| | ContentProvider & Custom Signature Permission | [`FamilyProvider.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/provider/FamilyProvider.java)<br>[`AndroidManifest.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/AndroidManifest.xml) | Custom ContentProvider exposing Room data via `UriMatcher` and Cursor queries, protected by custom signature permission `com.shikor.app.permission.READ_FAMILY`. |
| | ContentResolver Client | [`ExportActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/tools/ExportActivity.java) | Consumes `FamilyProvider` via `getContentResolver().query(FamilyProvider.CONTENT_URI, ...)` to generate contact directories. |
| **Data & Storage** | Room Database | [`AppDatabase.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/AppDatabase.java) | Singleton Room database with Version 2 schema, TypeConverters, and migration rules. |
| | Room DAOs | [`PersonDao.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/daos/PersonDao.java)<br>[`StoryDao.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/daos/StoryDao.java)<br>[`PhotoDao.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/daos/PhotoDao.java)<br>[`FamilyDao.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/daos/FamilyDao.java)<br>[`HijriDao.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/daos/HijriDao.java) | Data access objects with reactive `LiveData` queries, SQL `LIKE` search, Cursor queries, and pending sync filter methods. |
| | Room Entities | [`PersonEntity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/entities/PersonEntity.java)<br>[`StoryEntity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/entities/StoryEntity.java)<br>[`PhotoEntity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/entities/PhotoEntity.java)<br>[`FamilyEntity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/entities/FamilyEntity.java)<br>[`HijriCacheEntity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/entities/HijriCacheEntity.java) | Relational tables modeling family units, stories, photos, and cached Islamic conversions. |
| | Room Migration | [`AppDatabase.MIGRATION_1_2`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/AppDatabase.java) | Safe database migration script altering `people` table to append `phoneNumber`. |
| | Room TypeConverter | [`StringListConverter.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/local/converters/StringListConverter.java) | Converts `List<String>` of tagged person IDs to comma-delimited strings for Room persistence. |
| | ViewModel & LiveData | [`PersonViewModel.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/person/PersonViewModel.java)<br>[`StoryViewModel.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/story/StoryViewModel.java)<br>[`FamilyViewModel.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/family/FamilyViewModel.java) | MVVM architecture holding reactive state across configuration changes. |
| | SharedPreferences | [`Prefs.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/util/Prefs.java) | Encapsulates app settings, session tokens, tree view modes, reminder times, language, and theme modes. |
| | FileProvider Sharing | [`file_paths.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/xml/file_paths.xml)<br>[`TreeExporter.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/util/TreeExporter.java)<br>[`ExportActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/tools/ExportActivity.java) | Safe content sharing via `content://` URIs with `FLAG_GRANT_READ_URI_PERMISSION`. |
| **Networking & Cloud** | Low-level HttpURLConnection | [`AladhanClient.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/network/AladhanClient.java) | Standard Java HTTP networking fetching `https://api.aladhan.com/v1/gToH` over TLS without heavy third-party dependencies. |
| | JSON Parsing | [`HijriParser.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/network/HijriParser.java) | Parses Aladhan JSON payloads into `HijriCacheEntity` models. |
| | XML Pull Parsing | [`KinshipResolver.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/util/KinshipResolver.java) | Parses `res/xml/kinship_terms.xml` using native Android `XmlPullParser`. |
| | Firebase Authentication | [`LoginActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/auth/LoginActivity.java)<br>[`RegisterActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/auth/RegisterActivity.java) | Email/Password user authentication with password reset and persistent tokens. |
| | Firebase Firestore Sync | [`FamilyRepository.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/data/repository/FamilyRepository.java)<br>[`SyncManager.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/sync/SyncManager.java) | Real-time cloud sync using snapshot listeners and offline mutation queues (`SYNC_STATE_PENDING`). |
| | Network Monitoring | [`SyncManager.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/sync/SyncManager.java) | Listens for connectivity state changes using `ConnectivityManager.NetworkCallback`. |
| **Alarms & Background** | Exact Alarms | [`AlarmScheduler.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/util/AlarmScheduler.java) | Schedules alarms using `AlarmManager.setExactAndAllowWhileIdle()` to guarantee delivery across Doze mode. |
| | Notification Channels & Actions | [`NotificationHelper.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/util/NotificationHelper.java) | Creates separate Notification Channels with heads-up priority, custom vibration, and interactive action buttons. |
| | Android 13+ Runtime Permissions | [`MainActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/main/MainActivity.java)<br>[`SettingsFragment.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/main/SettingsFragment.java) | `ActivityResultContracts.RequestPermission` handling for `POST_NOTIFICATIONS`. |
| **UI, Canvas & Media** | Custom 2D Canvas & Matrix | [`FamilyTreeView.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/view/FamilyTreeView.java) | Interactive hardware-accelerated Canvas with matrix transformation, scale gestures (0.4x - 3.0x), flinging, and sway animations. |
| | Tree Layout Algorithm | [`TreeLayoutEngine.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/view/TreeLayoutEngine.java) | Calculates hierarchical non-overlapping coordinates recursively for couples and generational branches. |
| | Display-List Caching | [`FamilyTreeView.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/view/FamilyTreeView.java) (`android.graphics.Picture`) | Pre-records complex static roots, trunk, and branches into `Picture` display lists for sustained 60 FPS rendering. |
| | PDF Document Generation | [`TreeExporter.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/util/TreeExporter.java) | Generates multi-page A4 archival PDF reports using native `android.graphics.pdf.PdfDocument`. |
| | PNG Graphic Capture | [`FamilyTreeView.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/view/FamilyTreeView.java) (`exportFullTreeBitmap`) | Renders the complete family tree diagram into a high-resolution Bitmap. |
| | RecyclerView Multi-View Types | [`StoryAdapter.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/story/StoryAdapter.java)<br>[`TimelineAdapter.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/adapter/TimelineAdapter.java) | Adapters handling multiple view types (Text/Audio cards vs Photo cards; Year headers vs Memory cards). |
| | Material Design 3 Components | Layouts across `app/src/main/res/layout/` | MaterialToolbar, BottomNavigationView, NavigationRailView, ExtendedFloatingActionButton, MaterialCardView, MaterialSwitch, MaterialTimePicker, Chips. |
| | Tablet Optimization (`sw600dp`) | [`layout-sw600dp/activity_main.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/layout-sw600dp/activity_main.xml)<br>[`layout-sw600dp/fragment_settings.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/layout-sw600dp/fragment_settings.xml)<br>[`layout-sw600dp/activity_relationship_finder.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/layout-sw600dp/activity_relationship_finder.xml)<br>[`layout-sw600dp/activity_export.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/layout-sw600dp/activity_export.xml) | Responsive multi-pane layouts utilizing `NavigationRailView` and constrained ergonomic max-widths. |
| | Dark Mode | [`values-night/colors.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/values-night/colors.xml)<br>[`values-night/themes.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/values-night/themes.xml) | Calibrated low-saturation dark palette featuring Deep Moss, Forest Charcoal, and Amber Bark. |
| | Full Bangla Localization | [`values-bn/strings.xml`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/res/values-bn/strings.xml) | Authentic Bengali strings for every label, button, navigation item, kinship title, and notification. |
| | Accessibility (a11y) | Layouts across `app/src/main/res/layout/` | Minimum 48dp touch targets, semantic content descriptions, and `importantForAccessibility="no"` on decorative visuals. |

---

## Component Communication Diagram

The following text-based architecture diagram illustrates how components interact across UI, services, data persistence, and cloud layers:

```
+----------------------------------------------------------------------------------------------------+
|                                         PRESENTATION LAYER                                         |
|                                                                                                    |
|  [ MainActivity ] <=======> [ TreeFragment / MembersFragment / TimelineFragment / SettingsFragment ] |
|         |                                      |                               |                   |
|         | Intent                               | LiveData Observer             | Intent            |
|         v                                      v                               v                   |
|  [ PersonDetailActivity ] <---------> [ ViewModels ] ----------> [ RelationshipFinderActivity ]    |
|  [ AddEditPersonActivity ]    (PersonViewModel, StoryViewModel)   [ ExportActivity ]               |
|  [ AddStoryActivity ] <---+                    |                                 |                 |
|  [ StoryDetailActivity ]  |                    | Direct Queries                  | ContentResolver |
+---------------------------|--------------------|---------------------------------|-----------------+
                            |                    v                                 v                 
                Bind/Start  |          +--------------------+             +------------------+       
                            |          |  FamilyRepository  |             |  FamilyProvider  |       
                            |          +--------------------+             +------------------+       
                            |                    |                                 |                 
                            v                    v Room DAOs                       v Room Cursor     
                 +-----------------------+     +----------------------------------------+            
                 | AudioRecordingService |     |          AppDatabase (Room)            |            
                 |  (Foreground Service) |     |  (people, stories, photos, hijri_cache)|            
                 +-----------------------+     +----------------------------------------+            
                            |                                      ^                                 
                            | Dynamic Broadcast                    | Offline Mutations & Cache       
                            v                                      v                                 
                 [ RecordingStateReceiver ]                 +------------------+                     
                                                            |   SyncManager    |                     
                                                            | (NetworkCallback)|                     
                                                            +------------------+                     
                                                                   ^   |                             
                                                  Snapshot Listener|   | Mutation Uploads            
                                                                   |   v                             
                                                       +------------------------+                    
                                                       |   Firebase Firestore   |                    
                                                       +------------------------+                    

+----------------------------------------------------------------------------------------------------+
|                                     BACKGROUND & ALARMS SUBSYSTEM                                  |
|                                                                                                    |
|   [ SettingsFragment ] ---> [ AlarmScheduler ] ---> (AlarmManager.setExactAndAllowWhileIdle)       |
|                                                              |                                     |
|                                                              v Triggers at preferred time          |
|   [ Phone Reboot ] ------------> [ BootReceiver ] ---> [ ReminderReceiver ]                        |
|   (ACTION_BOOT_COMPLETED)         (Reschedules alarms)       |                                     |
|                                                              v Builds notification                 |
|                                                    [ NotificationHelper ]                          |
|                                                              |                                     |
|                                              Heads-Up Alert  v With Action Buttons                 |
|                                        [ Reminder Notification Shade ]                             |
|                                         ├── "Call"   --> (ACTION_DIAL Intent)                      |
|                                         └── "Snooze" --> [ ReminderActionReceiver ]                |
+----------------------------------------------------------------------------------------------------+
```

---

## Activity & Component Lifecycle Log Guide

### Centralized Lifecycle Logging
All activities in Shikor inherit from [`BaseActivity.java`](file:///c:/Users/Nafij/Desktop/Shikor/app/src/main/java/com/shikor/app/ui/base/BaseActivity.java). Every lifecycle state transition is automatically logged with the consistent tag:
```
ShikorLifecycle
```

When navigating through the app, rotating the screen, or placing the app in the background, you can inspect the exact sequence in Logcat:
```text
D/ShikorLifecycle: [MainActivity] onCreate() called
D/ShikorLifecycle: [MainActivity] onStart() called
D/ShikorLifecycle: [MainActivity] onResume() called
D/ShikorLifecycle: [MainActivity] onPause() called
D/ShikorLifecycle: [PersonDetailActivity] onCreate() called
D/ShikorLifecycle: [PersonDetailActivity] onStart() called
D/ShikorLifecycle: [PersonDetailActivity] onResume() called
D/ShikorLifecycle: [MainActivity] onStop() called
```

### Logcat Filtering Commands
Run these commands from your terminal to monitor specific subsystems:

1. **Monitor Activity Lifecycle Transitions**:
   ```bash
   adb logcat -v time -s ShikorLifecycle
   ```
2. **Monitor Cloud Synchronization & Network State**:
   ```bash
   adb logcat -v time -s SyncManager,FamilyRepository
   ```
3. **Monitor Reminders, Alarms & Receivers**:
   ```bash
   adb logcat -v time -s AlarmScheduler,ReminderReceiver,ReminderActionReceiver,BootReceiver
   ```
4. **Monitor Foreground Audio Recording Service**:
   ```bash
   adb logcat -v time -s AudioRecordingService
   ```
5. **Combined Comprehensive Audit**:
   ```bash
   adb logcat -v time -s ShikorLifecycle,SyncManager,AlarmScheduler,FamilyProvider
   ```

---

## Permissions Architecture & Rationale

| Permission | Protection Level | Purpose & Technical Rationale |
| :--- | :--- | :--- |
| `android.permission.INTERNET` | Normal | Required for Aladhan Hijri API queries, Firebase Firestore cloud synchronization, and remote image loading. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Monitored by `SyncManager` via `ConnectivityManager.NetworkCallback` to adapt between offline and online states dynamically. |
| `android.permission.RECORD_AUDIO` | Dangerous (Runtime) | Required by `AudioRecordingService` and `AddStoryActivity` to record elder voice stories using `MediaRecorder`. |
| `android.permission.FOREGROUND_SERVICE` | Normal | Required on Android 9+ (API 28) to run `AudioRecordingService` without being killed by background execution limits. |
| `android.permission.FOREGROUND_SERVICE_MICROPHONE` | Normal | Explicit foreground service type declaration mandated on Android 14+ (API 34) for microphone usage. |
| `android.permission.POST_NOTIFICATIONS` | Dangerous (Runtime) | Android 13+ (API 33) runtime permission requested in `MainActivity` and `SettingsFragment` before showing remembrance alerts or recording notifications. |
| `android.permission.SCHEDULE_EXACT_ALARM` | Normal (with settings toggle) | Required on Android 12+ (API 31) for `AlarmScheduler` to trigger daily birthday and anniversary alerts at the user's preferred minute. |
| `android.permission.USE_EXACT_ALARM` | Normal | Fallback exact alarm capability for alarms and timers on Android 13+. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Normal | Allows `BootReceiver` to wake up when the device finishes booting and reinstate scheduled remembrance alarms into `AlarmManager`. |
| `com.shikor.app.permission.READ_FAMILY` | **Signature** | Custom signature-level permission declared in `AndroidManifest.xml` restricting access to `FamilyProvider` solely to applications signed with the same developer certificate. |

---

## Known Limitations & Design Considerations

1. **Aladhan API Rate Limits & Initial Lookup**:
   - The Aladhan Islamic Calendar API is queried over HTTP via `AladhanClient`. While lookups are cached offline indefinitely in Room (`HijriCacheEntity`), the very first time a unique birth date is viewed requires an active internet connection. If offline, the profile gracefully falls back to displaying the Gregorian date.
2. **OEM Battery Optimization & Exact Alarms**:
   - While `AlarmScheduler` utilizes `setExactAndAllowWhileIdle()` and requests the exact alarm permission on Android 12+, certain aggressive OEM battery optimization skins (e.g., Xiaomi MIUI/HyperOS, Huawei EMUI, Samsung OneUI sleep modes) may delay exact background alarms unless the user disables battery optimization for Shikor in system settings. A direct shortcut is provided in Settings (`btnGrantExactAlarm`).
3. **Very Large Trees (> 1,000 members)**:
   - The custom Canvas tree layout engine (`TreeLayoutEngine`) and `TreePainter` compute coordinates recursively off the main thread and cache static structures into `android.graphics.Picture`. While tested up to several hundred nodes at sustained 60 FPS, trees with thousands of nodes are best navigated using the generation selector or pinch-zoom overview (`fitWholeTree`).
4. **Android FileProvider Scoping**:
   - PDF, PNG, CSV, and vCard exports are stored in the app's secure internal cache directory (`getCacheDir()/exports`) and granted temporary read access to third-party apps (WhatsApp, Drive, Gmail) using `FLAG_GRANT_READ_URI_PERMISSION`. This avoids requiring legacy external storage permissions on modern Android versions.
