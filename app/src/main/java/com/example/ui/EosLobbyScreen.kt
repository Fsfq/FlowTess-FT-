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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.Dp
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

/**
 * MD3 bouncy press animation modifier for clickable components or pills.
 */
@Composable
private fun Modifier.bouncyClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.93f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncyClickScale"
    )

    return this
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Returns a MutableInteractionSource and a Modifier with bouncy scale animation for standard Buttons.
 */
@Composable
private fun rememberBouncyInteraction(scaleDown: Float = 0.93f): Pair<MutableInteractionSource, Modifier> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncyButtonScale"
    )
    val modifier = Modifier.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
    return interactionSource to modifier
}

/**
 * Vertically-centered input field for EOS lobby that completely prevents text clipping and eaten letters.
 */
@Composable
private fun EosInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    height: Dp = 50.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    themeColor: Color = MaterialTheme.colorScheme.primary
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        cursorBrush = SolidColor(themeColor),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(shape)
                    .background(containerColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leadingIcon?.invoke()
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
                trailingIcon?.invoke()
            }
        }
    )
}


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
            onNavigateToGame()
        }
    }

    var showLeavePenaltyDialog by remember { mutableStateOf(false) }

    fun executeLeaveEosRoom() {
        val penalized = viewModel.recordRoomExitAndApplyPenaltyIfNeeded()
        if (penalized) {
            showLeavePenaltyDialog = true
        }
        EosManager.leaveRoom()
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

    // Back handler
    BackHandler {
        if (currentRoom != null) {
            executeLeaveEosRoom()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (currentRoom != null) currentRoom!!.name else when (currentLang) {
                            Language.RU -> "МУЛЬТИПЛЕЕР"
                            Language.UA -> "МУЛЬТИПЛЕЄР"
                            Language.KK -> "МУЛЬТИПЛЕЕР"
                            Language.DE -> "MULTIPLAYER"
                            Language.ZH -> "多人对战"
                            else -> "MULTIPLAYER"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (currentRoom != null) {
                                executeLeaveEosRoom()
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
                    // Balance without background pill
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.openRewardedAdDialog()
                            }
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$localCredits",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Free Coins",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
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
                    },
                    onLeave = { executeLeaveEosRoom() }
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
 * Main EOS Lobby Browser: Unified Hero Action Card with direct join & create,
 * plus MD3 Segmented Chips for Rooms, Online Peers, and Global Chat.
 */
@Composable
private fun EosLobbyMainView(
    themeColor: Color,
    currentLang: Language,
    viewModel: MainViewModel,
    localCredits: Int,
    isLoggedIn: Boolean
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Rooms, 1: Peers, 2: Chat
    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var directCodeInput by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── HERO ACTION CARD (MD3 Expressive Container) ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Row: Primary Create & Search Buttons with bouncy spring animation
                val (createInteraction, createModifier) = rememberBouncyInteraction()
                val (searchInteraction, searchModifier) = rememberBouncyInteraction()
                var isSearchingRooms by remember { mutableStateOf(false) }

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
                        interactionSource = createInteraction,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .then(createModifier),
                        shape = RoundedCornerShape(16.dp),
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
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 0
                            isSearchingRooms = true
                            EosManager.refreshRooms {
                                isSearchingRooms = false
                            }
                        },
                        interactionSource = searchInteraction,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .then(searchModifier),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSearchingRooms) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = themeColor
                                )
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, tint = themeColor, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Поиск"
                                    Language.UA -> "Пошук"
                                    Language.KK -> "Іздеу"
                                    Language.DE -> "Suche"
                                    Language.ZH -> "搜索"
                                    else -> "Search"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Bottom Direct Code Field + Connect Button
                val (joinInteraction, joinModifier) = rememberBouncyInteraction()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EosInputField(
                        value = directCodeInput,
                        onValueChange = { directCodeInput = it.trim().uppercase() },
                        placeholder = when (currentLang) {
                            Language.RU -> "Код"
                            Language.UA -> "Код"
                            Language.KK -> "Код"
                            Language.DE -> "Code"
                            Language.ZH -> "代码"
                            else -> "Code"
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = themeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (directCodeInput.isNotEmpty()) {
                                IconButton(onClick = { directCodeInput = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        val clip = clipboardManager.getText()?.text
                                        if (!clip.isNullOrBlank()) {
                                            directCodeInput = clip.trim().uppercase()
                                            viewModel.triggerAudioFeedback("click")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        themeColor = themeColor,
                        height = 50.dp,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            if (directCodeInput.isNotBlank()) {
                                viewModel.triggerAudioFeedback("click")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                EosManager.joinRoom(directCodeInput)
                            }
                        },
                        enabled = directCodeInput.length >= 4,
                        interactionSource = joinInteraction,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .then(joinModifier),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Вход"
                                    Language.UA -> "Вхід"
                                    Language.KK -> "Кіру"
                                    Language.DE -> "Beitreten"
                                    Language.ZH -> "加入"
                                    else -> "Join"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // ── SEGMENTED FILTER ROW (MD3 Animated Sliding Pill) ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val tabWidth = maxWidth / tabs.size
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedTab,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                    label = "eosTabIndicator"
                )

                // Smooth animated sliding pill indicator
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .height(38.dp)
                        .offset(x = indicatorOffset)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        val badgeCount = when (index) {
                            0 -> availableRooms.size
                            1 -> onlinePeers.size
                            else -> null
                        }
                        val animatedTextColor by animateColorAsState(
                            targetValue = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "tabTextColor"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .bouncyClick {
                                    selectedTab = index
                                    viewModel.triggerAudioFeedback("click")
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    ),
                                    color = animatedTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (badgeCount != null && badgeCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Text(
                                            text = "$badgeCount",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
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

        // ── TAB CONTENT WITH SMOOTH SLIDE/FADE TRANSITION ──
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width / 4 } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width / 4 } + fadeOut()
                    )
                } else {
                    (slideInHorizontally { width -> -width / 4 } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width / 4 } + fadeOut()
                    )
                }
            },
            label = "eosTabTransition",
            modifier = Modifier.weight(1f)
        ) { targetTab ->
            when (targetTab) {
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
                    // PEERS TAB
                    EosPeersTab(
                        peers = onlinePeers,
                        themeColor = themeColor,
                        currentLang = currentLang,
                        viewModel = viewModel
                    )
                }
                2 -> {
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
 * Tab 0: Modern MD3 Rooms List with Search and Join.
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
    val localCredits by viewModel.credits.collectAsStateWithLifecycle()
    val filteredRooms = remember(rooms, searchQuery) {
        if (searchQuery.isBlank()) rooms
        else rooms.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search & Refresh Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EosInputField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = when (currentLang) {
                    Language.RU -> "Поиск по имени, коду..."
                    Language.UA -> "Пошук за назвою, кодом..."
                    Language.KK -> "Аты, коды бойынша іздеу..."
                    Language.DE -> "Suche nach Name, Code..."
                    Language.ZH -> "按名称或房间号搜索..."
                    else -> "Search by name or code..."
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = themeColor, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                themeColor = themeColor,
                height = 50.dp,
                modifier = Modifier.weight(1f)
            )

            FilledTonalIconButton(
                onClick = {
                    viewModel.triggerAudioFeedback("click")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    EosManager.refreshRooms()
                },
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = themeColor, modifier = Modifier.size(20.dp))
            }
        }

        if (filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    tint = themeColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Активных комнат пока нет"
                                Language.UA -> "Активних кімнат немає"
                                Language.KK -> "Әзірге бөлмелер жоқ"
                                Language.DE -> "Keine Räume gefunden"
                                Language.ZH -> "暂无活跃房间"
                                else -> "No active rooms found"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Создайте свою комнату или воспользуйтесь быстрым поиском"
                                Language.UA -> "Створіть свою кімнату або скористайтеся швидким пошуком"
                                Language.KK -> "Өз бөлмеңізді жасаңыз немесе жылдам іздеуді қолданыңыз"
                                Language.DE -> "Erstelle einen Raum oder nutze die Schnellsuche"
                                Language.ZH -> "您可以点击上方创建房间，或直接使用快速匹配"
                                else -> "Create your room or use Quick Match above"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRooms, key = { it.id }) { room ->
                    val canAffordRoom = localCredits >= room.bet
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f).padding(end = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = room.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (room.isPrivate) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Private",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Host indicator
                                    Text(
                                        text = "${room.hostName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Capacity / Players indicator
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = if (room.guestPuid != null) "2/2" else "1/2",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Bet chip
                                    if (room.bet > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFFD700).copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MonetizationOn,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "${room.bet}",
                                                    color = Color(0xFFFFD700),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            val (joinInteraction, joinModifier) = rememberBouncyInteraction()
                            Button(
                                onClick = {
                                    if (canAffordRoom) {
                                        viewModel.triggerAudioFeedback("click")
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        EosManager.joinRoom(room.id)
                                    }
                                },
                                enabled = canAffordRoom,
                                interactionSource = joinInteraction,
                                shape = RoundedCornerShape(14.dp),
                                modifier = joinModifier,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColor,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (canAffordRoom) {
                                        when (currentLang) {
                                            Language.RU -> "Войти"
                                            Language.UA -> "Увійти"
                                            Language.KK -> "Кіру"
                                            Language.DE -> "Beitreten"
                                            Language.ZH -> "加入"
                                            else -> "Join"
                                        }
                                    } else {
                                        when (currentLang) {
                                            Language.RU -> "Мало монет"
                                            Language.UA -> "Мало монет"
                                            Language.KK -> "Аз монета"
                                            Language.DE -> "Zu wenig"
                                            Language.ZH -> "硬币不足"
                                            else -> "Low coins"
                                        }
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
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
 * Tab 1: Discovered Online Peers.
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
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = themeColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Поиск EOS игроков в вашей сети..."
                            Language.UA -> "Пошук гравців у мережі..."
                            Language.KK -> "Желіде ойыншыларды іздеу..."
                            Language.DE -> "Suche nach EOS-Spielern..."
                            Language.ZH -> "正在发现网络中的 EOS 玩家..."
                            else -> "Discovering online peers..."
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(peers, key = { it.puid }) { peer ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = themeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = themeColor, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = peer.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Вызов",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Global EOS Lobby Chat.
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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
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
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = themeColor
                                )
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
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
            EosInputField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = when (currentLang) {
                    Language.RU -> "Сообщение в общий чат..."
                    Language.UA -> "Повідомлення в загальний чат..."
                    Language.KK -> "Жалпы чатқа хабарлама..."
                    Language.DE -> "Nachricht an globalen Chat..."
                    Language.ZH -> "发送大厅聊天消息..."
                    else -> "Message to global chat..."
                },
                modifier = Modifier.weight(1f),
                height = 50.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                themeColor = themeColor,
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

            FilledIconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        EosManager.sendLobbyChatMessage(chatInput.trim())
                        chatInput = ""
                    }
                },
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = themeColor)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


/**
 * Inside Room View: Displays Host, Guest slots, Ready status, In-Room Chat and Start Match button.
 * Redesigned in Android 17 / MD3 style.
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
        // ── ROOM HEADER CARD (MD3 Container) ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (room.code.isNotBlank()) "КОД КОМНАТЫ" else "EOS LOBBY ID",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = themeColor
                    )
                    Text(
                        text = if (room.code.isNotBlank()) room.code else room.id.take(8),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = if (room.code.isNotBlank()) 3.sp else 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (room.code.isNotBlank()) {
                        Text(
                            text = "Lobby ID: ${room.id.take(8)}...${room.id.takeLast(4)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = {
                            val copyVal = if (room.code.isNotBlank()) room.code else room.id
                            clipboardManager.setText(AnnotatedString(copyVal))
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = themeColor, modifier = Modifier.size(20.dp))
                    }

                    FilledTonalIconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val shareVal = if (room.code.isNotBlank()) room.code else room.id
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Присоединяйся к игре в Tetris EOS! Код комнаты: $shareVal")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share EOS Room"))
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = themeColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // ── PLAYERS SLOTS (MD3 Dual Card Hierarchy) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // HOST CARD
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = "Host", tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                        }
                    }
                    Text(
                        text = room.hostName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00E676).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "ГОТОВ",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // GUEST CARD
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (room.guestPuid != null) {
                        Surface(
                            shape = CircleShape,
                            color = themeColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.SportsEsports, contentDescription = "Guest", tint = themeColor, modifier = Modifier.size(24.dp))
                            }
                        }
                        Text(
                            text = room.guestName ?: "Guest",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (room.isGuestReady) Color(0xFF00E676).copy(alpha = 0.18f) else Color(0xFFFF5252).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (room.isGuestReady) "ГОТОВ" else "НЕ ГОТОВ",
                                color = if (room.isGuestReady) Color(0xFF00E676) else Color(0xFFFF5252),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = "Ожидание игрока...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "СЛОТ СВОБОДЕН",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── IN-ROOM CHAT (MD3 Container) ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            if (roomChat.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Чат комнаты. Общайтесь перед началом матча!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(roomChat, key = { it.id }) { msg ->
                        val isSystem = msg.senderPuid == "SYSTEM"
                        if (isSystem) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = themeColor
                                    )
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
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
            EosInputField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = when (currentLang) {
                    Language.RU -> "Сообщение в комнату..."
                    Language.UA -> "Повідомлення в кімнату..."
                    Language.KK -> "Бөлмеге хабарлама..."
                    Language.DE -> "Nachricht an Raum..."
                    Language.ZH -> "发送房间聊天消息..."
                    else -> "Room message..."
                },
                modifier = Modifier.weight(1f),
                height = 50.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                themeColor = themeColor,
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

            FilledIconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        EosManager.sendRoomChatMessage(chatInput.trim())
                        chatInput = ""
                    }
                },
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = themeColor)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── ACTION BUTTONS: READY / START GAME ──
        val (actionPrimaryInteraction, actionPrimaryModifier) = rememberBouncyInteraction()
        val (leaveInteraction, leaveModifier) = rememberBouncyInteraction()

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
                    interactionSource = actionPrimaryInteraction,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .then(actionPrimaryModifier),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (room.isGuestReady) Color(0xFFFF5252) else Color(0xFF00E676),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (room.isGuestReady) "Не готов" else "Готов",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
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
                    interactionSource = actionPrimaryInteraction,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .then(actionPrimaryModifier),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (bothReady) Color(0xFF00E676) else themeColor,
                        contentColor = if (bothReady) Color.Black else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (room.guestPuid == null) "Ожидание второго игрока..." else if (bothReady) "Начать матч!" else "Начать матч",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
            }

            FilledTonalButton(
                onClick = {
                    viewModel.triggerAudioFeedback("click")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLeave()
                },
                interactionSource = leaveInteraction,
                modifier = Modifier
                    .weight(0.5f)
                    .height(50.dp)
                    .then(leaveModifier),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Выйти",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Modern MD3 Dialog to create a custom EOS room.
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
    val requestedBet = betAmount.toIntOrNull() ?: 0
    val isOverBudget = requestedBet > localCredits

    val quickBets = listOf(0, 50, 100, 250, 500)

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
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Название комнаты") },
                    trailingIcon = {
                        IconButton(onClick = { roomName = "EOS Room #${(100..999).random()}" }) {
                            Icon(Icons.Default.Casino, contentDescription = "Randomize", tint = themeColor)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                TextField(
                    value = betAmount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) betAmount = it },
                    label = { Text("Ставка (монеты)") },
                    leadingIcon = {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700))
                    },
                    isError = isOverBudget,
                    supportingText = if (isOverBudget) {
                        { Text("Максимум: $localCredits монет", color = MaterialTheme.colorScheme.error) }
                    } else {
                        { Text("Доступно: $localCredits монет") }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                // Quick Bet Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickBets.forEach { b ->
                        val isSelected = requestedBet == b
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { betAmount = "$b" }
                        ) {
                            Text(
                                text = if (b == 0) "0" else "$b",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Приватная (только по коду)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bet = requestedBet.coerceIn(0, localCredits)
                    onCreate(roomName.ifBlank { "EOS Room" }, bet, isPrivate)
                },
                enabled = !isOverBudget,
                shape = RoundedCornerShape(14.dp),
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

