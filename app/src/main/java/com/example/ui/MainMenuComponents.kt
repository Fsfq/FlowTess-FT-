package com.example.ui

import androidx.activity.compose.BackHandler

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Leaderboard
import java.util.Locale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.db.FriendUser
import com.example.db.PublicUserProfile
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.MainViewModel
import com.example.db.HighScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.TileMode


@Composable
fun MenuButton(
    text: String,
    iconType: String,
    themeColor: Color,
    onClick: () -> Unit
) {
    val debouncer = rememberClickDebouncer(1000L)
    Card(
        onClick = { debouncer.process { onClick() } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        ListItem(
            modifier = Modifier.padding(vertical = 4.dp),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            headlineContent = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingContent = {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = themeColor.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MenuIconCanvas(
                            iconType = iconType,
                            tint = themeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            trailingContent = {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(
                            modifier = Modifier.size(14.dp)
                        ) {
                            val cw = size.width
                            val ch = size.height
                            val strokePx = 2.dp.toPx()
                            drawLine(
                                color = themeColor,
                                start = Offset(cw * 0.2f, ch * 0.15f),
                                end = Offset(cw * 0.8f, ch * 0.5f),
                                strokeWidth = strokePx
                            )
                            drawLine(
                                color = themeColor,
                                start = Offset(cw * 0.8f, ch * 0.5f),
                                end = Offset(cw * 0.2f, ch * 0.85f),
                                strokeWidth = strokePx
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun MenuIconCanvas(
    iconType: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidthPx = 2.dp.toPx()
        
        when (iconType) {
            "play" -> {
                val path = Path().apply {
                    moveTo(w * 0.25f, h * 0.2f)
                    lineTo(w * 0.85f, h * 0.5f)
                    lineTo(w * 0.25f, h * 0.8f)
                    close()
                }
                drawPath(path = path, color = tint)
            }
            "leaderboard" -> {
                drawRect(
                    color = tint.copy(alpha = 0.6f),
                    topLeft = Offset(w * 0.15f, h * 0.45f),
                    size = Size(w * 0.2f, h * 0.45f)
                )
                drawRect(
                    color = tint,
                    topLeft = Offset(w * 0.4f, h * 0.25f),
                    size = Size(w * 0.2f, h * 0.65f)
                )
                drawRect(
                    color = tint.copy(alpha = 0.8f),
                    topLeft = Offset(w * 0.65f, h * 0.55f),
                    size = Size(w * 0.2f, h * 0.35f)
                )
            }
            "trophy" -> {
                val cupPath = Path().apply {
                    moveTo(w * 0.2f, h * 0.2f)
                    lineTo(w * 0.8f, h * 0.2f)
                    cubicTo(w * 0.8f, h * 0.55f, w * 0.2f, h * 0.55f, w * 0.2f, h * 0.2f)
                    close()
                }
                drawPath(path = cupPath, color = tint)
                
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.5f),
                    end = Offset(w * 0.5f, h * 0.75f),
                    strokeWidth = strokeWidthPx
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.3f, h * 0.75f),
                    end = Offset(w * 0.7f, h * 0.75f),
                    strokeWidth = strokeWidthPx * 1.5f
                )
            }
            "settings" -> {
                drawCircle(
                    color = tint,
                    radius = w * 0.15f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = strokeWidthPx)
                )
                drawCircle(
                    color = tint,
                    radius = w * 0.3f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = strokeWidthPx)
                )
                val teethCount = 8
                for (i in 0 until teethCount) {
                    val angle = (2 * Math.PI * i) / teethCount
                    val cos = Math.cos(angle).toFloat()
                    val sin = Math.sin(angle).toFloat()
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.5f + w * 0.3f * cos, h * 0.5f + w * 0.3f * sin),
                        end = Offset(w * 0.5f + w * 0.4f * cos, h * 0.5f + w * 0.4f * sin),
                        strokeWidth = strokeWidthPx * 1.2f
                    )
                }
            }
            "upgrades" -> {
                val p1 = Path().apply {
                    moveTo(w * 0.25f, h * 0.45f)
                    lineTo(w * 0.5f, h * 0.2f)
                    lineTo(w * 0.75f, h * 0.45f)
                }
                drawPath(
                    path = p1,
                    color = tint,
                    style = Stroke(width = strokeWidthPx)
                )
                val p2 = Path().apply {
                    moveTo(w * 0.25f, h * 0.75f)
                    lineTo(w * 0.5f, h * 0.5f)
                    lineTo(w * 0.75f, h * 0.75f)
                }
                drawPath(
                    path = p2,
                    color = tint.copy(alpha = 0.6f),
                    style = Stroke(width = strokeWidthPx)
                )
            }
            "admin" -> {
                val shieldPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.8f, h * 0.25f)
                    lineTo(w * 0.8f, h * 0.6f)
                    cubicTo(w * 0.8f, h * 0.85f, w * 0.5f, h * 0.95f, w * 0.5f, h * 0.95f)
                    cubicTo(w * 0.5f, h * 0.95f, w * 0.2f, h * 0.85f, w * 0.2f, h * 0.6f)
                    lineTo(w * 0.2f, h * 0.25f)
                    close()
                }
                drawPath(path = shieldPath, color = tint, style = Stroke(width = strokeWidthPx))
                
                drawCircle(color = tint, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.4f))
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.48f),
                    end = Offset(w * 0.5f, h * 0.75f),
                    strokeWidth = strokeWidthPx
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.62f),
                    end = Offset(w * 0.62f, h * 0.62f),
                    strokeWidth = strokeWidthPx
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.7f),
                    end = Offset(w * 0.62f, h * 0.7f),
                    strokeWidth = strokeWidthPx
                )
            }
            "friends" -> {
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.35f, h * 0.35f))
                drawCircle(color = tint, radius = w * 0.13f, center = Offset(w * 0.68f, h * 0.38f))
                val p1 = Path().apply {
                    moveTo(w * 0.15f, h * 0.8f)
                    cubicTo(w * 0.15f, h * 0.58f, w * 0.55f, h * 0.58f, w * 0.55f, h * 0.8f)
                    close()
                }
                drawPath(path = p1, color = tint)
                val p2 = Path().apply {
                    moveTo(w * 0.52f, h * 0.8f)
                    cubicTo(w * 0.52f, h * 0.62f, w * 0.85f, h * 0.62f, w * 0.85f, h * 0.8f)
                    close()
                }
                drawPath(path = p2, color = tint.copy(alpha = 0.7f))
            }
            "multiplayer" -> {
                // Gamepad outline
                val padPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.32f)
                    lineTo(w * 0.78f, h * 0.32f)
                    cubicTo(w * 0.98f, h * 0.32f, w * 0.98f, h * 0.78f, w * 0.76f, h * 0.78f)
                    cubicTo(w * 0.65f, h * 0.78f, w * 0.58f, h * 0.58f, w * 0.5f, h * 0.58f)
                    cubicTo(w * 0.42f, h * 0.58f, w * 0.35f, h * 0.78f, w * 0.24f, h * 0.78f)
                    cubicTo(w * 0.02f, h * 0.78f, w * 0.02f, h * 0.32f, w * 0.22f, h * 0.32f)
                    close()
                }
                drawPath(path = padPath, color = tint, style = Stroke(width = strokeWidthPx))
                
                // D-Pad Cross on Left
                drawLine(
                    color = tint,
                    start = Offset(w * 0.20f, h * 0.50f),
                    end = Offset(w * 0.36f, h * 0.50f),
                    strokeWidth = strokeWidthPx * 1.3f
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.28f, h * 0.42f),
                    end = Offset(w * 0.28f, h * 0.58f),
                    strokeWidth = strokeWidthPx * 1.3f
                )
                
                // 4 Action Buttons on Right
                drawCircle(color = tint, radius = w * 0.035f, center = Offset(w * 0.68f, h * 0.50f))
                drawCircle(color = tint, radius = w * 0.035f, center = Offset(w * 0.80f, h * 0.50f))
                drawCircle(color = tint, radius = w * 0.035f, center = Offset(w * 0.74f, h * 0.44f))
                drawCircle(color = tint, radius = w * 0.035f, center = Offset(w * 0.74f, h * 0.56f))
            }
            else -> {
                drawCircle(color = tint, radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = strokeWidthPx))
                drawCircle(color = tint, radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.35f))
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.48f),
                    end = Offset(w * 0.5f, h * 0.7f),
                    strokeWidth = strokeWidthPx * 1.5f
                )
            }
        }
    }
}


