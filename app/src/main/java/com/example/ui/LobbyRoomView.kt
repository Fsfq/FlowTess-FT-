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
    val localCredits by viewModel.credits.collectAsStateWithLifecycle()

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
            val clip = ClipData.newPlainText("FlowTess Room Code", room.roomId)
            clipboard.setPrimaryClip(clip)
            showCopiedToast = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("click")
        } catch (e: Exception) {
            // Ignore
        }
    }

    var showLeavePenaltyDialog by remember { mutableStateOf(false) }

    fun executeLeaveRoom() {
        val penalized = viewModel.recordRoomExitAndApplyPenaltyIfNeeded()
        if (penalized) {
            showLeavePenaltyDialog = true
        }
        viewModel.lobbyManager.leaveRoom()
    }

    if (showLeavePenaltyDialog) {
        val penTitle = when (currentLang) {
            Language.RU -> "Штраф за частые выходы!"
            Language.UA -> "Штраф за часті виходи!"
            Language.KK -> "Жиі шығу үшін айыппұл!"
            Language.DE -> "Strafe für häufiges Verlassen!"
            Language.ZH -> "频繁退出房间惩罚"
            else -> "Room Leaver Penalty!"
        }
        val penDesc = when (currentLang) {
            Language.RU -> "Вы покинули комнаты более 3 раз за последний час. С вашего баланса списан штраф 100 монет за срыв подбора игроков."
            Language.UA -> "Ви залишили кімнати більше 3 разів за останню годину. З вашого балансу списано штраф 100 монет за зрив підбору гравців."
            Language.KK -> "Сіз соңғы бір сағатта бөлмеден 3 реттен көп шықтыңыз. Балансыңыздан 100 монета айыппұл ұсталды."
            Language.DE -> "Du hast Räume mehr als 3 Mal in der letzten Stunde verlassen. 100 Münzen Strafe wurden abgezogen."
            Language.ZH -> "您在最近1小时内退出房间超过3次。因影响正常联机匹配，系统已扣除 100 金币惩罚。"
            else -> "You left rooms more than 3 times within an hour. A 100 coin penalty was deducted from your balance."
        }
        AlertDialog(
            onDismissRequest = { showLeavePenaltyDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(penTitle, fontWeight = FontWeight.Bold) },
            text = { Text(penDesc, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { showLeavePenaltyDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
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
                                val codePrefix = when (currentLang) {
                                    Language.RU -> "Код"
                                    Language.UA -> "Код"
                                    Language.KK -> "Код"
                                    Language.DE -> "Code"
                                    Language.ZH -> "代码"
                                    else -> "Code"
                                }
                                Text(
                                    text = "$codePrefix: ${room.roomId}",
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
                            executeLeaveRoom()
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
        var showInviteFriendsDialog by remember { mutableStateOf(false) }

        if (showInviteFriendsDialog) {
            val friends by viewModel.friendsList.collectAsStateWithLifecycle()
            val inviteDuelTitle = when (currentLang) {
                Language.RU -> "Пригласить друга в дуэль"
                Language.UA -> "Запросити друга на дуель"
                Language.KK -> "Досыңды дуэльге шақыру"
                Language.DE -> "Freund zum Duell einladen"
                Language.ZH -> "邀请好友加入对决"
                else -> "Invite Friend to Duel"
            }
            val noFriendsDesc = when (currentLang) {
                Language.RU -> "У вас пока нет друзей в списке."
                Language.UA -> "У вас поки немає друзів у списку."
                Language.KK -> "Сізде әзірге достар жоқ."
                Language.DE -> "Du hast noch keine Freunde in der Liste."
                Language.ZH -> "您的好友列表暂无好友。"
                else -> "You have no friends in your list."
            }
            val inviteBtn = when (currentLang) {
                Language.RU -> "Позвать"
                Language.UA -> "Покликати"
                Language.KK -> "Шақыру"
                Language.DE -> "Einladen"
                Language.ZH -> "邀请"
                else -> "Invite"
            }
            AlertDialog(
                onDismissRequest = { showInviteFriendsDialog = false },
                title = {
                    Text(
                        text = inviteDuelTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (friends.isEmpty()) {
                        Text(
                            text = noFriendsDesc,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(friends) { friend ->
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            PlayerAvatarView(
                                                playerName = friend.username,
                                                avatarEmoji = friend.avatarEmoji,
                                                avatarBgColorHex = friend.avatarBgColor,
                                                avatarFrame = friend.avatarFrame,
                                                avatarBase64 = friend.avatarBase64,
                                                size = 36.dp,
                                                themeColor = themeColor
                                            )
                                            Column {
                                                Text(friend.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text(friend.onlineTier, style = MaterialTheme.typography.labelSmall, color = themeColor)
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.lobbyManager.sendRoomInvite(
                                                    targetUid = friend.uid,
                                                    roomId = room.roomId,
                                                    roomName = room.name,
                                                    hostName = localName,
                                                    avatarEmoji = customAvatarEmoji,
                                                    avatarBgColor = customAvatarBgColor,
                                                    avatarFrame = equippedAvatarFrame,
                                                    hostTier = localTier
                                                )
                                                viewModel.triggerAudioFeedback("success")
                                                showInviteFriendsDialog = false
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(inviteBtn, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInviteFriendsDialog = false }) {
                        Text(Translations.get("cancel", currentLang))
                    }
                }
            )
        }

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

                val hostRoleTitle = if (isMeHost) {
                    when (currentLang) {
                        Language.RU -> "ХОСТ (ВЫ)"
                        Language.UA -> "ХОСТ (ВИ)"
                        Language.KK -> "ХОСТ (СІЗ)"
                        Language.DE -> "HOST (DU)"
                        Language.ZH -> "房主 (您)"
                        else -> "HOST (YOU)"
                    }
                } else {
                    when (currentLang) {
                        Language.RU -> "ХОСТ"
                        Language.UA -> "ХОСТ"
                        Language.KK -> "ХОСТ"
                        Language.DE -> "HOST"
                        Language.ZH -> "房主"
                        else -> "ROOM HOST"
                    }
                }

                // Left: Host Card
                RoomPlayerCard(
                    playerName = hostPlayer?.name ?: room.hostName,
                    playerTier = hostPlayer?.tier ?: room.hostTier,
                    isReady = hostPlayer?.isReady ?: true,
                    roleTitle = hostRoleTitle,
                    themeColor = themeColor,
                    avatarEmoji = hostPlayer?.avatarEmoji ?: "",
                    avatarBgColor = hostPlayer?.avatarBgColor ?: "",
                    avatarFrame = hostPlayer?.avatarFrame ?: "standard",
                    avatarBase64 = hostPlayer?.avatarBase64 ?: "",
                    modifier = Modifier.weight(1f),
                    hasGradient = hostPlayer?.hasGradient ?: false,
                    winStreak = hostPlayer?.winStreak ?: 0,
                    rating = hostPlayer?.rating ?: 1000,
                    customTag = hostPlayer?.customTag ?: "",
                    credits = if (isMeHost) localCredits else (hostPlayer?.credits ?: 0),
                    isOnline = true
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
                            .height(182.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = themeColor,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                                val waitingOpponent = when (currentLang) {
                                    Language.RU -> "Ожидание..."
                                    Language.UA -> "Очікування..."
                                    Language.KK -> "Күтілуде..."
                                    Language.DE -> "Warten..."
                                    Language.ZH -> "等待对手..."
                                    else -> "Waiting..."
                                }
                                val codePrefix = when (currentLang) {
                                    Language.RU -> "Код"
                                    Language.UA -> "Код"
                                    Language.KK -> "Код"
                                    Language.DE -> "Code"
                                    Language.ZH -> "代码"
                                    else -> "Code"
                                }
                                Text(
                                    text = waitingOpponent,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$codePrefix: ${room.roomId}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = themeColor
                                )
                            }
                            if (isMeHost) {
                                val inviteFriendBtnText = when (currentLang) {
                                    Language.RU -> "Позвать друга"
                                    Language.UA -> "Покликати друга"
                                    Language.KK -> "Досты шақыру"
                                    Language.DE -> "Freund einladen"
                                    Language.ZH -> "邀请好友"
                                    else -> "Invite Friend"
                                }
                                FilledTonalButton(
                                    onClick = { showInviteFriendsDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = inviteFriendBtnText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val isMeGuest = guestPlayer.uid == localUid
                    val guestRoleTitle = if (isMeGuest) {
                        when (currentLang) {
                            Language.RU -> "ГОСТЬ (ВЫ)"
                            Language.UA -> "ГІСТЬ (ВИ)"
                            Language.KK -> "ҚОНАҚ (СІЗ)"
                            Language.DE -> "GAST (DU)"
                            Language.ZH -> "挑战者 (您)"
                            else -> "GUEST (YOU)"
                        }
                    } else {
                        when (currentLang) {
                            Language.RU -> "ГОСТЬ"
                            Language.UA -> "ГІСТЬ"
                            Language.KK -> "ҚОНАҚ"
                            Language.DE -> "GAST"
                            Language.ZH -> "挑战者"
                            else -> "GUEST"
                        }
                    }
                    RoomPlayerCard(
                        playerName = guestPlayer.name,
                        playerTier = guestPlayer.tier,
                        isReady = guestPlayer.isReady,
                        roleTitle = guestRoleTitle,
                        themeColor = if (guestPlayer.isReady) Color(0xFF00E676) else MaterialTheme.colorScheme.error,
                        avatarEmoji = guestPlayer.avatarEmoji,
                        avatarBgColor = guestPlayer.avatarBgColor,
                        avatarFrame = guestPlayer.avatarFrame,
                        avatarBase64 = guestPlayer.avatarBase64,
                        modifier = Modifier.weight(1f),
                        hasGradient = guestPlayer.hasGradient,
                        winStreak = guestPlayer.winStreak,
                        rating = guestPlayer.rating,
                        customTag = guestPlayer.customTag,
                        credits = if (isMeGuest) localCredits else guestPlayer.credits,
                        isOnline = true
                    )
                }
            }

            // ACTION READY / START BUTTON
            if (isHost) {
                val hostBtnText = if (allReady) {
                    when (currentLang) {
                        Language.RU -> "НАЧАТЬ ДУЭЛЬ"
                        Language.UA -> "ПОЧАТИ ДУЕЛЬ"
                        Language.KK -> "ДУЭЛЬДІ БАСТАУ"
                        Language.DE -> "DUELL STARTEN"
                        Language.ZH -> "开启对决"
                        else -> "START DUEL"
                    }
                } else {
                    when (currentLang) {
                        Language.RU -> "ОЖИДАНИЕ ГОТОВНОСТИ"
                        Language.UA -> "ОЧІКУВАННЯ ГОТОВНОСТІ"
                        Language.KK -> "ДАЙЫНДЫҚТЫ КҮТУ"
                        Language.DE -> "WARTE AUF BEREITSCHAFT"
                        Language.ZH -> "等待玩家准备"
                        else -> "WAITING FOR PLAYERS"
                    }
                }
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
                        text = hostBtnText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                val localReady = localPlayer?.isReady ?: false
                val guestBtnText = if (localReady) {
                    when (currentLang) {
                        Language.RU -> "ГОТОВ К БОЮ!"
                        Language.UA -> "ГОТОВИЙ ДО БОЮ!"
                        Language.KK -> "ШАЙҚАСҚА ДАЙЫН!"
                        Language.DE -> "BEREIT ZUM KAMPF!"
                        Language.ZH -> "准备就绪！"
                        else -> "READY FOR BATTLE!"
                    }
                } else {
                    when (currentLang) {
                        Language.RU -> "НАЖМИ «ГОТОВ»"
                        Language.UA -> "НАТИСНИ «ГОТОВИЙ»"
                        Language.KK -> "«ДАЙЫН» БАСЫҢЫЗ"
                        Language.DE -> "DRÜCKE «BEREIT»"
                        Language.ZH -> "点击「准备」"
                        else -> "PRESS «READY»"
                    }
                }
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
                        text = guestBtnText,
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
    avatarBase64: String = "",
    modifier: Modifier = Modifier,
    hasGradient: Boolean = false,
    winStreak: Int = 0,
    rating: Int = 1000,
    customTag: String = "",
    credits: Int = 0,
    isOnline: Boolean = true
) {
    ElevatedCard(
        modifier = modifier.height(182.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = themeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = roleTitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                if (winStreak > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF5722).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "x$winStreak",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF5722),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Circular Player Avatar
            PlayerAvatarView(
                playerName = playerName,
                avatarEmoji = avatarEmoji,
                avatarBgColorHex = avatarBgColor,
                avatarFrame = avatarFrame,
                avatarBase64 = avatarBase64,
                size = 44.dp,
                themeColor = themeColor,
                showOnlineDot = true,
                isOnline = isOnline
            )

            val playerColor = parseHexColor(avatarBgColor, themeColor)
            val playerBrush = rememberAnimatedNicknameBrush(baseColor = playerColor)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (customTag.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 3.dp)
                        ) {
                            Text(
                                text = "[$customTag]",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .widthIn(max = 55.dp)
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = playerName,
                        style = if (hasGradient) {
                            MaterialTheme.typography.labelMedium.copy(brush = playerBrush)
                        } else {
                            MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        },
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ELO $rating • $playerTier",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (credits > 0) {
                        Text(
                            text = "• $credits",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }

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

