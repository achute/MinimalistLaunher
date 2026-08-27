package com.example.model

data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val category: String,
    val version: String,
    val licenseType: String,
    val description: String,
    val projectUrl: String,
    val licenseNotice: String
)

object OpenSourceLibrariesData {
    val libraries = listOf(
        OpenSourceLibrary(
            name = "Jetpack Compose & Material 3",
            author = "Google LLC / The Android Open Source Project",
            category = "UI Framework & Design System",
            version = "2024.09.00 / 1.3.0",
            licenseType = "Apache License 2.0",
            description = "Modern toolkit for building native Android UI with declarative Kotlin components and Material Design 3 tokens.",
            projectUrl = "https://developer.android.com/jetpack/compose",
            licenseNotice = """
                Copyright (C) 2024 The Android Open Source Project
                Licensed under the Apache License, Version 2.0.
                You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
            """.trimIndent()
        ),
        OpenSourceLibrary(
            name = "AndroidX Biometric",
            author = "Google LLC / AndroidX",
            category = "Security & Authentication",
            version = "1.2.0-alpha05",
            licenseType = "Apache License 2.0",
            description = "Provides native biometric prompt dialogs (Fingerprint, Face, Iris, Device PIN/Pattern) with hardware security fallback.",
            projectUrl = "https://developer.android.com/jetpack/androidx/releases/biometric",
            licenseNotice = """
                Copyright (C) 2024 The Android Open Source Project
                Licensed under the Apache License, Version 2.0.
                You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
            """.trimIndent()
        ),
        OpenSourceLibrary(
            name = "AndroidX Room Persistence",
            author = "Google LLC / AndroidX",
            category = "Database & Local Storage",
            version = "2.7.0",
            licenseType = "Apache License 2.0",
            description = "Robust abstraction layer over SQLite for on-device persistence of focus profiles, tasks, custom labels, and encrypted private space vaults.",
            projectUrl = "https://developer.android.com/jetpack/androidx/releases/room",
            licenseNotice = """
                Copyright (C) 2024 The Android Open Source Project
                Licensed under the Apache License, Version 2.0.
                You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
            """.trimIndent()
        ),
        OpenSourceLibrary(
            name = "AndroidX DataStore Preferences",
            author = "Google LLC / AndroidX",
            category = "Settings & State Persistence",
            version = "1.1.7",
            licenseType = "Apache License 2.0",
            description = "Asynchronous key-value storage solution built with Kotlin Coroutines and Flow to replace SharedPreferences.",
            projectUrl = "https://developer.android.com/topic/libraries/architecture/datastore",
            licenseNotice = """
                Copyright (C) 2024 The Android Open Source Project
                Licensed under the Apache License, Version 2.0.
                You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
            """.trimIndent()
        ),
        OpenSourceLibrary(
            name = "KotlinX Coroutines",
            author = "JetBrains s.r.o.",
            category = "Asynchronous Concurrency",
            version = "1.10.2",
            licenseType = "Apache License 2.0",
            description = "Library support for Kotlin coroutines with Flow, reactive streams, and structured concurrency on Android.",
            projectUrl = "https://github.com/Kotlin/kotlinx.coroutines",
            licenseNotice = """
                Copyright 2000-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
                Licensed under the Apache License, Version 2.0.
            """.trimIndent()
        ),
        OpenSourceLibrary(
            name = "AndroidX Core KTX & Activity",
            author = "Google LLC / AndroidX",
            category = "Core Android Extensions",
            version = "1.18.0 / 1.10.1",
            licenseType = "Apache License 2.0",
            description = "Kotlin extensions for core Android platform APIs, edge-to-edge window insets, and Activity Compose contracts.",
            projectUrl = "https://developer.android.com/jetpack/androidx",
            licenseNotice = """
                Copyright (C) 2024 The Android Open Source Project
                Licensed under the Apache License, Version 2.0.
            """.trimIndent()
        ),
        OpenSourceLibrary(
            name = "Retrofit & Moshi",
            author = "Square, Inc.",
            category = "Type-Safe Serialization & Architecture",
            version = "2.12.0 / 1.15.2",
            licenseType = "Apache License 2.0",
            description = "Type-safe HTTP client and modern JSON library for Kotlin and Android architectures.",
            projectUrl = "https://github.com/square/retrofit",
            licenseNotice = """
                Copyright 2024 Square, Inc.
                Licensed under the Apache License, Version 2.0 (the "License").
            """.trimIndent()
        ),
        OpenSourceLibrary(
            name = "Roborazzi & Robolectric",
            author = "Takahirom / Robolectric Community",
            category = "JVM UI & Screenshot Testing",
            version = "1.59.0 / 4.16.1",
            licenseType = "Apache License 2.0 / MIT",
            description = "Fast JVM-based automated screenshot and UI regression verification toolchain for Android Compose.",
            projectUrl = "https://github.com/takahirom/roborazzi",
            licenseNotice = """
                Copyright 2024 takahirom & Robolectric
                Licensed under the Apache License, Version 2.0 and the MIT License.
            """.trimIndent()
        )
    )
}
