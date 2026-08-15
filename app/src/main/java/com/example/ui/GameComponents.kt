package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.absoluteValue
import androidx.compose.ui.text.font.FontWeight
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
                                viewModel.pauseGame()
                                onBack()
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
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
                                        contentDescription = "Relax Mode Settings",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
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

                        if (!gameState.isGameOver && gameState.currentPiece != null) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPlaying) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primary
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        if (isPlaying) {
                                            viewModel.pauseGame()
                                        } else {
                                            viewModel.resumeGame()
                                        }
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Resume",
                                        tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                val success = viewModel.saveCurrentGame()
                                saveStatusMessage = if (success) {
                                    Translations.get("save_success", currentLang)
                                } else {
                                    Translations.get("save_fail", currentLang)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

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
                    // HOLD BOX
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
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
                                    imageVector = Icons.Default.SaveAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                AdaptiveText(
                                    text = Translations.get("hold", currentLang).uppercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
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

                    // LINES STAT CARD
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = if (currentLang == Language.RU) "ЛИНИИ" else "LINES",
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
                            modifier = Modifier.fillMaxWidth(),
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
                                        text = if (currentLang == Language.RU) "ВРЕМЯ" else "TIME",
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
                                2.dp, 
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(themeColor.copy(alpha = 0.8f), themeColor.copy(alpha = 0.2f))
                                )
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

                    // PAUSE OVERLAY
                    if (!isPlaying && !gameState.isGameOver && gameState.currentPiece != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center
                        ) {
                            ElevatedCard(
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                elevation = CardDefaults.elevatedCardElevation(6.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Pause,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (currentLang == Language.RU) "ИГРА НА ПАУЗЕ" else "GAME PAUSED",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Button(
                                            onClick = { viewModel.resumeGame() },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth(0.85f)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentLang == Language.RU) "ПРОДОЛЖИТЬ" else "RESUME",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.startGame(gameState.gameMode)
                                                viewModel.triggerAudioFeedback("click")
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth(0.85f)
                                        ) {
                                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentLang == Language.RU) "ЗАНОВО" else "RESTART",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // GAME OVER OVERLAY
                    if (gameState.isGameOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.82f)),
                            contentAlignment = Alignment.Center
                        ) {
                            ElevatedCard(
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                elevation = CardDefaults.elevatedCardElevation(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = Translations.get("game_over", currentLang).uppercase(),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (currentLang == Language.RU) "ИТОГОВЫЙ СЧЕТ" else "FINAL SCORE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${gameState.score}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { viewModel.startGame(gameState.gameMode) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.85f)
                                    ) {
                                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Translations.get("restart", currentLang).uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
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
                        modifier = Modifier.fillMaxWidth(),
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
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
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
                        modifier = Modifier.fillMaxWidth(),
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
                            val ghostColor = Colors.getOrElse(piece.colorIndex) { Color.White }.copy(alpha = 0.60f)
                            drawRoundRect(
                                color = ghostColor,
                                topLeft = Offset(drawNx * cellSize + 1.2f, ny * cellSize + 1.2f),
                                size = Size(cellSize - 2.4f, cellSize - 2.4f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
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

private fun DrawScope.drawBlock(
    color: Color,
    x: Float,
    y: Float,
    size: Float,
    style: String
) {
    val pad = 1f
    val bSize = size - pad * 2

    when (style) {
        "flat" -> {
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
        "glass" -> {
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
        "retro" -> {
            drawRect(color = color, topLeft = Offset(x, y), size = Size(size, size))
            drawRect(color = Color.White.copy(alpha = 0.45f), topLeft = Offset(x + 2f, y + 2f), size = Size(size - 4f, size - 4f), style = Stroke(width = 1.5f))
            drawRect(color = Color.Black.copy(alpha = 0.45f), topLeft = Offset(x + 5f, y + 5f), size = Size(size - 10f, size - 10f))
        }
        "material" -> {
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
        "glowing_jewel" -> {
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
        "steampunk" -> {
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
        val iconColor = if (buttonStyle == "classic" && isPrimary) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.primary
        }

        when (actionType) {
            "left" -> Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Left", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "right" -> Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Right", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "down" -> Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
            "rotate" -> Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotate", tint = iconColor.copy(alpha = finalAlpha), modifier = Modifier.size((28 * scale).dp))
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
    val ghostVisible by viewModel.ghostVisible.collectAsStateWithLifecycle()
    val ghostOutlineOnly by viewModel.ghostOutlineOnly.collectAsStateWithLifecycle()
    val smoothFallingEnabled by viewModel.smoothFallingEnabled.collectAsStateWithLifecycle()
    val lineClearChallenge by viewModel.lineClearChallenge.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentLang == Language.RU) "НАСТРОЙКИ РЕЛАКС РЕЖИМА" else "RELAX MODE SETTINGS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "Призрачная фигура" else "Ghost piece",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = ghostVisible,
                        onCheckedChange = { viewModel.setGhostVisible(it) }
                    )
                }

                if (ghostVisible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (currentLang == Language.RU) "Только контур призрака" else "Ghost outline only",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = ghostOutlineOnly,
                            onCheckedChange = { viewModel.setGhostOutlineOnly(it) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "Плавное падение" else "Smooth falling",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = smoothFallingEnabled,
                        onCheckedChange = { viewModel.setSmoothFallingEnabled(it) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "Челлендж линий" else "Line challenge",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = lineClearChallenge,
                        onCheckedChange = { viewModel.setLineClearChallenge(it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) {
                Text(if (currentLang == Language.RU) "Готово" else "Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
