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
import androidx.compose.material.icons.filled.Send
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

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF6C63FF)): Color {
    if (hex.isBlank()) return defaultColor
    return try {
        val cleanHex = hex.trim().removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> android.graphics.Color.parseColor("#FF$cleanHex")
            8 -> android.graphics.Color.parseColor("#$cleanHex")
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun rememberAnimatedNicknameBrush(baseColor: Color = MaterialTheme.colorScheme.primary): Brush {
    val transition = rememberInfiniteTransition(label = "NicknameGradientAnim")
    val gradientSpan = 600f
    val animOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = gradientSpan,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "NicknameOffset"
    )

    // Generate dynamic harmonious palette seamlessly matching current interface theme
    val colors = remember(baseColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.argb(
                (baseColor.alpha * 255).toInt(),
                (baseColor.red * 255).toInt(),
                (baseColor.green * 255).toInt(),
                (baseColor.blue * 255).toInt()
            ),
            hsv
        )
        val baseHue = hsv[0]
        val sat = hsv[1].coerceIn(0.70f, 0.95f)
        val value = hsv[2].coerceIn(0.85f, 1f)

        // Harmonious continuous color loop across color wheel derived from theme hue
        val c1 = Color(android.graphics.Color.HSVToColor(floatArrayOf(baseHue, sat, value)))
        val c2 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
        val c3 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 70f) % 360f, (sat * 0.85f).coerceIn(0.55f, 1f), value)))
        val c4 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
        val c5 = c1

        listOf(c1, c2, c3, c4, c5)
    }

    return remember(animOffset, colors) {
        Brush.linearGradient(
            colors = colors,
            start = Offset(animOffset, 0f),
            end = Offset(animOffset + gradientSpan, 0f),
            tileMode = TileMode.Repeated
        )
    }
}

enum class GameMode {
    CLASSIC,
    EXTENDED,
    FAST_RUN,
    REVERSE,
    BLOCK_BLAST
}

@Composable
fun TetrisApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentLang by viewModel.language.collectAsStateWithLifecycle()

    val showBottomBar = currentRoute in listOf("menu", "profile", "settings", "profile_achievements", "cases")

    val navHostContent = @Composable {
        NavHost(
            navController = navController,
            startDestination = "menu",
            enterTransition = { fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150)) }
        ) {
            // Bottom bar tab order for directional slide (Menu -> Profile -> Cases -> Settings)
            val tabOrder = mapOf("menu" to 0, "profile" to 1, "profile_achievements" to 1, "cases" to 2, "settings" to 3)

            fun tabEnter(towards: String, from: String): EnterTransition {
                val toIdx = tabOrder[towards] ?: return fadeIn(tween(220))
                val fromIdx = tabOrder[from] ?: return fadeIn(tween(220))
                if (toIdx == fromIdx) return fadeIn(tween(220))
                val direction = if (toIdx > fromIdx) 1 else -1
                return slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.35f * direction).toInt() },
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
            }

            fun tabExit(from: String, towards: String): ExitTransition {
                val fromIdx = tabOrder[from] ?: return fadeOut(tween(180))
                val toIdx = tabOrder[towards] ?: return fadeOut(tween(180))
                if (toIdx == fromIdx) return fadeOut(tween(180))
                val direction = if (toIdx > fromIdx) -1 else 1
                return slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (fullWidth * 0.35f * direction).toInt() },
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
            }

            composable(
                "menu",
                enterTransition = { tabEnter("menu", initialState.destination.route ?: "") },
                exitTransition = { tabExit("menu", targetState.destination.route ?: "") },
                popEnterTransition = { tabEnter("menu", initialState.destination.route ?: "") },
                popExitTransition = { tabExit("menu", targetState.destination.route ?: "") }
            ) {
                MainMenuScreen(
                    viewModel = viewModel,
                    onResumeGame = {
                        if (viewModel.loadSavedGame()) {
                            navController.navigate("game")
                        }
                    },
                    onLeaderboard = { navController.navigate("leaderboard") },
                    onProfile = { navController.navigate("profile") },
                    onModeSelection = { navController.navigate("mode_selection") },
                    onPlayMode = { mode ->
                        if (mode == com.example.game.GameMode.BLOCK_BLAST) {
                            viewModel.startBlockBlast()
                            navController.navigate("block_blast")
                        } else {
                            viewModel.startGame(mode)
                            navController.navigate("game")
                        }
                    },
                    onMultiplayer = {
                        navController.navigate("lobby")
                    }
                )
            }
            composable("game") {
                GameScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("leaderboard") {
                LeaderboardScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(
                "settings",
                enterTransition = { tabEnter("settings", initialState.destination.route ?: "") },
                exitTransition = { tabExit("settings", targetState.destination.route ?: "") },
                popEnterTransition = { tabEnter("settings", initialState.destination.route ?: "") },
                popExitTransition = { tabExit("settings", targetState.destination.route ?: "") }
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCustomizeControls = { navController.navigate("custom_controls") }
                )
            }
            composable("custom_controls") {
                CustomControlsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("block_blast") {
                BlockBlastScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(
                "cases",
                enterTransition = { tabEnter("cases", initialState.destination.route ?: "") },
                exitTransition = { tabExit("cases", targetState.destination.route ?: "") },
                popEnterTransition = { tabEnter("cases", initialState.destination.route ?: "") },
                popExitTransition = { tabExit("cases", targetState.destination.route ?: "") }
            ) {
                CasesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("mode_selection") {
                ModeSelectionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onPlayMode = { mode ->
                        if (mode == com.example.game.GameMode.BLOCK_BLAST) {
                            viewModel.startBlockBlast()
                            navController.navigate("block_blast")
                        } else {
                            viewModel.startGame(mode)
                            navController.navigate("game")
                        }
                    }
                )
            }
            composable(
                "profile_achievements",
                enterTransition = { tabEnter("profile_achievements", initialState.destination.route ?: "") },
                exitTransition = { tabExit("profile_achievements", targetState.destination.route ?: "") },
                popEnterTransition = { tabEnter("profile_achievements", initialState.destination.route ?: "") },
                popExitTransition = { tabExit("profile_achievements", targetState.destination.route ?: "") }
            ) {
                ProfileScreen(viewModel = viewModel, initialTab = 2, onBack = { navController.popBackStack() })
            }
            composable(
                "profile",
                enterTransition = { tabEnter("profile", initialState.destination.route ?: "") },
                exitTransition = { tabExit("profile", targetState.destination.route ?: "") },
                popEnterTransition = { tabEnter("profile", initialState.destination.route ?: "") },
                popExitTransition = { tabExit("profile", targetState.destination.route ?: "") }
            ) {
                ProfileScreen(viewModel = viewModel, initialTab = 0, onBack = { navController.popBackStack() })
            }
            composable("lobby") {
                LobbyScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToGame = { navController.navigate("multiplayer_game") }
                )
            }
            composable("multiplayer_game") {
                MultiplayerGameScreen(
                    viewModel = viewModel,
                    onBackToLobby = {
                        navController.navigate("lobby") {
                            popUpTo("lobby") { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    val context = LocalContext.current
    var isPreloading by rememberSaveable { mutableStateOf(true) }
    var preloadProgress by remember { mutableFloatStateOf(0.15f) }
    var preloadStatusText by remember { mutableStateOf(if (currentLang == Language.RU) "Кэширование ресурсов..." else "Warming up cache...") }

    val themeColorVal = MaterialTheme.colorScheme.primary

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

    LaunchedEffect(Unit) {
        if (isPreloading) {
            withContext(Dispatchers.IO) {
                val profilePrefs = context.getSharedPreferences("block_tetris_prefs", Context.MODE_PRIVATE)
                val gamePrefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

                val warmSteps = listOf(
                    if (currentLang == Language.RU) "Прогрев настроек и темы" else "Warming settings & themes",
                    if (currentLang == Language.RU) "Инициализация аудио движка" else "Initializing audio engine",
                    if (currentLang == Language.RU) "Кэширование профиля и кредитов" else "Caching profile & credits",
                    if (currentLang == Language.RU) "Предзагрузка скинов и стилей" else "Preloading skins & styles",
                    if (currentLang == Language.RU) "Кэширование базы рекордов" else "Caching highscores database",
                    if (currentLang == Language.RU) "Десериализация инвентаря кейсов" else "Deserializing cases inventory",
                    if (currentLang == Language.RU) "Прогрев шрифтов и локализации" else "Warming fonts & translations",
                    if (currentLang == Language.RU) "Инициализация игровых движков" else "Initializing game engines",
                    if (currentLang == Language.RU) "Оптимизация UI компонентов" else "Optimizing UI components",
                    if (currentLang == Language.RU) "Финализация кэша вкладок" else "Finalizing tabs cache"
                )

                for (i in 1..10) {
                    preloadProgress = (i - 0.2f) / 10f
                    val stepDesc = warmSteps.getOrElse(i - 1) { "Прогрев" }
                    preloadStatusText = if (currentLang == Language.RU) {
                        "Прогрев кэша ($i/10): $stepDesc..."
                    } else {
                        "Cache warm-up ($i/10): $stepDesc..."
                    }

                    // Реальные операции прогрева кэша
                    when (i) {
                        1 -> {
                            profilePrefs.all
                            gamePrefs.all
                            viewModel.themeColor.value
                        }
                        2 -> {
                            viewModel.soundVolume.value
                            viewModel.soundEnabled.value
                        }
                        3 -> {
                            viewModel.playerName.value
                            viewModel.credits.value
                            viewModel.purchasedThemes.value
                        }
                        4 -> {
                            viewModel.purchasedAvatarFrames.value
                            viewModel.purchasedTitles.value
                            viewModel.purchasedSoundPacks.value
                            viewModel.purchasedControlButtonStyles.value
                            viewModel.purchasedFonts.value
                        }
                        5 -> {
                            viewModel.allAccounts
                        }
                        6 -> {
                            val rawInv = profilePrefs.getStringSet("case_inventory", emptySet()) ?: emptySet()
                            rawInv.forEach { deserializeInventoryItem(it) }
                        }
                        7 -> {
                            Translations.get("settings", currentLang)
                            Translations.get("profile", currentLang)
                        }
                        8 -> {
                            viewModel.gameEngine
                            viewModel.blockBlastEngine
                        }
                        9 -> {
                            profilePrefs.getStringSet("purchased_modes", emptySet())
                        }
                        10 -> {
                            preloadProgress = 1.0f
                        }
                    }
                    delay(220)
                }

                preloadProgress = 1.0f
                preloadStatusText = if (currentLang == Language.RU) "Все вкладки готовы к игре!" else "All tabs ready to play!"
                delay(120)
            }
            isPreloading = false
        }
    }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Box(modifier = Modifier.fillMaxSize()) {
        if (isWideScreen && showBottomBar) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    val items = listOf(
                        Triple("menu", if (currentLang == Language.RU) "Меню" else "Menu", Icons.Default.Home),
                        Triple("profile", if (currentLang == Language.RU) "Профиль" else "Profile", Icons.Default.Person),
                        Triple("cases", if (currentLang == Language.RU) "Кейсы" else "Cases", Icons.Default.CardGiftcard),
                        Triple("settings", if (currentLang == Language.RU) "Настройки" else "Settings", Icons.Default.Settings)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    items.forEach { (route, label, icon) ->
                        val isSelected = currentRoute == route || (route == "profile" && currentRoute == "profile_achievements")
                        NavigationRailItem(
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontWeight = FontWeight.Bold) },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo("menu") {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    navHostContent()
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            val items = listOf(
                                Triple("menu", if (currentLang == Language.RU) "Меню" else "Menu", Icons.Default.Home),
                                Triple("profile", if (currentLang == Language.RU) "Профиль" else "Profile", Icons.Default.Person),
                                Triple("cases", if (currentLang == Language.RU) "Кейсы" else "Cases", Icons.Default.CardGiftcard),
                                Triple("settings", if (currentLang == Language.RU) "Настройки" else "Settings", Icons.Default.Settings)
                            )
                            items.forEach { (route, label, icon) ->
                                val isSelected = currentRoute == route || (route == "profile" && currentRoute == "profile_achievements")
                                NavigationBarItem(
                                    icon = { Icon(imageVector = icon, contentDescription = label) },
                                    label = { Text(text = label, fontWeight = FontWeight.Bold) },
                                    selected = isSelected,
                                    onClick = {
                                        if (currentRoute != route) {
                                            navController.navigate(route) {
                                                popUpTo("menu") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues)) {
                    navHostContent()
                }
            }
        }

        // App Launch Preload & Cache Warmup Overlay with Skip Button
        AnimatedVisibility(
            visible = isPreloading,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    themeColorVal.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                ) {
                    // Top Bar with Icon on Top-Left and Skip Button on Top-Right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 44.dp, start = 20.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Icon in Top-Left Corner
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (appIcon != null) {
                                    Image(
                                        bitmap = appIcon,
                                        contentDescription = "Tetris App Icon",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.VideogameAsset,
                                        contentDescription = "Tetris App Icon",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Skip Button in Top-Right Corner
                        FilledTonalButton(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                isPreloading = false
                            },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "ПРОПУСТИТЬ" else "SKIP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Center Progress Info in MD3 Style without breathing
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TETRIS MATRIX",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = preloadStatusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val animatedProgress by animateFloatAsState(
                            targetValue = preloadProgress,
                            animationSpec = tween(250, easing = FastOutSlowInEasing),
                            label = "PreloadProgressBar"
                        )

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = themeColorVal,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            }
        }



        // Public Profile Modal Dialog
        val selectedProfile by viewModel.selectedPublicProfile.collectAsStateWithLifecycle()
        if (selectedProfile != null) {
            OtherUserProfileDialog(
                profile = selectedProfile!!,
                currentLang = currentLang,
                themeColor = themeColorVal,
                onDismiss = { viewModel.closeUserProfile() },
                onAddFriend = {
                    viewModel.sendFriendRequest(selectedProfile!!.username) { _, _ -> }
                },
                onInviteToDuel = {
                    viewModel.closeUserProfile()
                    navController.navigate("lobby")
                }
            )
        }

        // Friends System Dialog
        val showFriendsDialog by viewModel.showFriendsDialog.collectAsStateWithLifecycle()
        if (showFriendsDialog) {
            FriendsDialog(
                viewModel = viewModel,
                currentLang = currentLang,
                themeColor = themeColorVal,
                onDismiss = { viewModel.setShowFriendsDialog(false) },
                onOpenLobby = {
                    viewModel.setShowFriendsDialog(false)
                    navController.navigate("lobby")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    viewModel: MainViewModel,
    onResumeGame: () -> Unit,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit,
    onModeSelection: () -> Unit,
    onPlayMode: (com.example.game.GameMode) -> Unit,
    onMultiplayer: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val hasSaved by viewModel.hasSavedGame.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val isAdminSessionAuthenticated by viewModel.isAdminSessionAuthenticated.collectAsStateWithLifecycle()

    var showAdminPanelDialog by remember { mutableStateOf(false) }
    var showAdminPassDialog by remember { mutableStateOf(false) }
    var adminPassInput by remember { mutableStateOf("") }
    var adminPassError by remember { mutableStateOf(false) }
    val adminPassHashTarget = remember { com.example.db.PasswordHasher.hash("7532") }
    var showAuthGuardDialog by remember { mutableStateOf(false) }


    val themeColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            if (showAuthGuardDialog) {
                AlertDialog(
                    onDismissRequest = { showAuthGuardDialog = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = {
                        Text(
                            text = if (currentLang == Language.RU) "Требуется авторизация" else "Authentication Required",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            text = if (currentLang == Language.RU) 
                                "Для игры в сетевом режиме необходимо зарегистрироваться или войти в свой аккаунт. Хотите перейти в профиль?" 
                                else "To play online multiplayer, you need to sign in or create an account. Would you like to go to your profile?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAuthGuardDialog = false
                                onProfile()
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (currentLang == Language.RU) "ВХОД / РЕГИСТРАЦИЯ" else "LOGIN / REGISTER", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showAuthGuardDialog = false 
                                viewModel.triggerAudioFeedback("click")
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (currentLang == Language.RU) "ОТМЕНА" else "CANCEL", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showAdminPassDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showAdminPassDialog = false
                        adminPassInput = ""
                        adminPassError = false
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
                    title = {
                        Text(
                            text = if (currentLang == Language.RU) "Защита Админ-Панели" else "Admin Panel Guard",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = if (currentLang == Language.RU) "Введите секретный пароль доступа к консоли администратора:" else "Enter secret password to access administrator console:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = adminPassInput,
                                onValueChange = {
                                    adminPassInput = it
                                    adminPassError = false
                                },
                                label = { Text(if (currentLang == Language.RU) "Пароль доступа" else "Access Password") },
                                singleLine = true,
                                isError = adminPassError,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (adminPassError) {
                                Text(
                                    text = if (currentLang == Language.RU) "Неверный пароль доступа!" else "Incorrect access password!",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (com.example.db.PasswordHasher.hash(adminPassInput.trim()) == adminPassHashTarget) {
                                    viewModel.setAdminSessionAuthenticated(true)
                                    showAdminPassDialog = false
                                    adminPassInput = ""
                                    adminPassError = false
                                    showAdminPanelDialog = true
                                } else {
                                    adminPassError = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (currentLang == Language.RU) "ВОЙТИ" else "ENTER", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAdminPassDialog = false
                                adminPassInput = ""
                                adminPassError = false
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (currentLang == Language.RU) "ОТМЕНА" else "CANCEL")
                        }
                    }
                )
            }

            if (showAdminPanelDialog) {
                var editUsername by remember { mutableStateOf(playerName) }
                var editCredits by remember { mutableStateOf("") }
                var editRank by remember { mutableStateOf("BRONZE") }
                var editBonusXp by remember { mutableStateOf("0") }
                var editHasGradient by remember { mutableStateOf(false) }
                var broadcastTitle by remember { mutableStateOf("") }
                var broadcastText by remember { mutableStateOf("") }
                var broadcastSentFeedback by remember { mutableStateOf<String?>(null) }
                var userSearchQuery by remember { mutableStateOf("") }
                var selectedAdminTab by remember { mutableIntStateOf(0) }
                val firebaseUsers by viewModel.firebaseUsers.collectAsStateWithLifecycle(initialValue = emptyList())
                var editingAdminUser by remember { mutableStateOf<Map<String, Any>?>(null) }
                var editUserCredits by remember { mutableStateOf("") }
                var editUserHighScore by remember { mutableStateOf("") }
                var editUserLines by remember { mutableStateOf("") }
                var editUserGames by remember { mutableStateOf("") }
                var editUserXp by remember { mutableStateOf("") }
                var editUserTier by remember { mutableStateOf("BRONZE") }
                var editUserGradient by remember { mutableStateOf(false) }
                var editUserTagUnlocked by remember { mutableStateOf(false) }
                var editUserTag by remember { mutableStateOf("") }

                LaunchedEffect(selectedAdminTab) {
                    if (selectedAdminTab == 1) {
                        viewModel.fetchFirebaseUsersForAdmin()
                    }
                }

                AnimatedVisibility(
                    visible = showAdminPanelDialog,
                    enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(250)),
                    exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(200))
                ) {
                    BackHandler { showAdminPanelDialog = false }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            topBar = {
                                @OptIn(ExperimentalMaterial3Api::class)
                                CenterAlignedTopAppBar(
                                    modifier = Modifier.statusBarsPadding(),
                                    title = {
                                        Text(
                                            text = if (currentLang == Language.RU) "Админ-панель" else "Admin Panel",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { showAdminPanelDialog = false }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        ) { paddingValues ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .consumeWindowInsets(paddingValues)
                                    .imePadding()
                            ) {
                                // MD3 Segmented Tab Selector
                                val adminTabs = listOf(
                                    Triple(0, if (currentLang == Language.RU) "Профиль" else "Profile", Icons.Default.Person),
                                    Triple(1, if (currentLang == Language.RU) "Игроки" else "Players", Icons.Default.People),
                                    Triple(2, if (currentLang == Language.RU) "Оповещения" else "Broadcast", Icons.Default.Send),
                                    Triple(3, if (currentLang == Language.RU) "Система" else "System", Icons.Default.Settings)
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp)
                                    ) {
                                        val tabWidth = maxWidth / adminTabs.size
                                        val indicatorOffset by animateDpAsState(
                                            targetValue = tabWidth * selectedAdminTab,
                                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                                            label = "adminTabIndicator"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .width(tabWidth)
                                                .height(42.dp)
                                                .offset(x = indicatorOffset)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(themeColor)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            adminTabs.forEach { (index, title, icon) ->
                                                val isSelected = selectedAdminTab == index
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(42.dp)
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .clickable {
                                                            viewModel.triggerAudioFeedback("click")
                                                            selectedAdminTab = index
                                                        },
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
                                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                AnimatedContent(
                                    targetState = selectedAdminTab,
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
                                    label = "AdminTabContentAnim"
                                ) { tab ->
                                    when (tab) {
                                        0 -> {
                                            // PROFILE STATE EDITOR
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = if (currentLang == Language.RU) "Редактирование профиля" else "Profile Editor",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        OutlinedTextField(
                                                            value = editUsername,
                                                            onValueChange = { editUsername = it },
                                                            label = { Text(if (currentLang == Language.RU) "Целевой никнейм" else "Target Username") },
                                                            singleLine = true,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            OutlinedTextField(
                                                                value = editCredits,
                                                                onValueChange = { editCredits = it },
                                                                label = { Text(if (currentLang == Language.RU) "Кредиты 🪙" else "Credits 🪙") },
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            OutlinedTextField(
                                                                value = editBonusXp,
                                                                onValueChange = { editBonusXp = it },
                                                                label = { Text(if (currentLang == Language.RU) "Бонус XP" else "Bonus XP") },
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = if (currentLang == Language.RU) "Градиент никнейма" else "Nickname Gradient",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Switch(
                                                                checked = editHasGradient,
                                                                onCheckedChange = { editHasGradient = it }
                                                            )
                                                        }

                                                        Button(
                                                            onClick = {
                                                                if (editUsername.isNotBlank()) {
                                                                    val creds = editCredits.toIntOrNull()
                                                                    if (creds != null) {
                                                                        viewModel.adminUpdateAccountCredits(editUsername.trim(), creds)
                                                                    }
                                                                    val xp = editBonusXp.toIntOrNull() ?: 0
                                                                    viewModel.adminUpdateAccountBonusXp(editUsername.trim(), xp)
                                                                    viewModel.adminUpdateAccountGradient(editUsername.trim(), editHasGradient)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                }
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(if (currentLang == Language.RU) "ПРИМЕНИТЬ ДАННЫЕ" else "SAVE PROFILE STATE", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                // Quick Action Cheats Card
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Text(
                                                            text = if (currentLang == Language.RU) "БЫСТРЫЕ ДЕЙСТВИЯ" else "QUICK ACTIONS",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminGiveAllCosmetics(editUsername.ifBlank { playerName })
                                                                viewModel.triggerAudioFeedback("success")
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(if (currentLang == Language.RU) "ВЫДАТЬ ВСЮ КОСМЕТИКУ (Скины, Рамки, Шрифты)" else "GRANT ALL COSMETICS & SKINS", fontWeight = FontWeight.Bold)
                                                        }

                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminUnlockAllAchievements()
                                                                viewModel.triggerAudioFeedback("success")
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(if (currentLang == Language.RU) "ОТКРЫТЬ ВСЕ ДОСТИЖЕНИЯ + 5000 🪙" else "UNLOCK ALL ACHIEVEMENTS + 5000 🪙", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        1 -> {
                                            // FIREBASE USERS & BANS
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = userSearchQuery,
                                                    onValueChange = { userSearchQuery = it },
                                                    label = { Text(if (currentLang == Language.RU) "Поиск игроков..." else "Search players...") },
                                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                val filteredUsers = remember(firebaseUsers, userSearchQuery) {
                                                    if (userSearchQuery.isBlank()) firebaseUsers
                                                    else firebaseUsers.filter {
                                                        val pName = (it["player_name"] as? String) ?: ""
                                                        val email = (it["email"] as? String) ?: ""
                                                        pName.contains(userSearchQuery, ignoreCase = true) ||
                                                        email.contains(userSearchQuery, ignoreCase = true)
                                                    }
                                                }

                                                Text(
                                                    text = if (currentLang == Language.RU) "Всего игроков в базе: ${filteredUsers.size}" else "Total players in database: ${filteredUsers.size}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                LazyColumn(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    contentPadding = PaddingValues(bottom = 24.dp)
                                                ) {
                                                    items(filteredUsers) { user ->
                                                        val uName = (user["player_name"] as? String) ?: "Unknown"
                                                        val uEmail = (user["email"] as? String) ?: "No email"
                                                        val uUid = (user["uid"] as? String) ?: ""
                                                        val uCredits = (user["credits"] as? Number)?.toLong() ?: 0L
                                                        val isBanned = (user["is_banned"] as? Boolean) == true
                                                        val isEmailVerified = (user["email_verified"] as? Boolean) == true

                                                        ElevatedCard(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(18.dp),
                                                            colors = CardDefaults.elevatedCardColors(
                                                                containerColor = if (isBanned) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                                                else MaterialTheme.colorScheme.surfaceContainerHigh
                                                            )
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.padding(14.dp),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(modifier = Modifier.weight(1f)) {
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                        ) {
                                                                            Text(
                                                                                text = uName,
                                                                                style = MaterialTheme.typography.titleMedium,
                                                                                fontWeight = FontWeight.ExtraBold
                                                                            )
                                                                            if (uName == "FsFq") {
                                                                                Surface(
                                                                                    shape = RoundedCornerShape(6.dp),
                                                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                                                ) {
                                                                                    Text(
                                                                                        text = "ADMIN",
                                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                        Text(
                                                                            text = if (isEmailVerified) (if (currentLang == Language.RU) "Почта: Подтверждена ✅" else "Email: Verified ✅")
                                                                                   else (if (currentLang == Language.RU) "Почта: Не подтверждена ⏳" else "Email: Pending ⏳"),
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100),
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    }

                                                                    Surface(
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        color = if (isBanned) MaterialTheme.colorScheme.error else if (isEmailVerified) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF9100).copy(alpha = 0.2f)
                                                                    ) {
                                                                        Text(
                                                                            text = if (isBanned) "BANNED" else if (isEmailVerified) "VERIFIED" else "PENDING",
                                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                            color = if (isBanned) MaterialTheme.colorScheme.onError else if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100),
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                }

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = "🪙 $uCredits",
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color(0xFFFFB300)
                                                                    )

                                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                        OutlinedButton(
                                                                            onClick = {
                                                                                editingAdminUser = user
                                                                                editUserCredits = ((user["credits"] as? Number)?.toInt() ?: 0).toString()
                                                                                editUserHighScore = ((user["stats_high_score"] as? Number)?.toInt() ?: 0).toString()
                                                                                editUserLines = ((user["stats_cleared_lines"] as? Number)?.toInt() ?: 0).toString()
                                                                                editUserGames = ((user["stats_games_played"] as? Number)?.toInt() ?: 0).toString()
                                                                                editUserXp = ((user["bonus_xp"] as? Number)?.toInt() ?: 0).toString()
                                                                                editUserTier = (user["online_tier"] as? String) ?: "BRONZE"
                                                                                editUserGradient = (user["has_nickname_gradient"] as? Boolean) == true
                                                                                editUserTagUnlocked = (user["custom_tag_unlocked"] as? Boolean) == true
                                                                                editUserTag = (user["custom_tag"] as? String) ?: ""
                                                                                viewModel.triggerAudioFeedback("click")
                                                                            },
                                                                            shape = RoundedCornerShape(10.dp)
                                                                        ) {
                                                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                                                            Spacer(modifier = Modifier.width(4.dp))
                                                                            Text(
                                                                                text = if (currentLang == Language.RU) "Изменить" else "Edit",
                                                                                style = MaterialTheme.typography.labelSmall
                                                                            )
                                                                        }

                                                                        if (uName != "FsFq") {
                                                                            OutlinedButton(
                                                                                onClick = {
                                                                                    viewModel.adminBanUser(uUid, !isBanned)
                                                                                    viewModel.triggerAudioFeedback("click")
                                                                                },
                                                                                shape = RoundedCornerShape(10.dp)
                                                                            ) {
                                                                                Text(
                                                                                    text = if (isBanned) (if (currentLang == Language.RU) "Разбан" else "Unban")
                                                                                    else (if (currentLang == Language.RU) "Бан" else "Ban"),
                                                                                    style = MaterialTheme.typography.labelSmall
                                                                                )
                                                                            }

                                                                            IconButton(
                                                                                onClick = {
                                                                                    viewModel.adminDeleteFirebaseUserDirect(uUid)
                                                                                    viewModel.triggerAudioFeedback("gameover")
                                                                                }
                                                                            ) {
                                                                                Icon(
                                                                                    imageVector = Icons.Default.Delete,
                                                                                    contentDescription = "Delete",
                                                                                    tint = MaterialTheme.colorScheme.error,
                                                                                    modifier = Modifier.size(20.dp)
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
                                        2 -> {
                                            // GLOBAL BROADCAST ANNOUNCEMENTS
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = if (currentLang == Language.RU) "ОТПРАВКА СИСТЕМНОГО ОПОВЕЩЕНИЯ" else "GLOBAL SERVER BROADCAST",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        Text(
                                                            text = if (currentLang == Language.RU)
                                                                "Сообщение отобразится у всех онлайн-игроков в чате лобби и будет сохранено в базе данных."
                                                                else "Message will be broadcast to all connected players in the multiplayer lobby chat and saved to global database.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        OutlinedTextField(
                                                            value = broadcastTitle,
                                                            onValueChange = { broadcastTitle = it },
                                                            label = { Text(if (currentLang == Language.RU) "Заголовок оповещения" else "Broadcast Title") },
                                                            singleLine = true,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        OutlinedTextField(
                                                            value = broadcastText,
                                                            onValueChange = { broadcastText = it },
                                                            label = { Text(if (currentLang == Language.RU) "Текст сообщения" else "Message Content") },
                                                            minLines = 3,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Button(
                                                            onClick = {
                                                                if (broadcastText.isNotBlank()) {
                                                                    viewModel.adminSendGlobalBroadcast(broadcastTitle, broadcastText)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                    broadcastSentFeedback = if (currentLang == Language.RU) "Оповещение успешно отправлено!" else "Broadcast dispatched successfully!"
                                                                    broadcastTitle = ""
                                                                    broadcastText = ""
                                                                }
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(if (currentLang == Language.RU) "ОТПРАВИТЬ ВСЕМ" else "BROADCAST TO ALL", fontWeight = FontWeight.Bold)
                                                        }

                                                        broadcastSentFeedback?.let { msg ->
                                                            Text(
                                                                text = msg,
                                                                color = Color(0xFF00E676),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        3 -> {
                                            // DATABASE & SYSTEM TOOLS
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = if (currentLang == Language.RU) "УПРАВЛЕНИЕ БАЗОЙ ДАННЫХ" else "DATABASE MANAGEMENT",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminClearAllScores()
                                                                viewModel.triggerAudioFeedback("gameover")
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(if (currentLang == Language.RU) "СБРОСИТЬ ВСЕ РЕКОРДЫ В БД" else "RESET ALL DATABASE SCORES", fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminClearAllNonAdminAccounts()
                                                                viewModel.triggerAudioFeedback("gameover")
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(if (currentLang == Language.RU) "УДАЛИТЬ ВСЕ АККАУНТЫ КРОМЕ АДМИНА" else "DELETE ALL NON-ADMIN USERS", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (editingAdminUser != null) {
                                    val target = editingAdminUser!!
                                    val tUid = (target["uid"] as? String) ?: ""
                                    val tName = (target["player_name"] as? String) ?: "Player"

                                    AlertDialog(
                                        onDismissRequest = { editingAdminUser = null },
                                        title = {
                                            Text(
                                                text = if (currentLang == Language.RU) "Редактирование: $tName" else "Edit Stats: $tName",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        },
                                        text = {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .verticalScroll(rememberScrollState()),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = editUserCredits,
                                                    onValueChange = { editUserCredits = it },
                                                    label = { Text(if (currentLang == Language.RU) "Монеты 🪙" else "Credits 🪙") },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = editUserHighScore,
                                                        onValueChange = { editUserHighScore = it },
                                                        label = { Text(if (currentLang == Language.RU) "Рекорд 🏆" else "High Score 🏆") },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = editUserLines,
                                                        onValueChange = { editUserLines = it },
                                                        label = { Text(if (currentLang == Language.RU) "Линии 🧱" else "Lines 🧱") },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = editUserGames,
                                                        onValueChange = { editUserGames = it },
                                                        label = { Text(if (currentLang == Language.RU) "Игры 🎮" else "Games 🎮") },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = editUserXp,
                                                        onValueChange = { editUserXp = it },
                                                        label = { Text(if (currentLang == Language.RU) "Опыт ⭐" else "Bonus XP ⭐") },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = editUserTier,
                                                    onValueChange = { editUserTier = it },
                                                    label = { Text(if (currentLang == Language.RU) "Ранг (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)" else "Tier") },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(if (currentLang == Language.RU) "Градиент ника" else "Gradient Nickname", style = MaterialTheme.typography.bodyMedium)
                                                    Switch(checked = editUserGradient, onCheckedChange = { editUserGradient = it })
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(if (currentLang == Language.RU) "Личный Тег разблокирован" else "Custom Tag Unlocked", style = MaterialTheme.typography.bodyMedium)
                                                    Switch(checked = editUserTagUnlocked, onCheckedChange = { editUserTagUnlocked = it })
                                                }
                                                if (editUserTagUnlocked) {
                                                    OutlinedTextField(
                                                        value = editUserTag,
                                                        onValueChange = { editUserTag = it },
                                                        label = { Text(if (currentLang == Language.RU) "Текст личного тега" else "Custom Tag Text") },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    viewModel.adminUpdateUserStats(
                                                        uid = tUid,
                                                        credits = editUserCredits.toIntOrNull() ?: 0,
                                                        highScore = editUserHighScore.toIntOrNull() ?: 0,
                                                        linesCleared = editUserLines.toIntOrNull() ?: 0,
                                                        gamesPlayed = editUserGames.toIntOrNull() ?: 0,
                                                        bonusXp = editUserXp.toIntOrNull() ?: 0,
                                                        onlineTier = editUserTier.trim().uppercase(),
                                                        hasGradient = editUserGradient,
                                                        customTagUnlocked = editUserTagUnlocked,
                                                        customTag = editUserTag.trim()
                                                    )
                                                    viewModel.triggerAudioFeedback("success")
                                                    editingAdminUser = null
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(if (currentLang == Language.RU) "СОХРАНИТЬ" else "SAVE", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { editingAdminUser = null }) {
                                                Text(if (currentLang == Language.RU) "Отмена" else "Cancel")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = "TETRIS",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 6.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                if (playerName == "FsFq") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            if (isAdminSessionAuthenticated) {
                                                showAdminPanelDialog = true
                                            } else {
                                                adminPassInput = ""
                                                adminPassError = false
                                                showAdminPassDialog = true
                                            }
                                        },
                                        modifier = Modifier.size(42.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Admin Console",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (hasSaved) {
                                Button(
                                    onClick = onResumeGame,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        text = Translations.get("resume", currentLang).uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            MenuButton(
                                text = if (currentLang == Language.RU) "КЛАССИЧЕСКИЙ РЕЖИМ" else "CLASSIC MODE",
                                iconType = "play",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                            )

                            MenuButton(
                                text = if (currentLang == Language.RU) "РЕЖИМЫ ИГРЫ" else "CHOOSE Game Mode",
                                iconType = "upgrades",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = { onModeSelection() }
                            )

                            MenuButton(
                                text = Translations.get("leaderboard", currentLang),
                                iconType = "leaderboard",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = onLeaderboard
                            )

                            MenuButton(
                                text = if (currentLang == Language.RU) "МУЛЬТИПЛЕЕР" else "MULTIPLAYER",
                                iconType = "multiplayer",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    if (playerName == "Player 1") {
                                        viewModel.triggerAudioFeedback("error")
                                        showAuthGuardDialog = true
                                    } else {
                                        viewModel.triggerAudioFeedback("click")
                                        onMultiplayer()
                                    }
                                }
                            )

                            MenuButton(
                                text = if (currentLang == Language.RU) "ДРУЗЬЯ" else "FRIENDS",
                                iconType = "friends",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    viewModel.setShowFriendsDialog(true)
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                        ) {
                            Text(
                                text = "TETRIS",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 6.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            if (playerName == "FsFq") {
                                Spacer(modifier = Modifier.width(8.dp))
                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        if (isAdminSessionAuthenticated) {
                                            showAdminPanelDialog = true
                                        } else {
                                            adminPassInput = ""
                                            adminPassError = false
                                            showAdminPassDialog = true
                                        }
                                    },
                                    modifier = Modifier.size(38.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Admin Console",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (hasSaved) {
                            Button(
                                onClick = onResumeGame,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = Translations.get("resume", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        MenuButton(
                            text = if (currentLang == Language.RU) "КЛАССИЧЕСКИЙ РЕЖИМ" else "CLASSIC MODE",
                            iconType = "play",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                        )

                        MenuButton(
                            text = if (currentLang == Language.RU) "РЕЖИМЫ ИГРЫ" else "CHOOSE Game Mode",
                            iconType = "upgrades",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onModeSelection() }
                        )

                        MenuButton(text = Translations.get("leaderboard", currentLang), iconType = "leaderboard", themeColor = MaterialTheme.colorScheme.primary, onClick = onLeaderboard)

                        MenuButton(
                            text = if (currentLang == Language.RU) "МУЛЬТИПЛЕЕР" else "MULTIPLAYER",
                            iconType = "multiplayer",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                if (playerName == "Player 1") {
                                    viewModel.triggerAudioFeedback("error")
                                    showAuthGuardDialog = true
                                } else {
                                    viewModel.triggerAudioFeedback("click")
                                    onMultiplayer()
                                }
                            }
                        )

                        MenuButton(
                            text = if (currentLang == Language.RU) "ДРУЗЬЯ" else "FRIENDS",
                            iconType = "friends",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                viewModel.setShowFriendsDialog(true)
                            }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
}

@Composable
fun MenuButton(
    text: String,
    iconType: String,
    themeColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = { onClick() },
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
                    Text(
                        text = if (currentLang == Language.RU) "ТАБЛИЦА РЕКОРДОВ" else "LEADERBOARD",
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

                    val tabs = listOf(
                        Triple(0, if (currentLang == Language.RU) "Локальные" else "Local", Icons.Default.PhoneAndroid),
                        Triple(1, if (currentLang == Language.RU) "Мировые" else "Global Arena", Icons.Default.Public)
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
                                "overall" to (if (currentLang == Language.RU) "Все режимы" else "All Modes"),
                                "classic" to (if (currentLang == Language.RU) "Классический" else "Classic"),
                                "extended" to (if (currentLang == Language.RU) "Расширенный" else "Extended"),
                                "fast_run" to (if (currentLang == Language.RU) "Гипер-Режим" else "Hyper Rush"),
                                "reverse" to (if (currentLang == Language.RU) "Хаос" else "Chaos"),
                                "block_blast" to "ZETA",
                                "zen" to (if (currentLang == Language.RU) "Дзен" else "Zen"),
                                "time_attack" to (if (currentLang == Language.RU) "Тайм-Атак" else "Time Attack"),
                                "pulse_extreme" to (if (currentLang == Language.RU) "Вихрь" else "Vortex Pulse"),
                                "mirror" to (if (currentLang == Language.RU) "Зеркальный" else "Mirror"),
                                "penta" to (if (currentLang == Language.RU) "Пента-Хаос" else "Pentatris")
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
                                        text = if (currentLang == Language.RU) "Нет записей" else "No Records Yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (currentLang == Language.RU) "Сыграйте партию и станьте первым в списке рекордов!" else "Play a match and become the first on the leaderboard!",
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
                                                    text = if (currentLang == Language.RU) "ТОП ЧЕМПИОНОВ" else "CHAMPIONS PODIUM",
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
                                                size = 40.dp,
                                                themeColor = themeColor
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
                size = if (rank == 1) 48.dp else 40.dp,
                themeColor = medalColor
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                    0 -> {
                        // Color Palette Swatches Card (All Free & Unlocked, with Monet Gradient Circle)
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
                                    text = if (currentLang == Language.RU) "Качество графики" else "Graphics Quality",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                val graphicsQuality by viewModel.graphicsQuality.collectAsStateWithLifecycle()
                                val qualityLevels = listOf(
                                    "low" to (if (currentLang == Language.RU) "Низкая" else "Low"),
                                    "medium" to (if (currentLang == Language.RU) "Средняя" else "Medium"),
                                    "high" to (if (currentLang == Language.RU) "Высокая" else "High")
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
                    }

                    1 -> {
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
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val purchasedCubeSkinsSet = remember {
                                    val sharedPrefs = context.getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
                                    sharedPrefs.getStringSet("purchased_cube_skins", setOf("neon")) ?: setOf("neon")
                                }
                                val styles = listOf(
                                    "neon" to (if (currentLang == Language.RU) "Гипер Неон" else "Hyper Neon"),
                                    "glass" to (if (currentLang == Language.RU) "Глассморфизм" else "Glassmorphism"),
                                    "retro" to (if (currentLang == Language.RU) "Ретро-Концентрик" else "Retro Concentric"),
                                    "flat" to (if (currentLang == Language.RU) "Простой Плоский" else "Minimal Flat"),
                                    "material" to (if (currentLang == Language.RU) "Material 3" else "Android Material 3")
                                )
                                styles.forEach { (key, title) ->
                                    val isOwned = purchasedCubeSkinsSet.contains(key) || key == "neon"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (isOwned) {
                                                    viewModel.setBlockStyle(key)
                                                } else {
                                                    viewModel.triggerAudioFeedback("error")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        if (currentLang == Language.RU) "Купите этот стиль в магазине!" else "Purchase this style in the store!",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = blockStyle == key,
                                            onClick = {
                                                if (isOwned) {
                                                    viewModel.setBlockStyle(key)
                                                } else {
                                                    viewModel.triggerAudioFeedback("error")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        if (currentLang == Language.RU) "Купите этот стиль в магазине!" else "Purchase this style in the store!",
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
                                    "classic" to (if (currentLang == Language.RU) "Стандартная сетка" else "Classic Grid Lines"),
                                    "dashed" to (if (currentLang == Language.RU) "Пунктирный визуал" else "Dashed Matrix Wireframe"),
                                    "none" to (if (currentLang == Language.RU) "Без линий (Пространство)" else "Void (No Lines)")
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

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (currentLang == Language.RU) "Интенсивность тряски экрана" else "Screen Shake Intensity",
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
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = if (currentLang == Language.RU) "Только контур призрака" else "Ghost outline only",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = if (currentLang == Language.RU) "Чёткое очертание вместо текстур блоков" else "Clear outline without block textures",
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
                                        text = if (currentLang == Language.RU) "Эффект сканирования (CRT Scanlines)" else "Retro CRT Scanlines Filter",
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

                    2 -> {
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
                                    text = Translations.get("control_buttons_layout", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                val layouts = listOf(
                                    "classic" to (if (currentLang == Language.RU) "Сетка по центру" else "Tactical Grid Centered"),
                                    "split" to (if (currentLang == Language.RU) "Разделенное по краям" else "Divided Fingertip Split"),
                                    "arcade" to (if (currentLang == Language.RU) "Аркадный стиль" else "Arcade Controller style")
                                )
                                layouts.forEach { (key, title) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setControlStyle(key) }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = controlStyle == key,
                                            onClick = { viewModel.setControlStyle(key) },
                                            colors = RadioButtonDefaults.colors(selectedColor = themeColorVal)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = title, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }

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
                                    "neon" to (if (currentLang == Language.RU) "Неоновая рамка" else "Glowing Neon Border"),
                                    "glass" to (if (currentLang == Language.RU) "Матовое стекло" else "Semi-Transparent Glass"),
                                    "classic" to (if (currentLang == Language.RU) "Классический сплошной" else "Classic Material Solid")
                                )
                                val context = androidx.compose.ui.platform.LocalContext.current
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
                                                        if (currentLang == Language.RU) "Купите этот стиль кнопок в магазине!" else "Purchase this button style in the store!",
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
                                                        if (currentLang == Language.RU) "Купите этот стиль кнопок в магазине!" else "Purchase this button style in the store!",
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
                                    0.75f to (if (currentLang == Language.RU) "Легкая (0.75x)" else "Easy (0.75x)"),
                                    1.0f to (if (currentLang == Language.RU) "Норма (1.0x)" else "Normal (1.0x)"),
                                    1.25f to (if (currentLang == Language.RU) "Высокая (1.25x)" else "Hard (1.25x)"),
                                    1.5f to (if (currentLang == Language.RU) "Экстрим (1.5x)" else "Extreme (1.5x)")
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

                    3 -> {
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            AdaptiveText(
                                                text = if (currentLang == Language.RU) "Громкость звуковых эффектов" else "Sound Effects Volume",
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
                                        text = if (currentLang == Language.RU) "Фоновая музыка в лобби" else "Lobby Background Music",
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            AdaptiveText(
                                                text = if (currentLang == Language.RU) "Громкость музыки" else "Lobby Music Volume",
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

                    4 -> {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            onClick = onCustomizeControls
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = if (currentLang == Language.RU) "Кастомизация управления" else "Customize Controls Layout",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = if (currentLang == Language.RU) "Настроить размер, прозрачность, тип и позицию кнопок" else "Configure scale, opacity, style and position of controls"
                                    )
                                },
                                trailingContent = {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }

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
                                    text = if (currentLang == Language.RU) "Вертикальная позиция управления" else "Tactical Controller Vertical position",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                val positions = listOf(
                                    "bottom" to (if (currentLang == Language.RU) "Нижнее положение" else "Lower Edge bottom position"),
                                    "middle" to (if (currentLang == Language.RU) "Центральное положение" else "Comfort Middle height position"),
                                    "top" to (if (currentLang == Language.RU) "Верхнее положение" else "Elevated Top reach position")
                                )
                                positions.forEach { (key, title) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setControlVerticalPosition(key) }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = controlVerticalPosition == key,
                                            onClick = { viewModel.setControlVerticalPosition(key) },
                                            colors = RadioButtonDefaults.colors(selectedColor = themeColorVal)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = title, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }


                                    5 -> {
                        // Language Selection
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

                        // Single Reset Settings Card
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
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "СБРОСИТЬ ВСЕ НАСТРОЙКИ" else "RESET ALL SETTINGS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "Восстановит все параметры приложения по умолчанию" else "Restores all game configurations to factory defaults",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        text = if (currentLang == Language.RU) "СБРОСИТЬ" else "RESET DEFAULTS",
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
            val categoryTitles = when (currentLang) {
                Language.RU -> listOf("ОСНОВНЫЕ", "ВИЗУАЛ", "ГЕЙМПЛЕЙ", "ЗВУК И ВИБРАЦИЯ", "ИНТЕРФЕЙС", "СИСТЕМА")
                Language.DE -> listOf("ALLGEMEIN", "VISUELL", "GAMEPLAY", "AUDIO & VIBRATION", "INTERFACE", "SYSTEM")
                else -> listOf("GENERAL", "VISUALS", "TACTICAL", "AUDIO & VIBRATION", "INTERFACE", "SYSTEM")
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
                        categoryTitles.forEachIndexed { index, title ->
                            val selected = activeCategory == index
                            NavigationDrawerItem(
                                label = {
                                    AdaptiveText(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                selected = selected,
                                onClick = { activeCategory = index },
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
                        categoryTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = activeCategory == index,
                                onClick = { activeCategory = index },
                                text = {
                                    AdaptiveText(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppDialog(
    currentLang: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (currentLang == Language.RU) "О ПРИЛОЖЕНИИ" else "ABOUT APP",
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
                val tabs = listOf(
                    Triple(0, if (currentLang == Language.RU) "Проект" else "About", Icons.Default.Info),
                    Triple(1, if (currentLang == Language.RU) "Спонсоры" else "Sponsors", Icons.Default.Star),
                    Triple(2, if (currentLang == Language.RU) "Команда" else "Team", Icons.Default.People)
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
                                        text = "FsFq Tetris",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "0.93.3 Alpha",
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
                                        Text(
                                            text = if (currentLang == Language.RU) "О ПРОЕКТЕ" else "ABOUT PROJECT",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (currentLang == Language.RU)
                                                "Современная кросс-режимная реализация легендарной классики с онлайн-мультиплеером, кейсами, богатой кастомизацией и системой престижа."
                                                else "Modern multi-mode implementation of the legendary classic featuring real-time multiplayer, loot crates, deep cosmetics customization, and prestige progression.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFF4081).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (currentLang == Language.RU) "ГЕНЕРАЛЬНЫЙ СПОНСОР" else "MAIN SPONSOR",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFFF4081),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }

                                            Text(
                                                text = if (currentLang == Language.RU)
                                                    "Особая благодарность и признательность за неоценимую поддержку развития проекта! ❤️"
                                                    else "Special gratitude and heartfelt thanks for invaluable support of the project! ❤️",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }


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
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = if (currentLang == Language.RU) "АВТОР И РАЗРАБОТЧИК" else "AUTHOR & DEVELOPER",
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
                                        Text(
                                            text = if (currentLang == Language.RU) "ТЕСТИРОВЩИКИ" else "TESTERS / QA",
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
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    onBack()
}

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
fun unusedMultiplayerScreen() {
    /*
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val onlineTier by viewModel.onlineTier.collectAsStateWithLifecycle()
    val onlineRegion by viewModel.onlineRegion.collectAsStateWithLifecycle()
    val themeColorStr by viewModel.themeColor.collectAsStateWithLifecycle()
    val localName by viewModel.playerName.collectAsStateWithLifecycle()
    
    val themeColorVal = when(themeColorStr) {
        "CYAN", "neon" -> Color(0xFF00ADB5)
        "RED", "rose" -> Color(0xFFE94560)
        "VIOLET", "indigo" -> Color(0xFF8C52FF)
        "GOLD", "amber" -> Color(0xFFFFCC00)
        "GREEN", "emerald" -> Color(0xFF00E676)
        else -> Color(0xFF00ADB5)
    }

    val coroutineScope = rememberCoroutineScope()
    var matchState by remember { mutableStateOf<MultiplayerState>(MultiplayerState.IDLE) }
    var matchRunningTime by remember { mutableStateOf(0) }
    
    var roomCodeInput by remember { mutableStateOf("") }
    var regionalPing by remember { mutableStateOf(14) }
    var playerScore by remember { mutableStateOf(0) }
    var playerCombo by remember { mutableStateOf(0) }
    
    var playerGrid by remember { mutableStateOf(List(12) { IntArray(10) }) }
    
    var opponentGridState by remember { mutableStateOf(List(12) { IntArray(10) }) }
    var opponentScoreState by remember { mutableStateOf(0) }
    var opponentGameOver by remember { mutableStateOf(false) }
    
    var connectionLogs by remember { mutableStateOf(listOf<String>()) }
    var systemMessages by remember { mutableStateOf(listOf<String>()) }
    var chatInput by remember { mutableStateOf("") }
    
    var isChatOpen by remember { mutableStateOf(false) }
    var opponentName by remember { mutableStateOf("Volt_Apex_88") }
    var opponentPing by remember { mutableStateOf(20) }
    var countdownValue by remember { mutableStateOf(3) }

    // Fluctuating Regional Ping effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            regionalPing = (12..25).random()
            opponentPing = (15..32).random()
        }
    }

    fun appendLog(msg: String) {
        connectionLogs = connectionLogs + msg
    }

    // Instanciate our robust, live P2PConnectionManager
    val connectionManager = remember {
        P2PConnectionManager(
            onAttackReceived = { linesCount ->
                viewModel.triggerAudioFeedback("gameover")
                val newGrid = playerGrid.map { it.clone() }.toMutableList()
                for (i in 0 until linesCount) {
                    if (newGrid.isNotEmpty()) {
                        newGrid.removeAt(0) // drop top row
                    }
                    val garbageRow = IntArray(10) { 1 }
                    garbageRow[(0..9).random()] = 0 // insert hole
                    newGrid.add(garbageRow)
                }
                playerGrid = newGrid
                systemMessages = systemMessages + (
                    if (currentLang == Language.RU) "Оппонент отправил тебе $linesCount линий мусора!"
                    else "Opponent sent $linesCount garbage line(s) to you!"
                )
            },
            onOpponentStateReceived = { grid, score, isGameOver ->
                opponentGridState = grid
                opponentScoreState = score
                opponentGameOver = isGameOver
                if (isGameOver) {
                    systemMessages = systemMessages + (
                        if (currentLang == Language.RU) "Оппонент выбыл! Ты на высоте!"
                        else "Opponent's grid is full! You dominate!"
                    )
                }
            },
            onStatusChanged = { status ->
                // Handled in sub checks
            },
            onChatReceived = { sender, text ->
                systemMessages = systemMessages + "$sender: $text"
            },
            onDisconnected = {
                matchState = MultiplayerState.IDLE
            }
        )
    }

    // Capture lifecycle connections cleanly
    val managerStatus by connectionManager.status.collectAsStateWithLifecycle()
    val managerRoomId by connectionManager.roomId.collectAsStateWithLifecycle()
    val managerOpponentName by connectionManager.opponentName.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            connectionManager.disconnect()
        }
    }

    // Drive local Game Timer and countdown when connected
    LaunchedEffect(managerStatus) {
        if (managerStatus == P2PStatus.CONNECTED) {
            matchState = MultiplayerState.CONNECTED
            countdownValue = 3
            systemMessages = systemMessages + (
                if (currentLang == Language.RU) "[Коннект] P2P тоннель установлен с $managerOpponentName! (Пинг: ${opponentPing}ms)"
                else "[Connect] P2P tunnel secured with $managerOpponentName! (Ping: ${opponentPing}ms)"
            )
            viewModel.triggerAudioFeedback("start")
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            
            playerScore = 0
            playerCombo = 0
            playerGrid = List(12) { IntArray(10) }
            opponentGridState = List(12) { IntArray(10) }
            opponentScoreState = 0
            opponentGameOver = false
            matchRunningTime = 0
            
            matchState = MultiplayerState.PLAYING
            systemMessages = systemMessages + (
                if (currentLang == Language.RU) "БИТВА НАЧАЛАСЬ! Кликни на сетку, чтобы бросать блоки!"
                else "MATCH STARTED! Click columns on the board to drop blocks!"
            )
        } else if (managerStatus == P2PStatus.DISCONNECTED) {
            matchState = MultiplayerState.IDLE
        } else if (managerStatus == P2PStatus.ERROR) {
            matchState = MultiplayerState.IDLE
            systemMessages = systemMessages + "Connection error! Dropped back to lobby."
        }
    }

    // Match Timeline and bot action logic
    LaunchedEffect(matchState) {
        if (matchState == MultiplayerState.GAME_OVER) {
            val playerWins = playerScore >= opponentScoreState && !playerGrid[0].any { it > 0 }
            viewModel.awardMultiplayerCredits(playerScore, opponentScoreState, playerWins)
        }
        if (matchState == MultiplayerState.PLAYING) {
            while (matchState == MultiplayerState.PLAYING) {
                delay(1000)
                matchRunningTime++
                
                // Keep network sync'd
                connectionManager.sendStateUpdate(playerGrid, playerScore, false)
                
                // Emulate occasional action if opponent is an AI Bot
                if (managerOpponentName.contains("Bot") || managerOpponentName.contains("Alpha")) {
                    if ((1..100).random() < 22) {
                        val tempOpGrid = opponentGridState.map { it.clone() }.toMutableList()
                        val randomCol = (0..9).random()
                        var lowest = -1
                        for (r in 11 downTo 0) {
                            if (tempOpGrid[r][randomCol] == 0) {
                                lowest = r
                                break
                            }
                        }
                        if (lowest != -1) {
                            tempOpGrid[lowest][randomCol] = (1..3).random()
                            opponentGridState = tempOpGrid
                            opponentScoreState += (20..50).random()
                        }
                    }
                }
                
                if (matchRunningTime >= 95) {
                    matchState = MultiplayerState.GAME_OVER
                }
            }
        }
    }

    // Interactive drop logic
    fun dropBlockInColumn(col: Int) {
        if (matchState != MultiplayerState.PLAYING) return
        val newGrid = playerGrid.map { it.clone() }.toMutableList()
        // Find the lowest empty row (value == 0) in this column
        var lowestRow = -1
        for (r in 11 downTo 0) {
            if (newGrid[r][col] == 0) {
                lowestRow = r
                break
            }
        }
        if (lowestRow != -1) {
            val blockColor = (1..3).random()
            newGrid[lowestRow][col] = blockColor
            
            // Check for completed rows
            val rowsToRemove = mutableListOf<Int>()
            for (r in 0..11) {
                if (newGrid[r].all { it > 0 }) {
                    rowsToRemove.add(r)
                }
            }
            
            if (rowsToRemove.isNotEmpty()) {
                // Remove rows and shift down
                for (r in rowsToRemove) {
                    newGrid.removeAt(r)
                    newGrid.add(0, IntArray(10) { 0 })
                }
                playerScore += 160 * rowsToRemove.size
                playerCombo++
                viewModel.triggerAudioFeedback("clear")
                connectionManager.sendAttackTrigger(rowsToRemove.size)
                systemMessages = systemMessages + (
                    if (currentLang == Language.RU) "Очистка! Ты срезал ряд! (+${160 * rowsToRemove.size})"
                    else "Hit! Row cleared! (+${160 * rowsToRemove.size})"
                )
            } else {
                viewModel.triggerAudioFeedback("move")
            }
            
            playerGrid = newGrid
            connectionManager.sendStateUpdate(playerGrid, playerScore, false)
        } else {
            // Grid full -> Local Game Over!
            viewModel.triggerAudioFeedback("gameover")
            systemMessages = systemMessages + (
                if (currentLang == Language.RU) "Сетка переполнена! ИГРА ОКОНЧЕНА!"
                else "Grid full! GAME OVER!"
            )
            matchState = MultiplayerState.GAME_OVER
            connectionManager.sendStateUpdate(playerGrid, playerScore, true)
        }
    }

    val multiplayerGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0C090F),
            Color(0xFF13111A),
            themeColorVal.copy(alpha = 0.08f),
            Color(0xFF0B090C)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(multiplayerGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            // TOP HEADBAR WITH BACK TOGGLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        connectionManager.disconnect()
                        onBack()
                    },
                    modifier = Modifier.background(Color(0xFF181922), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = themeColorVal
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (currentLang == Language.RU) "ПИНГ РЕГИОНА" else "REGIONAL PING",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$regionalPing ms ($onlineRegion)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (regionalPing < 20) Color(0xFF00FFCC) else themeColorVal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // CONSOLE STATUS / MAIN CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, themeColorVal, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181922).copy(alpha = 0.9f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (managerStatus == P2PStatus.CONNECTED) Color.Green else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == Language.RU) {
                            "СОКЕТ СТАТУС: ${managerStatus.name}"
                        } else {
                            "SOCKET STATUS: ${managerStatus.name}"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            when (matchState) {
                MultiplayerState.IDLE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentLang == Language.RU) "ЛИГА МУЛЬТИПЛЕЕРА" else "CONCENSUS BATTLEGROUND",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentLang == Language.RU) "Реальные игры с игроками по всему миру" else "Pure WebSocket-driven global matches",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(30.dp))
                        
                        // HOST GAME UNIT
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.2.dp, Brush.horizontalGradient(
                                    colors = listOf(themeColorVal, themeColorVal.copy(alpha = 0.3f), themeColorVal)
                                ), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF110E22)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (currentLang == Language.RU) "1. СОЗДАТЬ СВОЕ ЛОББИ" else "OPTION A: HOST NEW GAME",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (currentLang == Language.RU) "Создайте комнату и сообщите друг-код вашему оппоненту." else "Spin up a live room and share code with a friend.",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        connectionManager.hostRoom(localName)
                                        matchState = MultiplayerState.SEARCHING
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColorVal),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "ОТКРЫТЬ КАНАЛ ВЕЩАНИЯ" else "OPEN TRANSMISSION CHANNEL",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // JOIN GAME UNIT
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.2.dp, Brush.horizontalGradient(
                                    colors = listOf(themeColorVal.copy(alpha = 0.3f), themeColorVal, themeColorVal.copy(alpha = 0.3f))
                                ), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF110E22)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (currentLang == Language.RU) "2. ПОДКЛЮЧИТЬСЯ К ДРУГУ" else "OPTION B: JOIN EXISTING CHANNEL",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = roomCodeInput,
                                    onValueChange = { roomCodeInput = it.uppercase() },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(if (currentLang == Language.RU) "Введите Room код (ROOM-XXXX)" else "Target Room Code (ROOM-XXXX)", fontSize = 11.sp) },
                                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = themeColorVal,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (roomCodeInput.isNotBlank()) {
                                            connectionManager.joinRoom(roomCodeInput, localName)
                                            matchState = MultiplayerState.SEARCHING
                                        }
                                    },
                                    enabled = roomCodeInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeColorVal,
                                        disabledContainerColor = themeColorVal.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "ВНЕДРИТЬСЯ В КАНАЛ" else "INJECT INTO CHANNEL",
                                        fontWeight = FontWeight.Bold,
                                        color = if (roomCodeInput.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
                    
                    MultiplayerState.SEARCHING -> {
                        // Diagnostic network console for P2P connection
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CONNECTING WEBSOCKET/P2P REAL-TIME TERMINAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, themeColorVal)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxSize()
                                ) {
                                    items(connectionLogs) { log ->
                                        Text(
                                            text = log,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (log.contains("DATA CHANNEL")) Color.Green else Color.Cyan,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = themeColorVal, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (currentLang == Language.RU) "СИНХРОНИЗАЦИЯ С УЗЛОМ PEERJS..." else "ESTABLISHING ICE CONNECTIVITY...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    MultiplayerState.CONNECTED -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "P2P СОКЕТ УСТАНОВЛЕН!" else "WebRTC P2P PIPELINE IS GO!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColorVal
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(themeColorVal.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VideogameAsset,
                                            contentDescription = null,
                                            tint = themeColorVal,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(if (currentLang == Language.RU) "ТЫ" else "YOU", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(onlineTier, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                
                                Text("P2P", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Computer,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(opponentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("PEER CHALLENGER", fontSize = 9.sp, color = themeColorVal)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(30.dp))
                            
                            Text(
                                text = countdownValue.toString(),
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColorVal
                            )
                        }
                    }
                    
                    MultiplayerState.PLAYING, MultiplayerState.GAME_OVER -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (matchState == MultiplayerState.PLAYING) Color.Green else Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLang == Language.RU) "ОНЛАЙН СХВАТКА" else "LIVE SOCKET FIGHT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = { isChatOpen = !isChatOpen },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isChatOpen) themeColorVal else Color(0xFF1F202D)
                                    ),
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "ЧАТ" else "CHAT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChatOpen) Color.Black else Color.White
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Interactive guidance hint
                            if (matchState == MultiplayerState.PLAYING) {
                                Text(
                                    text = if (currentLang == Language.RU) "НАЖИМАЙТЕ НА КОЛОНКИ СЕТКИ, ЧТОБЫ БРОСАТЬ БЛОКИ!" else "TAP MINIBOARD COLUMNS TO DROP STACK BLOCKS!",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal.copy(alpha = 0.8f),
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp)
                                )
                            }
                            
                            // Dual Stacking boards side by side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MiniBoard(
                                    grid = playerGrid,
                                    title = if (currentLang == Language.RU) "ТЫ ($localName)" else "YOU ($localName)",
                                    score = playerScore,
                                    combo = playerCombo,
                                    onColumnClick = { colIndex ->
                                        if (matchState == MultiplayerState.PLAYING) {
                                            // Handle block drop
                                            val currentGrid = playerGrid.map { it.clone() }.toMutableList()
                                            var placedRow = -1
                                            for (r in 11 downTo 0) {
                                                if (currentGrid[r][colIndex] == 0) {
                                                    currentGrid[r][colIndex] = (1..3).random()
                                                    placedRow = r
                                                    break
                                                }
                                            }
                                            
                                            if (placedRow != -1) {
                                                playerGrid = currentGrid
                                                viewModel.triggerAudioFeedback("tap")
                                                
                                                // Check for row completion
                                                var linesCleared = 0
                                                val finalGrid = mutableListOf<IntArray>()
                                                for (r in 0..11) {
                                                    val row = currentGrid[r]
                                                    val isFull = row.all { it > 0 }
                                                    if (isFull) {
                                                        linesCleared++
                                                    } else {
                                                        finalGrid.add(row.clone())
                                                    }
                                                }
                                                while (finalGrid.size < 12) {
                                                    finalGrid.add(0, IntArray(10))
                                                }
                                                
                                                if (linesCleared > 0) {
                                                    playerScore += linesCleared * 100
                                                    playerCombo += linesCleared
                                                    viewModel.triggerAudioFeedback("clear")
                                                    playerGrid = finalGrid
                                                    // Trigger attack send to the opponent
                                                    connectionManager.sendAttackTrigger(linesCleared)
                                                    systemMessages = systemMessages + (
                                                        if (currentLang == Language.RU) "Очистка! Ты отправил $linesCleared линий мусора!"
                                                        else "Row match! Sent $linesCleared lines of garbage!"
                                                    )
                                                } else {
                                                    playerGrid = finalGrid
                                                }
                                                
                                                // Broadcast update
                                                connectionManager.sendStateUpdate(playerGrid, playerScore, false)
                                            } else {
                                                // Overflow checking
                                                viewModel.triggerAudioFeedback("gameover")
                                                matchState = MultiplayerState.GAME_OVER
                                                connectionManager.sendStateUpdate(playerGrid, playerScore, true)
                                                systemMessages = systemMessages + (
                                                    if (currentLang == Language.RU) "Сетка переполнена! Игра Окончена!"
                                                    else "Stack overflow! Game Over!"
                                                )
                                            }
                                        }
                                    }
                                )
                                
                                MiniBoard(
                                    grid = opponentGridState,
                                    title = managerOpponentName.ifBlank { opponentName },
                                    score = opponentScoreState,
                                    combo = 0,
                                    onColumnClick = null
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Realtime actions / manual garbage cheat helper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        if (matchState == MultiplayerState.PLAYING) {
                                            viewModel.triggerAudioFeedback("clear")
                                            // Send random line attack to verify socket
                                            connectionManager.sendAttackTrigger(1)
                                            systemMessages = systemMessages + (
                                                if (currentLang == Language.RU) "Ручная атака: отправлена линия мусора!"
                                                else "Manual action: sent garbage line to opponent!"
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F202D)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "ПОДБРОСИТЬ МУСОР" else "MANUAL Attack",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColorVal
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        if (matchState == MultiplayerState.PLAYING) {
                                            // Escape match or simulated self-hit to trigger fallbacks
                                            viewModel.triggerAudioFeedback("gameover")
                                            matchState = MultiplayerState.GAME_OVER
                                            connectionManager.sendStateUpdate(playerGrid, playerScore, true)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF291E23)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "СДАТЬСЯ" else "SURRENDER战",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE94560)
                                    )
                                }
                            }
                            
                            if (matchState == MultiplayerState.GAME_OVER) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = themeColorVal.copy(alpha = 0.12f)),
                                    border = BorderStroke(1.dp, themeColorVal)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val playerWins = playerScore >= opponentScoreState && !playerGrid[0].any { it > 0 }
                                        val textWins = if (currentLang == Language.RU) {
                                            if (playerWins) "ПОБЕДА! ВЫ ОПЕРЕДИЛИ ОППОНЕНТА!" else "ИГРА ЗАВЕРШЕНА"
                                        } else {
                                            if (playerWins) "TRIUMPH! YOU DOMINATE THE DUEL!" else "COMBAT COMPLETED"
                                        }
                                        Text(textWins, fontWeight = FontWeight.Black, fontSize = 13.sp, color = themeColorVal)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("YOUR: $playerScore VS Opposition: $opponentScoreState", fontSize = 10.sp, color = Color.White)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                matchState = MultiplayerState.IDLE
                                                connectionManager.disconnect()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColorVal),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Text("GO TO LOBBY HUB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // FLOATING OVERLAY CHAT DRAWER ACCESSIBLE VIA BUTTON
                if ((matchState == MultiplayerState.PLAYING || matchState == MultiplayerState.GAME_OVER) && isChatOpen) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(vertical = 8.dp)
                            .imePadding()
                            .border(1.dp, themeColorVal.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.92f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE WebRTC CHANNEL CHAT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                IconButton(onClick = { isChatOpen = false }) {
                                    Text("✕", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                reverseLayout = true
                            ) {
                                items(systemMessages.reversed()) { msg ->
                                    Text(
                                        text = msg,
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = if (msg.startsWith("[")) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (msg.contains("Ты") || msg.contains("You") || msg.contains("Ты очистил") || msg.contains("РАУНД НАЧАЛСЯ")) themeColorVal else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatInput, 
                                    onValueChange = { chatInput = it }, 
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Type message...", fontSize = 11.sp) },
                                    textStyle = TextStyle(fontSize = 11.sp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (chatInput.isNotBlank()) {
                                            systemMessages = systemMessages + "You: $chatInput"
                                            chatInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColorVal),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text("SEND", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    */
}

// Visual mini board grid helper with complete 20-row full height and color palette support
@Composable
fun MiniBoard(
    grid: List<IntArray>,
    title: String,
    score: Int,
    combo: Int,
    lines: Int = 0,
    onColumnClick: ((Int) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF08080C)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            // Complete 10x20 full-height matrix representation with all pieces and colors
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F0F16))
                    .padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                grid.forEach { row ->
                    Row {
                        row.forEachIndexed { cellIndex, cell ->
                            val color = when {
                                cell in 1 until com.example.game.Colors.size -> com.example.game.Colors[cell]
                                cell > 0 -> Color(0xFFB0BEC5)
                                else -> Color(0xFF161620)
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.4.dp)
                                    .padding(0.4.dp)
                                    .background(color, RoundedCornerShape(1.dp))
                                    .clickable(enabled = onColumnClick != null) {
                                        onColumnClick?.invoke(cellIndex)
                                    }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$score 🪙",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (lines > 0) {
                    Text(
                        text = "$lines ⚡",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else if (combo > 0) {
                    Text(
                        text = "x$combo 🔥",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFB300)
                    )
                }
            }
        }
    }
}

// ── Full-Screen Mode Selection Screen ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onPlayMode: (com.example.game.GameMode) -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    var lockedModeWarning by remember { mutableStateOf<String?>(null) }

    val purchasedModesSet = remember(credits) {
        val sharedPrefs = viewModel.getApplication<android.app.Application>()
            .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.getStringSet("purchased_modes", setOf("classic", "extended", "fast_run", "reverse", "block_blast"))
            ?: setOf("classic", "extended", "fast_run", "reverse", "block_blast")
    }

    data class ModeInfo(
        val mode: com.example.game.GameMode,
        val modeId: String,
        val title: String,
        val description: String,
        val howToPlay: String,
        val cost: Int,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val categoryTag: String // "FREE", "PAID", "HARD", "FUN"
    )

    val modes = listOf(
        ModeInfo(
            com.example.game.GameMode.CLASSIC, "classic",
            if (currentLang == Language.RU) "Классический" else "Classic",
            if (currentLang == Language.RU) "Оригинальный режим с нарастающей сложностью." else "The original experience with increasing speed.",
            if (currentLang == Language.RU) "Укладывайте фигурки и заполняйте горизонтальные линии. Каждые 10 очищенных линий повышают уровень и скорость игры." else "Fit falling blocks to clear horizontal lines. Every 10 cleared lines increases game level and falling speed.",
            0, Icons.Default.VideogameAsset, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.TIME_ATTACK, "time_attack",
            if (currentLang == Language.RU) "Тайм-Атак" else "Time Attack",
            if (currentLang == Language.RU) "Режим с ограничением времени: старт с 60 секунд." else "Time-limited mode: starts at 60 seconds.",
            if (currentLang == Language.RU) "Игра начинается с 60 секундами на таймере. Очищайте линии, чтобы прибавлять по 10 секунд за каждую линию. Время стремительно иссекает!" else "Starts with 60s on the clock. Clear lines to gain +10 seconds per line. Score as much as possible before time runs out!",
            0, Icons.Default.Schedule, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.EXTENDED, "extended",
            if (currentLang == Language.RU) "Пентатрис" else "Pentatris",
            if (currentLang == Language.RU) "Игра фигурами из пяти блоков для повышенной сложности." else "Gameplay using five-block pieces for an extra challenge.",
            if (currentLang == Language.RU) "Режим классического тетриса, но с использованием 5-блочных фигур (пентамино). Заполняйте линии в условиях высокой плотности фигур." else "Classic tetris rules, but played with 5-block pentamino pieces. Fill rows with complex shapes.",
            250, Icons.Default.Star, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.FAST_RUN, "fast_run",
            if (currentLang == Language.RU) "Быстрый старт" else "Fast Start",
            if (currentLang == Language.RU) "Начало игры с 10-го уровня сложности." else "Starts the game at Level 10 difficulty.",
            if (currentLang == Language.RU) "Игра сразу начинается на 10 уровне скорости! Требует молниеносной реакции и быстрого принятия решений." else "Game starts immediately at speed level 10! Requires lightning reflexes and fast placement decisions.",
            400, Icons.Default.FlashOn, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.ZEN_FLOW, "zen",
            if (currentLang == Language.RU) "Дзен" else "Zen Cosmic Flow",
            if (currentLang == Language.RU) "Бесконечный режим: очистка при переполнении, без проигрыша." else "Endless game mode: clears board on overflow, no defeat.",
            if (currentLang == Language.RU) "Режим без проигрыша и спешки. При достижении верха поля нижние линии автоматически очищаются." else "Endless mode with no loss. When reaching the top, bottom lines auto-clear, allowing calm and endless relaxation.",
            400, Icons.Default.Spa, "FUN"
        ),
        ModeInfo(
            com.example.game.GameMode.REVERSE_CONTROLS, "reverse",
            if (currentLang == Language.RU) "Инверсия" else "Inverted Controls",
            if (currentLang == Language.RU) "Классический режим с инвертированным управлением." else "Classic gameplay with inverted directional controls.",
            if (currentLang == Language.RU) "Кнопки Влево и Вправо поменяны местами! Попробуйте перестроить привычки мышления во время игры." else "Left and Right control buttons are inverted! Test your muscle memory and brain adaptability under pressure.",
            500, Icons.Default.Visibility, "FUN"
        ),
        ModeInfo(
            com.example.game.GameMode.BLOCK_BLAST, "block_blast",
            if (currentLang == Language.RU) "Головоломка ZETA" else "ZETA Puzzle",
            if (currentLang == Language.RU) "Свободное размещение фигурок на игровом поле." else "Free placement of polyominos on the board.",
            if (currentLang == Language.RU) "Перетаскивайте и ставьте процедурно сгенерированные блоки в любую свободную область поля для сбора линий." else "Drag and place procedurally generated blocks anywhere on the grid to clear rows and columns.",
            600, Icons.Default.Computer, "FUN"
        ),
        ModeInfo(
            com.example.game.GameMode.PULSE_EXTREME, "pulse_extreme",
            if (currentLang == Language.RU) "Импульсный Вихрь" else "Vortex Pulse",
            if (currentLang == Language.RU) "Каждые 4 фигуры снизу поднимается новая мусорная линия." else "Garbage line is added at bottom every 4 placed pieces.",
            if (currentLang == Language.RU) "Экстремальный режим! Каждые 4 установленных блока снизу поля выталкивается неполная мусорная линия." else "Extreme challenge! Every 4 dropped blocks forces a random garbage line to emerge from the bottom.",
            800, Icons.Default.Bolt, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.MIRROR_DIMENSION, "mirror",
            if (currentLang == Language.RU) "Зеркальный Мир" else "Mirror Dimension",
            if (currentLang == Language.RU) "Поле отражается по горизонтали во время игры." else "The game field is mirrored horizontally during gameplay.",
            if (currentLang == Language.RU) "Игровое поле и падающие блоки периодически отражаются зеркально по горизонтали!" else "The board periodically flips horizontally, challenging your spatial orientation.",
            1000, Icons.Default.SwapHoriz, "FUN"
        ),
        ModeInfo(
            com.example.game.GameMode.PENTARY_CHAOS, "penta",
            if (currentLang == Language.RU) "Пента-Хаос" else "Pentary Chaos",
            if (currentLang == Language.RU) "Все падающие фигуры состоят из пяти блоков." else "All falling pieces consist of five blocks.",
            if (currentLang == Language.RU) "Максимальная сложность! Все фигуры представляют собой сложные пентамино (5 блоков)." else "Ultimate difficulty! Every falling piece is a complex pentamino (5 blocks), requiring strategic grid planning.",
            1200, Icons.Default.AutoAwesome, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.RELAX, "relax",
            if (currentLang == Language.RU) "Релакс-Песочница" else "Relax Sandbox",
            if (currentLang == Language.RU) "Настраиваемый режим с выбором блоков и скоростей." else "Customizable sandbox: select blocks and speed.",
            if (currentLang == Language.RU) "Песочница с гибкой настройкой параметров игры под ваше настроение." else "A sandbox mode allowing full customization of gameplay rules to match your preference.",
            0, Icons.Default.Spa, "FREE"
        )
    )

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedInfoMode by remember { mutableStateOf<ModeInfo?>(null) }

    val filterChips = listOf(
        "ALL" to (if (currentLang == Language.RU) "Все" else "All"),
        "FREE" to (if (currentLang == Language.RU) "Бесплатные" else "Free"),
        "PAID" to (if (currentLang == Language.RU) "Платные" else "Paid"),
        "HARD" to (if (currentLang == Language.RU) "Сложные" else "Hard"),
        "FUN" to (if (currentLang == Language.RU) "Развлечение" else "Casual / Fun")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(top = 8.dp),
                title = {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "ВЫБОР РЕЖИМА" else "SELECT MODE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier.padding(end = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Credits",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AdaptiveText(
                                text = "$credits",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
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

        val filteredModes = remember(selectedCategoryFilter, modes, purchasedModesSet) {
            when (selectedCategoryFilter) {
                "FREE" -> modes.filter { it.cost == 0 }
                "PAID" -> modes.filter { it.cost > 0 }
                "HARD" -> modes.filter { it.categoryTag == "HARD" }
                "FUN" -> modes.filter { it.categoryTag == "FUN" || it.categoryTag == "SPECIAL" }
                else -> modes
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Chips bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterChips) { (tag, label) ->
                    val isSelected = selectedCategoryFilter == tag
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            selectedCategoryFilter = tag
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColorVal,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cards list with animated filter transitions
            AnimatedContent(
                targetState = selectedCategoryFilter,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(260)) +
                        slideInVertically(animationSpec = tween(300)) { it / 12 })
                        .togetherWith(fadeOut(animationSpec = tween(200)))
                },
                label = "modeFilterTransition"
            ) { currentFilter ->
                val animFilteredModes = remember(currentFilter, modes, purchasedModesSet) {
                    when (currentFilter) {
                        "FREE" -> modes.filter { it.cost == 0 }
                        "PAID" -> modes.filter { it.cost > 0 }
                        "HARD" -> modes.filter { it.categoryTag == "HARD" }
                        "FUN" -> modes.filter { it.categoryTag == "FUN" || it.categoryTag == "SPECIAL" }
                        else -> modes
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (animFilteredModes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "Нет режимов в этой категории" else "No modes found in this category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    animFilteredModes.forEachIndexed { index, modeInfo ->
                        val isPurchased = purchasedModesSet.contains(modeInfo.modeId)
                        val isPlayable = isPurchased || modeInfo.cost == 0

                        // Staggered card entrance animation
                        var appeared by remember { mutableStateOf(false) }
                        LaunchedEffect(currentFilter) {
                            appeared = false
                            kotlinx.coroutines.delay(index * 50L)
                            appeared = true
                        }
                        val cardAlpha by animateFloatAsState(
                            targetValue = if (appeared) 1f else 0f,
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                            label = "cardAlpha_$index"
                        )
                        val cardOffsetY by animateDpAsState(
                            targetValue = if (appeared) 0.dp else 24.dp,
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                            label = "cardOffset_$index"
                        )

                        ElevatedCard(
                            onClick = {
                                if (isPlayable) {
                                    viewModel.triggerAudioFeedback("click")
                                    onPlayMode(modeInfo.mode)
                                } else {
                                    if (credits >= modeInfo.cost) {
                                        val updated = purchasedModesSet.toMutableSet().apply { add(modeInfo.modeId) }
                                        viewModel.spendCredits(modeInfo.cost)
                                        val sharedPrefs = viewModel.getApplication<android.app.Application>()
                                            .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
                                        sharedPrefs.edit().putStringSet("purchased_modes", updated).apply()
                                        viewModel.triggerAudioFeedback("buy")
                                        lockedModeWarning = if (currentLang == Language.RU) "Режим разблокирован!" else "Game Mode acquired!"
                                    } else {
                                        viewModel.triggerAudioFeedback("error")
                                        lockedModeWarning = if (currentLang == Language.RU) "Недостаточно кредитов!" else "Insufficient credits!"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = cardAlpha
                                    translationY = cardOffsetY.toPx()
                                },
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isPlayable) MaterialTheme.colorScheme.surfaceContainerHigh
                                                 else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = if (isPlayable) 3.dp else 1.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mode Icon Avatar
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isPlayable) themeColorVal.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    tonalElevation = 1.dp
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (isPlayable) {
                                            Icon(
                                                imageVector = modeInfo.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp),
                                                tint = themeColorVal
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                // Title & 2nd line tags
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AdaptiveText(
                                        text = modeInfo.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isPlayable) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )

                                    // Second line tags chips for all modes
                                    val modeTags = remember(modeInfo, isPurchased, currentLang) {
                                        val list = mutableListOf<Pair<String, Color>>()
                                        if (modeInfo.cost == 0) {
                                            list.add((if (currentLang == Language.RU) "БЕСПЛАТНО" else "FREE") to themeColorVal)
                                        } else if (isPurchased) {
                                            list.add((if (currentLang == Language.RU) "КУПЛЕНО" else "OWNED") to Color(0xFF4CAF50))
                                        } else {
                                            list.add("${modeInfo.cost} " + (if (currentLang == Language.RU) "КР." else "CR") to Color(0xFFFFB300))
                                        }

                                        when (modeInfo.categoryTag) {
                                            "HARD" -> list.add((if (currentLang == Language.RU) "СЛОЖНЫЙ" else "HARD") to Color(0xFFFF5252))
                                            "FUN" -> list.add((if (currentLang == Language.RU) "ФАН" else "CASUAL") to Color(0xFF00E5FF))
                                            else -> list.add((if (currentLang == Language.RU) "СТАНДАРТ" else "STANDARD") to themeColorVal)
                                        }
                                        list
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        modeTags.forEach { (tagText, tagColor) ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = tagColor.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = tagText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = tagColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Info "i" button without grey border/background
                                IconButton(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        selectedInfoMode = modeInfo
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Mode Info",
                                        tint = themeColorVal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom action bar inside card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPlayable) {
                                    Button(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            onPlayMode(modeInfo.mode)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColorVal,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        AdaptiveText(
                                            text = if (currentLang == Language.RU) "ИГРАТЬ" else "PLAY",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = {
                                            if (credits >= modeInfo.cost) {
                                                val updated = purchasedModesSet.toMutableSet().apply { add(modeInfo.modeId) }
                                                viewModel.spendCredits(modeInfo.cost)
                                                val sharedPrefs = viewModel.getApplication<android.app.Application>()
                                                    .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
                                                sharedPrefs.edit().putStringSet("purchased_modes", updated).apply()
                                                viewModel.triggerAudioFeedback("buy")
                                                lockedModeWarning = if (currentLang == Language.RU) "Режим разблокирован!" else "Game Mode acquired!"
                                            } else {
                                                viewModel.triggerAudioFeedback("error")
                                                lockedModeWarning = if (currentLang == Language.RU) "Недостаточно кредитов!" else "Insufficient credits!"
                                            }
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFFFB300).copy(alpha = 0.18f),
                                            contentColor = Color(0xFFFF8F00)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = Color(0xFFFF8F00),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        AdaptiveText(
                                            text = "${if (currentLang == Language.RU) "ОТКРЫТЬ ЗА" else "UNLOCK FOR"} ${modeInfo.cost} 🪙",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold
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

    // Info Dialog showing detailed rules/explanation for the selected mode
    selectedInfoMode?.let { info ->
        AlertDialog(
            onDismissRequest = { selectedInfoMode = null },
            icon = {
                Icon(
                    imageVector = info.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (info.cost == 0) {
                                    if (currentLang == Language.RU) "Бесплатный режим" else "Free Mode"
                                } else {
                                    "${if (currentLang == Language.RU) "Стоимость:" else "Cost:"} ${info.cost} 🪙"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (info.cost == 0) MaterialTheme.colorScheme.primary else Color(0xFFFF8F00)
                            )
                        }
                    }
                    Text(
                        text = info.howToPlay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedInfoMode = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (currentLang == Language.RU) "ПОНЯТНО" else "GOT IT")
                }
            }
        )
    }

    if (lockedModeWarning != null) {
        AlertDialog(
            onDismissRequest = { lockedModeWarning = null },
            title = { AdaptiveText(if (currentLang == Language.RU) "Внимание" else "System Alert") },
            text = { AdaptiveText(lockedModeWarning ?: "", maxLines = 3) },
            confirmButton = {
                TextButton(onClick = { lockedModeWarning = null }) {
                    AdaptiveText("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomControlsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val themeColorKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeColorVal = remember(themeColorKey) {
        when (themeColorKey) {
            "indigo" -> Color(0xFFD0BCFF)
            "red" -> Color(0xFFFF5555)
            "neon" -> Color(0xFF00FFCC)
            "amber" -> Color(0xFFF59E0B)
            "rose" -> Color(0xFFF43F5E)
            "sky" -> Color(0xFF0EA5E9)
            "cyber_pink" -> Color(0xFFFF007F)
            "toxic_green" -> Color(0xFF39FF14)
            else -> Color(0xFFD0BCFF)
        }
    }

    val gameState by viewModel.gameEngine.gameState.collectAsStateWithLifecycle()
    val controlButtonScale by viewModel.controlButtonScale.collectAsStateWithLifecycle()
    val controlButtonAlpha by viewModel.controlButtonAlpha.collectAsStateWithLifecycle()
    val controlStyle by viewModel.controlStyle.collectAsStateWithLifecycle()
    val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()
    val controlVerticalPosition by viewModel.controlVerticalPosition.collectAsStateWithLifecycle()

    // Start CLASSIC mode sandbox game when entering CustomControlsScreen
    LaunchedEffect(Unit) {
        viewModel.startGame(com.example.game.GameMode.CLASSIC)
    }

    // Auto clear grid when it fills up to allow infinite control testing without gameover interruption
    LaunchedEffect(gameState) {
        if (gameState.isGameOver) {
            viewModel.startGame(com.example.game.GameMode.CLASSIC)
        } else {
            val hasBlocksInUpperGrid = (0..16).any { r ->
                gameState.grid[r].any { it != 0 }
            }
            if (hasBlocksInUpperGrid) {
                viewModel.clearRelaxBoard()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (currentLang == Language.RU) "КАСТОМИЗАЦИЯ УПРАВЛЕНИЯ" else "CUSTOMIZE CONTROLS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Config Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Size slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "Размер кнопок" else "Button Size",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${(controlButtonScale * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                color = themeColorVal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Slider(
                            value = controlButtonScale,
                            onValueChange = { viewModel.setControlButtonScale(it) },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                        )
                    }

                    // Transparency slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "Прозрачность кнопок" else "Button Transparency",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${(controlButtonAlpha * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                color = themeColorVal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Slider(
                            value = controlButtonAlpha,
                            onValueChange = { viewModel.setControlButtonAlpha(it) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                        )
                    }

                    // Quick layout options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.setLeftHandedControls(!leftHandedControls)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (leftHandedControls) themeColorVal else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "ЛЕВША" else "LEFT HAND",
                                fontWeight = FontWeight.Bold,
                                color = if (leftHandedControls) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                val nextStyle = when (controlStyle) {
                                    "buttons" -> "arcade"
                                    "arcade" -> "split"
                                    "split" -> "swipe"
                                    else -> "buttons"
                                }
                                viewModel.setControlStyle(nextStyle)
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColorVal, contentColor = Color.Black)
                        ) {
                            Text(
                                text = controlStyle.uppercase(),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                val nextPos = when (controlVerticalPosition) {
                                    "bottom" -> "middle"
                                    "middle" -> "top"
                                    else -> "bottom"
                                }
                                viewModel.setControlVerticalPosition(nextPos)
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = controlVerticalPosition.uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Sandbox board game view & controls layout
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.5f)
                            .border(1.5.dp, themeColorVal.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        GameBoardView(
                            gameState = gameState,
                            blockStyle = viewModel.blockStyle.collectAsStateWithLifecycle().value,
                            ghostVisible = viewModel.ghostVisible.collectAsStateWithLifecycle().value,
                            ghostOutlineOnly = viewModel.ghostOutlineOnly.collectAsStateWithLifecycle().value,
                            smoothFallingEnabled = viewModel.smoothFallingEnabled.collectAsStateWithLifecycle().value,
                            gridLineDensity = viewModel.gridLineDensity.collectAsStateWithLifecycle().value,
                            boardColorSkin = viewModel.themeColor.collectAsStateWithLifecycle().value
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        GameControlsSection(
                            viewModel = viewModel,
                            gameState = gameState,
                            controlStyle = controlStyle,
                            leftHandedControls = leftHandedControls,
                            controlVerticalPosition = controlVerticalPosition,
                            controlButtonScale = controlButtonScale,
                            controlButtonStyle = viewModel.controlButtonStyle.collectAsStateWithLifecycle().value,
                            onLeftPress = { viewModel.gameEngine.moveLeft() },
                            onRightPress = { viewModel.gameEngine.moveRight() },
                            onDownPress = { viewModel.gameEngine.softDrop() },
                            onRotatePress = { viewModel.gameEngine.rotate() },
                            onHardDropPress = { viewModel.gameEngine.hardDrop() },
                            onHoldPress = { viewModel.gameEngine.hold() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerAvatarView(
    playerName: String,
    avatarEmoji: String = "",
    avatarBgColorHex: String = "",
    avatarFrame: String = "standard",
    customBitmap: androidx.compose.ui.graphics.ImageBitmap? = null,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    themeColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    showOnlineDot: Boolean = false,
    isOnline: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AvatarFrameAnim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frameRotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "framePulse"
    )

    val frameBrush = remember(avatarFrame, themeColor, secondaryColor) {
        when (avatarFrame) {
            "neon_ae" -> androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color(0xFF00FFCC), Color(0xFF0077FF), Color(0xFFFF0077), Color(0xFF00FFCC)))
            "gold_ma" -> androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFE4B5), Color(0xFFFFD700)))
            "chrono_gl" -> androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color(0xFFFF0055), Color(0xFFFF5252), Color(0xFFFF7A00), Color(0xFFFF0055)))
            "omega_ti" -> androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF0055), Color(0xFFFFD700), Color(0xFF00F0FF)))
            "matrix_gl" -> androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color(0xFF00FF66), Color(0xFF003300), Color(0xFF33FF33), Color(0xFF00FF66)))
            else -> androidx.compose.ui.graphics.Brush.sweepGradient(listOf(themeColor.copy(alpha = 0.6f), secondaryColor.copy(alpha = 0.6f), themeColor.copy(alpha = 0.6f)))
        }
    }

    val hasAnimatedFrame = avatarFrame in listOf("neon_ae", "gold_ma", "chrono_gl", "omega_ti", "matrix_gl")
    val parsedBgColor = remember(avatarBgColorHex) {
        try {
            if (avatarBgColorHex.isNotBlank()) Color(android.graphics.Color.parseColor(if (avatarBgColorHex.startsWith("#")) avatarBgColorHex else "#$avatarBgColorHex"))
            else Color(0xFF2C2D35)
        } catch (e: Exception) {
            Color(0xFF2C2D35)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        // Outer Glow
        if (hasAnimatedFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = 0.35f
                    }
                    .clip(CircleShape)
                    .background(frameBrush)
            )
        }

        // Rotating Frame Ring
        if (hasAnimatedFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.98f)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
                    .border(
                        width = if (size > 60.dp) 2.5.dp else 1.5.dp,
                        brush = frameBrush,
                        shape = CircleShape
                    )
            )
        }

        // Circular Avatar Content
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize(if (hasAnimatedFrame) 0.84f else 1f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(parsedBgColor, parsedBgColor.copy(alpha = 0.75f))
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (hasAnimatedFrame) Color.Transparent else themeColor.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        ) {
            if (customBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = customBitmap,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else if (avatarEmoji.isNotBlank()) {
                Text(
                    text = avatarEmoji,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.42f).sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Text(
                    text = playerName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    ),
                    color = if (parsedBgColor != Color(0xFF2C2D35)) Color.White else themeColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Online Status Dot
        if (showOnlineDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(if (size > 50.dp) 12.dp else 9.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF00E676) else Color(0xFFFF9100))
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileDialog(
    profile: PublicUserProfile,
    currentLang: Language,
    themeColor: Color,
    onDismiss: () -> Unit,
    onAddFriend: () -> Unit,
    onInviteToDuel: () -> Unit
) {
    var friendAddedState by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "ПРОФИЛЬ ИГРОКА" else "PLAYER PROFILE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = themeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Avatar & Animated Nickname
                    PlayerAvatarView(
                        playerName = profile.username,
                        avatarEmoji = profile.avatarEmoji,
                        avatarBgColorHex = profile.avatarBgColor,
                        avatarFrame = profile.avatarFrame,
                        size = 80.dp,
                        themeColor = themeColor
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val playerColor = parseHexColor(profile.avatarBgColor, themeColor)
                        val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = playerColor)
                        Text(
                            text = profile.username,
                            style = if (profile.hasGradient) {
                                MaterialTheme.typography.titleLarge.copy(
                                    brush = nicknameBrush,
                                    fontWeight = FontWeight.Black
                                )
                            } else {
                                MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        )

                        if (profile.title.isNotBlank() && profile.title != "none") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = profile.title.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Tier & League Badge
                    val tierColor = when (profile.onlineTier.uppercase()) {
                        "BRONZE" -> Color(0xFFCD7F32)
                        "SILVER" -> Color(0xFFC0C0C0)
                        "GOLD" -> Color(0xFFFFD700)
                        "PLATINUM" -> Color(0xFF00E5FF)
                        "DIAMOND" -> Color(0xFF7C4DFF)
                        "MASTER" -> Color(0xFFFF1744)
                        "GRANDMASTER" -> Color(0xFFFF5252)
                        "LEGEND" -> Color(0xFFFF9100)
                        else -> themeColor
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tierColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = tierColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ЛИГА: ${profile.onlineTier.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = tierColor
                            )
                        }
                    }

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (currentLang == Language.RU) "Рекорд" else "High Score",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%,d", profile.highScore),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = themeColor
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (currentLang == Language.RU) "Уровень" else "Mastery Level",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "LVL ${profile.userLevel}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                friendAddedState = true
                                onAddFriend()
                            },
                            enabled = !friendAddedState,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (friendAddedState) Icons.Default.Check else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (friendAddedState) {
                                        if (currentLang == Language.RU) "Запрос отправлен" else "Request Sent"
                                    } else {
                                        if (currentLang == Language.RU) "В друзья" else "Add Friend"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onInviteToDuel,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (currentLang == Language.RU) "В дуэль" else "Duel 1v1",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsDialog(
    viewModel: MainViewModel,
    currentLang: Language,
    themeColor: Color,
    onDismiss: () -> Unit,
    onOpenLobby: () -> Unit
) {
    val friendsList by viewModel.friendsList.collectAsStateWithLifecycle()
    val friendRequests by viewModel.friendRequests.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var activeTab by remember { mutableIntStateOf(0) } // 0: Friends, 1: Requests, 2: Search
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FriendUser>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var actionToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionToast) {
        if (actionToast != null) {
            delay(2000)
            actionToast = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = if (currentLang == Language.RU) "ДРУЗЬЯ И СОЮЗНИКИ" else "FRIENDS & ALLIES",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sliding Pill Tabs
                val tabs = listOf(
                    Triple(0, if (currentLang == Language.RU) "Друзья (${friendsList.size})" else "Friends (${friendsList.size})", Icons.Default.People),
                    Triple(1, if (currentLang == Language.RU) "Запросы (${friendRequests.size})" else "Requests (${friendRequests.size})", Icons.Default.Mail),
                    Triple(2, if (currentLang == Language.RU) "Поиск" else "Search", Icons.Default.Search)
                )

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
                        val tabWidth = maxWidth / tabs.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * activeTab,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                            label = "friendsTabIndicator"
                        )

                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(44.dp)
                                .offset(x = indicatorOffset)
                                .clip(RoundedCornerShape(18.dp))
                                .background(themeColor)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            tabs.forEach { (index, title, icon) ->
                                val isSelected = activeTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            activeTab = index
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Toast notification
                actionToast?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Tab Contents with Directional Transitions
                AnimatedContent(
                    targetState = activeTab,
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
                    label = "FriendsTabContent"
                ) { tab ->
                    when (tab) {
                        0 -> {
                            // My Friends List
                            if (friendsList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.People,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(54.dp)
                                        )
                                        Text(
                                            text = if (currentLang == Language.RU) "У вас пока нет добавленных друзей" else "No friends added yet",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FilledTonalButton(
                                            onClick = { activeTab = 2 },
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(if (currentLang == Language.RU) "Найти игроков" else "Find Players")
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(friendsList) { friend ->
                                        ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            viewModel.openUserProfile(friend.uid, friend.username)
                                                        }
                                                ) {
                                                    PlayerAvatarView(
                                                        playerName = friend.username,
                                                        avatarEmoji = friend.avatarEmoji,
                                                        avatarBgColorHex = friend.avatarBgColor,
                                                        avatarFrame = friend.avatarFrame,
                                                        showOnlineDot = true,
                                                        isOnline = friend.isOnline,
                                                        size = 46.dp,
                                                        themeColor = themeColor
                                                    )

                                                    Column {
                                                        val friendColor = parseHexColor(friend.avatarBgColor, themeColor)
                                                        val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = friendColor)
                                                        Text(
                                                            text = friend.username,
                                                            style = if (friend.hasGradient) {
                                                                MaterialTheme.typography.bodyLarge.copy(
                                                                    brush = nicknameBrush,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            } else {
                                                                MaterialTheme.typography.bodyLarge.copy(
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            },
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "Лига: ${friend.onlineTier}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            onOpenLobby()
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.SportsEsports,
                                                            contentDescription = "Duel",
                                                            tint = themeColor,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.removeFriend(friend) { ok ->
                                                                if (ok) {
                                                                    actionToast = if (currentLang == Language.RU) "Друг удален" else "Friend removed"
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Remove",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Friend Requests List
                            if (friendRequests.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "Входящих запросов в друзья нет" else "No incoming friend requests",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(friendRequests) { req ->
                                        ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    PlayerAvatarView(
                                                        playerName = req.username,
                                                        avatarEmoji = req.avatarEmoji,
                                                        avatarBgColorHex = req.avatarBgColor,
                                                        avatarFrame = req.avatarFrame,
                                                        size = 46.dp,
                                                        themeColor = themeColor
                                                    )

                                                    Column {
                                                        Text(
                                                            text = req.username,
                                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = if (currentLang == Language.RU) "Хочет добавить вас в друзья" else "Wants to be friends",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    FilledIconButton(
                                                        onClick = {
                                                            viewModel.acceptFriendRequest(req) { ok ->
                                                                if (ok) {
                                                                    actionToast = if (currentLang == Language.RU) "Запрос принят!" else "Request accepted!"
                                                                }
                                                            }
                                                        },
                                                        colors = IconButtonDefaults.filledIconButtonColors(
                                                            containerColor = Color(0xFF00E676),
                                                            contentColor = Color.Black
                                                        ),
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = "Accept", modifier = Modifier.size(18.dp))
                                                    }

                                                    FilledIconButton(
                                                        onClick = {
                                                            viewModel.declineFriendRequest(req) { ok ->
                                                                if (ok) {
                                                                    actionToast = if (currentLang == Language.RU) "Запрос отклонен" else "Request declined"
                                                                }
                                                            }
                                                        },
                                                        colors = IconButtonDefaults.filledIconButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                        ),
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Decline", modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Player Search
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        if (it.length >= 2) {
                                            isSearching = true
                                            viewModel.searchPlayers(it) { results ->
                                                searchResults = results
                                                isSearching = false
                                            }
                                        } else {
                                            searchResults = emptyList()
                                        }
                                    },
                                    placeholder = {
                                        Text(if (currentLang == Language.RU) "Введите никнейм игрока..." else "Search player by nickname...")
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = themeColor)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = {
                                                searchQuery = ""
                                                searchResults = emptyList()
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )

                                if (isSearching) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = themeColor)
                                    }
                                } else if (searchResults.isEmpty() && searchQuery.length >= 2) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (currentLang == Language.RU) "Игроки не найдены" else "No players found",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(searchResults) { user ->
                                            ElevatedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.elevatedCardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        PlayerAvatarView(
                                                            playerName = user.username,
                                                            avatarEmoji = user.avatarEmoji,
                                                            avatarBgColorHex = user.avatarBgColor,
                                                            avatarFrame = user.avatarFrame,
                                                            size = 46.dp,
                                                            themeColor = themeColor
                                                        )

                                                        Column {
                                                            Text(
                                                                text = user.username,
                                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = "Лига: ${user.onlineTier}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.sendFriendRequest(user.username) { ok, msg ->
                                                                actionToast = msg
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = themeColor,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    ) {
                                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (currentLang == Language.RU) "Добавить" else "Add", fontSize = 12.sp)
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
        }
    }
}