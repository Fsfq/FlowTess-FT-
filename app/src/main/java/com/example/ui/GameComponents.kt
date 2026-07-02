package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.R
import com.example.game.Colors
import com.example.game.GameState
import com.example.game.Position
import com.example.game.Tetromino
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val gameState by viewModel.gameEngine.gameState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()

    val blockStyle by viewModel.blockStyle.collectAsStateWithLifecycle()
    val ghostVisible by viewModel.ghostVisible.collectAsStateWithLifecycle()
    val nextCount by viewModel.nextCount.collectAsStateWithLifecycle()
    val controlStyle by viewModel.controlStyle.collectAsStateWithLifecycle()
    val smoothFallingEnabled by viewModel.smoothFallingEnabled.collectAsStateWithLifecycle()
    val gridOpacity by viewModel.gridOpacity.collectAsStateWithLifecycle()
    val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()
    val controlButtonScale by viewModel.controlButtonScale.collectAsStateWithLifecycle()
    val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()
    val gridLineDensity by viewModel.gridLineDensity.collectAsStateWithLifecycle()
    val controlVerticalPosition by viewModel.controlVerticalPosition.collectAsStateWithLifecycle()

    val screenShakeIntensity by viewModel.screenShakeIntensity.collectAsStateWithLifecycle()
    val scanlinesFilter by viewModel.scanlinesFilter.collectAsStateWithLifecycle()
    val boardColorSkin by viewModel.boardColorSkin.collectAsStateWithLifecycle()

    val themeColorKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeColor = remember(themeColorKey) {
        when (themeColorKey) {
            "cyan" -> Color(0xFF00FFCC)
            "purple" -> Color(0xFFD0BCFF)
            "pink" -> Color(0xFFF43F5E)
            "gold_ma" -> Color(0xFFFFD700)
            "emerald" -> Color(0xFF10B981)
            "blue" -> Color(0xFF2196F3)
            "red" -> Color(0xFFF44336)
            else -> Color(0xFF6366F1) // indigo
        }
    }
    val statsHighScore by viewModel.statsHighScore.collectAsStateWithLifecycle()

    var shakeX by remember { mutableStateOf(0.dp) }
    var shakeY by remember { mutableStateOf(0.dp) }

    LaunchedEffect(gameState.lines) {
        if (gameState.lines > 0 && screenShakeIntensity > 0f) {
            val baseRange = (8 * screenShakeIntensity).toInt().coerceAtLeast(2)
            for (i in 0..6) {
                shakeX = ((-baseRange..baseRange).random()).dp
                shakeY = ((-baseRange..baseRange).random()).dp
                delay(30)
            }
            shakeX = 0.dp
            shakeY = 0.dp
        }
    }

    var saveStatusMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.pauseGame()
        }
    }

    LaunchedEffect(saveStatusMessage) {
        if (saveStatusMessage != null) {
            delay(2000)
            saveStatusMessage = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(top = 8.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AdaptiveText(
                            text = Translations.get("high_score", currentLang).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        AdaptiveText(
                            text = "$statsHighScore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.pauseGame()
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (gameState.gameMode == com.example.game.GameMode.RELAX) {
                            var showRelaxDialog by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    showRelaxDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Relax Mode Settings",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            if (showRelaxDialog) {
                                RelaxSettingsDialog(
                                    viewModel = viewModel,
                                    onDismiss = { showRelaxDialog = false }
                                )
                            }
                        }

                        if (!gameState.isGameOver && gameState.currentPiece != null) {
                            IconButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    if (isPlaying) {
                                        viewModel.pauseGame()
                                    } else {
                                        viewModel.resumeGame()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Resume",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val success = viewModel.saveCurrentGame()
                                saveStatusMessage = if (success) {
                                    Translations.get("save_success", currentLang)
                                } else {
                                    Translations.get("save_fail", currentLang)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            AdaptiveText(
                                text = Translations.get("save_game", currentLang).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedCard(
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                AdaptiveText(
                                    text = "LVL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                AdaptiveText(
                                    text = "${gameState.level}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }
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
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            AnimatedVisibility(visible = saveStatusMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AdaptiveText(
                        text = saveStatusMessage ?: "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.width(78.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AdaptiveText(
                            text = Translations.get("hold", currentLang).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedCard(
                        modifier = Modifier.size(72.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            gameState.holdPiece?.let { PreviewNextPiece(it, blockStyle) }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "ЛИНИИ" else "LINES",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            AdaptiveText(
                                text = "${gameState.lines}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (gameState.gameMode == com.example.game.GameMode.TIME_ATTACK) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (gameState.timeRemainingSeconds <= 15) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (gameState.timeRemainingSeconds <= 15) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (gameState.timeRemainingSeconds <= 15) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AdaptiveText(
                                        text = if (currentLang == Language.RU) "ВРЕМЯ" else "TIME",
                                        color = if (gameState.timeRemainingSeconds <= 15) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                val minutes = gameState.timeRemainingSeconds / 60
                                val seconds = gameState.timeRemainingSeconds % 60
                                val minStr = if (minutes < 10) "0$minutes" else "$minutes"
                                val secStr = if (seconds < 10) "0$seconds" else "$seconds"
                                AdaptiveText(
                                    text = "$minStr:$secStr",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (gameState.timeRemainingSeconds <= 15) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = shakeX, y = shakeY)
                        .aspectRatio(10f / 20f)
                        .background(Color(0xFF0F0E14).copy(alpha = gridOpacity), RoundedCornerShape(14.dp))
                        .border(
                            BorderStroke(
                                2.5.dp, 
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(themeColor, themeColor.copy(alpha = 0.3f))
                                )
                            ), 
                            RoundedCornerShape(14.dp)
                        )
                        .padding(4.dp)
                ) {
                    GameBoardView(
                        gameState = gameState,
                        blockStyle = blockStyle,
                        ghostVisible = ghostVisible,
                        smoothFallingEnabled = smoothFallingEnabled,
                        gridLineDensity = gridLineDensity,
                        boardColorSkin = boardColorSkin,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (scanlinesFilter) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val count = size.height / 6f
                            for (i in 0..count.toInt()) {
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.12f),
                                    topLeft = Offset(0f, i * 6f),
                                    size = Size(size.width, 2f)
                                )
                            }
                        }
                    }

                    if (gameState.isGameOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(24.dp)
                            ) {
                                AdaptiveText(
                                    text = Translations.get("game_over", currentLang).uppercase(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.startGame(gameState.gameMode) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    AdaptiveText(
                                        text = Translations.get("restart", currentLang).uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.width(78.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AdaptiveText(
                            text = Translations.get("next", currentLang).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val countToShow = minOf(nextCount, gameState.nextPieces.size)
                            for (i in 0 until countToShow) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PreviewNextPiece(gameState.nextPieces[i], blockStyle)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AdaptiveText(
                                    text = Translations.get("score", currentLang).uppercase(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            AdaptiveText(
                                text = "${gameState.score}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

        Spacer(modifier = Modifier.height(20.dp))

        if (!gameState.isGameOver) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (controlVerticalPosition == "middle") 60.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
        } // Closes main Column

    } // Closes Box
} // Closes Scaffold content lambda
} // Closes GameScreen function
}

@Composable
fun GameControlsSection(
    viewModel: MainViewModel,
    gameState: GameState,
    controlStyle: String,
    leftHandedControls: Boolean,
    controlVerticalPosition: String,
    controlButtonScale: Float,
    controlButtonStyle: String,
    onLeftPress: () -> Unit,
    onRightPress: () -> Unit,
    onDownPress: () -> Unit,
    onRotatePress: () -> Unit,
    onHardDropPress: () -> Unit,
    onHoldPress: () -> Unit
) {
    if (controlStyle == "split") {
        val leftSegment = @Composable {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ControlButton(actionType = "left", onClick = onLeftPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                ControlButton(actionType = "down", onClick = onDownPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
            }
        }
        val rightSegment = @Composable {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ControlButton(actionType = "right", onClick = onRightPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
            }
        }
        val middleSegment = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ControlButton(actionType = "rotate", onClick = onRotatePress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale * 0.9f, buttonStyle = controlButtonStyle, viewModel = viewModel)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
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
                ControlButton(actionType = "rotate", onClick = onRotatePress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ControlButton(actionType = "left", onClick = onLeftPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                    ControlButton(actionType = "down", onClick = onDownPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                    ControlButton(actionType = "right", onClick = onRightPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                }
            }
        }
        val triggerCol = @Composable {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
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
            @Composable { ControlButton(actionType = "left", onClick = onLeftPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
            @Composable { ControlButton(actionType = "down", onClick = onDownPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
            @Composable { ControlButton(actionType = "rotate", onClick = onRotatePress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
            @Composable { ControlButton(actionType = "right", onClick = onRightPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leftHandedControls) {
                listBtns.reversed().forEach { it() }
            } else {
                listBtns.forEach { it() }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
            Spacer(modifier = Modifier.width(12.dp))
            ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
        }
    }
}

@Composable
fun PreviewNextPiece(piece: Tetromino, style: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / 4
        piece.shape.forEach { p ->
            val color = Colors.getOrElse(piece.colorIndex) { Color.Gray }
            val x = (p.x + 1) * cellSize
            val y = (p.y + 1) * cellSize
            drawBlock(color, x, y, cellSize, style)
        }
    }
}

@Composable
fun GameBoardView(
    gameState: GameState,
    blockStyle: String,
    ghostVisible: Boolean,
    smoothFallingEnabled: Boolean,
    gridLineDensity: String = "standard",
    boardColorSkin: String = "cyberpunk",
    modifier: Modifier = Modifier
) {
    val animatedY by androidx.compose.animation.core.animateFloatAsState(
        targetValue = gameState.currentPos.y.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150, easing = androidx.compose.animation.core.LinearEasing),
        label = "SmoothFalling"
    )

    val animatedX by androidx.compose.animation.core.animateFloatAsState(
        targetValue = gameState.currentPos.x.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 100, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "SmoothSliding"
    )

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "GradientAnimation")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween<Float>(durationMillis = 6000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "Offset"
    )

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = {}
            )
        }
    ) {
        val cols = 10
        val visibleRows = 20
        val cellSize = size.width / cols
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (gridLineDensity != "none") {
            val isDashed = gridLineDensity == "dashed"
            val pathEffect = if (isDashed) {
                androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            } else null

            val gridColor = when (boardColorSkin) {
                "cyberpunk" -> Color(0xFF00FFCC)
                "matrix" -> Color(0xFF00FF00)
                "violet" -> Color(0xFF8A2BE2)
                "ruby" -> Color(0xFFFF014C)
                "monochrome" -> Color(0xFF8E9297)
                else -> Color.White
            }.copy(alpha = if (isDashed) 0.12f else 0.08f)

            val strokeWidthVal = if (isDashed) 1.2f else 1.0f
            // High-performance single-pass line rendering instead of 200 nested rectangle draws
            for (x in 0..cols) {
                val lx = x * cellSize
                drawLine(
                    color = gridColor,
                    start = Offset(lx, 0f),
                    end = Offset(lx, visibleRows * cellSize),
                    strokeWidth = strokeWidthVal,
                    pathEffect = pathEffect
                )
            }
            for (y in 0..visibleRows) {
                val ly = y * cellSize
                drawLine(
                    color = gridColor,
                    start = Offset(0f, ly),
                    end = Offset(cols * cellSize, ly),
                    strokeWidth = strokeWidthVal,
                    pathEffect = pathEffect
                )
            }
        }

        val isMirror = gameState.gameMode == com.example.game.GameMode.MIRROR_DIMENSION

        for (y in 2 until 22) {
            for (x in 0 until cols) {
                if (y >= 0 && y < gameState.grid.size && x >= 0 && x < gameState.grid[y].size) {
                    val value = gameState.grid[y][x]
                    if (value != 0) {
                        val drawX = if (isMirror) cols - 1 - x else x
                        drawBlock(
                            color = Colors.getOrElse(value) { Color.Gray },
                            x = drawX * cellSize,
                            y = (y - 2) * cellSize,
                            size = cellSize,
                            style = blockStyle,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            animOffset = gradientOffset
                        )
                    }
                }
            }
        }

        gameState.currentPiece?.let { piece ->
            if (ghostVisible) {
                var ghostY = gameState.currentPos.y
                while (isValidGhostMove(gameState.currentPos.copy(y = ghostY + 1), piece, gameState.grid)) {
                    ghostY++
                }
                piece.shape.forEach { p ->
                    val nx = gameState.currentPos.x + p.x
                    val ny = ghostY + p.y - 2
                    if (ny >= 0) {
                        val drawNx = if (isMirror) cols - 1 - nx else nx
                        drawBlock(
                            color = Colors.getOrElse(piece.colorIndex) { Color.Gray }.copy(alpha = 0.2f),
                            x = drawNx * cellSize,
                            y = ny * cellSize,
                            size = cellSize,
                            style = blockStyle,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            animOffset = gradientOffset
                        )
                    }
                }
            }

            val drawY = if (smoothFallingEnabled) animatedY else gameState.currentPos.y.toFloat()
            val drawXVal = animatedX

            piece.shape.forEach { p ->
                val nx = drawXVal + p.x
                val ny = drawY + p.y - 2
                if (ny >= 0) {
                    val drawNx = if (isMirror) cols - 1 - nx else nx
                    drawBlock(
                        color = Colors.getOrElse(piece.colorIndex) { Color.Gray },
                        x = drawNx * cellSize,
                        y = ny * cellSize,
                        size = cellSize,
                        style = blockStyle,
                        canvasWidth = canvasWidth,
                        canvasHeight = canvasHeight,
                        animOffset = gradientOffset
                    )
                }
            }
        }
    }
}

private fun isValidGhostMove(pos: Position, piece: Tetromino, grid: List<IntArray>): Boolean {
    for (p in piece.shape) {
        val nx = pos.x + p.x
        val ny = pos.y + p.y
        if (nx !in 0..9) return false
        if (ny >= grid.size) return false
        if (ny >= 0 && nx >= 0 && nx < grid[ny].size && grid[ny][nx] != 0) return false
    }
    return true
}

private fun DrawScope.drawBlock(
    color: Color,
    x: Float,
    y: Float,
    size: Float,
    style: String,
    canvasWidth: Float = 0f,
    canvasHeight: Float = 0f,
    animOffset: Float = 0f
) {
    when (style) {
        "red_gradient", "green_gradient", "blue_gradient", "purple_gradient" -> {
            val startY = animOffset % (canvasHeight + 1f)
            val brush = when (style) {
                "red_gradient" -> androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFFFF0055), Color(0xFFFF5500)),
                    start = Offset(0f, startY),
                    end = Offset(canvasWidth, startY + canvasHeight)
                )
                "green_gradient" -> androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF00FFCC), Color(0xFF00FF00)),
                    start = Offset(0f, startY),
                    end = Offset(canvasWidth, startY + canvasHeight)
                )
                "blue_gradient" -> androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF00FFFF), Color(0xFF0055FF)),
                    start = Offset(0f, startY),
                    end = Offset(canvasWidth, startY + canvasHeight)
                )
                "purple_gradient" -> androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFFFF00FF), Color(0xFF8800FF)),
                    start = Offset(0f, startY),
                    end = Offset(canvasWidth, startY + canvasHeight)
                )
                else -> androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(color, color))
            }
            drawRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(size, size)
            )
        }
        "glowing_jewel" -> {
            val gemColor = Color(0xFF007FFF)
            drawRoundRect(
                color = gemColor,
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(size - 2f, size - 2f),
                cornerRadius = CornerRadius(size * 0.15f)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x + size * 0.2f, y + size * 0.2f),
                size = Size(size * 0.6f, size * 0.6f),
                style = Stroke(width = 1f)
            )
            drawLine(Color.White.copy(alpha = 0.3f), Offset(x, y), Offset(x + size * 0.2f, y + size * 0.2f))
            drawLine(Color.White.copy(alpha = 0.3f), Offset(x + size, y), Offset(x + size * 0.8f, y + size * 0.2f))
            drawLine(Color.White.copy(alpha = 0.3f), Offset(x, y + size), Offset(x + size * 0.2f, y + size * 0.8f))
            drawLine(Color.White.copy(alpha = 0.3f), Offset(x + size, y + size), Offset(x + size * 0.8f, y + size * 0.8f))
        }
        "steampunk" -> {
            val brassColor = Color(0xFFB8860B)
            drawRect(
                color = brassColor,
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(size - 2f, size - 2f)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(size - 2f, size - 2f),
                style = Stroke(width = 2f)
            )
            val rivetRadius = size * 0.08f
            val offset = size * 0.18f
            drawCircle(Color(0xFFD4AF37), rivetRadius, Offset(x + offset, y + offset))
            drawCircle(Color(0xFFD4AF37), rivetRadius, Offset(x + size - offset, y + offset))
            drawCircle(Color(0xFFD4AF37), rivetRadius, Offset(x + offset, y + size - offset))
            drawCircle(Color(0xFFD4AF37), rivetRadius, Offset(x + size - offset, y + size - offset))
        }
        "neon" -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(size - 2f, size - 2f),
                cornerRadius = CornerRadius(size * 0.15f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(x + size * 0.1f, y + size * 0.1f),
                size = Size(size * 0.8f, size * 0.8f),
                cornerRadius = CornerRadius(size * 0.1f),
                style = Stroke(width = size * 0.05f)
            )
        }
        "glass" -> {
            drawRoundRect(
                color = color.copy(alpha = 0.75f),
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(size - 2f, size - 2f),
                cornerRadius = CornerRadius(size * 0.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(x + size * 0.1f, y + size * 0.1f),
                size = Size(size * 0.8f, size * 0.3f),
                cornerRadius = CornerRadius(size * 0.08f)
            )
        }
        "retro" -> {
            drawRect(
                color = color,
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(size - 2f, size - 2f)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(x + size * 0.15f, y + size * 0.15f),
                size = Size(size * 0.7f, size * 0.7f)
            )
            drawRect(
                color = color,
                topLeft = Offset(x + size * 0.25f, y + size * 0.25f),
                size = Size(size * 0.5f, size * 0.5f)
            )
        }
        "flat" -> {
            drawRect(
                color = color,
                topLeft = Offset(x + 0.5f, y + 0.5f),
                size = Size(size - 1f, size - 1f)
            )
        }
        "material" -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + 1.5f, y + 1.5f),
                size = Size(size - 3f, size - 3f),
                cornerRadius = CornerRadius(size * 0.28f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(x + 3f, y + 3f),
                size = Size(size - 6f, size - 6f),
                cornerRadius = CornerRadius(size * 0.22f),
                style = Stroke(width = size * 0.08f)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.15f),
                topLeft = Offset(x + size * 0.15f, y + size * 0.15f),
                size = Size(size * 0.7f, size * 0.7f),
                cornerRadius = CornerRadius(size * 0.12f),
                style = Stroke(width = size * 0.04f)
            )
        }
        else -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(size - 2f, size - 2f),
                cornerRadius = CornerRadius(size * 0.15f)
            )
        }
    }
}

@Composable
fun ControlButton(
    text: String? = null,
    actionType: String? = null,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    scale: Float = 1.0f,
    buttonStyle: String = "neon",
    viewModel: MainViewModel? = null
) {
    val isPlayingFlow = viewModel?.isPlaying
    val isPlaying by if (isPlayingFlow != null) {
        isPlayingFlow.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(true) }
    }

    val controlButtonAlphaFlow = viewModel?.controlButtonAlpha
    val controlButtonAlpha by if (controlButtonAlphaFlow != null) {
        controlButtonAlphaFlow.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val finalAlpha = if (!isPlaying) 0.35f else controlButtonAlpha

    val canRepeat = actionType == "down" || actionType == "left" || actionType == "right"
    var isPressed by remember { mutableStateOf(false) }

    val scaleFactor by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = 350f
        ),
        label = "btn_scale"
    )

    if (isPressed && canRepeat && isPlaying) {
        val currentOnClick by rememberUpdatedState(onClick)
        LaunchedEffect(isPressed) {
            delay(180)
            while (isPressed) {
                currentOnClick()
                val repeatDelay = if (actionType == "down") 45L else 75L
                delay(repeatDelay)
            }
        }
    }

    val buttonSize = (56 * scale).dp
    val buttonCorner = (28 * scale).dp

    val backgroundBrush = when (buttonStyle) {
        "glass" -> {
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isPressed) 0.18f else 0.08f),
                    Color.White.copy(alpha = if (isPressed) 0.24f else 0.12f)
                )
            )
        }
        "neon" -> {
            val primaryColor = MaterialTheme.colorScheme.primary
            val bgColor = if (isPrimary) primaryColor else MaterialTheme.colorScheme.surfaceVariant
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    bgColor.copy(alpha = if (isPressed) 0.35f else 0.14f),
                    bgColor.copy(alpha = if (isPressed) 0.25f else 0.09f)
                )
            )
        }
        else -> { // classic
            val primaryColor = MaterialTheme.colorScheme.primary
            val surfaceCol = if (isPrimary) {
                if (isPressed) primaryColor.copy(alpha = 0.75f) else primaryColor
            } else {
                if (isPressed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f) else MaterialTheme.colorScheme.surfaceVariant
            }
            androidx.compose.ui.graphics.SolidColor(surfaceCol)
        }
    }

    val borderStroke = when (buttonStyle) {
        "glass" -> {
            androidx.compose.foundation.BorderStroke(
                width = 1.2.dp,
                color = Color.White.copy(alpha = if (isPressed) 0.55f else 0.28f)
            )
        }
        "neon" -> {
            val glowingColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
            androidx.compose.foundation.BorderStroke(
                width = if (isPressed) 2.4.dp else 1.6.dp,
                color = if (isPressed) glowingColor else glowingColor.copy(alpha = 0.48f)
            )
        }
        else -> { // classic
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isPressed) MaterialTheme.colorScheme.primary else (if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline)
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(buttonSize)
            .graphicsLayer(scaleX = scaleFactor, scaleY = scaleFactor, alpha = finalAlpha)
            .clip(RoundedCornerShape(buttonCorner))
            .background(backgroundBrush)
            .pointerInput(onClick, isPlaying) {
                if (isPlaying) {
                    detectTapGestures(
                        onPress = {
                            try {
                                isPressed = true
                                onClick()
                                try {
                                    awaitRelease()
                                } catch (e: Exception) {
                                }
                            } finally {
                                isPressed = false
                            }
                        }
                    )
                }
            }
            .border(borderStroke, RoundedCornerShape(buttonCorner))
    ) {
        if (actionType != null) {
            val color = when (buttonStyle) {
                "glass" -> Color.White
                "neon" -> if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                else -> if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            }
            if (actionType == "hold") {
                Icon(
                    imageVector = Icons.Default.SaveAlt,
                    contentDescription = "Hold",
                    tint = color,
                    modifier = Modifier.size((24 * scale).dp)
                )
            } else {
                val pathScale = scale
                Canvas(modifier = Modifier.size((26 * scale).dp)) {
                val w = size.width
                val h = size.height
                
                val mainStroke = Stroke(
                    width = (3.5f * pathScale).dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
                val shadowStroke = Stroke(
                    width = (4.5f * pathScale).dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )

                val shadowColor = color.copy(alpha = 0.22f)

                when (actionType) {
                    "drop" -> {
                        val p1 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.22f, h * 0.22f)
                            lineTo(w * 0.5f, h * 0.44f)
                            lineTo(w * 0.78f, h * 0.22f)
                        }
                        val p2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.22f, h * 0.44f)
                            lineTo(w * 0.5f, h * 0.66f)
                            lineTo(w * 0.78f, h * 0.44f)
                        }
                        val p3 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.22f, h * 0.66f)
                            lineTo(w * 0.5f, h * 0.88f)
                            lineTo(w * 0.78f, h * 0.66f)
                        }

                        drawPath(path = p1, color = shadowColor, style = shadowStroke)
                        drawPath(path = p2, color = shadowColor, style = shadowStroke)
                        drawPath(path = p3, color = shadowColor, style = shadowStroke)

                        drawPath(path = p1, color = color, style = mainStroke)
                        drawPath(path = p2, color = color, style = mainStroke)
                        drawPath(path = p3, color = color, style = mainStroke)
                    }
                    "left" -> {
                        val p1 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.70f, h * 0.22f)
                            lineTo(w * 0.42f, h * 0.5f)
                            lineTo(w * 0.70f, h * 0.78f)
                        }
                        val p2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.44f, h * 0.22f)
                            lineTo(w * 0.16f, h * 0.5f)
                            lineTo(w * 0.44f, h * 0.78f)
                        }

                        drawPath(path = p1, color = shadowColor, style = shadowStroke)
                        drawPath(path = p2, color = shadowColor, style = shadowStroke)

                        drawPath(path = p1, color = color, style = mainStroke)
                        drawPath(path = p2, color = color, style = mainStroke)
                    }
                    "right" -> {
                        val p1 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.30f, h * 0.22f)
                            lineTo(w * 0.58f, h * 0.5f)
                            lineTo(w * 0.30f, h * 0.78f)
                        }
                        val p2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.56f, h * 0.22f)
                            lineTo(w * 0.84f, h * 0.5f)
                            lineTo(w * 0.56f, h * 0.78f)
                        }

                        drawPath(path = p1, color = shadowColor, style = shadowStroke)
                        drawPath(path = p2, color = shadowColor, style = shadowStroke)

                        drawPath(path = p1, color = color, style = mainStroke)
                        drawPath(path = p2, color = color, style = mainStroke)
                    }
                    "down" -> {
                        val p1 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.22f, h * 0.30f)
                            lineTo(w * 0.5f, h * 0.58f)
                            lineTo(w * 0.78f, h * 0.30f)
                        }
                        val p2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.22f, h * 0.56f)
                            lineTo(w * 0.5f, h * 0.84f)
                            lineTo(w * 0.78f, h * 0.56f)
                        }

                        drawPath(path = p1, color = shadowColor, style = shadowStroke)
                        drawPath(path = p2, color = shadowColor, style = shadowStroke)

                        drawPath(path = p1, color = color, style = mainStroke)
                        drawPath(path = p2, color = color, style = mainStroke)
                    }
                    "rotate" -> {
                        val arcSize = size * 0.70f
                        val offset = size * 0.15f
                        
                        drawArc(
                            color = shadowColor,
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(offset.width, offset.height),
                            size = Size(arcSize.width, arcSize.height),
                            style = shadowStroke
                        )
                        drawArc(
                            color = color,
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(offset.width, offset.height),
                            size = Size(arcSize.width, arcSize.height),
                            style = mainStroke
                        )

                        val arrowHead = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.66f, h * 0.15f) // Perfectly horizontal symmetrical tip
                            lineTo(w * 0.42f, h * 0.03f) // Symmetrical top corner
                            lineTo(w * 0.42f, h * 0.27f) // Symmetrical bottom corner
                            close()
                        }
                        
                        drawPath(path = arrowHead, color = shadowColor)
                        drawPath(path = arrowHead, color = color)
                    }
                }
            }
            }
        } else if (text != null) {
            val color = when (buttonStyle) {
                "glass" -> Color.White
                "neon" -> if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                else -> if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            }
            AdaptiveText(
                text = text,
                fontSize = (11 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}



@Composable
fun RelaxSettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val immortal by viewModel.relaxImmortal.collectAsStateWithLifecycle()
    val speed by viewModel.relaxSpeed.collectAsStateWithLifecycle()
    val blockSet by viewModel.relaxBlockSet.collectAsStateWithLifecycle()
    val ghostEnabled by viewModel.relaxGhostEnabled.collectAsStateWithLifecycle()
    
    val themeColorKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeColor = remember(themeColorKey) {
        when (themeColorKey) {
            "indigo" -> Color(0xFFD0BCFF)
            "red" -> Color(0xFFFF5555)
            "neon" -> Color(0xFF00FFCC)
            "amber" -> Color(0xFFF59E0B)
            "rose" -> Color(0xFFF43F5E)
            "sky" -> Color(0xFF0EA5E9)
            "cyber_pink" -> Color(0xFFFF007F)
            "toxic_green" -> Color(0xFF39FF14)
            else -> Color(0xFFD0BCFF)
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == Language.RU) "НАСТРОЙКИ РЕЛАКСА" else "SANDBOX SETTINGS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = themeColor
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("equip")
                            viewModel.clearRelaxBoard()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear grid",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card 1: Main Toggles
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Immortality
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (currentLang == Language.RU) "Бессмертный режим" else "Immortality Mode",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = if (currentLang == Language.RU) "Очистка поля при переполнении" else "Clears matrix on overflow",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = immortal,
                                    onCheckedChange = {
                                        viewModel.triggerAudioFeedback("equip")
                                        viewModel.setRelaxImmortal(it)
                                    }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Ghost Block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (currentLang == Language.RU) "Отображать проекцию" else "Show Ghost Piece",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = if (currentLang == Language.RU) "Визуальная тень падающей фигуры" else "Displays where block will land",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = ghostEnabled,
                                    onCheckedChange = {
                                        viewModel.triggerAudioFeedback("equip")
                                        viewModel.setRelaxGhostEnabled(it)
                                    }
                                )
                            }
                        }
                    }

                    // Card 2: Falling Block Set Selection
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "НАБОР ПАДАЮЩИХ ФИГУР" else "FALLING TETROMINOS",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = themeColor
                            )
                            
                            val blockOptions = listOf(
                                "only_i" to (if (currentLang == Language.RU) "Только прямые (I)" else "Only Line pieces (I)"),
                                "ideal" to (if (currentLang == Language.RU) "Идеальные (I, O, T)" else "Ideal simple (I, O, T)"),
                                "standard" to (if (currentLang == Language.RU) "Классический набор (7 фигур)" else "Classic standard (7 pieces)"),
                                "all" to (if (currentLang == Language.RU) "Все фигуры (+Пентатрис)" else "All shapes (standard + 5-blocks)")
                            )

                            blockOptions.forEach { opt ->
                                val isSelected = blockSet == opt.first
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) themeColor.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable {
                                            viewModel.triggerAudioFeedback("click")
                                            viewModel.setRelaxBlockSet(opt.first)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            viewModel.setRelaxBlockSet(opt.first)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = themeColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = opt.second,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Card 3: Speed (Gravity) Selection
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "СКОРОСТЬ ПАДЕНИЯ (ГРАВИТАЦИЯ)" else "FALL SPEED (GRAVITY)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = themeColor
                            )

                            val speedOptions = listOf(
                                "static" to (if (currentLang == Language.RU) "Без гравитации" else "Zero gravity"),
                                "slow" to (if (currentLang == Language.RU) "Медленно" else "Slow speed"),
                                "normal" to (if (currentLang == Language.RU) "Средняя" else "Normal speed"),
                                "fast" to (if (currentLang == Language.RU) "Высокая" else "Fast speed")
                            )

                            speedOptions.forEach { opt ->
                                val isSelected = speed == opt.first
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) themeColor.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable {
                                            viewModel.triggerAudioFeedback("click")
                                            viewModel.setRelaxSpeed(opt.first)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            viewModel.setRelaxSpeed(opt.first)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = themeColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = opt.second,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            viewModel.clearRelaxBoard()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                    ) {
                        Text(if (currentLang == Language.RU) "ОЧИСТИТЬ ПОЛЕ" else "CLEAR BOARD", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor, contentColor = Color.Black)
                    ) {
                        Text(if (currentLang == Language.RU) "ИГРАТЬ" else "PLAY", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
