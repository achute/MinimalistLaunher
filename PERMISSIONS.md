# Permissions Guide — Minimalist Launcher

This document provides a complete, transparent breakdown of all Android system permissions used by **Minimalist Launcher**, the rationale behind each permission, and our strict **Zero-Internet Privacy Guarantee**.

---

## Zero-Internet Guarantee (Privacy First)

> **`android.permission.INTERNET` is NOT requested or included.**

- **100% Offline-First**: Minimalist Launcher has **zero network capabilities**. It cannot connect to the internet, send telemetry, transmit analytics, or upload your data to any remote servers.
- **Local Storage Only**: All settings, focus profiles, task items, custom aliases, screentime stats, and hidden app configurations are stored locally on your device using Android Room SQLite and encrypted preferences.

---

## Summary of Permissions

| Permission | Android Name | Level / Category | Required For |
| :--- | :--- | :--- | :--- |
| **Query All Packages** | `android.permission.QUERY_ALL_PACKAGES` | System / Launcher | Discovering and displaying all installed apps in the app drawer, focus lists, and search. |
| **Usage Stats** | `android.permission.PACKAGE_USAGE_STATS` | Special App Access | Tracking daily app screen time, showing top-used apps on the dashboard, and populating recent apps. |
| **Accessibility Service** | `android.permission.BIND_ACCESSIBILITY_SERVICE` | Special Access | Monitoring app launches in real-time to enforce user-configured daily digital wellbeing limits. |
| **Display Over Other Apps** | `android.permission.SYSTEM_ALERT_WINDOW` | Special App Access | Displaying the block screen overlay when a daily app screentime limit is reached. |
| **Biometric Authentication** | `android.permission.USE_BIOMETRIC` | Normal | Unlocking the encrypted Private Space / Hidden Apps vault with fingerprint, face, or device PIN. |
| **Fingerprint (Legacy)** | `android.permission.USE_FINGERPRINT` | Normal (Legacy API) | Backward-compatible fingerprint authentication on older Android versions (API < 28). |
| **Expand Status Bar** | `android.permission.EXPAND_STATUS_BAR` | Normal | Pulling down the notification panel and quick settings via the swipe-down gesture. |

---

## Detailed Rationale & Usage

### 1. `android.permission.QUERY_ALL_PACKAGES`
* **Declaration**: `<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />`
* **Category**: Standard Home Launcher Requirement
* **Why the app needs it**:
  As an Android Home replacement application (`android.intent.category.HOME` & `android.intent.category.LAUNCHER`), Minimalist Launcher needs to query the `PackageManager` to retrieve the list of all applications installed on your device.
* **How it is used**:
  - Populates the searchable **App Drawer**.
  - Allows you to select apps for **Focus Mode Profiles** and **Bottom Quick Action Slots**.
  - Enables launching, renaming, and uninstalling apps directly from the launcher.

---

### 2. `android.permission.PACKAGE_USAGE_STATS`
* **Declaration**: `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`
* **Category**: Special App Access ("Usage Access" in Android Settings)
* **Why the app needs it**:
  Enables the launcher to read daily device usage metrics via Android's `UsageStatsManager`.
* **How it is used**:
  - Displays the **Digital Wellbeing** card in the Utility Dashboard with your top 5 most-used apps and daily screen time totals.
  - Dynamically populates the **Recent Apps** strip at the top of the App Drawer.
  - Evaluates whether an app has reached its daily time limit.
* **User Control**: This permission is optional. If not granted, the launcher will simply prompt you with a link to grant Usage Access in system settings, and screentime widgets will remain dormant until enabled.

---

### 3. `android.permission.BIND_ACCESSIBILITY_SERVICE`
* **Declaration**: Declared on `.service.AppBlockerAccessibilityService`
* **Category**: Special Accessibility Service
* **Why the app needs it**:
  Android restricts background apps from detecting when another app is opened. To enforce daily digital wellbeing screentime limits accurately at the moment an app is opened, an Accessibility Service (`TYPE_WINDOW_STATE_CHANGED`) is required.
* **How it is used**:
  - Listens only for window transition events to detect when a target application is launched.
  - Checks if the launched app has exceeded its configured daily screentime quota.
  - If the limit is exceeded, immediately opens `BlockScreenActivity` to help you stay mindful and avoid endless scrolling.
* **Privacy Assurance**: The service **does not inspect screen content, keystrokes, or text inputs**. It only inspects the package identifier during window transitions.

---

### 4. `android.permission.SYSTEM_ALERT_WINDOW`
* **Declaration**: `<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />`
* **Category**: Special App Access ("Display over other apps")
* **Why the app needs it**:
  Allows the digital wellbeing limit enforcement system to present the `BlockScreenActivity` on top of restricted applications when time limits expire.

---

### 5. `android.permission.USE_BIOMETRIC` & `android.permission.USE_FINGERPRINT`
* **Declaration**:
  ```xml
  <uses-permission android:name="android.permission.USE_BIOMETRIC" />
  <uses-permission android:name="android.permission.USE_FINGERPRINT" />
  ```
* **Category**: Hardware Biometric Security
* **Why the app needs it**:
  Secures your **Private Space / Hidden Apps** vault.
* **How it is used**:
  - Triggers the native Android `BiometricPrompt` dialog (supporting Fingerprint, Face Unlock, and secure Device PIN/Pattern fallback).
  - Integrates with the hardware-backed **Android KeyStore** (AES-256-GCM encryption) to ensure hidden apps are only visible when unlocked by the authenticated device owner.

---

### 6. `android.permission.EXPAND_STATUS_BAR`
* **Declaration**: `<uses-permission android:name="android.permission.EXPAND_STATUS_BAR" />`
* **Category**: Normal Gesture Shortcut
* **Why the app needs it**:
  Supports standard launcher gestures without requiring the user to reach to the very top edge of large modern smartphone screens.
* **How it is used**:
  - Allows you to swipe down anywhere on the home screen to expand the Android notification shade and Quick Settings tiles (`StatusBarManager`).

---

## How to Manage or Revoke Permissions

You can manage or revoke any special permissions at any time through Android System Settings:
1. **Usage Access**: `Settings > Apps & Notifications > Special app access > Usage access > Minimalist`
2. **Accessibility Service**: `Settings > Accessibility > Minimalist Blocker Service`
3. **Display Over Other Apps**: `Settings > Apps > Special app access > Display over other apps`
4. **Default Home App**: `Settings > Apps > Default apps > Home app > Minimalist`
