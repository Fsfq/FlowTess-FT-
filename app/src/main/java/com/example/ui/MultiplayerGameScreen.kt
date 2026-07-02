package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.GameMode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGameScreen(
    viewModel: MainViewModel,
    onBackToLobby: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val themeColorKey by viewModel.themeColor.collectAsStateWithLifecycle()
    
    val gameState by viewModel.gameEngine.gameState.collectAsStateWithLifecycle()
    val room by viewModel.lobbyManager.currentRoom.collectAsStateWithLifecycle()
    
    val localUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val isHost = remember(room) { room?.hostId == localUid }
    val opponent = remember(room) { room?.players?.find { it.uid != localUid } }
    val opponentScore = remember(room, isHost) { if (isHost) room?.opponentScore ?: 0 else room?.hostScore ?: 0 }
    
    val themeColor = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition(label = "BorderPulse")
    val pulseBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderPulseAlpha"
    )

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var matchFinishedAwarded by remember { mutableStateOf(false) }
    
    // Start game engine on entry
    LaunchedEffect(Unit) {
        viewModel.startGame(GameMode.CLASSIC)
    }

    // 1. Sync local state to Firestore
    LaunchedEffect(gameState.grid, gameState.score, gameState.lines, gameState.isGameOver) {
        if (room != null) {
            // Flatten bottom 12 rows of the 22x10 grid to push to Firestore
            val flatGrid = gameState.grid.takeLast(12).flatMap { row -> row.toList() }
            viewModel.lobbyManager.updatePlayerGameState(flatGrid, gameState.score, gameState.lines, gameState.isGameOver)
        }
    }

    // 2. Attack detection (watch line clears)
    var prevLines by remember { mutableStateOf(0) }
    LaunchedEffect(gameState.lines) {
        val cleared = gameState.lines - prevLines
        if (cleared > 0 && prevLines > 0) {
            // Simple Tetris garbage mapping
            val garbageToSend = when (cleared) {
                2 -> 1
                3 -> 2
                4 -> 4
                else -> 0
            }
            if (garbageToSend > 0) {
                viewModel.lobbyManager.sendGarbageToOpponent(garbageToSend)
            }
        }
        prevLines = gameState.lines
    }

    // 3. Listen to incoming garbage
    val opponentGarbageCount = if (isHost) room?.opponentGarbageToSend ?: 0 else room?.hostGarbageToSend ?: 0
    LaunchedEffect(opponentGarbageCount) {
        if (opponentGarbageCount > 0) {
            viewModel.gameEngine.addGarbageLines(opponentGarbageCount)
            // Clear in db
            viewModel.lobbyManager.clearGarbageReceived(
                if (isHost) "opponentGarbageToSend" else "hostGarbageToSend"
            )
        }
    }

    // 4. Game Over evaluation
    LaunchedEffect(room?.hostGameOver, room?.opponentGameOver) {
        if (room != null && room!!.hostGameOver && room!!.opponentGameOver && room!!.winnerId.isEmpty()) {
            if (isHost) {
                // Determine winner
                val winnerId = when {
                    room!!.hostScore > room!!.opponentScore -> room!!.hostId
                    room!!.opponentScore > room!!.hostScore -> opponent?.uid ?: "draw"
                    else -> "draw"
                }
                viewModel.lobbyManager.setWinner(winnerId)
            }
        }
    }

    // 5. Award credits on finish
    LaunchedEffect(room?.status, room?.winnerId) {
        if (room?.status == "finished" && !matchFinishedAwarded && room!!.winnerId.isNotEmpty()) {
            matchFinishedAwarded = true
            val won = room!!.winnerId == localUid
            viewModel.awardMultiplayerCredits(
                playerScore = if (isHost) room!!.hostScore else room!!.opponentScore,
                opponentScore = if (isHost) room!!.opponentScore else room!!.hostScore,
                won = won
            )
            viewModel.triggerAudioFeedback(if (won) "success" else "gameover")
        }
    }

    // Handle sudden room deletion (opponent exits)
    LaunchedEffect(room) {
        if (room == null && !matchFinishedAwarded) {
            // Drop back
            onBackToLobby()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "МАТЧ СМЕРТИ" else "LIVE BATTLE ARENA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { showExitConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Surrender", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Scores header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "ВЫ" else "YOU",
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColor
                    )
                    AdaptiveText(
                        text = "${gameState.score}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                AdaptiveText(
                    text = "VS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )

                Column(horizontalAlignment = Alignment.End) {
                    AdaptiveText(
                        text = opponent?.name ?: (if (currentLang == Language.RU) "СОПЕРНИК" else "OPPONENT"),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                    AdaptiveText(
                        text = "$opponentScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Arena Boards Row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Local Game board
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    themeColor.copy(alpha = pulseBorderAlpha),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = pulseBorderAlpha),
                                    themeColor.copy(alpha = pulseBorderAlpha)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Reused GameBoardView
                    GameBoardView(
                        gameState = gameState,
                        blockStyle = viewModel.blockStyle.collectAsStateWithLifecycle().value,
                        ghostVisible = viewModel.ghostVisible.collectAsStateWithLifecycle().value,
                        smoothFallingEnabled = viewModel.smoothFallingEnabled.collectAsStateWithLifecycle().value,
                        gridLineDensity = viewModel.gridLineDensity.collectAsStateWithLifecycle().value,
                        boardColorSkin = viewModel.boardColorSkin.collectAsStateWithLifecycle().value,
                        modifier = Modifier.fillMaxSize()
                    )

                    // local game over indicator
                    if (gameState.isGameOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AdaptiveText(
                                text = if (currentLang == Language.RU) "ФИНИШ\nЖДЕМ ОППОНЕНТА" else "BOARD FULL\nWAITING FOR OPPONENT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Pending garbage visual gauge indicator
                    if (opponentGarbageCount > 0) {
                        val gaugeColor = when {
                            opponentGarbageCount <= 2 -> Color(0xFF00FF88) // green
                            opponentGarbageCount <= 5 -> Color(0xFFFFCC00) // yellow
                            else -> Color(0xFFFF3366) // red
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(6.dp)
                                .align(Alignment.CenterStart)
                                .background(Color.Black.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction = (opponentGarbageCount.toFloat() / 10f).coerceAtMost(1f))
                                    .align(Alignment.BottomCenter)
                                    .background(gaugeColor)
                            )
                        }
                    }
                }

                // Sidebar with Next Piece + Opponent Board
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Next Piece Preview
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AdaptiveText(
                                text = if (currentLang == Language.RU) "СЛЕДУЮЩИЙ" else "NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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

                    // Opponent Mini Board
                    val opponentFlatGrid = if (isHost) room?.opponentGrid ?: emptyList() else room?.hostGrid ?: emptyList()
                    val opponentGrid2D = remember(opponentFlatGrid) { convertFlatListToGrid(opponentFlatGrid) }
                    val opponentCombo = if (isHost) room?.opponentCombo ?: 0 else room?.hostCombo ?: 0
                    val opponentGameOver = if (isHost) room?.opponentGameOver ?: false else room?.hostGameOver ?: false

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        MiniBoard(
                            grid = opponentGrid2D,
                            title = opponent?.name ?: (if (currentLang == Language.RU) "СОПЕРНИК" else "OPPONENT"),
                            score = opponentScore,
                            combo = opponentCombo
                        )

                        if (opponentGameOver) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.75f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "ВЫБЫЛ" else "OUT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gameplay Controls Bar
            val controlVerticalPosition by viewModel.controlVerticalPosition.collectAsStateWithLifecycle()
            val controlStyle by viewModel.controlStyle.collectAsStateWithLifecycle()
            val controlButtonScale by viewModel.controlButtonScale.collectAsStateWithLifecycle()
            val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()
            val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()

            if (!gameState.isGameOver) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (controlVerticalPosition == "middle") 60.dp else 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (controlStyle == "split") {
                        val leftSegment = @Composable {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ControlButton(actionType = "left", onClick = { viewModel.gameEngine.moveLeft() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                ControlButton(actionType = "down", onClick = { viewModel.gameEngine.softDrop() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            }
                        }
                        val rightSegment = @Composable {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ControlButton(actionType = "right", onClick = { viewModel.gameEngine.moveRight() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                ControlButton(actionType = "drop", onClick = { viewModel.gameEngine.hardDrop() }, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            }
                        }
                        val middleSegment = @Composable {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ControlButton(actionType = "rotate", onClick = { viewModel.gameEngine.rotate() }, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                ControlButton(actionType = "hold", onClick = { viewModel.gameEngine.hold() }, scale = controlButtonScale * 0.9f, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (leftHandedControls) {
                                rightSegment()
                                middleSegment()
                                leftSegment()
                            } else {
                                leftSegment()
                                middleSegment()
                                rightSegment()
                            }
                        }
                    } else if (controlStyle == "arcade") {
                        val actionCol = @Composable {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                ControlButton(actionType = "rotate", onClick = { viewModel.gameEngine.rotate() }, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ControlButton(actionType = "left", onClick = { viewModel.gameEngine.moveLeft() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                    ControlButton(actionType = "down", onClick = { viewModel.gameEngine.softDrop() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                    ControlButton(actionType = "right", onClick = { viewModel.gameEngine.moveRight() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                }
                            }
                        }
                        val triggerCol = @Composable {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ControlButton(actionType = "drop", onClick = { viewModel.gameEngine.hardDrop() }, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                                ControlButton(actionType = "hold", onClick = { viewModel.gameEngine.hold() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (leftHandedControls) {
                                triggerCol()
                                Spacer(modifier = Modifier.width(16.dp))
                                actionCol()
                            } else {
                                actionCol()
                                Spacer(modifier = Modifier.width(16.dp))
                                triggerCol()
                            }
                        }
                    } else {
                        val listBtns = listOf(
                            @Composable { ControlButton(actionType = "left", onClick = { viewModel.gameEngine.moveLeft() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
                            @Composable { ControlButton(actionType = "down", onClick = { viewModel.gameEngine.softDrop() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
                            @Composable { ControlButton(actionType = "rotate", onClick = { viewModel.gameEngine.rotate() }, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
                            @Composable { ControlButton(actionType = "right", onClick = { viewModel.gameEngine.moveRight() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (leftHandedControls) {
                                listBtns.reversed().forEach { it() }
                            } else {
                                listBtns.forEach { it() }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ControlButton(actionType = "drop", onClick = { viewModel.gameEngine.hardDrop() }, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            Spacer(modifier = Modifier.width(12.dp))
                            ControlButton(actionType = "hold", onClick = { viewModel.gameEngine.hold() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Exit confirmation Dialog
        if (showExitConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                title = {
                    Text(if (currentLang == Language.RU) "Капитулировать?" else "Surrender Match?")
                },
                text = {
                    Text(if (currentLang == Language.RU) "Вы действительно хотите сдаться и выйти в лобби?" 
                         else "Are you sure you want to surrender and exit to the lobby?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitConfirmDialog = false
                            viewModel.lobbyManager.leaveRoom()
                            onBackToLobby()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (currentLang == Language.RU) "Сдаться" else "Surrender")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmDialog = false }) {
                        Text(if (currentLang == Language.RU) "Отмена" else "Cancel")
                    }
                }
            )
        }

        // Match Over Final Dialog
        if (room?.status == "finished" && room!!.winnerId.isNotEmpty()) {
            val won = room!!.winnerId == localUid
            val isDraw = room!!.winnerId == "draw"
            val myScore = if (isHost) room!!.hostScore else room!!.opponentScore
            val oppScore = if (isHost) room!!.opponentScore else room!!.hostScore
            val myCredits = if (won) (50 + myScore / 5 + 150) else (50 + myScore / 5)
            
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isDraw) Color.Yellow.copy(alpha = 0.15f)
                                    else if (won) Color.Green.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isDraw) "🤝" else if (won) "🏆" else "💀",
                                fontSize = 32.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isDraw) (if (currentLang == Language.RU) "НИЧЬЯ!" else "DRAW MATCH!")
                                   else if (won) (if (currentLang == Language.RU) "ПОБЕДА!" else "VICTORY!")
                                   else (if (currentLang == Language.RU) "ПОРАЖЕНИЕ" else "DEFEAT"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isDraw) Color.Yellow else if (won) Color.Green else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isDraw) (if (currentLang == Language.RU) "Оба игрока набрали равные очки!" else "Both players finished with equal score!")
                                   else if (won) (if (currentLang == Language.RU) "Отличная игра! Награда начислена на ваш счет." else "Incredible execution! Credits awarded to your account.")
                                   else (if (currentLang == Language.RU) "Соперник превзошел вас по очкам. Попробуйте еще раз!" else "Opponent scored higher. Practice and re-queue!"),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Player Stats Card
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (won) Color.Green.copy(alpha = 0.05f) else Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = if (won) 2.dp else 1.dp,
                                    color = if (won) Color.Green else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) "ВЫ" else "YOU",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (won) Color.Green else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$myScore",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+$myCredits CR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Opponent Stats Card
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (!won && !isDraw) Color.Red.copy(alpha = 0.05f) else Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = if (!won && !isDraw) 2.dp else 1.dp,
                                    color = if (!won && !isDraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = opponent?.name ?: (if (currentLang == Language.RU) "СОПЕРНИК" else "OPPONENT"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!won && !isDraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$oppScore",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isDraw) "DRAW" else if (!won) "WINNER" else "OUT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDraw) Color.Yellow else if (!won) Color.Green else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.lobbyManager.leaveRoom()
                            onBackToLobby()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (currentLang == Language.RU) "В ЛОББИ" else "GO TO LOBBY",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            )
        }
    }
}

// Flat list deserializer helper
private fun convertFlatListToGrid(flatList: List<Int>): List<IntArray> {
    val grid = mutableListOf<IntArray>()
    for (r in 0 until 12) {
        val row = IntArray(10)
        for (c in 0 until 10) {
            val idx = r * 10 + c
            row[c] = if (idx < flatList.size) flatList[idx] else 0
        }
        grid.add(row)
    }
    return grid
}
