package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.absoluteValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.Colors
import com.example.game.GameState
import com.example.game.Position
import com.example.game.Tetromino
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ═══════════════════════════════════════════════════════════════════════════
// MODERN GAME SCREEN LAYOUT (CYBER-ARCADE REDESIGN)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun ModernGameLayout(
    viewModel: MainViewModel,
    gameState: GameState,
    isPlaying: Boolean,
    currentLang: Language,
    themeColor: Color,
    statsHighScore: Int,
    shakeX: androidx.compose.ui.unit.Dp,
    shakeY: androidx.compose.ui.unit.Dp,
    blockStyle: String,
    ghostVisible: Boolean,
    ghostOutlineOnly: Boolean,
    nextCount: Int,
    controlStyle: String,
    smoothFallingEnabled: Boolean,
    gridOpacity: Float,
    leftHandedControls: Boolean,
    controlButtonScale: Float,
    controlButtonStyle: String,
    gridLineDensity: String,
    controlVerticalPosition: String,
    scanlinesFilter: Boolean,
    boardColorSkin: String,
    graphicsQuality: String,
    onBack: () -> Unit
) {
    val controlBottomPadding by viewModel.controlBottomPadding.collectAsStateWithLifecycle()
    val isDangerCeiling = remember(gameState.grid) {
        gameState.grid.take(5).any { row -> row.any { it != 0 } }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Menu Button
                        FilledTonalIconButton(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.pauseGame()
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = Translations.get("menu", currentLang),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center: Score & Level Info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${gameState.score}",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "${Translations.get("level", currentLang).uppercase()} ${gameState.level}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                if (statsHighScore > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$statsHighScore",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Actions: Settings, Save & Pause
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (gameState.gameMode == com.example.game.GameMode.RELAX) {
                                var showRelaxDialog by remember { mutableStateOf(false) }
                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        showRelaxDialog = true
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = Translations.get("settings", currentLang),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (showRelaxDialog) {
                                    RelaxSettingsDialog(viewModel = viewModel, onDismiss = { showRelaxDialog = false })
                                }
                            }


                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pattern Puzzle Banner
                if (gameState.gameMode == com.example.game.GameMode.PATTERN_PUZZLE) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Extension,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${Translations.get("puzzle_stencil", currentLang)}: ${gameState.puzzleGoalDescription}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = "${gameState.puzzleFilledCount}/${gameState.puzzleTargetCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else if (gameState.gameMode == com.example.game.GameMode.MEMORY_PUZZLE) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (gameState.memoryCountdownSeconds > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (gameState.memoryCountdownSeconds > 0) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (gameState.memoryCountdownSeconds > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (gameState.memoryCountdownSeconds > 0) {
                                    when (currentLang) {
                                        Language.RU -> "ЗАПОМНИТЕ: ${gameState.memoryCountdownSeconds} сек!"
                                        Language.UA -> "ЗАПАМ'ЯТАЙТЕ: ${gameState.memoryCountdownSeconds} сек!"
                                        Language.KK -> "ЕСТЕ САҚТАҢЫЗ: ${gameState.memoryCountdownSeconds} сек!"
                                        Language.DE -> "MERKEN: ${gameState.memoryCountdownSeconds}s!"
                                        Language.ZH -> "记忆倒计时：${gameState.memoryCountdownSeconds}秒！"
                                        else -> "MEMORIZE: ${gameState.memoryCountdownSeconds}s!"
                                    }
                                } else {
                                    when (currentLang) {
                                        Language.RU -> "ПАМЯТЬ: ШАБЛОН СКРЫТ"
                                        Language.UA -> "ПАМ'ЯТЬ: ШАБЛОН ПРИХОВАНО"
                                        Language.KK -> "ЖАДЫ: ҮЛГІ ЖАСЫРЫЛДЫ"
                                        Language.DE -> "GEDÄCHTNIS: MUSTER VERSTECKT"
                                        Language.ZH -> "记忆模式：图案已隐藏"
                                        else -> "MEMORY: PATTERN HIDDEN"
                                    }
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (gameState.memoryCountdownSeconds > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = "${gameState.puzzleFilledCount}/${gameState.puzzleTargetCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (gameState.memoryCountdownSeconds > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ─────────────────────────────────────────────────────────────
                // PLAYFIELD ROW: LEFT WING (HOLD) + CENTER BOARD + RIGHT WING (NEXT)
                // ─────────────────────────────────────────────────────────────
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val totalH = maxHeight
                    val totalW = maxWidth
                    val sidePanelW = when {
                        totalW < 340.dp -> 54.dp
                        totalW < 380.dp -> 60.dp
                        else -> 66.dp
                    }
                    val hSpacing = if (totalW < 360.dp) 6.dp else 8.dp
                    val maxBoardW = (totalW - (sidePanelW * 2) - (hSpacing * 2)).coerceAtLeast(80.dp)
                    val maxBoardH = (totalH - 4.dp).coerceAtLeast(160.dp)
                    val boardH = minOf(maxBoardH, maxBoardW * 2f)
                    val boardW = boardH * 0.5f

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT WING (HOLD & STATS)
                        Column(
                            modifier = Modifier.width(sidePanelW),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isPerfectionistMode = gameState.gameMode == com.example.game.GameMode.PERFECTIONIST
                        val perfectionistHint = remember(gameState.grid, gameState.currentPiece, gameState.holdPiece, gameState.hasHeldThisTurn) {
                            if (isPerfectionistMode && gameState.currentPiece != null) {
                                viewModel.gameEngine.calculateOptimalPlacement(
                                    grid = gameState.grid,
                                    piece = gameState.currentPiece!!,
                                    holdPiece = gameState.holdPiece,
                                    nextPiece = gameState.nextPieces.firstOrNull(),
                                    canHold = !gameState.hasHeldThisTurn
                                )
                            } else null
                        }
                        val isHoldRecommended = isPerfectionistMode && perfectionistHint?.shouldHold == true && !gameState.hasHeldThisTurn

                        // MD3 Expressive HOLD Card
                        val holdBorder = when {
                            isHoldRecommended -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            gameState.hasHeldThisTurn -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = holdBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.triggerAudioFeedback("click")
                                    viewModel.gameEngine.hold()
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Translations.get("hold", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Black,
                                    color = if (gameState.hasHeldThisTurn) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (gameState.holdPiece != null) {
                                        PreviewNextPiece(gameState.holdPiece!!, blockStyle, graphicsQuality = graphicsQuality)
                                    } else {
                                        Text(
                                            text = Translations.get("tap", currentLang),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }

                        // Lines Cleared Chip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Translations.get("lines", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${gameState.lines}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Pieces Placed Chip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Translations.get("pieces", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${gameState.piecesPlaced}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(hSpacing))

                    // ── CENTRAL BOARD MATRIX ──
                    Box(
                        modifier = Modifier
                            .size(width = boardW, height = boardH)
                            .offset(x = shakeX, y = shakeY)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(
                                BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameBoardView(
                            gameState = gameState,
                            blockStyle = blockStyle,
                            ghostVisible = ghostVisible,
                            ghostOutlineOnly = ghostOutlineOnly,
                            smoothFallingEnabled = smoothFallingEnabled,
                            gridLineDensity = gridLineDensity,
                            boardColorSkin = boardColorSkin,
                            graphicsQuality = graphicsQuality,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Danger Ceiling Alert
                        if (isDangerCeiling) {
                            val hazardTransition = rememberInfiniteTransition(label = "HazardPulse")
                            val hazardAlpha by hazardTransition.animateFloat(
                                initialValue = 0.35f,
                                targetValue = 0.95f,
                                animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                                label = "HazardAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = hazardAlpha))
                            )
                        }

                        // Memory Mode 3-2-1 Countdown Badge
                        androidx.compose.animation.AnimatedVisibility(
                            visible = gameState.gameMode == com.example.game.GameMode.MEMORY_PUZZLE && gameState.memoryCountdownSeconds > 0,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f),
                                shadowElevation = 8.dp,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${gameState.memoryCountdownSeconds}",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(hSpacing))

                    // RIGHT WING (NEXT QUEUE & TIMER/BADGES)
                    Column(
                        modifier = Modifier.width(sidePanelW),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // MD3 Expressive NEXT Queue Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Translations.get("next", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Primary next piece
                                val firstNext = gameState.nextPieces.firstOrNull()
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (firstNext != null) {
                                        PreviewNextPiece(firstNext, blockStyle, graphicsQuality = graphicsQuality)
                                    }
                                }
                                // Subsequent queue mini previews
                                val subsequent = gameState.nextPieces.drop(1).take(minOf(2, nextCount - 1))
                                if (subsequent.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    subsequent.forEach { piece ->
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(vertical = 1.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PreviewNextPiece(piece, blockStyle, graphicsQuality = graphicsQuality)
                                        }
                                    }
                                }
                            }
                        }

                        // Time Attack Countdown or Tetrises Cleared Badge
                        if (gameState.gameMode == com.example.game.GameMode.TIME_ATTACK) {
                            val isUrgent = gameState.timeRemainingSeconds <= 15
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    val minutes = gameState.timeRemainingSeconds / 60
                                    val seconds = gameState.timeRemainingSeconds % 60
                                    val minStr = if (minutes < 10) "0$minutes" else "$minutes"
                                    val secStr = if (seconds < 10) "0$seconds" else "$seconds"
                                    Text(
                                        text = "$minStr:$secStr",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        } else if (gameState.tetrisesCleared > 0) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "x${gameState.tetrisesCleared}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

                // ─────────────────────────────────────────────────────────────
                // CONTROLS SECTION (Standard configurable controls)
                // ─────────────────────────────────────────────────────────────
                if (!gameState.isGameOver) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (controlVerticalPosition == "middle") 60.dp else (controlBottomPadding.coerceAtLeast(6)).dp),
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
                }
            }
        }
    }
}
