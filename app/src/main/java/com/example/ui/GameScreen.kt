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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthDp = configuration.screenWidthDp
    val isCompactScreen = screenHeightDp < 680
    val isUltraCompact = screenHeightDp < 600

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

    var showExitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !gameState.isGameOver) {
        viewModel.pauseGame()
        showExitConfirmDialog = true
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitConfirmDialog = false
                viewModel.resumeGame()
            },
            title = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Пауза / Выход"
                        Language.UA -> "Пауза / Вихід"
                        Language.KK -> "Кідіріс / Шығу"
                        Language.DE -> "Pause / Beenden"
                        Language.ZH -> "暂停 / 退出游戏"
                        else -> "Pause / Exit"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Игра приостановлена. Выйти в главное меню? Текущий прогресс матча будет зафиксирован."
                        Language.UA -> "Гру призупинено. Вийти в головне меню? Поточний прогрес матчу буде зафіксовано."
                        Language.KK -> "Ойын кідіртілді. Басты мәзірге шығу керек пе?"
                        Language.DE -> "Spiel pausiert. Zum Hauptmenü zurückkehren?"
                        Language.ZH -> "游戏已暂停。是否返回主菜单？当前得分将会结算。"
                        else -> "Game paused. Return to main menu? Current score will be saved."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.exitGameToMenu(onBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        when (currentLang) {
                            Language.RU -> "Выйти"
                            Language.UA -> "Вийти"
                            Language.KK -> "Шығу"
                            Language.DE -> "Beenden"
                            Language.ZH -> "退出"
                            else -> "Exit"
                        }
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.resumeGame()
                    }
                ) {
                    Text(
                        when (currentLang) {
                            Language.RU -> "Продолжить"
                            Language.UA -> "Продовжити"
                            Language.KK -> "Жалғастыру"
                            Language.DE -> "Fortsetzen"
                            Language.ZH -> "继续游戏"
                            else -> "Resume"
                        }
                    )
                }
            }
        )
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
                .padding(padding),
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

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = if (isUltraCompact) 4.dp else 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val totalH = maxHeight
                val totalW = maxWidth
                val sidePanelW = when {
                    totalW < 340.dp -> 60.dp
                    totalW < 380.dp -> 70.dp
                    else -> 82.dp
                }
                val hSpacing = if (totalW < 360.dp) 6.dp else 10.dp
                val maxBoardW = (totalW - (sidePanelW * 2) - (hSpacing * 2)).coerceAtLeast(80.dp)
                val maxBoardH = (totalH - 4.dp).coerceAtLeast(160.dp)
                val boardH = minOf(maxBoardH, maxBoardW * 2f)
                val boardW = boardH * 0.5f

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT PANEL (HOLD & STATS)
                    Column(
                        modifier = Modifier.width(sidePanelW),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isCompactScreen) 6.dp else 10.dp)
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

                Spacer(modifier = Modifier.width(hSpacing))

                // CENTER GAME BOARD
                Box(
                    modifier = Modifier
                        .size(width = boardW, height = boardH)
                        .offset(x = shakeX, y = shakeY)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A0A0E).copy(alpha = gridOpacity.coerceAtLeast(0.85f)))
                        .border(
                            BorderStroke(
                                1.5.dp, 
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ), 
                            RoundedCornerShape(18.dp)
                        )
                        .padding(3.dp),
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
                            modifier = Modifier.size(68.dp)
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

                // RIGHT PANEL (NEXT & SCORE)
                Column(
                    modifier = Modifier.width(sidePanelW),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isCompactScreen) 6.dp else 10.dp)
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
        }

        Spacer(modifier = Modifier.height(if (isCompactScreen) 6.dp else 14.dp))

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

