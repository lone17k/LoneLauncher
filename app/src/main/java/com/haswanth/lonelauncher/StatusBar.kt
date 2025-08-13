package com.haswanth.lonelauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.haswanth.lonelauncher.data.getTime12h
import kotlinx.coroutines.delay

@Composable
fun StatusBar(
    modifier: Modifier = Modifier,
    batteryPercent: Int = 100,
    wifiStrength: Int = 3
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1C1C))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val time = remember { mutableStateOf(getTime12h()) }
        LaunchedEffect(Unit) {
            while (true) {
                time.value = getTime12h()
                delay(1000)
            }
        }
        Text(time.value, color = Color.White)

        Row {
            Text("📶$wifiStrength", color = Color.White)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(12.dp)
                    .border(1.dp, Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = batteryPercent / 100f)
                        .background(Color.Green)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text("$batteryPercent%", color = Color.White)
        }
    }
}
