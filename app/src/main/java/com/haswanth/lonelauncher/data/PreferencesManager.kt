package com.haswanth.lonelauncher.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "lone_launcher_prefs")

val KEY_FAVORITES = stringSetPreferencesKey("favorites_pkgs")
val KEY_SWIPE_LEFT = stringPreferencesKey("swipe_left_action")
val KEY_SWIPE_RIGHT = stringPreferencesKey("swipe_right_action")
val KEY_SHOW_WORKING_ONLY = booleanPreferencesKey("show_working_only")
val KEY_INCLUDE_NON_LAUNCHER = booleanPreferencesKey("include_non_launcher")

const val ACTION_CONTACTS = "contacts"
const val ACTION_CAMERA = "camera"

suspend fun loadPreferences(
    context: Context,
    onFavorites: (Set<String>) -> Unit,
    onSwipeLeft: (String) -> Unit,
    onSwipeRight: (String) -> Unit,
    onShowWorkingOnly: (Boolean) -> Unit,
    onIncludeNonLauncher: (Boolean) -> Unit
) {
    val prefs = context.dataStore.data.first()
    onFavorites(prefs[KEY_FAVORITES] ?: emptySet())
    onSwipeLeft(prefs[KEY_SWIPE_LEFT] ?: ACTION_CONTACTS)
    onSwipeRight(prefs[KEY_SWIPE_RIGHT] ?: ACTION_CAMERA)
    onShowWorkingOnly(prefs[KEY_SHOW_WORKING_ONLY] ?: true)
    onIncludeNonLauncher(prefs[KEY_INCLUDE_NON_LAUNCHER] ?: false)
}


fun saveFavorites(context: Context, scope: CoroutineScope, favorites: Set<String>) {
    scope.launch { context.dataStore.edit { it[KEY_FAVORITES] = favorites } }
}

fun saveSwipeLeft(context: Context, scope: CoroutineScope, action: String) {
    scope.launch { context.dataStore.edit { it[KEY_SWIPE_LEFT] = action } }
}

fun saveSwipeRight(context: Context, scope: CoroutineScope, action: String) {
    scope.launch { context.dataStore.edit { it[KEY_SWIPE_RIGHT] = action } }
}

fun saveShowWorkingOnly(context: Context, scope: CoroutineScope, value: Boolean) {
    scope.launch { context.dataStore.edit { it[KEY_SHOW_WORKING_ONLY] = value } }
}

fun saveIncludeNonLauncher(context: Context, scope: CoroutineScope, value: Boolean) {
    scope.launch { context.dataStore.edit { it[KEY_INCLUDE_NON_LAUNCHER] = value } }
}
