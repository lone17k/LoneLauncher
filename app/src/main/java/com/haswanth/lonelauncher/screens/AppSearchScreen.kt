package com.haswanth.lonelauncher.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haswanth.lonelauncher.data.AppEntry
import com.haswanth.lonelauncher.data.queryApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppSearchScreen(
    showWorkingOnly: Boolean,
    includeNonLauncher: Boolean,
    favorites: Set<String>,
    onFavoritesChange: (Set<String>) -> Unit,
    onLaunchedSingleMatch: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    var searchQuery by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var filtered by remember { mutableStateOf<List<AppEntry>>(emptyList()) }

    var menuExpanded by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppEntry?>(null) }

    // Load apps once in background, always including favorites
    LaunchedEffect(showWorkingOnly, includeNonLauncher, favorites) {
        val loadedApps = withContext(Dispatchers.IO) {
            queryApps(pm, showWorkingOnly, includeNonLauncher, favorites)
        }
        apps = loadedApps
        filtered = loadedApps
    }

    // Filter list in background when query changes
    LaunchedEffect(searchQuery, apps) {
        val q = searchQuery.trim()
        filtered = if (q.isEmpty()) apps
        else apps.filter { it.label.contains(q, ignoreCase = true) }
    }

    // Auto-launch when exactly one match
    LaunchedEffect(filtered, searchQuery) {
        if (searchQuery.isNotBlank() && filtered.size == 1) {
            val only = filtered.first()
            if (only.launchable) {
                pm.getLaunchIntentForPackage(only.packageName)?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                    onLaunchedSingleMatch()
                }
            }
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1C1C), shape = MaterialTheme.shapes.small)
                .padding(12.dp)
        )

        Spacer(Modifier.height(12.dp))
        Divider(color = Color.DarkGray)

        LazyColumn {
            items(filtered) { app ->
                val alpha = if (app.launchable) 1f else 0.5f
                Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .alpha(alpha)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .combinedClickable(
                            onClick = {
                                if (app.launchable) {
                                    pm.getLaunchIntentForPackage(app.packageName)?.let {
                                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(it)
                                        onLaunchedSingleMatch()
                                    }
                                }
                            },
                            onLongClick = {
                                selectedApp = app
                                menuExpanded = true
                            }
                        )
                )
            }
        }
    }

    // Context menu
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        val app = selectedApp
        if (app != null) {
            val isFavorite = favorites.contains(app.packageName)

            // Toggle Favorite
            DropdownMenuItem(
                text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites") },
                onClick = {
                    val newFavs = favorites.toMutableSet()
                    if (isFavorite) newFavs.remove(app.packageName)
                    else newFavs.add(app.packageName)
                    onFavoritesChange(newFavs)
                    menuExpanded = false
                }
            )

            // Uninstall
            DropdownMenuItem(
                text = { Text("Uninstall") },
                onClick = {
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    menuExpanded = false
                }
            )

            // App Info
            DropdownMenuItem(
                text = { Text("App Info") },
                onClick = {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    menuExpanded = false
                }
            )
        }
    }
}
