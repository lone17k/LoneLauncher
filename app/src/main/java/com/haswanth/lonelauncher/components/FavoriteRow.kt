package com.haswanth.lonelauncher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FavoriteRow(
    label: String,
    pkg: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    val shape = RoundedCornerShape(8.dp)

    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color.Black, shape)
            .clickable(enabled = enabled) { onToggle() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = Color.Black,
                checkmarkColor = Color.White,
                uncheckedColor = Color.Black
            )
        )
        Spacer(Modifier.width(18.dp))
        Text(
            text = label,
            color = Color.Black,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
