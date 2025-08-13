package com.haswanth.lonelauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*

fun performAction(action: String, context: Context) {
    when (action) {
        ACTION_CONTACTS -> {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CONTACTS)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
        ACTION_CAMERA -> {
            val intent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
        else -> {
            val launch = context.packageManager.getLaunchIntentForPackage(action)
            launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (launch != null) context.startActivity(launch)
        }
    }
}

fun getTime12h(): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

fun getDatePretty(): String =
    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
fun queryApps(
    pm: PackageManager,
    showWorkingOnly: Boolean,
    includeNonLauncher: Boolean,
    favorites: Set<String> = emptySet()
): List<AppEntry> {
    val list = mutableListOf<AppEntry>()

    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
    } else {
        pm.queryIntentActivities(mainIntent, 0)
    }

    for (ri in resolvedInfos) {
        val ai = ri.activityInfo.applicationInfo
        val isSystemApp = ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
        val isUpdatedSystemApp = ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
        if (isSystemApp && !isUpdatedSystemApp) continue

        val pkgName = ai.packageName
        val label = runCatching { ri.loadLabel(pm).toString() }.getOrElse { pkgName }
        val launchable = pm.getLaunchIntentForPackage(pkgName) != null

        if (!showWorkingOnly || launchable || favorites.contains(pkgName)) {
            list.add(AppEntry(label, pkgName, launchable))
        }
    }

    if (includeNonLauncher || favorites.isNotEmpty()) {
        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (ai in allApps) {
            val pkgName = ai.packageName
            if (list.any { it.packageName == pkgName }) continue
            val isSystemApp = ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val isUpdatedSystemApp = ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            if (isSystemApp && !isUpdatedSystemApp) continue
            val label = runCatching { pm.getApplicationLabel(ai).toString() }.getOrElse { pkgName }
            val launchable = pm.getLaunchIntentForPackage(pkgName) != null

            if (!showWorkingOnly || launchable || favorites.contains(pkgName)) {
                list.add(AppEntry(label, pkgName, launchable))
            }
        }
    }

    return list.sortedBy { it.label.lowercase(Locale.getDefault()) }
}
