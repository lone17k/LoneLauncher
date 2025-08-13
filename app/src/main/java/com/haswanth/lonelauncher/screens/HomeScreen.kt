package com.haswanth.lonelauncher.screens

import HomeFont
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haswanth.lonelauncher.data.getDatePretty
import com.haswanth.lonelauncher.data.getTime12h
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    favorites: List<String>,
    onSwipeUp: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onLongPress: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    // State
    var time by remember { mutableStateOf(getTime12h()) }
    var date by remember { mutableStateOf(getDatePretty()) }
    var battery by remember { mutableStateOf(getBatteryLevel(context)) }
    var isCharging by remember { mutableStateOf(isDeviceCharging(context)) }
    var quote by remember { mutableStateOf("Loading quote...") }
    var weather by remember { mutableStateOf("Loading weather...") }

    // Battery animation
    val animatedBattery by animateFloatAsState(targetValue = battery / 100f)
    val infiniteTransition = rememberInfiniteTransition(label = "charging_anim")
    val chargingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "charging_alpha"
    )

    // Clock & battery updater
    LaunchedEffect(Unit) {
        while (true) {
            time = getTime12h()
            date = getDatePretty()
            battery = getBatteryLevel(context)
            isCharging = isDeviceCharging(context)
            delay(1000)
        }
    }

    // Quote fetch
    LaunchedEffect(Unit) {
        quote = fetchDailyQuote()
    }

    // Location permission launcher
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val loc = getLastKnownLocation(context)
                if (loc != null) {
                    weather = fetchWeather(loc.latitude, loc.longitude)
                } else {
                    weather = "☀ --°C"
                }
            }
        } else {
            weather = "☀ --°C"
        }
    }

    // Request location once
    LaunchedEffect(Unit) {
        locationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40) onSwipeUp()
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40) onSwipeLeft()
                    if (dragAmount > 40) onSwipeRight()
                }
            }
            .combinedClickable(
                onClick = { /* no-op */ },
                onLongClick = onLongPress
            )
    ) {
        // Time, Date, Weather, Battery bar
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val parts = time.split(" ")
            val mainTime = parts.getOrElse(0) { "" }
            val amPm = parts.getOrElse(1) { "" }

            Text(
                buildAnnotatedString {
                    append(mainTime)
                    addStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        start = 0,
                        end = mainTime.length
                    )
                    append(" ")
                    append(amPm)
                    addStyle(
                        style = SpanStyle(
                            color = Color.Gray,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        start = mainTime.length + 1,
                        end = mainTime.length + 1 + amPm.length
                    )
                }
            )
            Text(date, color = Color.Gray, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            Text(weather, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(8.dp))

            // Battery bar
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                // Battery bar
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedBattery)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isCharging) chargingAlpha else 0.8f),
                                        Color.White.copy(alpha = 0.4f),
                                        Color.White.copy(alpha = if (isCharging) chargingAlpha else 0.8f)
                                    )
                                )
                            )
                    )

                    // Dynamically choose text color
                    val percentageTextColor = if (battery > 50) Color.Black else Color.White

                    Text(
                        "$battery%",
                        color = percentageTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                    )
                }

            }
        }

        // Favorites list
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            favorites.forEach { pkg ->
                val label = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                }.getOrElse { pkg }
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontFamily = HomeFont,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            val launch = pm.getLaunchIntentForPackage(pkg)
                            launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (launch != null) context.startActivity(launch)
                        }
                )
            }
        }

        // Daily Quote
        Text(
            text = quote,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

// --- Helpers ---
fun getBatteryLevel(context: Context): Int {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}

fun isDeviceCharging(context: Context): Boolean {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return bm.isCharging
}

suspend fun fetchDailyQuote(): String = withContext(Dispatchers.IO) {
    val fallbackQuotes = listOf(
        "Less is more.",
        "Simplicity is the ultimate sophistication.",
        "Stay minimal, stay focused.",
        "Do more with less.",
        "Clean design, clear mind.",
        "Perfection is achieved when there is nothing left to take away.",
        "Clarity is the counterbalance of profound thoughts.",
        "Small steps every day.",
        "Focus on the essentials.",
        "Quiet is the new loud.",
        "Make it simple, but significant.",
        "The details are not the details; they make the design.",
        "Simplicity is the keynote of all true elegance.",
        "Be yourself; everyone else is already taken.",
        "Your space reflects your mind.",
        "Choose less, achieve more.",
        "Minimalism is not lack of something. It’s simply the perfect amount.",
        "Order and simplicity are the keys to peace.",
        "Function over form, but beauty matters.",
        "Elegance is refusal.",
        "Simplicity is the essence of happiness.",
        "Designed by Lone, crafted by you.",
        "Designed by Haswanth, crafted by you.",
        "Less clutter, more clarity.",
        "Simplicity is the soul of modern design.",
        "Haswanth Raj — the mind behind Lone Launcher.",
        "Lone Launcher: Where simplicity meets elegance.",
        "Lone Launcher: Designed for the minimalists.",
        "Lone Launcher: Crafted for clarity.",
    )

    try {
        val json = URL("https://api.quotable.io/random").readText()
        val obj = JSONObject(json)
        "\"${obj.getString("content")}\" — ${obj.getString("author")}"
    } catch (e: Exception) {
        fallbackQuotes.random()
    }
}

@SuppressLint("MissingPermission")
fun getLastKnownLocation(context: Context): Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
}

suspend fun fetchWeather(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
    try {
        val json = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
        ).readText()
        val obj = JSONObject(json)
        val weatherObj = obj.getJSONObject("current_weather")
        val temp = weatherObj.getDouble("temperature")
        val code = weatherObj.getInt("weathercode")
        val icon = when (code) {
            0 -> "☀"
            in 1..3 -> "⛅"
            in 45..48 -> "🌫"
            in 51..67 -> "🌦"
            in 71..77 -> "❄"
            in 80..82 -> "🌧"
            else -> "☁"
        }
        "$icon ${temp.toInt()}°C"
    } catch (e: Exception) {
        "☀ --°C"
    }
}
