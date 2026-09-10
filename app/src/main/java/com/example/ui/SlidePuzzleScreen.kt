package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.Colors
import com.example.game.SlideBlock
import com.example.game.SlidePuzzleEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidePuzzleScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.slidePuzzleEngine.state.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state.blocks.isEmpty() || state.isGameOver) {
            viewModel.startSlidePuzzle()
        }
    }

    val dangerCeiling = remember(state.blocks) {
        state.blocks.any { it.row <= 2 }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalIconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = Translations.get("back", currentLang),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.score}",
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
                                        text = "${Translations.get("lines", currentLang).uppercase()} ${state.linesCleared}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                if (state.combo > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FlashOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "x${state.combo}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FilledTonalIconButton(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                viewModel.startSlidePuzzle()
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = Translations.get("restart", currentLang),
                                modifier = Modifier.size(18.dp)
                            )
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header hint
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Сдвигайте блоки для сбора и взрыва полных линий"
                            Language.UA -> "Зсувайте блоки для збору та вибуху повних ліній"
                            Language.KK -> "Толық сызықтарды жою үшін блоктарды жылжытыңыз"
                            Language.DE -> "Verschiebe Blöcke, um volle Reihen zu sprengen"
                            Language.ZH -> "左右滑动方块填满整行消除并触发重力连击"
                            else -> "Slide blocks to fill and clear complete lines"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Board (8 cols x 10 rows)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val boardH = minOf(maxHeight, maxWidth * (10f / 8f))
                    val boardW = boardH * (8f / 10f)

                    Box(
                        modifier = Modifier
                            .size(width = boardW, height = boardH)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(
                                BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(4.dp)
                    ) {
                        val boardWidth = boardW
                        val boardHeight = boardH
                        val cellWidth = boardWidth / SlidePuzzleEngine.COLS
                        val cellHeight = boardHeight / SlidePuzzleEngine.ROWS
                        val cellWidthPx = with(LocalDensity.current) { cellWidth.toPx() }

                        // Danger ceiling line
                        if (dangerCeiling) {
                            val hazardTransition = rememberInfiniteTransition(label = "SlideHazard")
                            val hazardAlpha by hazardTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 0.9f,
                                animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                                label = "SlideHazardAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight * 2)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(MaterialTheme.colorScheme.error.copy(alpha = hazardAlpha * 0.3f), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        // Render blocks
                        for (block in state.blocks) {
                            SlideBlockItem(
                                block = block,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                cellWidthPx = cellWidthPx,
                                onMove = { targetCol ->
                                    viewModel.moveSlideBlock(block.id, targetCol)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Game Over Dialog
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = Translations.get("game_over", currentLang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${Translations.get("score", currentLang)}: ${state.score}", fontWeight = FontWeight.Bold)
                            Text("${Translations.get("lines", currentLang)}: ${state.linesCleared}")
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Награда: +${50 + state.score / 60} монет"
                                    Language.UA -> "Нагорода: +${50 + state.score / 60} монет"
                                    Language.KK -> "Марапат: +${50 + state.score / 60} тиын"
                                    Language.DE -> "Belohnung: +${50 + state.score / 60} Münzen"
                                    Language.ZH -> "奖励结算：+${50 + state.score / 60} 游戏币"
                                    else -> "Reward: +${50 + state.score / 60} credits"
                                },
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.startSlidePuzzle() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(Translations.get("restart", currentLang))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onBack) {
                            Text(Translations.get("back", currentLang))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SlideBlockItem(
    block: SlideBlock,
    cellWidth: androidx.compose.ui.unit.Dp,
    cellHeight: androidx.compose.ui.unit.Dp,
    cellWidthPx: Float,
    onMove: (Int) -> Unit
) {
    var dragAccumulator by remember { mutableStateOf(0f) }
    val blockColor = remember(block.colorIndex) {
        Colors.getOrElse(block.colorIndex) { Color(0xFF4FC3F7) }
    }

    val leftOffset = cellWidth * block.col + (dragAccumulator / cellWidthPx.coerceAtLeast(1f)).dp * cellWidth.value
    val topOffset = cellHeight * block.row

    Box(
        modifier = Modifier
            .offset(x = cellWidth * block.col, y = topOffset)
            .width(cellWidth * block.length)
            .height(cellHeight)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(blockColor)
            .border(
                BorderStroke(1.dp, Color.Gray.copy(alpha = 0.35f)),
                RoundedCornerShape(8.dp)
            )
            .pointerInput(block.id, block.row, block.col) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = {
                        val steps = (dragAccumulator / (cellWidthPx * 0.45f)).toInt()
                        if (steps != 0) {
                            onMove(block.col + steps)
                        }
                        dragAccumulator = 0f
                    },
                    onDragCancel = { dragAccumulator = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                        if (dragAccumulator > cellWidthPx * 0.75f) {
                            onMove(block.col + 1)
                            dragAccumulator = 0f
                        } else if (dragAccumulator < -cellWidthPx * 0.75f) {
                            onMove(block.col - 1)
                            dragAccumulator = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Subtle inner highlight line
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(2.dp)
                .align(Alignment.TopCenter)
                .background(Color.White.copy(alpha = 0.35f), CircleShape)
        )
    }
}
