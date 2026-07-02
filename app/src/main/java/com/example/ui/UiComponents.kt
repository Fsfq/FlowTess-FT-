package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.MainViewModel
import com.example.R
import com.example.db.HighScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            enterTransition = { fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.92f, animationSpec = tween(180)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.92f, animationSpec = tween(180)) }
        ) {
            composable("menu") {
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
            composable("settings") {
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
            composable("cases") {
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
            composable("profile_achievements") {
                ProfileScreen(viewModel = viewModel, initialTab = 2, onBack = { navController.popBackStack() })
            }
            composable("profile") {
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

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

    var showAdminPanelDialog by remember { mutableStateOf(false) }
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
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = {
                        Text(
                            text = if (currentLang == Language.RU) "Требуется авторизация" else "Authentication Required",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = if (currentLang == Language.RU) 
                                "Для игры в сетевом режиме необходимо зарегистрироваться или войти в свой аккаунт. Хотите перейти в профиль?" 
                                else "To play online multiplayer, you need to sign in or create an account. Would you like to go to your profile?"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAuthGuardDialog = false
                                viewModel.triggerAudioFeedback("click")
                                onProfile()
                            }
                        ) {
                            Text(if (currentLang == Language.RU) "ВХОД / РЕГИСТРАЦИЯ" else "LOGIN / REGISTER")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showAuthGuardDialog = false 
                                viewModel.triggerAudioFeedback("click")
                            }
                        ) {
                            Text(if (currentLang == Language.RU) "ОТМЕНА" else "CANCEL")
                        }
                    }
                )
            }

            if (showAdminPanelDialog) {
                var editUsername by remember { mutableStateOf("") }
                var editCredits by remember { mutableStateOf("") }
                var editRank by remember { mutableStateOf("BRONZE") }
                var editBonusXp by remember { mutableStateOf("0") }
                var editHasGradient by remember { mutableStateOf(false) }
                var selectedAdminTab by remember { mutableStateOf(0) }
                val firebaseUsers by viewModel.firebaseUsers.collectAsStateWithLifecycle(initialValue = emptyList())

                LaunchedEffect(selectedAdminTab) {
                    if (selectedAdminTab == 1) {
                        viewModel.fetchFirebaseUsersForAdmin()
                    }
                }
                
                Dialog(
                    onDismissRequest = { showAdminPanelDialog = false },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        decorFitsSystemWindows = false
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            topBar = {
                                OptIn(ExperimentalMaterial3Api::class)
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = if (currentLang == Language.RU) "КОНСОЛЬ АДМИНИСТРАТОРА" else "ADMINISTRATOR CONSOLE",
                                            fontWeight = FontWeight.Black
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { showAdminPanelDialog = false }) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                                TabRow(
                                    selectedTabIndex = selectedAdminTab,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Tab(
                                        selected = selectedAdminTab == 0,
                                        onClick = { selectedAdminTab = 0 },
                                        text = { Text(if (currentLang == Language.RU) "ЛОКАЛЬНАЯ БД" else "LOCAL DB ACCOUNTS") }
                                    )
                                    Tab(
                                        selected = selectedAdminTab == 1,
                                        onClick = { selectedAdminTab = 1 },
                                        text = { Text(if (currentLang == Language.RU) "FIREBASE ОБЛАКО" else "FIREBASE CLOUD") }
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // ──────── USER ACCOUNTS LIST ────────
                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = if (selectedAdminTab == 0) {
                                                    if (currentLang == Language.RU) "СПООФИНГ / СПИСОК ЛОКАЛЬНЫХ АККАУНТОВ" else "LOCAL DATABASE USER ACCOUNTS"
                                                } else {
                                                    if (currentLang == Language.RU) "ОБЛАЧНЫЕ АККАУНТЫ FIREBASE" else "FIREBASE USERS IN CLOUD"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            
                                            if (selectedAdminTab == 0) {
                                                allAccounts.forEach { acc ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                            .clickable {
                                                                viewModel.triggerAudioFeedback("click")
                                                                viewModel.switchAccount(acc.username, acc.onlineTier, acc.credits, acc.hasGradient, acc.bonusXp)
                                                                editUsername = acc.username
                                                                editCredits = acc.credits.toString()
                                                                editRank = acc.onlineTier
                                                                editBonusXp = acc.bonusXp.toString()
                                                                editHasGradient = acc.hasGradient
                                                                showAdminPanelDialog = false
                                                            }
                                                            .padding(12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            if (acc.hasGradient) {
                                                                Text(
                                                                    text = acc.username,
                                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                                        brush = Brush.linearGradient(
                                                                            colors = listOf(Color(0xFFE94560), Color(0xFFFF0055), Color(0xFFFF7B00))
                                                                        )
                                                                    ),
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            } else {
                                                                Text(
                                                                    text = acc.username,
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Text(
                                                                text = "Rank: ${acc.onlineTier} • Credits: ${acc.credits} • Lvl: ${acc.bonusXp / 500 + 1}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            IconButton(
                                                                onClick = {
                                                                    viewModel.triggerAudioFeedback("click")
                                                                    editUsername = acc.username
                                                                    editCredits = acc.credits.toString()
                                                                    editRank = acc.onlineTier
                                                                    editBonusXp = acc.bonusXp.toString()
                                                                    editHasGradient = acc.hasGradient
                                                                },
                                                                modifier = Modifier.size(32.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Edit,
                                                                    contentDescription = "Edit",
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                            
                                                            if (acc.username != "FsFq") {
                                                                IconButton(
                                                                    onClick = { 
                                                                        viewModel.deleteAccount(acc.username)
                                                                        viewModel.triggerAudioFeedback("gameover")
                                                                    },
                                                                    modifier = Modifier
                                                                        .size(32.dp)
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                        .background(MaterialTheme.colorScheme.errorContainer)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Delete,
                                                                        contentDescription = "Delete",
                                                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (firebaseUsers.isEmpty()) {
                                                    Text(
                                                        text = if (currentLang == Language.RU) "Нет пользователей в облаке" else "No users found in cloud.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                } else {
                                                    firebaseUsers.forEach { userMap ->
                                                        val uName = (userMap["player_name"] as? String) ?: "FirebaseUser"
                                                        val uUid = (userMap["uid"] as? String) ?: ""
                                                        val uCredits = (userMap["credits"] as? Long)?.toInt() ?: 0
                                                        val uRank = (userMap["online_tier"] as? String) ?: "BRONZE"
                                                        val uBonusXp = (userMap["bonus_xp"] as? Long)?.toInt() ?: 0
                                                        val uHasGradient = (userMap["has_nickname_gradient"] as? Boolean) ?: false

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                                .padding(12.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                if (uHasGradient) {
                                                                    Text(
                                                                        text = uName,
                                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                                            brush = Brush.linearGradient(
                                                                                colors = listOf(Color(0xFFE94560), Color(0xFFFF0055), Color(0xFFFF7B00))
                                                                            )
                                                                        ),
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                } else {
                                                                    Text(
                                                                        text = uName,
                                                                        style = MaterialTheme.typography.bodyLarge,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                                Text(
                                                                    text = "Rank: $uRank • Credits: $uCredits • Lvl: ${uBonusXp / 500 + 1}\nUID: $uUid",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }

                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                IconButton(
                                                                    onClick = {
                                                                        viewModel.triggerAudioFeedback("click")
                                                                        editUsername = uName
                                                                        editCredits = uCredits.toString()
                                                                        editRank = uRank
                                                                        editBonusXp = uBonusXp.toString()
                                                                        editHasGradient = uHasGradient
                                                                    },
                                                                    modifier = Modifier.size(32.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Edit,
                                                                        contentDescription = "Edit",
                                                                        tint = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }

                                                                if (uName != "FsFq") {
                                                                    IconButton(
                                                                        onClick = {
                                                                            viewModel.adminDeleteFirebaseUserDirect(uUid)
                                                                            viewModel.triggerAudioFeedback("gameover")
                                                                        },
                                                                        modifier = Modifier
                                                                            .size(32.dp)
                                                                            .clip(RoundedCornerShape(8.dp))
                                                                            .background(MaterialTheme.colorScheme.errorContainer)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Delete,
                                                                            contentDescription = "Delete",
                                                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                                                            modifier = Modifier.size(16.dp)
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

                                    // ──────── ACCOUNT STATE EDITOR ────────
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = if (currentLang == Language.RU) "РЕДАКТОР ПРОФИЛЯ" else "ACCOUNT STATE EDITOR",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            
                                            OutlinedTextField(
                                                value = editUsername,
                                                onValueChange = { editUsername = it },
                                                label = { Text(if (currentLang == Language.RU) "Имя пользователя" else "Target Username") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = editCredits,
                                                    onValueChange = { editCredits = it },
                                                    label = { Text(if (currentLang == Language.RU) "Количество кредитов" else "Credits amount") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val btnShape = RoundedCornerShape(8.dp)
                                                    val padVals = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    FilledTonalButton(
                                                        onClick = {
                                                            val curr = editCredits.toIntOrNull() ?: 0
                                                            editCredits = (curr + 1000).coerceAtMost(99999).toString()
                                                            viewModel.triggerAudioFeedback("click")
                                                        },
                                                        shape = btnShape,
                                                        contentPadding = padVals,
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Text("+1K 🪙", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    FilledTonalButton(
                                                        onClick = {
                                                            val curr = editCredits.toIntOrNull() ?: 0
                                                            editCredits = (curr + 5000).coerceAtMost(99999).toString()
                                                            viewModel.triggerAudioFeedback("click")
                                                        },
                                                        shape = btnShape,
                                                        contentPadding = padVals,
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Text("+5K 🪙", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    FilledTonalButton(
                                                        onClick = {
                                                            editCredits = "99999"
                                                            viewModel.triggerAudioFeedback("success")
                                                        },
                                                        shape = btnShape,
                                                        contentPadding = padVals,
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Text("MAX", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                            
                                            OutlinedTextField(
                                                value = editRank,
                                                onValueChange = { editRank = it },
                                                label = { Text(if (currentLang == Language.RU) "Ранг (например: COSMIC LEGEND)" else "Rank (e.g. COSMIC LEGEND)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = editBonusXp,
                                                    onValueChange = { editBonusXp = it },
                                                    label = { Text(if (currentLang == Language.RU) "Бонус опыт (XP)" else "Bonus Experience (XP)") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val btnShape = RoundedCornerShape(8.dp)
                                                    val padVals = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    FilledTonalButton(
                                                        onClick = {
                                                            val curr = editBonusXp.toIntOrNull() ?: 0
                                                            editBonusXp = (curr + 500).coerceAtMost(50000).toString()
                                                            viewModel.triggerAudioFeedback("click")
                                                        },
                                                        shape = btnShape,
                                                        contentPadding = padVals,
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Text("+500 XP", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    FilledTonalButton(
                                                        onClick = {
                                                            val curr = editBonusXp.toIntOrNull() ?: 0
                                                            editBonusXp = (curr + 2000).coerceAtMost(50000).toString()
                                                            viewModel.triggerAudioFeedback("click")
                                                        },
                                                        shape = btnShape,
                                                        contentPadding = padVals,
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Text("+2K XP", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    FilledTonalButton(
                                                        onClick = {
                                                            editBonusXp = "50000"
                                                            viewModel.triggerAudioFeedback("success")
                                                        },
                                                        shape = btnShape,
                                                        contentPadding = padVals,
                                                        modifier = Modifier.weight(1f).height(32.dp)
                                                    ) {
                                                        Text("MAX", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (currentLang == Language.RU) "Градиент Ника (Красный)" else "Nickname Gradient (Mystic Red)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Switch(
                                                    checked = editHasGradient,
                                                    onCheckedChange = { editHasGradient = it }
                                                )
                                            }

                                            // Action Buttons Grid
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        val creditsVal = editCredits.toIntOrNull()
                                                        val xpVal = editBonusXp.toIntOrNull()
                                                        if (editUsername.isNotBlank() && creditsVal != null && xpVal != null) {
                                                            if (selectedAdminTab == 0) {
                                                                viewModel.adminUpdateAccountCredits(editUsername, creditsVal)
                                                                viewModel.adminUpdateAccountRank(editUsername, editRank)
                                                                viewModel.adminUpdateAccountGradient(editUsername, editHasGradient)
                                                                viewModel.adminUpdateAccountBonusXp(editUsername, xpVal)
                                                            } else {
                                                                val matchingUser = firebaseUsers.find { (it["player_name"] as? String) == editUsername }
                                                                matchingUser?.let {
                                                                    val uid = it["uid"] as String
                                                                    viewModel.adminUpdateFirebaseUserFull(uid, creditsVal, editRank, xpVal, editHasGradient)
                                                                }
                                                            }
                                                            viewModel.triggerAudioFeedback("success")
                                                        } else {
                                                            viewModel.triggerAudioFeedback("error")
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (currentLang == Language.RU) "ПРИМЕНИТЬ ИЗМЕНЕНИЯ" else "APPLY STATE CHANGES")
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            if (editUsername.isNotBlank()) {
                                                                if (selectedAdminTab == 0) {
                                                                    viewModel.adminUnlockAllLocal(editUsername)
                                                                } else {
                                                                    val matchingUser = firebaseUsers.find { (it["player_name"] as? String) == editUsername }
                                                                    matchingUser?.let {
                                                                        val uid = it["uid"] as String
                                                                        viewModel.adminUnlockAllFirebase(uid)
                                                                    }
                                                                }
                                                                viewModel.triggerAudioFeedback("success")
                                                            } else {
                                                                viewModel.triggerAudioFeedback("error")
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (currentLang == Language.RU) "РАЗБЛОК. ВСЁ" else "UNLOCK ALL")
                                                    }

                                                    Button(
                                                        onClick = {
                                                            if (editUsername.isNotBlank()) {
                                                                if (selectedAdminTab == 0) {
                                                                    viewModel.adminWipeStatsLocal(editUsername)
                                                                } else {
                                                                    val matchingUser = firebaseUsers.find { (it["player_name"] as? String) == editUsername }
                                                                    matchingUser?.let {
                                                                        val uid = it["uid"] as String
                                                                        viewModel.adminWipeStatsFirebase(uid)
                                                                    }
                                                                }
                                                                viewModel.triggerAudioFeedback("gameover")
                                                            } else {
                                                                viewModel.triggerAudioFeedback("error")
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (currentLang == Language.RU) "СБРОС СТАТИСТИКИ" else "WIPE STATS")
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // ──────── DANGEROUS SYSTEM ACTIONS ────────
                                    if (selectedAdminTab == 0) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = if (currentLang == Language.RU) "СИСТЕМНЫЕ ДЕЙСТВИЯ (ОПАСНО)" else "DANGEROUS SYSTEM ACTIONS",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                
                                                Button(
                                                    onClick = {
                                                        viewModel.adminUnlockAllAchievements()
                                                        viewModel.triggerAudioFeedback("success")
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (currentLang == Language.RU) "РАЗБЛОКИРОВАТЬ ДОСТИЖЕНИЯ И +5000 КРЕДИТОВ" else "UNLOCK ACHIEVEMENTS & +5000 CREDITS")
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.adminClearAllScores()
                                                        viewModel.triggerAudioFeedback("gameover")
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (currentLang == Language.RU) "СБРОСИТЬ ВСЕ РЕКОРДЫ В БД" else "RESET ALL DATABASE SCORES")
                                                }
                                                
                                                Button(
                                                    onClick = {
                                                        viewModel.adminClearAllNonAdminAccounts()
                                                        viewModel.triggerAudioFeedback("gameover")
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (currentLang == Language.RU) "УДАЛИТЬ ВСЕ АККАУНТЫ КРОМЕ АДМИНА" else "DELETE ALL ACCOUNTS EXCEPT ADMIN")
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
                            Text(
                                text = "FSFQ TETRIS",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 6.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp),
                                textAlign = TextAlign.Center
                            )

                            val onlinePlayersCount by viewModel.lobbyManager.onlinePlayersCount.collectAsStateWithLifecycle()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.Green)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${if (currentLang == Language.RU) "Онлайн" else "Online"}: $onlinePlayersCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
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
                                text = if (currentLang == Language.RU) "РЕЖИМЫ ИГРЫ" else "CHOOSE Game Mode",
                                iconType = "upgrades",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = { onModeSelection() }
                            )

                            MenuButton(
                                text = Translations.get("play", currentLang).uppercase() + " (${if (currentLang == Language.RU) "ПО УМОЛЧАНИЮ" else "CLASSIC"})",
                                iconType = "play",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                            )

                            MenuButton(
                                text = Translations.get("leaderboard", currentLang),
                                iconType = "leaderboard",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = onLeaderboard
                            )

                            MenuButton(
                                text = if (currentLang == Language.RU) "ОНЛАЙН СРАЖЕНИЕ" else "ONLINE MULTIPLAYER",
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
                        Text(
                            text = "FSFQ TETRIS",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        val onlinePlayersCount by viewModel.lobbyManager.onlinePlayersCount.collectAsStateWithLifecycle()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Green)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (currentLang == Language.RU) "Онлайн" else "Online"}: $onlinePlayersCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

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
                            text = if (currentLang == Language.RU) "РЕЖИМЫ ИГРЫ" else "CHOOSE Game Mode",
                            iconType = "upgrades",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onModeSelection() }
                        )

                        MenuButton(
                            text = Translations.get("play", currentLang).uppercase() + " (${if (currentLang == Language.RU) "ПО УМОЛЧАНИЮ" else "CLASSIC"})",
                            iconType = "play",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                        )

                        MenuButton(text = Translations.get("leaderboard", currentLang), iconType = "leaderboard", themeColor = MaterialTheme.colorScheme.primary, onClick = onLeaderboard)

                        MenuButton(
                            text = if (currentLang == Language.RU) "ОНЛАЙН СРАЖЕНИЕ" else "ONLINE MULTIPLAYER",
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

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

            // High-tech floating settings spoof trigger for overlord privileges
            if (playerName == "FsFq") {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.triggerAudioFeedback("click")
                        showAdminPanelDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (currentLang == Language.RU) "АДМИН-ПАНЕЛЬ" else "ADMIN PANEL",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                )
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
    OutlinedCard(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    MenuIconCanvas(
                        iconType = iconType,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingContent = {
                Canvas(
                    modifier = Modifier.size(16.dp)
                ) {
                    val cw = size.width
                    val ch = size.height
                    val strokePx = 1.5.dp.toPx()
                    drawLine(
                        color = themeColor.copy(alpha = 0.7f),
                        start = Offset(cw * 0.2f, ch * 0.15f),
                        end = Offset(cw * 0.8f, ch * 0.5f),
                        strokeWidth = strokePx
                    )
                    drawLine(
                        color = themeColor.copy(alpha = 0.7f),
                        start = Offset(cw * 0.8f, ch * 0.5f),
                        end = Offset(cw * 0.2f, ch * 0.85f),
                        strokeWidth = strokePx
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
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
    var selectedTab by remember { mutableStateOf(0) }
    var selectedModeCategory by remember { mutableStateOf("overall") }

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
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(top = 8.dp),
                title = {
                    Text(
                        text = Translations.get("leaderboard", currentLang).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (currentLang == Language.RU) "ЛОКАЛЬНЫЙ" else "LOCAL") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (currentLang == Language.RU) "ГЛОБАЛЬНЫЙ" else "GLOBAL ARENA") }
                )
            }

            if (selectedTab == 1) {
                val categories = remember {
                    listOf(
                        "overall" to (if (currentLang == Language.RU) "Все режимы" else "Overall"),
                        "classic" to (if (currentLang == Language.RU) "Классический" else "Classic"),
                        "extended" to (if (currentLang == Language.RU) "Расширенный" else "Extended"),
                        "fast_run" to (if (currentLang == Language.RU) "Гипер-Режим" else "Hyper Blast"),
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
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedModeCategory == cat.first
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    selectedModeCategory = cat.first
                                    viewModel.triggerAudioFeedback("click")
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.second,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (displayScores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentLang == Language.RU) "НЕТ ЗАПИСЕЙ" else "NO RECORDS YET",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    displayScores.forEachIndexed { index, score ->
                        val isTopThree = index < 3
                        val placementColor = when (index) {
                            0 -> Color(0xFFFFD700) // Gold
                            1 -> Color(0xFFC0C0C0) // Silver
                            2 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val containerColor = if (isTopThree) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                        val borderColor = if (isTopThree) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardDefaults.outlinedShape,
                            colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            ListItem(
                                headlineContent = {
                                    if (score.hasGradient) {
                                        Text(
                                            text = score.playerName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFFE94560), Color(0xFFFF0055), Color(0xFFFF7B00))
                                                )
                                            ),
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = score.playerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (score.playerName == "FsFq") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(placementColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = placementColor
                                        )
                                    }
                                },
                                trailingContent = {
                                    Text(
                                        text = "${score.score}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                supportingContent = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        if (score.customTag.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = score.customTag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                        }
                                        if (score.playerName == "FsFq") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Админ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        if (index == 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFE94560))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "задрот",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        if (score.score > 25000) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFFFB300))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "25🪙+",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    AdaptiveText(
                        text = Translations.get("settings", currentLang).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
        val themeColorVal = MaterialTheme.colorScheme.primary

        val settingsContent = @Composable { category: Int ->
            when (category) {
                    0 -> {

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                AdaptiveText(
                                    text = Translations.get("active_theme_accent", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val themes = listOf(
                                        "indigo" to Color(0xFFD0BCFF),
                                        "red" to Color(0xFFFF5555),
                                        "neon" to Color(0xFF00FFCC),
                                        "amber" to Color(0xFFF59E0B),
                                        "rose" to Color(0xFFF43F5E),
                                        "sky" to Color(0xFF0EA5E9),
                                        "cyber_pink" to Color(0xFFFF007F),
                                        "toxic_green" to Color(0xFF39FF14)
                                    )
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    themes.chunked(4).forEach { rowThemes ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            rowThemes.forEach { (name, color) ->
                                                val selected = themeColor == name
                                                val isOwned = name == "indigo" || name == "red" || name == "neon" || purchasedThemes.contains(name)
                                                Box(
                                                    modifier = Modifier.size(40.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(50))
                                                            .background(color)
                                                            .border(
                                                                width = if (selected) 3.dp else 0.dp,
                                                                color = MaterialTheme.colorScheme.onBackground,
                                                                shape = RoundedCornerShape(50)
                                                            )
                                                            .clickable {
                                                                if (isOwned) {
                                                                    viewModel.setThemeColor(name)
                                                                } else {
                                                                    viewModel.triggerAudioFeedback("error")
                                                                    android.widget.Toast.makeText(
                                                                        context,
                                                                        if (currentLang == Language.RU) "Купите эту тему в магазине!" else "Purchase this theme in the store!",
                                                                        android.widget.Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                    )
                                                    if (!isOwned) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(50))
                                                                .background(Color.Black.copy(alpha = 0.5f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Lock,
                                                                contentDescription = "Locked",
                                                                tint = Color.White.copy(alpha = 0.8f),
                                                                modifier = Modifier.size(16.dp)
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



                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = Translations.get("starting_level_selector", currentLang),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${Translations.get("level", currentLang)}: $customStartLevel",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = themeColorVal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = customStartLevel.toFloat(),
                                    onValueChange = { viewModel.setCustomStartLevel(it.toInt()) },
                                    valueRange = 1f..15f,
                                    steps = 13,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColorVal,
                                        activeTrackColor = themeColorVal
                                    )
                                )
                            }
                        }                    }

                    1 -> {
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = Translations.get("block_architecture_style", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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
                                            .clip(RoundedCornerShape(8.dp))
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
                                            .padding(vertical = 4.dp),
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



                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = Translations.get("grid_transparency", currentLang),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${(gridOpacity * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = themeColorVal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = gridOpacity,
                                    onValueChange = { viewModel.setGridOpacity(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColorVal,
                                        activeTrackColor = themeColorVal
                                    )
                                )
                            }
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = Translations.get("grid_line_density", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val densities = listOf(
                                    "classic" to (if (currentLang == Language.RU) "Стандартная сетка" else "Classic Grid Lines"),
                                    "dashed" to (if (currentLang == Language.RU) "Пунктирный визуал" else "Dashed Matrix Wireframe"),
                                    "none" to (if (currentLang == Language.RU) "Без линий (Пространство)" else "Void (No Lines)")
                                )
                                densities.forEach { (key, title) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setGridLineDensity(key) }
                                            .padding(vertical = 4.dp),
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



                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
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

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }


                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }
                    }

                    2 -> {
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = Translations.get("control_buttons_layout", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val layouts = listOf(
                                    "classic" to (if (currentLang == Language.RU) "Сетка по центру" else "Tactical Grid Centered"),
                                    "split" to (if (currentLang == Language.RU) "Разделенное по краям" else "Divided Fingertip Split"),
                                    "arcade" to (if (currentLang == Language.RU) "Аркадный стиль" else "Arcade Controller style")
                                )
                                layouts.forEach { (key, title) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setControlStyle(key) }
                                            .padding(vertical = 4.dp),
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



                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = Translations.get("control_button_style", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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
                                            .clip(RoundedCornerShape(8.dp))
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
                                            .padding(vertical = 4.dp),
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



                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        AdaptiveText(
                                            text = Translations.get("control_button_scale", currentLang),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        AdaptiveText(
                                            text = "${(controlButtonScale * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = themeColorVal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = controlButtonScale,
                                    onValueChange = { viewModel.setControlButtonScale(it) },
                                    valueRange = 0.8f..1.4f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColorVal,
                                        activeTrackColor = themeColorVal
                                    )
                                )
                            }
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        AdaptiveText(
                                            text = Translations.get("game_speed_multiplier", currentLang),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        AdaptiveText(
                                            text = "${gameSpeedMultiplier}x",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = themeColorVal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = gameSpeedMultiplier,
                                    onValueChange = { viewModel.setGameSpeedMultiplier(it) },
                                    valueRange = 0.5f..2.5f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColorVal,
                                        activeTrackColor = themeColorVal
                                    )
                                )
                            }
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        AdaptiveText(
                                            text = Translations.get("next_pieces_preview", currentLang),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        AdaptiveText(
                                            text = "$nextCount",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = themeColorVal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Slider(
                                    value = nextCount.toFloat(),
                                    onValueChange = { viewModel.setNextCount(it.toInt()) },
                                    valueRange = 1f..5f,
                                    steps = 3,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeColorVal,
                                        activeTrackColor = themeColorVal
                                    )
                                )
                            }
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }
                    }

                    3 -> {
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }

                        if (soundEnabled) {
                            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
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

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }

                        if (lobbyMusicEnabled) {
                            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
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

                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
                                }
                            )
                        }

                    }

                    4 -> {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
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
                                }
                            )
                        }



                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (currentLang == Language.RU) "Вертикальная позиция управления" else "Tactical Controller Vertical position",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val positions = listOf(
                                    "bottom" to (if (currentLang == Language.RU) "Нижнее положение" else "Lower Edge bottom position"),
                                    "middle" to (if (currentLang == Language.RU) "Центральное положение" else "Comfort Middle height position"),
                                    "top" to (if (currentLang == Language.RU) "Верхнее положение" else "Elevated Top reach position")
                                )
                                positions.forEach { (key, title) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setControlVerticalPosition(key) }
                                            .padding(vertical = 4.dp),
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
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                AdaptiveText(
                                    text = Translations.get("change_language", currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Language.values().forEach { lang ->
                                        val selected = currentLang == lang
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (selected) themeColorVal 
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .clickable { viewModel.setLanguage(lang) }
                                        ) {
                                            AdaptiveText(
                                                text = lang.code,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Auto-Save Highscore
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = Translations.get("auto_save_highscore", currentLang),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingContent = {
                                     Text(
                                         text = if (currentLang == Language.RU) "Автосохранение результатов в базу данных" else "Auto-save results to database"
                                     )
                                },
                                trailingContent = {
                                     Switch(
                                         checked = autoSaveHighscore,
                                         onCheckedChange = { viewModel.setAutoSaveHighscore(it) }
                                     )
                                }
                            )
                        }

                        // Single Reset Settings Card
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "СБРОСИТЬ ВСЕ НАСТРОЙКИ" else "RESET ALL SETTINGS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "Восстановит все параметры приложения по умолчанию" else "Restores all game configurations to factory defaults",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("success")
                                        viewModel.resetSettingsToDefault()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
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

// Visual mini board grid helper
@Composable
fun MiniBoard(
    grid: List<IntArray>,
    title: String,
    score: Int,
    combo: Int,
    onColumnClick: ((Int) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            // 10x12 matrix Representation
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(2.dp)
            ) {
                grid.forEach { row ->
                    Row {
                        row.forEachIndexed { cellIndex, cell ->
                            val color = when (cell) {
                                1 -> Color(0xFFFF2A6D) // Neon Pink (Tetris blocks)
                                2 -> Color(0xFF05D9E8) // Neon Cyan
                                3 -> Color(0xFFFF007F) // Deep Pink
                                else -> Color(0xFF1E1E24) // Empty Block Matrix Cell
                            }
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .padding(0.7.dp)
                                    .background(color, RoundedCornerShape(1.5.dp))
                                    .clickable(enabled = onColumnClick != null) {
                                        onColumnClick?.invoke(cellIndex)
                                    }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PTS: $score", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (combo > 0) {
                    Text("x$combo", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFCC00))
                } else {
                    Text("IDLE", fontSize = 8.sp, color = Color.Gray)
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
        val cost: Int,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val modes = listOf(
        ModeInfo(
            com.example.game.GameMode.CLASSIC, "classic",
            if (currentLang == Language.RU) "Классический" else "Classic",
            if (currentLang == Language.RU) "Оригинальный режим с нарастающей сложностью." else "The original experience with increasing speed.",
            0, Icons.Default.VideogameAsset
        ),
        ModeInfo(
            com.example.game.GameMode.TIME_ATTACK, "time_attack",
            if (currentLang == Language.RU) "Тайм-Атак" else "Time Attack",
            if (currentLang == Language.RU) "Режим с ограничением времени: старт с 60 секунд. Каждое удаление линий добавляет 10 секунд к таймеру." else "Time-limited mode: starts at 60 seconds. Each line clear adds 10 seconds to the timer.",
            0, Icons.Default.Schedule
        ),
        ModeInfo(
            com.example.game.GameMode.EXTENDED, "extended",
            if (currentLang == Language.RU) "Пентатрис (Простой)" else "Pentatris (Simple)",
            if (currentLang == Language.RU) "Игра фигурами из пяти блоков для повышенной сложности." else "Gameplay using five-block pieces for an extra challenge.",
            250, Icons.Default.Star
        ),
        ModeInfo(
            com.example.game.GameMode.FAST_RUN, "fast_run",
            if (currentLang == Language.RU) "Быстрый старт" else "Fast Start",
            if (currentLang == Language.RU) "Начало игры с 10-го уровня сложности." else "Starts the game at Level 10 difficulty.",
            400, Icons.Default.FlashOn
        ),
        ModeInfo(
            com.example.game.GameMode.ZEN_FLOW, "zen",
            if (currentLang == Language.RU) "Дзен" else "Zen Cosmic Flow",
            if (currentLang == Language.RU) "Бесконечный режим: автоматическое очищение поля при переполнении, отключено поражение." else "Endless game mode: clears the board on overflow, defeat is disabled.",
            400, Icons.Default.Spa
        ),
        ModeInfo(
            com.example.game.GameMode.REVERSE_CONTROLS, "reverse",
            if (currentLang == Language.RU) "Инверсия" else "Inverted Controls",
            if (currentLang == Language.RU) "Классический режим с инвертированным управлением." else "Classic gameplay with inverted directional controls.",
            500, Icons.Default.Visibility
        ),
        ModeInfo(
            com.example.game.GameMode.BLOCK_BLAST, "block_blast",
            "ZETA",
            if (currentLang == Language.RU) "Головоломка ZETA: свободное размещение фигур на поле." else "ZETA puzzle mode: place procedural polyominos on the board.",
            600, Icons.Default.Computer
        ),
        ModeInfo(
            com.example.game.GameMode.PULSE_EXTREME, "pulse_extreme",
            if (currentLang == Language.RU) "Импульсный Вихрь" else "Vortex Pulse",
            if (currentLang == Language.RU) "Повышенная сложность: каждые 4 установленные фигуры снизу поля добавляется случайная заполненная линия." else "Increased difficulty: a random garbage line is added at the bottom every 4 placed pieces.",
            800, Icons.Default.Bolt
        ),
        ModeInfo(
            com.example.game.GameMode.MIRROR_DIMENSION, "mirror",
            if (currentLang == Language.RU) "Зеркальный Мир" else "Mirror Dimension",
            if (currentLang == Language.RU) "Игровое поле зеркально отражается по горизонтальной оси во время игрового процесса." else "The game field is mirrored horizontally during gameplay.",
            1000, Icons.Default.SwapHoriz
        ),
        ModeInfo(
            com.example.game.GameMode.PENTARY_CHAOS, "penta",
            if (currentLang == Language.RU) "Пента-Хаос" else "Pentary Chaos",
            if (currentLang == Language.RU) "Режим повышенной сложности: все падающие фигуры состоят из пяти блоков." else "High difficulty mode: all falling pieces consist of five blocks.",
            1200, Icons.Default.AutoAwesome
        ),
        ModeInfo(
            com.example.game.GameMode.RELAX, "relax",
            if (currentLang == Language.RU) "Релакс-Песочница" else "Relax Sandbox",
            if (currentLang == Language.RU) "Настраиваемый режим с выбором блоков, бессмертием и скоростью. Не идет в рекорды." else "Customizable sandbox: select blocks, toggle immortality and speed. No high scores.",
            0, Icons.Default.Spa
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(top = 8.dp),
                title = {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "РЕЖИМЫ ИГРЫ" else "GAME MODES",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Credits",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AdaptiveText(
                            text = "$credits 🪙",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            modes.forEach { modeInfo ->
                val isPurchased = purchasedModesSet.contains(modeInfo.modeId)
                val isPlayable = isPurchased || modeInfo.cost == 0

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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mode icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isPlayable) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlayable) {
                                Icon(
                                    imageVector = modeInfo.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Text info
                        Column(modifier = Modifier.weight(1f)) {
                            AdaptiveText(
                                text = modeInfo.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlayable) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AdaptiveText(
                                text = modeInfo.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }

                        // Action area
                        if (isPlayable) {
                            FilledTonalButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    onPlayMode(modeInfo.mode)
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "ИГРАТЬ" else "PLAY",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFD700).copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    AdaptiveText(
                                        text = "${modeInfo.cost} 🪙",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFFB300)
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
