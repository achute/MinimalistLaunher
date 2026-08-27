package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.widget.LauncherAppWidgetHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MinimalistApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val appWidgetHost by lazy { LauncherAppWidgetHost(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
