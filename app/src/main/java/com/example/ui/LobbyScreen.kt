package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.db.ChatMessage
import com.example.db.LobbyRoom
import com.example.db.RoomPlayer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

val NicknameGradientBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFE94560), Color(0xFFFF0055), Color(0xFFFF7B00))
)

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

    // Connect logic on enter
    LaunchedEffect(localName, localTier, hasNicknameGradient) {
        viewModel.lobbyManager.startPresenceUpdates(localName, localTier, hasNicknameGradient)
        viewModel.lobbyManager.startRoomsSubscription()
        viewModel.lobbyManager.startLobbyChatSubscription()
    }

    // Watch current room status to auto-transition into game screen
    LaunchedEffect(currentRoom?.status) {
        if (currentRoom?.status == "playing") {
            onNavigateToGame()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Keep presence running if they just go back, but we can clean up if desired.
            // Actually let's stop subscriptions when they exit the lobby screen
            viewModel.lobbyManager.cleanUpAllListeners()
        }
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
    
    val coroutineScope = rememberCoroutineScope()
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var joinRoomPendingPassword by remember { mutableStateOf<LobbyRoom?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var joinErrorMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AdaptiveText(
                            text = if (currentLang == Language.RU) "ОНЛАЙН ЛОББИ" else "CONCENSUS BATTLEGROUND",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Green)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AdaptiveText(
                                text = "${if (currentLang == Language.RU) "Онлайн" else "Online"}: $onlinePlayersCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateRoomDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeColor.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Room", tint = themeColor)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 14.dp)
        ) {
            // Rooms List section
            AdaptiveText(
                text = if (currentLang == Language.RU) "ДОСТУПНЫЕ КОМНАТЫ" else "ACTIVE BATTLE ROOMS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = themeColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                if (activeRooms.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AdaptiveText(
                            text = if (currentLang == Language.RU) "Нет активных комнат. Создайте свою!" else "No active rooms. Host a battle!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeRooms) { room ->
                            RoomListItemCard(
                                room = room,
                                currentLang = currentLang,
                                themeColor = themeColor,
                                onJoin = {
                                    if (room.isLocked) {
                                        passwordInput = ""
                                        joinErrorMessage = null
                                        joinRoomPendingPassword = room
                                    } else {
                                        viewModel.lobbyManager.joinRoom(
                                            roomId = room.roomId,
                                            passwordInput = "",
                                            playerName = localName,
                                            playerTier = localTier,
                                            playerHasGradient = hasNicknameGradient,
                                            onSuccess = {},
                                            onFailure = { err -> 
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

            Spacer(modifier = Modifier.height(10.dp))

            // Global Chat section
            AdaptiveText(
                text = if (currentLang == Language.RU) "ГЛОБАЛЬНЫЙ ЧАТ ЛОББИ" else "GLOBAL LOBBY CHAT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = themeColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            LobbyChatComponent(
                messages = lobbyChatMessages,
                themeColor = themeColor,
                currentLang = currentLang,
                onSendMessage = { text ->
                    viewModel.lobbyManager.sendLobbyChatMessage(text, localName, hasNicknameGradient)
                },
                modifier = Modifier.weight(0.55f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Create Room Dialog
        if (showCreateRoomDialog) {
            var roomNameInput by remember { mutableStateOf("$localName's Room") }
            var roomPasswordInput by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showCreateRoomDialog = false },
                title = {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "Создать комнату" else "Create Battle Room",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = roomNameInput,
                            onValueChange = { roomNameInput = it },
                            label = { Text(if (currentLang == Language.RU) "Название комнаты" else "Room Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = roomPasswordInput,
                            onValueChange = { roomPasswordInput = it },
                            label = { Text(if (currentLang == Language.RU) "Пароль (необязательно)" else "Password (Optional)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (roomNameInput.isNotBlank()) {
                                viewModel.lobbyManager.createRoom(
                                    name = roomNameInput,
                                    passwordInput = roomPasswordInput,
                                    hostName = localName,
                                    hostTier = localTier,
                                    hostHasGradient = hasNicknameGradient,
                                    onSuccess = {
                                        showCreateRoomDialog = false
                                        viewModel.triggerAudioFeedback("success")
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                    ) {
                        Text(if (currentLang == Language.RU) "Создать" else "Host")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateRoomDialog = false }) {
                        Text(if (currentLang == Language.RU) "Отмена" else "Cancel")
                    }
                }
            )
        }

        // Password Verification Dialog
        if (joinRoomPendingPassword != null) {
            AlertDialog(
                onDismissRequest = { joinRoomPendingPassword = null },
                title = {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "Введите пароль" else "Enter Room Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdaptiveText(
                            text = if (currentLang == Language.RU) "Для входа в комнату '${joinRoomPendingPassword!!.name}' требуется пароль."
                                   else "Room '${joinRoomPendingPassword!!.name}' requires a security password.",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(if (currentLang == Language.RU) "Пароль" else "Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
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
                            joinErrorMessage = null
                            viewModel.lobbyManager.joinRoom(
                                roomId = joinRoomPendingPassword!!.roomId,
                                passwordInput = passwordInput,
                                playerName = localName,
                                playerTier = localTier,
                                playerHasGradient = hasNicknameGradient,
                                onSuccess = {
                                    joinRoomPendingPassword = null
                                    viewModel.triggerAudioFeedback("success")
                                },
                                onFailure = { err ->
                                    joinErrorMessage = err
                                    viewModel.triggerAudioFeedback("error")
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                    ) {
                        Text(if (currentLang == Language.RU) "Войти" else "Join")
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
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isFull) { onJoin() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xFF16152B)
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isFull) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else themeColor.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdaptiveText(
                        text = room.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    if (room.isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val hostHasGradient = room.players.find { it.uid == room.hostId }?.hasGradient ?: false
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isFull) Color.Red else Color.Green)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AdaptiveText(
                        text = "${if (currentLang == Language.RU) "Хост" else "Host"}: ${room.hostName}",
                        style = if (hostHasGradient) {
                            MaterialTheme.typography.labelMedium.copy(
                                brush = NicknameGradientBrush
                            )
                        } else {
                            MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Beautiful Pill Count
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isFull) MaterialTheme.colorScheme.errorContainer else themeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    AdaptiveText(
                        text = "${room.players.size}/2",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isFull) MaterialTheme.colorScheme.error else themeColor
                    )
                }

                Button(
                    onClick = onJoin,
                    enabled = !isFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor,
                        contentColor = Color.Black,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "ВХОД" else "JOIN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun LobbyChatComponent(
    messages: List<ChatMessage>,
    themeColor: Color,
    currentLang: Language,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val localUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Chat Log
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(messages) { msg ->
                    val isMyMsg = msg.senderId == localUid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMyMsg) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        if (!isMyMsg) {
                            // Opponent avatar bubble
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp, top = 2.dp)
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(themeColor.copy(alpha = 0.2f))
                                    .border(1.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = msg.senderName.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = if (isMyMsg) Alignment.End else Alignment.Start
                        ) {
                            if (!isMyMsg) {
                                Text(
                                    text = msg.senderName,
                                    style = if (msg.hasGradient) {
                                        MaterialTheme.typography.labelSmall.copy(
                                            brush = NicknameGradientBrush
                                        )
                                    } else {
                                        MaterialTheme.typography.labelSmall.copy(color = themeColor)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                )
                            }
                            
                            // Bubble card
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomEnd = if (isMyMsg) 2.dp else 16.dp,
                                            bottomStart = if (isMyMsg) 16.dp else 2.dp
                                        )
                                    )
                                    .background(if (isMyMsg) themeColor else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isMyMsg) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Timestamp
                            val formatter = remember {
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            }
                            val timeStr = remember(msg.timestamp) {
                                formatter.format(java.util.Date(msg.timestamp))
                            }
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Message Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text(if (currentLang == Language.RU) "Введите сообщение..." else "Send message...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
    val localUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isHost = room.hostId == localUid
    val opponent = room.players.find { it.uid != localUid }
    val localPlayer = room.players.find { it.uid == localUid }
    
    val allReady = room.players.size == 2 && room.players.all { it.isReady }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AdaptiveText(
                            text = room.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        AdaptiveText(
                            text = if (room.isLocked) "SECURE ENCRYPTED ROOM" else "PUBLIC GAME ROOM",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            viewModel.lobbyManager.leaveRoom()
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave Room")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
                .imePadding()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Host Card
                val hostPlayer = room.players.find { it.uid == room.hostId }
                RoomPlayerCard(
                    playerName = room.hostName,
                    playerTier = room.hostTier,
                    isReady = hostPlayer?.isReady ?: true,
                    roleTitle = if (currentLang == Language.RU) "ХОСТ" else "ROOM HOST",
                    themeColor = themeColor,
                    modifier = Modifier.weight(1f),
                    hasGradient = hostPlayer?.hasGradient ?: false
                )

                // Opponent Card
                if (opponent == null) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = themeColor, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "Ожидание..." else "Waiting...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    RoomPlayerCard(
                        playerName = opponent.name,
                        playerTier = opponent.tier,
                        isReady = opponent.isReady,
                        roleTitle = if (currentLang == Language.RU) "СОПЕРНИК" else "OPPONENT",
                        themeColor = themeColor,
                        modifier = Modifier.weight(1f),
                        hasGradient = opponent.hasGradient
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action ready buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isHost) {
                    Button(
                        onClick = {
                            viewModel.triggerAudioFeedback("success")
                            viewModel.lobbyManager.startGame()
                        },
                        enabled = allReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentLang == Language.RU) "НАЧАТЬ ИГРУ" else "START BATTLE",
                            fontWeight = FontWeight.Black,
                            color = if (allReady) Color.Black else Color.Gray
                        )
                    }
                } else {
                    val localReady = localPlayer?.isReady ?: false
                    Button(
                        onClick = {
                            viewModel.triggerAudioFeedback("tap")
                            viewModel.lobbyManager.toggleReady(!localReady)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (localReady) Color.Green else themeColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (localReady) {
                                if (currentLang == Language.RU) "ГОТОВ" else "READY"
                            } else {
                                if (currentLang == Language.RU) "НАЖМИ ГОТОВ" else "PRESS READY"
                            },
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Room Chat
            AdaptiveText(
                text = if (currentLang == Language.RU) "ЧАТ КОМНАТЫ" else "ROOM PARTY CHAT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = themeColor,
                modifier = Modifier.align(Alignment.Start).padding(vertical = 4.dp)
            )

            LobbyChatComponent(
                messages = roomChatMessages,
                themeColor = themeColor,
                currentLang = currentLang,
                onSendMessage = { text ->
                    viewModel.lobbyManager.sendRoomChatMessage(text, localName, hasNicknameGradient)
                },
                modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier,
    hasGradient: Boolean = false
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .border(
                1.dp,
                if (isReady) Color.Green.copy(alpha = 0.3f) else themeColor.copy(alpha = 0.15f),
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141324))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AdaptiveText(
                text = roleTitle,
                style = MaterialTheme.typography.labelSmall,
                color = themeColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            AdaptiveText(
                text = playerName,
                style = if (hasGradient) {
                    MaterialTheme.typography.bodyMedium.copy(
                        brush = NicknameGradientBrush
                    )
                } else {
                    MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                },
                fontWeight = FontWeight.Black
            )
            AdaptiveText(
                text = playerTier,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isReady) Color.Green else Color.Red)
                )
                Spacer(modifier = Modifier.width(4.dp))
                AdaptiveText(
                    text = if (isReady) "READY" else "NOT READY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isReady) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Language enum copy just in case, but we can read it from viewModel or imports if available.
// In the other screens, the Language model is: com.example.ui.Language. Wait, let's verify if com.example.ui.Language is imported or what language class is used.
// Let's check imports: we imported Language from package com.example.ui.Language if it exists.
// Yes, Language class is indeed com.example.ui.Language. We can check where Language is defined by doing a python search.
// In ProfileScreen.kt, it has: import com.example.ui.Language. So Language is in package com.example.ui.
