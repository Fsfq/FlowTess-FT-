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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.Colors
import com.example.game.GameEngine
import com.example.game.GameMode
import com.example.game.GameState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGameScreen(
    viewModel: MainViewModel,
    onBackToLobby: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val themeColorKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeColor = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current

    val gameState by viewModel.gameEngine.gameState.collectAsStateWithLifecycle()
    val room by viewModel.lobbyManager.currentRoom.collectAsStateWithLifecycle()

    val customAvatarEmoji by viewModel.customAvatarEmoji.collectAsStateWithLifecycle()
    val customAvatarBgColor by viewModel.customAvatarBgColor.collectAsStateWithLifecycle()
    val equippedAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val hasNicknameGradient by viewModel.hasNicknameGradient.collectAsStateWithLifecycle()
    val onlineRating by viewModel.onlineRating.collectAsStateWithLifecycle()
    val winStreak by viewModel.winStreak.collectAsStateWithLifecycle()

    val eosRoom by com.example.eos.EosManager.currentRoom.collectAsStateWithLifecycle()
    val isEosMode = (eosRoom != null)
    val eosLocalPuid by com.example.eos.EosManager.localPuid.collectAsStateWithLifecycle()
    val eosOpponentScore by com.example.eos.EosManager.opponentScore.collectAsStateWithLifecycle()
    val eosOpponentLines by com.example.eos.EosManager.opponentLines.collectAsStateWithLifecycle()
    val eosPendingGarbage by com.example.eos.EosManager.pendingGarbage.collectAsStateWithLifecycle()

    val localUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isHost = if (isEosMode) (eosRoom?.hostPuid == eosLocalPuid) else (room?.hostId == localUid)
    val opponent = room?.players?.find { it.uid.isNotEmpty() && it.uid != localUid }

    val liveHostState by viewModel.lobbyManager.liveHostBattleState.collectAsStateWithLifecycle()
    val liveOpponentState by viewModel.lobbyManager.liveOpponentBattleState.collectAsStateWithLifecycle()

    val myLiveState = if (isHost) liveHostState else liveOpponentState
    val oppLiveState = if (isHost) liveOpponentState else liveHostState

    val opponentScore = if (isEosMode) eosOpponentScore else oppLiveState.score
    val opponentLines = if (isEosMode) eosOpponentLines else oppLiveState.lines
    val opponentCombo = if (isEosMode) 0 else oppLiveState.combo
    val opponentGameOver = if (isEosMode) (eosRoom?.status == "finished") else oppLiveState.isGameOver
    val opponentGrid = remember(oppLiveState.grid) { convertFlatListToGrid(oppLiveState.grid) }

    val infiniteTransition = rememberInfiniteTransition(label = "BorderPulse")
    val pulseBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderPulseAlpha"
    )

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var matchFinishedAwarded by remember { mutableStateOf(false) }
    var showMatchFinishedDialog by remember { mutableStateOf(false) }
    var cachedWinnerId by remember { mutableStateOf<String?>(null) }
    var cachedOpponentLeft by remember { mutableStateOf(false) }
    var attackBannerText by remember { mutableStateOf<String?>(null) }

    // Start Game on entry or when round increases (only when room is active and match not finished)
    LaunchedEffect(room?.currentRound, eosRoom?.status) {
        val currentR = room?.currentRound
        if (currentR != null && currentR > 0 && (!isEosMode || eosRoom?.status == "playing") && !matchFinishedAwarded && !showMatchFinishedDialog) {
            viewModel.startGame(GameMode.CLASSIC)
        }
    }

    LaunchedEffect(gameState.score, gameState.lines) {
        if (isEosMode) {
            com.example.eos.EosManager.sendGameMove(gameState.score, gameState.lines)
        }
    }

    LaunchedEffect(gameState.isGameOver) {
        if (isEosMode && gameState.isGameOver) {
            com.example.eos.EosManager.sendGameOver(gameState.score)
        }
    }

    LaunchedEffect(eosPendingGarbage) {
        if (isEosMode && eosPendingGarbage > 0) {
            val lines = com.example.eos.EosManager.consumePendingGarbage()
            if (lines > 0) {
                viewModel.gameEngine.addGarbageLines(lines)
                viewModel.triggerAudioFeedback("fall")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 1. Rock-Solid Realtime State Sync to RTDB (100ms throttle + 20-row full height)
    // ─────────────────────────────────────────────────────────────
    LaunchedEffect(room?.roomId) {
        while (true) {
            delay(100)
            if (room != null && room?.status == "playing") {
                val flatGrid = getDisplayGridWithActivePiece(gameState)
                viewModel.lobbyManager.updatePlayerLiveState(
                    grid = flatGrid,
                    score = gameState.score,
                    lines = gameState.lines,
                    combo = gameState.tetrisesCleared,
                    isGameOver = gameState.isGameOver
                )
            }
        }
    }

    // Immediate dispatch on line clears, piece movements, rotations, drops, or game over
    LaunchedEffect(gameState.score, gameState.lines, gameState.isGameOver, gameState.currentPos, gameState.currentPiece) {
        if (room != null && room?.status == "playing") {
            val flatGrid = getDisplayGridWithActivePiece(gameState)
            viewModel.lobbyManager.updatePlayerLiveState(
                grid = flatGrid,
                score = gameState.score,
                lines = gameState.lines,
                combo = gameState.tetrisesCleared,
                isGameOver = gameState.isGameOver
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Opponent Disconnect / Forfeit Win Detection
    // ─────────────────────────────────────────────────────────────
    LaunchedEffect(room?.players, room?.status) {
        if (room != null && room?.status == "playing") {
            val remainingPlayers = room?.players ?: emptyList()
            if (remainingPlayers.size == 1 && remainingPlayers.first().uid == localUid) {
                // Opponent has left the game! Award forfeit win to remaining player
                viewModel.lobbyManager.setWinner(localUid)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 3. Attack Detection (Send Garbage on Line Clears only if enabled)
    // ─────────────────────────────────────────────────────────────
    var prevLines by remember { mutableIntStateOf(0) }
    LaunchedEffect(gameState.lines) {
        val cleared = gameState.lines - prevLines
        val isAttacksEnabled = isEosMode || ((room?.garbageIntensity ?: 1.0f) > 0f && room?.gameMode != "SCORE_RACE")
        if (cleared > 0 && prevLines > 0 && isAttacksEnabled) {
            val garbageToSend = when (cleared) {
                2 -> 1
                3 -> 2
                4 -> 4
                else -> 0
            }
            if (garbageToSend > 0) {
                if (isEosMode) {
                    com.example.eos.EosManager.sendGarbageLines(garbageToSend)
                } else {
                    viewModel.lobbyManager.sendGarbageToOpponent(garbageToSend)
                }
                attackBannerText = if (cleared == 4) "💥 TETRIS ATTACK! +4 💣" else "+$garbageToSend 💣"
            }
        }
        prevLines = gameState.lines
    }

    LaunchedEffect(attackBannerText) {
        if (attackBannerText != null) {
            delay(1800)
            attackBannerText = null
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 4. Incoming Garbage Processing
    // ─────────────────────────────────────────────────────────────
    val incomingGarbage = myLiveState.garbageToSend
    LaunchedEffect(incomingGarbage) {
        val isAttacksEnabled = (room?.garbageIntensity ?: 1.0f) > 0f && room?.gameMode != "SCORE_RACE"
        if (incomingGarbage > 0 && isAttacksEnabled) {
            viewModel.gameEngine.addGarbageLines(incomingGarbage)
            viewModel.triggerAudioFeedback("fall")
            viewModel.lobbyManager.clearGarbageReceived(isHost)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 5. Round & Game Over Evaluation (Knockout vs Score Race)
    // ─────────────────────────────────────────────────────────────
    var roundWinEvaluated by remember { mutableStateOf(false) }
    LaunchedEffect(room?.currentRound) {
        roundWinEvaluated = false
    }

    LaunchedEffect(gameState.isGameOver, opponentGameOver) {
        if (room != null && room?.status == "playing" && !roundWinEvaluated) {
            val isScoreRace = room?.gameMode == "SCORE_RACE"

            if (isScoreRace) {
                // In Score Race mode: highest score wins when both top out
                if (gameState.isGameOver && opponentGameOver) {
                    roundWinEvaluated = true
                    val winnerId = when {
                        gameState.score > opponentScore -> localUid
                        opponentScore > gameState.score -> opponent?.uid ?: "draw"
                        else -> "draw"
                    }
                    viewModel.lobbyManager.recordRoundWin(winnerId)
                }
            } else {
                // In Battle / Knockout mode: when both top out, compare score; when one tops out, other wins
                if (gameState.isGameOver && opponentGameOver) {
                    roundWinEvaluated = true
                    val winnerId = when {
                        gameState.score > opponentScore -> localUid
                        opponentScore > gameState.score -> opponent?.uid ?: "draw"
                        else -> "draw"
                    }
                    viewModel.lobbyManager.recordRoundWin(winnerId)
                } else if (gameState.isGameOver && !opponentGameOver) {
                    roundWinEvaluated = true
                    opponent?.uid?.let { viewModel.lobbyManager.recordRoundWin(it) }
                } else if (!gameState.isGameOver && opponentGameOver) {
                    roundWinEvaluated = true
                    viewModel.lobbyManager.recordRoundWin(localUid)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 6. Award Credits on Match Finished
    // ─────────────────────────────────────────────────────────────
    LaunchedEffect(room?.status, room?.winnerId) {
        if (room?.status == "finished" && !matchFinishedAwarded && !room?.winnerId.isNullOrEmpty()) {
            matchFinishedAwarded = true
            cachedWinnerId = room?.winnerId
            cachedOpponentLeft = (room?.players?.size ?: 0) < 2
            showMatchFinishedDialog = true
            val isWinner = room!!.winnerId == localUid
            val isDraw = room!!.winnerId == "draw"
            viewModel.awardMultiplayerCredits(
                playerScore = gameState.score,
                opponentScore = opponentScore,
                won = isWinner,
                isDraw = isDraw
            )
            viewModel.triggerAudioFeedback(if (isWinner) "success" else "gameover")
        }
    }

    // Handle sudden room closure or opponent leaving
    LaunchedEffect(room, matchFinishedAwarded) {
        if (room == null) {
            if (!matchFinishedAwarded && !showMatchFinishedDialog) {
                onBackToLobby()
            } else {
                cachedOpponentLeft = true
            }
        } else if (matchFinishedAwarded && (room?.players?.size ?: 0) < 2) {
            cachedOpponentLeft = true
        }
    }

    BackHandler {
        if (showMatchFinishedDialog) {
            showMatchFinishedDialog = false
            viewModel.lobbyManager.leaveRoom()
            com.example.eos.EosManager.leaveRoom()
            onBackToLobby()
        } else {
            showExitConfirmDialog = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val modeLabel = if (room?.gameMode == "SCORE_RACE") {
                                when (currentLang) {
                                    Language.RU -> "НА ОЧКИ"
                                    Language.UA -> "НА ОЧКИ"
                                    Language.KK -> "ҰПАЙҒА"
                                    Language.DE -> "PUNKTEJAGD"
                                    Language.ZH -> "竞速积分赛"
                                    else -> "SCORE RACE"
                                }
                            } else {
                                when (currentLang) {
                                    Language.RU -> "РАУНД ${room?.currentRound ?: 1}/${room?.roundTarget ?: 1}"
                                    Language.UA -> "РАУНД ${room?.currentRound ?: 1}/${room?.roundTarget ?: 1}"
                                    Language.KK -> "РАУНД ${room?.currentRound ?: 1}/${room?.roundTarget ?: 1}"
                                    Language.DE -> "RUNDE ${room?.currentRound ?: 1}/${room?.roundTarget ?: 1}"
                                    Language.ZH -> "回合 ${room?.currentRound ?: 1}/${room?.roundTarget ?: 1}"
                                    else -> "ROUND ${room?.currentRound ?: 1}/${room?.roundTarget ?: 1}"
                                }
                            }
                            Text(
                                text = modeLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Black,
                                color = themeColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        IconButton(
                            onClick = { showExitConfirmDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Surrender",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
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
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ─────────────────────────────────────────────────────────────
                // VERSUS HEADER CARD (MD3 Perfectionism)
                // ─────────────────────────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: You
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            PlayerAvatarView(
                                playerName = playerName,
                                avatarEmoji = customAvatarEmoji,
                                avatarBgColorHex = customAvatarBgColor,
                                avatarFrame = equippedAvatarFrame,
                                size = 38.dp,
                                themeColor = themeColor
                            )
                            Column(horizontalAlignment = Alignment.Start) {
                                val myNickBrush = if (hasNicknameGradient) rememberAnimatedNicknameBrush(baseColor = themeColor) else null
                                Text(
                                    text = playerName.uppercase(),
                                    style = if (myNickBrush != null) {
                                        MaterialTheme.typography.labelSmall.copy(brush = myNickBrush, fontWeight = FontWeight.Black)
                                    } else {
                                        MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${gameState.score}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = themeColor
                                )
                            }
                        }

                        // Center: VS + Round Tracker
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "VS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            // Round Dots Indicator: e.g. [ ● ○ ] vs [ ○ ○ ]
                            val myWins = if (isHost) room?.hostWins ?: 0 else room?.opponentWins ?: 0
                            val oppWins = if (isHost) room?.opponentWins ?: 0 else room?.hostWins ?: 0
                            val targetWins = ((room?.roundTarget ?: 1) / 2) + 1

                            if (room?.roundTarget ?: 1 > 1) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    for (w in 1..targetWins) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (w <= myWins) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(3.dp))
                                    for (w in 1..targetWins) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (w <= oppWins) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                            }
                        }

                        // Right: Opponent
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val oppColor = parseHexColor(opponent?.avatarBgColor ?: "", MaterialTheme.colorScheme.error)
                            val oppNickBrush = if (opponent?.hasGradient == true) rememberAnimatedNicknameBrush(baseColor = oppColor) else null

                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                val defaultOpponentLabel = when (currentLang) {
                                    Language.RU -> "СОПЕРНИК"
                                    Language.UA -> "СУПЕРНИК"
                                    Language.KK -> "ҚАРСЫЛАС"
                                    Language.DE -> "GEGNER"
                                    Language.ZH -> "对手"
                                    else -> "OPPONENT"
                                }
                                val oppDisplayName = if (isEosMode) {
                                    if (isHost) (eosRoom?.guestName ?: "Guest") else (eosRoom?.hostName ?: "Host")
                                } else {
                                    opponent?.name ?: defaultOpponentLabel
                                }
                                Text(
                                    text = oppDisplayName.uppercase(),
                                    style = if (oppNickBrush != null) {
                                        MaterialTheme.typography.labelSmall.copy(brush = oppNickBrush, fontWeight = FontWeight.Black)
                                    } else {
                                        MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$opponentScore",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            PlayerAvatarView(
                                playerName = opponent?.name ?: "?",
                                avatarEmoji = opponent?.avatarEmoji ?: "",
                                avatarBgColorHex = opponent?.avatarBgColor ?: "",
                                avatarFrame = opponent?.avatarFrame ?: "standard",
                                avatarBase64 = opponent?.avatarBase64 ?: "",
                                size = 38.dp,
                                themeColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // ─────────────────────────────────────────────────────────────
                // MAIN BATTLE ARENA (Board + Live Opponent Feed)
                // ─────────────────────────────────────────────────────────────
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val totalH = maxHeight
                    val totalW = maxWidth
                    val sideW = (totalW * 0.36f).coerceIn(85.dp, 130.dp)
                    val maxBoardW = (totalW - sideW - 10.dp).coerceAtLeast(80.dp)
                    val maxBoardH = (totalH - 4.dp).coerceAtLeast(160.dp)
                    val boardH = minOf(maxBoardH, maxBoardW * 2f)
                    val boardW = boardH * 0.5f

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Local Player Game Board
                        Box(
                            modifier = Modifier
                                .size(width = boardW, height = boardH)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF08080C))
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        themeColor.copy(alpha = pulseBorderAlpha),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = pulseBorderAlpha),
                                        themeColor.copy(alpha = pulseBorderAlpha)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameBoardView(
                            gameState = gameState,
                            blockStyle = viewModel.blockStyle.collectAsStateWithLifecycle().value,
                            ghostVisible = viewModel.ghostVisible.collectAsStateWithLifecycle().value,
                            smoothFallingEnabled = viewModel.smoothFallingEnabled.collectAsStateWithLifecycle().value,
                            gridLineDensity = viewModel.gridLineDensity.collectAsStateWithLifecycle().value,
                            boardColorSkin = viewModel.boardColorSkin.collectAsStateWithLifecycle().value,
                            ghostOutlineOnly = viewModel.ghostOutlineOnly.collectAsStateWithLifecycle().value,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Incoming Garbage Attack Warning Gauge
                        if (incomingGarbage > 0) {
                            val gaugeColor = when {
                                incomingGarbage <= 2 -> Color(0xFF00E676)
                                incomingGarbage <= 4 -> Color(0xFFFFB300)
                                else -> Color(0xFFFF1744)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .align(Alignment.CenterStart)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction = (incomingGarbage / 10f).coerceAtMost(1f))
                                        .align(Alignment.BottomCenter)
                                        .background(gaugeColor)
                                )
                            }
                        }

                        // Local Game Over Waiting Overlay
                        if (gameState.isGameOver) {
                            val toppedOutMsg = when (currentLang) {
                                Language.RU -> "ФИНИШ\nЖДЁМ СОПЕРНИКА"
                                Language.UA -> "ФІНІШ\nЧЕКАЄМО СУПЕРНИКА"
                                Language.KK -> "МӘРЕ\nҚАРСЫЛАСТЫ КҮТУДЕ"
                                Language.DE -> "AUSGESCHIEDEN\nWARTE AUF GEGNER"
                                Language.ZH -> "封顶出局\n正在等待对手完成"
                                else -> "TOPPED OUT\nWAITING FOR OPPONENT"
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.82f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = toppedOutMsg,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Tetris Attack Pop-up Banner
                        androidx.compose.animation.AnimatedVisibility(
                            visible = attackBannerText != null,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColor,
                                shadowElevation = 6.dp
                            ) {
                                Text(
                                    text = attackBannerText ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Right Side Column: Next Piece + Opponent Live Board (Full 20 rows)
                    Column(
                        modifier = Modifier
                            .width(sideW)
                            .height(boardH),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Next Piece Box
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Translations.get("next", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = themeColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (gameState.nextPieces.isNotEmpty()) {
                                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                        PreviewNextPiece(
                                            piece = gameState.nextPieces.first(),
                                            style = viewModel.blockStyle.collectAsStateWithLifecycle().value
                                        )
                                    }
                                }
                            }
                        }

                        // Opponent Live Mini Board (Full 20 rows from top to bottom)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            val miniOpponentTitle = opponent?.name ?: when (currentLang) {
                                Language.RU -> "СОПЕРНИК"
                                Language.UA -> "СУПЕРНИК"
                                Language.KK -> "ҚАРСЫЛАС"
                                Language.DE -> "GEGNER"
                                Language.ZH -> "对手"
                                else -> "OPPONENT"
                            }
                            MiniBoard(
                                grid = opponentGrid,
                                title = miniOpponentTitle,
                                score = opponentScore,
                                combo = opponentCombo,
                                lines = opponentLines
                            )

                            if (opponentGameOver) {
                                val opponentOutMsg = when (currentLang) {
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
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black.copy(alpha = 0.8f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opponentOutMsg,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

                // ─────────────────────────────────────────────────────────────
                // TOUCH CONTROLS
                // ─────────────────────────────────────────────────────────────
                val controlStyle by viewModel.controlStyle.collectAsStateWithLifecycle()
                val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()
                val controlVerticalPosition by viewModel.controlVerticalPosition.collectAsStateWithLifecycle()
                val controlButtonScale by viewModel.controlButtonScale.collectAsStateWithLifecycle()
                val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()

                if (!gameState.isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (controlVerticalPosition == "middle") 36.dp else 4.dp),
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

            // ─────────────────────────────────────────────────────────────
            // MATCH FINISHED MODAL (MD3 Perfectionism with Forfeit support)
            // ─────────────────────────────────────────────────────────────
            if (showMatchFinishedDialog) {
                val effectiveWinnerId = room?.winnerId ?: cachedWinnerId
                val isWinner = effectiveWinnerId == localUid
                val isDraw = effectiveWinnerId == "draw"
                val wasForfeit = cachedOpponentLeft || (room?.players?.size ?: 0) < 2

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isWinner) Color(0xFFFFD700).copy(alpha = 0.18f) else MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isWinner) Icons.Default.EmojiEvents else if (isDraw) Icons.Default.Handshake else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isWinner) Color(0xFFFFD700) else if (isDraw) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isWinner) {
                                        when (currentLang) {
                                            Language.RU -> "ПОБЕДА В МАТЧЕ!"
                                            Language.UA -> "ПЕРЕМОГА В МАТЧІ!"
                                            Language.KK -> "МАТЧТАҒЫ ЖЕҢІС!"
                                            Language.DE -> "SIEG IM MATCH!"
                                            Language.ZH -> "对决胜利！"
                                            else -> "VICTORY!"
                                        }
                                    } else if (isDraw) {
                                        when (currentLang) {
                                            Language.RU -> "НИЧЬЯ!"
                                            Language.UA -> "НІЧИЯ!"
                                            Language.KK -> "ТЕҢ ОЙЫН!"
                                            Language.DE -> "UNENTSCHIEDEN!"
                                            Language.ZH -> "平局！"
                                            else -> "DRAW!"
                                        }
                                    } else {
                                        when (currentLang) {
                                            Language.RU -> "ПОРАЖЕНИЕ"
                                            Language.UA -> "ПОРАЗКА"
                                            Language.KK -> "ЖЕҢІЛІС"
                                            Language.DE -> "NIEDERLAGE"
                                            Language.ZH -> "战败"
                                            else -> "DEFEAT"
                                        }
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isWinner) Color(0xFFFFD700) else if (isDraw) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                 )
                                 if (wasForfeit && isWinner) {
                                     Text(
                                         text = when (currentLang) {
                                             Language.RU -> "Соперник покинул матч"
                                             Language.UA -> "Супротивник залишив матч"
                                             Language.KK -> "Қарсылас ойыннан шығып кетті"
                                             Language.DE -> "Gegner hat aufgegeben"
                                             Language.ZH -> "对手已提前退出认输"
                                             else -> "Opponent forfeited the match"
                                         },
                                         style = MaterialTheme.typography.bodySmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant
                                     )
                                 }
                            }

                            // Stats comparison summary
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val ptsWord = when (currentLang) {
                                            Language.RU -> "очков"
                                            Language.UA -> "очок"
                                            Language.KK -> "ұпай"
                                            Language.DE -> "Pkt"
                                            Language.ZH -> "分"
                                            else -> "pts"
                                        }
                                        val lnsWord = when (currentLang) {
                                            Language.RU -> "линий"
                                            Language.UA -> "ліній"
                                            Language.KK -> "жол"
                                            Language.DE -> "Linien"
                                            Language.ZH -> "行"
                                            else -> "lines"
                                        }
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(text = playerName, style = MaterialTheme.typography.labelSmall, color = themeColor, fontWeight = FontWeight.Bold)
                                            Text(text = "${gameState.score} $ptsWord", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                            Text(text = "${gameState.lines} $lnsWord", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(text = "VS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = opponent?.name ?: Translations.get("player_opponent", currentLang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            Text(text = "$opponentScore $ptsWord", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                            Text(text = "$opponentLines $lnsWord", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                    // Match Rewards & ELO
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isWinner) Color(0xFF00E676).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (isWinner) "+25 ELO 📈" else if (isDraw) "±0 ELO" else "-15 ELO 📉",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (isWinner) Color(0xFF00E676) else if (isDraw) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    showMatchFinishedDialog = false
                                    viewModel.lobbyManager.leaveRoom()
                                    com.example.eos.EosManager.leaveRoom()
                                    onBackToLobby()
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ВЕРНУТЬСЯ В ЛОББИ"
                                        Language.UA -> "ПОВЕРНУТИСЯ В ЛОБІ"
                                        Language.KK -> "ЛОББИГЕ ОРАЛУ"
                                        Language.DE -> "ZURÜCK ZUR LOBBY"
                                        Language.ZH -> "返回对战大厅"
                                        else -> "RETURN TO LOBBY"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // SURRENDER CONFIRMATION DIALOG
            // ─────────────────────────────────────────────────────────────
            if (showExitConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showExitConfirmDialog = false },
                    title = {
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Сдаться и выйти?"
                                Language.UA -> "Здатися та вийти?"
                                Language.KK -> "Беріліп, шығу керек пе?"
                                Language.DE -> "Aufgeben und verlassen?"
                                Language.ZH -> "确定投降认输并退出？"
                                else -> "Surrender match?"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Выход из комнаты во время матча будет засчитан как техническое поражение."
                                Language.UA -> "Вихід з кімнати під час матчу буде зараховано як технічну поразку."
                                Language.KK -> "Матч кезінде бөлмеден шығу техникалық жеңіліс болып саналады."
                                Language.DE -> "Das Verlassen während des Spiels wird als Niederlage gewertet."
                                Language.ZH -> "对决进行中离开房间将判定为弃权落败。"
                                else -> "Leaving during an active match will count as a forfeit defeat."
                            }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitConfirmDialog = false
                                viewModel.lobbyManager.leaveRoom()
                                com.example.eos.EosManager.leaveRoom()
                                onBackToLobby()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Translations.get("surrender", currentLang), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showExitConfirmDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Translations.get("cancel", currentLang))
                        }
                    }
                )
            }
        }
    }
}

// Prepare 20-row visible grid including falling active piece so opponent sees full tower in real time!
private fun getDisplayGridWithActivePiece(gameState: GameState): List<Int> {
    // 20 visible rows out of 22 total (rows 2..21)
    val matrix = gameState.grid.takeLast(20).map { it.clone() }
    val piece = gameState.currentPiece
    val pos = gameState.currentPos
    if (piece != null && !gameState.isGameOver) {
        val startRowOffset = 2
        for (p in piece.shape) {
            val r = pos.y + p.y
            val c = pos.x + p.x
            val localRow = r - startRowOffset
            if (localRow in 0 until 20 && c in 0 until 10) {
                matrix[localRow][c] = piece.colorIndex
            }
        }
    }
    return matrix.flatMap { row -> row.toList() }
}

// Flat list deserializer helper (20x10 full height)
private fun convertFlatListToGrid(flatList: List<Int>): List<IntArray> {
    val grid = mutableListOf<IntArray>()
    for (r in 0 until 20) {
        val row = IntArray(10)
        for (c in 0 until 10) {
            val idx = r * 10 + c
            row[c] = if (idx < flatList.size) flatList[idx] else 0
        }
        grid.add(row)
    }
    return grid
}

@Composable
fun MiniBoard(
    grid: List<IntArray>,
    title: String,
    score: Int,
    combo: Int = 0,
    lines: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF0D0E15),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(10f / 20f)
            ) {
                val cellWidth = size.width / 10f
                val cellHeight = size.height / 20f
                val cellGap = 1.dp.toPx()

                for (r in 0 until minOf(20, grid.size)) {
                    val row = grid[r]
                    for (c in 0 until minOf(10, row.size)) {
                        val colorIdx = row[c]
                        val cellColor = if (colorIdx != 0) {
                            Colors.getOrElse(colorIdx) { Color(0xFF00FFCC) }
                        } else {
                            Color(0xFF14151F).copy(alpha = 0.5f)
                        }

                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(c * cellWidth + cellGap / 2f, r * cellHeight + cellGap / 2f),
                            size = Size(cellWidth - cellGap, cellHeight - cellGap),
                            cornerRadius = CornerRadius(1.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
