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
fun LeaderboardScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val topScores by viewModel.topScores.collectAsStateWithLifecycle(initialValue = emptyList())
    val globalScores by viewModel.globalScores.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val themeColor = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current
    
    var selectedTab by remember { mutableStateOf(0) }
    var selectedModeCategory by remember { mutableStateOf("overall") }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun triggerRefresh() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.triggerAudioFeedback("click")
        isRefreshing = true
        if (selectedModeCategory == "overall") {
            viewModel.fetchGlobalLeaderboard()
        } else {
            viewModel.fetchGlobalLeaderboardByMode(selectedModeCategory)
        }
        coroutineScope.launch {
            kotlinx.coroutines.delay(800)
            isRefreshing = false
        }
    }

    LaunchedEffect(selectedTab, selectedModeCategory) {
        if (selectedTab == 1) {
            if (selectedModeCategory == "overall") {
                viewModel.fetchGlobalLeaderboard()
            } else {
                viewModel.fetchGlobalLeaderboardByMode(selectedModeCategory)
            }
        }
    }

    val displayScores = if (selectedTab == 0) topScores else globalScores

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    val leaderboardTitle = when (currentLang) {
                        Language.RU -> "ТАБЛИЦА РЕКОРДОВ"
                        Language.UA -> "ТАБЛИЦЯ РЕКОРДІВ"
                        Language.KK -> "РЕКОРДТАР КЕСТЕСІ"
                        Language.DE -> "BESTENLISTE"
                        Language.ZH -> "排行榜"
                        else -> "LEADERBOARD"
                    }
                    Text(
                        text = leaderboardTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { triggerRefresh() }
                    ) {
                        val rotationAnim by animateFloatAsState(
                            targetValue = if (isRefreshing) 360f else 0f,
                            animationSpec = tween(700, easing = LinearOutSlowInEasing),
                            label = "RefreshRotation"
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = themeColor,
                            modifier = Modifier.graphicsLayer { rotationZ = rotationAnim }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // MD3 Segmented Tab Switcher with animated sliding pill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val tabWidth = maxWidth / 2
                    val indicatorOffset by animateDpAsState(
                        targetValue = tabWidth * selectedTab,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                        label = "leaderboardTabIndicator"
                    )

                    Box(
                        modifier = Modifier
                            .width(tabWidth)
                            .height(44.dp)
                            .offset(x = indicatorOffset)
                            .clip(RoundedCornerShape(18.dp))
                            .background(themeColor)
                    )

                    val localTabLabel = when (currentLang) {
                        Language.RU -> "Локальные"
                        Language.UA -> "Локальні"
                        Language.KK -> "Жергілікті"
                        Language.DE -> "Lokal"
                        Language.ZH -> "本地记录"
                        else -> "Local"
                    }
                    val globalTabLabel = when (currentLang) {
                        Language.RU -> "Мировые"
                        Language.UA -> "Світові"
                        Language.KK -> "Әлемдік"
                        Language.DE -> "Global"
                        Language.ZH -> "全球对决"
                        else -> "Global Arena"
                    }
                    val tabs = listOf(
                        Triple(0, localTabLabel, Icons.Default.PhoneAndroid),
                        Triple(1, globalTabLabel, Icons.Default.Public)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        tabs.forEach { (index, label, icon) ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        viewModel.triggerAudioFeedback("click")
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedTab = index
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> (fullWidth * 0.35f * direction).toInt() },
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(tween(220))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> (fullWidth * 0.35f * -direction).toInt() },
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut(tween(180))
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                label = "LeaderboardContentAnim"
            ) { currentTab ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mode Filter Chips for Global Tab
                    if (currentTab == 1) {
                        val categories = remember(currentLang) {
                            listOf(
                                "overall" to when (currentLang) {
                                    Language.RU -> "Все режимы"
                                    Language.UA -> "Всі режими"
                                    Language.KK -> "Барлық режимдер"
                                    Language.DE -> "Alle Modi"
                                    Language.ZH -> "所有模式"
                                    else -> "All Modes"
                                },
                                "classic" to when (currentLang) {
                                    Language.RU -> "Классический"
                                    Language.UA -> "Класичний"
                                    Language.KK -> "Классикалық"
                                    Language.DE -> "Klassisch"
                                    Language.ZH -> "经典模式"
                                    else -> "Classic"
                                },
                                "extended" to when (currentLang) {
                                    Language.RU -> "Расширенный"
                                    Language.UA -> "Розширений"
                                    Language.KK -> "Кеңейтілген"
                                    Language.DE -> "Erweitert"
                                    Language.ZH -> "扩展模式"
                                    else -> "Extended"
                                },
                                "fast_run" to when (currentLang) {
                                    Language.RU -> "Гипер-Режим"
                                    Language.UA -> "Гіпер-Режим"
                                    Language.KK -> "Гипер-Режим"
                                    Language.DE -> "Hyper-Modus"
                                    Language.ZH -> "极限极速"
                                    else -> "Hyper Rush"
                                },
                                "reverse" to when (currentLang) {
                                    Language.RU -> "Хаос"
                                    Language.UA -> "Хаос"
                                    Language.KK -> "Хаос"
                                    Language.DE -> "Chaos"
                                    Language.ZH -> "混乱模式"
                                    else -> "Chaos"
                                },
                                "block_blast" to "ZETA",
                                "zen" to when (currentLang) {
                                    Language.RU -> "Дзен"
                                    Language.UA -> "Дзен"
                                    Language.KK -> "Дзен"
                                    Language.DE -> "Zen"
                                    Language.ZH -> "禅模式"
                                    else -> "Zen"
                                },
                                "time_attack" to when (currentLang) {
                                    Language.RU -> "Тайм-Атак"
                                    Language.UA -> "Тайм-Атак"
                                    Language.KK -> "Тайм-Атак"
                                    Language.DE -> "Zeitangriff"
                                    Language.ZH -> "限时挑战"
                                    else -> "Time Attack"
                                },
                                "pulse_extreme" to when (currentLang) {
                                    Language.RU -> "Прилив"
                                    Language.UA -> "Приплив"
                                    Language.KK -> "Толысу"
                                    Language.DE -> "Flut"
                                    Language.ZH -> "潮汐"
                                    else -> "Tide"
                                },
                                "mirror" to when (currentLang) {
                                    Language.RU -> "Зеркальный"
                                    Language.UA -> "Дзеркальний"
                                    Language.KK -> "Айналық"
                                    Language.DE -> "Spiegelmodus"
                                    Language.ZH -> "镜像模式"
                                    else -> "Mirror"
                                },
                                "penta" to when (currentLang) {
                                    Language.RU -> "Пента-Хаос"
                                    Language.UA -> "Пента-Хаос"
                                    Language.KK -> "Пента-Хаос"
                                    Language.DE -> "Pentatris"
                                    Language.ZH -> "五阶狂潮"
                                    else -> "Pentatris"
                                },
                                "perfectionist" to when (currentLang) {
                                    Language.RU -> "Перфекционист"
                                    Language.UA -> "Перфекціоніст"
                                    Language.KK -> "Перфекционист"
                                    Language.DE -> "Perfektionist"
                                    Language.ZH -> "完美主义者"
                                    else -> "Perfectionist"
                                }
                            )
                        }

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(categories) { (key, label) ->
                                val isSelected = selectedModeCategory == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedModeCategory = key
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = themeColor,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Scores Content
                    if (displayScores.isEmpty()) {
                        val noRecordsTitle = when (currentLang) {
                            Language.RU -> "Нет записей"
                            Language.UA -> "Немає записів"
                            Language.KK -> "Жазбалар жоқ"
                            Language.DE -> "Keine Einträge"
                            Language.ZH -> "暂无记录"
                            else -> "No Records Yet"
                        }
                        val noRecordsDesc = when (currentLang) {
                            Language.RU -> "Сыграйте партию и станьте первым в списке рекордов!"
                            Language.UA -> "Зіграйте партію та станьте першим у списку рекордів!"
                            Language.KK -> "Ойын ойнап, рекордтар тізімінде бірінші болыңыз!"
                            Language.DE -> "Spiele eine Runde und sichere dir den ersten Platz!"
                            Language.ZH -> "进行一局游戏，成为排行榜上的第一名！"
                            else -> "Play a match and become the first on the leaderboard!"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        modifier = Modifier.size(54.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Leaderboard,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = noRecordsTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = noRecordsDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
                        ) {
                            // TOP 3 PODIUM HERO SHOWCASE
                            if (displayScores.size >= 3) {
                                item {
                                    val first = displayScores[0]
                                    val second = displayScores[1]
                                    val third = displayScores[2]

                                    val championsPodiumTitle = when (currentLang) {
                                        Language.RU -> "ТОП ЧЕМПИОНОВ"
                                        Language.UA -> "ТОП ЧЕМПІОНІВ"
                                        Language.KK -> "ЧЕМПИОНДАР ҮЗДІГІ"
                                        Language.DE -> "CHAMPIONS-PODIUM"
                                        Language.ZH -> "冠军领奖台"
                                        else -> "CHAMPIONS PODIUM"
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
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EmojiEvents,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = championsPodiumTitle,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // 3 Columns: 2nd (Silver), 1st (Gold - tallest), 3rd (Bronze)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                // 2nd Place (Silver)
                                                PodiumColumn(
                                                    rank = 2,
                                                    score = second,
                                                    medalColor = Color(0xFFC0C0C0),
                                                    height = 145.dp,
                                                    onClick = { viewModel.openUserProfile(second.uid, second.playerName) },
                                                    modifier = Modifier.weight(1f)
                                                )

                                                // 1st Place (Gold)
                                                PodiumColumn(
                                                    rank = 1,
                                                    score = first,
                                                    medalColor = Color(0xFFFFD700),
                                                    height = 175.dp,
                                                    onClick = { viewModel.openUserProfile(first.uid, first.playerName) },
                                                    modifier = Modifier.weight(1.15f)
                                                )

                                                // 3rd Place (Bronze)
                                                PodiumColumn(
                                                    rank = 3,
                                                    score = third,
                                                    medalColor = Color(0xFFCD7F32),
                                                    height = 135.dp,
                                                    onClick = { viewModel.openUserProfile(third.uid, third.playerName) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // REST OF SCORES (or all if < 3)
                            val listStartIndex = if (displayScores.size >= 3) 3 else 0
                            items(displayScores.subList(listStartIndex, displayScores.size)) { score ->
                                val index = displayScores.indexOf(score)
                                val rankColor = when (index) {
                                    0 -> Color(0xFFFFD700)
                                    1 -> Color(0xFFC0C0C0)
                                    2 -> Color(0xFFCD7F32)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openUserProfile(score.uid, score.playerName) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Rank Number Circle
                                            Surface(
                                                shape = CircleShape,
                                                color = rankColor.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, rankColor.copy(alpha = 0.4f)),
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = rankColor
                                                    )
                                                }
                                            }

                                            // Avatar
                                            PlayerAvatarView(
                                                playerName = score.playerName,
                                                avatarEmoji = score.avatarEmoji,
                                                avatarBgColorHex = score.avatarBgColorHex,
                                                avatarFrame = score.avatarFrame,
                                                avatarBase64 = score.avatarBase64,
                                                size = 40.dp,
                                                themeColor = themeColor,
                                                showOnlineDot = true,
                                                isOnline = score.isOnline
                                            )

                                            // Player Info & Tags
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val playerColor = parseHexColor(score.avatarBgColorHex, themeColor)
                                                    val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = playerColor)
                                                    Text(
                                                        text = score.playerName,
                                                        style = if (score.hasGradient) {
                                                            MaterialTheme.typography.bodyMedium.copy(
                                                                brush = nicknameBrush
                                                            )
                                                        } else {
                                                            MaterialTheme.typography.bodyMedium.copy(
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        },
                                                        fontWeight = FontWeight.ExtraBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    if (score.playerName == "FsFq") {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Color(0xFFFFD700)
                                                        ) {
                                                            Text(
                                                                text = "OVERLORD",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Black
                                                                ),
                                                                color = Color.Black,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }

                                                    if (score.customTag.isNotEmpty()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                                        ) {
                                                            Text(
                                                                text = score.customTag,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Score Pill
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = String.format(Locale.getDefault(), "%,d", score.score),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
}
}

@Composable
private fun PodiumColumn(
    rank: Int,
    score: com.example.db.HighScore,
    medalColor: Color,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PodiumAnim")
    val crownFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (rank == 1) -4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownFloat"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Surface(
        modifier = modifier
            .height(height)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(
            width = if (rank == 1) 1.8.dp else 1.dp,
            color = if (rank == 1) medalColor.copy(alpha = glowPulse) else medalColor.copy(alpha = 0.5f)
        ),
        shadowElevation = if (rank == 1) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = medalColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, medalColor),
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        if (rank == 1) translationY = crownFloat
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = medalColor
                    )
                }
            }

            PlayerAvatarView(
                playerName = score.playerName,
                avatarEmoji = score.avatarEmoji,
                avatarBgColorHex = score.avatarBgColorHex,
                avatarFrame = score.avatarFrame,
                avatarBase64 = score.avatarBase64,
                size = if (rank == 1) 48.dp else 40.dp,
                themeColor = medalColor,
                showOnlineDot = true,
                isOnline = score.isOnline
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val playerColor = parseHexColor(score.avatarBgColorHex, medalColor)
                val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = playerColor)
                Text(
                    text = score.playerName,
                    style = if (score.hasGradient) {
                        MaterialTheme.typography.labelSmall.copy(
                            brush = nicknameBrush
                        )
                    } else {
                        MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = String.format(Locale.getDefault(), "%,d", score.score),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Black,
                    color = medalColor
                )
            }
        }
    }
}

