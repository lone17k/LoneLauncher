package com.haswanth.lonelauncher

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.haswanth.lonelauncher.data.*
import com.haswanth.lonelauncher.screens.HomeScreen
import com.haswanth.lonelauncher.screens.AppSearchScreen
import com.haswanth.lonelauncher.screens.LauncherSettingsScreen
import kotlinx.coroutines.launch

enum class LauncherScreen { HOME, SEARCH, SETTINGS }

@Composable
fun LauncherRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }
    var swipeLeftAction by remember { mutableStateOf(ACTION_CONTACTS) }
    var swipeRightAction by remember { mutableStateOf(ACTION_CAMERA) }

    var currentScreen by remember { mutableStateOf(LauncherScreen.HOME) }

    LaunchedEffect(Unit) {

        loadPreferences(
            context = context,
            onFavorites = { favs: Set<String> -> favorites = favs },
            onSwipeLeft = { action: String -> swipeLeftAction = action },
            onSwipeRight = { action: String -> swipeRightAction = action },
            onShowWorkingOnly = { _ -> }, // ignored
            onIncludeNonLauncher = { _ -> } // ignored
        )
    }

    when (currentScreen) {
        LauncherScreen.HOME -> HomeScreen(
            favorites = favorites.toList(),
            onSwipeUp = { currentScreen = LauncherScreen.SEARCH },
            onSwipeLeft = { performAction(swipeLeftAction, context) },
            onSwipeRight = { performAction(swipeRightAction, context) },
            onLongPress = { currentScreen = LauncherScreen.SETTINGS }
        )

        LauncherScreen.SEARCH -> AppSearchScreen(
            showWorkingOnly = true,
            includeNonLauncher = false,
            favorites = favorites,
            onFavoritesChange = { newFavs ->
                favorites = newFavs // ✅ update in-memory state
                saveFavorites(context, scope, newFavs)
            },
            onLaunchedSingleMatch = { currentScreen = LauncherScreen.HOME },
            onBack = { currentScreen = LauncherScreen.HOME }
        )

        LauncherScreen.SETTINGS -> LauncherSettingsScreen(
            currentFavorites = favorites,
            onFavoritesChange = { newFavs ->
                favorites = newFavs // ✅ also update in-memory state here
                saveFavorites(context, scope, newFavs)
            },
            swipeLeftAction = swipeLeftAction,
            swipeRightAction = swipeRightAction,
            onSwipeLeftChange = { saveSwipeLeft(context, scope, it) },
            onSwipeRightChange = { saveSwipeRight(context, scope, it) },
            onBack = { currentScreen = LauncherScreen.HOME }
        )
    }
}
