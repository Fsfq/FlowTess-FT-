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
    var lastSendTimestamp by remember { mutableLongStateOf(0L) }
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
                    .imePadding()
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
                        val emptyChatText = if (isRoomChat) {
                            when (currentLang) {
                                Language.RU -> "Чат комнаты пуст. Напишите сопернику!"
                                Language.UA -> "Чат кімнати порожній. Напишіть супернику!"
                                Language.KK -> "Бөлме чаты бос. Қарсыласқа жазыңыз!"
                                Language.DE -> "Raum-Chat ist leer. Begrüße deinen Gegner!"
                                Language.ZH -> "房间聊天室暂无消息，和对手打个招呼吧！"
                                else -> "Room chat is empty. Greet your opponent!"
                            }
                        } else {
                            when (currentLang) {
                                Language.RU -> "Сообщений нет. Напишите первым!"
                                Language.UA -> "Повідомлень немає. Напишіть першим!"
                                Language.KK -> "Хабарламалар жоқ. Бірінші болып жазыңыз!"
                                Language.DE -> "Noch keine Nachrichten. Schreib als Erster!"
                                Language.ZH -> "暂无消息，发送第一条消息吧！"
                                else -> "No messages yet. Start the conversation!"
                            }
                        }
                        Text(
                            text = emptyChatText,
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
                                        avatarBase64 = msg.senderAvatarBase64,
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
                                        val replyPrefix = when (currentLang) {
                                            Language.RU -> "Ответ"
                                            Language.UA -> "Відповідь"
                                            Language.KK -> "Жауап"
                                            Language.DE -> "Antwort an"
                                            Language.ZH -> "回复"
                                            else -> "Reply to"
                                        }
                                        Text(
                                            text = "$replyPrefix: ${replyTarget.senderName}",
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
                    val chatPlaceholder = if (isRoomChat) {
                        when (currentLang) {
                            Language.RU -> "Сообщение в комнату..."
                            Language.UA -> "Повідомлення в кімнату..."
                            Language.KK -> "Бөлмеге хабарлама..."
                            Language.DE -> "Nachricht an Raum..."
                            Language.ZH -> "发送房间消息..."
                            else -> "Room message..."
                        }
                    } else {
                        when (currentLang) {
                            Language.RU -> "Сообщение в чат..."
                            Language.UA -> "Повідомлення в чат..."
                            Language.KK -> "Чатқа хабарлама..."
                            Language.DE -> "Chat-Nachricht..."
                            Language.ZH -> "发送公共消息..."
                            else -> "Chat message..."
                        }
                    }
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { if (it.length <= 250) textInput = it },
                        placeholder = {
                            Text(
                                text = chatPlaceholder,
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
                            val now = System.currentTimeMillis()
                            if (now - lastSendTimestamp < 500L) return@IconButton
                            val trimmed = textInput.trim().take(250)
                            if (trimmed.isNotEmpty()) {
                                lastSendTimestamp = now
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

                val actionsTitle = when (currentLang) {
                    Language.RU -> "Действия с сообщением"
                    Language.UA -> "Дії з повідомленням"
                    Language.KK -> "Хабарлама әрекеттері"
                    Language.DE -> "Nachrichtenaktionen"
                    Language.ZH -> "消息操作"
                    else -> "Message Actions"
                }

                AlertDialog(
                    onDismissRequest = { selectedMsgForMenu = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    title = {
                        Text(
                            text = actionsTitle,
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
                            val replyBtn = when (currentLang) {
                                Language.RU -> "Ответить"
                                Language.UA -> "Відповісти"
                                Language.KK -> "Жауап беру"
                                Language.DE -> "Antworten"
                                Language.ZH -> "回复"
                                else -> "Reply"
                            }
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
                                        text = replyBtn,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Copy Action
                            val copyTextBtn = when (currentLang) {
                                Language.RU -> "Копировать текст"
                                Language.UA -> "Копіювати текст"
                                Language.KK -> "Мәтінді көшіру"
                                Language.DE -> "Text kopieren"
                                Language.ZH -> "复制文本"
                                else -> "Copy text"
                            }
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
                                        text = copyTextBtn,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Delete Action
                            if (canDelete) {
                                val deleteMsgBtn = when (currentLang) {
                                    Language.RU -> "Удалить сообщение"
                                    Language.UA -> "Видалити повідомлення"
                                    Language.KK -> "Хабарламаны өшіру"
                                    Language.DE -> "Nachricht löschen"
                                    Language.ZH -> "删除消息"
                                    else -> "Delete message"
                                }
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
                                            text = deleteMsgBtn,
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
                            Text(Translations.get("cancel", currentLang))
                        }
                    }
                )
            }
        }
    }
}