package com.haswanth.lonelauncher.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haswanth.lonelauncher.data.queryApps
import com.haswanth.lonelauncher.components.FavoriteRow
import com.haswanth.lonelauncher.components.GesturePickerRow

@Composable
fun LauncherSettingsScreen(
    currentFavorites: Set<String>,
    onFavoritesChange: (Set<String>) -> Unit,
    swipeLeftAction: String,
    swipeRightAction: String,
    onSwipeLeftChange: (String) -> Unit,
    onSwipeRightChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    var favorites by remember { mutableStateOf(currentFavorites.toMutableSet()) }
    LaunchedEffect(currentFavorites) { favorites = currentFavorites.toMutableSet() }

    val apps = remember { queryApps(pm, true, false) }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(46.dp))
        Text("Control Panel", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Tune gestures and favorites", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(14.dp))
        Divider(color = Color.DarkGray)

        Spacer(Modifier.height(16.dp))
        Text("Gestures", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        GesturePickerRow("Swipe Left", swipeLeftAction, apps, onSwipeLeftChange)
        Spacer(Modifier.height(8.dp))
        GesturePickerRow("Swipe Right", swipeRightAction, apps, onSwipeRightChange)

        Spacer(Modifier.height(16.dp))
        Divider(color = Color.DarkGray)
        Spacer(Modifier.height(12.dp))
        Text("Favorites", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text("Apps to show on Home Screen.", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(apps) { app ->
                FavoriteRow(
                    label = app.label,
                    pkg = app.packageName,
                    checked = favorites.contains(app.packageName),
                    enabled = app.launchable,
                    onToggle = {
                        val newSet = favorites.toMutableSet()
                        if (newSet.contains(app.packageName)) newSet.remove(app.packageName)
                        else newSet.add(app.packageName)
                        favorites = newSet
                        onFavoritesChange(newSet)
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Divider(color = Color.DarkGray)
        Spacer(Modifier.height(12.dp))
        Text("Appearance", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                context.startActivity(Intent.createChooser(intent, "Select Wallpaper"))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, // White background
                contentColor = Color.Black    // Black text/icons
            ),
            shape = RoundedCornerShape(8.dp), // Rounded-square look

        ) {
            Text(
                text = "Set Wallpaper",
                fontWeight = FontWeight.Bold
            )
        }



        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // White background
                    contentColor = Color.Black    // Black text/icons
                ),
                shape = RoundedCornerShape(8.dp), // Rounded-square look


            ) { Text("Back",  fontWeight = FontWeight.Bold) }
        }
    }
}
