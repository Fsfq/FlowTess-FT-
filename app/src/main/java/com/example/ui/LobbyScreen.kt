package com.example.ui

import androidx.activity.compose.BackHandler

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.db.ChatMessage
import com.example.db.LobbyRoom
import com.example.db.RoomPlayer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

val NicknameGradientBrush: Brush
    @Composable get() = rememberAnimatedNicknameBrush()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToGame: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val themeColorKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val localName by viewModel.playerName.collectAsStateWithLifecycle()
    val localTier by viewModel.onlineTier.collectAsStateWithLifecycle()
    val hasNicknameGradient by viewModel.hasNicknameGradient.collectAsStateWithLifecycle()
    
    val currentRoom by viewModel.lobbyManager.currentRoom.collectAsStateWithLifecycle()
    val themeColor = MaterialTheme.colorScheme.primary

    val incomingInvite by viewModel.lobbyManager.incomingInvite.collectAsStateWithLifecycle()

    // Connect presence, room, and invite listeners on enter
    LaunchedEffect(localName, localTier, hasNicknameGradient) {
        viewModel.lobbyManager.startPresenceUpdates(localName, localTier, hasNicknameGradient)
        viewModel.lobbyManager.startRoomsSubscription()
        viewModel.lobbyManager.startLobbyChatSubscription()
        viewModel.lobbyManager.startInvitesListener()
    }

    // Auto-transition into multiplayer game when room status is "playing"
    LaunchedEffect(currentRoom?.status) {
        if (currentRoom?.status == "playing") {
            onNavigateToGame()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.lobbyManager.cleanUpAllListeners()
        }
    }

    // INCOMING ROOM INVITE DIALOG
    incomingInvite?.let { invite ->
        AlertDialog(
            onDismissRequest = { viewModel.lobbyManager.dismissInvite(invite.id) },
            icon = {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (currentLang == Language.RU) "Приглашение на дуэль!" else "Duel Invitation!",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PlayerAvatarView(
                        playerName = invite.hostName,
                        avatarEmoji = invite.hostAvatarEmoji,
                        avatarBgColorHex = invite.hostAvatarBgColor,
                        avatarFrame = invite.hostAvatarFrame,
                        size = 48.dp,
                        themeColor = themeColor
                    )
                    Text(
                        text = if (currentLang == Language.RU)
                            "${invite.hostName} приглашает вас в комнату «${invite.roomName}»"
                            else "${invite.hostName} invites you to «${invite.roomName}»",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (invite.betAmount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${if (currentLang == Language.RU) "Ставка" else "Wager"}: ${invite.betAmount} 🪙",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val roomId = invite.roomId
                        viewModel.lobbyManager.dismissInvite(invite.id)
                        viewModel.lobbyManager.joinRoom(
                            roomId = roomId,
                            passwordInput = "",
                            playerName = localName,
                            playerTier = localTier,
                            playerHasGradient = hasNicknameGradient,
                            onSuccess = { viewModel.triggerAudioFeedback("success") },
                            onFailure = { viewModel.triggerAudioFeedback("error") }
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentLang == Language.RU) "Принять бой" else "Accept Duel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.lobbyManager.dismissInvite(invite.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentLang == Language.RU) "Отклонить" else "Decline")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentRoom == null) {
            LobbyHubView(
                viewModel = viewModel,
                themeColor = themeColor,
                localName = localName,
                localTier = localTier,
                currentLang = currentLang,
                hasNicknameGradient = hasNicknameGradient,
                onBack = onBack
            )
        } else {
            LobbyRoomView(
                viewModel = viewModel,
                room = currentRoom!!,
                themeColor = themeColor,
                localName = localName,
                currentLang = currentLang,
                hasNicknameGradient = hasNicknameGradient
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyHubView(
    viewModel: MainViewModel,
    themeColor: Color,
    localName: String,
    localTier: String,
    currentLang: Language,
    hasNicknameGradient: Boolean,
    onBack: () -> Unit
) {
    val onlinePlayersCount by viewModel.lobbyManager.onlinePlayersCount.collectAsStateWithLifecycle()
    val activeRooms by viewModel.lobbyManager.activeRooms.collectAsStateWithLifecycle()
    val lobbyChatMessages by viewModel.lobbyManager.lobbyChatMessages.collectAsStateWithLifecycle()
    val customAvatarEmoji by viewModel.customAvatarEmoji.collectAsStateWithLifecycle()
    val customAvatarBgColor by viewModel.customAvatarBgColor.collectAsStateWithLifecycle()
    val equippedAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
    
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }
    var joinRoomPendingPassword by remember { mutableStateOf<LobbyRoom?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }
    var joinErrorMessage by remember { mutableStateOf<String?>(null) }
    var roomFilter by remember { mutableStateOf("ALL") } // "ALL", "OPEN", "LOCKED"

    val filteredRooms = remember(activeRooms, roomFilter) {
        when (roomFilter) {
            "OPEN" -> activeRooms.filter { it.players.size < 2 && !it.isLocked && it.status == "waiting" }
            "LOCKED" -> activeRooms.filter { it.isLocked }
            else -> activeRooms
        }
    }

    // Quick Match Action
    fun executeQuickMatch() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.triggerAudioFeedback("click")
        val candidate = activeRooms.firstOrNull { it.players.size < 2 && !it.isLocked && it.status == "waiting" }
        if (candidate != null) {
            viewModel.lobbyManager.joinRoom(
                roomId = candidate.roomId,
                passwordInput = "",
                playerName = localName,
                playerTier = localTier,
                playerHasGradient = hasNicknameGradient,
                avatarEmoji = customAvatarEmoji,
                avatarBgColor = customAvatarBgColor,
                avatarFrame = equippedAvatarFrame,
                onSuccess = {
                    viewModel.triggerAudioFeedback("success")
                },
                onFailure = {
                    viewModel.triggerAudioFeedback("error")
                }
            )
        } else {
            // Auto create an open battle room
            val defaultName = if (currentLang == Language.RU) "Комната $localName" else "$localName's Room"
            viewModel.lobbyManager.createRoom(
                name = defaultName,
                passwordInput = "",
                hostName = localName,
                hostTier = localTier,
                hostHasGradient = hasNicknameGradient,
                avatarEmoji = customAvatarEmoji,
                avatarBgColor = customAvatarBgColor,
                avatarFrame = equippedAvatarFrame,
                onSuccess = {
                    viewModel.triggerAudioFeedback("success")
                }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (currentLang == Language.RU) "МУЛЬТИПЛЕЕР" else "MULTIPLAYER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Text(
                                text = "${if (currentLang == Language.RU) "Онлайн" else "Online"}: $onlinePlayersCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showJoinCodeDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = "Join by Code",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCreateRoomDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Create Room",
                            tint = themeColor
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
                .imePadding()
        ) {
            // MD3 Segmented Tab Switcher with animated sliding pill
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
                    val tabWidth = maxWidth / 2
                    val indicatorOffset by animateDpAsState(
                        targetValue = tabWidth * pagerState.currentPage,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                        label = "lobbyTabIndicator"
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
                        Triple(0, if (currentLang == Language.RU) "Комнаты" else "Rooms", activeRooms.size),
                        Triple(1, if (currentLang == Language.RU) "Общий Чат" else "Global Chat", lobbyChatMessages.size)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        tabs.forEach { (index, label, count) ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        viewModel.triggerAudioFeedback("click")
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Default.Gamepad else Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (count > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                        ) {
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> {
                        // TAB 0: BATTLE ROOMS
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Quick Match Hero Card
                            item {
                                val infiniteTransition = rememberInfiniteTransition(label = "QuickMatchPulse")
                                val boltScale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(900, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "boltScale"
                                )

                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { executeQuickMatch() },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = themeColor,
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Bolt,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .graphicsLayer {
                                                                scaleX = boltScale
                                                                scaleY = boltScale
                                                            }
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = if (currentLang == Language.RU) "БЫСТРЫЙ БОЙ 1V1" else "QUICK MATCH 1V1",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Text(
                                                    text = if (currentLang == Language.RU) "Мгновенный поиск или создание дуэли" else "Instant matchmaking or auto-room host",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            // Filter Chips Row
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val filters = listOf(
                                        "ALL" to (if (currentLang == Language.RU) "Все комнаты" else "All Rooms"),
                                        "OPEN" to (if (currentLang == Language.RU) "Открытые" else "Open"),
                                        "LOCKED" to (if (currentLang == Language.RU) "С паролем" else "Locked")
                                    )

                                    filters.forEach { (key, label) ->
                                        val isSelected = roomFilter == key
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.triggerAudioFeedback("click")
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                roomFilter = key
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

                            // Active Rooms List
                            if (filteredRooms.isEmpty()) {
                                item {
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
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
                                                        imageVector = Icons.Default.SportsEsports,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (currentLang == Language.RU) "Нет доступных комнат" else "No Battle Rooms Found",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (currentLang == Language.RU) "Создайте новую комнату и пригласите друга по коду!" else "Create a new battle arena and invite your friend using the room code!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { showCreateRoomDialog = true },
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (currentLang == Language.RU) "Создать комнату" else "Create Room", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(filteredRooms) { room ->
                                    RoomListItemCard(
                                        room = room,
                                        currentLang = currentLang,
                                        themeColor = themeColor,
                                        onJoin = {
                                            if (room.isLocked) {
                                                joinRoomPendingPassword = room
                                            } else {
                                                viewModel.lobbyManager.joinRoom(
                                                    roomId = room.roomId,
                                                    passwordInput = "",
                                                    playerName = localName,
                                                    playerTier = localTier,
                                                    playerHasGradient = hasNicknameGradient,
                                                    avatarEmoji = customAvatarEmoji,
                                                    avatarBgColor = customAvatarBgColor,
                                                    avatarFrame = equippedAvatarFrame,
                                                    onSuccess = {
                                                        viewModel.triggerAudioFeedback("success")
                                                    },
                                                    onFailure = {
                                                        viewModel.triggerAudioFeedback("error")
                                                    }
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // TAB 1: GLOBAL LOBBY CHAT
                        val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
                        LobbyChatComponent(
                            messages = lobbyChatMessages,
                            themeColor = themeColor,
                            currentLang = currentLang,
                            onSendMessage = { text, replySender, replyText ->
                                viewModel.lobbyManager.sendLobbyChatMessage(
                                    text = text,
                                    username = localName,
                                    hasGradient = hasNicknameGradient,
                                    tier = localTier,
                                    avatarEmoji = customAvatarEmoji,
                                    avatarBgColor = customAvatarBgColor,
                                    avatarFrame = equippedAvatarFrame,
                                    replyToSender = replySender,
                                    replyToText = replyText
                                )
                            },
                            onDeleteMessage = { msgId ->
                                viewModel.lobbyManager.deleteLobbyChatMessage(msgId)
                            },
                            onToggleReaction = { msgId, emoji ->
                                viewModel.lobbyManager.toggleLobbyMessageReaction(msgId, emoji)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        )
                    }
                }
            }
        }

        // FULLSCREEN MD3 CREATE ROOM OVERLAY
        AnimatedVisibility(
            visible = showCreateRoomDialog,
            enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(250)),
            exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(200))
        ) {
            BackHandler { showCreateRoomDialog = false }
            var roomNameInput by remember { mutableStateOf(if (currentLang == Language.RU) "Комната $localName" else "$localName's Room") }
            var selectedGameMode by remember { mutableStateOf("CLASSIC") }
            var selectedGarbageIntensity by remember { mutableFloatStateOf(1.0f) }
            var selectedRoundTarget by remember { mutableIntStateOf(1) }
            var selectedWager by remember { mutableIntStateOf(0) }
            var isPasswordProtected by remember { mutableStateOf(false) }
            var roomPasswordInput by remember { mutableStateOf("") }

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
                                Text(
                                    text = if (currentLang == Language.RU) "СОЗДАНИЕ КОМНАТЫ" else "CREATE ROOM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { showCreateRoomDialog = false }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 8.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                            ) {
                                    Button(
                                        onClick = {
                                            if (roomNameInput.isNotBlank()) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.lobbyManager.createRoom(
                                                    name = roomNameInput.trim(),
                                                    passwordInput = if (isPasswordProtected) roomPasswordInput.trim() else "",
                                                    hostName = localName,
                                                    hostTier = localTier,
                                                    hostHasGradient = hasNicknameGradient,
                                                    avatarEmoji = customAvatarEmoji,
                                                    avatarBgColor = customAvatarBgColor,
                                                    avatarFrame = equippedAvatarFrame,
                                                    gameMode = selectedGameMode,
                                                    garbageIntensity = selectedGarbageIntensity,
                                                    roundTarget = selectedRoundTarget,
                                                    betAmount = selectedWager,
                                                    onSuccess = {
                                                        showCreateRoomDialog = false
                                                        viewModel.triggerAudioFeedback("success")
                                                    }
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (currentLang == Language.RU) "СОЗДАТЬ КОМНАТУ" else "CREATE ROOM",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
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
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            // 1. Room Name
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "НАЗВАНИЕ КОМНАТЫ" else "ROOM NAME",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )
                                    OutlinedTextField(
                                        value = roomNameInput,
                                        onValueChange = { roomNameInput = it },
                                        placeholder = { Text(if (currentLang == Language.RU) "Введите имя комнаты..." else "Enter arena title...") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // 2. Game Mode Selection
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "РЕЖИМ СОРЕВНОВАНИЯ" else "MATCH GAME MODE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )

                                    val modes = listOf(
                                        Triple("CLASSIC", if (currentLang == Language.RU) "Битва (с атаками)" else "Battle (Garbage Duel)", if (currentLang == Language.RU) "Дуэль на выбывание с атаками мусорными линиями" else "Knockout duel with garbage line attacks"),
                                        Triple("SCORE_RACE", if (currentLang == Language.RU) "Классика (на очки, без атак)" else "Pure Classic (Score Race)", if (currentLang == Language.RU) "Классический тетрис без атак, победа по очкам/выживанию" else "Pure classic mode with no attacks, highest score wins"),
                                        Triple("SPRINT", if (currentLang == Language.RU) "Спринт 40 линий" else "Sprint 40 Lines", if (currentLang == Language.RU) "Кто быстрее очистит 40 линий" else "First to clear 40 lines"),
                                        Triple("BLITZ", if (currentLang == Language.RU) "Блиц 2 минуты" else "Blitz 2 Minutes", if (currentLang == Language.RU) "Набор максимального счета за 120 сек" else "Highest score within 120s"),
                                        Triple("HYPER", if (currentLang == Language.RU) "Гипер-скорость" else "Hyper Rush", if (currentLang == Language.RU) "Экстремальное ускорение падения блоков" else "High gravity drop speed")
                                    )

                                    modes.forEach { (modeId, modeTitle, modeDesc) ->
                                        val isSelected = selectedGameMode == modeId
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable {
                                                    viewModel.triggerAudioFeedback("click")
                                                    selectedGameMode = modeId
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            border = BorderStroke(if (isSelected) 1.8.dp else 0.5.dp, if (isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedGameMode = modeId }
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = modeTitle,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = modeDesc,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Match Series Target & Garbage Intensity
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "ФОРМАТ СЕРИИ МАТЧА" else "MATCH SERIES FORMAT",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )

                                    val rounds = listOf(
                                        1 to (if (currentLang == Language.RU) "1 Раунд" else "Best of 1"),
                                        3 to (if (currentLang == Language.RU) "До 2 побед (BO3)" else "Best of 3"),
                                        5 to (if (currentLang == Language.RU) "До 3 побед (BO5)" else "Best of 5")
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rounds.forEach { (count, label) ->
                                            val isSelected = selectedRoundTarget == count
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.triggerAudioFeedback("click")
                                                    selectedRoundTarget = count
                                                },
                                                label = {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = themeColor,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Text(
                                        text = if (currentLang == Language.RU) "ИНТЕНСИВНОСТЬ АТАКИ МУСОРОМ" else "GARBAGE ATTACK INTENSITY",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )

                                    val intensities = listOf(
                                        0.0f to (if (currentLang == Language.RU) "0x Без атак" else "0x Off"),
                                        0.5f to (if (currentLang == Language.RU) "0.5x Слаб." else "0.5x"),
                                        1.0f to (if (currentLang == Language.RU) "1.0x Норм." else "1.0x"),
                                        1.5f to (if (currentLang == Language.RU) "1.5x Хард" else "1.5x"),
                                        2.0f to (if (currentLang == Language.RU) "2.0x Хаос" else "2.0x")
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        intensities.forEach { (intensity, label) ->
                                            val isSelected = selectedGarbageIntensity == intensity
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.triggerAudioFeedback("click")
                                                    selectedGarbageIntensity = intensity
                                                },
                                                label = {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = themeColor,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Privacy & Password Protection
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = if (currentLang == Language.RU) "ПРИВАТНАЯ КОМНАТА" else "PRIVATE ROOM",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = if (currentLang == Language.RU) "Вход только по паролю" else "Require password to join",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = isPasswordProtected,
                                            onCheckedChange = { isPasswordProtected = it }
                                        )
                                    }

                                    if (isPasswordProtected) {
                                        OutlinedTextField(
                                            value = roomPasswordInput,
                                            onValueChange = { roomPasswordInput = it },
                                            label = { Text(if (currentLang == Language.RU) "Пароль комнаты" else "Room Password") },
                                            singleLine = true,
                                            visualTransformation = PasswordVisualTransformation(),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // JOIN BY CODE DIALOG
        if (showJoinCodeDialog) {
            AlertDialog(
                onDismissRequest = { showJoinCodeDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Icon(imageVector = Icons.Default.Tag, contentDescription = null, tint = themeColor, modifier = Modifier.size(32.dp))
                },
                title = {
                    Text(
                        text = if (currentLang == Language.RU) "Вход по коду" else "Join by Room Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (currentLang == Language.RU) "Введите 6-значный номер комнаты, полученный от друга:" else "Enter the 6-digit room code shared by your friend:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { if (it.length <= 6) joinCodeInput = it },
                            label = { Text(if (currentLang == Language.RU) "Код комнаты (6 цифр)" else "Room Code (6 digits)") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val code = joinCodeInput.trim()
                            if (code.length == 6) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val targetRoom = activeRooms.find { it.roomId == code }
                                if (targetRoom != null) {
                                    showJoinCodeDialog = false
                                    if (targetRoom.isLocked) {
                                        passwordInput = ""
                                        joinErrorMessage = null
                                        joinRoomPendingPassword = targetRoom
                                    } else {
                                        viewModel.lobbyManager.joinRoom(
                                            roomId = targetRoom.roomId,
                                            passwordInput = "",
                                            playerName = localName,
                                            playerTier = localTier,
                                            playerHasGradient = hasNicknameGradient,
                                            onSuccess = { viewModel.triggerAudioFeedback("success") },
                                            onFailure = { viewModel.triggerAudioFeedback("error") }
                                        )
                                    }
                                } else {
                                    viewModel.lobbyManager.joinRoom(
                                        roomId = code,
                                        passwordInput = "",
                                        playerName = localName,
                                        playerTier = localTier,
                                        playerHasGradient = hasNicknameGradient,
                                        onSuccess = {
                                            showJoinCodeDialog = false
                                            viewModel.triggerAudioFeedback("success")
                                        },
                                        onFailure = {
                                            viewModel.triggerAudioFeedback("error")
                                        }
                                    )
                                }
                            }
                        },
                        enabled = joinCodeInput.trim().length == 6,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (currentLang == Language.RU) "Присоединиться" else "Join", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinCodeDialog = false }) {
                        Text(if (currentLang == Language.RU) "Отмена" else "Cancel")
                    }
                }
            )
        }

        // PASSWORD VERIFICATION DIALOG
        joinRoomPendingPassword?.let { room ->
            AlertDialog(
                onDismissRequest = { joinRoomPendingPassword = null },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                },
                title = {
                    Text(
                        text = if (currentLang == Language.RU) "Защищенная комната" else "Protected Room",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (currentLang == Language.RU) "Для входа в «${room.name}» введите пароль:" else "Enter password to join «${room.name}»:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(if (currentLang == Language.RU) "Пароль" else "Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (joinErrorMessage != null) {
                            Text(
                                text = joinErrorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.lobbyManager.joinRoom(
                                roomId = room.roomId,
                                passwordInput = passwordInput,
                                playerName = localName,
                                playerTier = localTier,
                                playerHasGradient = hasNicknameGradient,
                                avatarEmoji = customAvatarEmoji,
                                avatarBgColor = customAvatarBgColor,
                                avatarFrame = equippedAvatarFrame,
                                onSuccess = {
                                    joinRoomPendingPassword = null
                                    viewModel.triggerAudioFeedback("success")
                                },
                                onFailure = { err ->
                                    joinErrorMessage = if (currentLang == Language.RU) "Неверный пароль" else "Incorrect password"
                                    viewModel.triggerAudioFeedback("error")
                                }
                            )
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (currentLang == Language.RU) "Войти" else "Enter", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { joinRoomPendingPassword = null }) {
                        Text(if (currentLang == Language.RU) "Отмена" else "Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun RoomListItemCard(
    room: LobbyRoom,
    currentLang: Language,
    themeColor: Color,
    onJoin: () -> Unit
) {
    val isFull = room.players.size >= 2
    val isPlaying = room.status == "playing"
    val isOpen = !isFull && !isPlaying

    val infiniteTransition = rememberInfiniteTransition(label = "RoomCardPulse")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isOpen) Modifier.border(1.2.dp, themeColor.copy(alpha = borderGlow), RoundedCornerShape(22.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isOpen) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Host Circular Avatar
                val hostPlayer = room.players.find { it.uid == room.hostId }
                PlayerAvatarView(
                    playerName = room.hostName,
                    avatarEmoji = hostPlayer?.avatarEmoji ?: "",
                    avatarBgColorHex = hostPlayer?.avatarBgColor ?: "",
                    avatarFrame = hostPlayer?.avatarFrame ?: "standard",
                    size = 44.dp,
                    themeColor = themeColor
                )

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (room.isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Protected",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${if (currentLang == Language.RU) "Хост" else "Host"}: ${room.hostName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = room.hostTier,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (room.gameMode != "CLASSIC") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = themeColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = room.gameMode,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColor,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (room.roundTarget > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "BO${room.roundTarget}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Slots badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPlaying) MaterialTheme.colorScheme.errorContainer
                            else if (isFull) MaterialTheme.colorScheme.surfaceContainerHighest
                            else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (isPlaying) (if (currentLang == Language.RU) "В БОЮ" else "IN BATTLE")
                               else "${room.players.size}/2",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPlaying) MaterialTheme.colorScheme.onErrorContainer
                                else if (isFull) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                Button(
                    onClick = onJoin,
                    enabled = !isFull && !isPlaying,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "ВОЙТИ" else "JOIN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class ChatReactionDef(
    val id: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

val availableChatReactions = listOf(
    ChatReactionDef("fire", Icons.Default.LocalFireDepartment, Color(0xFFFF5722)),
    ChatReactionDef("thumb_up", Icons.Default.ThumbUp, Color(0xFF29B6F6)),
    ChatReactionDef("heart", Icons.Default.Favorite, Color(0xFFFF4081)),
    ChatReactionDef("star", Icons.Default.Star, Color(0xFFFFD700)),
    ChatReactionDef("bolt", Icons.Default.Bolt, Color(0xFFFFCA28)),
    ChatReactionDef("trophy", Icons.Default.EmojiEvents, Color(0xFFFFA000))
)

@Composable
fun LobbyChatComponent(
    messages: List<ChatMessage>,
    themeColor: Color,
    currentLang: Language,
    onSendMessage: (String, String, String) -> Unit, // text, replySender, replyText
    onDeleteMessage: ((String) -> Unit)? = null,
    onToggleReaction: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isRoomChat: Boolean = false,
    isRoomHost: Boolean = false
) {
    var textInput by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedMsgForMenu by remember { mutableStateOf<ChatMessage?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val localUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val haptic = LocalHapticFeedback.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val density = androidx.compose.ui.platform.LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    // Auto-scroll when new messages arrive or keyboard opens
    LaunchedEffect(messages.size, isKeyboardOpen) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Chat Messages Log
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRoomChat) {
                                if (currentLang == Language.RU) "Чат комнаты пуст. Напишите сопернику!" else "Room chat is empty. Greet your opponent!"
                            } else {
                                if (currentLang == Language.RU) "Сообщений нет. Напишите первым!" else "No messages yet. Start the conversation!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(messages, key = { it.id.ifEmpty { "${it.senderId}_${it.timestamp}" } }) { msg ->
                            val isMyMsg = msg.senderId == localUid
                            val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                            val timeStr = remember(msg.timestamp) {
                                if (msg.timestamp > 0) timeFormat.format(Date(msg.timestamp)) else ""
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(msg.id) {
                                        detectTapGestures(
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedMsgForMenu = msg
                                            }
                                        )
                                    },
                                horizontalArrangement = if (isMyMsg) Arrangement.End else Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                if (!isMyMsg) {
                                    PlayerAvatarView(
                                        playerName = msg.senderName,
                                        avatarEmoji = msg.senderAvatarEmoji,
                                        avatarBgColorHex = msg.senderAvatarBgColor,
                                        avatarFrame = msg.senderAvatarFrame,
                                        size = 34.dp,
                                        themeColor = themeColor,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = if (isMyMsg) Alignment.End else Alignment.Start,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    if (!isMyMsg) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                        ) {
                                            val senderColor = parseHexColor(msg.senderAvatarBgColor, themeColor)
                                            val senderBrush = rememberAnimatedNicknameBrush(baseColor = senderColor)
                                            Text(
                                                text = msg.senderName,
                                                style = if (msg.hasGradient) {
                                                    MaterialTheme.typography.labelSmall.copy(brush = senderBrush)
                                                } else {
                                                    MaterialTheme.typography.labelSmall.copy(color = themeColor)
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (msg.senderTier.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                                ) {
                                                    Text(
                                                        text = msg.senderTier,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMyMsg) 16.dp else 4.dp,
                                            bottomEnd = if (isMyMsg) 4.dp else 16.dp
                                        ),
                                        color = if (isMyMsg) themeColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        border = if (isMyMsg) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            // Quoted Reply preview inside bubble
                                            if (msg.replyToSender.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isMyMsg) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 6.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(3.dp)
                                                                .height(24.dp)
                                                                .background(if (isMyMsg) MaterialTheme.colorScheme.onPrimary else themeColor, RoundedCornerShape(2.dp))
                                                        )
                                                        Column {
                                                            Text(
                                                                text = msg.replyToSender,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isMyMsg) MaterialTheme.colorScheme.onPrimary else themeColor
                                                            )
                                                            Text(
                                                                text = msg.replyToText,
                                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                color = if (isMyMsg) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isMyMsg) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (timeStr.isNotEmpty()) {
                                                Text(
                                                    text = timeStr,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = if (isMyMsg) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.align(Alignment.End)
                                                )
                                            }
                                        }
                                    }

                                    // Reactions badge row with Material Icons
                                    if (msg.reactions.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            msg.reactions.forEach { (reactionId, uids) ->
                                                if (uids.isNotEmpty()) {
                                                    val iReacted = uids.contains(localUid)
                                                    val reactionDef = availableChatReactions.find { it.id == reactionId }
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = if (iReacted) themeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                        border = BorderStroke(1.dp, if (iReacted) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                                        modifier = Modifier.clickable {
                                                            onToggleReaction?.invoke(msg.id, reactionId)
                                                        }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                        ) {
                                                            if (reactionDef != null) {
                                                                Icon(
                                                                    imageVector = reactionDef.icon,
                                                                    contentDescription = null,
                                                                    tint = reactionDef.color,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            } else {
                                                                Icon(
                                                                    imageVector = Icons.Default.ThumbUp,
                                                                    contentDescription = null,
                                                                    tint = themeColor,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            }
                                                            Text(
                                                                text = uids.size.toString(),
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (iReacted) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
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

                // Reply Preview Bar
                AnimatedVisibility(
                    visible = replyingToMessage != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    replyingToMessage?.let { replyTarget ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Reply,
                                        contentDescription = "Reply",
                                        tint = themeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${if (currentLang == Language.RU) "Ответ" else "Reply to"}: ${replyTarget.senderName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColor
                                        )
                                        Text(
                                            text = replyTarget.text,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { replyingToMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Text Input Field Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = if (isRoomChat) {
                                    if (currentLang == Language.RU) "Сообщение в комнату..." else "Room message..."
                                } else {
                                    if (currentLang == Language.RU) "Сообщение в чат..." else "Chat message..."
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    IconButton(
                        onClick = {
                            val trimmed = textInput.trim()
                            if (trimmed.isNotEmpty()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSendMessage(
                                    trimmed,
                                    replyingToMessage?.senderName ?: "",
                                    replyingToMessage?.text ?: ""
                                )
                                textInput = ""
                                replyingToMessage = null
                            }
                        },
                        enabled = textInput.trim().isNotEmpty(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (textInput.trim().isNotEmpty()) themeColor
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (textInput.trim().isNotEmpty()) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Message Long-Press Actions Dialog / Modal
            selectedMsgForMenu?.let { msg ->
                val isMyMsg = msg.senderId == localUid
                val canDelete = isMyMsg || (isRoomChat && isRoomHost)

                AlertDialog(
                    onDismissRequest = { selectedMsgForMenu = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    title = {
                        Text(
                            text = if (currentLang == Language.RU) "Действия с сообщением" else "Message Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Material Reactions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                availableChatReactions.forEach { item ->
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onToggleReaction?.invoke(msg.id, item.id)
                                                selectedMsgForMenu = null
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.id,
                                                tint = item.color,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            // Reply Action
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        replyingToMessage = msg
                                        selectedMsgForMenu = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Reply, contentDescription = null, tint = themeColor)
                                    Text(
                                        text = if (currentLang == Language.RU) "Ответить" else "Reply",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Copy Action
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg.text))
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedMsgForMenu = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = themeColor)
                                    Text(
                                        text = if (currentLang == Language.RU) "Копировать текст" else "Copy text",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Delete Action
                            if (canDelete) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDeleteMessage?.invoke(msg.id)
                                            selectedMsgForMenu = null
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Text(
                                            text = if (currentLang == Language.RU) "Удалить сообщение" else "Delete message",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { selectedMsgForMenu = null }) {
                            Text(if (currentLang == Language.RU) "Закрыть" else "Close")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyRoomView(
    viewModel: MainViewModel,
    room: LobbyRoom,
    themeColor: Color,
    localName: String,
    currentLang: Language,
    hasNicknameGradient: Boolean
) {
    val roomChatMessages by viewModel.lobbyManager.roomChatMessages.collectAsStateWithLifecycle()
    val customAvatarEmoji by viewModel.customAvatarEmoji.collectAsStateWithLifecycle()
    val customAvatarBgColor by viewModel.customAvatarBgColor.collectAsStateWithLifecycle()
    val equippedAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
    val localTier = viewModel.onlineTier.collectAsStateWithLifecycle().value

    val localUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val isHost = remember(room.hostId, localUid) { room.hostId == localUid }
    val opponent = remember(room.players, localUid) { room.players.find { it.uid != localUid } }
    val localPlayer = remember(room.players, localUid) { room.players.find { it.uid == localUid } }
    
    val allReady = room.players.size == 2 && room.players.all { it.isReady }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showCopiedToast by remember { mutableStateOf(false) }

    fun copyRoomCode() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Tetris Room Code", room.roomId)
            clipboard.setPrimaryClip(clip)
            showCopiedToast = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("click")
        } catch (e: Exception) {
            // Ignore
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = room.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .clickable { copyRoomCode() }
                                .padding(top = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${if (currentLang == Language.RU) "Код" else "Code"}: ${room.roomId}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = themeColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.lobbyManager.leaveRoom()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave Room",
                            tint = MaterialTheme.colorScheme.onBackground
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
                .imePadding()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // DUEL ARENA: 2 PLAYER VERSUS CARDS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val hostPlayer = room.players.find { it.uid == room.hostId }
                val guestPlayer = room.players.find { it.uid != room.hostId }
                val isMeHost = room.hostId == localUid

                // Left: Host Card
                RoomPlayerCard(
                    playerName = hostPlayer?.name ?: room.hostName,
                    playerTier = hostPlayer?.tier ?: room.hostTier,
                    isReady = hostPlayer?.isReady ?: true,
                    roleTitle = if (isMeHost) (if (currentLang == Language.RU) "ХОСТ (ВЫ)" else "HOST (YOU)") else (if (currentLang == Language.RU) "ХОСТ" else "ROOM HOST"),
                    themeColor = themeColor,
                    avatarEmoji = hostPlayer?.avatarEmoji ?: "",
                    avatarBgColor = hostPlayer?.avatarBgColor ?: "",
                    avatarFrame = hostPlayer?.avatarFrame ?: "standard",
                    modifier = Modifier.weight(1f),
                    hasGradient = hostPlayer?.hasGradient ?: false
                )

                // Neon VS Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(1.5.dp, themeColor),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = themeColor
                        )
                    }
                }

                // Right: Guest / Opponent Card
                if (guestPlayer == null) {
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = themeColor,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (currentLang == Language.RU) "Ожидание игрока..." else "Waiting for player...",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (currentLang == Language.RU) "Код" else "Code"}: ${room.roomId}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = themeColor
                            )
                        }
                    }
                } else {
                    val isMeGuest = guestPlayer.uid == localUid
                    RoomPlayerCard(
                        playerName = guestPlayer.name,
                        playerTier = guestPlayer.tier,
                        isReady = guestPlayer.isReady,
                        roleTitle = if (isMeGuest) (if (currentLang == Language.RU) "ГОСТЬ (ВЫ)" else "GUEST (YOU)") else (if (currentLang == Language.RU) "ГОСТЬ" else "GUEST"),
                        themeColor = if (guestPlayer.isReady) Color(0xFF00E676) else MaterialTheme.colorScheme.error,
                        avatarEmoji = guestPlayer.avatarEmoji,
                        avatarBgColor = guestPlayer.avatarBgColor,
                        avatarFrame = guestPlayer.avatarFrame,
                        modifier = Modifier.weight(1f),
                        hasGradient = guestPlayer.hasGradient
                    )
                }
            }

            // ACTION READY / START BUTTON
            if (isHost) {
                Button(
                    onClick = {
                        viewModel.triggerAudioFeedback("success")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.lobbyManager.startGame()
                    },
                    enabled = allReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color.Black,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (allReady) {
                            if (currentLang == Language.RU) "НАЧАТЬ ДУЭЛЬ" else "START DUEL"
                        } else {
                            if (currentLang == Language.RU) "ОЖИДАНИЕ ГОТОВНОСТИ" else "WAITING FOR PLAYERS"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                val localReady = localPlayer?.isReady ?: false
                Button(
                    onClick = {
                        viewModel.triggerAudioFeedback("click")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.lobbyManager.toggleReady(!localReady)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (localReady) Color(0xFF00E676) else themeColor,
                        contentColor = if (localReady) Color.Black else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (localReady) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (localReady) {
                            if (currentLang == Language.RU) "ГОТОВ К БОЮ!" else "READY FOR BATTLE!"
                        } else {
                            if (currentLang == Language.RU) "НАЖМИ «ГОТОВ»" else "PRESS «READY»"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // ROOM PARTY CHAT
            LobbyChatComponent(
                messages = roomChatMessages,
                themeColor = themeColor,
                currentLang = currentLang,
                onSendMessage = { text, replySender, replyText ->
                    viewModel.lobbyManager.sendRoomChatMessage(
                        text = text,
                        username = localName,
                        hasGradient = hasNicknameGradient,
                        tier = localTier,
                        avatarEmoji = customAvatarEmoji,
                        avatarBgColor = customAvatarBgColor,
                        avatarFrame = equippedAvatarFrame,
                        replyToSender = replySender,
                        replyToText = replyText
                    )
                },
                onDeleteMessage = { msgId ->
                    viewModel.lobbyManager.deleteRoomChatMessage(msgId)
                },
                onToggleReaction = { msgId, emoji ->
                    viewModel.lobbyManager.toggleRoomMessageReaction(msgId, emoji)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                isRoomChat = true,
                isRoomHost = isHost
            )
        }
    }
}

@Composable
fun RoomPlayerCard(
    playerName: String,
    playerTier: String,
    isReady: Boolean,
    roleTitle: String,
    themeColor: Color,
    avatarEmoji: String = "",
    avatarBgColor: String = "",
    avatarFrame: String = "standard",
    modifier: Modifier = Modifier,
    hasGradient: Boolean = false
) {
    ElevatedCard(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = themeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = roleTitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Circular Player Avatar
            PlayerAvatarView(
                playerName = playerName,
                avatarEmoji = avatarEmoji,
                avatarBgColorHex = avatarBgColor,
                avatarFrame = avatarFrame,
                size = 46.dp,
                themeColor = themeColor,
                showOnlineDot = true,
                isOnline = true
            )

            val playerColor = parseHexColor(avatarBgColor, themeColor)
            val playerBrush = rememberAnimatedNicknameBrush(baseColor = playerColor)
            Text(
                text = playerName,
                style = if (hasGradient) {
                    MaterialTheme.typography.titleSmall.copy(brush = playerBrush)
                } else {
                    MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurface)
                },
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isReady) Color(0xFF00E676).copy(alpha = 0.18f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isReady) Color(0xFF00E676) else MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = if (isReady) "READY" else "WAITING",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (isReady) Color(0xFF00E676) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
