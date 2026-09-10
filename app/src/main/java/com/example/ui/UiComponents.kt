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

enum class GameMode {
    CLASSIC,
    EXTENDED,
    FAST_RUN,
    REVERSE,
    BLOCK_BLAST
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
                                } else if (mode == com.example.game.GameMode.SLIDE_PUZZLE) {
                                    viewModel.startSlidePuzzle()
                                    navController.navigate("slide_puzzle") { launchSingleTop = true }
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
                BlockBlastScreen(viewModel = viewModel, onBack = { safePopBackStack() })
            }
            composable("slide_puzzle") {
                SlidePuzzleScreen(viewModel = viewModel, onBack = { safePopBackStack() })
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
                            } else if (mode == com.example.game.GameMode.SLIDE_PUZZLE) {
                                viewModel.startSlidePuzzle()
                                navController.navigate("slide_puzzle") { launchSingleTop = true }
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    viewModel: MainViewModel,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit,
    onModeSelection: () -> Unit,
    onPlayMode: (com.example.game.GameMode) -> Unit,
    onMultiplayer: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val isAdminSessionAuthenticated by viewModel.isAdminSessionAuthenticated.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()

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
                        val authReqTitle = when (currentLang) {
                            Language.RU -> "Требуется авторизация"
                            Language.UA -> "Потрібна авторизація"
                            Language.KK -> "Авторизация қажет"
                            Language.DE -> "Anmeldung erforderlich"
                            Language.ZH -> "需要登录账号"
                            else -> "Authentication Required"
                        }
                        Text(
                            text = authReqTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        val authReqBody = when (currentLang) {
                            Language.RU -> "Для игры в сетевом режиме необходимо зарегистрироваться или войти в свой аккаунт. Хотите перейти в профиль?"
                            Language.UA -> "Для гри в мережевому режимі необхідно зареєструватися або увійти до свого акаунта. Бажаєте перейти до профілю?"
                            Language.KK -> "Желілік режимде ойнау үшін тіркелу немесе аккаунтқа кіру қажет. Профильге өткіңіз келе ме?"
                            Language.DE -> "Für den Online-Mehrspielermodus musst du dich anmelden oder ein Konto erstellen. Möchtest du zum Profil wechseln?"
                            Language.ZH -> "进行在线联机对战需要先登录或注册账号。是否前往个人资料页？"
                            else -> "To play online multiplayer, you need to sign in or create an account. Would you like to go to your profile?"
                        }
                        Text(
                            text = authReqBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        val authConfirmText = when (currentLang) {
                            Language.RU -> "ВХОД / РЕГИСТРАЦИЯ"
                            Language.UA -> "ВХІД / РЕЄСТРАЦІЯ"
                            Language.KK -> "КІРУ / ТІРКЕЛУ"
                            Language.DE -> "ANMELDEN / REGISTRIEREN"
                            Language.ZH -> "登录 / 注册"
                            else -> "LOGIN / REGISTER"
                        }
                        Button(
                            onClick = {
                                showAuthGuardDialog = false
                                onProfile()
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(authConfirmText, fontWeight = FontWeight.Bold)
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
                            Text(Translations.get("cancel", currentLang).uppercase(), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showAdminPassDialog) {
                val adminGuardTitle = when (currentLang) {
                    Language.RU -> "Защита Админ-Панели"
                    Language.UA -> "Захист Адмін-Панелі"
                    Language.KK -> "Әкімші панелін қорғау"
                    Language.DE -> "Admin-Panel-Schutz"
                    Language.ZH -> "管理控制台安全验证"
                    else -> "Admin Panel Guard"
                }
                val adminGuardDesc = when (currentLang) {
                    Language.RU -> "Введите секретный пароль доступа к консоли администратора:"
                    Language.UA -> "Введіть секретний пароль доступу до консолі адміністратора:"
                    Language.KK -> "Әкімші консоліне кіру үшін құпия сөзді енгізіңіз:"
                    Language.DE -> "Geheimes Passwort für den Zugriff auf die Administratorkonsole eingeben:"
                    Language.ZH -> "请输入访问管理员控制台的安全密码："
                    else -> "Enter secret password to access administrator console:"
                }
                val adminPassLabel = when (currentLang) {
                    Language.RU -> "Пароль доступа"
                    Language.UA -> "Пароль доступу"
                    Language.KK -> "Қолжетімділік құпия сөзі"
                    Language.DE -> "Zugangspasswort"
                    Language.ZH -> "访问密码"
                    else -> "Access Password"
                }
                val adminPassErrorText = when (currentLang) {
                    Language.RU -> "Неверный пароль доступа!"
                    Language.UA -> "Невірний пароль доступу!"
                    Language.KK -> "Құпия сөз қате!"
                    Language.DE -> "Falsches Zugangspasswort!"
                    Language.ZH -> "访问密码错误！"
                    else -> "Incorrect access password!"
                }
                val adminEnterBtn = when (currentLang) {
                    Language.RU -> "ВОЙТИ"
                    Language.UA -> "УВІЙТИ"
                    Language.KK -> "КІРУ"
                    Language.DE -> "EINTRETEN"
                    Language.ZH -> "进入"
                    else -> "ENTER"
                }
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
                            text = adminGuardTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = adminGuardDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = adminPassInput,
                                onValueChange = {
                                    adminPassInput = it
                                    adminPassError = false
                                },
                                label = { Text(adminPassLabel) },
                                singleLine = true,
                                isError = adminPassError,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (adminPassError) {
                                Text(
                                    text = adminPassErrorText,
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
                            Text(adminEnterBtn, fontWeight = FontWeight.Bold)
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
                            Text(Translations.get("cancel", currentLang).uppercase())
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
                var selectedUserFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Online, 2: Verified, 3: Unverified, 4: Banned, 5: Rich
                var selectedAdminTab by remember { mutableIntStateOf(0) }
                val firebaseUsers by viewModel.firebaseUsers.collectAsStateWithLifecycle(initialValue = emptyList())
                var editingAdminUser by remember { mutableStateOf<Map<String, Any>?>(null) }
                var grantingCoinsUser by remember { mutableStateOf<Map<String, Any>?>(null) }
                var grantCoinsCustomAmount by remember { mutableStateOf("") }
                var editUserCredits by remember { mutableStateOf("") }
                var editUserHighScore by remember { mutableStateOf("") }
                var editUserLines by remember { mutableStateOf("") }
                var editUserGames by remember { mutableStateOf("") }
                var editUserXp by remember { mutableStateOf("") }
                var editUserTier by remember { mutableStateOf("BRONZE") }
                var editUserGradient by remember { mutableStateOf(false) }
                var editUserTagUnlocked by remember { mutableStateOf(false) }
                var editUserTag by remember { mutableStateOf("") }
                var editUserEmailVerified by remember { mutableStateOf(false) }
                var showWipeConfirmDialog by remember { mutableStateOf(false) }
                var adminToastMessage by remember { mutableStateOf<String?>(null) }
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                LaunchedEffect(adminToastMessage) {
                    if (adminToastMessage != null) {
                        delay(2500)
                        adminToastMessage = null
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.fetchFirebaseUsersForAdmin()
                }

                LaunchedEffect(selectedAdminTab) {
                    if (selectedAdminTab == 1 || selectedAdminTab == 3) {
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = themeColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Админ-панель FsFq"
                                                    Language.UA -> "Адмін-панель FsFq"
                                                    Language.KK -> "FsFq Әкімші панелі"
                                                    Language.DE -> "FsFq Admin-Konsole"
                                                    Language.ZH -> "FsFq 管理控制台"
                                                    else -> "Admin Panel FsFq"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
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
                                    actions = {
                                        IconButton(onClick = {
                                            viewModel.fetchFirebaseUsersForAdmin()
                                            viewModel.triggerAudioFeedback("click")
                                            adminToastMessage = when (currentLang) {
                                                Language.RU -> "Данные обновлены 🔄"
                                                Language.UA -> "Дані оновлено 🔄"
                                                Language.KK -> "Деректер жаңартылды 🔄"
                                                Language.DE -> "Daten aktualisiert 🔄"
                                                Language.ZH -> "数据已刷新 🔄"
                                                else -> "Data refreshed 🔄"
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refresh",
                                                tint = themeColor
                                            )
                                        }
                                        IconButton(onClick = {
                                            viewModel.setAdminSessionAuthenticated(false)
                                            showAdminPanelDialog = false
                                            viewModel.triggerAudioFeedback("gameover")
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Lock",
                                                tint = MaterialTheme.colorScheme.error
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
                                // Server Stats Summary Bar
                                val totalUsers = firebaseUsers.size
                                val onlineCount = firebaseUsers.count { (it["is_online"] as? Boolean) == true }
                                val verifiedCount = firebaseUsers.count { 
                                    (it["email_verified"] as? Boolean) == true || 
                                    (it["is_verified"] as? Boolean) == true || 
                                    (it["emailVerified"] as? Boolean) == true 
                                }
                                val bannedCount = firebaseUsers.count { (it["is_banned"] as? Boolean) == true }
                                val totalEconomy = firebaseUsers.sumOf { (it["credits"] as? Number)?.toLong() ?: 0L }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$totalUsers",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = themeColor
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Игроки"
                                                    Language.UA -> "Гравці"
                                                    Language.KK -> "Ойыншылар"
                                                    Language.DE -> "Spieler"
                                                    Language.ZH -> "玩家总数"
                                                    else -> "Users"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$onlineCount",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF00E676)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Онлайн"
                                                    Language.UA -> "Онлайн"
                                                    Language.KK -> "Желіде"
                                                    Language.DE -> "Online"
                                                    Language.ZH -> "当前在线"
                                                    else -> "Online"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$verifiedCount",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF40C4FF)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Вериф."
                                                    Language.UA -> "Вериф."
                                                    Language.KK -> "Тексерілді"
                                                    Language.DE -> "Verifiziert"
                                                    Language.ZH -> "已认证"
                                                    else -> "Verified"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (totalEconomy >= 1_000_000) String.format(Locale.US, "%.1fM", totalEconomy / 1_000_000.0)
                                                       else if (totalEconomy >= 1_000) String.format(Locale.US, "%.1fk", totalEconomy / 1_000.0)
                                                       else "$totalEconomy",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFFFD54F)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Экономика"
                                                    Language.UA -> "Економіка"
                                                    Language.KK -> "Экономика"
                                                    Language.DE -> "Wirtschaft"
                                                    Language.ZH -> "经济流通"
                                                    else -> "Economy"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (bannedCount > 0) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$bannedCount",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Text(
                                                    text = when (currentLang) {
                                                        Language.RU -> "Бан"
                                                        Language.UA -> "Бан"
                                                        Language.KK -> "Бұғаттау"
                                                        Language.DE -> "Gesperrt"
                                                        Language.ZH -> "封禁用户"
                                                        else -> "Banned"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                adminToastMessage?.let { msg ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF00E676).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = msg,
                                            color = Color(0xFF00E676),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                        )
                                    }
                                }

                                // MD3 Segmented Tab Selector
                                val tabAdminProfile = when (currentLang) {
                                    Language.RU -> "Профиль"
                                    Language.UA -> "Профіль"
                                    Language.KK -> "Профиль"
                                    Language.DE -> "Profil"
                                    Language.ZH -> "管理员"
                                    else -> "Profile"
                                }
                                val tabAdminPlayers = when (currentLang) {
                                    Language.RU -> "Игроки"
                                    Language.UA -> "Гравці"
                                    Language.KK -> "Ойыншылар"
                                    Language.DE -> "Spieler"
                                    Language.ZH -> "玩家列表"
                                    else -> "Players"
                                }
                                val tabAdminBroadcast = when (currentLang) {
                                    Language.RU -> "Оповещения"
                                    Language.UA -> "Оповіщення"
                                    Language.KK -> "Хабарландыру"
                                    Language.DE -> "Broadcast"
                                    Language.ZH -> "全服广播"
                                    else -> "Broadcast"
                                }
                                val tabAdminSystem = when (currentLang) {
                                    Language.RU -> "Система"
                                    Language.UA -> "Система"
                                    Language.KK -> "Жүйе"
                                    Language.DE -> "System"
                                    Language.ZH -> "系统维护"
                                    else -> "System"
                                }
                                val adminTabs = listOf(
                                    Triple(0, tabAdminProfile, Icons.Default.Person),
                                    Triple(1, tabAdminPlayers, Icons.Default.People),
                                    Triple(2, tabAdminBroadcast, Icons.Default.Send),
                                    Triple(3, tabAdminSystem, Icons.Default.Settings)
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

                                Spacer(modifier = Modifier.height(4.dp))

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
                                            // TAB 0: PROFILE & QUICK COIN GENERATOR
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                // Live Profile Status Card
                                                val liveCredits by viewModel.credits.collectAsStateWithLifecycle()
                                                val liveRating by viewModel.onlineRating.collectAsStateWithLifecycle()
                                                val liveTier by viewModel.onlineTier.collectAsStateWithLifecycle()
                                                val liveGradient by viewModel.hasNicknameGradient.collectAsStateWithLifecycle()
                                                val liveTag by viewModel.customTag.collectAsStateWithLifecycle()
                                                val liveBonusXp by viewModel.bonusXp.collectAsStateWithLifecycle()

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
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = when (currentLang) {
                                                                    Language.RU -> "МОЙ ПРОФИЛЬ: $playerName"
                                                                    Language.UA -> "МІЙ ПРОФІЛЬ: $playerName"
                                                                    Language.KK -> "МЕНІҢ ПРОФИЛІМ: $playerName"
                                                                    Language.DE -> "MEIN PROFIL: $playerName"
                                                                    Language.ZH -> "我的个人档案：$playerName"
                                                                    else -> "MY PROFILE: $playerName"
                                                                },
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = themeColor
                                                            )
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = MaterialTheme.colorScheme.primaryContainer
                                                            ) {
                                                                Text(
                                                                    text = "ADMIN 👑",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            Surface(
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(14.dp),
                                                                color = Color(0xFFFFD54F).copy(alpha = 0.15f)
                                                            ) {
                                                                Column(modifier = Modifier.padding(10.dp)) {
                                                                    Text(
                                                                        text = when (currentLang) {
                                                                            Language.RU -> "Баланс монет"
                                                                            Language.UA -> "Баланс монет"
                                                                            Language.KK -> "Монета балансы"
                                                                            Language.DE -> "Münzenbestand"
                                                                            Language.ZH -> "代币余额"
                                                                            else -> "Live Balance"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                        color = Color(0xFFFFB300)
                                                                    )
                                                                    Text(
                                                                        text = "🪙 $liveCredits",
                                                                        style = MaterialTheme.typography.titleMedium,
                                                                        fontWeight = FontWeight.Black,
                                                                        color = Color(0xFFFFB300)
                                                                    )
                                                                }
                                                            }

                                                            Surface(
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(14.dp),
                                                                color = themeColor.copy(alpha = 0.15f)
                                                            ) {
                                                                Column(modifier = Modifier.padding(10.dp)) {
                                                                    Text(
                                                                        text = when (currentLang) {
                                                                            Language.RU -> "Ранг / ELO"
                                                                            Language.UA -> "Ранг / ELO"
                                                                            Language.KK -> "Дәреже / ELO"
                                                                            Language.DE -> "Rang / ELO"
                                                                            Language.ZH -> "段位积分 / ELO"
                                                                            else -> "Tier / ELO"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                        color = themeColor
                                                                    )
                                                                    Text(
                                                                        text = "$liveTier ($liveRating)",
                                                                        style = MaterialTheme.typography.titleMedium,
                                                                        fontWeight = FontWeight.Black,
                                                                        color = themeColor
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // INSTANT COIN GENERATOR & PRESETS
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
                                                            text = when (currentLang) {
                                                                Language.RU -> "ВЫДАЧА МОНЕТ И РЕСУРСОВ 🪙"
                                                                Language.UA -> "ВИДАЧА МОНЕТ ТА РЕСУРСІВ 🪙"
                                                                Language.KK -> "МОНЕТА МЕН РЕСУРС БЕРУ 🪙"
                                                                Language.DE -> "MÜNZEN-GENERATOR & PRESETS 🪙"
                                                                Language.ZH -> "代币生成与快捷预设 🪙"
                                                                else -> "COIN GENERATOR & PRESETS 🪙"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFFFFB300)
                                                        )

                                                        OutlinedTextField(
                                                            value = editUsername,
                                                            onValueChange = { editUsername = it },
                                                            label = {
                                                                val targetUserLabel = when (currentLang) {
                                                                    Language.RU -> "Целевой никнейм (по умолчанию $playerName)"
                                                                    Language.UA -> "Цільовий нікнейм (за замовчуванням $playerName)"
                                                                    Language.KK -> "Мақсатты бүркеншік ат (әдепкі $playerName)"
                                                                    Language.DE -> "Ziel-Benutzername (Standard $playerName)"
                                                                    Language.ZH -> "目标玩家昵称（默认 $playerName）"
                                                                    else -> "Target Username"
                                                                }
                                                                Text(targetUserLabel)
                                                            },
                                                            singleLine = true,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "Быстрое начисление (+ к текущему балансу):"
                                                                Language.UA -> "Швидке нарахування (+ до поточного балансу):"
                                                                Language.KK -> "Жылдам қосу (+ ағымдағы балансқа):"
                                                                Language.DE -> "Schnellaufladung (+ zum Guthaben):"
                                                                Language.ZH -> "快速充值（+ 追加至现有余额）："
                                                                else -> "Quick add (+ to balance):"
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        // Quick Add Grid
                                                        val quickCoins = listOf(1_000, 10_000, 50_000, 100_000, 500_000, 1_000_000)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            quickCoins.take(3).forEach { amount ->
                                                                FilledTonalButton(
                                                                    onClick = {
                                                                        viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = amount, isDelta = true)
                                                                        viewModel.triggerAudioFeedback("success")
                                                                        adminToastMessage = "+$amount 🪙 успешно начислено!"
                                                                    },
                                                                    modifier = Modifier.weight(1f),
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "+${amount / 1000}k",
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        style = MaterialTheme.typography.labelMedium
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            quickCoins.drop(3).forEach { amount ->
                                                                FilledTonalButton(
                                                                    onClick = {
                                                                        viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = amount, isDelta = true)
                                                                        viewModel.triggerAudioFeedback("success")
                                                                        adminToastMessage = "+$amount 🪙 успешно начислено!"
                                                                    },
                                                                    modifier = Modifier.weight(1f),
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                                                ) {
                                                                    Text(
                                                                        text = if (amount >= 1_000_000) "+1M" else "+${amount / 1000}k",
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        style = MaterialTheme.typography.labelMedium
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Button(
                                                                onClick = {
                                                                    viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = 999_999, isDelta = false)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                    adminToastMessage = "Баланс установлен на 999,999 🪙"
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                                                            ) {
                                                                Text("MAX (999k)", color = Color.Black, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                                                            }

                                                            OutlinedButton(
                                                                onClick = {
                                                                    viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = 0, isDelta = false)
                                                                    viewModel.triggerAudioFeedback("gameover")
                                                                    adminToastMessage = "Баланс сброшен в 0 🪙"
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(12.dp)
                                                            ) {
                                                                val resetZeroLabel = when (currentLang) {
                                                                        Language.RU -> "Сброс (0)"
                                                                        Language.UA -> "Скидання (0)"
                                                                        Language.KK -> "Қалпына келтіру (0)"
                                                                        Language.DE -> "Zurücksetzen (0)"
                                                                        Language.ZH -> "归零 (0)"
                                                                        else -> "Reset (0)"
                                                                    }
                                                                    Text(resetZeroLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                            }
                                                        }

                                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                                        // Exact manual input
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            OutlinedTextField(
                                                                value = editCredits,
                                                                onValueChange = { editCredits = it },
                                                                label = {
                                                                    val exactCreditsLabel = when (currentLang) {
                                                                        Language.RU -> "Точная сумма 🪙"
                                                                        Language.UA -> "Точна сума 🪙"
                                                                        Language.KK -> "Дәл сома 🪙"
                                                                        Language.DE -> "Exakter Betrag 🪙"
                                                                        Language.ZH -> "精确代币数值 🪙"
                                                                        else -> "Exact Credits 🪙"
                                                                    }
                                                                    Text(exactCreditsLabel)
                                                                },
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            Button(
                                                                onClick = {
                                                                    val creds = editCredits.toIntOrNull()
                                                                    if (creds != null) {
                                                                        viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = creds, isDelta = false)
                                                                        viewModel.triggerAudioFeedback("success")
                                                                        adminToastMessage = "Баланс установлен на $creds 🪙"
                                                                        editCredits = ""
                                                                    }
                                                                },
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                val setCoinsBtnLabel = when (currentLang) {
                                                                        Language.RU -> "ЗАДАТЬ"
                                                                        Language.UA -> "ЗАДАТИ"
                                                                        Language.KK -> "ОРНАТУ"
                                                                        Language.DE -> "FESTLEGEN"
                                                                        Language.ZH -> "设置"
                                                                        else -> "SET"
                                                                    }
                                                                    Text(setCoinsBtnLabel, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }

                                                // TIER & COSMETICS EDITOR
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
                                                            text = when (currentLang) {
                                                                Language.RU -> "РАНГ И СТАТУС"
                                                                Language.UA -> "РАНГ ТА СТАТУС"
                                                                Language.KK -> "ДӘРЕЖЕ МЕН МӘРТЕБЕ"
                                                                Language.DE -> "RANG & STATUS"
                                                                Language.ZH -> "段位与状态配置"
                                                                else -> "TIER & STATUS"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        val tiers = listOf("BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "LEGEND")
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .horizontalScroll(rememberScrollState()),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            tiers.forEach { tier ->
                                                                FilterChip(
                                                                    selected = editRank == tier,
                                                                    onClick = {
                                                                        editRank = tier
                                                                        viewModel.adminUpdateAccountRank(editUsername.ifBlank { playerName }, tier)
                                                                        viewModel.triggerAudioFeedback("click")
                                                                        adminToastMessage = "Ранг изменен на $tier"
                                                                    },
                                                                    label = { Text(tier, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                                                                    shape = RoundedCornerShape(10.dp)
                                                                )
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            OutlinedTextField(
                                                                value = editBonusXp,
                                                                onValueChange = { editBonusXp = it },
                                                                label = {
                                                                    val bonusXpLabel = when (currentLang) {
                                                                        Language.RU -> "Бонус XP"
                                                                        Language.UA -> "Бонус XP"
                                                                        Language.KK -> "Бонус XP"
                                                                        Language.DE -> "Bonus-XP"
                                                                        Language.ZH -> "经验加成 XP"
                                                                        else -> "Bonus XP"
                                                                    }
                                                                    Text(bonusXpLabel)
                                                                },
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            Button(
                                                                onClick = {
                                                                    val xp = editBonusXp.toIntOrNull() ?: 0
                                                                    viewModel.adminUpdateAccountBonusXp(editUsername.ifBlank { playerName }, xp)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                    adminToastMessage = "Опыт обновлен: $xp XP"
                                                                },
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                val setXpBtnLabel = when (currentLang) {
                                                                        Language.RU -> "ЗАДАТЬ XP"
                                                                        Language.UA -> "ЗАДАТИ XP"
                                                                        Language.KK -> "XP ОРНАТУ"
                                                                        Language.DE -> "XP FESTLEGEN"
                                                                        Language.ZH -> "设置 XP"
                                                                        else -> "SET XP"
                                                                    }
                                                                    Text(setXpBtnLabel, fontWeight = FontWeight.Bold)
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = when (currentLang) {
                                                                    Language.RU -> "Градиент никнейма"
                                                                    Language.UA -> "Градієнт нікнейму"
                                                                    Language.KK -> "Бүркеншік ат градиенті"
                                                                    Language.DE -> "Farbverlauf-Name"
                                                                    Language.ZH -> "昵称渐变特效"
                                                                    else -> "Nickname Gradient"
                                                                },
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Switch(
                                                                checked = editHasGradient,
                                                                onCheckedChange = {
                                                                    editHasGradient = it
                                                                    viewModel.adminUpdateAccountGradient(editUsername.ifBlank { playerName }, it)
                                                                    viewModel.triggerAudioFeedback("click")
                                                                    adminToastMessage = if (it) "Градиент включен" else "Градиент отключен"
                                                                }
                                                            )
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
                                                            text = when (currentLang) {
                                                                Language.RU -> "РАЗБЛОКИРОВКА И ЧИТЫ"
                                                                Language.UA -> "РОЗБЛОКУВАННЯ ТА ЧІТИ"
                                                                Language.KK -> "ҚҰЛЫПТЫ АШУ ЖӘНЕ ЧИТТЕР"
                                                                Language.DE -> "FREISCHALTUNGEN & CHEATS"
                                                                Language.ZH -> "全量解锁与测试指令"
                                                                else -> "UNLOCKS & CHEATS"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminGiveAllCosmetics(editUsername.ifBlank { playerName })
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "Вся косметика разблокирована!"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val grantCosmeticsLabel = when (currentLang) {
                                                                    Language.RU -> "ВЫДАТЬ ВСЮ КОСМЕТИКУ (Скины, Рамки, Шрифты)"
                                                                    Language.UA -> "ВИДАТИ ВСЮ КОСМЕТИКУ (Скіни, Рамки, Шрифти)"
                                                                    Language.KK -> "БАРЛЫҚ КОСМЕТИКАНЫ БЕРУ (Скиндер, Жақтаулар, Қаріптер)"
                                                                    Language.DE -> "ALLE KOSMETIK FREISCHALTEN (Skins, Rahmen, Schriftarten)"
                                                                    Language.ZH -> "解锁全部装扮道具（皮肤、头像框、特效字体）"
                                                                    else -> "GRANT ALL COSMETICS & SKINS"
                                                                }
                                                                Text(grantCosmeticsLabel, fontWeight = FontWeight.Bold)
                                                        }

                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminUnlockAllAchievements()
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "Все достижения открыты + 5000 🪙"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val unlockAchLabel = when (currentLang) {
                                                                    Language.RU -> "ОТКРЫТЬ ВСЕ ДОСТИЖЕНИЯ + 5000 🪙"
                                                                    Language.UA -> "ВІДКРИТИ ВСІ ДОСЯГНЕННЯ + 5000 🪙"
                                                                    Language.KK -> "БАРЛЫҚ ЖЕТІСТІКТЕРДІ АШУ + 5000 🪙"
                                                                    Language.DE -> "ALLE ERFOLGE FREISCHALTEN + 5000 🪙"
                                                                    Language.ZH -> "解锁所有成就并获取 +5000 🪙"
                                                                    else -> "UNLOCK ALL ACHIEVEMENTS + 5000 🪙"
                                                                }
                                                                Text(unlockAchLabel, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        1 -> {
                                            // TAB 1: FIREBASE USERS & DETAILED INSPECTION
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = userSearchQuery,
                                                    onValueChange = { userSearchQuery = it },
                                                    label = {
                                                        val userSearchLabel = when (currentLang) {
                                                            Language.RU -> "Поиск (по нику, UID, почте)..."
                                                            Language.UA -> "Пошук (за ніком, UID, поштою)..."
                                                            Language.KK -> "Іздеу (ник, UID, пошта бойынша)..."
                                                            Language.DE -> "Suche (Name, UID, E-Mail)..."
                                                            Language.ZH -> "搜索玩家（按昵称、UID 或邮箱）..."
                                                            else -> "Search (name, UID, email)..."
                                                        }
                                                        Text(userSearchLabel)
                                                    },
                                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                // Filter Chips
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val verifiedCount = firebaseUsers.count { 
                                                        (it["email_verified"] as? Boolean) == true || 
                                                        (it["is_verified"] as? Boolean) == true || 
                                                        (it["emailVerified"] as? Boolean) == true 
                                                    }
                                                    val unverifiedCount = firebaseUsers.size - verifiedCount
                                                    val filterLabels = listOf(
                                                        "Все (${firebaseUsers.size})",
                                                        "Онлайн (${firebaseUsers.count { (it["is_online"] as? Boolean) == true }})",
                                                        "Вериф. ($verifiedCount)",
                                                        "Невериф. ($unverifiedCount)",
                                                        "Бан (${firebaseUsers.count { (it["is_banned"] as? Boolean) == true }})",
                                                        "Богатые (${firebaseUsers.count { ((it["credits"] as? Number)?.toLong() ?: 0L) >= 50_000 }})"
                                                    )
                                                    filterLabels.forEachIndexed { index, label ->
                                                        FilterChip(
                                                            selected = selectedUserFilter == index,
                                                            onClick = { selectedUserFilter = index },
                                                            label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                    }
                                                }

                                                val filteredUsers = remember(firebaseUsers, userSearchQuery, selectedUserFilter) {
                                                    var list = firebaseUsers
                                                    if (userSearchQuery.isNotBlank()) {
                                                        list = list.filter {
                                                            val pName = (it["player_name"] as? String) ?: ""
                                                            val email = (it["email"] as? String) ?: ""
                                                            val uid = (it["uid"] as? String) ?: ""
                                                            pName.contains(userSearchQuery, ignoreCase = true) ||
                                                            email.contains(userSearchQuery, ignoreCase = true) ||
                                                            uid.contains(userSearchQuery, ignoreCase = true)
                                                        }
                                                    }
                                                    when (selectedUserFilter) {
                                                        1 -> list.filter {
                                                            val rawSynced = (it["last_synced_timestamp"] as? Number)?.toLong() ?: 0L
                                                            (it["is_online"] as? Boolean) == true && (rawSynced == 0L || (System.currentTimeMillis() - rawSynced) < 120_000L)
                                                        }
                                                        2 -> list.filter { (it["email_verified"] as? Boolean) == true || (it["is_verified"] as? Boolean) == true || (it["emailVerified"] as? Boolean) == true }
                                                        3 -> list.filter { !((it["email_verified"] as? Boolean) == true || (it["is_verified"] as? Boolean) == true || (it["emailVerified"] as? Boolean) == true) }
                                                        4 -> list.filter { (it["is_banned"] as? Boolean) == true }
                                                        5 -> list.filter { ((it["credits"] as? Number)?.toLong() ?: 0L) >= 50_000 }
                                                        else -> list
                                                    }
                                                }

                                                LazyColumn(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                    contentPadding = PaddingValues(bottom = 24.dp)
                                                ) {
                                                    items(filteredUsers) { user ->
                                                        val uName = (user["player_name"] as? String) ?: "Unknown"
                                                        val uEmail = (user["email"] as? String) ?: "No email"
                                                        val uUid = (user["uid"] as? String) ?: ""
                                                        val uCredits = (user["credits"] as? Number)?.toLong() ?: 0L
                                                        val isBanned = (user["is_banned"] as? Boolean) == true
                                                        val rawLastSynced = (user["last_synced_timestamp"] as? Number)?.toLong() ?: 0L
                                                        val isRecentlyActive = (System.currentTimeMillis() - rawLastSynced) < 120_000L
                                                        val isOnline = ((user["is_online"] as? Boolean) == true) && (rawLastSynced == 0L || isRecentlyActive)
                                                        val isEmailVerified = (user["email_verified"] as? Boolean) == true || (user["is_verified"] as? Boolean) == true || (user["emailVerified"] as? Boolean) == true
                                                        val uTier = (user["online_tier"] as? String) ?: "BRONZE"
                                                        val uRating = (user["online_rating"] as? Number)?.toInt() ?: 1000
                                                        val uHighScore = (user["stats_high_score"] as? Number)?.toInt() ?: 0
                                                        val uLines = (user["stats_cleared_lines"] as? Number)?.toInt() ?: 0
                                                        val uGames = (user["stats_games_played"] as? Number)?.toInt() ?: 0
                                                        val uBonusXp = (user["bonus_xp"] as? Number)?.toInt() ?: 0
                                                        val uCustomTag = (user["custom_tag"] as? String) ?: ""
                                                        val uFrame = (user["equipped_avatar_frame"] as? String) ?: "standard"
                                                        val uAvatarEmoji = (user["custom_avatar_emoji"] as? String) ?: (user["avatarEmoji"] as? String) ?: ""
                                                        val uAvatarBgColor = (user["custom_avatar_bg_color"] as? String) ?: (user["avatarBgColor"] as? String) ?: ""
                                                        val uAvatarBase64 = (user["custom_avatar_base64"] as? String) ?: ""

                                                        ElevatedCard(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(20.dp),
                                                            colors = CardDefaults.elevatedCardColors(
                                                                containerColor = if (isBanned) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                                                else MaterialTheme.colorScheme.surfaceContainerHigh
                                                            )
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.padding(14.dp),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                // Header Row: Avatar + Name + Badges
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                                    ) {
                                                                        PlayerAvatarView(
                                                                            playerName = uName,
                                                                            avatarEmoji = uAvatarEmoji,
                                                                            avatarBgColorHex = uAvatarBgColor,
                                                                            avatarFrame = uFrame,
                                                                            avatarBase64 = uAvatarBase64,
                                                                            size = 40.dp,
                                                                            showOnlineDot = true,
                                                                            isOnline = isOnline
                                                                        )

                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                        ) {
                                                                            Text(
                                                                                text = uName,
                                                                                style = MaterialTheme.typography.titleMedium,
                                                                                fontWeight = FontWeight.Black
                                                                            )

                                                                            if (uName == "FsFq" || uUid == "ge9Lzx5EkCfbINDZEG6I8vYcJCd2") {
                                                                                Surface(
                                                                                    shape = RoundedCornerShape(6.dp),
                                                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                                                ) {
                                                                                    Text(
                                                                                        text = "ADMIN",
                                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                                                    )
                                                                                }
                                                                            }

                                                                            if (uCustomTag.isNotBlank()) {
                                                                                Surface(
                                                                                    shape = RoundedCornerShape(6.dp),
                                                                                    color = themeColor.copy(alpha = 0.2f)
                                                                                ) {
                                                                                    Text(
                                                                                        text = "[$uCustomTag]",
                                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        color = themeColor,
                                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    Surface(
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        color = if (isBanned) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) 
                                                                                else if (isEmailVerified) Color(0xFF00E676).copy(alpha = 0.2f) 
                                                                                else Color(0xFFFF9100).copy(alpha = 0.2f),
                                                                        modifier = Modifier.clickable {
                                                                            if (!uUid.startsWith("local_")) {
                                                                                viewModel.adminSetEmailVerified(uUid, !isEmailVerified)
                                                                                viewModel.triggerAudioFeedback("click")
                                                                                adminToastMessage = if (!isEmailVerified) "Почта $uName подтверждена ✓" else "Подтверждение почты $uName снято"
                                                                            }
                                                                        }
                                                                    ) {
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = if (isBanned) Icons.Default.Block else if (isEmailVerified) Icons.Default.CheckCircle else Icons.Default.MarkEmailUnread,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(11.dp),
                                                                                tint = if (isBanned) MaterialTheme.colorScheme.error else if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100)
                                                                            )
                                                                            Text(
                                                                                text = if (isBanned) "BANNED" else if (isEmailVerified) "VERIFIED" else "PENDING",
                                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                                color = if (isBanned) MaterialTheme.colorScheme.error else if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100)
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                // UID + Email Row (with copy UID & copy Email buttons)
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = "UID: ${uUid.take(12)}...",
                                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                        IconButton(
                                                                            onClick = {
                                                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(uUid))
                                                                                viewModel.triggerAudioFeedback("click")
                                                                                adminToastMessage = "UID скопирован: $uUid"
                                                                            },
                                                                            modifier = Modifier.size(18.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.ContentCopy,
                                                                                contentDescription = "Copy UID",
                                                                                modifier = Modifier.size(12.dp),
                                                                                tint = themeColor
                                                                            )
                                                                        }
                                                                    }

                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = if (uEmail.length > 22) uEmail.take(20) + "..." else uEmail,
                                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                        if (uEmail != "No email" && uEmail.isNotBlank()) {
                                                                            IconButton(
                                                                                onClick = {
                                                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(uEmail))
                                                                                    viewModel.triggerAudioFeedback("click")
                                                                                    adminToastMessage = "Email скопирован: $uEmail"
                                                                                },
                                                                                modifier = Modifier.size(18.dp)
                                                                            ) {
                                                                                Icon(
                                                                                    imageVector = Icons.Default.ContentCopy,
                                                                                    contentDescription = "Copy Email",
                                                                                    modifier = Modifier.size(12.dp),
                                                                                    tint = themeColor
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                // Stats Grid: Credits, ELO/Tier, Score, Lines, Games
                                                                Surface(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    color = MaterialTheme.colorScheme.surfaceContainer
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(8.dp),
                                                                        horizontalArrangement = Arrangement.SpaceAround,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("🪙 $uCredits", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFB300))
                                                                            Text("Монеты", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uTier ($uRating)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = themeColor)
                                                                            Text("Лига / ELO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uHighScore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                                            Text("Рекорд", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uLines", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                                            Text("Линии", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uGames", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                                            Text("Игры", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                    }
                                                                }

                                                                // Action Buttons Row
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    // + Coins Quick Action Button
                                                                    FilledTonalButton(
                                                                        onClick = {
                                                                            grantingCoinsUser = user
                                                                            grantCoinsCustomAmount = ""
                                                                            viewModel.triggerAudioFeedback("click")
                                                                        },
                                                                        shape = RoundedCornerShape(10.dp),
                                                                        modifier = Modifier.weight(1f),
                                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                                                    ) {
                                                                        Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB300))
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text("+ Монеты", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                                    }

                                                                    // Full Edit Button
                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            editingAdminUser = user
                                                                            editUserCredits = uCredits.toString()
                                                                            editUserHighScore = uHighScore.toString()
                                                                            editUserLines = uLines.toString()
                                                                            editUserGames = uGames.toString()
                                                                            editUserXp = uBonusXp.toString()
                                                                            editUserTier = uTier
                                                                            editUserGradient = (user["has_nickname_gradient"] as? Boolean) == true
                                                                            editUserTagUnlocked = (user["custom_tag_unlocked"] as? Boolean) == true
                                                                            editUserTag = uCustomTag
                                                                            editUserEmailVerified = isEmailVerified
                                                                            viewModel.triggerAudioFeedback("click")
                                                                        },
                                                                        shape = RoundedCornerShape(10.dp),
                                                                        modifier = Modifier.weight(1f),
                                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                                                    ) {
                                                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        val editBtnLabel = when (currentLang) {
                                                                            Language.RU -> "Правка"
                                                                            Language.UA -> "Редагувати"
                                                                            Language.KK -> "Өңдеу"
                                                                            Language.DE -> "Bearbeiten"
                                                                            Language.ZH -> "编辑"
                                                                            else -> "Edit"
                                                                        }
                                                                        Text(editBtnLabel, style = MaterialTheme.typography.labelSmall)
                                                                    }

                                                                    if (uName != "FsFq" && uUid != "ge9Lzx5EkCfbINDZEG6I8vYcJCd2") {
                                                                        OutlinedButton(
                                                                            onClick = {
                                                                                viewModel.adminBanUser(uUid, !isBanned)
                                                                                viewModel.triggerAudioFeedback("click")
                                                                                adminToastMessage = if (!isBanned) "Игрок $uName забанен" else "Игрок $uName разбанен"
                                                                            },
                                                                            shape = RoundedCornerShape(10.dp),
                                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                                                        ) {
                                                                            val banBtnLabel = if (isBanned) {
                                                                                when (currentLang) {
                                                                                    Language.RU -> "Разбан"
                                                                                    Language.UA -> "Розбан"
                                                                                    Language.KK -> "Бұғаттан шығару"
                                                                                    Language.DE -> "Entbannen"
                                                                                    Language.ZH -> "解封"
                                                                                    else -> "Unban"
                                                                                }
                                                                            } else {
                                                                                when (currentLang) {
                                                                                    Language.RU -> "Бан"
                                                                                    Language.UA -> "Бан"
                                                                                    Language.KK -> "Бұғаттау"
                                                                                    Language.DE -> "Bannen"
                                                                                    Language.ZH -> "封禁"
                                                                                    else -> "Ban"
                                                                                }
                                                                            }
                                                                            Text(
                                                                                text = banBtnLabel,
                                                                                style = MaterialTheme.typography.labelSmall,
                                                                                color = if (isBanned) Color(0xFF00E676) else MaterialTheme.colorScheme.error
                                                                            )
                                                                        }

                                                                        IconButton(
                                                                            onClick = {
                                                                                viewModel.adminDeleteFirebaseUserDirect(uUid)
                                                                                viewModel.triggerAudioFeedback("gameover")
                                                                                adminToastMessage = "Аккаунт $uName удален из базы"
                                                                            },
                                                                            modifier = Modifier.size(32.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Delete,
                                                                                contentDescription = "Delete",
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
                                        }
                                        2 -> {
                                            // TAB 2: GLOBAL BROADCAST ANNOUNCEMENTS
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
                                                            text = when (currentLang) {
                                                                Language.RU -> "ОТПРАВКА СИСТЕМНОГО ОПОВЕЩЕНИЯ 📢"
                                                                Language.UA -> "ВІДПРАВКА СИСТЕМНОГО ОПОВІЩЕННЯ 📢"
                                                                Language.KK -> "ЖҮЙЕЛІК ХАБАРЛАНДЫРУ ЖІБЕРУ 📢"
                                                                Language.DE -> "SYSTEMWEITE BENACHRICHTIGUNG 📢"
                                                                Language.ZH -> "发布全服系统公告 📢"
                                                                else -> "GLOBAL SERVER BROADCAST 📢"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "Сообщение отобразится у всех онлайн-игроков в чате лобби и будет сохранено в базе данных."
                                                                Language.UA -> "Повідомлення відобразиться у всіх онлайн-гравців у чаті лобі та буде збережено в базі даних."
                                                                Language.KK -> "Хабарлама лобби чатындағы барлық онлайн ойыншыларға көрсетіледі және дерекқорда сақталады."
                                                                Language.DE -> "Die Nachricht wird allen Online-Spielern im Lobby-Chat angezeigt und in der Datenbank gespeichert."
                                                                Language.ZH -> "公告将即时广播给所有在全服大厅聊天的在线玩家，并存入数据库记录。"
                                                                else -> "Message will be broadcast to all connected players in the multiplayer lobby chat and saved to global database."
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        // Quick template buttons
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "Быстрые шаблоны:"
                                                                Language.UA -> "Швидкі шаблони:"
                                                                Language.KK -> "Жылдам үлгілер:"
                                                                Language.DE -> "Schnellvorlagen:"
                                                                Language.ZH -> "快捷模板："
                                                                else -> "Quick templates:"
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .horizontalScroll(rememberScrollState()),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "🛠️ Технические работы"
                                                                    broadcastText = "Серверные работы завершены. Все сетевые режимы работают штатно!"
                                                                },
                                                                label = { Text("Техработы", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "🎁 Бонусные награды"
                                                                    broadcastText = "Администрация начислила бонусные монеты всем активным игрокам! Проверьте свой баланс."
                                                                },
                                                                label = { Text("Награды", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "🚀 Обновление 0.94.9"
                                                                    broadcastText = "Вышло обновление клиента: улучшена стабильность мультиплеера и синхронизация монет!"
                                                                },
                                                                label = { Text("Обновление", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "🏆 Турнир начался"
                                                                    broadcastText = "Запущен турнирный сезон! Играйте в сетевых дуэлях и занимайте топ в лидерборде."
                                                                },
                                                                label = { Text("Турнир", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }

                                                        OutlinedTextField(
                                                            value = broadcastTitle,
                                                            onValueChange = { broadcastTitle = it },
                                                            label = {
                                                                val broadcastTitleLabel = when (currentLang) {
                                                                    Language.RU -> "Заголовок оповещения"
                                                                    Language.UA -> "Заголовок оповіщення"
                                                                    Language.KK -> "Хабарландыру тақырыбы"
                                                                    Language.DE -> "Titel der Nachricht"
                                                                    Language.ZH -> "公告标题"
                                                                    else -> "Broadcast Title"
                                                                }
                                                                Text(broadcastTitleLabel)
                                                            },
                                                            singleLine = true,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        OutlinedTextField(
                                                            value = broadcastText,
                                                            onValueChange = { broadcastText = it },
                                                            label = {
                                                                val broadcastContentLabel = when (currentLang) {
                                                                    Language.RU -> "Текст сообщения"
                                                                    Language.UA -> "Текст повідомлення"
                                                                    Language.KK -> "Хабарлама мәтіні"
                                                                    Language.DE -> "Nachrichteninhalt"
                                                                    Language.ZH -> "公告正文"
                                                                    else -> "Message Content"
                                                                }
                                                                Text(broadcastContentLabel)
                                                            },
                                                            minLines = 3,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Button(
                                                            onClick = {
                                                                if (broadcastText.isNotBlank()) {
                                                                    viewModel.adminSendGlobalBroadcast(broadcastTitle, broadcastText)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                    broadcastSentFeedback = when (currentLang) {
                                                                        Language.RU -> "Оповещение успешно отправлено всем игрокам!"
                                                                        Language.UA -> "Оповіщення успішно надіслано всім гравцям!"
                                                                        Language.KK -> "Хабарландыру барлық ойыншыларға сәтті жіберілді!"
                                                                        Language.DE -> "Benachrichtigung erfolgreich an alle Spieler gesendet!"
                                                                        Language.ZH -> "全服系统公告已成功下发！"
                                                                        else -> "Broadcast dispatched successfully!"
                                                                    }
                                                                    broadcastTitle = ""
                                                                    broadcastText = ""
                                                                }
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val sendAllBtnLabel = when (currentLang) {
                                                                    Language.RU -> "ОТПРАВИТЬ ВСЕМ"
                                                                    Language.UA -> "НАДІСЛАТИ ВСІМ"
                                                                    Language.KK -> "БАРЛЫҒЫНА ЖІБЕРУ"
                                                                    Language.DE -> "AN ALLE SENDEN"
                                                                    Language.ZH -> "全员下发"
                                                                    else -> "BROADCAST TO ALL"
                                                                }
                                                                Text(sendAllBtnLabel, fontWeight = FontWeight.Bold)
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
                                            // TAB 3: DATABASE & SYSTEM TOOLS
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
                                                            text = when (currentLang) {
                                                                Language.RU -> "ГЛОБАЛЬНАЯ ЭКОНОМИКА И НАГРАДЫ"
                                                                Language.UA -> "ГЛОБАЛЬНА ЕКОНОМІКА ТА НАГОРОДИ"
                                                                Language.KK -> "ЖАҺАНДЫҚ ЭКОНОМИКА ЖӘНЕ СЫЙЛЫҚТАР"
                                                                Language.DE -> "GLOBALE WIRTSCHAFT & BELOHNUNGEN"
                                                                Language.ZH -> "全服经济与奖励分配"
                                                                else -> "GLOBAL ECONOMY & REWARDS"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminGiveCreditsToAll(10_000)
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "Всем игрокам начислено по +10,000 🪙"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                                                        ) {
                                                            Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val grantAll10kLabel = when (currentLang) {
                                                                    Language.RU -> "ВЫДАТЬ ВСЕМ ИГРОКАМ ПО +10,000 🪙"
                                                                    Language.UA -> "ВИДАТИ ВСІМ ГРАВЦЯМ ПО +10,000 🪙"
                                                                    Language.KK -> "БАРЛЫҚ ОЙЫНШЫЛАРҒА +10,000 🪙 БЕРУ"
                                                                    Language.DE -> "ALLEN SPIELERN +10.000 🪙 GEBEN"
                                                                    Language.ZH -> "向全服所有玩家发放 +10,000 🪙"
                                                                    else -> "GRANT +10,000 🪙 TO ALL USERS"
                                                                }
                                                                Text(grantAll10kLabel, color = Color.Black, fontWeight = FontWeight.Black)
                                                        }
                                                    }
                                                }

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
                                                            text = when (currentLang) {
                                                                Language.RU -> "УПРАВЛЕНИЕ БАЗОЙ ДАННЫХ"
                                                                Language.UA -> "УПРАВЛІННЯ БАЗОЮ ДАНИХ"
                                                                Language.KK -> "ДЕРЕКҚОРДЫ БАСҚАРУ"
                                                                Language.DE -> "DATENBANK-VERWALTUNG"
                                                                Language.ZH -> "数据库核心管理"
                                                                else -> "DATABASE MANAGEMENT"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MaterialTheme.colorScheme.error
                                                        )

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminClearAllScores()
                                                                viewModel.triggerAudioFeedback("gameover")
                                                                adminToastMessage = "Все рекорды в БД сброшены"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val resetScoresLabel = when (currentLang) {
                                                                    Language.RU -> "СБРОСИТЬ ВСЕ РЕКОРДЫ В БД"
                                                                    Language.UA -> "СКИНУТИ ВСІ РЕКОРДИ В БД"
                                                                    Language.KK -> "ДЕРЕКҚОРДАҒЫ БАРЛЫҚ РЕКОРДТАРДЫ ҚАЛПЫНА КЕЛТІРУ"
                                                                    Language.DE -> "ALLE HIGHSCORES IN DB ZURÜCKSETZEN"
                                                                    Language.ZH -> "重置数据库所有历史最高得分"
                                                                    else -> "RESET ALL DATABASE SCORES"
                                                                }
                                                                Text(resetScoresLabel, fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminClearAllNonAdminAccounts()
                                                                viewModel.triggerAudioFeedback("gameover")
                                                                adminToastMessage = "Все сторонние аккаунты удалены"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val deleteUsersLabel = when (currentLang) {
                                                                    Language.RU -> "УДАЛИТЬ ВСЕ АККАУНТЫ КРОМЕ АДМИНА"
                                                                    Language.UA -> "ВИДАЛИТИ ВСІ АКАУНТИ КРІМ АДМІНА"
                                                                    Language.KK -> "ӘКІМШІДЕН БАСҚА БАРЛЫҚ АККАУНТТАРДЫ ЖОЮ"
                                                                    Language.DE -> "ALLE BENUTZER AUSSER ADMIN LÖSCHEN"
                                                                    Language.ZH -> "清空所有非管理员注册账号"
                                                                    else -> "DELETE ALL NON-ADMIN USERS"
                                                                }
                                                                Text(deleteUsersLabel, fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                showWipeConfirmDialog = true
                                                                viewModel.triggerAudioFeedback("click")
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("ПОЛНЫЙ ВАЙП БД (FIRESTORE + БАЗА)", fontWeight = FontWeight.Black)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Quick Coins Grant Modal for specific user
                                if (grantingCoinsUser != null) {
                                    val target = grantingCoinsUser!!
                                    val tUid = (target["uid"] as? String) ?: ""
                                    val tName = (target["player_name"] as? String) ?: "Player"
                                    val tCurrentCoins = (target["credits"] as? Number)?.toLong() ?: 0L

                                    AlertDialog(
                                        onDismissRequest = { grantingCoinsUser = null },
                                        title = {
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Выдача монет: $tName 🪙"
                                                    Language.UA -> "Видача монет: $tName 🪙"
                                                    Language.KK -> "Монета беру: $tName 🪙"
                                                    Language.DE -> "Münzen vergeben: $tName 🪙"
                                                    Language.ZH -> "发放代币：$tName 🪙"
                                                    else -> "Grant Coins: $tName 🪙"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        },
                                        text = {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = when (currentLang) {
                                                        Language.RU -> "Текущий баланс: $tCurrentCoins 🪙"
                                                        Language.UA -> "Поточний баланс: $tCurrentCoins 🪙"
                                                        Language.KK -> "Ағымдағы баланс: $tCurrentCoins 🪙"
                                                        Language.DE -> "Aktueller Stand: $tCurrentCoins 🪙"
                                                        Language.ZH -> "当前持有余额：$tCurrentCoins 🪙"
                                                        else -> "Current Balance: $tCurrentCoins 🪙"
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFFB300)
                                                )

                                                Text(
                                                    text = when (currentLang) {
                                                        Language.RU -> "Быстрое добавление:"
                                                        Language.UA -> "Швидке додавання:"
                                                        Language.KK -> "Жылдам қосу:"
                                                        Language.DE -> "Schnellauswahl:"
                                                        Language.ZH -> "快捷增发："
                                                        else -> "Quick Add Presets:"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    listOf(5_000, 25_000, 100_000).forEach { amount ->
                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminGiveCredits(uid = tUid, username = tName, amount = amount, isDelta = true)
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "+$amount 🪙 начислено пользователю $tName"
                                                                grantingCoinsUser = null
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(10.dp)
                                                        ) {
                                                            Text("+${amount / 1000}k", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }

                                                OutlinedTextField(
                                                    value = grantCoinsCustomAmount,
                                                    onValueChange = { grantCoinsCustomAmount = it },
                                                    label = {
                                                        val customAmountLabel = when (currentLang) {
                                                            Language.RU -> "Своя сумма (+)"
                                                            Language.UA -> "Своя сума (+)"
                                                            Language.KK -> "Өз сомасы (+)"
                                                            Language.DE -> "Eigener Betrag (+)"
                                                            Language.ZH -> "自定义充值数额 (+)"
                                                            else -> "Custom amount (+)"
                                                        }
                                                        Text(customAmountLabel)
                                                    },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    val delta = grantCoinsCustomAmount.toIntOrNull()
                                                    if (delta != null && delta > 0) {
                                                        viewModel.adminGiveCredits(uid = tUid, username = tName, amount = delta, isDelta = true)
                                                        viewModel.triggerAudioFeedback("success")
                                                        adminToastMessage = "+$delta 🪙 начислено пользователю $tName"
                                                        grantingCoinsUser = null
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                val grantBtnLabel = when (currentLang) {
                                                    Language.RU -> "НАЧИСЛИТЬ"
                                                    Language.UA -> "НАРАХУВАТИ"
                                                    Language.KK -> "ЕСЕПТЕУ"
                                                    Language.DE -> "VERGEBEN"
                                                    Language.ZH -> "确认充值"
                                                    else -> "GRANT"
                                                }
                                                Text(grantBtnLabel, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { grantingCoinsUser = null }) {
                                                val cancelBtnLabel = when (currentLang) {
                                                    Language.RU -> "Отмена"
                                                    Language.UA -> "Скасувати"
                                                    Language.KK -> "Бас тарту"
                                                    Language.DE -> "Abbrechen"
                                                    Language.ZH -> "取消"
                                                    else -> "Cancel"
                                                }
                                                Text(cancelBtnLabel)
                                            }
                                        }
                                    )
                                }

                                // Full Edit User Modal
                                if (editingAdminUser != null) {
                                    val target = editingAdminUser!!
                                    val tUid = (target["uid"] as? String) ?: ""
                                    val tName = (target["player_name"] as? String) ?: "Player"

                                    AlertDialog(
                                        onDismissRequest = { editingAdminUser = null },
                                        title = {
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Редактирование: $tName"
                                                    Language.UA -> "Редагування: $tName"
                                                    Language.KK -> "Өңдеу: $tName"
                                                    Language.DE -> "Bearbeiten: $tName"
                                                    Language.ZH -> "编辑玩家数据：$tName"
                                                    else -> "Edit Stats: $tName"
                                                },
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
                                                    label = {
                                                    val creditsLabel = when (currentLang) {
                                                        Language.RU -> "Монеты 🪙"
                                                        Language.UA -> "Монети 🪙"
                                                        Language.KK -> "Монеталар 🪙"
                                                        Language.DE -> "Münzen 🪙"
                                                        Language.ZH -> "代币 🪙"
                                                        else -> "Credits 🪙"
                                                    }
                                                    Text(creditsLabel)
                                                },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = editUserHighScore,
                                                        onValueChange = { editUserHighScore = it },
                                                        label = {
                                                        val highScoreLabel = when (currentLang) {
                                                            Language.RU -> "Рекорд"
                                                            Language.UA -> "Рекорд"
                                                            Language.KK -> "Рекорд"
                                                            Language.DE -> "Highscore"
                                                            Language.ZH -> "最高分"
                                                            else -> "High Score"
                                                        }
                                                        Text(highScoreLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = editUserLines,
                                                        onValueChange = { editUserLines = it },
                                                        label = {
                                                        val linesLabel = when (currentLang) {
                                                            Language.RU -> "Линии"
                                                            Language.UA -> "Лінії"
                                                            Language.KK -> "Жолдар"
                                                            Language.DE -> "Linien"
                                                            Language.ZH -> "消除行数"
                                                            else -> "Lines"
                                                        }
                                                        Text(linesLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = editUserGames,
                                                        onValueChange = { editUserGames = it },
                                                        label = {
                                                        val gamesLabel = when (currentLang) {
                                                            Language.RU -> "Игры"
                                                            Language.UA -> "Ігри"
                                                            Language.KK -> "Ойындар"
                                                            Language.DE -> "Spiele"
                                                            Language.ZH -> "对局总数"
                                                            else -> "Games"
                                                        }
                                                        Text(gamesLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = editUserXp,
                                                        onValueChange = { editUserXp = it },
                                                        label = {
                                                        val xpLabel = when (currentLang) {
                                                            Language.RU -> "Опыт"
                                                            Language.UA -> "Досвід"
                                                            Language.KK -> "Тәжірибе"
                                                            Language.DE -> "Bonus-XP"
                                                            Language.ZH -> "经验值"
                                                            else -> "Bonus XP"
                                                        }
                                                        Text(xpLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = editUserTier,
                                                    onValueChange = { editUserTier = it },
                                                    label = {
                                                    val tierLabel = when (currentLang) {
                                                        Language.RU -> "Ранг (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.UA -> "Ранг (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.KK -> "Дәреже (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.DE -> "Rang (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.ZH -> "段位 (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        else -> "Tier"
                                                    }
                                                    Text(tierLabel)
                                                },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val gradientNickLabel = when (currentLang) {
                                                        Language.RU -> "Градиент ника"
                                                        Language.UA -> "Градієнт ніка"
                                                        Language.KK -> "Бүркеншік ат градиенті"
                                                        Language.DE -> "Farbverlauf-Name"
                                                        Language.ZH -> "昵称渐变特效"
                                                        else -> "Gradient Nickname"
                                                    }
                                                    Text(gradientNickLabel, style = MaterialTheme.typography.bodyMedium)
                                                    Switch(checked = editUserGradient, onCheckedChange = { editUserGradient = it })
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val customTagUnlockedLabel = when (currentLang) {
                                                        Language.RU -> "Личный Тег разблокирован"
                                                        Language.UA -> "Особистий Тег розблоковано"
                                                        Language.KK -> "Жеке тег ашылды"
                                                        Language.DE -> "Individueller Tag freigeschaltet"
                                                        Language.ZH -> "个性称号已解锁"
                                                        else -> "Custom Tag Unlocked"
                                                    }
                                                    Text(customTagUnlockedLabel, style = MaterialTheme.typography.bodyMedium)
                                                    Switch(checked = editUserTagUnlocked, onCheckedChange = { editUserTagUnlocked = it })
                                                }
                                                if (editUserTagUnlocked) {
                                                    OutlinedTextField(
                                                        value = editUserTag,
                                                        onValueChange = { editUserTag = it },
                                                        label = {
                                                        val customTagTextLabel = when (currentLang) {
                                                            Language.RU -> "Текст личного тега"
                                                            Language.UA -> "Текст особистого тега"
                                                            Language.KK -> "Жеке тег мәтіні"
                                                            Language.DE -> "Tag-Text"
                                                            Language.ZH -> "个性称号文本"
                                                            else -> "Custom Tag Text"
                                                        }
                                                        Text(customTagTextLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (editUserEmailVerified) Icons.Default.CheckCircle else Icons.Default.MarkEmailUnread,
                                                            contentDescription = null,
                                                            tint = if (editUserEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Подтверждение почты (Verified)", style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                    Switch(checked = editUserEmailVerified, onCheckedChange = { editUserEmailVerified = it })
                                                }

                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                                // Quick action buttons for this user
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.adminGiveAllCosmetics(tName)
                                                            viewModel.triggerAudioFeedback("success")
                                                            adminToastMessage = "Косметика выдана игроку $tName"
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text("Вся косметика", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            if (!tUid.startsWith("local_")) {
                                                                viewModel.adminWipeStatsFirebase(tUid)
                                                            } else {
                                                                viewModel.adminWipeStatsLocal(tName)
                                                            }
                                                            viewModel.triggerAudioFeedback("gameover")
                                                            adminToastMessage = "Статистика $tName сброшена"
                                                            editUserCredits = "750"
                                                            editUserHighScore = "0"
                                                            editUserLines = "0"
                                                            editUserGames = "0"
                                                            editUserXp = "0"
                                                            editUserTier = "BRONZE"
                                                            editUserGradient = false
                                                            editUserTagUnlocked = false
                                                            editUserTag = ""
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text("Сброс статы", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    viewModel.adminSetEmailVerified(tUid, editUserEmailVerified)
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
                                                    adminToastMessage = "Данные $tName сохранены!"
                                                    editingAdminUser = null
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                val saveUserBtnLabel = when (currentLang) {
                                                    Language.RU -> "СОХРАНИТЬ"
                                                    Language.UA -> "ЗБЕРЕГТИ"
                                                    Language.KK -> "САҚТАУ"
                                                    Language.DE -> "SPEICHERN"
                                                    Language.ZH -> "保存修改"
                                                    else -> "SAVE"
                                                }
                                                Text(saveUserBtnLabel, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { editingAdminUser = null }) {
                                                val cancelBtnLabel = when (currentLang) {
                                                    Language.RU -> "Отмена"
                                                    Language.UA -> "Скасувати"
                                                    Language.KK -> "Бас тарту"
                                                    Language.DE -> "Abbrechen"
                                                    Language.ZH -> "取消"
                                                    else -> "Cancel"
                                                }
                                                Text(cancelBtnLabel)
                                            }
                                        }
                                    )
                                }

                                if (showWipeConfirmDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showWipeConfirmDialog = false },
                                        title = { Text("⚠️ Полный вайп базы данных", fontWeight = FontWeight.Black) },
                                        text = { 
                                            Text(
                                                "Вы действительно хотите полностью очистить всю базу данных Firestore (пользователи кроме FsFq, рекорды, лобби, чаты) и локальные аккаунты?",
                                                style = MaterialTheme.typography.bodyMedium
                                            ) 
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    viewModel.adminFullDatabaseWipe()
                                                    viewModel.triggerAudioFeedback("gameover")
                                                    adminToastMessage = "Полный вайп базы данных выполнен!"
                                                    showWipeConfirmDialog = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("УДАЛИТЬ ВСЁ", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showWipeConfirmDialog = false }) {
                                                Text("Отмена")
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
                                    text = "FLOWTESS",
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

                            // Coins Badge
                            Surface(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    viewModel.openRewardedAdDialog()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = java.text.NumberFormat.getIntegerInstance().format(credits),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Watch Ad",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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
                            val classicBtnText = when (currentLang) {
                                Language.RU -> "КЛАССИЧЕСКИЙ РЕЖИМ"
                                Language.UA -> "КЛАСИЧНИЙ РЕЖИМ"
                                Language.KK -> "КЛАССИКАЛЫҚ РЕЖИМ"
                                Language.DE -> "KLASSISCHER MODUS"
                                Language.ZH -> "经典模式"
                                else -> "CLASSIC MODE"
                            }
                            val gameModesBtnText = when (currentLang) {
                                Language.RU -> "РЕЖИМЫ ИГРЫ"
                                Language.UA -> "РЕЖИМИ ГРИ"
                                Language.KK -> "ОЙЫН РЕЖИМДЕРІ"
                                Language.DE -> "SPIELMODI"
                                Language.ZH -> "游戏模式"
                                else -> "GAME MODES"
                            }
                            val multiplayerBtnText = when (currentLang) {
                                Language.RU -> "МУЛЬТИПЛЕЕР"
                                Language.UA -> "МУЛЬТИПЛЕЄР"
                                Language.KK -> "МУЛЬТИПЛЕЕР"
                                Language.DE -> "MEHRSPIELER"
                                Language.ZH -> "多人联机"
                                else -> "MULTIPLAYER"
                            }
                            val friendsBtnText = when (currentLang) {
                                Language.RU -> "ДРУЗЬЯ"
                                Language.UA -> "ДРУЗІ"
                                Language.KK -> "ДОСТАР"
                                Language.DE -> "FREUNDE"
                                Language.ZH -> "好友"
                                else -> "FRIENDS"
                            }

                            MenuButton(
                                text = classicBtnText,
                                iconType = "play",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                            )

                            MenuButton(
                                text = gameModesBtnText,
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
                                text = multiplayerBtnText,
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
                                text = friendsBtnText,
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
                                text = "FLOWTESS",
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

                        // Coins Chip
                        Surface(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.openRewardedAdDialog()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = java.text.NumberFormat.getIntegerInstance().format(credits),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Watch Ad",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        val classicBtnText = when (currentLang) {
                            Language.RU -> "КЛАССИЧЕСКИЙ РЕЖИМ"
                            Language.UA -> "КЛАСИЧНИЙ РЕЖИМ"
                            Language.KK -> "КЛАССИКАЛЫҚ РЕЖИМ"
                            Language.DE -> "KLASSISCHER MODUS"
                            Language.ZH -> "经典模式"
                            else -> "CLASSIC MODE"
                        }
                        val gameModesBtnText = when (currentLang) {
                            Language.RU -> "РЕЖИМЫ ИГРЫ"
                            Language.UA -> "РЕЖИМИ ГРИ"
                            Language.KK -> "ОЙЫН РЕЖИМДЕРІ"
                            Language.DE -> "SPIELMODI"
                            Language.ZH -> "游戏模式"
                            else -> "GAME MODES"
                        }
                        val multiplayerBtnText = when (currentLang) {
                            Language.RU -> "МУЛЬТИПЛЕЕР"
                            Language.UA -> "МУЛЬТИПЛЕЄР"
                            Language.KK -> "МУЛЬТИПЛЕЕР"
                            Language.DE -> "MEHRSPIELER"
                            Language.ZH -> "多人联机"
                            else -> "MULTIPLAYER"
                        }
                        val friendsBtnText = when (currentLang) {
                            Language.RU -> "ДРУЗЬЯ"
                            Language.UA -> "ДРУЗІ"
                            Language.KK -> "ДОСТАР"
                            Language.DE -> "FREUNDE"
                            Language.ZH -> "好友"
                            else -> "FRIENDS"
                        }

                        MenuButton(
                            text = classicBtnText,
                            iconType = "play",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                        )

                        MenuButton(
                            text = gameModesBtnText,
                            iconType = "upgrades",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onModeSelection() }
                        )

                        MenuButton(text = Translations.get("leaderboard", currentLang), iconType = "leaderboard", themeColor = MaterialTheme.colorScheme.primary, onClick = onLeaderboard)

                        MenuButton(
                            text = multiplayerBtnText,
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
                            text = friendsBtnText,
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
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
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
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val purchasedCubeSkinsSet = remember {
                                val sharedPrefs = context.getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
                                sharedPrefs.getStringSet("purchased_cube_skins", setOf("neon")) ?: setOf("neon")
                            }
                            val styles = listOf(
                                "neon" to Translations.getLocalizedCubeSkinTitle("neon", currentLang),
                                "glass" to Translations.getLocalizedCubeSkinTitle("glass", currentLang),
                                "retro" to Translations.getLocalizedCubeSkinTitle("retro", currentLang),
                                "flat" to Translations.getLocalizedCubeSkinTitle("flat", currentLang),
                                "material" to Translations.getLocalizedCubeSkinTitle("material", currentLang)
                            )
                            val purchaseStyleToast = when (currentLang) {
                                Language.RU -> "Купите этот стиль в магазине!"
                                Language.UA -> "Придбайте цей стиль у магазині!"
                                Language.KK -> "Бұл стильді дүкеннен сатып алыңыз!"
                                Language.DE -> "Kaufe diesen Stil im Shop!"
                                Language.ZH -> "请在商店中购买此皮肤风格！"
                                else -> "Purchase this style in the store!"
                            }
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
                                                    purchaseStyleToast,
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
                                                    purchaseStyleToast,
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
                                    Column {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Отступ снизу экрана"
                                                Language.UA -> "Відступ знизу екрана"
                                                Language.KK -> "Төменнен шегініс"
                                                Language.DE -> "Abstand zum unteren Rand"
                                                Language.ZH -> "距离屏幕底部的边距"
                                                else -> "Bottom Screen Margin"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Подгонка под жесты и навигацию"
                                                Language.UA -> "Підгонка під жести"
                                                Language.KK -> "Ыңғайлы орынға келтіру"
                                                Language.DE -> "Anpassung an Gestenleiste"
                                                Language.ZH -> "避免被手势导航条误触"
                                                else -> "Avoid system navigation bar"
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
                                            text = "$controlBottomPadding dp",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                }
                                Slider(
                                    value = controlBottomPadding.toFloat(),
                                    onValueChange = { viewModel.setControlBottomPadding(it.toInt()) },
                                    valueRange = 0f..80f
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
                                                Language.RU -> "Особая благодарность и признательность за неоценимую поддержку развития проекта! ❤️"
                                                Language.UA -> "Особлива подяка та вдячність за неоціненну підтримку розвитку проєкту! ❤️"
                                                Language.KK -> "Жобаның дамуына баға жетпес қолдау көрсеткені үшін ерекше алғыс пен ризашылық! ❤️"
                                                Language.DE -> "Besonderer Dank und herzliche Anerkennung für die unschätzbare Unterstützung dieses Projekts! ❤️"
                                                Language.ZH -> "特别感谢对本项目开发与持续改进做出的无价支持与贡献！❤️"
                                                else -> "Special gratitude and heartfelt thanks for invaluable support of the project! ❤️"
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
        sharedPrefs.getStringSet("purchased_modes", setOf("classic", "extended", "fast_run", "slide", "block_blast"))
            ?: setOf("classic", "extended", "fast_run", "slide", "block_blast")
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
            when (currentLang) {
                Language.RU -> "Стандарт"
                Language.UA -> "Стандарт"
                Language.KK -> "Стандарт"
                Language.DE -> "Standard"
                Language.ZH -> "标准"
                else -> "Standard"
            },
            when (currentLang) {
                Language.RU -> "Оригинальный режим с нарастающей сложностью."
                Language.UA -> "Оригінальний режим із наростаючою складністю."
                Language.KK -> "Өсіп келе жатқан қиындығы бар түпнұсқа режим."
                Language.DE -> "Das originale Erlebnis mit steigender Geschwindigkeit."
                Language.ZH -> "速度逐渐递增的经典原始模式。"
                else -> "The original experience with increasing speed."
            },
            when (currentLang) {
                Language.RU -> "Укладывайте фигурки и заполняйте горизонтальные линии. Каждые 10 очищенных линий повышают уровень и скорость игры."
                Language.UA -> "Укладайте фігурки та заповнюйте горизонтальні лінії. Кожні 10 очищених ліній підвищують рівень і швидкість гри."
                Language.KK -> "Фигураларды орналастырып, көлденең сызықтарды толтырыңыз. Әрбір 10 тазартылған сызық ойын деңгейі мен жылдамдығын арттырады."
                Language.DE -> "Platziere herabfallende Blöcke, um Linien zu vervollständigen. Alle 10 Linien steigen Level und Tempo."
                Language.ZH -> "旋转摆放掉落的方块以消除整行。每消除10行即可提升等级与下落速度。"
                else -> "Fit falling blocks to clear horizontal lines. Every 10 cleared lines increases game level and falling speed."
            },
            0, Icons.Default.VideogameAsset, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.TIME_ATTACK, "time_attack",
            when (currentLang) {
                Language.RU -> "Блиц"
                Language.UA -> "Бліц"
                Language.KK -> "Блиц"
                Language.DE -> "Blitz"
                Language.ZH -> "闪击"
                else -> "Blitz"
            },
            when (currentLang) {
                Language.RU -> "Режим с ограничением времени: старт с 60 секунд."
                Language.UA -> "Режим з обмеженням часу: старт із 60 секунд."
                Language.KK -> "Уақыт шектеуі бар режим: 60 секундтан басталады."
                Language.DE -> "Zeitbegrenzter Modus: Startet bei 60 Sekunden."
                Language.ZH -> "限时竞速模式：从60秒倒计时开始。"
                else -> "Time-limited mode: starts at 60 seconds."
            },
            when (currentLang) {
                Language.RU -> "Игра начинается с 60 секундами на таймере. Очищайте линии, чтобы прибавлять по 10 секунд за каждую линию. Время стремительно иссекает!"
                Language.UA -> "Гра починається з 60 секундами на таймері. Очищайте лінії, щоб додавати по 10 секунд за кожну лінію. Час стрімко спливає!"
                Language.KK -> "Ойын таймерде 60 секундпен басталады. Әр сызық үшін 10 секунд қосу мақсатында сызықтарды тазартыңыз. Уақыт тез таусылады!"
                Language.DE -> "Startet mit 60s auf der Uhr. Jede gelöschte Zeile bringt +10s. Sammle Punkte, bevor die Zeit abläuft!"
                Language.ZH -> "初始计时器为60秒。每消除一行增加10秒时间奖励，在时间耗尽前尽可能斩获高分！"
                else -> "Starts with 60s on the clock. Clear lines to gain +10 seconds per line. Score as much as possible before time runs out!"
            },
            ShopPrices.getModeCost("time_attack"), Icons.Default.Schedule, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.EXTENDED, "extended",
            when (currentLang) {
                Language.RU -> "Спектр"
                Language.UA -> "Спектр"
                Language.KK -> "Спектр"
                Language.DE -> "Spektrum"
                Language.ZH -> "光谱"
                else -> "Spectrum"
            },
            when (currentLang) {
                Language.RU -> "Игра фигурами из пяти блоков для повышенной сложности."
                Language.UA -> "Гра фігурами з п'яти блоків для підвищеної складності."
                Language.KK -> "Жоғары қиындық үшін бес блоктан тұратын фигуралармен ойнау."
                Language.DE -> "Gameplay mit 5-Block-Steinen für zusätzliche Herausforderung."
                Language.ZH -> "使用五格骨牌方块提升挑战难度。"
                else -> "Gameplay using five-block pieces for an extra challenge."
            },
            when (currentLang) {
                Language.RU -> "Режим классического тетриса, но с использованием 5-блочных фигур (пентамино). Заполняйте линии в условиях высокой плотности фигур."
                Language.UA -> "Режим класичного тетрісу, але з використанням 5-блокових фігур (пентаміно). Заповнюйте лінії в умовах високої щільності фігур."
                Language.KK -> "Классикалық тетрис режимі, бірақ 5 блокты фигураларды (пентамино) қолданумен. Тығыз фигуралар жағдайында сызықтарды толтырыңыз."
                Language.DE -> "Klassische Tetris-Regeln mit 5-Block-Pentaminos. Fülle Reihen mit komplexen Formen."
                Language.ZH -> "经典下落消除玩法，但使用复杂的五联块（Pentamino）。在复杂的方块几何中考验空间规划。"
                else -> "Classic tetris rules, but played with 5-block pentamino pieces. Fill rows with complex shapes."
            },
            ShopPrices.getModeCost("extended"), Icons.Default.Star, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.FAST_RUN, "fast_run",
            when (currentLang) {
                Language.RU -> "Спринт"
                Language.UA -> "Спринт"
                Language.KK -> "Спринт"
                Language.DE -> "Sprint"
                Language.ZH -> "冲刺"
                else -> "Sprint"
            },
            when (currentLang) {
                Language.RU -> "Начало игры с 10-го уровня сложности."
                Language.UA -> "Початок гри з 10-го рівня складності."
                Language.KK -> "Ойынды 10-деңгейдегі қиындықтан бастау."
                Language.DE -> "Startet direkt bei Schwierigkeitsgrad Level 10."
                Language.ZH -> "从难度等级10直接开始极速挑战。"
                else -> "Starts the game at Level 10 difficulty."
            },
            when (currentLang) {
                Language.RU -> "Игра сразу начинается на 10 уровне скорости! Требует молниеносной реакции и быстрого принятия решений."
                Language.UA -> "Гра одразу починається на 10 рівні швидкості! Вимагає блискавичної реакції та швидкого прийняття рішень."
                Language.KK -> "Ойын бірден 10 жылдамдық деңгейінде басталады! Жылдам реакция мен жылдам шешім қабылдауды талап етеді."
                Language.DE -> "Startet direkt auf Geschwindigkeitsstufe 10! Erfordert blitzschnelle Reflexe."
                Language.ZH -> "直接以极高下落速度开始！极度考验临场反应与瞬间决策能力。"
                else -> "Game starts immediately at speed level 10! Requires lightning reflexes and fast placement decisions."
            },
            ShopPrices.getModeCost("fast_run"), Icons.Default.FlashOn, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.SLIDE_PUZZLE, "slide",
            when (currentLang) {
                Language.RU -> "Слайдер"
                Language.UA -> "Слайдер"
                Language.KK -> "Слайдер"
                Language.DE -> "Schieberätsel"
                Language.ZH -> "滑块消除"
                else -> "Slide Puzzle"
            },
            when (currentLang) {
                Language.RU -> "Двигайте блоки горизонтально для заполнения линий."
                Language.UA -> "Рухайте блоки горизонтально для заповнення ліній."
                Language.KK -> "Сызықтарды толтыру үшін блоктарды көлденең жылжытыңыз."
                Language.DE -> "Verschiebe Blöcke horizontal, um Linien zu füllen."
                Language.ZH -> "水平滑动方块以填满整行消除并触发重力连击。"
                else -> "Slide blocks horizontally to fill rows and trigger chain falls."
            },
            when (currentLang) {
                Language.RU -> "Сдвигайте блоки влево и вправо. Когда блок падает в пустоту, срабатывает физика гравитации. Заполненные линии уничтожаются, принося комбо!"
                Language.UA -> "Зсувайте блоки вліво та вправо. Коли блок падає в порожнечу, спрацьовує гравітація. Заповнені лінії знищуються!"
                Language.KK -> "Блоктарды солға және оңға жылжытыңыз. Блок бос орынға құлаған кезде гравитация іске қосылады. Толық сызықтар жойылады!"
                Language.DE -> "Schiebe Blöcke nach links oder rechts. Blöcke fallen nach unten. Komplette Reihen explodieren und bringen Kettenreaktionen!"
                Language.ZH -> "左右滑动方块使其落入空缺处。整行填满后立即消除，上方方块受重力下坠可能触发连锁消除与高额连击奖励！"
                else -> "Slide blocks left and right. Falling blocks settle with gravity. Completed lines clear and trigger cascade chain combos!"
            },
            ShopPrices.getModeCost("slide"), Icons.Default.SwapHoriz, "FUN"
        ),
        ModeInfo(
            com.example.game.GameMode.BLOCK_BLAST, "block_blast",
            when (currentLang) {
                Language.RU -> "Zeta"
                Language.UA -> "Zeta"
                Language.KK -> "Zeta"
                Language.DE -> "Zeta"
                Language.ZH -> "Zeta"
                else -> "Zeta"
            },
            when (currentLang) {
                Language.RU -> "Свободное размещение фигурок на игровом поле."
                Language.UA -> "Вільне розміщення фігурок на ігровому полі."
                Language.KK -> "Ойын алаңында фигураларды еркін орналастыру."
                Language.DE -> "Freies Platzieren von Blöcken auf dem Spielfeld."
                Language.ZH -> "在网格中自由拖拽摆放方块。"
                else -> "Free placement of polyominos on the board."
            },
            when (currentLang) {
                Language.RU -> "Перетаскивайте и ставьте процедурно сгенерированные блоки в любую свободную область поля для сбора линий."
                Language.UA -> "Перетягуйте та ставте процедурно згенеровані блоки в будь-яку вільну область поля для збору ліній."
                Language.KK -> "Сызықтарды жинау үшін процедуралық түрде жасалған блоктарды өрістің кез келген бос аймағына сүйреп апарыңыз."
                Language.DE -> "Platziere zufällig generierte Blöcke auf dem Gitter, um Zeilen und Spalten zu leeren."
                Language.ZH -> "将随机生成的方块组合拖放至棋盘任意空位，填满整行或整列进行消除并获取连击积分。"
                else -> "Drag and place procedurally generated blocks anywhere on the grid to clear rows and columns."
            },
            ShopPrices.getModeCost("block_blast"), Icons.Default.Computer, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.PATTERN_PUZZLE, "pattern",
            when (currentLang) {
                Language.RU -> "Шаблон"
                Language.UA -> "Шаблон"
                Language.KK -> "Үлгі"
                Language.DE -> "Muster"
                Language.ZH -> "图腾拼图"
                else -> "Pattern Puzzle"
            },
            when (currentLang) {
                Language.RU -> "Заполните светящийся шаблон на поле заданными блоками."
                Language.UA -> "Заповніть сяючий шаблон на полі заданими блоками."
                Language.KK -> "Берілген блоктармен өрістегі жарқыраған үлгіні толтырыңыз."
                Language.DE -> "Fülle die markierte Schablone mit fallenden Steinen."
                Language.ZH -> "用掉落方块精准填充棋盘上的目标轮廓。"
                else -> "Fill the target outline pattern on the board with falling blocks."
            },
            when (currentLang) {
                Language.RU -> "На поле отображается контур фигуры. Заполните каждую подсвеченную клетку, чтобы пройти уровень!"
                Language.UA -> "На полі відображається контур фігури. Заповніть кожну підсвічену клітинку, щоб пройти рівень!"
                Language.KK -> "Өрісте фигураның контуры көрсетіледі. Деңгейден өту үшін әрбір бөлектелген ұяшықты толтырыңыз!"
                Language.DE -> "Auf dem Feld erscheint eine Schablone. Fülle alle markierten Zellen, um das Level abzuschließen!"
                Language.ZH -> "棋盘上投射出目标轮廓。精准摆放方块填满所有高亮方格即可通关！"
                else -> "A target outline is shown on the grid. Fill every highlighted cell to complete the stage!"
            },
            ShopPrices.getModeCost("pattern"), Icons.Default.AutoAwesome, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.MIRROR_DIMENSION, "mirror",
            when (currentLang) {
                Language.RU -> "Зеркало"
                Language.UA -> "Дзеркало"
                Language.KK -> "Айна"
                Language.DE -> "Spiegel"
                Language.ZH -> "镜界"
                else -> "Mirror"
            },
            when (currentLang) {
                Language.RU -> "Поле отражается по горизонтали во время игры."
                Language.UA -> "Поле відображається по горизонталі під час гри."
                Language.KK -> "Ойын барысында алаң көлденеңінен шағылысады."
                Language.DE -> "Das Spielfeld spiegelt sich horizontal im Spiel."
                Language.ZH -> "游戏过程中棋盘会周期性水平翻转镜像。"
                else -> "The game field is mirrored horizontally during gameplay."
            },
            when (currentLang) {
                Language.RU -> "Игровое поле и падающие блоки периодически отражаются зеркально по горизонтали!"
                Language.UA -> "Ігрове поле та падаючі блоки періодично відображаються дзеркально по горизонталі!"
                Language.KK -> "Ойын алаңы мен құлаған блоктар мезгіл-мезгіл көлденеңінен айнадай шағылысады!"
                Language.DE -> "Das Spielfeld dreht sich periodisch horizontal um und fordert dein räumliches Denken heraus."
                Language.ZH -> "整个棋盘和下落方块会突然水平镜面翻转，颠覆你的空间知觉与操控直觉！"
                else -> "The board periodically flips horizontally, challenging your spatial orientation."
            },
            ShopPrices.getModeCost("mirror"), Icons.Default.SwapHoriz, "FUN"
        ),
        ModeInfo(
            com.example.game.GameMode.MEMORY_PUZZLE, "memory",
            when (currentLang) {
                Language.RU -> "Память"
                Language.UA -> "Пам'ять"
                Language.KK -> "Жады"
                Language.DE -> "Gedächtnis"
                Language.ZH -> "记忆大师"
                else -> "Memory Puzzle"
            },
            when (currentLang) {
                Language.RU -> "Запомните шаблон за 3 секунды до того, как он исчезнет!"
                Language.UA -> "Запам'ятайте шаблон за 3 секунди до того, як він зникне!"
                Language.KK -> "Үлгіні жоғалғанға дейін 3 секунд ішінде есте сақтаңыз!"
                Language.DE -> "Präge dir das Muster in 3 Sekunden ein, bevor es verschwindet!"
                Language.ZH -> "在目标图案隐形前的3秒内记住其位置！"
                else -> "Memorize the pattern in 3 seconds before it disappears!"
            },
            when (currentLang) {
                Language.RU -> "Шаблон показывается всего 3 секунды в начале каждого раунда, после чего исчезает! Соберите его по памяти."
                Language.UA -> "Шаблон показується лише 3 секунди на початку кожного раунду, після чого зникає! Зберіть його по пам'яті."
                Language.KK -> "Үлгі әр раундтың басында тек 3 секунд көрсетіледі, содан кейін жоғалады! Оны жадыңыздан жинаңыз."
                Language.DE -> "Das Muster wird nur 3 Sekunden lang eingeblendet und verschwindet dann! Setze es aus dem Gedächtnis zusammen."
                Language.ZH -> "目标图案仅展示3秒随即隐形！全凭瞬间记忆在脑海中复原并完成方块拼合。"
                else -> "The target pattern is visible for only 3 seconds before vanishing! Fill it entirely from memory."
            },
            ShopPrices.getModeCost("memory"), Icons.Default.Visibility, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.RELAX, "relax",
            when (currentLang) {
                Language.RU -> "Песочница"
                Language.UA -> "Пісочниця"
                Language.KK -> "Құмсалғыш"
                Language.DE -> "Sandbox"
                Language.ZH -> "沙盒"
                else -> "Sandbox"
            },
            when (currentLang) {
                Language.RU -> "Настраиваемый режим с выбором блоков и скоростей."
                Language.UA -> "Налаштовуваний режим з вибором блоків і швидкостей."
                Language.KK -> "Блоктар мен жылдамдықтарды таңдау мүмкіндігі бар бапталатын режим."
                Language.DE -> "Anpassbare Sandbox: Wähle Steine und Geschwindigkeit."
                Language.ZH -> "自由配置方块与速度的自定义沙盒。"
                else -> "Customizable sandbox: select blocks and speed."
            },
            when (currentLang) {
                Language.RU -> "Песочница с гибкой настройкой параметров игры под ваше настроение."
                Language.UA -> "Пісочниця з гнучким налаштуванням параметрів гри під ваш настрій."
                Language.KK -> "Көңіл-күйіңізге сай ойын параметрлерін еркін реттейтін құмсалғыш."
                Language.DE -> "Ein Sandbox-Modus для vollkommen individuelle Spielregeln nach Lust und Laune."
                Language.ZH -> "完全自由定义的沙盒模式，随心定制专属游戏规则和方块类型。"
                else -> "A sandbox mode allowing full customization of gameplay rules to match your preference."
            },
            ShopPrices.getModeCost("relax"), Icons.Default.Spa, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.PERFECTIONIST, "perfectionist",
            when (currentLang) {
                Language.RU -> "Идеал"
                Language.UA -> "Ідеал"
                Language.KK -> "Мінсіз"
                Language.DE -> "Perfektion"
                Language.ZH -> "完美"
                else -> "Perfection"
            },
            when (currentLang) {
                Language.RU -> "Идеальные фигуры и ИИ-подсветка лучшей позиции."
                Language.UA -> "Ідеальні фігури та ШІ-підсвічування кращої позиції."
                Language.KK -> "Мінсіз фигуралар және ЖИ ең жақсы орын нұсқауы."
                Language.DE -> "Ideale Steine & KI-Leitstrahl für perfekte Platzierung."
                Language.ZH -> "极致顺滑方块序列与AI黄金最优落点辅助。"
                else -> "Ideal piece bags & holographic AI optimal placement guide."
            },
            when (currentLang) {
                Language.RU -> "Режим абсолютной гармонии: игра выдает идеальные фигуры для непрерывных линий, а золотой голографический гид подсказывает математически точную позицию сброса! (Награда в 15 раз меньше)."
                Language.UA -> "Режим абсолютної гармонії: ідеальні фігури та золотий голографічний гід! (Нагорода в 15 разів менше)."
                Language.KK -> "Үйлесімділік режимі: мінсіз фигуралар мен алтын голографиялық көмекші! (Сыйақы 15 есе аз)."
                Language.DE -> "Modus der absoluten Harmonie: Perfekte Figuren und goldener KI-Leitstrahl für fehlerfreies Stapeln! (15-fach reduzierte Belohnung)."
                Language.ZH -> "完美秩序模式：系统持续生成最易拼合的方块，全息黄金光标实时指引AI最优摆放位置！（收益为经典模式的1/15）。"
                else -> "Mode of absolute harmony: Ideal piece sequence with real-time golden holographic optimal drop guide! (Rewards scaled 1/15th)."
            },
            ShopPrices.getModeCost("perfectionist"), Icons.Default.Star, "PAID"
        )
    )

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedInfoMode by remember { mutableStateOf<ModeInfo?>(null) }

    val filterChips = listOf(
        "ALL" to when (currentLang) {
            Language.RU -> "Все"
            Language.UA -> "Всі"
            Language.KK -> "Барлығы"
            Language.DE -> "Alle"
            Language.ZH -> "全部"
            else -> "All"
        },
        "FREE" to when (currentLang) {
            Language.RU -> "Бесплатные"
            Language.UA -> "Безкоштовні"
            Language.KK -> "Тегін"
            Language.DE -> "Kostenlos"
            Language.ZH -> "免费"
            else -> "Free"
        },
        "PAID" to when (currentLang) {
            Language.RU -> "Платные"
            Language.UA -> "Платні"
            Language.KK -> "Ақылы"
            Language.DE -> "Kaufbar"
            Language.ZH -> "付费"
            else -> "Paid"
        },
        "HARD" to when (currentLang) {
            Language.RU -> "Сложные"
            Language.UA -> "Складні"
            Language.KK -> "Күрделі"
            Language.DE -> "Schwer"
            Language.ZH -> "高难"
            else -> "Hard"
        },
        "FUN" to when (currentLang) {
            Language.RU -> "Развлечение"
            Language.UA -> "Розваги"
            Language.KK -> "Көңілді"
            Language.DE -> "Casual / Spaß"
            Language.ZH -> "休闲趣味"
            else -> "Casual / Fun"
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(top = 8.dp),
                title = {
                    val selectModeTitle = when (currentLang) {
                        Language.RU -> "РЕЖИМЫ"
                        Language.UA -> "РЕЖИМИ"
                        Language.KK -> "РЕЖИМДЕР"
                        Language.DE -> "MODI"
                        Language.ZH -> "游戏模式"
                        else -> "MODES"
                    }
                    AdaptiveText(
                        text = selectModeTitle,
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

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterChips) { (key, label) ->
                    val isSelected = selectedCategoryFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            selectedCategoryFilter = key
                        },
                        label = {
                            AdaptiveText(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColorVal,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }

            val filteredModes = remember(selectedCategoryFilter, modes) {
                when (selectedCategoryFilter) {
                    "FREE" -> modes.filter { it.cost == 0 }
                    "PAID" -> modes.filter { it.cost > 0 }
                    "HARD" -> modes.filter { it.categoryTag == "HARD" }
                    "FUN" -> modes.filter { it.categoryTag == "FUN" }
                    else -> modes
                }
            }

            run {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (filteredModes.isEmpty()) {
                        item {
                            val emptyCategoryMsg = when (currentLang) {
                                Language.RU -> "Нет режимов в этой категории"
                                Language.UA -> "Немає режимів у цій категорії"
                                Language.KK -> "Бұл санатта режимдер жоқ"
                                Language.DE -> "Keine Modi in dieser Kategorie"
                                Language.ZH -> "该分类下暂无游戏模式"
                                else -> "No modes found in this category"
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emptyCategoryMsg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(filteredModes.size) { index ->
                        val modeInfo = filteredModes[index]
                        val isPurchased = purchasedModesSet.contains(modeInfo.modeId)
                        val isPlayable = isPurchased || modeInfo.cost == 0


                        val modeAcquiredToast = when (currentLang) {
                            Language.RU -> "Режим разблокирован!"
                            Language.UA -> "Режим розблоковано!"
                            Language.KK -> "Режим ашылды!"
                            Language.DE -> "Spielmodus freigeschaltet!"
                            Language.ZH -> "模式解锁成功！"
                            else -> "Game Mode acquired!"
                        }
                        val insufficientCreditsToast = when (currentLang) {
                            Language.RU -> "Недостаточно кредитов!"
                            Language.UA -> "Недостатньо кредитів!"
                            Language.KK -> "Кредит жеткіліксіз!"
                            Language.DE -> "Nicht genügend Credits!"
                            Language.ZH -> "金币余额不足！"
                            else -> "Insufficient credits!"
                        }

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
                                        lockedModeWarning = modeAcquiredToast
                                    } else {
                                        viewModel.triggerAudioFeedback("error")
                                        lockedModeWarning = insufficientCreditsToast
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
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
                                            val freeTag = when (currentLang) {
                                                Language.RU -> "БЕСПЛАТНО"
                                                Language.UA -> "БЕЗКОШТОВНО"
                                                Language.KK -> "ТЕГІН"
                                                Language.DE -> "GRATIS"
                                                Language.ZH -> "免费"
                                                else -> "FREE"
                                            }
                                            val ownedTag = when (currentLang) {
                                                Language.RU -> "КУПЛЕНО"
                                                Language.UA -> "КУПЛЕНО"
                                                Language.KK -> "САТЫП АЛЫНДЫ"
                                                Language.DE -> "GEKAUFT"
                                                Language.ZH -> "已拥有"
                                                else -> "OWNED"
                                            }
                                            val crSuffix = when (currentLang) {
                                                Language.RU -> "КР."
                                                Language.UA -> "КР."
                                                Language.KK -> "КР."
                                                Language.DE -> "CR"
                                                Language.ZH -> "代币"
                                                else -> "CR"
                                            }
                                            val hardTag = when (currentLang) {
                                                Language.RU -> "СЛОЖНЫЙ"
                                                Language.UA -> "СКЛАДНИЙ"
                                                Language.KK -> "КҮРДЕЛІ"
                                                Language.DE -> "SCHWER"
                                                Language.ZH -> "困难"
                                                else -> "HARD"
                                            }
                                            val casualTag = when (currentLang) {
                                                Language.RU -> "ФАН"
                                                Language.UA -> "ФАН"
                                                Language.KK -> "КӨҢІЛДІ"
                                                Language.DE -> "CASUAL"
                                                Language.ZH -> "休闲"
                                                else -> "CASUAL"
                                            }
                                            val standardTag = when (currentLang) {
                                                Language.RU -> "СТАНДАРТ"
                                                Language.UA -> "СТАНДАРТ"
                                                Language.KK -> "СТАНДАРТ"
                                                Language.DE -> "STANDARD"
                                                Language.ZH -> "标准"
                                                else -> "STANDARD"
                                            }

                                            if (modeInfo.cost == 0) {
                                                list.add(freeTag to themeColorVal)
                                            } else if (isPurchased) {
                                                list.add(ownedTag to Color(0xFF4CAF50))
                                            } else {
                                                list.add("${modeInfo.cost} $crSuffix" to Color(0xFFFFB300))
                                            }

                                            when (modeInfo.categoryTag) {
                                                "HARD" -> list.add(hardTag to Color(0xFFFF5252))
                                                "FUN" -> list.add(casualTag to Color(0xFF00E5FF))
                                                else -> list.add(standardTag to themeColorVal)
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
                                        val playLabel = when (currentLang) {
                                            Language.RU -> "ИГРАТЬ"
                                            Language.UA -> "ГРАТИ"
                                            Language.KK -> "ОЙНАУ"
                                            Language.DE -> "SPIELEN"
                                            Language.ZH -> "开始游戏"
                                            else -> "PLAY"
                                        }
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
                                                text = playLabel,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    } else {
                                        val unlockPrefix = when (currentLang) {
                                            Language.RU -> "ОТКРЫТЬ ЗА"
                                            Language.UA -> "ВІДКРИТИ ЗА"
                                            Language.KK -> "МЫНАҒАН АШУ"
                                            Language.DE -> "KAUFEN FÜR"
                                            Language.ZH -> "解锁花费"
                                            else -> "UNLOCK FOR"
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                if (credits >= modeInfo.cost) {
                                                    val updated = purchasedModesSet.toMutableSet().apply { add(modeInfo.modeId) }
                                                    viewModel.spendCredits(modeInfo.cost)
                                                    val sharedPrefs = viewModel.getApplication<android.app.Application>()
                                                        .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
                                                    sharedPrefs.edit().putStringSet("purchased_modes", updated).apply()
                                                    viewModel.triggerAudioFeedback("buy")
                                                    lockedModeWarning = modeAcquiredToast
                                                } else {
                                                    viewModel.triggerAudioFeedback("error")
                                                    lockedModeWarning = insufficientCreditsToast
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
                                                text = "$unlockPrefix ${modeInfo.cost} 🪙",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
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

    // Info Dialog showing detailed rules/explanation for the selected mode
    selectedInfoMode?.let { info ->
        val freeModeLabel = when (currentLang) {
            Language.RU -> "Бесплатный режим"
            Language.UA -> "Безкоштовний режим"
            Language.KK -> "Тегін режим"
            Language.DE -> "Kostenloser Modus"
            Language.ZH -> "免费模式"
            else -> "Free Mode"
        }
        val costPrefix = when (currentLang) {
            Language.RU -> "Стоимость:"
            Language.UA -> "Вартість:"
            Language.KK -> "Құны:"
            Language.DE -> "Kosten:"
            Language.ZH -> "价格："
            else -> "Cost:"
        }
        val gotItLabel = when (currentLang) {
            Language.RU -> "ПОНЯТНО"
            Language.UA -> "ЗРОЗУМІЛО"
            Language.KK -> "ТҮСІНІКТІ"
            Language.DE -> "VERSTANDEN"
            Language.ZH -> "我知道了"
            else -> "GOT IT"
        }
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
                                text = if (info.cost == 0) freeModeLabel else "$costPrefix ${info.cost} 🪙",
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
                val gotItLabel = when (currentLang) {
                    Language.RU -> "ПОНЯТНО"
                    Language.UA -> "ЗРОЗУМІЛО"
                    Language.KK -> "ТҮСІНІКТІ"
                    Language.DE -> "VERSTANDEN"
                    Language.ZH -> "我知道了"
                    else -> "GOT IT"
                }
                Button(
                    onClick = { selectedInfoMode = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(gotItLabel)
                }
            }
        )
    }

    if (lockedModeWarning != null) {
        val systemAlertTitle = when (currentLang) {
            Language.RU -> "Внимание"
            Language.UA -> "Увага"
            Language.KK -> "Назар аударыңыз"
            Language.DE -> "Hinweis"
            Language.ZH -> "系统提示"
            else -> "System Alert"
        }
        AlertDialog(
            onDismissRequest = { lockedModeWarning = null },
            title = { AdaptiveText(systemAlertTitle) },
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
    val controlDas by viewModel.controlDas.collectAsStateWithLifecycle()
    val controlArr by viewModel.controlArr.collectAsStateWithLifecycle()
    val controlBottomPadding by viewModel.controlBottomPadding.collectAsStateWithLifecycle()
    val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()
    val purchasedControlButtonStyles by viewModel.purchasedControlButtonStyles.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCodeText by remember { mutableStateOf("") }
    var importErrorText by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

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

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Сбросить управление?"
                        Language.UA -> "Скинути керування?"
                        Language.KK -> "Басқаруды қайтару?"
                        Language.DE -> "Steuerung zurücksetzen?"
                        Language.ZH -> "重置按键设置？"
                        else -> "Reset controls?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Вернуть все параметры кнопок, таймингов и макета к рекомендованным настройкам?"
                        Language.UA -> "Повернути всі параметри кнопок, таймінгів та макета до рекомендованих налаштувань?"
                        Language.KK -> "Барлық баптауларды бастапқы қалыпқа қайтару керек пе?"
                        Language.DE -> "Alle Tasten-, Timing- und Layout-Einstellungen auf Standardwerte zurücksetzen?"
                        Language.ZH -> "是否将所有按键、延迟与布局参数恢复为推荐默认值？"
                        else -> "Reset all button sizes, timings, and layout to recommended defaults?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetControlConfigToDefaults()
                        viewModel.triggerAudioFeedback("click")
                        showResetDialog = false
                        android.widget.Toast.makeText(
                            context,
                            when (currentLang) {
                                Language.RU -> "Настройки управления сброшены к стандартным"
                                Language.UA -> "Налаштування керування скинуто"
                                Language.KK -> "Басқару баптаулары бастапқы күйге қайтарылды"
                                Language.DE -> "Steuerungseinstellungen zurückgesetzt"
                                Language.ZH -> "按键设置已恢复默认"
                                else -> "Control settings reset to default"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Сбросить"
                            Language.UA -> "Скинути"
                            Language.KK -> "Қайтару"
                            Language.DE -> "Zurücksetzen"
                            Language.ZH -> "重置"
                            else -> "Reset"
                        },
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Отмена"
                            Language.UA -> "Скасувати"
                            Language.KK -> "Болдырмау"
                            Language.DE -> "Abbrechen"
                            Language.ZH -> "取消"
                            else -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importErrorText = null
            },
            title = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Импорт кода управления"
                        Language.UA -> "Імпорт коду керування"
                        Language.KK -> "Басқару кодын импорттау"
                        Language.DE -> "Steuerungscode importieren"
                        Language.ZH -> "导入按键配置代码"
                        else -> "Import Control Code"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Вставьте код конфигурации (TTR-CTRL:... или Base64 JSON):"
                            Language.UA -> "Вставте код конфігурації (TTR-CTRL:... або Base64 JSON):"
                            Language.KK -> "Конфигурация кодын қойыңыз:"
                            Language.DE -> "Konfigurationscode einfügen (TTR-CTRL:... oder Base64):"
                            Language.ZH -> "粘贴配置代码（TTR-CTRL:... 或 Base64 JSON）："
                            else -> "Paste configuration code (TTR-CTRL:... or Base64 JSON):"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importCodeText,
                        onValueChange = {
                            importCodeText = it
                            importErrorText = null
                        },
                        placeholder = { Text("TTR-CTRL:...") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        isError = importErrorText != null
                    )
                    if (importErrorText != null) {
                        Text(
                            text = importErrorText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    TextButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                importCodeText = clip.trim()
                                importErrorText = null
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Вставить из буфера"
                                Language.UA -> "Вставити з буфера"
                                Language.KK -> "Буферден қою"
                                Language.DE -> "Aus Zwischenablage einfügen"
                                Language.ZH -> "从剪贴板粘贴"
                                else -> "Paste from clipboard"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importControlConfigCode(importCodeText)
                        if (success) {
                            viewModel.triggerAudioFeedback("click")
                            showImportDialog = false
                            importCodeText = ""
                            importErrorText = null
                            android.widget.Toast.makeText(
                                context,
                                when (currentLang) {
                                    Language.RU -> "Конфигурация управления успешно применена!"
                                    Language.UA -> "Конфігурацію успішно застосовано!"
                                    Language.KK -> "Басқару конфигурациясы сәтті қолданылды!"
                                    Language.DE -> "Steuerungskonfiguration erfolgreich übernommen!"
                                    Language.ZH -> "按键配置已成功应用！"
                                    else -> "Control configuration applied successfully!"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.triggerAudioFeedback("error")
                            importErrorText = when (currentLang) {
                                Language.RU -> "Неверный формат кода управления!"
                                Language.UA -> "Невірний формат коду керування!"
                                Language.KK -> "Басқару кодының форматы қате!"
                                Language.DE -> "Ungültiges Code-Format!"
                                Language.ZH -> "配置代码格式错误！"
                                else -> "Invalid control code format!"
                            }
                        }
                    }
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Применить"
                            Language.UA -> "Застосувати"
                            Language.KK -> "Қолдану"
                            Language.DE -> "Anwenden"
                            Language.ZH -> "应用"
                            else -> "Apply"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        importErrorText = null
                    }
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Отмена"
                            Language.UA -> "Скасувати"
                            Language.KK -> "Болдырмау"
                            Language.DE -> "Abbrechen"
                            Language.ZH -> "取消"
                            else -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "КАСТОМИЗАЦИЯ УПРАВЛЕНИЯ"
                            Language.UA -> "КАСТОМІЗАЦІЯ КЕРУВАННЯ"
                            Language.KK -> "БАСҚАРУДЫ ТЕҢШЕУ"
                            Language.DE -> "STEUERUNG ANPASSEN"
                            Language.ZH -> "自定义按键布局"
                            else -> "CUSTOMIZE CONTROLS"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset to defaults")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Modern MD3 Navigation Tab Row
            val tabs = listOf(
                when (currentLang) {
                    Language.RU -> "Макет"
                    Language.UA -> "Макет"
                    Language.KK -> "Макет"
                    Language.DE -> "Layout"
                    Language.ZH -> "布局"
                    else -> "Layout"
                } to Icons.Default.Gamepad,
                when (currentLang) {
                    Language.RU -> "Параметры"
                    Language.UA -> "Параметри"
                    Language.KK -> "Баптаулар"
                    Language.DE -> "Parameter"
                    Language.ZH -> "参数"
                    else -> "Settings"
                } to Icons.Default.Tune,
                when (currentLang) {
                    Language.RU -> "Код"
                    Language.UA -> "Код"
                    Language.KK -> "Код"
                    Language.DE -> "Code"
                    Language.ZH -> "代码"
                    else -> "Code"
                } to Icons.Default.Share,
                when (currentLang) {
                    Language.RU -> "Песочница"
                    Language.UA -> "Пісочниця"
                    Language.KK -> "Сынақ"
                    Language.DE -> "Sandbox"
                    Language.ZH -> "测试"
                    else -> "Sandbox"
                } to Icons.Default.PlayArrow
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            selectedTab = index
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }

            when (selectedTab) {
                // TAB 0: LAYOUT PRESETS, HANDEDNESS, VERTICAL POS & VISUAL STYLE
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Presets Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "СХЕМА РАСПОЛОЖЕНИЯ КНОПОК"
                                        Language.UA -> "СХЕМА РОЗТАШУВАННЯ КНОПОК"
                                        Language.KK -> "БАТЫРМАЛАР ОРНАЛАСУЫ"
                                        Language.DE -> "TASTEN-LAYOUT SCHEME"
                                        Language.ZH -> "按键布局方案"
                                        else -> "BUTTON LAYOUT SCHEME"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                val presetList = listOf(
                                    "split" to (Translations.getLocalizedControlPresetName("split", currentLang) to when (currentLang) {
                                        Language.RU -> "Для двух больших пальцев по краям экрана"
                                        Language.UA -> "Для двох великих пальців по краях"
                                        Language.KK -> "Екі бас бармаққа арналған"
                                        Language.DE -> "Geteilte Zweihand-Steuerung"
                                        Language.ZH -> "双拇指边缘分列布局"
                                        else -> "Two-thumb ergonomics on edges"
                                    }),
                                    "classic" to (Translations.getLocalizedControlPresetName("classic", currentLang) to when (currentLang) {
                                        Language.RU -> "4 кнопки в ряд + сброс и удержание"
                                        Language.UA -> "4 кнопки в ряд + скидання і утримання"
                                        Language.KK -> "Қатардағы 4 батырма"
                                        Language.DE -> "4 Tasten in Reihe + Drop/Hold"
                                        Language.ZH -> "4键排布 + 底部下落/暂存"
                                        else -> "4 inline buttons + drop/hold"
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
                                        Language.DE -> "Kompakt links для гри однією рукою"
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

                                presetList.forEach { (key, info) ->
                                    val (title, desc) = info
                                    val isSelected = controlStyle == key
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                        border = if (isSelected) BorderStroke(1.5.dp, themeColorVal) else null,
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
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Handedness & Vertical Position Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ОРИЕНТАЦИЯ И ПОЗИЦИЯ"
                                        Language.UA -> "ОРІЄНТАЦІЯ ТА ПОЗИЦІЯ"
                                        Language.KK -> "БАҒЫТ ПЕН ОРНЫ"
                                        Language.DE -> "AUSRICHTUNG & POSITION"
                                        Language.ZH -> "手位与垂直位置"
                                        else -> "ORIENTATION & POSITION"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                // Left handed switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Режим для левши (зеркальный)"
                                                Language.UA -> "Режим для шульги (дзеркальний)"
                                                Language.KK -> "Солақай режимі"
                                                Language.DE -> "Linkshänder-Modus (gespiegelt)"
                                                Language.ZH -> "左手镜像模式"
                                                else -> "Left-handed mode (mirrored)"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Меняет местами блок движений и блок действий"
                                                Language.UA -> "Міняє місцями рух і дію"
                                                Language.KK -> "Қозғалыс пен әрекет орындарын ауыстырады"
                                                Language.DE -> "Tauscht Bewegung und Aktionen"
                                                Language.ZH -> "对调移动与动作操作区"
                                                else -> "Swaps motion and action blocks"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = leftHandedControls,
                                        onCheckedChange = {
                                            viewModel.setLeftHandedControls(it)
                                            viewModel.triggerAudioFeedback("click")
                                        }
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Vertical Position
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Высота панели управления"
                                            Language.UA -> "Висота панелі керування"
                                            Language.KK -> "Басқару панелінің биіктігі"
                                            Language.DE -> "Vertikale Position"
                                            Language.ZH -> "操作面板垂直位置"
                                            else -> "Vertical Position"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val positions = listOf(
                                            "bottom" to when (currentLang) {
                                                Language.RU -> "Снизу"
                                                Language.UA -> "Знизу"
                                                Language.KK -> "Төменде"
                                                Language.DE -> "Unten"
                                                Language.ZH -> "底部"
                                                else -> "Bottom"
                                            },
                                            "middle" to when (currentLang) {
                                                Language.RU -> "По центру"
                                                Language.UA -> "По центру"
                                                Language.KK -> "Ортада"
                                                Language.DE -> "Mitte"
                                                Language.ZH -> "居中"
                                                else -> "Middle"
                                            },
                                            "top" to when (currentLang) {
                                                Language.RU -> "Сверху"
                                                Language.UA -> "Зверху"
                                                Language.KK -> "Жоғарыда"
                                                Language.DE -> "Oben"
                                                Language.ZH -> "顶部"
                                                else -> "Top"
                                            }
                                        )
                                        positions.forEach { (posKey, label) ->
                                            val isSel = controlVerticalPosition == posKey
                                            FilterChip(
                                                selected = isSel,
                                                onClick = {
                                                    viewModel.setControlVerticalPosition(posKey)
                                                    viewModel.triggerAudioFeedback("click")
                                                },
                                                label = { Text(label) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Button Visual Style Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ВИЗУАЛЬНЫЙ СТИЛЬ КНОПОК"
                                        Language.UA -> "ВІЗУАЛЬНИЙ СТИЛЬ КНОПОК"
                                        Language.KK -> "БАТЫРМАЛАР СТИЛІ"
                                        Language.DE -> "TASTEN-DESIGN"
                                        Language.ZH -> "按键外观风格"
                                        else -> "BUTTON VISUAL THEME"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                val styles = listOf(
                                    "neon" to "Neon Glow",
                                    "classic" to "Classic Solid",
                                    "glass" to "Glassmorphism",
                                    "gold" to "Golden Edge",
                                    "plasma" to "Plasma Cyber"
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    styles.forEach { (key, name) ->
                                        val isOwned = key == "classic" || key == "neon" || purchasedControlButtonStyles.contains(key)
                                        val isSel = controlButtonStyle == key
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                if (isOwned) {
                                                    viewModel.setControlButtonStyle(key)
                                                    viewModel.triggerAudioFeedback("click")
                                                } else {
                                                    viewModel.triggerAudioFeedback("error")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        when (currentLang) {
                                                            Language.RU -> "Купите этот стиль в магазине!"
                                                            Language.UA -> "Придбайте цей стиль у магазині!"
                                                            Language.KK -> "Бұл стильді дүкеннен алыңыз!"
                                                            Language.DE -> "Kaufe diesen Stil im Shop!"
                                                            Language.ZH -> "请在商店中解锁该风格！"
                                                            else -> "Unlock this style in store!"
                                                        },
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            label = { Text(name) },
                                            leadingIcon = if (!isOwned) {
                                                { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                            } else if (isSel) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }

                        // Live Preview Box & Sandbox CTA
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Предпросмотр раскладки:"
                                        Language.UA -> "Попередній перегляд:"
                                        Language.KK -> "Орналасуды алдын ала көру:"
                                        Language.DE -> "Layout-Vorschau:"
                                        Language.ZH -> "按键布局实时预览："
                                        else -> "Layout Preview:"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GameControlsSection(
                                        viewModel = viewModel,
                                        gameState = gameState,
                                        controlStyle = controlStyle,
                                        leftHandedControls = leftHandedControls,
                                        controlVerticalPosition = "bottom",
                                        controlButtonScale = (controlButtonScale * 0.85f).coerceIn(0.55f, 1.15f),
                                        controlButtonStyle = controlButtonStyle,
                                        onLeftPress = { viewModel.triggerAudioFeedback("move") },
                                        onRightPress = { viewModel.triggerAudioFeedback("move") },
                                        onDownPress = { viewModel.triggerAudioFeedback("move") },
                                        onRotatePress = { viewModel.triggerAudioFeedback("rotate") },
                                        onHardDropPress = { viewModel.triggerAudioFeedback("drop") },
                                        onHoldPress = { viewModel.triggerAudioFeedback("click") }
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        selectedTab = 3 // Switch to Sandbox
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Тестировать в игре (Песочница)"
                                            Language.UA -> "Тестувати в грі (Пісочниця)"
                                            Language.KK -> "Ойында сынап көру"
                                            Language.DE -> "In Sandbox testen"
                                            Language.ZH -> "进入实战沙盒测试"
                                            else -> "Test in Sandbox Game"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 1: GEOMETRY, SIZES, PADDING, DAS & ARR TIMINGS
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Button Scale & Opacity Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "РАЗМЕР И ПРОЗРАЧНОСТЬ"
                                        Language.UA -> "РОЗМІР ТА ПРОЗОРІСТЬ"
                                        Language.KK -> "ӨЛШЕМ МЕН МӨЛДІРЛІК"
                                        Language.DE -> "GRÖSSE & TRANSPARENZ"
                                        Language.ZH -> "按键尺寸与透明度"
                                        else -> "SIZE & OPACITY"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                // Scale Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Размер кнопок"
                                                Language.UA -> "Розмір кнопок"
                                                Language.KK -> "Батырмалар өлшемі"
                                                Language.DE -> "Tastengröße"
                                                Language.ZH -> "按键大小"
                                                else -> "Button Size"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${(controlButtonScale * 100).toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlButtonScale,
                                        onValueChange = { viewModel.setControlButtonScale(it) },
                                        valueRange = 0.5f..1.5f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(0.75f to "75%", 1.0f to "100%", 1.25f to "125%").forEach { (sc, label) ->
                                            AssistChip(
                                                onClick = { viewModel.setControlButtonScale(sc) },
                                                label = { Text(label) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Alpha Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Прозрачность кнопок"
                                                Language.UA -> "Прозорість кнопок"
                                                Language.KK -> "Мөлдірлігі"
                                                Language.DE -> "Deckkraft"
                                                Language.ZH -> "按键透明度"
                                                else -> "Button Opacity"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${(controlButtonAlpha * 100).toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlButtonAlpha,
                                        onValueChange = { viewModel.setControlButtonAlpha(it) },
                                        valueRange = 0.1f..1.0f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Bottom Padding
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Отступ снизу (для навигации)"
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
                                                    Language.RU -> "Защита от случайных срабатываний жестов"
                                                    Language.UA -> "Захист від випадкових жестів"
                                                    Language.KK -> "Ым-ишарадан қорғау"
                                                    Language.DE -> "Schutz vor Gestenleiste"
                                                    Language.ZH -> "防止被手势小白条误触"
                                                    else -> "Prevents accidental gesture trigger"
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
                            }
                        }

                        // Timings Tuning Card (DAS & ARR)
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ЧУВСТВИТЕЛЬНОСТЬ И ТАЙМИНГИ (DAS / ARR)"
                                        Language.UA -> "ЧУТЛИВІСТЬ ТА ВІДГУК (DAS / ARR)"
                                        Language.KK -> "СЕЗІМТАЛДЫҚ ПЕН ЖЫЛДАМДЫҚ"
                                        Language.DE -> "ANSPRECHVERHALTEN (DAS / ARR)"
                                        Language.ZH -> "按键灵敏度 (DAS / ARR)"
                                        else -> "TIMINGS & RESPONSE (DAS / ARR)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                // DAS Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "DAS (Задержка зажатия)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Время до непрерывного сдвига",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "$controlDas мс",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlDas.toFloat(),
                                        onValueChange = { viewModel.setControlDas(it.toInt()) },
                                        valueRange = 80f..300f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(120 to "Pro (120мс)", 160 to "Стандарт", 220 to "Мягкий").forEach { (d, label) ->
                                            AssistChip(
                                                onClick = { viewModel.setControlDas(d) },
                                                label = { Text(label) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // ARR Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "ARR (Скорость авто-повтора)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Интервал между шагами (меньше = быстрее)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "$controlArr мс",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlArr.toFloat(),
                                        onValueChange = { viewModel.setControlArr(it.toInt()) },
                                        valueRange = 16f..80f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(20 to "Турбо (20мс)", 35 to "Стандарт", 50 to "Плавный").forEach { (a, label) ->
                                            AssistChip(
                                                onClick = { viewModel.setControlArr(a) },
                                                label = { Text(label) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: EXPORT / IMPORT CONFIG CODE
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Config Code Box
                        val currentCode = remember(controlStyle, controlButtonScale, controlButtonAlpha, controlButtonStyle, controlVerticalPosition, leftHandedControls, controlDas, controlArr, controlBottomPadding) {
                            viewModel.exportControlConfigCode()
                        }

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ТЕКУЩИЙ КОД УПРАВЛЕНИЯ"
                                        Language.UA -> "ПОТОЧНИЙ КОД КЕРУВАННЯ"
                                        Language.KK -> "АҒЫМДАҒЫ БАСҚАРУ КОДЫ"
                                        Language.DE -> "AKTUELLER STEUERUNGSCODE"
                                        Language.ZH -> "当前操作配置代码"
                                        else -> "CURRENT CONTROL CODE"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Этот код содержит всю вашу раскладку, масштаб, прозрачность, отступы и задержки DAS/ARR. Скопируйте и поделитесь с друзьями:"
                                        Language.UA -> "Цей код містить усі ваші налаштування. Скопіюйте та поділіться з друзями:"
                                        Language.KK -> "Бұл код барлық параметрлерді сақтайды. Көшіріп, достарыңызбен бөлісіңіз:"
                                        Language.DE -> "Dieser Code enthält alle Parameter. Kopieren und mit Freunden teilen:"
                                        Language.ZH -> "此代码包含您的全部按键布局、尺寸、底边距及DAS/ARR延迟，可一键分享："
                                        else -> "This code encapsulates all your layout, scale, opacity, margins and DAS/ARR timings. Copy to share with friends:"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentCode,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(12.dp),
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentCode))
                                            android.widget.Toast.makeText(
                                                context,
                                                when (currentLang) {
                                                    Language.RU -> "Код управления скопирован в буфер обмена!"
                                                    Language.UA -> "Код скопійовано в буфер!"
                                                    Language.KK -> "Басқару коды көшірілді!"
                                                    Language.DE -> "Code in Zwischenablage kopiert!"
                                                    Language.ZH -> "控制代码已复制到剪贴板！"
                                                    else -> "Control code copied to clipboard!"
                                                },
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Копировать"
                                                Language.UA -> "Копіювати"
                                                Language.KK -> "Көшіру"
                                                Language.DE -> "Kopieren"
                                                Language.ZH -> "复制"
                                                else -> "Copy"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            try {
                                                val sendIntent = android.content.Intent().apply {
                                                    action = android.content.Intent.ACTION_SEND
                                                    putExtra(
                                                        android.content.Intent.EXTRA_TEXT,
                                                        "Tetris Controls Config: $currentCode"
                                                    )
                                                    type = "text/plain"
                                                }
                                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                                context.startActivity(shareIntent)
                                            } catch (e: Exception) {
                                                // Fallback copy if no share handler
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentCode))
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Поделиться"
                                                Language.UA -> "Поділитися"
                                                Language.KK -> "Бөлісу"
                                                Language.DE -> "Teilen"
                                                Language.ZH -> "分享"
                                                else -> "Share"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Import Code Section
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ИМПОРТ КОДА УПРАВЛЕНИЯ"
                                        Language.UA -> "ІМПОРТ КОДУ КЕРУВАННЯ"
                                        Language.KK -> "КОДТЫ ИМПОРТТАУ"
                                        Language.DE -> "CODE IMPORTIEREN"
                                        Language.ZH -> "导入已有配置代码"
                                        else -> "IMPORT CONTROL CODE"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Вставьте код управления, полученный от другого игрока, чтобы мгновенно применить его настройки:"
                                        Language.UA -> "Вставте код керування від іншого гравця:"
                                        Language.KK -> "Басқа ойыншының кодын қойыңыз:"
                                        Language.DE -> "Füge einen Code ein, um die Steuerung zu übernehmen:"
                                        Language.ZH -> "粘贴其他玩家分享的控制代码以立即套用："
                                        else -> "Paste a code from another player to instantly apply their layout:"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = importCodeText,
                                    onValueChange = {
                                        importCodeText = it
                                        importErrorText = null
                                    },
                                    placeholder = { Text("TTR-CTRL:...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = importErrorText != null,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                val clip = clipboardManager.getText()?.text
                                                if (!clip.isNullOrBlank()) {
                                                    importCodeText = clip.trim()
                                                    importErrorText = null
                                                    viewModel.triggerAudioFeedback("click")
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste from clipboard")
                                        }
                                    }
                                )

                                if (importErrorText != null) {
                                    Text(
                                        text = importErrorText ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Button(
                                    onClick = {
                                        val ok = viewModel.importControlConfigCode(importCodeText)
                                        if (ok) {
                                            viewModel.triggerAudioFeedback("click")
                                            importCodeText = ""
                                            importErrorText = null
                                            android.widget.Toast.makeText(
                                                context,
                                                when (currentLang) {
                                                    Language.RU -> "Конфигурация успешно загружена!"
                                                    Language.UA -> "Конфігурацію успішно завантажено!"
                                                    Language.KK -> "Конфигурация сәтті жүктелді!"
                                                    Language.DE -> "Konfiguration erfolgreich geladen!"
                                                    Language.ZH -> "配置已成功套用！"
                                                    else -> "Configuration loaded successfully!"
                                                },
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            viewModel.triggerAudioFeedback("error")
                                            importErrorText = when (currentLang) {
                                                Language.RU -> "Неверный код! Проверьте формат."
                                                Language.UA -> "Невірний код! Перевірте формат."
                                                Language.KK -> "Қате код! Форматты тексеріңіз."
                                                Language.DE -> "Ungültiger Code! Prüfen."
                                                Language.ZH -> "代码无效！请检查格式。"
                                                else -> "Invalid code! Check format."
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Применить этот код"
                                            Language.UA -> "Застосувати цей код"
                                            Language.KK -> "Осы кодты қолдану"
                                            Language.DE -> "Diesen Code anwenden"
                                            Language.ZH -> "立即应用此代码"
                                            else -> "Apply this code"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Reset Button
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Сбросить управление к рекомендованному"
                                    Language.UA -> "Скинути до рекомендованого"
                                    Language.KK -> "Басқаруды стандартқа қайтару"
                                    Language.DE -> "Auf Standardwerte zurücksetzen"
                                    Language.ZH -> "恢复推荐默认按键设置"
                                    else -> "Reset controls to recommended defaults"
                                }
                            )
                        }
                    }
                }

                // TAB 3: PLAYABLE SANDBOX
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Sandbox banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "🕹️ Песочница: поле авто-очищается"
                                        Language.UA -> "🕹️ Пісочниця: поле авто-очищується"
                                        Language.KK -> "🕹️ Сынақ: алаң өздігінен тазарады"
                                        Language.DE -> "🕹️ Sandbox: Spielfeld leert sich auto."
                                        Language.ZH -> "🕹️ 实战沙盒：网格填满自动重置"
                                        else -> "🕹️ Sandbox: board auto-clears"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(
                                    onClick = {
                                        viewModel.clearRelaxBoard()
                                        viewModel.triggerAudioFeedback("click")
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Очистить"
                                            Language.UA -> "Очистити"
                                            Language.KK -> "Тазарту"
                                            Language.DE -> "Leeren"
                                            Language.ZH -> "清屏"
                                            else -> "Clear"
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        // Sandbox Game Board View
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxHeight(0.55f)
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

                        // Sandbox Controls Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = controlBottomPadding.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GameControlsSection(
                                viewModel = viewModel,
                                gameState = gameState,
                                controlStyle = controlStyle,
                                leftHandedControls = leftHandedControls,
                                controlVerticalPosition = controlVerticalPosition,
                                controlButtonScale = controlButtonScale,
                                controlButtonStyle = controlButtonStyle,
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
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(20.dp)) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google),
        contentDescription = "Google Logo",
        modifier = modifier
    )
}

@Composable
fun PlayerAvatarView(
    playerName: String,
    avatarEmoji: String = "",
    avatarBgColorHex: String = "",
    avatarFrame: String = "standard",
    customBitmap: androidx.compose.ui.graphics.ImageBitmap? = null,
    avatarBase64: String? = null,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    themeColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    showOnlineDot: Boolean = false,
    isOnline: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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

    val parsedBgColor = remember(avatarBgColorHex, playerName) {
        parsePlayerColor(avatarBgColorHex, playerName, themeColor)
    }

    val hasAnimatedFrame = avatarFrame != "standard" && avatarFrame != "none" && avatarFrame.isNotBlank()

    val frameBrush = remember(parsedBgColor, secondaryColor) {
        val isMetallic = (parsedBgColor.red < 0.22f && parsedBgColor.green < 0.22f && parsedBgColor.blue < 0.22f) ||
                (parsedBgColor.red > 0.80f && parsedBgColor.green > 0.80f && parsedBgColor.blue > 0.80f)

        if (isMetallic) {
            androidx.compose.ui.graphics.Brush.sweepGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFA0A5B5),
                    Color(0xFFE8EDF8),
                    Color(0xFF656A7A),
                    Color(0xFFFFFFFF)
                )
            )
        } else {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(
                android.graphics.Color.argb(255, (parsedBgColor.red * 255).toInt(), (parsedBgColor.green * 255).toInt(), (parsedBgColor.blue * 255).toInt()),
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

            androidx.compose.ui.graphics.Brush.sweepGradient(listOf(c1, c2, c3, c4, c5))
        }
    }

    val resolvedBitmap: androidx.compose.ui.graphics.ImageBitmap? = remember(customBitmap, avatarBase64, playerName) {
        if (customBitmap != null) {
            customBitmap
        } else if (!avatarBase64.isNullOrBlank()) {
            try {
                val clean = avatarBase64.substringAfter("base64,")
                val bytes = android.util.Base64.decode(clean, android.util.Base64.NO_WRAP)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else if (playerName.isNotBlank()) {
            try {
                val file = java.io.File(context.filesDir, "custom_avatar_${playerName}.jpg")
                if (file.exists() && file.length() > 0) {
                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
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
            if (resolvedBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = resolvedBitmap,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else if (avatarEmoji.isNotBlank()) {
                Text(
                    text = avatarEmoji,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.45f).sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                val initial = if (playerName.isNotBlank()) playerName.take(1).uppercase() else "?"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.45f).sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    ),
                    color = Color.White,
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

private data class ProfileStatItem(
    val label: String,
    val value: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileDialog(
    profile: PublicUserProfile,
    currentLang: Language,
    themeColor: Color,
    isFriend: Boolean = false,
    isSelf: Boolean = false,
    onDismiss: () -> Unit,
    onAddFriend: () -> Unit,
    onInviteToDuel: () -> Unit
) {
    var friendAddedState by remember { mutableStateOf(false) }

    // Decode player's custom background image if present
    val bgBitmap = remember(profile.customBackgroundBase64) {
        if (profile.customBackgroundBase64.isNotBlank()) {
            try {
                val bytes = android.util.Base64.decode(profile.customBackgroundBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val playerAccentColor = parseHexColor(profile.avatarBgColor, themeColor)

    val tierColor = when (profile.onlineTier.ifBlank { "BRONZE" }.uppercase()) {
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

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.statusBarsPadding(),
                        title = {
                            val profileTitle = when (currentLang) {
                                Language.RU -> "ПРОФИЛЬ"
                                Language.UA -> "ПРОФІЛЬ"
                                Language.KK -> "ПРОФИЛЬ"
                                Language.DE -> "PROFIL"
                                Language.ZH -> "个人资料"
                                else -> "PROFILE"
                            }
                            Text(
                                text = profileTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        },
                        actions = {
                            // Online / Offline Status Badge
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (profile.isOnline) Color(0xFF00E676).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (profile.isOnline) Color(0xFF00E676).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.35f)),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (profile.isOnline) Color(0xFF00E676) else Color.Gray, CircleShape)
                                    )
                                    Text(
                                        text = if (profile.isOnline) {
                                            when (currentLang) {
                                                Language.RU -> "В СЕТИ"
                                                Language.UA -> "В МЕРЕЖІ"
                                                Language.KK -> "ЖЕЛІДЕ"
                                                Language.DE -> "ONLINE"
                                                Language.ZH -> "在线"
                                                else -> "ONLINE"
                                            }
                                        } else {
                                            when (currentLang) {
                                                Language.RU -> "ОФЛАЙН"
                                                Language.UA -> "ОФЛАЙН"
                                                Language.KK -> "ОФЛАЙН"
                                                Language.DE -> "OFFLINE"
                                                Language.ZH -> "离线"
                                                else -> "OFFLINE"
                                            }
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                        color = if (profile.isOnline) Color(0xFF00E676) else Color.LightGray
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    val configuration = LocalConfiguration.current
                    val isCompactHeight = configuration.screenHeightDp < 680
                    val isNarrowWidth = configuration.screenWidthDp < 360

                    val btnHeight = if (isCompactHeight) 40.dp else 46.dp
                    val btnCorner = if (isCompactHeight) 12.dp else 16.dp
                    val btnPaddingV = if (isCompactHeight) 6.dp else 10.dp
                    val btnPaddingH = if (isNarrowWidth) 10.dp else 16.dp
                    val fontSize = if (isNarrowWidth || isCompactHeight) 11.sp else 12.sp
                    val iconSize = if (isCompactHeight) 16.dp else 18.dp

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        shadowElevation = 8.dp
                    ) {
                        val requestSentText = when (currentLang) {
                            Language.RU -> "Запрос отправлен"
                            Language.UA -> "Запит надіслано"
                            Language.KK -> "Сұраныс жіберілді"
                            Language.DE -> "Anfrage gesendet"
                            Language.ZH -> "已发送申请"
                            else -> "Request Sent"
                        }
                        val addFriendText = when (currentLang) {
                            Language.RU -> "В друзья"
                            Language.UA -> "У друзі"
                            Language.KK -> "Дос қосу"
                            Language.DE -> "Freund hinzufügen"
                            Language.ZH -> "加为好友"
                            else -> "Add Friend"
                        }
                        val alreadyFriendsText = when (currentLang) {
                            Language.RU -> "В друзьях"
                            Language.UA -> "У друзях"
                            Language.KK -> "Достарда"
                            Language.DE -> "Befreundet"
                            Language.ZH -> "已是好友"
                            else -> "Friends"
                        }
                        val duelBtnText = when (currentLang) {
                            Language.RU -> "Вызвать на дуэль"
                            Language.UA -> "Викликати на дуель"
                            Language.KK -> "Дуэльге шақыру"
                            Language.DE -> "1v1 Duell"
                            Language.ZH -> "发起对战"
                            else -> "1v1 Duel"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = btnPaddingH, vertical = btnPaddingV)
                        ) {
                            if (isSelf) {
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(btnHeight),
                                    shape = RoundedCornerShape(btnCorner),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeColor,
                                        contentColor = if (themeColor.red * 0.299f + themeColor.green * 0.587f + themeColor.blue * 0.114f > 0.6f) Color(0xFF111111) else Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    val selfProfileText = when (currentLang) {
                                        Language.RU -> "Ваш профиль (Закрыть)"
                                        Language.UA -> "Ваш профіль (Закрити)"
                                        Language.KK -> "Сіздің профиліңіз (Жабу)"
                                        Language.DE -> "Dein Profil (Schließen)"
                                        Language.ZH -> "你的资料 (关闭)"
                                        else -> "Your Profile (Close)"
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(iconSize)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selfProfileText,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = fontSize
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isActionDisabled = isFriend || friendAddedState
                                    val friendText = when {
                                        isFriend -> alreadyFriendsText
                                        friendAddedState -> requestSentText
                                        else -> addFriendText
                                    }
                                    val friendIcon = if (isActionDisabled) Icons.Default.Check else Icons.Default.PersonAdd

                                    Button(
                                        onClick = {
                                            friendAddedState = true
                                            onAddFriend()
                                        },
                                        enabled = !isActionDisabled,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(btnHeight),
                                        shape = RoundedCornerShape(btnCorner),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColor,
                                            contentColor = if (themeColor.red * 0.299f + themeColor.green * 0.587f + themeColor.blue * 0.114f > 0.6f) Color(0xFF111111) else Color.White,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = friendIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(iconSize)
                                            )
                                            Text(
                                                text = friendText,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = fontSize
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = onInviteToDuel,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(btnHeight),
                                        shape = RoundedCornerShape(btnCorner),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.35f)),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFFF5722).copy(alpha = 0.18f),
                                            contentColor = Color(0xFFFF7043)
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                modifier = Modifier.size(iconSize)
                                            )
                                            Text(
                                                text = duelBtnText,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = fontSize
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
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HERO CARD (Matches Player Profile background styling)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (bgBitmap != null) {
                                Image(
                                    bitmap = bgBitmap,
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    alpha = 0.55f
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.40f),
                                                    Color.Black.copy(alpha = 0.75f)
                                                )
                                            )
                                        )
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PlayerAvatarView(
                                    playerName = profile.username,
                                    avatarEmoji = profile.avatarEmoji,
                                    avatarBgColorHex = profile.avatarBgColor,
                                    avatarFrame = profile.avatarFrame,
                                    avatarBase64 = profile.avatarBase64,
                                    size = 84.dp,
                                    themeColor = playerAccentColor,
                                    showOnlineDot = true,
                                    isOnline = profile.isOnline
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Player Nickname & Tag
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (profile.customTag.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = playerAccentColor.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, playerAccentColor.copy(alpha = 0.5f)),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text(
                                                text = "[${profile.customTag}]",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                color = playerAccentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = playerAccentColor)
                                    Text(
                                        text = profile.username.ifBlank { "Player" },
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
                                }

                                // Online Tier & Rating Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tierColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = tierColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${Translations.getLocalizedRank(profile.onlineTier.ifBlank { "BRONZE" }, currentLang).uppercase()} • ${profile.rating} ELO",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = tierColor
                                        )
                                    }
                                }

                                if (profile.title.isNotBlank() && profile.title != "none") {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = Translations.getLocalizedTitle(profile.title, currentLang).uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // COMPREHENSIVE STATS GRID (2x3) with Vector Icons (No Text Emojis)
                    val statsData = listOf(
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Рекорд"
                                Language.UA -> "Рекорд"
                                Language.KK -> "Рекорд"
                                Language.DE -> "Rekord"
                                Language.ZH -> "最高分"
                                else -> "High Score"
                            },
                            String.format(Locale.getDefault(), "%,d", profile.highScore),
                            playerAccentColor,
                            Icons.Default.EmojiEvents
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Линии"
                                Language.UA -> "Лінії"
                                Language.KK -> "Сызықтар"
                                Language.DE -> "Linien"
                                Language.ZH -> "消除行数"
                                else -> "Lines"
                            },
                            String.format(Locale.getDefault(), "%,d", profile.clearedLines),
                            Color(0xFF00E5FF),
                            Icons.Default.FlashOn
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Матчи"
                                Language.UA -> "Матчі"
                                Language.KK -> "Матчтар"
                                Language.DE -> "Spiele"
                                Language.ZH -> "对战场次"
                                else -> "Matches"
                            },
                            "${profile.gamesPlayed}",
                            Color(0xFFB388FF),
                            Icons.Default.SportsEsports
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Тетрисы"
                                Language.UA -> "Тетріси"
                                Language.KK -> "Тетристер"
                                Language.DE -> "Tetrise"
                                Language.ZH -> "四行全消"
                                else -> "Tetrises"
                            },
                            "${profile.tetrisesCount}",
                            Color(0xFFFF4081),
                            Icons.Default.CheckCircle
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Уровень"
                                Language.UA -> "Рівень"
                                Language.KK -> "Деңгей"
                                Language.DE -> "Level"
                                Language.ZH -> "等级"
                                else -> "Level"
                            },
                            "LVL ${profile.userLevel}",
                            Color(0xFFFFD700),
                            Icons.Default.Star
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Баланс"
                                Language.UA -> "Баланс"
                                Language.KK -> "Баланс"
                                Language.DE -> "Münzen"
                                Language.ZH -> "金币"
                                else -> "Coins"
                            },
                            String.format(Locale.getDefault(), "%,d", profile.credits),
                            Color(0xFFFFD700),
                            Icons.Default.MonetizationOn
                        )
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statsData.chunked(2).forEach { rowPair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for ((label, value, color, icon) in rowPair) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f),
                                        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                color = color.copy(alpha = 0.15f)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = color,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = label.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = value,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 15.sp),
                                                    color = color
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ACHIEVEMENTS SUMMARY CARD (Only Achievements displayed)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ДОСТИЖЕНИЯ"
                                        Language.UA -> "ДОСЯГНЕННЯ"
                                        Language.KK -> "ЖЕТІСТІКТЕР"
                                        Language.DE -> "ERFOLGE"
                                        Language.ZH -> "成就荣誉"
                                        else -> "ACHIEVEMENTS"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Разблокировано: ${profile.unlockedAchievements.size}"
                                        Language.UA -> "Розблоковано: ${profile.unlockedAchievements.size}"
                                        Language.KK -> "Ашылғандар: ${profile.unlockedAchievements.size}"
                                        Language.DE -> "Freigeschaltet: ${profile.unlockedAchievements.size}"
                                        Language.ZH -> "已解锁：${profile.unlockedAchievements.size}"
                                        else -> "Unlocked: ${profile.unlockedAchievements.size}"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
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
                val friendsDialogTitle = when (currentLang) {
                    Language.RU -> "ДРУЗЬЯ И СОЮЗНИКИ"
                    Language.UA -> "ДРУЗІ ТА СОЮЗНИКИ"
                    Language.KK -> "ДОСТАР ЖӘНЕ ОДАҚТАСТАР"
                    Language.DE -> "FREUNDE & VERBÜNDETE"
                    Language.ZH -> "好友与盟友"
                    else -> "FRIENDS & ALLIES"
                }
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
                        text = friendsDialogTitle,
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
                val tabFriendsLabel = when (currentLang) {
                    Language.RU -> "Друзья"
                    Language.UA -> "Друзі"
                    Language.KK -> "Достар"
                    Language.DE -> "Freunde"
                    Language.ZH -> "好友"
                    else -> "Friends"
                }
                val tabRequestsLabel = when (currentLang) {
                    Language.RU -> "Запросы"
                    Language.UA -> "Запити"
                    Language.KK -> "Сұраныстар"
                    Language.DE -> "Anfragen"
                    Language.ZH -> "好友申请"
                    else -> "Requests"
                }
                val tabSearchLabel = when (currentLang) {
                    Language.RU -> "Поиск"
                    Language.UA -> "Пошук"
                    Language.KK -> "Іздеу"
                    Language.DE -> "Suche"
                    Language.ZH -> "搜索"
                    else -> "Search"
                }
                val tabs = listOf(
                    Triple(0, "$tabFriendsLabel (${friendsList.size})", Icons.Default.People),
                    Triple(1, "$tabRequestsLabel (${friendRequests.size})", Icons.Default.Mail),
                    Triple(2, tabSearchLabel, Icons.Default.Search)
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
                                val emptyFriendsText = when (currentLang) {
                                    Language.RU -> "У вас пока нет добавленных друзей"
                                    Language.UA -> "У вас ще немає доданих друзів"
                                    Language.KK -> "Сізде әзірше достар жоқ"
                                    Language.DE -> "Noch keine Freunde hinzugefügt"
                                    Language.ZH -> "暂无好友"
                                    else -> "No friends added yet"
                                }
                                val findPlayersBtnText = when (currentLang) {
                                    Language.RU -> "Найти игроков"
                                    Language.UA -> "Знайти гравців"
                                    Language.KK -> "Ойыншыларды табу"
                                    Language.DE -> "Spieler suchen"
                                    Language.ZH -> "寻找玩家"
                                    else -> "Find Players"
                                }
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
                                            text = emptyFriendsText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FilledTonalButton(
                                            onClick = { activeTab = 2 },
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(findPlayersBtnText)
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
                                                        avatarBase64 = friend.avatarBase64,
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
                                                        val tierWord = when (currentLang) {
                                                            Language.RU -> "Лига"
                                                            Language.UA -> "Ліга"
                                                            Language.KK -> "Лига"
                                                            Language.DE -> "Liga"
                                                            Language.ZH -> "段位"
                                                            else -> "Tier"
                                                        }
                                                        Text(
                                                            text = "🪙 ${String.format(Locale.getDefault(), "%,d", friend.credits)} • $tierWord: ${Translations.getLocalizedRank(friend.onlineTier, currentLang)}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                val currentRoom by viewModel.lobbyManager.currentRoom.collectAsStateWithLifecycle()
                                                val customAvatarEmoji by viewModel.customAvatarEmoji.collectAsStateWithLifecycle()
                                                val customAvatarBgColor by viewModel.customAvatarBgColor.collectAsStateWithLifecycle()
                                                val equippedAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
                                                val localPlayerName by viewModel.playerName.collectAsStateWithLifecycle()
                                                val localTier by viewModel.onlineTier.collectAsStateWithLifecycle()

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (friend.isOnline && currentRoom != null && currentRoom!!.status == "waiting") {
                                                        val inviteBtnText = when (currentLang) {
                                                            Language.RU -> "Позвать"
                                                            Language.UA -> "Запросити"
                                                            Language.KK -> "Шақыру"
                                                            Language.DE -> "Einladen"
                                                            Language.ZH -> "邀请"
                                                            else -> "Invite"
                                                        }
                                                        val inviteSentToastText = when (currentLang) {
                                                            Language.RU -> "Приглашение отправлено!"
                                                            Language.UA -> "Запрошення надіслано!"
                                                            Language.KK -> "Шақыру жіберілді!"
                                                            Language.DE -> "Einladung gesendet!"
                                                            Language.ZH -> "邀请已发送！"
                                                            else -> "Invite sent!"
                                                        }
                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.triggerAudioFeedback("click")
                                                                viewModel.lobbyManager.sendRoomInvite(
                                                                    targetUid = friend.uid,
                                                                    roomId = currentRoom!!.roomId,
                                                                    roomName = currentRoom!!.name,
                                                                    hostName = localPlayerName,
                                                                    avatarEmoji = customAvatarEmoji,
                                                                    avatarBgColor = customAvatarBgColor,
                                                                    avatarFrame = equippedAvatarFrame,
                                                                    hostTier = localTier
                                                                )
                                                                actionToast = inviteSentToastText
                                                            },
                                                            shape = RoundedCornerShape(12.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(inviteBtnText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    } else {
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
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.removeFriend(friend) { ok ->
                                                                if (ok) {
                                                                    actionToast = when (currentLang) {
                                                                        Language.RU -> "Друг удален"
                                                                        Language.UA -> "Друга видалено"
                                                                        Language.KK -> "Дос өшірілді"
                                                                        Language.DE -> "Freund entfernt"
                                                                        Language.ZH -> "已删除好友"
                                                                        else -> "Friend removed"
                                                                    }
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

                        // Tab 1: Friend Requests
                        1 -> {
                            if (friendRequests.isEmpty()) {
                                val emptyReqText = when (currentLang) {
                                    Language.RU -> "Входящих запросов в друзья нет"
                                    Language.UA -> "Вхідних запитів у друзі немає"
                                    Language.KK -> "Кіріс дос сұраныстары жоқ"
                                    Language.DE -> "Keine eingehenden Freundschaftsanfragen"
                                    Language.ZH -> "暂无好友申请"
                                    else -> "No incoming friend requests"
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emptyReqText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                val wantsToBeFriendsText = when (currentLang) {
                                    Language.RU -> "Хочет добавить вас в друзья"
                                    Language.UA -> "Хоче додати вас у друзі"
                                    Language.KK -> "Сізді дос ретінде қосқысы келеді"
                                    Language.DE -> "Möchte dein Freund sein"
                                    Language.ZH -> "申请添加你为好友"
                                    else -> "Wants to be friends"
                                }
                                val reqAcceptedText = when (currentLang) {
                                    Language.RU -> "Запрос принят!"
                                    Language.UA -> "Запит прийнято!"
                                    Language.KK -> "Сұраныс қабылданды!"
                                    Language.DE -> "Anfrage angenommen!"
                                    Language.ZH -> "已同意申请！"
                                    else -> "Request accepted!"
                                }
                                val reqDeclinedText = when (currentLang) {
                                    Language.RU -> "Запрос отклонен"
                                    Language.UA -> "Запит відхилено"
                                    Language.KK -> "Сұраныс қабылданбады"
                                    Language.DE -> "Anfrage abgelehnt"
                                    Language.ZH -> "已拒绝申请"
                                    else -> "Request declined"
                                }
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
                                                            text = wantsToBeFriendsText,
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
                                                                    actionToast = reqAcceptedText
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
                                                                    actionToast = reqDeclinedText
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
                            val searchPlaceholder = when (currentLang) {
                                Language.RU -> "Введите никнейм игрока..."
                                Language.UA -> "Введіть нікнейм гравця..."
                                Language.KK -> "Ойыншының никнеймін енгізіңіз..."
                                Language.DE -> "Spielernamen eingeben..."
                                Language.ZH -> "输入玩家昵称搜索..."
                                else -> "Search player by nickname..."
                            }
                            val noPlayersFoundText = when (currentLang) {
                                Language.RU -> "Игроки не найдены"
                                Language.UA -> "Гравців не знайдено"
                                Language.KK -> "Ойыншылар табылмады"
                                Language.DE -> "Keine Spieler gefunden"
                                Language.ZH -> "未找到玩家"
                                else -> "No players found"
                            }
                            val addBtnText = when (currentLang) {
                                Language.RU -> "Добавить"
                                Language.UA -> "Додати"
                                Language.KK -> "Қосу"
                                Language.DE -> "Hinzufügen"
                                Language.ZH -> "添加"
                                else -> "Add"
                            }
                            val tierLabel = when (currentLang) {
                                Language.RU -> "Лига"
                                Language.UA -> "Ліга"
                                Language.KK -> "Лига"
                                Language.DE -> "Liga"
                                Language.ZH -> "段位"
                                else -> "Tier"
                            }
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
                                        Text(searchPlaceholder)
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
                                            text = noPlayersFoundText,
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
                                                                text = "$tierLabel: ${Translations.getLocalizedRank(user.onlineTier, currentLang)}",
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
                                                        Text(addBtnText, fontSize = 12.sp)
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