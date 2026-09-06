package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.eos.EosChatMessage
import com.example.eos.EosManager
import com.example.eos.EosRoom
import com.example.game.GameMode
import kotlinx.coroutines.launch

/**
 * Full-featured Epic Online Services (EOS) Lobby Screen.
 * Complete clone of the server multiplayer menu powered by the real EOS engine.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EosLobbyScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToGame: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val activeThemeKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val localName by viewModel.playerName.collectAsStateWithLifecycle()
    val localTier by viewModel.onlineTier.collectAsStateWithLifecycle()
    val localCredits by viewModel.credits.collectAsStateWithLifecycle()

    val currentRoom by EosManager.currentRoom.collectAsStateWithLifecycle()
    val localPuid by EosManager.localPuid.collectAsStateWithLifecycle()
    val isSdkReady by EosManager.isSdkReady.collectAsStateWithLifecycle()
    val isLoggedIn by EosManager.isLoggedIn.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    val themeColor = remember(activeThemeKey) {
        when (activeThemeKey) {
            "black" -> Color(0xFFE2E2E6)
            "indigo" -> Color(0xFFD0BCFF)
            "neon" -> Color(0xFF00FFCC)
            "emerald" -> Color(0xFF10B981)
            "amber" -> Color(0xFFF59E0B)
            "rose" -> Color(0xFFF43F5E)
            "sky" -> Color(0xFF0EA5E9)
            "orange" -> Color(0xFFFF5722)
            "toxic_green" -> Color(0xFF39FF14)
            "cyber_pink" -> Color(0xFFFF007F)
            else -> Color(0xFF6366F1)
        }
    }

    // Sync player name & tier with EosManager
    LaunchedEffect(localName, localTier) {
        EosManager.updatePlayerProfile(localName, localTier)
    }

    // Listen for game start signal
    LaunchedEffect(Unit) {
        EosManager.gameStartSignal.collect {
            viewModel.startGame(GameMode.CLASSIC)
            onNavigateToGame()
        }
    }

    // Back handler
    BackHandler {
        if (currentRoom != null) {
            EosManager.leaveRoom()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = themeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (currentRoom != null) currentRoom!!.name else "EOS MULTIPLAYER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (currentRoom != null) {
                                EosManager.leaveRoom()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Balance Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.openRewardedAdDialog()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🪙",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$localCredits",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Free Coins",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(13.dp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (currentRoom != null) {
                // ── INSIDE ROOM VIEW ──
                InsideEosRoomView(
                    room = currentRoom!!,
                    localPuid = localPuid ?: "",
                    themeColor = themeColor,
                    currentLang = currentLang,
                    viewModel = viewModel,
                    onStartGame = {
                        EosManager.startMatch()
                        viewModel.startGame(GameMode.CLASSIC)
                        onNavigateToGame()
                    },
                    onLeave = { EosManager.leaveRoom() }
                )
            } else {
                // ── MAIN LOBBY BROWSER VIEW ──
                EosLobbyMainView(
                    themeColor = themeColor,
                    currentLang = currentLang,
                    viewModel = viewModel,
                    localCredits = localCredits,
                    isLoggedIn = isLoggedIn
                )
            }
        }
    }
}

/**
 * Main EOS Lobby Browser: Tabs for Rooms, Direct Code, Online Peers and Global Chat.
 */
@Composable
private fun EosLobbyMainView(
    themeColor: Color,
    currentLang: Language,
    viewModel: MainViewModel,
    localCredits: Int,
    isLoggedIn: Boolean
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Rooms, 1: Direct Code, 2: Peers, 3: Chat
    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val availableRooms by EosManager.availableRooms.collectAsStateWithLifecycle()
    val onlinePeers by EosManager.onlinePeers.collectAsStateWithLifecycle()
    val lobbyChat by EosManager.lobbyChat.collectAsStateWithLifecycle()

    val tabs = listOf(
        when (currentLang) {
            Language.RU -> "Комнаты"
            Language.UA -> "Кімнати"
            Language.KK -> "Бөлмелер"
            Language.DE -> "Räume"
            Language.ZH -> "房间列表"
            else -> "Rooms"
        },
        when (currentLang) {
            Language.RU -> "Прямой P2P"
            Language.UA -> "Прямий P2P"
            Language.KK -> "Тікелей P2P"
            Language.DE -> "Direkt-Code"
            Language.ZH -> "直连代码"
            else -> "Direct Code"
        },
        when (currentLang) {
            Language.RU -> "Игроки"
            Language.UA -> "Гравці"
            Language.KK -> "Ойыншылар"
            Language.DE -> "Spieler"
            Language.ZH -> "在线玩家"
            else -> "Peers"
        },
        when (currentLang) {
            Language.RU -> "Чат"
            Language.UA -> "Чат"
            Language.KK -> "Чат"
            Language.DE -> "Chat"
            Language.ZH -> "大厅聊天"
            else -> "Chat"
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── TOP ACTION ROW: CREATE ROOM & QUICK MATCH ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.triggerAudioFeedback("click")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showCreateDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
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
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Создать"
                            Language.UA -> "Створити"
                            Language.KK -> "Жасау"
                            Language.DE -> "Erstellen"
                            Language.ZH -> "创建房间"
                            else -> "Create"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    viewModel.triggerAudioFeedback("click")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // Quick Match: Create or join instant room
                    EosManager.createRoom(
                        name = "Quick Match",
                        bet = 0,
                        isPrivate = false
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = themeColor, modifier = Modifier.size(18.dp))
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Быстрый поиск"
                            Language.UA -> "Швидкий пошук"
                            Language.KK -> "Жылдам іздеу"
                            Language.DE -> "Schnellsuche"
                            Language.ZH -> "快速匹配"
                            else -> "Quick Match"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // ── TAB SELECTOR ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) themeColor else Color.Transparent)
                            .clickable {
                                selectedTab = index
                                viewModel.triggerAudioFeedback("click")
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // ── TAB CONTENT ──
        when (selectedTab) {
            0 -> {
                // ROOMS LIST TAB
                EosRoomsListTab(
                    rooms = availableRooms,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    themeColor = themeColor,
                    currentLang = currentLang,
                    viewModel = viewModel
                )
            }
            1 -> {
                // DIRECT P2P CODE TAB
                EosDirectCodeTab(
                    themeColor = themeColor,
                    currentLang = currentLang,
                    viewModel = viewModel
                )
            }
            2 -> {
                // PEERS TAB
                EosPeersTab(
                    peers = onlinePeers,
                    themeColor = themeColor,
                    currentLang = currentLang,
                    viewModel = viewModel
                )
            }
            3 -> {
                // GLOBAL EOS CHAT TAB
                EosLobbyChatTab(
                    messages = lobbyChat,
                    themeColor = themeColor,
                    currentLang = currentLang,
                    viewModel = viewModel
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateEosRoomDialog(
            themeColor = themeColor,
            currentLang = currentLang,
            localCredits = localCredits,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, bet, isPrivate ->
                showCreateDialog = false
                EosManager.createRoom(name, bet, isPrivate)
            }
        )
    }
}

/**
 * Tab 0: Rooms List with Search and Join.
 */
@Composable
private fun EosRoomsListTab(
    rooms: List<EosRoom>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    themeColor: Color,
    currentLang: Language,
    viewModel: MainViewModel
) {
    val haptic = LocalHapticFeedback.current
    val filteredRooms = remember(rooms, searchQuery) {
        if (searchQuery.isBlank()) rooms
        else rooms.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Поиск комнат по имени или ID..."
                        Language.UA -> "Пошук кімнат..."
                        Language.KK -> "Бөлмелерді іздеу..."
                        Language.DE -> "Räume suchen..."
                        Language.ZH -> "按名称或房间号搜索..."
                        else -> "Search rooms..."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = themeColor, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        if (filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Активных комнат пока нет. Создайте первую!"
                            Language.UA -> "Активних кімнат немає. Створіть першу!"
                            Language.KK -> "Әзірге бөлмелер жоқ. Біріншісін жасаңыз!"
                            Language.DE -> "Keine Räume gefunden. Erstelle einen!"
                            Language.ZH -> "暂无活跃房间，点击上方创建！"
                            else -> "No active rooms found. Create one!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRooms, key = { it.id }) { room ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = room.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (room.bet > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFFD700).copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = "🪙 ${room.bet}",
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Хост: ${room.hostName} • ID: ${room.id.takeLast(6)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    EosManager.joinRoom(room.id)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColor,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Войти"
                                        Language.UA -> "Увійти"
                                        Language.KK -> "Кіру"
                                        Language.DE -> "Beitreten"
                                        Language.ZH -> "加入"
                                        else -> "Join"
                                    },
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

/**
 * Tab 1: Direct Code connection.
 */
@Composable
private fun EosDirectCodeTab(
    themeColor: Color,
    currentLang: Language,
    viewModel: MainViewModel
) {
    var codeInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it.uppercase() },
                label = { Text("EOS Lobby ID / Room Code") },
                leadingIcon = {
                    Icon(Icons.Default.Tag, contentDescription = null, tint = themeColor)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                codeInput = clip.trim().uppercase()
                                viewModel.triggerAudioFeedback("click")
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    if (codeInput.isNotBlank()) {
                        viewModel.triggerAudioFeedback("click")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        EosManager.joinRoom(codeInput)
                    }
                },
                enabled = codeInput.length >= 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Подключиться к комнате"
                            Language.UA -> "Підключитися до кімнати"
                            Language.KK -> "Бөлмеге қосылу"
                            Language.DE -> "Verbinden"
                            Language.ZH -> "直连加入房间"
                            else -> "Connect to Room"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}

/**
 * Tab 2: Discovered Online Peers.
 */
@Composable
private fun EosPeersTab(
    peers: List<com.example.eos.EosPlayerPeer>,
    themeColor: Color,
    currentLang: Language,
    viewModel: MainViewModel
) {
    if (peers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Поиск активных EOS игроков в вашей сети..."
                        Language.UA -> "Пошук гравців..."
                        Language.KK -> "Ойыншыларды іздеу..."
                        Language.DE -> "Suche nach EOS-Spielern..."
                        Language.ZH -> "正在发现网络中的 EOS 玩家..."
                        else -> "Discovering online peers..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(peers, key = { it.puid }) { peer ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = themeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = themeColor, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = peer.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Ранг: ${peer.tier} • Ping: ${peer.pingMs}ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                EosManager.createRoom(
                                    name = "Дуэль против ${peer.name}",
                                    bet = 0
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Вызов",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Global EOS Lobby Chat.
 */
@Composable
private fun EosLobbyChatTab(
    messages: List<EosChatMessage>,
    themeColor: Color,
    currentLang: Language,
    viewModel: MainViewModel
) {
    var chatInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "В чате пока нет сообщений. Напишите первым!"
                            Language.UA -> "Повідомлень немає."
                            Language.KK -> "Хабарламалар жоқ."
                            Language.DE -> "Noch keine Nachrichten."
                            Language.ZH -> "大厅暂无发言，说点什么吧！"
                            else -> "No messages yet."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        Column {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColor
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Сообщение...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (chatInput.isNotBlank()) {
                            EosManager.sendLobbyChatMessage(chatInput.trim())
                            chatInput = ""
                        }
                    }
                )
            )

            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        EosManager.sendLobbyChatMessage(chatInput.trim())
                        chatInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(themeColor)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Inside Room View: Displays Host, Guest slots, Ready status, In-Room Chat and Start Match button.
 */
@Composable
private fun InsideEosRoomView(
    room: EosRoom,
    localPuid: String,
    themeColor: Color,
    currentLang: Language,
    viewModel: MainViewModel,
    onStartGame: () -> Unit,
    onLeave: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val roomChat by EosManager.roomChat.collectAsStateWithLifecycle()
    var chatInput by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()

    val isHost = (localPuid == room.hostPuid)
    val isGuest = (room.guestPuid != null && localPuid == room.guestPuid)
    val bothReady = (room.isHostReady && room.isGuestReady && room.guestPuid != null)

    LaunchedEffect(roomChat.size) {
        if (roomChat.isNotEmpty()) {
            chatListState.animateScrollToItem(roomChat.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── ROOM HEADER CARD ──
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "EOS LOBBY CODE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColor
                    )
                    Text(
                        text = room.id,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(room.id))
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = themeColor)
                    }

                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Присоединяйся к игре в Tetris EOS! Код: ${room.id}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share EOS Room"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = themeColor)
                    }
                }
            }
        }

        // ── PLAYERS SLOTS ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // HOST CARD
            ElevatedCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "👑", fontSize = 20.sp)
                    Text(
                        text = room.hostName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF00E676).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "READY",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // GUEST CARD
            ElevatedCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (room.guestPuid != null) {
                        Text(text = "🎮", fontSize = 20.sp)
                        Text(
                            text = room.guestName ?: "Guest",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (room.isGuestReady) Color(0xFF00E676).copy(alpha = 0.18f) else Color(0xFFFF5252).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (room.isGuestReady) "READY" else "NOT READY",
                                color = if (room.isGuestReady) Color(0xFF00E676) else Color(0xFFFF5252),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Ожидание...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── IN-ROOM CHAT ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(roomChat, key = { it.id }) { msg ->
                    Column {
                        Text(
                            text = msg.senderName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (msg.senderPuid == "SYSTEM") Color(0xFF00E676) else themeColor
                        )
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // ── CHAT INPUT ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Сообщение...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (chatInput.isNotBlank()) {
                            EosManager.sendRoomChatMessage(chatInput.trim())
                            chatInput = ""
                        }
                    }
                )
            )

            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        EosManager.sendRoomChatMessage(chatInput.trim())
                        chatInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(themeColor)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // ── ACTION BUTTONS: READY / START GAME ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isHost) {
                Button(
                    onClick = {
                        viewModel.triggerAudioFeedback("click")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        EosManager.toggleReady()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (room.isGuestReady) Color(0xFFFF5252) else Color(0xFF00E676),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (room.isGuestReady) "Не готов" else "Готов",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            } else {
                Button(
                    onClick = {
                        viewModel.triggerAudioFeedback("start")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartGame()
                    },
                    enabled = (room.guestPuid != null),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (bothReady) Color(0xFF00E676) else themeColor,
                        contentColor = if (bothReady) Color.Black else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (room.guestPuid == null) "Ожидание второго игрока..." else if (bothReady) "Начать матч!" else "Начать матч",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    viewModel.triggerAudioFeedback("click")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLeave()
                },
                modifier = Modifier
                    .weight(0.6f)
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Выйти",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Dialog to create a custom EOS room.
 */
@Composable
private fun CreateEosRoomDialog(
    themeColor: Color,
    currentLang: Language,
    localCredits: Int,
    onDismiss: () -> Unit,
    onCreate: (name: String, bet: Int, isPrivate: Boolean) -> Unit
) {
    var roomName by remember { mutableStateOf("EOS Room #${(100..999).random()}") }
    var betAmount by remember { mutableStateOf("0") }
    var isPrivate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLang) {
                    Language.RU -> "Создать EOS комнату"
                    Language.UA -> "Створити EOS кімнату"
                    Language.KK -> "EOS бөлмесін жасау"
                    Language.DE -> "EOS-Raum erstellen"
                    Language.ZH -> "创建 EOS 对战房间"
                    else -> "Create EOS Room"
                },
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Название комнаты") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = betAmount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) betAmount = it },
                    label = { Text("Ставка (монеты)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Приватная комната (по коду)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bet = betAmount.toIntOrNull() ?: 0
                    onCreate(roomName.ifBlank { "EOS Room" }, bet, isPrivate)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("Создать", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
