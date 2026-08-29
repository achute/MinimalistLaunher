# Minimalist Launcher

An offline, text-only, privacy-first minimalist Android home launcher engineered with **Jetpack Compose** and modern **Android Architecture Components**. Designed to eliminate digital distractions, curb doom-scrolling, and provide intentional smartphone interaction through elegant typography, granular focus modes, digital wellbeing analytics, and embedded widget hosting.

---

## Highlights & Features

### 1. Distraction-Free Typography Home Screen
* **Clean Text Layout**: Replaces colorful, addictive app icon badges with clean, monochrome typographic labels.
* **Customizable Clock Header**: Displays digital time with toggles for 24-hour format, seconds display, and date.
* **Typographic Themes**: Choose between *Retro Monospace*, *Clean Sans*, *Elegant Serif*, *Digital Matrix*, *Terminal VT100*, and *Typewriter* styles.
* **Custom Font Engine**: Import any `.ttf` or `.otf` font file from your device storage with an option to apply custom typography across the entire launcher interface.

### 2. Contextual Focus Profiles & Private Space Coupling
* **5 Quick-Access Focus Slots**: Keep your 5 essential apps right on the home screen.
* **Custom Profiles**: Create and switch between distinct focus configurations (e.g. *Deep Work*, *Minimal*, *Evening & Wind Down*, *Private Vault*).
* **Private Space Tied Modes**: Configure focus profiles to require an unlocked Private Space (`requiresPrivateSpace = true`). If Private Space is locked, the launcher automatically reverts to the default safe profile, keeping private apps hidden and locked.
* **Auto-Lock & Unlock Sync**: Automatically locks Private Space when switching to standard focus profiles, or triggers biometric unlock when switching to private profiles.
* **Visual Status Indicators**: Profiles requiring Private Space display `[PVT]` badges and discrete lock state tags in the quick switcher.

### 3. Utility & Health Dashboard (Swipe Left)
* **Real-Time Battery Telemetry**: Live status card tracking battery percentage, charging state, power source (AC Fast Charging, USB, Wireless), battery temperature in °C, voltage, and health status.
* **Digital Wellbeing Insights**: Live daily screen-time metrics, breakdown of top-used applications, and percentage-of-day usage distribution.
* **Minimalist Task Manager**: Integrated offline to-do list with priority flags, task completion toggles, and one-tap clearing of finished tasks.
* **AppWidgetHost Integration**: Embed native system widgets (e.g., system battery or weather widgets) directly inside the launcher without needing internet access.

### 4. Searchable App Drawer & Native Private Space (Swipe Up)
* **Instant Text Search**: Instant fuzzy filtering across all installed applications.
* **Visual `[PVT]` Indicator**: Private Space apps are clearly distinguished with a `[PVT]` prefix across the app drawer, focus slots, and quick pickers.
* **Recent Apps Strip**: Quick access to recently used applications powered by Android Usage Stats.
* **Native Android 15 Private Space**: Seamlessly integrates with Android 15's native Private Space user profile. Appears as a discrete pill at the bottom of the drawer, unlocking with system authentication (`UserManager.requestQuietModeEnabled()`) and collapsing/locking with a single tap.
* **Granular App Management (Long-Press)**:
  * Pin to any Focus Profile slot (automatically flags profile as private if pinning a private app).
  * Map to bottom quick-action slots.
  * Assign custom app aliases (rename app labels).
  * Configure daily screentime limits.
  * Direct access to Android App Info or Uninstall.

### 5. Digital Wellbeing & App Screentime Limits
* **Per-App Time Limits**: Set custom daily time allowances in minutes for distracting apps.
* **Intelligent Enforcer**: Employs an Accessibility Service (`AppBlockerAccessibilityService`) and system overlays to intercept app launches once the daily budget is exhausted, presenting a mindful block screen with snooze and home navigation options.

### 6. 4-Way Gesture Navigation
* **Swipe Up**: Open Fullscreen App Drawer & Search.
* **Swipe Down**: Expand Android Notification Shade and Quick Settings.
* **Swipe Left**: Open Utility & Health Dashboard.
* **Swipe Right**: Quick Launch custom action (Default Browser, Search, Camera, Phone, or custom app).

### 7. Customization & Visual Themes
* **Theme Palettes**: *Monochromatic Dark*, *Minimal Light*, *OLED Pure Black*, *Solarized Slate*, *Nordic Cold*, *Amber Phosphor CRT*, *Matrix Green*, and *Sepia Paper*.
* **Wallpaper Dimming**: Support for wallpaper pass-through with adjustable dimming opacity or solid minimalist backgrounds.

---

## Zero-Internet Privacy Philosophy

Minimalist Launcher does **not** declare or use the `android.permission.INTERNET` permission.
* **No Analytics or Telemetry**: Zero data collection, trackers, or network sockets.
* **100% Local Persistence**: All preferences, tasks, focus configurations, and custom labels reside exclusively in local on-device SQLite databases (Room) and encrypted storage.

For a full list of all system permissions required and their justifications, see [PERMISSIONS.md](PERMISSIONS.md).

---

## Tech Stack & Architecture

* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3) design tokens.
* **Language & Concurrency**: Kotlin 2.0+, Kotlin Coroutines, and reactive `StateFlow` / `callbackFlow`.
* **Architecture**: Clean MVVM (Model-View-ViewModel) pattern with single source of truth state management.
* **Local Persistence**:
  * **AndroidX Room**: SQLite abstraction for focus profiles, tasks, custom labels, app limits, and hidden apps.
  * **AndroidX DataStore**: Asynchronous key-value store for user preferences and theme settings.
* **Security**:
  * **Android KeyStore**: Hardware-backed cryptographic key generation (`AES/GCM/NoPadding`).
  * **AndroidX Biometric**: Biometric prompt integration with hardware security fallbacks.
* **System Integration**:
  * **AppWidgetHost & AppWidgetManager**: Native Android widget hosting.
  * **AccessibilityService**: Real-time window state monitoring for screentime limit enforcement.
  * **UsageStatsManager**: On-device screentime analytics.

---

## Project Structure

```text
app/src/main/
├── AndroidManifest.xml             # Declares launcher intent-filters, permissions, and services
├── java/com/example/
│   ├── MainActivity.kt             # Launcher root activity, gesture handlers, and widget lifecycle
│   ├── MinimalistApp.kt            # Application subclass initializing Room DB and AppWidgetHost
│   ├── data/
│   │   ├── AppDatabase.kt          # Room Database definition & type converters
│   │   ├── LauncherDao.kt          # DAO queries for profiles, tasks, limits, labels, and widgets
│   │   └── PreferencesDataStore.kt # DataStore repository for settings and themes
│   ├── model/
│   │   ├── Models.kt               # Entity and domain models (FocusProfile, TaskItem, AppLimitRule, etc.)
│   │   └── OpenSourceLibrary.kt    # Open-source licenses catalog
│   ├── service/
│   │   └── AppBlockerAccessibilityService.kt # Accessibility service enforcing digital wellbeing limits
│   ├── ui/
│   │   ├── BlockScreenActivity.kt  # Activity shown when daily screen time quota is reached
│   │   ├── components/             # Reusable Composables (AppDrawerSheet, ClockHeader, Dashboard, Dialogs, etc.)
│   │   ├── theme/                  # Color, Typography, and M3 Theme definitions
│   │   └── viewmodel/              # LauncherViewModel orchestrating state and domain operations
│   ├── util/
│   │   ├── AppManager.kt           # Package manager operations, app launching, and system intents
│   │   ├── BatteryHelper.kt        # BroadcastReceiver flow for live battery statistics
│   │   ├── BiometricAuthHelper.kt  # AndroidX BiometricPrompt helper
│   │   ├── CryptoUtil.kt           # Android KeyStore AES-GCM cipher encryption/decryption
│   │   ├── FontManager.kt          # Dynamic TTF/OTF typeface loading and SAF file importer
│   │   └── UsageStatsHelper.kt     # UsageStatsManager aggregation and formatting
│   └── widget/
│       ├── AppWidgetHostManager.kt # App widget allocation, picking, and binding logic
│       ├── EmbeddedWidgetView.kt   # AndroidView wrapper hosting custom AppWidgetHostViews
│       └── LauncherAppWidgetHost.kt# Custom AppWidgetHost implementation
└── res/                            # Vector drawables, mipmaps, strings, and accessibility configs
```

---

## Building and Running

### Prerequisites
* Android Studio Ladybug / Koala or newer
* JDK 17+
* Android SDK (API Level 35 compileSdk, minSdk 26)

### Build Commands
```bash
# Assemble debug APK
./gradlew :app:assembleDebug
```

---

## License

This project is open-source. For detailed library licenses, refer to the in-app **Open Source Licenses** viewer in Settings or check [OpenSourceLibrary.kt](app/src/main/java/com/example/model/OpenSourceLibrary.kt).
