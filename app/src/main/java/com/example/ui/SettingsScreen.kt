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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit, onCustomizeControls: () -> Unit = {}, onAdmin: () -> Unit = {}) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val blockStyle by viewModel.blockStyle.collectAsStateWithLifecycle()
    val nextCount by viewModel.nextCount.collectAsStateWithLifecycle()
    val ghostVisible by viewModel.ghostVisible.collectAsStateWithLifecycle()
    val ghostOutlineOnly by viewModel.ghostOutlineOnly.collectAsStateWithLifecycle()
    val controlStyle by viewModel.controlStyle.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val smoothFallingEnabled by viewModel.smoothFallingEnabled.collectAsStateWithLifecycle()
    val gridOpacity by viewModel.gridOpacity.collectAsStateWithLifecycle()
    val customStartLevel by viewModel.customStartLevel.collectAsStateWithLifecycle()
    
    val lobbyMusicEnabled by viewModel.lobbyMusicEnabled.collectAsStateWithLifecycle()
    val soundVolume by viewModel.soundVolume.collectAsStateWithLifecycle()
    val lobbyMusicVolume by viewModel.lobbyMusicVolume.collectAsStateWithLifecycle()

    val screenShakeIntensity by viewModel.screenShakeIntensity.collectAsStateWithLifecycle()
    val scanlinesFilter by viewModel.scanlinesFilter.collectAsStateWithLifecycle()
    
    val lineClearChallenge by viewModel.lineClearChallenge.collectAsStateWithLifecycle()
    val autoSaveHighscore by viewModel.autoSaveHighscore.collectAsStateWithLifecycle()
    val fastDropLockSpeed by viewModel.fastDropLockSpeed.collectAsStateWithLifecycle()
    val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()
    val gameSpeedMultiplier by viewModel.gameSpeedMultiplier.collectAsStateWithLifecycle()
    val controlButtonScale by viewModel.controlButtonScale.collectAsStateWithLifecycle()
    val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()
    val gridLineDensity by viewModel.gridLineDensity.collectAsStateWithLifecycle()
    val customFontKey by viewModel.customFontKey.collectAsStateWithLifecycle()
    val controlVerticalPosition by viewModel.controlVerticalPosition.collectAsStateWithLifecycle()
    val controlDas by viewModel.controlDas.collectAsStateWithLifecycle()
    val controlArr by viewModel.controlArr.collectAsStateWithLifecycle()
    val controlBottomPadding by viewModel.controlBottomPadding.collectAsStateWithLifecycle()
    val newGameUiEnabled by viewModel.newGameUiEnabled.collectAsStateWithLifecycle()

    val purchasedThemes by viewModel.purchasedThemes.collectAsStateWithLifecycle()
    val purchasedFonts by viewModel.purchasedFonts.collectAsStateWithLifecycle()
    val purchasedControlButtonStyles by viewModel.purchasedControlButtonStyles.collectAsStateWithLifecycle()
    var activeCategory by remember { mutableStateOf(0) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    AdaptiveText(
                        text = Translations.get("settings", currentLang).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                },

                actions = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            showAboutDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About App",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val themeColorVal = MaterialTheme.colorScheme.primary

        val settingsContent = @Composable { category: Int ->
            when (category) {
                // ─────────────────────────────────────────────────────────────
                // 0: ВИЗУАЛ / ВНЕШНИЙ ВИД (VISUALS & THEMES)
                // ─────────────────────────────────────────────────────────────
                0 -> {
                    // Color Palette Swatches Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            AdaptiveText(
                                text = Translations.get("active_theme_accent", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val themes = listOf(
                                    "monet" to null,
                                    "indigo" to Color(0xFFD0BCFF),
                                    "black" to Color(0xFF1B1B1F),
                                    "red" to Color(0xFFFF5555),
                                    "neon" to Color(0xFF00FFCC),
                                    "emerald" to Color(0xFF10B981),
                                    "amber" to Color(0xFFF59E0B),
                                    "rose" to Color(0xFFF43F5E),
                                    "sky" to Color(0xFF0EA5E9),
                                    "orange" to Color(0xFFFF5722),
                                    "cyber_pink" to Color(0xFFFF007F),
                                    "toxic_green" to Color(0xFF39FF14),
                                    "gold" to Color(0xFFFFD700)
                                )
                                val monetGradient = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF8C52FF),
                                        Color(0xFF00E5FF),
                                        Color(0xFF00E676),
                                        Color(0xFFFFD600),
                                        Color(0xFFFF5252),
                                        Color(0xFF8C52FF)
                                    )
                                )
                                themes.chunked(5).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        rowThemes.forEach { (name, color) ->
                                            val selected = themeColor == name || (name == "monet" && themeColor == "dynamic")
                                            Box(
                                                modifier = Modifier.size(44.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val backgroundModifier = if (name == "monet") {
                                                    Modifier.background(monetGradient)
                                                } else {
                                                    Modifier.background(color ?: MaterialTheme.colorScheme.primary)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(RoundedCornerShape(50))
                                                        .then(backgroundModifier)
                                                        .border(
                                                            width = if (selected) 3.dp else (if (name == "black") 1.5.dp else 0.dp),
                                                            color = if (selected) MaterialTheme.colorScheme.primary else (if (name == "black") Color(0xFF555555) else Color.Transparent),
                                                            shape = RoundedCornerShape(50)
                                                        )
                                                        .clickable {
                                                            viewModel.triggerAudioFeedback("click")
                                                            viewModel.setThemeColor(name)
                                                        }
                                                ) {
                                                    if (selected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = if (name == "gold" || name == "neon" || name == "emerald") Color.Black else Color.White,
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .align(Alignment.Center)
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

                    // Block Architecture 3D Style Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = Translations.get("block_architecture_style", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val styles = listOf(
                                "neon" to Translations.getLocalizedCubeSkinTitle("neon", currentLang),
                                "glass" to Translations.getLocalizedCubeSkinTitle("glass", currentLang),
                                "material" to Translations.getLocalizedCubeSkinTitle("material", currentLang),
                                "flat" to Translations.getLocalizedCubeSkinTitle("flat", currentLang),
                                "steampunk" to Translations.getLocalizedCubeSkinTitle("steampunk", currentLang),
                                "glowing_jewel" to Translations.getLocalizedCubeSkinTitle("glowing_jewel", currentLang)
                            )
                            styles.forEach { (key, title) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.setBlockStyle(key)
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = blockStyle == key,
                                        onClick = {
                                            viewModel.setBlockStyle(key)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = themeColorVal)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AdaptiveText(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Graphics Quality Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            val gfxQualityTitle = when (currentLang) {
                                Language.RU -> "Качество графики"
                                Language.UA -> "Якість графіки"
                                Language.KK -> "Графика сапасы"
                                Language.DE -> "Grafikqualität"
                                Language.ZH -> "画面质量"
                                else -> "Graphics Quality"
                            }
                            Text(
                                text = gfxQualityTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            val graphicsQuality by viewModel.graphicsQuality.collectAsStateWithLifecycle()
                            val qualityLevels = listOf(
                                "low" to when (currentLang) {
                                    Language.RU -> "Низкая"
                                    Language.UA -> "Низька"
                                    Language.KK -> "Төмен"
                                    Language.DE -> "Niedrig"
                                    Language.ZH -> "低画质"
                                    else -> "Low"
                                },
                                "medium" to when (currentLang) {
                                    Language.RU -> "Средняя"
                                    Language.UA -> "Середня"
                                    Language.KK -> "Орташа"
                                    Language.DE -> "Mittel"
                                    Language.ZH -> "中等画质"
                                    else -> "Medium"
                                },
                                "high" to when (currentLang) {
                                    Language.RU -> "Высокая"
                                    Language.UA -> "Висока"
                                    Language.KK -> "Жоғары"
                                    Language.DE -> "Hoch"
                                    Language.ZH -> "高画质"
                                    else -> "High"
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                qualityLevels.forEach { (key, title) ->
                                    val isSelected = graphicsQuality == key
                                    FilledTonalButton(
                                        onClick = { viewModel.setGraphicsQuality(key) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (isSelected) themeColorVal else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Grid Line Density Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = Translations.get("grid_line_density", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val densities = listOf(
                                "classic" to when (currentLang) {
                                    Language.RU -> "Стандартная сетка"
                                    Language.UA -> "Стандартна сітка"
                                    Language.KK -> "Стандартты тор"
                                    Language.DE -> "Standard-Gitter"
                                    Language.ZH -> "标准网格线"
                                    else -> "Classic Grid Lines"
                                },
                                "dashed" to when (currentLang) {
                                    Language.RU -> "Пунктирный визуал"
                                    Language.UA -> "Пунктирний візуал"
                                    Language.KK -> "Үзік сызықты визуал"
                                    Language.DE -> "Gestrichelte Matrix"
                                    Language.ZH -> "虚线网格"
                                    else -> "Dashed Matrix Wireframe"
                                },
                                "none" to when (currentLang) {
                                    Language.RU -> "Без линий (Пространство)"
                                    Language.UA -> "Без ліній (Простір)"
                                    Language.KK -> "Сызықсыз (Кеңістік)"
                                    Language.DE -> "Ohne Linien (Vakuum)"
                                    Language.ZH -> "无网格线（纯净空间）"
                                    else -> "Void (No Lines)"
                                }
                            )
                            densities.forEach { (key, title) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.setGridLineDensity(key) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = gridLineDensity == key,
                                        onClick = { viewModel.setGridLineDensity(key) },
                                        colors = RadioButtonDefaults.colors(selectedColor = themeColorVal)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = title, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }

                    // Ghost Piece Visibility Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = Translations.get("show_ghost_target", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = ghostVisible,
                                    onCheckedChange = { viewModel.setGhostVisible(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    if (ghostVisible) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            val ghostOutlineTitle = when (currentLang) {
                                Language.RU -> "Только контур призрака"
                                Language.UA -> "Лише контур примари"
                                Language.KK -> "Тек елес сұлбасы"
                                Language.DE -> "Nur Geist-Umriss"
                                Language.ZH -> "仅显示虚影轮廓"
                                else -> "Ghost outline only"
                            }
                            val ghostOutlineDesc = when (currentLang) {
                                Language.RU -> "Чёткое очертание вместо текстур блоков"
                                Language.UA -> "Чіткий контур замість текстур блоків"
                                Language.KK -> "Блок текстураларының орнына айқын сұлба"
                                Language.DE -> "Klare Kontur statt Blocktextur"
                                Language.ZH -> "使用清晰轮廓替代方块纹理"
                                else -> "Clear outline without block textures"
                            }
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = ghostOutlineTitle,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = ghostOutlineDesc,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                trailingContent = {
                                    Switch(
                                        checked = ghostOutlineOnly,
                                        onCheckedChange = { viewModel.setGhostOutlineOnly(it) }
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }

                    // Screen Shake Intensity Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            val shakeTitle = when (currentLang) {
                                Language.RU -> "Интенсивность тряски экрана"
                                Language.UA -> "Інтенсивність тремтіння екрана"
                                Language.KK -> "Экран сілкінісінің қарқындылығы"
                                Language.DE -> "Bildschirm-Wackeln Intensität"
                                Language.ZH -> "屏幕震动强度"
                                else -> "Screen Shake Intensity"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shakeTitle,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format("%.2f", screenShakeIntensity),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = themeColorVal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Slider(
                                value = screenShakeIntensity,
                                onValueChange = { viewModel.setScreenShakeIntensity(it) },
                                valueRange = 0f..2f,
                                colors = SliderDefaults.colors(
                                    thumbColor = themeColorVal,
                                    activeTrackColor = themeColorVal
                                )
                            )
                        }
                    }

                    // CRT Scanlines Filter Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        val scanlinesTitle = when (currentLang) {
                            Language.RU -> "Эффект сканирования (CRT Scanlines)"
                            Language.UA -> "Ефект сканування (CRT Scanlines)"
                            Language.KK -> "Сканерлеу әсері (CRT Scanlines)"
                            Language.DE -> "CRT-Scanlines-Filter"
                            Language.ZH -> "复古CRT扫描线滤镜"
                            else -> "Retro CRT Scanlines Filter"
                        }
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = scanlinesTitle,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = scanlinesFilter,
                                    onCheckedChange = { viewModel.setScanlinesFilter(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 1: ГЕЙМПЛЕЙ И ФИЗИКА (GAMEPLAY & PHYSICS)
                // ─────────────────────────────────────────────────────────────
                1 -> {
                    // Starting Level Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = Translations.get("starting_level_selector", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val levels = listOf(1, 3, 5, 8, 10, 15)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                levels.chunked(3).forEach { levelRow ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        levelRow.forEach { lvl ->
                                            val isSelected = customStartLevel == lvl
                                            FilledTonalButton(
                                                onClick = { viewModel.setCustomStartLevel(lvl) },
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = if (isSelected) themeColorVal else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Text(
                                                    text = "${Translations.get("level", currentLang)} $lvl",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Game Speed Multiplier Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            AdaptiveText(
                                text = Translations.get("game_speed_multiplier", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val speedOptions = listOf(
                                0.75f to when (currentLang) {
                                    Language.RU -> "Легкая (0.75x)"
                                    Language.UA -> "Легка (0.75x)"
                                    Language.KK -> "Жеңіл (0.75x)"
                                    Language.DE -> "Leicht (0.75x)"
                                    Language.ZH -> "慢速 (0.75x)"
                                    else -> "Easy (0.75x)"
                                },
                                1.0f to when (currentLang) {
                                    Language.RU -> "Норма (1.0x)"
                                    Language.UA -> "Норма (1.0x)"
                                    Language.KK -> "Қалыпты (1.0x)"
                                    Language.DE -> "Normal (1.0x)"
                                    Language.ZH -> "标准 (1.0x)"
                                    else -> "Normal (1.0x)"
                                },
                                1.25f to when (currentLang) {
                                    Language.RU -> "Высокая (1.25x)"
                                    Language.UA -> "Висока (1.25x)"
                                    Language.KK -> "Жоғары (1.25x)"
                                    Language.DE -> "Schnell (1.25x)"
                                    Language.ZH -> "快速 (1.25x)"
                                    else -> "Hard (1.25x)"
                                },
                                1.5f to when (currentLang) {
                                    Language.RU -> "Экстрим (1.5x)"
                                    Language.UA -> "Екстрим (1.5x)"
                                    Language.KK -> "Экстрим (1.5x)"
                                    Language.DE -> "Extrem (1.5x)"
                                    Language.ZH -> "极速挑战 (1.5x)"
                                    else -> "Extreme (1.5x)"
                                }
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                speedOptions.chunked(2).forEach { speedRow ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        speedRow.forEach { (speedVal, label) ->
                                            val isSelected = Math.abs(gameSpeedMultiplier - speedVal) < 0.05f
                                            FilledTonalButton(
                                                onClick = { viewModel.setGameSpeedMultiplier(speedVal) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = if (isSelected) themeColorVal else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Next Pieces Preview Queue Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            val nextCountTitle = when (currentLang) {
                                Language.RU -> "Очередь следующих фигур"
                                Language.UA -> "Черга наступних фігур"
                                Language.KK -> "Келесі фигуралар кезегі"
                                Language.DE -> "Vorschau nächster Steine"
                                Language.ZH -> "下一方块预览数量"
                                else -> "Next Pieces Preview Queue"
                            }
                            AdaptiveText(
                                text = nextCountTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val nextOptions = listOf(1, 2, 3, 4, 5, 6)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                nextOptions.forEach { count ->
                                    val isSelected = nextCount == count
                                    FilledTonalButton(
                                        onClick = { viewModel.setNextCount(count) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (isSelected) themeColorVal else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Extra Smooth Falling Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = Translations.get("extra_smooth_falling", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = smoothFallingEnabled,
                                    onCheckedChange = { viewModel.setSmoothFallingEnabled(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // Fast Drop Lock Speed Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = Translations.get("fast_drop_lock_speed", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            supportingContent = {
                                AdaptiveText(
                                    text = Translations.get("fast_drop_lock_speed_desc", currentLang),
                                    maxLines = 3
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = fastDropLockSpeed,
                                    onCheckedChange = { viewModel.setFastDropLockSpeed(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // Line Clear Challenge Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = Translations.get("line_clear_challenge", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            supportingContent = {
                                AdaptiveText(
                                    text = Translations.get("line_clear_challenge_desc", currentLang),
                                    maxLines = 3
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = lineClearChallenge,
                                    onCheckedChange = { viewModel.setLineClearChallenge(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 2: УПРАВЛЕНИЕ (CONTROLS)
                // ─────────────────────────────────────────────────────────────
                2 -> {
                    // Customize Controls Layout Editor Navigation Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = onCustomizeControls
                    ) {
                        val customizeControlsTitle = when (currentLang) {
                            Language.RU -> "Кастомизация управления"
                            Language.UA -> "Кастомізація керування"
                            Language.KK -> "Басқаруды теңшеу"
                            Language.DE -> "Steuerung anpassen"
                            Language.ZH -> "自定义按键布局"
                            else -> "Customize Controls Layout"
                        }
                        val customizeControlsDesc = when (currentLang) {
                            Language.RU -> "Настроить размер, прозрачность, тип и позицию кнопок"
                            Language.UA -> "Налаштувати розмір, прозорість, тип та позицію кнопок"
                            Language.KK -> "Батырмалардың өлшемін, мөлдірлігін, түрін және орнын баптау"
                            Language.DE -> "Größe, Deckkraft, Stil und Position der Tasten konfigurieren"
                            Language.ZH -> "调整按键大小、透明度、样式和屏幕位置"
                            else -> "Configure scale, opacity, style and position of controls"
                        }
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = customizeControlsTitle,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = customizeControlsDesc
                                )
                            },
                            trailingContent = {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // 1. Control Buttons Layout Presets Card (7 Presets)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = themeColorVal,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Translations.get("control_buttons_layout", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColorVal
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val layouts = listOf(
                                "classic" to (Translations.getLocalizedControlPresetName("classic", currentLang) to when (currentLang) {
                                    Language.RU -> "4 кнопки в ряд + сброс/удержание снизу"
                                    Language.UA -> "4 кнопки в ряд + скидання/утримання знизу"
                                    Language.KK -> "Қатардағы 4 батырма"
                                    Language.DE -> "4 Tasten in einer Reihe + Drop/Hold"
                                    Language.ZH -> "4键排布 + 底部下落/暂存"
                                    else -> "4 inline buttons + drop/hold below"
                                }),
                                "split" to (Translations.getLocalizedControlPresetName("split", currentLang) to when (currentLang) {
                                    Language.RU -> "Для двух больших пальцев по краям"
                                    Language.UA -> "Для двох великих пальців по краях"
                                    Language.KK -> "Екі бас бармаққа арналған"
                                    Language.DE -> "Geteilte Zweihand-Steuerung"
                                    Language.ZH -> "双拇指边缘分列布局"
                                    else -> "Two-thumb ergonomics on edges"
                                }),
                                "arcade" to (Translations.getLocalizedControlPresetName("arcade", currentLang) to when (currentLang) {
                                    Language.RU -> "Крестовина D-Pad слева, действия справа"
                                    Language.UA -> "Хрестовина D-Pad зліва, дії справа"
                                    Language.KK -> "D-Pad сол жақта, әрекеттер оң жақта"
                                    Language.DE -> "Arcade D-Pad links, Aktionen rechts"
                                    Language.ZH -> "左侧D-Pad摇杆，右侧动作键"
                                    else -> "D-Pad left, action triggers right"
                                }),
                                "one_hand_right" to (Translations.getLocalizedControlPresetName("one_hand_right", currentLang) to when (currentLang) {
                                    Language.RU -> "Компактно справа для игры одной рукой"
                                    Language.UA -> "Компактно справа для гри однією рукою"
                                    Language.KK -> "Оң қолмен ойнауға арналған"
                                    Language.DE -> "Kompakt rechts für Einhand-Bedienung"
                                    Language.ZH -> "右手单手操作紧凑布局"
                                    else -> "Compact right cluster for 1-hand play"
                                }),
                                "one_hand_left" to (Translations.getLocalizedControlPresetName("one_hand_left", currentLang) to when (currentLang) {
                                    Language.RU -> "Компактно слева для игры одной рукой"
                                    Language.UA -> "Компактно зліва для гри однією рукою"
                                    Language.KK -> "Сол қолмен ойнауға арналған"
                                    Language.DE -> "Kompakt links für Einhand-Bedienung"
                                    Language.ZH -> "左手单手操作紧凑布局"
                                    else -> "Compact left cluster for 1-hand play"
                                }),
                                "claw_pro" to (Translations.getLocalizedControlPresetName("claw_pro", currentLang) to when (currentLang) {
                                    Language.RU -> "Киберспортивная матрица из 6 клавиш"
                                    Language.UA -> "Кіберспортивна матриця з 6 клавіш"
                                    Language.KK -> "Киберспорттық 6 батырма"
                                    Language.DE -> "6-Tasten Pro Matrix"
                                    Language.ZH -> "电竞6键独立控制矩阵"
                                    else -> "6-key competitive matrix"
                                })
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                layouts.forEach { (key, info) ->
                                    val (title, desc) = info
                                    val isSelected = controlStyle == key
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setControlStyle(key)
                                                viewModel.triggerAudioFeedback("click")
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.setControlStyle(key)
                                                    viewModel.triggerAudioFeedback("click")
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = themeColorVal)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. DAS & ARR Timing Tuning Card (Тонкая настройка отклика)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "ЧУВСТВИТЕЛЬНОСТЬ И ОТКЛИК (DAS / ARR)"
                                    Language.UA -> "ЧУТЛИВІСТЬ ТА ВІДГУК (DAS / ARR)"
                                    Language.KK -> "СЕЗІМТАЛДЫҚ ПЕН ЖЫЛДАМДЫҚ"
                                    Language.DE -> "ANSPRECHVERHALTEN (DAS / ARR)"
                                    Language.ZH -> "按键响应灵敏度 (DAS / ARR)"
                                    else -> "RESPONSE & TIMINGS (DAS / ARR)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )

                            // DAS Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "DAS (Задержка зажатия)"
                                                Language.UA -> "DAS (Затримка затискання)"
                                                Language.KK -> "DAS (Басу кідірісі)"
                                                Language.DE -> "DAS (Startverzögerung)"
                                                Language.ZH -> "DAS (长按触发延迟)"
                                                else -> "DAS (Initial Hold Delay)"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Время до начала авто-повтора"
                                                Language.UA -> "Час до початку авто-повтору"
                                                Language.KK -> "Авто-қайталауға дейінгі уақыт"
                                                Language.DE -> "Zeit bis Dauerfeuer startet"
                                                Language.ZH -> "按住到开始连续移动的时间"
                                                else -> "Delay before continuous repeat"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = themeColorVal.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$controlDas мс",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                }
                                Slider(
                                    value = controlDas.toFloat(),
                                    onValueChange = { viewModel.setControlDas(it.toInt()) },
                                    valueRange = 100f..300f
                                )
                            }

                            // ARR Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "ARR (Скорость повтора)"
                                                Language.UA -> "ARR (Швидкість повтору)"
                                                Language.KK -> "ARR (Қайталау жылдамдығы)"
                                                Language.DE -> "ARR (Wiederholrate)"
                                                Language.ZH -> "ARR (连按重复频率)"
                                                else -> "ARR (Auto-Repeat Rate)"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Интервал между сдвигами при зажатии"
                                                Language.UA -> "Інтервал між зсувами при затисканні"
                                                Language.KK -> "Қайталау арасындағы уақыт"
                                                Language.DE -> "Intervall für Dauerfeuer-Schritte"
                                                Language.ZH -> "连续连击移动的间隔"
                                                else -> "Interval between repeat shifts"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = themeColorVal.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$controlArr мс",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                }
                                Slider(
                                    value = controlArr.toFloat(),
                                    onValueChange = { viewModel.setControlArr(it.toInt()) },
                                    valueRange = 16f..80f
                                )
                            }
                        }
                    }

                    // 3. Ergonomics & Geometry Tuning Card (Геометрия и размеры)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "ГЕОМЕТРИЯ И РАЗМЕР КНОПОК"
                                    Language.UA -> "ГЕОМЕТРІЯ ТА РОЗМІР КНОПОК"
                                    Language.KK -> "БАТЫРМАЛАР ӨЛШЕМІ МЕН ОРНЫ"
                                    Language.DE -> "GEOMETRIE & TASTENGRÖSSE"
                                    Language.ZH -> "按键尺寸与底边距调整"
                                    else -> "GEOMETRY & BUTTON SIZING"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )

                            // Bottom Margin Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Отступ снизу"
                                                Language.UA -> "Відступ знизу"
                                                Language.KK -> "Төменнен шегініс"
                                                Language.DE -> "Unterer Randabstand"
                                                Language.ZH -> "底部防误触边距"
                                                else -> "Bottom Screen Margin"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Защита от системных жестов"
                                                Language.UA -> "Захист від системних жестів"
                                                Language.KK -> "Жүйелік қимылдардан қорғау"
                                                Language.DE -> "Schutz vor Gestenleiste"
                                                Language.ZH -> "防止被手势栏误触"
                                                else -> "Prevents accidental gestures"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${controlBottomPadding} dp",
                                        fontWeight = FontWeight.Bold,
                                        color = themeColorVal
                                    )
                                }
                                Slider(
                                    value = controlBottomPadding.toFloat(),
                                    onValueChange = { viewModel.setControlBottomPadding(it.toInt()) },
                                    valueRange = 0f..60f,
                                    colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                )
                            }

                            // Button Scale Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = Translations.get("control_button_scale", currentLang),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Масштаб элементов управления"
                                                Language.UA -> "Масштаб елементів керування"
                                                Language.KK -> "Батырмалар масштабы"
                                                Language.DE -> "Tastengröße skalieren"
                                                Language.ZH -> "调整所有按键物理大小"
                                                else -> "Scale overall button touch area"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = themeColorVal.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${"%.2f".format(controlButtonScale)}x",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                }
                                Slider(
                                    value = controlButtonScale,
                                    onValueChange = { viewModel.setControlButtonScale(it) },
                                    valueRange = 0.70f..1.40f
                                )
                            }

                            // Quick Reset to Recommended
                            FilledTonalButton(
                                onClick = {
                                    viewModel.setControlDas(160)
                                    viewModel.setControlArr(35)
                                    viewModel.setControlBottomPadding(12)
                                    viewModel.setControlButtonScale(1.0f)
                                    viewModel.triggerAudioFeedback("click")
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Сброс к оптимальным параметрам"
                                        Language.UA -> "Скинути до оптимальних значень"
                                        Language.KK -> "Оңтайлы мәндерге қайтару"
                                        Language.DE -> "Auf Standardwerte zurücksetzen"
                                        Language.ZH -> "恢复推荐默认参数"
                                        else -> "Reset to Recommended Values"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 4. Control Button Visual Style Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = Translations.get("control_button_style", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val btnStyles = listOf(
                                "neon" to Translations.getLocalizedButtonTitle("neon", currentLang),
                                "glass" to Translations.getLocalizedButtonTitle("glass", currentLang),
                                "classic" to Translations.getLocalizedButtonTitle("classic", currentLang)
                            )
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val purchaseBtnStyleToast = when (currentLang) {
                                Language.RU -> "Купите этот стиль кнопок в магазине!"
                                Language.UA -> "Придбайте цей стиль кнопок у магазині!"
                                Language.KK -> "Бұл батырма стилін дүкеннен сатып алыңыз!"
                                Language.DE -> "Kaufe diesen Tastenstil im Shop!"
                                Language.ZH -> "请在商店中购买此按键风格！"
                                else -> "Purchase this button style in the store!"
                            }
                            btnStyles.forEach { (key, title) ->
                                val isOwned = key == "classic" || key == "neon" || purchasedControlButtonStyles.contains(key)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (isOwned) {
                                                viewModel.setControlButtonStyle(key)
                                            } else {
                                                viewModel.triggerAudioFeedback("error")
                                                android.widget.Toast.makeText(
                                                    context,
                                                    purchaseBtnStyleToast,
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = controlButtonStyle == key,
                                        onClick = {
                                            if (isOwned) {
                                                viewModel.setControlButtonStyle(key)
                                            } else {
                                                viewModel.triggerAudioFeedback("error")
                                                android.widget.Toast.makeText(
                                                    context,
                                                    purchaseBtnStyleToast,
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        enabled = isOwned,
                                        colors = RadioButtonDefaults.colors(selectedColor = themeColorVal)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AdaptiveText(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isOwned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    if (!isOwned) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Left Handed Controls Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = Translations.get("left_handed_controls", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = leftHandedControls,
                                    onCheckedChange = { viewModel.setLeftHandedControls(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // 6. Interactive Live Test Pad Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "ТЕСТИРОВАНИЕ КНОПОК"
                                    Language.UA -> "ТЕСТУВАННЯ КНОПОК"
                                    Language.KK -> "БАТЫРМАЛАРДЫ ТЕКСЕРУ"
                                    Language.DE -> "STEUERUNG TESTEN"
                                    Language.ZH -> "实时按键手感测试区"
                                    else -> "LIVE CONTROLS TEST"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Проверьте отклик, размер и удобство расположения прямо здесь:"
                                    Language.UA -> "Перевірте відгук та розташування прямо тут:"
                                    Language.KK -> "Батырмалардың өлшемі мен орналасуын осында тексеріңіз:"
                                    Language.DE -> "Tastenreaktion und Position hier direkt testen:"
                                    Language.ZH -> "直接在此区域按下并测试手感与按键延迟："
                                    else -> "Test timings, button size, and layout position directly here:"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GameControlsSection(
                                        viewModel = viewModel,
                                        gameState = com.example.game.GameState(),
                                        controlStyle = controlStyle,
                                        leftHandedControls = leftHandedControls,
                                        controlVerticalPosition = "bottom",
                                        controlButtonScale = controlButtonScale,
                                        controlButtonStyle = controlButtonStyle,
                                        onLeftPress = { viewModel.triggerAudioFeedback("move") },
                                        onRightPress = { viewModel.triggerAudioFeedback("move") },
                                        onDownPress = { viewModel.triggerAudioFeedback("move") },
                                        onRotatePress = { viewModel.triggerAudioFeedback("rotate") },
                                        onHardDropPress = { viewModel.triggerAudioFeedback("drop") },
                                        onHoldPress = { viewModel.triggerAudioFeedback("click") }
                                    )
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 3: ЗВУК И ВИБРАЦИЯ (AUDIO & HAPTICS)
                // ─────────────────────────────────────────────────────────────
                3 -> {
                    // Sound Effects Toggle Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = Translations.get("sound_effects", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = soundEnabled,
                                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // Sound Effects Volume Slider Card
                    if (soundEnabled) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val soundVolTitle = when (currentLang) {
                                    Language.RU -> "Громкость звуковых эффектов"
                                    Language.UA -> "Гучність звукових ефектів"
                                    Language.KK -> "Дыбыс әсерлерінің дауысы"
                                    Language.DE -> "Lautstärke der Soundeffekte"
                                    Language.ZH -> "音效音量"
                                    else -> "Sound Effects Volume"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        AdaptiveText(
                                            text = soundVolTitle,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        AdaptiveText(
                                            text = "${(soundVolume * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = themeColorVal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = soundVolume,
                                    onValueChange = { viewModel.setSoundVolume(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColorVal,
                                        activeTrackColor = themeColorVal
                                    )
                                )
                            }
                        }
                    }

                    // Lobby Music Toggle Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        val lobbyMusicTitle = when (currentLang) {
                            Language.RU -> "Фоновая музыка в лобби"
                            Language.UA -> "Фонова музика в лобі"
                            Language.KK -> "Лоббидегі фондық музыка"
                            Language.DE -> "Hintergrundmusik in Lobby"
                            Language.ZH -> "大厅背景音乐"
                            else -> "Lobby Background Music"
                        }
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = lobbyMusicTitle,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = lobbyMusicEnabled,
                                    onCheckedChange = { viewModel.setLobbyMusicEnabled(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // Lobby Music Volume Slider Card
                    if (lobbyMusicEnabled) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val lobbyMusicVolTitle = when (currentLang) {
                                    Language.RU -> "Громкость музыки"
                                    Language.UA -> "Гучність музики"
                                    Language.KK -> "Музыка дауысы"
                                    Language.DE -> "Musiklautstärke"
                                    Language.ZH -> "音乐音量"
                                    else -> "Lobby Music Volume"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        AdaptiveText(
                                            text = lobbyMusicVolTitle,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        AdaptiveText(
                                            text = "${(lobbyMusicVolume * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = themeColorVal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = lobbyMusicVolume,
                                    onValueChange = { viewModel.setLobbyMusicVolume(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColorVal,
                                        activeTrackColor = themeColorVal
                                    )
                                )
                            }
                        }
                    }

                    // Vibration / Haptics Toggle Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = Translations.get("vibration", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = vibrationEnabled,
                                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 4: ЯЗЫК И СИСТЕМА (LANGUAGE & SYSTEM)
                // ─────────────────────────────────────────────────────────────
                4 -> {
                    // New Game Interface Mode Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        val newUiTitle = when (currentLang) {
                            Language.RU -> "Новый интерфейс игры"
                            Language.UA -> "Новий інтерфейс гри"
                            Language.KK -> "Ойынның жаңа интерфейсі"
                            Language.DE -> "Neues Spiel-Interface"
                            Language.ZH -> "全新游戏界面"
                            else -> "Modern Game Interface"
                        }
                        ListItem(
                            headlineContent = {
                                AdaptiveText(
                                    text = newUiTitle,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = newGameUiEnabled,
                                    onCheckedChange = {
                                        viewModel.triggerAudioFeedback("click")
                                        viewModel.setNewGameUiEnabled(it)
                                    }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    // Language Selection Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            AdaptiveText(
                                text = Translations.get("change_language", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Language.values().toList().chunked(3).forEach { langRow ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        langRow.forEach { lang ->
                                            val selected = currentLang == lang
                                            FilledTonalButton(
                                                onClick = {
                                                    viewModel.triggerAudioFeedback("click")
                                                    viewModel.setLanguage(lang)
                                                },
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = if (selected) themeColorVal else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Text(
                                                    text = lang.displayName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reset All Settings Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val resetAllTitle = when (currentLang) {
                                Language.RU -> "СБРОСИТЬ ВСЕ НАСТРОЙКИ"
                                Language.UA -> "СКИДАННЯ ВСІХ НАЛАШТУВАНЬ"
                                Language.KK -> "БАРЛЫҚ БАПТАУЛАРДЫ ҚАЛПЫНА КЕЛТІРУ"
                                Language.DE -> "ALLE EINSTELLUNGEN ZURÜCKSETZEN"
                                Language.ZH -> "重置所有设置"
                                else -> "RESET ALL SETTINGS"
                            }
                            val resetAllDesc = when (currentLang) {
                                Language.RU -> "Восстановит все параметры приложения по умолчанию"
                                Language.UA -> "Відновить усі параметри програми за замовчуванням"
                                Language.KK -> "Қолданбаның барлық параметрлерін әдепкі күйіне қайтарады"
                                Language.DE -> "Stellt alle Einstellungen auf Werkseinstellungen zurück"
                                Language.ZH -> "将应用和游戏的所有设置恢复为出厂默认值"
                                else -> "Restores all game configurations to factory defaults"
                            }
                            val resetBtnLabel = when (currentLang) {
                                Language.RU -> "СБРОСИТЬ"
                                Language.UA -> "СКИНУТИ"
                                Language.KK -> "ҚАЛПЫНА КЕЛТІРУ"
                                Language.DE -> "ZURÜCKSETZEN"
                                Language.ZH -> "立即重置"
                                else -> "RESET DEFAULTS"
                            }
                            AdaptiveText(
                                text = resetAllTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AdaptiveText(
                                text = resetAllDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.triggerAudioFeedback("success")
                                    viewModel.resetSettingsToDefault()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = resetBtnLabel,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val isWideScreen = maxWidth >= 600.dp
            data class CategoryMeta(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

            val categories = when (currentLang) {
                Language.RU -> listOf(
                    CategoryMeta("ВИЗУАЛ", Icons.Default.Palette),
                    CategoryMeta("ГЕЙМПЛЕЙ", Icons.Default.SportsEsports),
                    CategoryMeta("УПРАВЛЕНИЕ", Icons.Default.Gamepad),
                    CategoryMeta("ЗВУК", Icons.Default.VolumeUp),
                    CategoryMeta("СИСТЕМА", Icons.Default.Language)
                )
                Language.UA -> listOf(
                    CategoryMeta("ВІЗУАЛ", Icons.Default.Palette),
                    CategoryMeta("ГЕЙМПЛЕЙ", Icons.Default.SportsEsports),
                    CategoryMeta("КЕРУВАННЯ", Icons.Default.Gamepad),
                    CategoryMeta("ЗВУК", Icons.Default.VolumeUp),
                    CategoryMeta("СИСТЕМА", Icons.Default.Language)
                )
                Language.KK -> listOf(
                    CategoryMeta("ВИЗУАЛ", Icons.Default.Palette),
                    CategoryMeta("ГЕЙМПЛЕЙ", Icons.Default.SportsEsports),
                    CategoryMeta("БАСҚАРУ", Icons.Default.Gamepad),
                    CategoryMeta("ДЫБЫС", Icons.Default.VolumeUp),
                    CategoryMeta("ЖҮЙЕ", Icons.Default.Language)
                )
                Language.DE -> listOf(
                    CategoryMeta("VISUELL", Icons.Default.Palette),
                    CategoryMeta("GAMEPLAY", Icons.Default.SportsEsports),
                    CategoryMeta("STEUERUNG", Icons.Default.Gamepad),
                    CategoryMeta("AUDIO", Icons.Default.VolumeUp),
                    CategoryMeta("SYSTEM", Icons.Default.Language)
                )
                Language.ZH -> listOf(
                    CategoryMeta("画面视觉", Icons.Default.Palette),
                    CategoryMeta("战术玩法", Icons.Default.SportsEsports),
                    CategoryMeta("操作控制", Icons.Default.Gamepad),
                    CategoryMeta("音频震动", Icons.Default.VolumeUp),
                    CategoryMeta("系统设置", Icons.Default.Language)
                )
                else -> listOf(
                    CategoryMeta("VISUALS", Icons.Default.Palette),
                    CategoryMeta("GAMEPLAY", Icons.Default.SportsEsports),
                    CategoryMeta("CONTROLS", Icons.Default.Gamepad),
                    CategoryMeta("AUDIO", Icons.Default.VolumeUp),
                    CategoryMeta("SYSTEM", Icons.Default.Language)
                )
            }

            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .width(220.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEachIndexed { index, cat ->
                            val selected = activeCategory == index
                            NavigationDrawerItem(
                                icon = { Icon(cat.icon, contentDescription = cat.title) },
                                label = {
                                    AdaptiveText(
                                        text = cat.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    activeCategory = index
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedContainerColor = Color.Transparent,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        settingsContent(activeCategory)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = activeCategory,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 0.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        categories.forEachIndexed { index, cat ->
                            Tab(
                                selected = activeCategory == index,
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    activeCategory = index
                                },
                                icon = { Icon(cat.icon, contentDescription = cat.title, modifier = Modifier.size(18.dp)) },
                                text = {
                                    AdaptiveText(
                                        text = cat.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        settingsContent(activeCategory)
                    }
                }
            }
        }

        if (showAboutDialog) {
            AboutAppDialog(
                currentLang = currentLang,
                onDismiss = { showAboutDialog = false }
            )
        }
    }
}

/**
 * Resolves the application version dynamically from Android PackageManager API with fallback to BuildConfig.
 */
@Composable
fun rememberAppVersionName(): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(context) {
        try {
            val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName?.ifBlank { com.example.BuildConfig.VERSION_NAME } ?: com.example.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            com.example.BuildConfig.VERSION_NAME
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppDialog(
    currentLang: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appVersionName = rememberAppVersionName()
    val appIcon = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            val w = drawable.intrinsicWidth.coerceAtLeast(1)
            val h = drawable.intrinsicHeight.coerceAtLeast(1)
            val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    var selectedAboutTab by remember { mutableIntStateOf(0) }
    val themeColor = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val aboutAppTitle = when (currentLang) {
                            Language.RU -> "О ПРИЛОЖЕНИИ"
                            Language.UA -> "ПРО ДОДАТОК"
                            Language.KK -> "ҚОЛДАНБА ТУРАЛЫ"
                            Language.DE -> "ÜBER DIE APP"
                            Language.ZH -> "关于应用"
                            else -> "ABOUT APP"
                        }
                        Text(
                            text = aboutAppTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Segmented Tab Selector
                val tabAbout = when (currentLang) {
                    Language.RU -> "Проект"
                    Language.UA -> "Проєкт"
                    Language.KK -> "Жоба"
                    Language.DE -> "Projekt"
                    Language.ZH -> "项目概况"
                    else -> "About"
                }
                val tabSponsors = when (currentLang) {
                    Language.RU -> "Спонсоры"
                    Language.UA -> "Спонсори"
                    Language.KK -> "Демеушілер"
                    Language.DE -> "Sponsoren"
                    Language.ZH -> "赞助鸣谢"
                    else -> "Sponsors"
                }
                val tabTeam = when (currentLang) {
                    Language.RU -> "Команда"
                    Language.UA -> "Команда"
                    Language.KK -> "Команда"
                    Language.DE -> "Team"
                    Language.ZH -> "制作团队"
                    else -> "Team"
                }
                val tabs = listOf(
                    Triple(0, tabAbout, Icons.Default.Info),
                    Triple(1, tabSponsors, Icons.Default.Star),
                    Triple(2, tabTeam, Icons.Default.People)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabWidth = maxWidth / tabs.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * selectedAboutTab,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                            label = "aboutTabIndicator"
                        )

                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(40.dp)
                                .offset(x = indicatorOffset)
                                .clip(RoundedCornerShape(16.dp))
                                .background(themeColor)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            tabs.forEach { (index, title, icon) ->
                                val isSelected = selectedAboutTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedAboutTab = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (selectedAboutTab) {
                        0 -> {
                            // ABOUT PROJECT TAB
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier.size(88.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (appIcon != null) {
                                            Image(
                                                bitmap = appIcon,
                                                contentDescription = "Tetris Icon",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(24.dp))
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = com.example.R.mipmap.ic_launcher),
                                                contentDescription = "Tetris Icon",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(24.dp))
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "FlowTess",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = appVersionName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val aboutProjectHeader = when (currentLang) {
                                            Language.RU -> "О ПРОЕКТЕ"
                                            Language.UA -> "ПРО ПРОЄКТ"
                                            Language.KK -> "ЖОБА ТУРАЛЫ"
                                            Language.DE -> "ÜBER DAS PROJEKT"
                                            Language.ZH -> "关于项目"
                                            else -> "ABOUT PROJECT"
                                        }
                                        val aboutProjectDesc = when (currentLang) {
                                            Language.RU -> "Современная кросс-режимная реализация легендарной классики с онлайн-мультиплеером, кейсами, богатой кастомизацией и системой престижа."
                                            Language.UA -> "Сучасна крос-режимна реалізація легендарної класики з онлайн-мультиплеєром, кейсами, багатою кастомізацією та системою престижу."
                                            Language.KK -> "Онлайн-мультиплеері, кейстері, бай кастомизациясы және бедел жүйесі бар аңызға айналған классиканың заманауи кросс-режимдік нұсқасы."
                                            Language.DE -> "Moderne Multi-Modus-Implementierung des legendären Klassikers mit Echtzeit-Mehrspieler, Beutekisten, tiefgreifender Anpassung und Prestige-Fortschritt."
                                            Language.ZH -> "传奇经典方块游戏的现代化多模式实现，包含实时多人对战、战利品箱、深度个性化定制及荣誉威望系统。"
                                            else -> "Modern multi-mode implementation of the legendary classic featuring real-time multiplayer, loot crates, deep cosmetics customization, and prestige progression."
                                        }
                                        Text(
                                            text = aboutProjectHeader,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = aboutProjectDesc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        1 -> {
                            // SPONSORS TAB
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Main Sponsor Card: ˖✧˚ʚᴅɪᴀɴᴀɞ˚✧˖
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                BorderStroke(
                                                    2.dp,
                                                    Brush.horizontalGradient(
                                                        listOf(
                                                            Color(0xFFFF4081),
                                                            Color(0xFFE040FB),
                                                            Color(0xFFFF80AB)
                                                        )
                                                    )
                                                ),
                                                RoundedCornerShape(24.dp)
                                            )
                                            .padding(18.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFF4081).copy(alpha = 0.15f),
                                                border = BorderStroke(1.5.dp, Color(0xFFFF4081)),
                                                modifier = Modifier.size(54.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF4081),
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "˖✧˚ʚᴅɪᴀɴᴀɞ˚✧˖",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFF4081)
                                            )

                                            val mainSponsorLabel = when (currentLang) {
                                                Language.RU -> "ГЕНЕРАЛЬНЫЙ СПОНСОР"
                                                Language.UA -> "ГЕНЕРАЛЬНИЙ СПОНСОР"
                                                Language.KK -> "БАС ДЕМЕУШІ"
                                                Language.DE -> "HAUPTSPONSOR"
                                                Language.ZH -> "首席赞助者"
                                                else -> "MAIN SPONSOR"
                                            }
                                            val mainSponsorDesc = when (currentLang) {
                                                Language.RU -> "Особая благодарность и признательность за неоценимую поддержку развития проекта!"
                                                Language.UA -> "Особлива подяка та вдячність за неоціненну підтримку розвитку проєкту!"
                                                Language.KK -> "Жобаның дамуына баға жетпес қолдау көрсеткені үшін ерекше алғыс пен ризашылық!"
                                                Language.DE -> "Besonderer Dank und herzliche Anerkennung für die unschätzbare Unterstützung dieses Projekts!"
                                                Language.ZH -> "特别感谢对本项目开发与持续改进做出的无价支持与贡献！"
                                                else -> "Special gratitude and heartfelt thanks for invaluable support of the project!"
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFF4081).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = mainSponsorLabel,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFFF4081),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }

                                            Text(
                                                text = mainSponsorDesc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        2 -> {
                            // TEAM & QA TAB
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Author Card
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "FsFq",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val authorDevLabel = when (currentLang) {
                                            Language.RU -> "АВТОР И РАЗРАБОТЧИК"
                                            Language.UA -> "АВТОР ТА РОЗРОБНИК"
                                            Language.KK -> "АВТОР ЖӘНЕ ӘЗІРЛЕУШІ"
                                            Language.DE -> "AUTOR & ENTWICKLER"
                                            Language.ZH -> "作者与主开发者"
                                            else -> "AUTHOR & DEVELOPER"
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = authorDevLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // QA Testers Card
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val testersHeader = when (currentLang) {
                                            Language.RU -> "ТЕСТИРОВЩИКИ"
                                            Language.UA -> "ТЕСТУВАЛЬНИКИ"
                                            Language.KK -> "ТЕСТЕРЛЕР"
                                            Language.DE -> "TESTER / QA"
                                            Language.ZH -> "测试人员 / QA"
                                            else -> "TESTERS / QA"
                                        }
                                        Text(
                                            text = testersHeader,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            listOf("хоббит_плей", "scorp1ck").forEach { testerName ->
                                                Surface(
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(
                                                            text = testerName,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = "QA Tester",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

