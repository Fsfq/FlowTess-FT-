package com.example.ui

import com.example.MasterySystem
import com.example.LevelReward
import androidx.compose.material.icons.filled.AutoAwesome

import androidx.compose.animation.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Check
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Bolt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import android.content.Context
import androidx.compose.ui.platform.LocalContext



fun getAvatarFrameBrush(equippedAvatarFrame: String, themeColor: Color, secondaryColor: Color): Brush {
    return when (equippedAvatarFrame.lowercase()) {
        "chrono_gl", "gradient_frame" -> Brush.sweepGradient(
            listOf(
                Color(0xFFFF0055),
                Color(0xFFFF7700),
                Color(0xFFFFDD00),
                Color(0xFF00DD77),
                Color(0xFF0099FF),
                Color(0xFF8800FF),
                Color(0xFFFF0055)
            )
        )
        "frame_white" -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE0E0E0)))
        "frame_blue" -> Brush.linearGradient(listOf(Color(0xFF2196F3), Color(0xFF1976D2)))
        "frame_green" -> Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF388E3C)))
        "frame_yellow" -> Brush.linearGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFDD835)))
        "frame_orange" -> Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFF57C00)))
        "frame_red" -> Brush.linearGradient(listOf(Color(0xFFF44336), Color(0xFFD32F2F)))
        "frame_purple" -> Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2)))
        "frame_dark" -> Brush.linearGradient(listOf(Color(0xFF455A64), Color(0xFF263238)))
        "standard" -> Brush.sweepGradient(listOf(themeColor, secondaryColor, themeColor))
        // Legacy fallback
        "neon_frame" -> Brush.linearGradient(listOf(Color(0xFF00FFCC), Color(0xFF00B0FF)))
        "gold_frame" -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))
        "cyber_frame" -> Brush.linearGradient(listOf(Color(0xFFE040FB), Color(0xFF7C4DFF)))
        "fire_frame" -> Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFFF9100)))
        "ice_frame" -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF)))
        "matrix_frame" -> Brush.linearGradient(listOf(Color(0xFF00FF66), Color(0xFF00C853)))
        "galaxy_frame" -> Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF3D5AFE)))
        "rainbow_frame" -> Brush.linearGradient(listOf(Color(0xFFFF0055), Color(0xFFFFDD00)))
        else -> {
            val isMonochrome = (themeColor.red < 0.22f && themeColor.green < 0.22f && themeColor.blue < 0.22f) ||
                    (themeColor.red > 0.80f && themeColor.green > 0.80f && themeColor.blue > 0.80f)

            if (isMonochrome) {
                Brush.sweepGradient(listOf(Color(0xFFFFFFFF), Color(0xFFA0A5B5), Color(0xFFE8EDF8), Color(0xFF656A7A), Color(0xFFFFFFFF)))
            } else {
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(
                    android.graphics.Color.argb(
                        (themeColor.alpha * 255).toInt(),
                        (themeColor.red * 255).toInt(),
                        (themeColor.green * 255).toInt(),
                        (themeColor.blue * 255).toInt()
                    ),
                    hsv
                )
                val baseHue = hsv[0]
                val sat = hsv[1].coerceIn(0.70f, 0.98f)
                val value = hsv[2].coerceIn(0.85f, 1f)

                val c1 = Color(android.graphics.Color.HSVToColor(floatArrayOf(baseHue, sat, value)))
                val c2 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
                val c3 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 70f) % 360f, (sat * 0.85f).coerceIn(0.55f, 1f), value)))
                val c4 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
                val c5 = c1

                Brush.sweepGradient(listOf(c1, c2, c3, c4, c5))
            }
        }
    }
}

fun getBoardSkinGridColor(skinId: String): Color = when (skinId.lowercase()) {
    "cyberpunk" -> Color(0xFF00FFCC)
    "matrix", "emerald_matrix" -> Color(0xFF00FF00)
    "violet" -> Color(0xFF8A2BE2)
    "ruby" -> Color(0xFFFF014C)
    "monochrome", "carbon_neutral" -> Color(0xFF8E9297)
    "retro_amber" -> Color(0xFFFFB300)
    "vaporwave_pink" -> Color(0xFFFF4081)
    "midnight_gold" -> Color(0xFFFFD700)
    "plasma_storm" -> Color(0xFF7C4DFF)
    "glacial_frost" -> Color(0xFF80D8FF)
    else -> Color(0xFF00FFCC)
}

fun getControlBtnStyleBrush(styleId: String): Brush = when (styleId) {
    "gold_legendary", "gold" -> Brush.verticalGradient(
        listOf(Color(0xFFFFD54F).copy(alpha = 0.65f), Color(0xFFFF8F00).copy(alpha = 0.45f))
    )
    "plasma_legendary", "plasma" -> Brush.verticalGradient(
        listOf(Color(0xFF7C4DFF).copy(alpha = 0.55f), Color(0xFF00E5FF).copy(alpha = 0.45f))
    )
    "glass" -> Brush.verticalGradient(
        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.12f))
    )
    "neon" -> Brush.verticalGradient(
        listOf(Color(0xFF00E5FF).copy(alpha = 0.25f), Color(0xFF00B0FF).copy(alpha = 0.12f))
    )
    else -> Brush.verticalGradient(
        listOf(Color(0xFF333A48), Color(0xFF222834))
    )
}

fun getControlBtnBorderStroke(styleId: String): BorderStroke = when (styleId) {
    "gold_legendary", "gold" -> BorderStroke(
        1.5.dp,
        Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFF9C4), Color(0xFFFFA000), Color(0xFFFFD700)))
    )
    "plasma_legendary", "plasma" -> BorderStroke(
        1.5.dp,
        Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFE040FB), Color(0xFF00E5FF)))
    )
    "glass" -> BorderStroke(1.2.dp, Color.White.copy(alpha = 0.45f))
    "neon" -> BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.8f))
    else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
}

fun getRankColors(rankId: String): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> = when (rankId.uppercase()) {
    "BRONZE" -> Triple(Color(0xFFCD7F32), Color(0xFF8B4513), Icons.Default.Shield)
    "SILVER" -> Triple(Color(0xFFC0C0C0), Color(0xFF708090), Icons.Default.Shield)
    "GOLD" -> Triple(Color(0xFFFFD700), Color(0xFFFF8F00), Icons.Default.Star)
    "PLATINUM" -> Triple(Color(0xFF00E5FF), Color(0xFF0097A7), Icons.Default.Diamond)
    "DIAMOND" -> Triple(Color(0xFF00B0FF), Color(0xFF1565C0), Icons.Default.Diamond)
    "MASTER" -> Triple(Color(0xFFA855F7), Color(0xFF6B21A8), Icons.Default.WorkspacePremium)
    "GRANDMASTER" -> Triple(Color(0xFFEF4444), Color(0xFF991B1B), Icons.Default.MilitaryTech)
    "CHALLENGER" -> Triple(Color(0xFFEC4899), Color(0xFF831843), Icons.Default.Whatshot)
    else -> Triple(Color(0xFFFFD700), Color(0xFFFF8F00), Icons.Default.MilitaryTech)
}

fun getModeVisuals(modeId: String): Pair<List<Color>, androidx.compose.ui.graphics.vector.ImageVector> = when (modeId.lowercase()) {
    "time_attack" -> Pair(listOf(Color(0xFFFF5722), Color(0xFFFF1744)), Icons.Default.Timer)
    "fast_run" -> Pair(listOf(Color(0xFFFFD600), Color(0xFFFF9100)), Icons.Default.Speed)
    "relax" -> Pair(listOf(Color(0xFF00E676), Color(0xFF00B0FF)), Icons.Default.Spa)
    "mirror" -> Pair(listOf(Color(0xFF9C27B0), Color(0xFF00E5FF)), Icons.Default.Flip)
    "extended" -> Pair(listOf(Color(0xFF3D5AFE), Color(0xFF00E5FF)), Icons.Default.Extension)
    "reverse" -> Pair(listOf(Color(0xFF651FFF), Color(0xFFFF4081)), Icons.Default.SwapVert)
    "block_blast" -> Pair(listOf(Color(0xFFFF9800), Color(0xFFFF5722)), Icons.Default.GridView)
    "pattern" -> Pair(listOf(Color(0xFF00BCD4), Color(0xFF3F51B5)), Icons.Default.AutoFixHigh)
    "perfectionist" -> Pair(listOf(Color(0xFFFFD700), Color(0xFFFF6D00)), Icons.Default.Stars)
    else -> Pair(listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF)), Icons.Default.PlayCircleOutline)
}

data class StoreItemInspectData(
    val itemType: String,
    val itemId: String,
    val title: String,
    val description: String,
    val category: String,
    val rarity: DropRarity,
    val cost: Int,
    val isOwned: Boolean,
    val isActive: Boolean,
    val isLocked: Boolean = false,
    val onAction: () -> Unit
)

@Composable
fun StoreItemVisualPreview(
    itemType: String?,
    itemId: String?,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    rarityColor: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier.size(46.dp),
    themeColor: Color = MaterialTheme.colorScheme.primary,
    onInspectClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .then(
                if (onInspectClick != null) Modifier.clickable { onInspectClick() } else Modifier
            ),
        shape = RoundedCornerShape(13.dp),
        color = Color(0xFF0D121B),
        border = BorderStroke(
            1.2.dp,
            if (isActive) MaterialTheme.colorScheme.primary
            else rarityColor.copy(alpha = 0.4f)
        ),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (itemType?.uppercase()) {
                "BLOCK" -> {
                    val blockStyle = itemId ?: "flat"
                    Canvas(modifier = Modifier.size(34.dp)) {
                        val cellPx = size.width / 2.2f
                        val startX = (size.width - cellPx * 2f) / 2f
                        val startY = (size.height - cellPx * 2f) / 2f
                        drawBlock(Color(0xFF00E5FF), startX, startY, cellPx, blockStyle)
                        drawBlock(Color(0xFFE040FB), startX + cellPx, startY, cellPx, blockStyle)
                        drawBlock(Color(0xFFFFD600), startX, startY + cellPx, cellPx, blockStyle)
                        drawBlock(Color(0xFF00E676), startX + cellPx, startY + cellPx, cellPx, blockStyle)
                    }
                }
                "FRAME" -> {
                    val frameId = itemId ?: "standard"
                    val brush = getAvatarFrameBrush(frameId, themeColor, MaterialTheme.colorScheme.secondary)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2430))
                            .border(2.5.dp, brush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                "SKIN" -> {
                    val skinId = itemId ?: "cyberpunk"
                    val gridColor = getBoardSkinGridColor(skinId)
                    Canvas(modifier = Modifier.size(36.dp)) {
                        val cols = 4
                        val rows = 4
                        val cSize = size.width / cols.toFloat()
                        for (i in 0..cols) {
                            val pos = i * cSize
                            drawLine(gridColor.copy(alpha = 0.45f), Offset(pos, 0f), Offset(pos, size.height), strokeWidth = 1f)
                            drawLine(gridColor.copy(alpha = 0.45f), Offset(0f, pos), Offset(size.width, pos), strokeWidth = 1f)
                        }
                        drawRoundRect(
                            color = gridColor,
                            topLeft = Offset(cSize * 1f + 1f, cSize * 2f + 1f),
                            size = Size(cSize - 2f, cSize - 2f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                        drawRoundRect(
                            color = Color(0xFFE040FB),
                            topLeft = Offset(cSize * 2f + 1f, cSize * 2f + 1f),
                            size = Size(cSize - 2f, cSize - 2f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                    }
                }
                "BUTTON" -> {
                    val btnStyle = itemId ?: "classic"
                    val bgBrush = getControlBtnStyleBrush(btnStyle)
                    val border = getControlBtnBorderStroke(btnStyle)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgBrush)
                            .border(border, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                "RANK" -> {
                    val rankId = itemId ?: "BRONZE"
                    val (c1, _, rankIcon) = getRankColors(rankId)
                    val rankGrad = Brush.radialGradient(listOf(c1.copy(alpha = 0.35f), Color.Transparent))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(rankGrad, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = rankIcon,
                            contentDescription = null,
                            tint = c1,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                "TITLE" -> {
                    val titleId = itemId ?: "none"
                    val (tagCol, tagIcon) = when (titleId) {
                        "node" -> Pair(Color(0xFF00E5FF), Icons.Default.Hub)
                        "lord" -> Pair(Color(0xFFFFD700), Icons.Default.Shield)
                        "cosmic_overlord" -> Pair(Color(0xFFD500F9), Icons.Default.Public)
                        "ai_consensus" -> Pair(Color(0xFF00FF66), Icons.Default.SmartToy)
                        else -> Pair(Color.Gray, Icons.Default.Close)
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tagCol.copy(alpha = 0.16f))
                            .border(1.dp, tagCol.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tagIcon,
                            contentDescription = null,
                            tint = tagCol,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                "MODE" -> {
                    val modeId = itemId ?: "classic"
                    val (gradColors, modeIcon) = getModeVisuals(modeId)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Brush.linearGradient(gradColors))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                "PRESTIGE" -> {
                    val pNum = when (itemId) {
                        "prestige_1" -> "I"
                        "prestige_2" -> "II"
                        "prestige_3" -> "III"
                        else -> "★"
                    }
                    val mult = when (itemId) {
                        "prestige_1" -> "x2"
                        "prestige_2" -> "x4"
                        "prestige_3" -> "x8"
                        else -> ""
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = pNum,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFFFFD700)
                        )
                        if (mult.isNotEmpty()) {
                            Text(
                                text = mult,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.5.sp
                                ),
                                color = Color(0xFFFF5252)
                            )
                        }
                    }
                }
                else -> {
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = if (isActive) MaterialTheme.colorScheme.primary else rarityColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StoreItemInspectDialog(
    data: StoreItemInspectData,
    currentLang: Language,
    themeColor: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = data.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = data.rarity.color.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = data.rarity.getLocalizedName(currentLang).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = data.rarity.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF090D14),
                    border = BorderStroke(1.5.dp, data.rarity.color.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (data.itemType.uppercase()) {
                            "BLOCK" -> {
                                Canvas(modifier = Modifier.size(96.dp)) {
                                    val cell = size.width / 4.4f
                                    val tColor = Color(0xFFE040FB)
                                    drawBlock(tColor, cell * 0.2f, cell * 0.2f, cell, data.itemId)
                                    drawBlock(tColor, cell * 1.2f, cell * 0.2f, cell, data.itemId)
                                    drawBlock(tColor, cell * 2.2f, cell * 0.2f, cell, data.itemId)
                                    drawBlock(tColor, cell * 1.2f, cell * 1.2f, cell, data.itemId)

                                    val oColor = Color(0xFFFFD600)
                                    drawBlock(oColor, cell * 2.2f, cell * 2.2f, cell, data.itemId)
                                    drawBlock(oColor, cell * 3.2f, cell * 2.2f, cell, data.itemId)
                                    drawBlock(oColor, cell * 2.2f, cell * 3.2f, cell, data.itemId)
                                    drawBlock(oColor, cell * 3.2f, cell * 3.2f, cell, data.itemId)

                                    val iColor = Color(0xFF00E5FF)
                                    drawBlock(iColor, cell * 0.2f, cell * 2.4f, cell, data.itemId)
                                    drawBlock(iColor, cell * 0.2f, cell * 3.4f, cell, data.itemId)
                                }
                            }
                            "FRAME" -> {
                                val brush = getAvatarFrameBrush(data.itemId, themeColor, MaterialTheme.colorScheme.secondary)
                                Box(
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B2230))
                                        .border(4.dp, brush, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            "SKIN" -> {
                                val gridColor = getBoardSkinGridColor(data.itemId)
                                Canvas(modifier = Modifier.size(110.dp)) {
                                    val cols = 6
                                    val rows = 6
                                    val cell = size.width / cols
                                    for (i in 0..cols) {
                                        val p = i * cell
                                        drawLine(gridColor.copy(alpha = 0.45f), Offset(p, 0f), Offset(p, size.height), strokeWidth = 1.2f)
                                        drawLine(gridColor.copy(alpha = 0.45f), Offset(0f, p), Offset(size.width, p), strokeWidth = 1.2f)
                                    }
                                    val colors = listOf(Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFFFFD600), Color(0xFF00E676))
                                    for (c in 1..4) {
                                        drawRoundRect(
                                            color = colors[c - 1],
                                            topLeft = Offset(c * cell + 1f, 4 * cell + 1f),
                                            size = Size(cell - 2f, cell - 2f),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                                        )
                                    }
                                }
                            }
                            "BUTTON" -> {
                                var demoPressed by remember { mutableStateOf(false) }
                                val bgBrush = getControlBtnStyleBrush(data.itemId)
                                val border = getControlBtnBorderStroke(data.itemId)
                                Surface(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { demoPressed = !demoPressed },
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.Transparent,
                                    border = border
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(bgBrush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                            "RANK" -> {
                                val (c1, _, rankIcon) = getRankColors(data.itemId)
                                val rankGrad = Brush.radialGradient(listOf(c1.copy(alpha = 0.4f), Color.Transparent))
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(rankGrad, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = rankIcon,
                                        contentDescription = null,
                                        tint = c1,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }
                            "TITLE" -> {
                                val (tagCol, tagIcon) = when (data.itemId) {
                                    "node" -> Pair(Color(0xFF00E5FF), Icons.Default.Hub)
                                    "lord" -> Pair(Color(0xFFFFD700), Icons.Default.Shield)
                                    "cosmic_overlord" -> Pair(Color(0xFFD500F9), Icons.Default.Public)
                                    "ai_consensus" -> Pair(Color(0xFF00FF66), Icons.Default.SmartToy)
                                    else -> Pair(Color.Gray, Icons.Default.Close)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tagCol.copy(alpha = 0.18f),
                                    border = BorderStroke(1.5.dp, tagCol.copy(alpha = 0.75f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(tagIcon, contentDescription = null, tint = tagCol, modifier = Modifier.size(24.dp))
                                        Text(
                                            text = data.title.uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                            color = tagCol
                                        )
                                    }
                                }
                            }
                            "MODE" -> {
                                val (gradColors, modeIcon) = getModeVisuals(data.itemId)
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Brush.linearGradient(gradColors))
                                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                            "PRESTIGE" -> {
                                val pNum = when (data.itemId) {
                                    "prestige_1" -> "I"
                                    "prestige_2" -> "II"
                                    "prestige_3" -> "III"
                                    else -> "★"
                                }
                                val pMult = when (data.itemId) {
                                    "prestige_1" -> "x2"
                                    "prestige_2" -> "x4"
                                    "prestige_3" -> "x8"
                                    else -> ""
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = pNum,
                                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = pMult,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFF5252)
                                    )
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = data.rarity.color,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (data.cost > 0 && !data.isOwned) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val coinsLabel = when (currentLang) {
                                Language.RU -> "монет"
                                Language.UA -> "монет"
                                Language.KK -> "тиын"
                                Language.DE -> "Münzen"
                                Language.ZH -> "金币"
                                else -> "coins"
                            }
                            Text(
                                text = "${data.cost} $coinsLabel",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    data.onAction()
                },
                enabled = !data.isLocked,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (data.isActive) MaterialTheme.colorScheme.primaryContainer
                                     else MaterialTheme.colorScheme.primary,
                    contentColor = if (data.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                val actionBtnText = when {
                    data.isActive -> when (currentLang) {
                        Language.RU -> "АКТИВНО"
                        Language.UA -> "АКТИВНО"
                        Language.KK -> "БЕЛСЕНДІ"
                        Language.DE -> "AKTIV"
                        Language.ZH -> "已启用"
                        else -> "ACTIVE"
                    }
                    data.isOwned -> when (currentLang) {
                        Language.RU -> "ВЫБРАТЬ"
                        Language.UA -> "ОБРАТИ"
                        Language.KK -> "ТАҢДАУ"
                        Language.DE -> "WÄHLEN"
                        Language.ZH -> "选择"
                        else -> "EQUIP"
                    }
                    data.isLocked -> when (currentLang) {
                        Language.RU -> "НЕДОСТУПНО"
                        Language.UA -> "НЕДОСТУПНО"
                        Language.KK -> "ҚОЛЖЕТІМСІЗ"
                        Language.DE -> "GESPERRT"
                        Language.ZH -> "锁定"
                        else -> "LOCKED"
                    }
                    else -> when (currentLang) {
                        Language.RU -> "РАЗБЛОКИРОВАТЬ"
                        Language.UA -> "РОЗБЛОКУВАТИ"
                        Language.KK -> "АШУ"
                        Language.DE -> "KAUFEN"
                        Language.ZH -> "解锁"
                        else -> "UNLOCK"
                    }
                }
                Text(actionBtnText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.get("back", currentLang), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun StoreItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    category: String,
    title: String,
    description: String,
    isActive: Boolean,
    isOwned: Boolean,
    cost: Int,
    currentLang: Language,
    isLocked: Boolean = false,
    rarity: DropRarity? = null,
    itemType: String? = null,
    itemId: String? = null,
    isSquare: Boolean = true,
    onInspect: (() -> Unit)? = null,
    onAction: () -> Unit
) {
    val effectiveRarity = rarity ?: when {
        cost <= 0 -> DropRarity.COMMON
        cost <= 400 -> DropRarity.UNCOMMON
        cost <= 800 -> DropRarity.RARE
        cost <= 2000 -> DropRarity.EPIC
        else -> DropRarity.LEGENDARY
    }
    val rarityText = effectiveRarity.getLocalizedName(currentLang).uppercase()
    val rarityColor = effectiveRarity.color

    ElevatedCard(
        modifier = (if (isSquare) Modifier.fillMaxWidth().height(172.dp) else Modifier.fillMaxWidth())
            .then(
                if (isActive) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            )
            .clickable(enabled = !isLocked) { onAction() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                             else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = (if (isSquare) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                .padding(11.dp),
            verticalArrangement = if (isSquare) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Visual Preview + Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreItemVisualPreview(
                    itemType = itemType,
                    itemId = itemId,
                    fallbackIcon = icon,
                    rarityColor = rarityColor,
                    isActive = isActive,
                    modifier = Modifier.size(42.dp),
                    onInspectClick = onInspect
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onInspect != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onInspect() }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Inspect",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (isActive) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = rarityColor.copy(alpha = 0.14f)
                    ) {
                        AdaptiveText(
                            text = rarityText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            ),
                            color = rarityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Middle: Title & Description
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    minLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    minLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Bottom: Action Button / Status (Consistent 32.dp height on ALL cards)
            val isRank = category.equals("Rank", ignoreCase = true) || 
                         category.equals("Ранг", ignoreCase = true) ||
                         category.equals("Rang", ignoreCase = true) ||
                         category.equals("段位", ignoreCase = true)

            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().height(32.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "АКТИВНО"
                                Language.UA -> "АКТИВНО"
                                Language.KK -> "БЕЛСЕНДІ"
                                Language.DE -> "AKTIV"
                                Language.ZH -> "已启用"
                                else -> "ACTIVE"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (isOwned && !isRank) {
                val ownedLabel = if (itemType == "MODE") {
                    when (currentLang) {
                        Language.RU -> "РАЗБЛОКИРОВАНО"
                        Language.UA -> "РОЗБЛОКОВАНО"
                        Language.KK -> "АШЫЛҒАН"
                        Language.DE -> "FREIGESCHALTET"
                        Language.ZH -> "已解锁"
                        else -> "UNLOCKED"
                    }
                } else {
                    when (currentLang) {
                        Language.RU -> "ВЫБРАТЬ"
                        Language.UA -> "ОБРАТИ"
                        Language.KK -> "ТАҢДАУ"
                        Language.DE -> "WÄHLEN"
                        Language.ZH -> "选择"
                        else -> "EQUIP"
                    }
                }
                OutlinedButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = ownedLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                val buttonEnabled = when {
                    isRank -> !isOwned
                    else -> !isLocked
                }
                val lockBtnText = when (currentLang) {
                    Language.RU -> "БЛОК"
                    Language.UA -> "БЛОК"
                    Language.KK -> "ҚҰЛЫП"
                    Language.DE -> "GESPERRT"
                    Language.ZH -> "未解锁"
                    else -> "LOCKED"
                }
                Button(
                    onClick = onAction,
                    enabled = buttonEnabled,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceContainerHighest
                                         else MaterialTheme.colorScheme.primary,
                        contentColor = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                       else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (isLocked) lockBtnText else if (cost <= 0) (when (currentLang) {
                            Language.RU -> "БЕСПЛАТНО"
                            Language.UA -> "БЕЗКОШТОВНО"
                            Language.KK -> "ТЕГІН"
                            Language.DE -> "GRATIS"
                            Language.ZH -> "免费"
                            else -> "FREE"
                        }) else "$cost монет",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsTabContent(
    viewModel: MainViewModel,
    themeColor: Color,
    currentLang: Language
) {
    val achievements by viewModel.achievementsList.collectAsStateWithLifecycle()
    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val totalCount = remember(achievements) { achievements.size }
    val progressFactor = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    var achievementFilter by remember { mutableStateOf("ALL") }
    val filterOptions = remember(currentLang) {
        listOf(
            "ALL" to when (currentLang) {
                Language.RU -> "Все"
                Language.UA -> "Всі"
                Language.KK -> "Барлығы"
                Language.DE -> "Alle"
                Language.ZH -> "全部"
                else -> "All"
            },
            "UNLOCKED" to when (currentLang) {
                Language.RU -> "Открытые"
                Language.UA -> "Відкриті"
                Language.KK -> "Ашылғандар"
                Language.DE -> "Freigeschaltet"
                Language.ZH -> "已解锁"
                else -> "Unlocked"
            },
            "LOCKED" to when (currentLang) {
                Language.RU -> "В процессе"
                Language.UA -> "В процесі"
                Language.KK -> "Орындалуда"
                Language.DE -> "In Bearbeitung"
                Language.ZH -> "进行中"
                else -> "In Progress"
            }
        )
    }

    val filteredAchievements = remember(achievements, achievementFilter) {
        when (achievementFilter) {
            "UNLOCKED" -> achievements.filter { it.isUnlocked }
            "LOCKED" -> achievements.filter { !it.isUnlocked }
            else -> achievements
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 12.dp,
            end = 16.dp,
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hall of Fame Hero Card
        item {
            val hallOfFameTitle = when (currentLang) {
                Language.RU -> "ЗАЛ СЛАВЫ"
                Language.UA -> "ЗАЛА СЛАВИ"
                Language.KK -> "ДАҢҚ ЗАЛЫ"
                Language.DE -> "RUHMESHALLE"
                Language.ZH -> "荣誉殿堂"
                else -> "HALL OF FAME"
            }
            val unlockedProgressText = when (currentLang) {
                Language.RU -> "Разблокировано наград: $unlockedCount из $totalCount (${(progressFactor * 100).toInt()}%)"
                Language.UA -> "Розблоковано нагород: $unlockedCount з $totalCount (${(progressFactor * 100).toInt()}%)"
                Language.KK -> "Ашылған жетістіктер: $totalCount ішінен $unlockedCount (${(progressFactor * 100).toInt()}%)"
                Language.DE -> "Freigeschaltete Erfolge: $unlockedCount von $totalCount (${(progressFactor * 100).toInt()}%)"
                Language.ZH -> "已解锁成就：$unlockedCount / $totalCount (${(progressFactor * 100).toInt()}%)"
                else -> "Unlocked achievements: $unlockedCount of $totalCount (${(progressFactor * 100).toInt()}%)"
            }
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = hallOfFameTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = unlockedProgressText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progressFactor },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (filterKey, filterLabel) ->
                    val isSelected = achievementFilter == filterKey
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            achievementFilter = filterKey
                        },
                        label = {
                            Text(
                                text = filterLabel,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null
                    )
                }
            }
        }

        items(filteredAchievements.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (ach in pair) {
                    Box(modifier = Modifier.weight(1f)) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(38.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (ach.isUnlocked) MaterialTheme.colorScheme.primaryContainer
                                               else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        tonalElevation = 1.dp
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (ach.iconType) {
                                                    "lines" -> Icons.Default.Extension
                                                    "score" -> Icons.Default.Star
                                                    "crown" -> Icons.Default.Shield
                                                    "speed" -> Icons.Default.Settings
                                                    "blast" -> Icons.Default.Extension
                                                    "combo" -> Icons.AutoMirrored.Filled.VolumeUp
                                                    else -> Icons.Default.EmojiEvents
                                                },
                                                contentDescription = null,
                                                tint = if (ach.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer
                                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    val rewardLabel = when {
                                        ach.crateKeyReward != null && ach.crateKeyCount > 0 && ach.pointsReward > 0 -> {
                                            val keyName = when (ach.crateKeyReward) {
                                                "wooden" -> if (currentLang == Language.RU) "Дер. Ключ" else "Wooden"
                                                "iron" -> if (currentLang == Language.RU) "Жел. Ключ" else "Iron"
                                                "golden" -> if (currentLang == Language.RU) "Зол. Ключ" else "Gold"
                                                "platinum" -> if (currentLang == Language.RU) "Плат. Ключ" else "Plat"
                                                "legendary" -> if (currentLang == Language.RU) "Лег. Ключ" else "Legend"
                                                "diamond" -> if (currentLang == Language.RU) "Алмаз" else "Diamond"
                                                "red_crate", "red_crate_lite" -> if (currentLang == Language.RU) "Красный" else "Red"
                                                else -> "Key"
                                            }
                                            "+${ach.pointsReward} монет + 1x $keyName"
                                        }
                                        ach.crateKeyReward != null && ach.crateKeyCount > 0 -> {
                                            val keyName = when (ach.crateKeyReward) {
                                                "wooden" -> if (currentLang == Language.RU) "Дер. Ключ" else "Wooden"
                                                "iron" -> if (currentLang == Language.RU) "Жел. Ключ" else "Iron"
                                                "golden" -> if (currentLang == Language.RU) "Зол. Ключ" else "Gold"
                                                "platinum" -> if (currentLang == Language.RU) "Плат. Ключ" else "Plat"
                                                "legendary" -> if (currentLang == Language.RU) "Лег. Ключ" else "Legend"
                                                "diamond" -> if (currentLang == Language.RU) "Алмаз" else "Diamond"
                                                "red_crate", "red_crate_lite" -> if (currentLang == Language.RU) "Красный" else "Red"
                                                else -> "Key"
                                            }
                                            "1x $keyName"
                                        }
                                        ach.pointsReward > 0 -> "+${ach.pointsReward} монет"
                                        else -> if (currentLang == Language.RU) "Престиж" else "Prestige"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                               else MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Text(
                                            text = rewardLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                            color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = Translations.getLocalizedAchievementTitle(ach.id, currentLang, ach.titleEn, ach.titleRu),
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )

                                val achDesc = Translations.getLocalizedAchievementDesc(ach.id, currentLang, ach.descriptionEn, ach.descriptionRu)

                                Text(
                                    text = achDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val fraction = if (ach.targetValue > 0) {
                                    (ach.currentValue.toFloat() / ach.targetValue).coerceIn(0f, 1f)
                                } else 0f

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${ach.currentValue}/${ach.targetValue}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 8.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NewTabContent(
    currentLang: Language,
    themeColor: Color
) {
    val appVersionName = rememberAppVersionName()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // О приложении / About App Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val aboutAppTitle = when (currentLang) {
                    Language.RU -> "О ПРИЛОЖЕНИИ"
                    Language.UA -> "ПРО ДОДАТОК"
                    Language.KK -> "ҚОСЫМША ТУРАЛЫ"
                    Language.DE -> "ÜBER DIE APP"
                    Language.ZH -> "关于应用"
                    else -> "ABOUT APP"
                }
                val aboutAppDesc = when (currentLang) {
                    Language.RU -> "FlowTess — это продвинутая версия классической головоломки, сочетающая в себе традиционный игровой процесс и инновационный режим ZETA Arena (Block Blast). В игре доступны кастомизация интерфейса, система скинов, звуковые паки, глобальные рекорды, синхронизация прогресса с облаком и полноценный мультиплеер с чатом."
                    Language.UA -> "FlowTess — це просунута версія класичної головоломки, що поєднує традиційний ігровий процес та інноваційний режим ZETA Arena (Block Blast). У грі доступні кастомізація інтерфейсу, система скінів, звукові паки, глобальні рекорди, синхронізація з хмарою та мультиплеєр з чатом."
                    Language.KK -> "FlowTess — бұл дәстүрлі ойын процесі мен инновациялық ZETA Arena (Block Blast) режимін біріктіретін классикалық басқатырғыштың заманауи нұсқасы. Ойында интерфейсті баптау, мұқабалар жүйесі, дыбыстар жиынтығы, ғаламдық рекордтар, бұлтты синхрондау және чаты бар толық мультиплеер қолжетімді."
                    Language.DE -> "FlowTess ist eine moderne Weiterentwicklung des klassischen Puzzlespiels. Es verbindet traditionelles Gameplay nahtlos mit der innovativen ZETA Arena (Block Blast). Zu den Features gehören Oberflächenanpassung, Skin-Pakete, Soundboards, globale Bestenlisten, Cloud-Synchronisierung und ein Mehrspieler-Modus mit Live-Chat."
                    Language.ZH -> "FlowTess 是经典方块消除游戏的全新进化版。它将传统玩法与创新的 ZETA Arena (Block Blast) 模式完美融合。支持全界面深度自定义、皮肤包、音效包、全球排行榜、云端存档同步以及带实时聊天的多人联机对战。"
                    else -> "FlowTess is an advanced evolution of the classic block puzzle game. It seamlessly blends traditional gameplay with the innovative ZETA Arena (Block Blast). Features include full interface customization, skin packs, unique soundboards, global high scores, secure cloud sync, and a multiplayer match lobby with live chat."
                }
                val versionLabel = when (currentLang) {
                    Language.RU -> "Версия"
                    Language.UA -> "Версія"
                    Language.KK -> "Нұсқа"
                    Language.DE -> "Version"
                    Language.ZH -> "版本"
                    else -> "Version"
                }
                val developerLabel = when (currentLang) {
                    Language.RU -> "Разработчик"
                    Language.UA -> "Розробник"
                    Language.KK -> "Әзірлеуші"
                    Language.DE -> "Entwickler"
                    Language.ZH -> "开发者"
                    else -> "Developer"
                }
                Text(
                    text = aboutAppTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = aboutAppDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = versionLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = appVersionName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = developerLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "FsFq",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Список изменений / Change Log Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val changelogTitle = when (currentLang) {
                    Language.RU -> "СПИСОК ИЗМЕНЕНИЙ"
                    Language.UA -> "СПИСОК ЗМІН"
                    Language.KK -> "ӨЗГЕРІСТЕР ТІЗІМІ"
                    Language.DE -> "ÄNDERUNGSPROTOKOLL"
                    Language.ZH -> "更新日志"
                    else -> "CHANGE LOG"
                }
                Text(
                    text = changelogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Current Version Card Content
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val v20Header = when (currentLang) {
                        Language.RU -> "Версия $appVersionName (Текущая)"
                        Language.UA -> "Версія $appVersionName (Поточна)"
                        Language.KK -> "Нұсқа $appVersionName (Ағымдағы)"
                        Language.DE -> "Version $appVersionName (Aktuell)"
                        Language.ZH -> "版本 $appVersionName (当前)"
                        else -> "Version $appVersionName (Current)"
                    }
                    Text(
                        text = v20Header,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    
                    val changes = when (currentLang) {
                        Language.RU -> listOf(
                            "• Добавлена строгая верификация почты через Firebase (защита онлайн-функций)",
                            "• Добавлен переключатель видимости вкладки 'Новое' в настройках (раздел 'Система')",
                            "• Кэширование FirebaseAuth для оптимизации скорости работы",
                            "• Полная реструктуризация кода с добавлением двуязычных неформальных комментариев",
                            "• Балансировка экономики: стоимость открытия кейсов увеличена на 25%, награды скорректированы",
                            "• Переименованы научно-фантастические и ИИ-достижения в строгие классические названия",
                            "• Ужесточена проверка проигрыша (строго по ряду 2 или выше)",
                            "• Немедленное удаление сессии при поражении для предотвращения дюпа монет"
                        )
                        Language.UA -> listOf(
                            "• Додана сувора верифікація пошти через Firebase (захист онлайн-функцій)",
                            "• Додано перемикач видимості вкладки 'Нове' в налаштуваннях (розділ 'Система')",
                            "• Кешування FirebaseAuth для оптимізації швидкодії",
                            "• Повна реструктуризація коду та оптимізація продуктивності",
                            "• Балансування економіки: вартість кейсів та нагороди скориговано",
                            "• Оновлено назви та опис досягнень на класичні",
                            "• Посилено перевірку завершення гри",
                            "• Негайне видалення збереженої гри при поразці"
                        )
                        Language.KK -> listOf(
                            "• Firebase арқылы қатаң пошта растауы қосылды (онлайн-функцияларды қорғау)",
                            "• Баптауларда 'Жаңа' қойындысының көріну қосқышы қосылды",
                            "• Жұмыс жылдамдығын оңтайландыру үшін FirebaseAuth кэштеу",
                            "• Кодты толық қайта құрылымдау және оңтайландыру",
                            "• Экономика балансы: кейстер құны мен сыйақылар түзетілді",
                            "• Жетістіктердің классикалық атаулары жаңартылды",
                            "• Ойынның аяқталуын тексеру күшейтілді",
                            "• Жеңіліс кезінде сақталған ойынды бірден өшіру"
                        )
                        Language.DE -> listOf(
                            "• Strikte Firebase-E-Mail-Verifizierung hinzugefügt (schützt Online-Funktionen)",
                            "• Umschalter für die Sichtbarkeit des 'Neu'-Reiters in den Einstellungen hinzugefügt",
                            "• Globale FirebaseAuth-Instanz-Optimierung",
                            "• Vollständiges Codebase-Refactoring zur Performanceverbesserung",
                            "• Wirtschafts-Balancing: Kistenpreise und Belohnungen angepasst",
                            "• Klassische Namen für alle Errungenschaften",
                            "• Strengere Game-Over-Erkennung",
                            "• Sofortiges Löschen von Spielständen bei Niederlage"
                        )
                        Language.ZH -> listOf(
                            "• 新增 Firebase 严格邮箱验证系统（保障在线对战及联机功能安全）",
                            "• 设置界面新增「新特性」选项卡显隐开关",
                            "• 优化 FirebaseAuth 全局缓存，提升加载速度",
                            "• 代码结构全面重构与性能深度调优",
                            "• 经济平衡调整：优化宝箱开启成本与奖励倍率",
                            "• 重构成就系统名称与图标，风格更经典专业",
                            "• 优化游戏结束检测判定",
                            "• 失败时立即销毁临时进度以防止金币漏洞"
                        )
                        else -> listOf(
                            "• Added strict Firebase Email Verification system (verifying email gates online play and chat)",
                            "• Added toggle switch for the 'New' tab visibility in settings (System tab)",
                            "• Cached FirebaseAuth instance globally to eliminate redundant service queries",
                            "• Full codebase review with helpful, informal RU/EN comments",
                            "• Economy balancing: increased crate costs by 25% and adjusted reward rates",
                            "• Rebranded space/AI achievements into professional, classic names",
                            "• Implemented strict game over rules (immediately triggers when block reaches row 2)",
                            "• Added immediate saved game destruction on defeat to fix coin exploits"
                        )
                    }

                    changes.forEach { change ->
                        Text(
                            text = change,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Version 1.0 Card Content
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val v10Header = when (currentLang) {
                        Language.RU -> "Версия 1.0"
                        Language.UA -> "Версія 1.0"
                        Language.KK -> "Нұсқа 1.0"
                        Language.DE -> "Version 1.0"
                        Language.ZH -> "版本 1.0"
                        else -> "Version 1.0"
                    }
                    val v10Desc = when (currentLang) {
                        Language.RU -> "• Первый релиз: режимы FlowTess и ZETA, лобби мультиплеера, аватары, рамки, теги и профиль."
                        Language.UA -> "• Перший реліз: режими FlowTess і ZETA, лобі мультиплеєра, аватари, рамки, теги та профіль."
                        Language.KK -> "• Алғашқы шығарылым: FlowTess және ZETA режимдері, мультиплеер лоббиі, аватарлар, жақтаулар, тегтер және профиль."
                        Language.DE -> "• Erstveröffentlichung: FlowTess- & ZETA-Modi, Mehrspieler-Lobby, Avatare, Rahmen, Tags und Profil."
                        Language.ZH -> "• 初始版本发布：FlowTess 与 ZETA 双模式、多人对战大厅、个性头像、相框商店、排行榜标签与个人资料。"
                        else -> "• Initial launch: FlowTess & ZETA gameplay modes, multiplayer matchmaking lobbies, custom avatars, frame store, and tags."
                    }
                    Text(
                        text = v10Header,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = v10Desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class RankData(val id: String, val cost: Int, val description: String)
data class SkinData(val id: String, val cost: Int, val displayName: String, val description: String)
data class GameModeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val emoji: String)
data class CubeSkinStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val previewEmoji: String)
data class AvatarFrameStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val rarityColor: Color)
data class PlayerBadgeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val emoji: String)
data class ThemeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val themeColor: Color)
data class FontStoreData(val id: String, val cost: Int, val displayName: String, val description: String)
data class ControlButtonStyleStoreData(val id: String, val cost: Int, val displayName: String, val description: String)

data class UnlockedItemInfo(
    val id: String,
    val title: String,
    val isUnlocked: Boolean,
    val isEquipped: Boolean = false,
    val rarity: DropRarity? = null
)

@Composable
fun ProfileUnlockedCategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    items: List<UnlockedItemInfo>,
    currentLang: Language
) {
    val unlockedCount = items.count { it.isUnlocked }
    val totalCount = items.size

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row with count badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (unlockedCount == totalCount) {
                        Color(0xFF2E7D32).copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                ) {
                    Text(
                        text = "$unlockedCount / $totalCount",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (unlockedCount == totalCount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Compact Progress bar
            LinearProgressIndicator(
                progress = { if (totalCount > 0) unlockedCount.toFloat() / totalCount.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (unlockedCount == totalCount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Items List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val unlockedBadgeText = when (currentLang) {
                    Language.RU -> "Открыто"
                    Language.UA -> "Відкрито"
                    Language.KK -> "Ашық"
                    Language.DE -> "Freigeschaltet"
                    Language.ZH -> "已解锁"
                    else -> "Unlocked"
                }
                val lockedBadgeText = when (currentLang) {
                    Language.RU -> "Закрыто"
                    Language.UA -> "Закрито"
                    Language.KK -> "Жабық"
                    Language.DE -> "Gesperrt"
                    Language.ZH -> "未解锁"
                    else -> "Locked"
                }
                val equippedBadgeText = when (currentLang) {
                    Language.RU -> "Надето"
                    Language.UA -> "Одягнено"
                    Language.KK -> "Киілген"
                    Language.DE -> "Aktiv"
                    Language.ZH -> "使用中"
                    else -> "Equipped"
                }

                items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (item.isEquipped) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        } else if (item.isUnlocked) {
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.25f)
                        },
                        border = if (item.isEquipped) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (item.isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (item.isUnlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (item.isEquipped) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (item.rarity != null) {
                                    Surface(
                                        color = item.rarity.color.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(5.dp),
                                        border = BorderStroke(0.8.dp, item.rarity.color.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = item.rarity.getLocalizedName(currentLang).uppercase(),
                                            color = item.rarity.color,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            if (item.isEquipped) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = equippedBadgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (item.isUnlocked) Color(0xFF2E7D32).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = if (item.isUnlocked) unlockedBadgeText else lockedBadgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 10.sp),
                                        color = if (item.isUnlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

