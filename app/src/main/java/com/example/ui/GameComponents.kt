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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val gameState by viewModel.gameEngine.gameState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()

    val blockStyle by viewModel.blockStyle.collectAsStateWithLifecycle()
    val ghostVisible by viewModel.ghostVisible.collectAsStateWithLifecycle()
    val ghostOutlineOnly by viewModel.ghostOutlineOnly.collectAsStateWithLifecycle()
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
    val graphicsQuality by viewModel.graphicsQuality.collectAsStateWithLifecycle()

    val themeColorKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val themeColor = remember(themeColorKey) {
        when (themeColorKey) {
            "black" -> Color(0xFFE2E2E6)
            "cyan", "neon" -> Color(0xFF00FFCC)
            "purple", "indigo" -> Color(0xFFD0BCFF)
            "pink", "rose" -> Color(0xFFF43F5E)
            "gold", "amber" -> Color(0xFFFFD700)
            "emerald" -> Color(0xFF10B981)
            "sky" -> Color(0xFF0EA5E9)
            "red" -> Color(0xFFFF5555)
            "toxic_green" -> Color(0xFF39FF14)
            "cyber_pink" -> Color(0xFFFF007F)
            else -> Color(0xFF6366F1)
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

    BackHandler(enabled = isPlaying && !gameState.isGameOver) {
        viewModel.pauseGame()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.pauseGame()
        }
    }

    val newGameUiEnabled by viewModel.newGameUiEnabled.collectAsStateWithLifecycle()

    if (newGameUiEnabled) {
        ModernGameLayout(
            viewModel = viewModel,
            gameState = gameState,
            isPlaying = isPlaying,
            currentLang = currentLang,
            themeColor = themeColor,
            statsHighScore = statsHighScore,
            shakeX = shakeX,
            shakeY = shakeY,
            blockStyle = blockStyle,
            ghostVisible = ghostVisible,
            ghostOutlineOnly = ghostOutlineOnly,
            nextCount = nextCount,
            controlStyle = controlStyle,
            smoothFallingEnabled = smoothFallingEnabled,
            gridOpacity = gridOpacity,
            leftHandedControls = leftHandedControls,
            controlButtonScale = controlButtonScale,
            controlButtonStyle = controlButtonStyle,
            gridLineDensity = gridLineDensity,
            controlVerticalPosition = controlVerticalPosition,
            scanlinesFilter = scanlinesFilter,
            boardColorSkin = boardColorSkin,
            graphicsQuality = graphicsQuality,
            onBack = onBack
        )
    } else {
        Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "LVL",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                                Text(
                                    text = "${gameState.level}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(14.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "$statsHighScore",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
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
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.pauseGame()
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = Translations.get("menu", currentLang),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (gameState.gameMode == com.example.game.GameMode.RELAX) {
                            var showRelaxDialog by remember { mutableStateOf(false) }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        showRelaxDialog = true
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Settings",
                                        tint = themeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            if (showRelaxDialog) {
                                RelaxSettingsDialog(
                                    viewModel = viewModel,
                                    onDismiss = { showRelaxDialog = false }
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

            if (gameState.gameMode == com.example.game.GameMode.PATTERN_PUZZLE) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                        Text(
                            text = "ШАБЛОН: ${gameState.puzzleGoalDescription} (УР. ${gameState.puzzleLevel})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${gameState.puzzleFilledCount}/${gameState.puzzleTargetCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else if (gameState.gameMode == com.example.game.GameMode.MEMORY_PUZZLE) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (gameState.memoryCountdownSeconds > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (gameState.memoryCountdownSeconds > 0) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (gameState.memoryCountdownSeconds > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
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
                        Text(
                            text = "${gameState.puzzleFilledCount}/${gameState.puzzleTargetCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (gameState.memoryCountdownSeconds > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT PANEL (HOLD & STATS)
                Column(
                    modifier = Modifier.width(82.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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

                    val holdPulseTransition = rememberInfiniteTransition(label = "HoldPulse")
                    val holdPulseAlpha by holdPulseTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(550, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "HoldPulseAlpha"
                    )

                    // HOLD BOX
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isHoldRecommended) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isHoldRecommended) Icons.Default.AutoAwesome else Icons.Default.SaveAlt,
                                    contentDescription = null,
                                    tint = if (isHoldRecommended) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                AdaptiveText(
                                    text = if (isHoldRecommended) {
                                        when (currentLang) {
                                            Language.RU -> "ХОЛД!"
                                            Language.UA -> "ХОЛД!"
                                            Language.KK -> "ХОЛД!"
                                            Language.DE -> "HALTEN"
                                            Language.ZH -> "建议暂存"
                                            else -> "HOLD!"
                                        }
                                    } else Translations.get("hold", currentLang).uppercase(),
                                    color = if (isHoldRecommended) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    gameState.holdPiece?.let { PreviewNextPiece(it, blockStyle, graphicsQuality = graphicsQuality) }
                                }
                            }
                        }
                    }

                    if (isPerfectionistMode && perfectionistHint?.actionLabel?.isNotEmpty() == true) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = perfectionistHint.actionLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // LINES STAT CARD
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                                Spacer(modifier = Modifier.width(3.dp))
                                AdaptiveText(
                                    text = Translations.get("lines", currentLang).uppercase(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
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
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (gameState.timeRemainingSeconds <= 15) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                                            MaterialTheme.colorScheme.onErrorContainer
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    AdaptiveText(
                                        text = Translations.get("time", currentLang),
                                        color = if (gameState.timeRemainingSeconds <= 15) {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
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
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // CENTER GAME BOARD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = shakeX, y = shakeY)
                        .aspectRatio(10f / 20f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A0A0E).copy(alpha = gridOpacity.coerceAtLeast(0.85f)))
                        .border(
                            BorderStroke(
                                1.5.dp, 
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ), 
                            RoundedCornerShape(18.dp)
                        )
                        .padding(3.dp)
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
                }

                Spacer(modifier = Modifier.width(10.dp))

                // RIGHT PANEL (NEXT & SCORE)
                Column(
                    modifier = Modifier.width(82.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // NEXT BOX
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
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
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                AdaptiveText(
                                    text = Translations.get("next", currentLang).uppercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val countToShow = minOf(nextCount, gameState.nextPieces.size)
                                    for (i in 0 until countToShow) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .padding(vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PreviewNextPiece(gameState.nextPieces[i], blockStyle, graphicsQuality = graphicsQuality)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SCORE CARD
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                                Spacer(modifier = Modifier.width(3.dp))
                                AdaptiveText(
                                    text = Translations.get("score", currentLang).uppercase(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
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

                    // TETRIS CLEARED BADGE
                    AnimatedVisibility(
                        visible = gameState.tetrisesCleared > 0,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "x${gameState.tetrisesCleared}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

        Spacer(modifier = Modifier.height(16.dp))

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
        }
    }
}
}
    }

    // ═══════════════════════════════════════════════════
    // PAUSE OVERLAY (Zeta / Modern MD3 Full-Width Style)
    // ═══════════════════════════════════════════════════
    if (!isPlaying && !gameState.isGameOver && gameState.currentPiece != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            val scale = remember { Animatable(0.88f) }
            LaunchedEffect(Unit) {
                scale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .widthIn(max = 390.dp)
                    .scale(scale.value)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pause Icon & Title Header
                    // Menu Icon & Title Header
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    val pauseMenuTitle = when (currentLang) {
                        Language.RU -> "МЕНЮ ИГРЫ"
                        Language.UA -> "МЕНЮ ГРИ"
                        Language.KK -> "ОЙЫН МӘЗІРІ"
                        Language.DE -> "SPIELMENÜ"
                        Language.ZH -> "游戏菜单"
                        else -> "GAME MENU"
                    }
                    Text(
                        text = pauseMenuTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Current Stats Strip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = Translations.get("score", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${gameState.score}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = Translations.get("lines", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${gameState.lines}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = Translations.get("level", currentLang).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${gameState.level}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.resumeGame()
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.get("resume", currentLang).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.startGame(gameState.gameMode)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.get("restart", currentLang).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.exitGameToMenu(onBack)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.get("exit_to_menu", currentLang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // GAME OVER OVERLAY (Zeta / Modern MD3 Full-Width Style)
    // ═══════════════════════════════════════════════════
    if (gameState.isGameOver) {
        val isNewRecord = gameState.score > statsHighScore && gameState.score > 0
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            val scale = remember { Animatable(0.85f) }
            LaunchedEffect(Unit) {
                scale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 410.dp)
                    .scale(scale.value)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Header Badge
                    Surface(
                        shape = CircleShape,
                        color = if (isNewRecord) Color(0xFFFFD700).copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isNewRecord) Icons.Default.EmojiEvents else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isNewRecord) Color(0xFFFFD700) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isNewRecord) Translations.get("new_record", currentLang)
                                   else Translations.get("game_over", currentLang).uppercase(),
                            color = if (isNewRecord) Color(0xFFFFD700) else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        if (isNewRecord) {
                            Text(
                                text = Translations.get("incredible_performance", currentLang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Final Score Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(
                            1.5.dp,
                            if (isNewRecord) Color(0xFFFFD700).copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = Translations.get("final_score", currentLang),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${gameState.score}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isNewRecord) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Stats Summary Row
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = Translations.get("lines", currentLang),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${gameState.lines}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(22.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SignalCellularAlt, contentDescription = null, modifier = Modifier.size(12.dp), tint = themeColor)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = Translations.get("level", currentLang),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${gameState.level}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(22.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFF5722))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = Translations.get("tetrises", currentLang),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "x${gameState.tetrisesCleared}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.startGame(gameState.gameMode)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.get("play_again", currentLang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                onBack()
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.get("main_menu", currentLang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

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
                .padding(padding)
                .navigationBarsPadding(),
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
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ─────────────────────────────────────────────────────────────
                // PLAYFIELD ROW: LEFT WING (HOLD) + CENTER BOARD + RIGHT WING (NEXT)
                // ─────────────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT WING (HOLD & STATS) - 66dp
                    Column(
                        modifier = Modifier.width(66.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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

                    Spacer(modifier = Modifier.width(8.dp))

                    // ── CENTRAL BOARD MATRIX ──
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .offset(x = shakeX, y = shakeY)
                            .aspectRatio(10f / 20f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(
                                BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(2.dp)
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
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // RIGHT WING (NEXT QUEUE & TIMER/BADGES) - 66dp
                    Column(
                        modifier = Modifier.width(66.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        color = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
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

                Spacer(modifier = Modifier.height(10.dp))

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
    val controlBottomPadding by viewModel.controlBottomPadding.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = controlBottomPadding.dp)
    ) {
        when (controlStyle) {
            "split" -> {
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
                        .padding(horizontal = 20.dp),
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
            }
            "arcade" -> {
                val dpadCol = @Composable {
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
                val actionCol = @Composable {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leftHandedControls) {
                        actionCol()
                        Spacer(modifier = Modifier.width(16.dp))
                        dpadCol()
                    } else {
                        dpadCol()
                        Spacer(modifier = Modifier.width(16.dp))
                        actionCol()
                    }
                }
            }
            "one_hand_right" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale * 0.85f, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "rotate", onClick = onRotatePress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ControlButton(actionType = "left", onClick = onLeftPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "down", onClick = onDownPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "right", onClick = onRightPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        }
                    }
                }
            }
            "one_hand_left" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "rotate", onClick = onRotatePress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale * 0.85f, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ControlButton(actionType = "left", onClick = onLeftPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "down", onClick = onDownPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                            ControlButton(actionType = "right", onClick = onRightPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        }
                    }
                }
            }
            "claw_pro" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale * 0.9f, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        ControlButton(actionType = "rotate_ccw", onClick = { viewModel.gameEngine.rotate() }, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        ControlButton(actionType = "rotate", onClick = onRotatePress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ControlButton(actionType = "left", onClick = onLeftPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        Spacer(modifier = Modifier.width(10.dp))
                        ControlButton(actionType = "down", onClick = onDownPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        Spacer(modifier = Modifier.width(10.dp))
                        ControlButton(actionType = "right", onClick = onRightPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                    }
                }
            }
            "swipe_hybrid" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .height((76 * controlButtonScale).dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onRotatePress() }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onLeftPress) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Left", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = onDownPress) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Soft Drop", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = onRightPress) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Right", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale * 0.9f, buttonStyle = controlButtonStyle, viewModel = viewModel)
                    }
                }
            }
            else -> { // classic
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val listBtns = listOf(
                        @Composable { ControlButton(actionType = "left", onClick = onLeftPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
                        @Composable { ControlButton(actionType = "down", onClick = onDownPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
                        @Composable { ControlButton(actionType = "rotate", onClick = onRotatePress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) },
                        @Composable { ControlButton(actionType = "right", onClick = onRightPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel) }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (leftHandedControls) {
                            listBtns.reversed().forEach { it() }
                        } else {
                            listBtns.forEach { it() }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ControlButton(actionType = "drop", onClick = onHardDropPress, isPrimary = true, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                        Spacer(modifier = Modifier.width(16.dp))
                        ControlButton(actionType = "hold", onClick = onHoldPress, scale = controlButtonScale, buttonStyle = controlButtonStyle, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewNextPiece(piece: Tetromino, style: String, graphicsQuality: String = "medium") {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / 4f
        val minX = piece.shape.minOf { it.x }
        val maxX = piece.shape.maxOf { it.x }
        val minY = piece.shape.minOf { it.y }
        val maxY = piece.shape.maxOf { it.y }
        val pieceWidth = (maxX - minX + 1) * cellSize
        val pieceHeight = (maxY - minY + 1) * cellSize
        val offsetX = (size.width - pieceWidth) / 2f - minX * cellSize
        val offsetY = (size.height - pieceHeight) / 2f - minY * cellSize

        piece.shape.forEach { p ->
            val color = Colors.getOrElse(piece.colorIndex) { Color.Gray }
            val x = p.x * cellSize + offsetX
            val y = p.y * cellSize + offsetY
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
    graphicsQuality: String = "medium",
    ghostOutlineOnly: Boolean = true,
    viewModel: MainViewModel? = null,
    modifier: Modifier = Modifier
) {
    val isHighOrUltra = graphicsQuality == "high" || graphicsQuality == "ultra"

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

    // Smooth Angular Motion Blur on rotation for High/Ultra graphics
    var prevPieceShape by remember { mutableStateOf<List<Position>?>(null) }
    var prevPieceColor by remember { mutableIntStateOf(0) }
    val rotationAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(gameState.currentPiece?.shape) {
        val cur = gameState.currentPiece
        if (cur != null) {
            val oldShape = prevPieceShape
            if (oldShape != null && oldShape != cur.shape && prevPieceColor == cur.colorIndex && isHighOrUltra) {
                rotationAnim.snapTo(-90f)
                rotationAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 110,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                )
            }
            prevPieceShape = cur.shape
            prevPieceColor = cur.colorIndex
        }
    }

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
                            style = blockStyle
                        )
                    }
                }
            }
        }
        // Режим «Шаблон» и «Память»: трафарет целевой фигуры
        val showStencil = when (gameState.gameMode) {
            com.example.game.GameMode.PATTERN_PUZZLE -> true
            com.example.game.GameMode.MEMORY_PUZZLE -> !gameState.isMemoryHidden
            else -> false
        }
        if (showStencil && gameState.patternTargets.isNotEmpty()) {
            gameState.patternTargets.forEach { target ->
                val tx = target.x
                val ty = target.y - 2
                if (ty >= 0 && tx in 0 until cols) {
                    val drawTx = if (isMirror) cols - 1 - tx else tx
                    val isFilled = target.y in 0 until gameState.grid.size && 
                                   tx in 0 until gameState.grid[target.y].size && 
                                   gameState.grid[target.y][tx] != 0

                    val stencilFill = if (isFilled) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.08f)
                    drawRoundRect(
                        color = stencilFill,
                        topLeft = Offset(drawTx * cellSize + 2f, ty * cellSize + 2f),
                        size = Size(cellSize - 4f, cellSize - 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.Gray.copy(alpha = if (isFilled) 0.60f else 0.35f),
                        topLeft = Offset(drawTx * cellSize + 2f, ty * cellSize + 2f),
                        size = Size(cellSize - 4f, cellSize - 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 0.8.dp.toPx()
                        )
                    )
                }
            }
        }

        // AI Perfect Placement Guide for Perfectionist Mode
        if (gameState.gameMode == com.example.game.GameMode.PERFECTIONIST && viewModel != null) {
            gameState.currentPiece?.let { piece ->
                val hint = viewModel.gameEngine.calculateOptimalPlacement(
                    grid = gameState.grid,
                    piece = piece,
                    holdPiece = gameState.holdPiece,
                    nextPiece = gameState.nextPieces.firstOrNull(),
                    canHold = !gameState.hasHeldThisTurn
                )
                hint?.let { optimal ->
                    val isHoldAdvised = optimal.shouldHold && !gameState.hasHeldThisTurn
                    val guideColor = when {
                        isHoldAdvised -> Color(0xFFFF9100)
                        optimal.actionLabel == "ТЕТРИС!" -> Color(0xFF00E676)
                        optimal.actionLabel == "T-SPIN" -> Color(0xFFE040FB)
                        else -> Color(0xFFFFD700)
                    }
                    optimal.shape.forEach { p ->
                        val hx = optimal.targetPos.x + p.x
                        val hy = optimal.targetPos.y + p.y - 2
                        if (hy >= 0 && hx in 0 until cols) {
                            val drawHx = if (isMirror) cols - 1 - hx else hx
                            // 1. Subtle Fill
                            drawRoundRect(
                                color = guideColor.copy(alpha = if (isHoldAdvised) 0.14f else 0.20f),
                                topLeft = Offset(drawHx * cellSize + 1.5f, hy * cellSize + 1.5f),
                                size = Size(cellSize - 3f, cellSize - 3f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            // 2. Crisp Border (thin, semi-transparent gray, no center dot)
                            drawRoundRect(
                                color = Color.Gray.copy(alpha = 0.40f),
                                topLeft = Offset(drawHx * cellSize + 1.5f, hy * cellSize + 1.5f),
                                size = Size(cellSize - 3f, cellSize - 3f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8.dp.toPx())
                            )
                        }
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
                        if (ghostOutlineOnly) {
                            val ghostColor = Color.Gray.copy(alpha = 0.35f)
                            drawRoundRect(
                                color = ghostColor,
                                topLeft = Offset(drawNx * cellSize + 1.2f, ny * cellSize + 1.2f),
                                size = Size(cellSize - 2.4f, cellSize - 2.4f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8.dp.toPx())
                            )
                        } else {
                            drawBlock(
                                color = Colors.getOrElse(piece.colorIndex) { Color.Gray }.copy(alpha = 0.16f),
                                x = drawNx * cellSize,
                                y = ny * cellSize,
                                size = cellSize,
                                style = blockStyle
                            )
                        }
                    }
                }
            }

            val posX = if (smoothFallingEnabled) animatedX else gameState.currentPos.x.toFloat()
            val posY = if (smoothFallingEnabled) animatedY else gameState.currentPos.y.toFloat()
            val pieceColor = Colors.getOrElse(piece.colorIndex) { Color.Gray }
            val rotAngle = rotationAnim.value
            val isRotating = isHighOrUltra && rotAngle.absoluteValue > 0.5f

            val pivotX = (posX + piece.pivot.x) * cellSize + cellSize / 2f
            val pivotY = (posY + piece.pivot.y - 2) * cellSize + cellSize / 2f
            val pivotPoint = Offset(if (isMirror) size.width - pivotX else pivotX, pivotY)

            // High/Ultra Angular Motion Blur Trails
            if (isRotating) {
                val blurFraction = rotAngle.absoluteValue / 90f // 1.0 down to 0.0

                // Ghost Trail 1 (furthest back)
                rotate(degrees = rotAngle * 0.65f, pivot = pivotPoint) {
                    piece.shape.forEach { p ->
                        val nx = (posX + p.x)
                        val ny = (posY + p.y - 2)
                        if (ny >= 0) {
                            val drawNx = if (isMirror) cols - 1 - nx else nx
                            drawBlock(
                                color = pieceColor.copy(alpha = 0.20f * blurFraction),
                                x = drawNx * cellSize,
                                y = ny * cellSize,
                                size = cellSize,
                                style = blockStyle
                            )
                        }
                    }
                }

                // Ghost Trail 2 (closer)
                rotate(degrees = rotAngle * 0.35f, pivot = pivotPoint) {
                    piece.shape.forEach { p ->
                        val nx = (posX + p.x)
                        val ny = (posY + p.y - 2)
                        if (ny >= 0) {
                            val drawNx = if (isMirror) cols - 1 - nx else nx
                            drawBlock(
                                color = pieceColor.copy(alpha = 0.38f * blurFraction),
                                x = drawNx * cellSize,
                                y = ny * cellSize,
                                size = cellSize,
                                style = blockStyle
                            )
                        }
                    }
                }
            }

            // Current Piece (smoothly spinning into 0 degrees)
            rotate(degrees = if (isHighOrUltra) rotAngle else 0f, pivot = pivotPoint) {
                piece.shape.forEach { p ->
                    val nx = (posX + p.x)
                    val ny = (posY + p.y - 2)
                    if (ny >= 0) {
                        val drawNx = if (isMirror) cols - 1 - nx else nx
                        drawBlock(
                            color = pieceColor,
                            x = drawNx * cellSize,
                            y = ny * cellSize,
                            size = cellSize,
                            style = blockStyle
                        )
                    }
                }
            }
        }
    }
}

private fun isValidGhostMove(pos: Position, piece: Tetromino, grid: List<IntArray>): Boolean {
    return piece.shape.all { p ->
        val nx = pos.x + p.x
        val ny = pos.y + p.y
        nx in 0 until 10 && ny < 22 && (ny < 0 || grid.getOrNull(ny)?.getOrNull(nx) == 0)
    }
}

internal fun DrawScope.drawBlock(
    color: Color,
    x: Float,
    y: Float,
    size: Float,
    style: String
) {
    val pad = 1f
    val bSize = size - pad * 2

    when {
        style == "flat" -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1f)
            )
        }
        style == "glass" -> {
            drawRoundRect(
                color = color.copy(alpha = 0.8f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(x + pad + 2f, y + pad + 2f),
                size = Size(bSize / 3f, bSize / 3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
        style == "retro" -> {
            drawRect(color = color, topLeft = Offset(x, y), size = Size(size, size))
            drawRect(color = Color.White.copy(alpha = 0.45f), topLeft = Offset(x + 2f, y + 2f), size = Size(size - 4f, size - 4f), style = Stroke(width = 1.5f))
            drawRect(color = Color.Black.copy(alpha = 0.45f), topLeft = Offset(x + 5f, y + 5f), size = Size(size - 10f, size - 10f))
        }
        style == "material" -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
                style = Stroke(width = 1f)
            )
        }
        style == "glowing_jewel" -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(x + pad + 1.5f, y + pad + 1.5f),
                size = Size(bSize - 3f, bSize - 3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.5f)
            )
        }
        style == "steampunk" -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFFD4AF37).copy(alpha = 0.85f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                style = Stroke(width = 1.5f)
            )
        }
        style == "red_gradient" -> {
            val brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFFFF1744), Color(0xFFFF5252), Color(0xFFB71C1C)),
                start = Offset(x + pad, y + pad),
                end = Offset(x + pad + bSize, y + pad + bSize)
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color(0xFFFF8A80).copy(alpha = 0.85f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(x + pad + 2f, y + pad + 2f),
                size = Size(bSize / 2.8f, bSize / 2.8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
            )
        }
        style == "green_gradient" -> {
            val brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFF00E676), Color(0xFF69F0AE), Color(0xFF1B5E20)),
                start = Offset(x + pad, y + pad),
                end = Offset(x + pad + bSize, y + pad + bSize)
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color(0xFFB9F6CA).copy(alpha = 0.85f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(x + pad + 2f, y + pad + 2f),
                size = Size(bSize / 2.8f, bSize / 2.8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
            )
        }
        style == "blue_gradient" -> {
            val brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFF00B0FF), Color(0xFF40C4FF), Color(0xFF0D47A1)),
                start = Offset(x + pad, y + pad),
                end = Offset(x + pad + bSize, y + pad + bSize)
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color(0xFF80D8FF).copy(alpha = 0.85f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(x + pad + 2f, y + pad + 2f),
                size = Size(bSize / 2.8f, bSize / 2.8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
            )
        }
        style == "purple_gradient" -> {
            val brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFFD500F9), Color(0xFFE040FB), Color(0xFF4A148C)),
                start = Offset(x + pad, y + pad),
                end = Offset(x + pad + bSize, y + pad + bSize)
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color(0xFFEA80FC).copy(alpha = 0.85f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(x + pad + 2f, y + pad + 2f),
                size = Size(bSize / 2.8f, bSize / 2.8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
            )
        }
        style.contains("gradient") -> {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(
                android.graphics.Color.argb(255, (color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt()),
                hsv
            )
            val c1 = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], (hsv[1] * 0.7f).coerceIn(0.2f, 1f), 1f)))
            val c2 = color
            val c3 = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], 1f, (hsv[2] * 0.6f).coerceIn(0.15f, 1f))))
            val brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(c1, c2, c3),
                start = Offset(x + pad, y + pad),
                end = Offset(x + pad + bSize, y + pad + bSize)
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = c1.copy(alpha = 0.85f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(x + pad + 2f, y + pad + 2f),
                size = Size(bSize / 2.8f, bSize / 2.8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
            )
        }
        else -> { // "neon" / default
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.75f),
                topLeft = Offset(x + pad + 1.5f, y + pad + 1.5f),
                size = Size(bSize - 3f, bSize - 3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                style = Stroke(width = 1.2f)
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

    val dasDelay = viewModel?.controlDas?.collectAsStateWithLifecycle()?.value ?: 160
    val arrDelay = viewModel?.controlArr?.collectAsStateWithLifecycle()?.value ?: 35

    if (isPressed && canRepeat && isPlaying) {
        val currentOnClick by rememberUpdatedState(onClick)
        LaunchedEffect(isPressed) {
            delay(dasDelay.toLong())
            while (isPressed) {
                currentOnClick()
                val repeatDelay = if (actionType == "down") (arrDelay * 0.75f).toLong().coerceAtLeast(16L) else arrDelay.toLong()
                delay(repeatDelay)
            }
        }
    }

    val buttonSize = (56 * scale).dp
    val buttonCorner = (28 * scale).dp

    val backgroundBrush = when (buttonStyle) {
        "gold_legendary", "gold" -> {
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = if (isPressed) {
                    listOf(Color(0xFFFFB300), Color(0xFFFF8F00))
                } else {
                    listOf(Color(0xFFFFD54F).copy(alpha = 0.55f), Color(0xFFFF8F00).copy(alpha = 0.35f))
                }
            )
        }
        "plasma_legendary", "plasma" -> {
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = if (isPressed) {
                    listOf(Color(0xFF7C4DFF).copy(alpha = 0.65f), Color(0xFF00E5FF).copy(alpha = 0.55f))
                } else {
                    listOf(Color(0xFF7C4DFF).copy(alpha = 0.35f), Color(0xFF00E5FF).copy(alpha = 0.25f))
                }
            )
        }
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
        "gold_legendary", "gold" -> {
            androidx.compose.foundation.BorderStroke(
                width = if (isPressed) 2.5.dp else 1.8.dp,
                brush = androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFF9C4), Color(0xFFFFA000), Color(0xFFFFD700)))
            )
        }
        "plasma_legendary", "plasma" -> {
            androidx.compose.foundation.BorderStroke(
                width = if (isPressed) 2.5.dp else 1.8.dp,
                brush = androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFE040FB), Color(0xFF00E5FF)))
            )
        }
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
            .scale(scaleFactor)
            .clip(RoundedCornerShape(buttonCorner))
            .background(backgroundBrush)
            .border(borderStroke, RoundedCornerShape(buttonCorner))
            .pointerInput(isPlaying) {
                if (isPlaying) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            onClick()
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
            }
    ) {
        val iconColor = when (buttonStyle) {
            "gold_legendary", "gold" -> Color(0xFFFFD700)
            "plasma_legendary", "plasma" -> Color(0xFF00E5FF)
            "classic" -> if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.primary
        }

        when (actionType) {
            "left" -> Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Left", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "right" -> Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Right", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "down" -> Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "rotate" -> Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotate", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "rotate_ccw" -> Icon(imageVector = Icons.Default.RotateLeft, contentDescription = "Rotate CCW", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "drop" -> Icon(imageVector = Icons.Default.KeyboardDoubleArrowDown, contentDescription = "Hard Drop", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "hold" -> Icon(imageVector = Icons.Default.Inventory2, contentDescription = "Hold", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((24 * scale).dp))
            else -> {
                if (text != null) {
                    Text(
                        text = text,
                        color = iconColor.copy(alpha = finalAlpha),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RelaxSettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val relaxImmortal by viewModel.relaxImmortal.collectAsStateWithLifecycle()
    val relaxBlockSet by viewModel.relaxBlockSet.collectAsStateWithLifecycle()
    val relaxSpeed by viewModel.relaxSpeed.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (currentLang) {
                        Language.RU -> "РЕЛАКС ХАБ"
                        Language.UA -> "РЕЛАКС ХАБ"
                        Language.KK -> "РЕЛАКС ХАБ"
                        Language.DE -> "RELAX-HUB"
                        Language.ZH -> "轻松解压中心"
                        else -> "RELAX HUB"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Бессмертие
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Бессмертие (без Game Over)"
                                    Language.UA -> "Безсмертя (без Game Over)"
                                    Language.KK -> "Өлместік (Game Over жоқ)"
                                    Language.DE -> "Unsterblichkeit (Kein Game Over)"
                                    Language.ZH -> "无限永生 (无游戏结束)"
                                    else -> "Immortal Mode (No Game Over)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Очистка поля при переполнении"
                                    Language.UA -> "Очищення поля при переповненні"
                                    Language.KK -> "Толған кезде алаңды тазалау"
                                    Language.DE -> "Automatisches Leeren bei Überlauf"
                                    Language.ZH -> "顶部溢出时自动清空顶格"
                                    else -> "Auto cleans board on overflow"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = relaxImmortal,
                            onCheckedChange = { viewModel.setRelaxImmortal(it) }
                        )
                    }
                }

                // 2. Набор фигур
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Набор фигур:"
                            Language.UA -> "Набір фігур:"
                            Language.KK -> "Пішіндер жиынтығы:"
                            Language.DE -> "Figuren-Set:"
                            Language.ZH -> "方块组合:"
                            else -> "Block Set:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val blockSets = listOf(
                        "only_i" to when (currentLang) {
                            Language.RU -> "Только палки (I-Only)"
                            Language.UA -> "Тільки палиці (I-Only)"
                            Language.KK -> "Тек таяқшалар"
                            Language.DE -> "Nur I-Balken"
                            Language.ZH -> "纯长条天堂"
                            else -> "Only I-Bars"
                        },
                        "ideal" to when (currentLang) {
                            Language.RU -> "Идеальный баланс (I, O, T)"
                            Language.UA -> "Ідеальний баланс"
                            Language.KK -> "Керемет баланс"
                            Language.DE -> "Ideale Balance"
                            Language.ZH -> "极简易搭组合"
                            else -> "Ideal Balance"
                        },
                        "standard" to when (currentLang) {
                            Language.RU -> "Классика (7 фигур)"
                            Language.UA -> "Класика (7 фігур)"
                            Language.KK -> "Классика (7 пішін)"
                            Language.DE -> "Standard (7 Figuren)"
                            Language.ZH -> "标准7种图形"
                            else -> "Standard 7"
                        },
                        "all" to when (currentLang) {
                            Language.RU -> "Все 10 фигур (с пентамино)"
                            Language.UA -> "Всі 10 фігур"
                            Language.KK -> "Барлық 10 пішін"
                            Language.DE -> "Alle 10 Figuren"
                            Language.ZH -> "全部10种异形"
                            else -> "All 10 Shapes"
                        }
                    )
                    blockSets.forEach { (key, label) ->
                        val isSelected = relaxBlockSet == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRelaxBlockSet(key)
                                    viewModel.triggerAudioFeedback("click")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setRelaxBlockSet(key)
                                        viewModel.triggerAudioFeedback("click")
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 3. Скорость гравитации
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Гравитация падения:"
                            Language.UA -> "Гравітація падіння:"
                            Language.KK -> "Құлау жылдамдығы:"
                            Language.DE -> "Fall-Geschwindigkeit:"
                            Language.ZH -> "下落重力:"
                            else -> "Gravity Speed:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val speedOptions = listOf(
                        "static" to when (currentLang) {
                            Language.RU -> "Без гравитации (ручной сброс)"
                            Language.UA -> "Без гравітації"
                            Language.KK -> "Гравитациясыз"
                            Language.DE -> "Keine Gravitation"
                            Language.ZH -> "悬浮静止模式"
                            else -> "Zero Gravity (Manual)"
                        },
                        "slow" to when (currentLang) {
                            Language.RU -> "Медитативная (1.5 сек)"
                            Language.UA -> "Медитативна"
                            Language.KK -> "Медитативті"
                            Language.DE -> "Meditativ (Langsam)"
                            Language.ZH -> "冥想超缓流速"
                            else -> "Meditative (Slow)"
                        },
                        "flow" to when (currentLang) {
                            Language.RU -> "Плавный поток (0.9 сек)"
                            Language.UA -> "Плавний потік"
                            Language.KK -> "Бірқалыпты ағын"
                            Language.DE -> "Sanfter Fluss"
                            Language.ZH -> "平缓禅意流速"
                            else -> "Smooth Flow (Normal)"
                        }
                    )
                    speedOptions.forEach { (key, label) ->
                        val isSelected = relaxSpeed == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRelaxSpeed(key)
                                    viewModel.triggerAudioFeedback("click")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setRelaxSpeed(key)
                                        viewModel.triggerAudioFeedback("click")
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 4. Манипуляции с полем
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Манипуляции с полем:"
                            Language.UA -> "Дії з полем:"
                            Language.KK -> "Алаң әрекеттері:"
                            Language.DE -> "Feld-Aktionen:"
                            Language.ZH -> "棋盘快捷操作:"
                            else -> "Field Actions:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearRelaxLowerRows()
                                viewModel.triggerAudioFeedback("clear")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Срезать низ (8)"
                                    Language.UA -> "Зрізати низ (8)"
                                    Language.KK -> "Астын кесу (8)"
                                    Language.DE -> "Unten (8) leeren"
                                    Language.ZH -> "清空底部8行"
                                    else -> "Cut Lower 8"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.clearRelaxBoard()
                                viewModel.triggerAudioFeedback("clear")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Очистить всё"
                                    Language.UA -> "Очистити все"
                                    Language.KK -> "Барлығын тазалау"
                                    Language.DE -> "Alles leeren"
                                    Language.ZH -> "清空整盘"
                                    else -> "Clear Board"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) {
                Text(Translations.get("done", currentLang), fontWeight = FontWeight.Bold)
            }
        }
    )
}
