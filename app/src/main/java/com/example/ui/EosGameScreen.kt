package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.eos.*
import com.example.game.GameMode
import com.example.game.GameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EosGameScreen(
    viewModel: MainViewModel,
    onBackToLobby: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val themeColor = MaterialTheme.colorScheme.primary

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthDp = configuration.screenWidthDp
    val isCompactScreen = screenHeightDp < 680
    val isUltraCompact = screenHeightDp < 600

    val localBattleState by EosBattleEngine.localState.collectAsStateWithLifecycle()
    val oppBattleState by EosBattleEngine.opponentState.collectAsStateWithLifecycle()
    val matchState by EosBattleEngine.matchState.collectAsStateWithLifecycle()
    val pingMs by EosManager.p2pPingMs.collectAsStateWithLifecycle()
    val currentRoom by EosManager.currentRoom.collectAsStateWithLifecycle()

    val customAvatarEmoji by viewModel.customAvatarEmoji.collectAsStateWithLifecycle()
    val customAvatarBgColor by viewModel.customAvatarBgColor.collectAsStateWithLifecycle()
    val equippedAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val hasNicknameGradient by viewModel.hasNicknameGradient.collectAsStateWithLifecycle()
    val localPuid by EosManager.localPuid.collectAsStateWithLifecycle()
    val localTier by EosManager.localPlayerTier.collectAsStateWithLifecycle()

    // Unified Control Settings from MainViewModel
    val controlStyle by viewModel.controlStyle.collectAsStateWithLifecycle()
    val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()
    val controlVerticalPosition by viewModel.controlVerticalPosition.collectAsStateWithLifecycle()
    val controlButtonScale by viewModel.controlButtonScale.collectAsStateWithLifecycle()
    val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }

    // Map local battle state to GameState adapter for GameBoardView and GameControlsSection
    val localGameState = remember(localBattleState, matchState.currentRound) {
        GameState(
            grid = localBattleState.grid,
            currentPiece = localBattleState.currentPiece,
            currentPos = localBattleState.currentPos,
            holdPiece = localBattleState.holdPiece,
            nextPieces = localBattleState.nextPieces,
            score = localBattleState.score,
            lines = localBattleState.lines,
            level = matchState.currentRound,
            isGameOver = localBattleState.isGameOver,
            hasHeldThisTurn = localBattleState.hasHeldThisTurn,
            gameMode = GameMode.CLASSIC
        )
    }

    // Connect isPlaying state in MainViewModel so ControlButton enables gestures and full alpha
    LaunchedEffect(matchState.status) {
        viewModel.setPlaying(matchState.status == EosBattleStatus.PLAYING)
    }

    // Audio & Reward Integration
    LaunchedEffect(Unit) {
        EosBattleEngine.onAudioTrigger = { soundKey ->
            viewModel.triggerAudioFeedback(soundKey)
        }
        EosBattleEngine.onMatchCompleted = { isWinner, localScore, oppScore, bet ->
            viewModel.awardMultiplayerCredits(localScore, oppScore, isWinner, false)
            if (isWinner && bet > 0) {
                viewModel.addRawCredits(bet * 2)
            }
        }
        EosBattleEngine.onRematchStarted = { bet ->
            if (viewModel.credits.value >= bet) {
                viewModel.spendCredits(bet)
                true
            } else {
                false
            }
        }

        val room = currentRoom
        if (room != null) {
            if (room.bet > 0) {
                viewModel.spendCredits(room.bet)
            }
            EosBattleEngine.initializeMatch(
                room = room,
                myPuid = localPuid ?: "",
                myName = playerName,
                myTier = localTier
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setPlaying(false)
            EosBattleEngine.stopMatch()
        }
    }

    fun handleExit() {
        viewModel.setPlaying(false)
        EosBattleEngine.stopMatch()
        EosManager.leaveRoom()
        onBackToLobby()
    }

    BackHandler {
        if (matchState.status == EosBattleStatus.MATCH_FINISHED) {
            handleExit()
        } else {
            showExitDialog = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.heightIn(max = if (isCompactScreen) 42.dp else 48.dp),
                title = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val roundLabel = when (currentLang) {
                                Language.RU -> "РАУНД ${matchState.currentRound}/3"
                                Language.UA -> "РАУНД ${matchState.currentRound}/3"
                                Language.KK -> "РАУНД ${matchState.currentRound}/3"
                                Language.DE -> "RUNDE ${matchState.currentRound}/3"
                                Language.ZH -> "回合 ${matchState.currentRound}/3"
                                else -> "ROUND ${matchState.currentRound}/3"
                            }
                            Text(
                                text = "EOS • $roundLabel",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = if (isCompactScreen) 11.sp else 12.sp),
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (matchState.status == EosBattleStatus.MATCH_FINISHED) {
                                    handleExit()
                                } else {
                                    showExitDialog = true
                                }
                            },
                            modifier = Modifier.size(if (isCompactScreen) 32.dp else 36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val pingDotColor = when {
                                pingMs <= 60 -> Color(0xFF00E676)
                                pingMs <= 140 -> Color(0xFFFFB300)
                                else -> MaterialTheme.colorScheme.error
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(pingDotColor)
                            )
                            Text(
                                text = "${pingMs}ms",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                .padding(padding)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ─────────────────────────────────────────────────────────────
                // 1. VERSUS CARD (Adaptive MD3 ElevatedCard)
                // ─────────────────────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (isCompactScreen) 1.dp else 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = if (isCompactScreen) 4.dp else 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Local Player
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            PlayerAvatarView(
                                playerName = playerName,
                                avatarEmoji = customAvatarEmoji,
                                avatarBgColorHex = customAvatarBgColor,
                                avatarFrame = equippedAvatarFrame,
                                size = if (isCompactScreen) 30.dp else 36.dp,
                                themeColor = themeColor
                            )
                            Column(horizontalAlignment = Alignment.Start) {
                                val myNickBrush = if (hasNicknameGradient) rememberAnimatedNicknameBrush(baseColor = themeColor) else null
                                Text(
                                    text = playerName,
                                    style = if (myNickBrush != null) {
                                        MaterialTheme.typography.labelSmall.copy(brush = myNickBrush, fontWeight = FontWeight.Bold, fontSize = if (isCompactScreen) 10.sp else 11.sp)
                                    } else {
                                        MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = if (isCompactScreen) 10.sp else 11.sp)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${localBattleState.score}",
                                    style = if (isCompactScreen) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = themeColor
                                )
                            }
                        }

                        // Center: VS + Round Win Dots
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "VS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            // Round indicator dots: Best of 3 (need 2 wins)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                for (w in 1..2) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCompactScreen) 5.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (w <= matchState.localRoundWins) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(3.dp))
                                for (w in 1..2) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCompactScreen) 5.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (w <= matchState.opponentRoundWins) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }
                        }

                        // Right: Opponent
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = oppBattleState.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isCompactScreen) 10.sp else 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${oppBattleState.score}",
                                    style = if (isCompactScreen) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            PlayerAvatarView(
                                playerName = oppBattleState.name,
                                avatarEmoji = "",
                                avatarBgColorHex = "",
                                avatarFrame = "standard",
                                size = if (isCompactScreen) 30.dp else 36.dp,
                                themeColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // ─────────────────────────────────────────────────────────────
                // 2. DYNAMIC ARENA (Fits any screen height/width smoothly)
                // ─────────────────────────────────────────────────────────────
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val totalH = maxHeight
                    val totalW = maxWidth

                    // Dynamic column widths
                    val rightColW = if (totalW < 360.dp) 72.dp else 84.dp
                    val spacing = 6.dp
                    val maxBoardW = (totalW - rightColW - spacing - 8.dp).coerceAtLeast(100.dp)

                    // Board ratio is 10:20 (0.5)
                    // Height cannot exceed totalH, width cannot exceed maxBoardW
                    val boardH = minOf(totalH, maxBoardW * 2f)
                    val boardW = boardH * 0.5f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boardH),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main Board container
                        Box(
                            modifier = Modifier
                                .width(boardW)
                                .height(boardH)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GameBoardView(
                                gameState = localGameState,
                                blockStyle = viewModel.blockStyle.collectAsStateWithLifecycle().value,
                                ghostVisible = viewModel.ghostVisible.collectAsStateWithLifecycle().value,
                                smoothFallingEnabled = viewModel.smoothFallingEnabled.collectAsStateWithLifecycle().value,
                                gridLineDensity = viewModel.gridLineDensity.collectAsStateWithLifecycle().value,
                                boardColorSkin = viewModel.boardColorSkin.collectAsStateWithLifecycle().value,
                                ghostOutlineOnly = viewModel.ghostOutlineOnly.collectAsStateWithLifecycle().value,
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Incoming Garbage Gauge
                            if (localBattleState.pendingGarbage > 0) {
                                val gaugeFraction = (localBattleState.pendingGarbage / 10f).coerceAtMost(1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(4.dp)
                                        .align(Alignment.CenterStart)
                                        .background(Color.Black.copy(alpha = 0.35f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fraction = gaugeFraction)
                                            .align(Alignment.BottomCenter)
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                }
                            }

                            // Round Over / Game Over Scrim
                            if (localBattleState.isGameOver) {
                                val waitMsg = when (currentLang) {
                                    Language.RU -> "РАУНД ЗАВЕРШЁН\nЖДЁМ СОПЕРНИКА"
                                    Language.UA -> "РАУНД ЗАВЕРШЕНО\nЧЕКАЄМО СУПЕРНИКА"
                                    Language.KK -> "РАУНД АЯҚТАЛДЫ\nҚАРСЫЛАСТЫ КҮТУДЕ"
                                    Language.DE -> "RUNDE VORBEI\nWARTE AUF GEGNER"
                                    Language.ZH -> "回合结束\n等待对手"
                                    else -> "ROUND OVER\nWAITING FOR OPPONENT"
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.8f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = waitMsg,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }

                            // Attack Banner
                            androidx.compose.animation.AnimatedVisibility(
                                visible = localBattleState.attackBanner != null,
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                    shadowElevation = 3.dp
                                ) {
                                    Text(
                                        text = localBattleState.attackBanner ?: "",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(spacing))

                        // Right Side Column: Next + Hold + Opponent Mini Board
                        Column(
                            modifier = Modifier
                                .width(rightColW)
                                .height(boardH),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Next Piece Box
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = if (isCompactScreen) 2.dp else 4.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val nextLabel = when (currentLang) {
                                        Language.RU -> "ДАЛЕЕ"
                                        Language.UA -> "ДАЛІ"
                                        Language.KK -> "КЕЛЕСІ"
                                        Language.DE -> "NÄCHSTE"
                                        Language.ZH -> "下一个"
                                        else -> "NEXT"
                                    }
                                    Text(
                                        text = nextLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )
                                    val previewBoxSize = if (isUltraCompact) 22.dp else if (isCompactScreen) 26.dp else 30.dp
                                    if (localBattleState.nextPieces.isNotEmpty()) {
                                        Box(modifier = Modifier.size(previewBoxSize), contentAlignment = Alignment.Center) {
                                            PreviewNextPiece(
                                                piece = localBattleState.nextPieces.first(),
                                                style = viewModel.blockStyle.collectAsStateWithLifecycle().value
                                            )
                                        }
                                    }
                                }
                            }

                            // Hold Piece Box
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        EosBattleEngine.hold()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = if (isCompactScreen) 2.dp else 4.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val holdLabel = when (currentLang) {
                                        Language.RU -> "ЗАПАС"
                                        Language.UA -> "ЗАПАС"
                                        Language.KK -> "ҚОР"
                                        Language.DE -> "HALTEN"
                                        Language.ZH -> "暂存"
                                        else -> "HOLD"
                                    }
                                    Text(
                                        text = holdLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = themeColor
                                    )
                                    val previewBoxSize = if (isUltraCompact) 22.dp else if (isCompactScreen) 26.dp else 30.dp
                                    Box(modifier = Modifier.size(previewBoxSize), contentAlignment = Alignment.Center) {
                                        val holdP = localBattleState.holdPiece
                                        if (holdP != null) {
                                            PreviewNextPiece(
                                                piece = holdP,
                                                style = viewModel.blockStyle.collectAsStateWithLifecycle().value
                                            )
                                        } else {
                                            Text(
                                                text = "-",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }

                            // Opponent Live Mini Board
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                MiniBoard(
                                    grid = oppBattleState.grid,
                                    title = oppBattleState.name,
                                    score = oppBattleState.score,
                                    combo = oppBattleState.combo,
                                    lines = oppBattleState.lines
                                )

                                if (oppBattleState.isGameOver) {
                                    val outMsg = when (currentLang) {
                                        Language.RU -> "ВЫБЫЛ"
                                        Language.UA -> "ВИБУВ"
                                        Language.KK -> "ШЫҒЫП ҚАЛДЫ"
                                        Language.DE -> "K.O."
                                        Language.ZH -> "出局"
                                        else -> "OUT"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.8f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = outMsg,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // ─────────────────────────────────────────────────────────────
                // 3. UNIFIED ON-SCREEN TOUCH CONTROLS FROM SETTINGS
                // ─────────────────────────────────────────────────────────────
                if (!localBattleState.isGameOver) {
                    val dynamicScale = if (isUltraCompact) {
                        (controlButtonScale * 0.76f).coerceAtLeast(0.65f)
                    } else if (isCompactScreen) {
                        (controlButtonScale * 0.88f).coerceAtLeast(0.7f)
                    } else {
                        controlButtonScale
                    }
                    val dynamicBottomPadding = if (isCompactScreen) {
                        0.dp
                    } else {
                        if (controlVerticalPosition == "middle") 20.dp else 2.dp
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dynamicBottomPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        GameControlsSection(
                            viewModel = viewModel,
                            gameState = localGameState,
                            controlStyle = controlStyle,
                            leftHandedControls = leftHandedControls,
                            controlVerticalPosition = controlVerticalPosition,
                            controlButtonScale = dynamicScale,
                            controlButtonStyle = controlButtonStyle,
                            onLeftPress = { EosBattleEngine.moveLeft() },
                            onRightPress = { EosBattleEngine.moveRight() },
                            onDownPress = { EosBattleEngine.softDrop() },
                            onRotatePress = { EosBattleEngine.rotate() },
                            onHardDropPress = { EosBattleEngine.hardDrop() },
                            onHoldPress = { EosBattleEngine.hold() }
                        )
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        // 4. OVERLAYS: MINIMALIST MD3 COUNTDOWN
        // ─────────────────────────────────────────────────────────────
        if (matchState.status == EosBattleStatus.COUNTDOWN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "РАУНД ${matchState.currentRound}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${matchState.countdownSeconds}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        // 5. OVERLAYS: ROUND OVER
        // ─────────────────────────────────────────────────────────────
        if (matchState.status == EosBattleStatus.ROUND_OVER) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "РАУНД ${matchState.currentRound - 1} ЗАВЕРШЁН",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${matchState.localRoundWins} : ${matchState.opponentRoundWins}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        // 6. OVERLAYS: MATCH FINISHED DIALOG (MD3 Standards)
        // ─────────────────────────────────────────────────────────────
        if (matchState.status == EosBattleStatus.MATCH_FINISHED) {
            val isWin = (matchState.isMatchWinner == true)
            val oppLeft = matchState.opponentLeft || matchState.finishReason == "FORFEIT_WIN"

            AlertDialog(
                onDismissRequest = { /* Modal */ },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isWin) Icons.Default.EmojiEvents else Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (isWin) "ПОБЕДА!" else "ПОРАЖЕНИЕ",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (oppLeft) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isWin) "Соперник покинул бой. Досрочная победа!" else "Соперник покинул комнату.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Счёт по раундам: ${matchState.localRoundWins} - ${matchState.opponentRoundWins}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Монеты",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "+${matchState.coinsWon}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Рейтинг",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    val rDelta = matchState.ratingDelta
                                    Text(
                                        text = if (rDelta >= 0) "+$rDelta ELO" else "$rDelta ELO",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rDelta >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ваши очки: ${localBattleState.score}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Соперник: ${oppBattleState.score}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    if (oppLeft) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                handleExit()
                            }
                        ) {
                            Text("Забрать награду и выйти")
                        }
                    } else {
                        val rematchLabel = when {
                            matchState.rematchRequestedByLocal && !matchState.rematchRequestedByOpponent -> "Ожидание..."
                            !matchState.rematchRequestedByLocal && matchState.rematchRequestedByOpponent -> "Принять реванш!"
                            else -> "Реванш"
                        }
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                EosBattleEngine.requestRematch()
                            },
                            enabled = !matchState.rematchRequestedByLocal
                        ) {
                            Text(rematchLabel)
                        }
                    }
                },
                dismissButton = {
                    if (!oppLeft) {
                        OutlinedButton(
                            onClick = {
                                handleExit()
                            }
                        ) {
                            Text("В лобби")
                        }
                    }
                }
            )
        }

        // ─────────────────────────────────────────────────────────────
        // 7. EXIT / SURRENDER CONFIRMATION DIALOG
        // ─────────────────────────────────────────────────────────────
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Покинуть бой?", fontWeight = FontWeight.Bold) },
                text = { Text("Выйти в лобби? Текущий матч будет признан поражением.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            showExitDialog = false
                            EosBattleEngine.forfeitMatch()
                            handleExit()
                        }
                    ) {
                        Text("Покинуть", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showExitDialog = false }) {
                        Text("Остаться")
                    }
                }
            )
        }
    }
}
