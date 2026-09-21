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
    val localCredits by viewModel.credits.collectAsStateWithLifecycle()
    
    val currentRoom by viewModel.lobbyManager.currentRoom.collectAsStateWithLifecycle()
    val themeColor = MaterialTheme.colorScheme.primary

    val incomingInvite by viewModel.lobbyManager.incomingInvite.collectAsStateWithLifecycle()

    // Connect presence, room, and invite listeners on enter
    LaunchedEffect(localName, localTier, hasNicknameGradient, localCredits) {
        viewModel.lobbyManager.startPresenceUpdates(localName, localTier, hasNicknameGradient, localCredits)
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
                    text = Translations.get("duel_invitation", currentLang),
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
                        avatarBase64 = invite.hostAvatarBase64,
                        size = 48.dp,
                        themeColor = themeColor
                    )
                    val inviteMsg = when (currentLang) {
                        Language.RU -> "${invite.hostName} приглашает вас в комнату «${invite.roomName}»"
                        Language.UA -> "${invite.hostName} запрошує вас до кімнати «${invite.roomName}»"
                        Language.KK -> "${invite.hostName} сізді «${invite.roomName}» бөлмесіне шақырады"
                        Language.DE -> "${invite.hostName} lädt dich in den Raum «${invite.roomName}» ein"
                        Language.ZH -> "${invite.hostName} 邀请你加入对战房间 «${invite.roomName}»"
                        else -> "${invite.hostName} invites you to «${invite.roomName}»"
                    }
                    Text(
                        text = inviteMsg,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                    Text(Translations.get("accept_duel", currentLang), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.lobbyManager.dismissInvite(invite.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Translations.get("decline", currentLang))
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
            val defaultName = when (currentLang) {
                Language.RU -> "Комната $localName"
                Language.UA -> "Кімната $localName"
                Language.KK -> "$localName бөлмесі"
                Language.DE -> "Raum von $localName"
                Language.ZH -> "$localName 的房间"
                else -> "$localName's Room"
            }
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
                            text = Translations.get("multiplayer", currentLang).uppercase(),
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
                                text = "${Translations.get("online", currentLang)}: $onlinePlayersCount",
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
                        Triple(
                            0,
                            when (currentLang) {
                                Language.RU -> "Комнаты"
                                Language.UA -> "Кімнати"
                                Language.KK -> "Бөлмелер"
                                Language.DE -> "Räume"
                                Language.ZH -> "对战房间"
                                else -> "Rooms"
                            },
                            activeRooms.size
                        ),
                        Triple(
                            1,
                            when (currentLang) {
                                Language.RU -> "Общий Чат"
                                Language.UA -> "Загальний Чат"
                                Language.KK -> "Жалпы Чат"
                                Language.DE -> "Globaler Chat"
                                Language.ZH -> "公共聊天"
                                else -> "Global Chat"
                            },
                            lobbyChatMessages.size
                        )
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
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (count > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                        ) {
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.onSurfaceVariant,
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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
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
                                                    text = Translations.get("quick_match_1v1", currentLang),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Text(
                                                    text = Translations.get("quick_match_desc", currentLang),
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

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val filters = listOf(
                                        "ALL" to Translations.getLobbyFilter("ALL", currentLang),
                                        "OPEN" to Translations.getLobbyFilter("OPEN", currentLang),
                                        "LOCKED" to Translations.getLobbyFilter("LOCKED", currentLang)
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
                                                text = Translations.get("no_rooms_found", currentLang),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = Translations.get("no_rooms_desc", currentLang),
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
                                                Text(Translations.get("create_room", currentLang), fontWeight = FontWeight.Bold)
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
            val defaultName = when (currentLang) {
                Language.RU -> "Комната $localName"
                Language.UA -> "Кімната $localName"
                Language.KK -> "$localName бөлмесі"
                Language.DE -> "Raum von $localName"
                Language.ZH -> "$localName 的房间"
                else -> "$localName's Room"
            }
            var roomNameInput by remember { mutableStateOf(defaultName) }
            var selectedGameMode by remember { mutableStateOf("CLASSIC") }
            var selectedGarbageIntensity by remember { mutableFloatStateOf(1.0f) }
            var selectedRoundTarget by remember { mutableIntStateOf(1) }
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
                                    text = Translations.get("create_room", currentLang).uppercase(),
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
                                            text = Translations.get("create_room", currentLang).uppercase(),
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
                                        text = Translations.get("room_name", currentLang),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )
                                    OutlinedTextField(
                                        value = roomNameInput,
                                        onValueChange = { roomNameInput = it },
                                        placeholder = { Text(Translations.get("enter_room_name", currentLang)) },
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
                                        text = Translations.get("match_mode", currentLang),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )

                                    val modes = listOf(
                                        Triple("CLASSIC", Translations.getLobbyModeTitle("CLASSIC", currentLang), Translations.getLobbyModeDesc("CLASSIC", currentLang)),
                                        Triple("SCORE_RACE", Translations.getLobbyModeTitle("SCORE_RACE", currentLang), Translations.getLobbyModeDesc("SCORE_RACE", currentLang)),
                                        Triple("SPRINT", Translations.getLobbyModeTitle("SPRINT", currentLang), Translations.getLobbyModeDesc("SPRINT", currentLang)),
                                        Triple("BLITZ", Translations.getLobbyModeTitle("BLITZ", currentLang), Translations.getLobbyModeDesc("BLITZ", currentLang)),
                                        Triple("HYPER", Translations.getLobbyModeTitle("HYPER", currentLang), Translations.getLobbyModeDesc("HYPER", currentLang))
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
                                    val seriesTitle = when (currentLang) {
                                        Language.RU -> "ФОРМАТ СЕРИИ МАТЧА"
                                        Language.UA -> "ФОРМАТ СЕРІЇ МАТЧУ"
                                        Language.KK -> "МАТЧ СЕРИЯСЫНЫҢ ФОРМАТЫ"
                                        Language.DE -> "SERIENFORMAT"
                                        Language.ZH -> "系列赛制规则"
                                        else -> "MATCH SERIES FORMAT"
                                    }
                                    Text(
                                        text = seriesTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )

                                    val rounds = listOf(
                                        Triple(1, "BO1", Translations.getLobbySeriesFormat(1, currentLang)),
                                        Triple(3, "BO3", Translations.getLobbySeriesFormat(3, currentLang)),
                                        Triple(5, "BO5", Translations.getLobbySeriesFormat(5, currentLang))
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rounds.forEach { (count, boText, labelText) ->
                                            val isSelected = selectedRoundTarget == count
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        viewModel.triggerAudioFeedback("click")
                                                        selectedRoundTarget = count
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = boText,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = labelText,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    val garbageTitle = when (currentLang) {
                                        Language.RU -> "ИНТЕНСИВНОСТЬ АТАКИ МУСОРОМ"
                                        Language.UA -> "ІНТЕНСИВНІСТЬ АТАКИ СМІТТЯМ"
                                        Language.KK -> "ҚОҚЫСПЕН ШАБУЫЛ ҚАРҚЫНДЫЛЫҒЫ"
                                        Language.DE -> "MÜLLANGRIFFS-INTENSITÄT"
                                        Language.ZH -> "垃圾行干扰强度"
                                        else -> "GARBAGE ATTACK INTENSITY"
                                    }
                                    Text(
                                        text = garbageTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )

                                    val intensities = listOf(
                                        Triple(0.0f, "0x", Translations.getLobbyGarbageIntensity(0.0f, currentLang)),
                                        Triple(0.5f, "0.5x", Translations.getLobbyGarbageIntensity(0.5f, currentLang)),
                                        Triple(1.0f, "1.0x", Translations.getLobbyGarbageIntensity(1.0f, currentLang)),
                                        Triple(1.5f, "1.5x", Translations.getLobbyGarbageIntensity(1.5f, currentLang)),
                                        Triple(2.0f, "2.0x", Translations.getLobbyGarbageIntensity(2.0f, currentLang))
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        intensities.forEach { (intensity, multText, labelText) ->
                                            val isSelected = selectedGarbageIntensity == intensity
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        viewModel.triggerAudioFeedback("click")
                                                        selectedGarbageIntensity = intensity
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = multText,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = labelText,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
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
                                    val privateTitle = when (currentLang) {
                                        Language.RU -> "ПРИВАТНАЯ КОМНАТА"
                                        Language.UA -> "ПРИВАТНА КІМНАТА"
                                        Language.KK -> "ЖЕКЕ БӨЛМЕ"
                                        Language.DE -> "PRIVATER RAUM"
                                        Language.ZH -> "私密房间"
                                        else -> "PRIVATE ROOM"
                                    }
                                    val privateDesc = when (currentLang) {
                                        Language.RU -> "Вход только по паролю"
                                        Language.UA -> "Вхід тільки за паролем"
                                        Language.KK -> "Тек құпия сөзбен кіру"
                                        Language.DE -> "Beitritt nur mit Passwort"
                                        Language.ZH -> "需要密码才能进入"
                                        else -> "Require password to join"
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = privateTitle,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = privateDesc,
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
                                        val roomPwdLabel = when (currentLang) {
                                            Language.RU -> "Пароль комнаты"
                                            Language.UA -> "Пароль кімнати"
                                            Language.KK -> "Бөлме құпия сөзі"
                                            Language.DE -> "Raum-Passwort"
                                            Language.ZH -> "房间密码"
                                            else -> "Room Password"
                                        }
                                        OutlinedTextField(
                                            value = roomPasswordInput,
                                            onValueChange = { roomPasswordInput = it },
                                            label = { Text(roomPwdLabel) },
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
            val joinByCodeTitle = when (currentLang) {
                Language.RU -> "Вход по коду"
                Language.UA -> "Вхід за кодом"
                Language.KK -> "Код бойынша кіру"
                Language.DE -> "Per Code beitreten"
                Language.ZH -> "输入房间代码加入"
                else -> "Join by Room Code"
            }
            val joinByCodeDesc = when (currentLang) {
                Language.RU -> "Введите 6-значный номер комнаты, полученный от друга:"
                Language.UA -> "Введіть 6-значний номер кімнати, отриманий від друга:"
                Language.KK -> "Досыңыздан алған 6 таңбалы бөлме нөмірін енгізіңіз:"
                Language.DE -> "Gib den 6-stelligen Raumcode deines Freundes ein:"
                Language.ZH -> "请输入好友分享的6位房间代码："
                else -> "Enter the 6-digit room code shared by your friend:"
            }
            val codeInputLabel = when (currentLang) {
                Language.RU -> "Код комнаты (6 цифр)"
                Language.UA -> "Код кімнати (6 цифр)"
                Language.KK -> "Бөлме коды (6 сан)"
                Language.DE -> "Raumcode (6 Ziffern)"
                Language.ZH -> "房间代码 (6位数字)"
                else -> "Room Code (6 digits)"
            }
            val joinBtn = when (currentLang) {
                Language.RU -> "Присоединиться"
                Language.UA -> "Приєднатися"
                Language.KK -> "Қосылу"
                Language.DE -> "Beitreten"
                Language.ZH -> "加入"
                else -> "Join"
            }
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
                        text = joinByCodeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .imePadding()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = joinByCodeDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { if (it.length <= 6) joinCodeInput = it },
                            label = { Text(codeInputLabel) },
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
                        Text(joinBtn, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinCodeDialog = false }) {
                        Text(Translations.get("cancel", currentLang))
                    }
                }
            )
        }

        // PASSWORD VERIFICATION DIALOG
        joinRoomPendingPassword?.let { room ->
            val protectedRoomTitle = when (currentLang) {
                Language.RU -> "Защищенная комната"
                Language.UA -> "Захищена кімната"
                Language.KK -> "Қорғалған бөлме"
                Language.DE -> "Geschützter Raum"
                Language.ZH -> "加密保护房间"
                else -> "Protected Room"
            }
            val protectedRoomPrompt = when (currentLang) {
                Language.RU -> "Для входа в «${room.name}» введите пароль:"
                Language.UA -> "Для входу в «${room.name}» введіть пароль:"
                Language.KK -> "«${room.name}» бөлмесіне кіру үшін құпия сөзді енгізіңіз:"
                Language.DE -> "Gib das Passwort ein, um «${room.name}» beizutreten:"
                Language.ZH -> "加入 «${room.name}» 请输入密码："
                else -> "Enter password to join «${room.name}»:"
            }
            val passwordLabel = when (currentLang) {
                Language.RU -> "Пароль"
                Language.UA -> "Пароль"
                Language.KK -> "Құпия сөз"
                Language.DE -> "Passwort"
                Language.ZH -> "密码"
                else -> "Password"
            }
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
                        text = protectedRoomTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .imePadding()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = protectedRoomPrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(passwordLabel) },
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
                                    joinErrorMessage = when (currentLang) {
                                        Language.RU -> "Неверный пароль"
                                        Language.UA -> "Невірний пароль"
                                        Language.KK -> "Құпия сөз қате"
                                        Language.DE -> "Falsches Passwort"
                                        Language.ZH -> "密码错误"
                                        else -> "Incorrect password"
                                    }
                                    viewModel.triggerAudioFeedback("error")
                                }
                            )
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        val enterBtn = when (currentLang) {
                            Language.RU -> "Войти"
                            Language.UA -> "Увійти"
                            Language.KK -> "Кіру"
                            Language.DE -> "Eintreten"
                            Language.ZH -> "进入"
                            else -> "Enter"
                        }
                        Text(enterBtn, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { joinRoomPendingPassword = null }) {
                        Text(Translations.get("cancel", currentLang))
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
                    avatarBase64 = hostPlayer?.avatarBase64 ?: "",
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
                        val hostLabel = when (currentLang) {
                            Language.RU -> "Хост"
                            Language.UA -> "Хост"
                            Language.KK -> "Хост"
                            Language.DE -> "Host"
                            Language.ZH -> "房主"
                            else -> "Host"
                        }
                        Text(
                            text = "$hostLabel: ${room.hostName}",
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
                val inBattleLabel = when (currentLang) {
                    Language.RU -> "В БОЮ"
                    Language.UA -> "У БОЮ"
                    Language.KK -> "ШАЙҚАСТА"
                    Language.DE -> "IM KAMPF"
                    Language.ZH -> "对战中"
                    else -> "IN BATTLE"
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPlaying) MaterialTheme.colorScheme.errorContainer
                            else if (isFull) MaterialTheme.colorScheme.surfaceContainerHighest
                            else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (isPlaying) inBattleLabel else "${room.players.size}/2",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPlaying) MaterialTheme.colorScheme.onErrorContainer
                                else if (isFull) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                val joinBtnText = when (currentLang) {
                    Language.RU -> "ВОЙТИ"
                    Language.UA -> "УВІЙТИ"
                    Language.KK -> "КІРУ"
                    Language.DE -> "BEITRETEN"
                    Language.ZH -> "加入"
                    else -> "JOIN"
                }
                Button(
                    onClick = onJoin,
                    enabled = !isFull && !isPlaying,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = joinBtnText,
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