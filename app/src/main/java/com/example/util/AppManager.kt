package com.example.util

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.model.AppInfoItem

object AppManager {

    fun getInstalledApps(context: Context): List<AppInfoItem> {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
        val userManager = context.getSystemService(Context.USER_SERVICE) as android.os.UserManager
        
        val appsList = mutableListOf<AppInfoItem>()
        val myPackage = context.packageName

        try {
            val profiles = launcherApps.profiles
            for (profile in profiles) {
                var isPrivate = false
                if (profile != android.os.Process.myUserHandle()) {
                    try {
                        val userInfo = launcherApps.getLauncherUserInfo(profile)
                        if (userInfo != null && userInfo.userType == "android.os.usertype.profile.PRIVATE") {
                            isPrivate = true
                        }
                    } catch (e: Exception) {} catch (e: Error) {}
                }

                val activities = launcherApps.getActivityList(null, profile)
                for (activityInfo in activities) {
                    val packageName = activityInfo.componentName.packageName
                    if (packageName == myPackage) continue
                    
                    appsList.add(
                        AppInfoItem(
                            packageName = packageName,
                            label = activityInfo.label.toString(),
                            userHandle = profile,
                            isPrivateProfile = isPrivate
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return appsList
            .distinctBy { it.packageName + "_" + (it.userHandle?.hashCode() ?: 0) }
            .sortedBy { it.label.lowercase() }
    }

    fun getPrivateProfileHandle(context: Context): android.os.UserHandle? {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
        try {
            for (profile in launcherApps.profiles) {
                if (profile != android.os.Process.myUserHandle()) {
                    try {
                        val userInfo = launcherApps.getLauncherUserInfo(profile)
                        if (userInfo != null && userInfo.userType == "android.os.usertype.profile.PRIVATE") {
                            return profile
                        }
                    } catch (e: Exception) {} catch (e: Error) {}
                }
            }
        } catch (e: Exception) {}
        return null
    }

    fun requestQuietModeEnabled(context: Context, enableQuietMode: Boolean, userHandle: android.os.UserHandle): Boolean {
        return try {
            val userManager = context.getSystemService(Context.USER_SERVICE) as android.os.UserManager
            userManager.requestQuietModeEnabled(enableQuietMode, userHandle)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isQuietModeEnabled(context: Context, userHandle: android.os.UserHandle): Boolean {
        return try {
            val userManager = context.getSystemService(Context.USER_SERVICE) as android.os.UserManager
            userManager.isQuietModeEnabled(userHandle)
        } catch (e: Exception) {
            false
        }
    }

    fun launchApp(context: Context, packageName: String, userHandle: android.os.UserHandle? = null): Boolean {
        return try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
            val user = userHandle ?: android.os.Process.myUserHandle()
            
            val activities = launcherApps.getActivityList(packageName, user)
            if (activities.isNotEmpty()) {
                launcherApps.startMainActivity(activities[0].componentName, user, null, null)
                true
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    Toast.makeText(context, "Cannot launch app ($packageName)", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Launch failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun launchBrowser(context: Context, url: String = "https://www.google.com", preferredPackage: String? = null): Boolean {
        val pm = context.packageManager

        // 1. If user set a specific preferred package
        if (!preferredPackage.isNullOrBlank()) {
            val launchIntent = pm.getLaunchIntentForPackage(preferredPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        // 2. Try default ACTION_VIEW for https://
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            return true
        } catch (e: Exception) {
            // Fallback to searching known browsers
        }

        // 3. Query browsable intent
        try {
            val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            val resolveInfo = pm.resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                val pkg = resolveInfo.activityInfo.packageName
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        } catch (e: Exception) {
            // Fallback
        }

        // 4. Try known browser package names
        val browserPackages = listOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.brave.browser",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.sec.android.app.sbrowser",
            "com.duckduckgo.mobile.android",
            "com.google.android.apps.searchlite",
            "com.google.android.googlequicksearchbox"
        )
        for (pkg in browserPackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        // 5. Try ACTION_WEB_SEARCH
        return try {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchIntent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "No web browser app found", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun launchDefaultAction(context: Context, type: String): Boolean {
        if (type.equals("browser", ignoreCase = true)) {
            return launchBrowser(context)
        }
        return try {
            val intent = when (type.lowercase()) {
                "phone", "dialer" -> Intent(Intent.ACTION_DIAL)
                "messages", "sms" -> Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
                "camera" -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                else -> null
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Action not supported on this device", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openAppSettings(context: Context, packageName: String, userHandle: android.os.UserHandle? = null) {
        try {
            val user = userHandle ?: android.os.Process.myUserHandle()
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps
            val activities = launcherApps.getActivityList(packageName, user)
            if (activities.isNotEmpty()) {
                launcherApps.startAppDetailsActivity(activities[0].componentName, user, null, null)
                return
            }
        } catch (e: Exception) {
            // fallback
        }
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open app settings", Toast.LENGTH_SHORT).show()
        }
    }

    fun uninstallApp(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.fromParts("package", packageName, null)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot initiate uninstall", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchSearch(context: Context, query: String = "", preferredPackage: String? = null) {
        try {
            if (!preferredPackage.isNullOrBlank()) {
                val intent = context.packageManager.getLaunchIntentForPackage(preferredPackage)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return
                }
            }

            if (query.isNotBlank()) {
                val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra("query", query)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                // Assist search intent
                val assistIntent = Intent(Intent.ACTION_ASSIST).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(assistIntent)
            }
        } catch (e: Exception) {
            try {
                val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(viewIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Search unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun expandNotificationShade(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val expandMethod = statusBarManagerClass.getMethod("expandNotificationsPanel")
            expandMethod.invoke(statusBarService)
        } catch (e: Exception) {
            // Fallback
            try {
                val statusBarService = context.getSystemService("statusbar")
                val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
                val expandMethod = statusBarManagerClass.getMethod("expand")
                expandMethod.invoke(statusBarService)
            } catch (ex: Exception) {
                // Not supported or restricted by OEM
            }
        }
    }

    fun openDefaultLauncherSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Open Settings > Apps > Default Apps > Home App", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Open Settings > Apps & Notifications > Special access > Usage Access", Toast.LENGTH_LONG).show()
        }
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Open Settings > Accessibility", Toast.LENGTH_LONG).show()
        }
    }

    fun openWallpaperPicker(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Select Wallpaper").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            try {
                val settingsIntent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Cannot open wallpaper picker", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
