package com.haswanth.lonelauncher.data

/**
 * Represents an installed application entry.
 *
 * @param label Display name of the app
 * @param packageName Unique package name (used to launch it)
 * @param launchable True if it has a launcher activity
 */
data class AppEntry(
    val label: String,
    val packageName: String,
    val launchable: Boolean
)
