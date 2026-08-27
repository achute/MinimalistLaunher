package com.example
import android.os.UserManager
import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle

fun test(context: Context, user: UserHandle) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val info = launcherApps.getLauncherUserInfo(user)
    val type = info?.userType
}
