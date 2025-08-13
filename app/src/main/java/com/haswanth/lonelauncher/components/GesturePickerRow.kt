package com.haswanth.lonelauncher.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haswanth.lonelauncher.data.ACTION_CAMERA
import com.haswanth.lonelauncher.data.ACTION_CONTACTS
import com.haswanth.lonelauncher.data.AppEntry

@Composable
fun GesturePickerRow(
    title: String,
    current: String,
    apps: List<AppEntry>,
    onChange: (String) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    var open by remember { mutableStateOf(false) }

    val currentLabel = when (current) {
        ACTION_CONTACTS -> "Contacts"
        ACTION_CAMERA -> "Camera"
        else -> runCatching {
            pm.getApplicationLabel(pm.getApplicationInfo(current, 0)).toString()
        }.getOrElse { current }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { open = !open }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$title:", color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(currentLabel, color = Color.Gray, fontSize = 16.sp)
        }

        AnimatedVisibility(visible = open) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onChange(ACTION_CONTACTS); open = false }
                        .padding(8.dp)
                ) { Text("• Contacts", color = Color.White) }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onChange(ACTION_CAMERA); open = false }
                        .padding(8.dp)
                ) { Text("• Camera", color = Color.White) }

                Divider(color = Color.DarkGray)
                Text("Apps", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    items(apps.filter { it.launchable }) { app ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onChange(app.packageName); open = false }
                                .padding(8.dp)
                        ) { Text("• ${app.label}", color = Color.White) }
                    }
                }
            }
        }
    }
}
