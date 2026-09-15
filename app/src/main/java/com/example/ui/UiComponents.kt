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

fun parsePlayerColor(hex: String, playerName: String = "", defaultColor: Color = Color(0xFF6C63FF)): Color {
    if (hex.isNotBlank()) {
        try {
            val cleanHex = hex.trim().removePrefix("#")
            val colorInt = when (cleanHex.length) {
                6 -> android.graphics.Color.parseColor("#FF$cleanHex")
                8 -> android.graphics.Color.parseColor("#$cleanHex")
                else -> null
            }
            if (colorInt != null) return Color(colorInt)
        } catch (e: Exception) { }
    }
    if (playerName.isNotBlank()) {
        val vibrantPalettes = listOf(
            Color(0xFF6C63FF),
            Color(0xFF00B4D8),
            Color(0xFF06D6A0),
            Color(0xFFFFB703),
            Color(0xFFFB5607),
            Color(0xFFFF006E),
            Color(0xFF8338EC),
            Color(0xFF3A86FF),
            Color(0xFF2EC4B6),
            Color(0xFFE71D36)
        )
        val idx = kotlin.math.abs(playerName.hashCode()) % vibrantPalettes.size
        return vibrantPalettes[idx]
    }
    return defaultColor
}

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF6C63FF)): Color {
    return parsePlayerColor(hex, "", defaultColor)
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

    // Generate individualized dynamic palette strictly based on the player's personal color
    val colors = remember(baseColor) {
        val isMonochrome = (baseColor.red < 0.22f && baseColor.green < 0.22f && baseColor.blue < 0.22f) ||
                (baseColor.red > 0.80f && baseColor.green > 0.80f && baseColor.blue > 0.80f)

        if (isMonochrome) {
            // High-Tech Liquid Mercury / Titanium Chrome for black & metallic profiles
            listOf(
                Color(0xFFFFFFFF),
                Color(0xFFA0A5B5),
                Color(0xFFE8EDF8),
                Color(0xFF656A7A),
                Color(0xFFFFFFFF)
            )
        } else {
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
            val sat = hsv[1].coerceIn(0.70f, 0.98f)
            val value = hsv[2].coerceIn(0.85f, 1f)

            // Harmonious continuous color loop across color wheel derived from player's personal hue
            val c1 = Color(android.graphics.Color.HSVToColor(floatArrayOf(baseHue, sat, value)))
            val c2 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
            val c3 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 70f) % 360f, (sat * 0.85f).coerceIn(0.55f, 1f), value)))
            val c4 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
            val c5 = c1

            listOf(c1, c2, c3, c4, c5)
        }
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

class ClickDebouncer(private val cooldownMs: Long = 1000L) {
    private var lastClickTime = 0L

    fun canClick(): Boolean {
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastClickTime >= cooldownMs) {
            lastClickTime = now
            return true
        }
        return false
    }

    inline fun process(action: () -> Unit) {
        if (canClick()) {
            action()
        }
    }
}

@Composable
fun rememberClickDebouncer(cooldownMs: Long = 1000L): ClickDebouncer {
    return remember { ClickDebouncer(cooldownMs) }
}

@Composable
fun TetrisApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val menuDebouncer = rememberClickDebouncer(1000L)
    val backDebouncer = rememberClickDebouncer(800L)

    fun navigateDirect(route: String, builder: (androidx.navigation.NavOptionsBuilder.() -> Unit)? = null) {
        val currentDest = navController.currentBackStackEntry?.destination?.route
        if (currentDest == route) return
        try {
            if (builder != null) {
                navController.navigate(route, builder)
            } else {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        } catch (e: Exception) {
            // Guard against race conditions during navigation transitions
        }
    }

    fun safePopBackStack() {
        if (!backDebouncer.canClick()) return
        try {
            navController.popBackStack()
        } catch (e: Exception) {
            // Safe back navigation
        }
    }

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
                    onLeaderboard = { if (menuDebouncer.canClick()) navigateDirect("leaderboard") },
                    onProfile = { if (menuDebouncer.canClick()) navigateDirect("profile") },
                    onModeSelection = { if (menuDebouncer.canClick()) navigateDirect("mode_selection") },
                    onPlayMode = { mode ->
                        if (menuDebouncer.canClick()) {
                            try {
                                if (mode == com.example.game.GameMode.BLOCK_BLAST) {
                                    viewModel.startBlockBlast()
                                    navController.navigate("block_blast") { launchSingleTop = true }
                                } else {
                                    viewModel.startGame(mode)
                                    navController.navigate("game") { launchSingleTop = true }
                                }
                            } catch (e: Exception) {}
                        }
                    },
                    onMultiplayer = {
                        if (menuDebouncer.canClick()) navigateDirect("multiplayer_select")
                    }
                )
            }
            composable("game") {
                GameScreen(viewModel = viewModel, onBack = { safePopBackStack() })
            }
            composable("leaderboard") {
                LeaderboardScreen(viewModel = viewModel, onBack = { safePopBackStack() })
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
                    onBack = { safePopBackStack() },
                    onCustomizeControls = { navigateDirect("custom_controls") }
                )
            }
            composable("custom_controls") {
                CustomControlsScreen(viewModel = viewModel, onBack = { safePopBackStack() })
            }
            composable("block_blast") {
                ZetaScreen(viewModel = viewModel, onBack = { safePopBackStack() })
            }
            composable(
                "cases",
                enterTransition = { tabEnter("cases", initialState.destination.route ?: "") },
                exitTransition = { tabExit("cases", targetState.destination.route ?: "") },
                popEnterTransition = { tabEnter("cases", initialState.destination.route ?: "") },
                popExitTransition = { tabExit("cases", targetState.destination.route ?: "") }
            ) {
                CasesScreen(viewModel = viewModel, onBack = { safePopBackStack() })
            }
            composable("mode_selection") {
                ModeSelectionScreen(
                    viewModel = viewModel,
                    onBack = { safePopBackStack() },
                    onPlayMode = { mode ->
                        try {
                            if (mode == com.example.game.GameMode.BLOCK_BLAST) {
                                viewModel.startBlockBlast()
                                navController.navigate("block_blast") { launchSingleTop = true }
                            } else {
                                viewModel.startGame(mode)
                                navController.navigate("game") { launchSingleTop = true }
                            }
                        } catch (e: Exception) {}
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
                ProfileScreen(viewModel = viewModel, initialTab = 2, onBack = { safePopBackStack() })
            }
            composable(
                "profile",
                enterTransition = { tabEnter("profile", initialState.destination.route ?: "") },
                exitTransition = { tabExit("profile", targetState.destination.route ?: "") },
                popEnterTransition = { tabEnter("profile", initialState.destination.route ?: "") },
                popExitTransition = { tabExit("profile", targetState.destination.route ?: "") }
            ) {
                ProfileScreen(viewModel = viewModel, initialTab = 0, onBack = { safePopBackStack() })
            }
            composable("multiplayer_select") {
                MultiplayerSelectScreen(
                    viewModel = viewModel,
                    onBack = { safePopBackStack() },
                    onSelectServer = { navigateDirect("lobby") },
                    onSelectEosP2p = { navigateDirect("eos_lobby") }
                )
            }
            composable("eos_lobby") {
                EosLobbyScreen(
                    viewModel = viewModel,
                    onBack = { safePopBackStack() },
                    onNavigateToGame = { navigateDirect("eos_game") }
                )
            }
            composable("eos_game") {
                EosGameScreen(
                    viewModel = viewModel,
                    onBackToLobby = {
                        val popped = navController.popBackStack()
                        if (!popped) {
                            navigateDirect("eos_lobby")
                        }
                    }
                )
            }
            composable("lobby") {
                LobbyScreen(
                    viewModel = viewModel,
                    onBack = { safePopBackStack() },
                    onNavigateToGame = { navigateDirect("multiplayer_game") }
                )
            }
            composable("multiplayer_game") {
                MultiplayerGameScreen(
                    viewModel = viewModel,
                    onBackToLobby = {
                        if (backDebouncer.canClick()) {
                            safePopBackStack()
                        }
                    }
                )
            }
        }
    }

    val themeColorVal = MaterialTheme.colorScheme.primary
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Box(modifier = Modifier.fillMaxSize()) {
        if (isWideScreen && showBottomBar) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val items = listOf(
                        Triple("menu", Translations.get("menu", currentLang), Icons.Default.Home),
                        Triple("profile", Translations.get("profile", currentLang), Icons.Default.Person),
                        Triple("cases", Translations.get("cases", currentLang), Icons.Default.CardGiftcard),
                        Triple("settings", Translations.get("settings", currentLang), Icons.Default.Settings)
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
                                    try {
                                        navController.navigate(route) {
                                            popUpTo("menu") {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } catch (e: Exception) {}
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
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            val items = listOf(
                                Triple("menu", Translations.get("menu", currentLang), Icons.Default.Home),
                                Triple("profile", Translations.get("profile", currentLang), Icons.Default.Person),
                                Triple("cases", Translations.get("cases", currentLang), Icons.Default.CardGiftcard),
                                Triple("settings", Translations.get("settings", currentLang), Icons.Default.Settings)
                            )
                            items.forEach { (route, label, icon) ->
                                val isSelected = currentRoute == route || (route == "profile" && currentRoute == "profile_achievements")
                                NavigationBarItem(
                                    icon = { Icon(imageVector = icon, contentDescription = label) },
                                    label = { Text(text = label, fontWeight = FontWeight.Bold) },
                                    selected = isSelected,
                                    onClick = {
                                        if (currentRoute != route) {
                                            try {
                                                navController.navigate(route) {
                                                    popUpTo("menu") {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            } catch (e: Exception) {}
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



        // Public Profile Full Screen Overlay
        val selectedProfile by viewModel.selectedPublicProfile.collectAsStateWithLifecycle()
        AnimatedVisibility(
            visible = selectedProfile != null,
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(200))
        ) {
            val prof = selectedProfile
            if (prof != null) {
                val localPlayerName by viewModel.playerName.collectAsStateWithLifecycle()
                val myUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val isSelf = (myUid != null && prof.uid == myUid) ||
                        (prof.username.isNotEmpty() && prof.username.equals(localPlayerName, ignoreCase = true))
                val friendsList by viewModel.friendsList.collectAsStateWithLifecycle()
                val isFriend = remember(friendsList, prof.uid, prof.username) {
                    friendsList.any { it.uid == prof.uid || (it.username.isNotEmpty() && it.username.equals(prof.username, ignoreCase = true)) }
                }
                val context = androidx.compose.ui.platform.LocalContext.current

                OtherUserProfileDialog(
                    profile = prof,
                    currentLang = currentLang,
                    themeColor = themeColorVal,
                    isFriend = isFriend,
                    isSelf = isSelf,
                    onDismiss = { viewModel.closeUserProfile() },
                    onAddFriend = {
                        viewModel.sendFriendRequest(prof.username) { ok, msg ->
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onInviteToDuel = {
                        val currentRoom = viewModel.lobbyManager.currentRoom.value
                        if (currentRoom != null) {
                            viewModel.lobbyManager.sendRoomInvite(
                                targetUid = prof.uid,
                                roomId = currentRoom.roomId,
                                roomName = currentRoom.name,
                                hostName = localPlayerName,
                                avatarEmoji = viewModel.customAvatarEmoji.value,
                                avatarBgColor = viewModel.customAvatarBgColor.value,
                                avatarFrame = viewModel.equippedAvatarFrame.value,
                                hostTier = viewModel.onlineTier.value
                            )
                            val sentMsg = when (currentLang) {
                                Language.RU -> "Приглашение на дуэль отправлено!"
                                Language.UA -> "Запрошення на дуель надіслано!"
                                Language.KK -> "Дуэльге шақыру жіберілді!"
                                Language.DE -> "Duell-Einladung gesendet!"
                                Language.ZH -> "对决邀请已发送！"
                                else -> "Duel invitation sent!"
                            }
                            android.widget.Toast.makeText(context, sentMsg, android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.closeUserProfile()
                            navigateDirect("lobby")
                        }
                    }
                )
            }
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
                    navigateDirect("lobby")
                }
            )
        }

        // Rewarded Ad Dialog
        val showRewardedAdDialog by viewModel.showRewardedAdDialog.collectAsStateWithLifecycle()
        if (showRewardedAdDialog) {
            RewardedAdDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeRewardedAdDialog() }
            )
        }
    }
}

