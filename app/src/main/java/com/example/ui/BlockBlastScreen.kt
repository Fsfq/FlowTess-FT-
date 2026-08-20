package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TouchApp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.BlockBlastFigure
import com.example.game.BlockBlastPosition
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockBlastScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.blockBlastEngine.state.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val activeThemeKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    val scanlinesFilter by viewModel.scanlinesFilter.collectAsStateWithLifecycle()
    
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

    val blockBlastColors = listOf(
        Color(0xFF151421), // 0: Empty
        Color(0xFF00ADB5), // 1: Neon Cyan
        Color(0xFF8A2BE2), // 2: Purple Neon
        Color(0xFFFF9F33), // 3: Orange Neon
        Color(0xFF00E676), // 4: Electric Mint
        Color(0xFFFF007F), // 5: Hot Cyber Pink
        Color(0xFFFFD700), // 6: Gold Vector
        Color(0xFF00E5FF), // 7: Teal Burst
        Color(0xFFE94560), // 8: Crimson Ruby
        Color(0xFF3370FF)  // 9: Sapphire Blue
    )

    // Selection/Drag states
    var selectedFigureIdx by remember { mutableStateOf<Int?>(null) }
    var hintText by remember { mutableStateOf<String?>(null) }
    
    var boardBounds by remember { mutableStateOf<Rect?>(null) }
    val cardOffsets = remember { mutableStateListOf(Offset.Zero, Offset.Zero, Offset.Zero) }
    val cardDragging = remember { mutableStateListOf(false, false, false) }
    val cardBoundsList = remember { mutableStateListOf<Rect?>(null, null, null) }
    
    var activeDraggingIdx by remember { mutableStateOf<Int?>(null) }
    var cursorScreenPos by remember { mutableStateOf(Offset.Zero) }
    var hoverRowCol by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(state.pool) {
        selectedFigureIdx?.let { idx ->
            if (state.pool.getOrNull(idx) == null) {
                selectedFigureIdx = null
            }
        }
    }

    LaunchedEffect(hintText) {
        if (hintText != null) {
            delay(2500)
            hintText = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = themeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = Translations.get("zeta_arena", currentLang),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // MD3 Elevated Score Dashboard
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            AdaptiveText(
                                text = Translations.get("current_score", currentLang),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColor,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AdaptiveText(
                                text = "${state.score}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Dynamic Combo Badge
                            AnimatedVisibility(
                                visible = state.combo > 0,
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color(0xFFFF5722),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "COMBO x${state.combo}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            AdaptiveText(
                                text = Translations.get("high_score", currentLang),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.highScore}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 8x8 Grid Container (MD3 ElevatedCard)
                ElevatedCard(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            boardBounds = coords.boundsInWindow()
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (r in 0..7) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (c in 0..7) {
                                        val cellValue = state.grid[r][c]
                                        val isFilled = cellValue != 0
                                        
                                        val emptyCellColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                        val emptyCellBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

                                        var cellColor = if (isFilled) blockBlastColors[cellValue % blockBlastColors.size] else emptyCellColor
                                        var cellOpacity = 1f
                                        var cellBorderColor = if (isFilled) cellColor.copy(alpha = 0.5f) else emptyCellBorderColor
                                        var isPreviewCell = false

                                        val currentDragIdx = activeDraggingIdx ?: selectedFigureIdx
                                        val targetHover = hoverRowCol
                                        
                                        if (currentDragIdx != null && targetHover != null) {
                                            val figure = state.pool.getOrNull(currentDragIdx)
                                            if (figure != null) {
                                                val hoverRow = targetHover.first
                                                val hoverCol = targetHover.second
                                                
                                                val isPart = figure.blocks.any { b -> 
                                                    (hoverRow + b.r == r) && (hoverCol + b.c == c) 
                                                }
                                                if (isPart) {
                                                    val fits = viewModel.blockBlastEngine.canPlaceFigure(figure, hoverRow, hoverCol, state.grid)
                                                    isPreviewCell = true
                                                    cellColor = if (fits) themeColor else Color(0xFFFF5252)
                                                    cellOpacity = 0.8f
                                                    cellBorderColor = Color.White
                                                }
                                            }
                                        }

                                        val cellBrush = if (isFilled || isPreviewCell) {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    cellColor.copy(alpha = cellOpacity),
                                                    cellColor.copy(alpha = cellOpacity * 0.75f)
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(emptyCellColor, emptyCellColor)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(cellBrush)
                                                .border(
                                                    width = if (isPreviewCell) 2.dp else 1.dp,
                                                    color = cellBorderColor,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    selectedFigureIdx?.let { idx ->
                                                        val figure = state.pool.getOrNull(idx)
                                                        if (figure != null) {
                                                            val targetR = (r - figure.rowsCount / 2).coerceIn(0, 8 - figure.rowsCount)
                                                            val targetC = (c - figure.colsCount / 2).coerceIn(0, 8 - figure.colsCount)
                                                            val success = viewModel.placeBlockBlastFigure(idx, targetR, targetC)
                                                            if (success) {
                                                                selectedFigureIdx = null
                                                                hintText = null
                                                                viewModel.triggerAudioFeedback("land")
                                                            } else {
                                                                hintText = when (currentLang) {
                                                                    Language.RU -> "Фигура здесь не помещается"
                                                                    Language.UA -> "Фігура тут не вміщується"
                                                                    Language.KK -> "Фигура мұнда сыймайды"
                                                                    Language.DE -> "Figur passt hier nicht hin"
                                                                    Language.ZH -> "此处无法放置该方块"
                                                                    else -> "Figure does not fit here"
                                                                }
                                                                viewModel.triggerAudioFeedback("move")
                                                            }
                                                        }
                                                    }
                                                }
                                        ) {
                                            if (isFilled && !isPreviewCell) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .align(Alignment.Center)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.35f))
                                                )
                                            } else if (!isFilled && !isPreviewCell) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .align(Alignment.Center)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (scanlinesFilter) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val count = size.height / 5f
                                for (i in 0..count.toInt()) {
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.12f),
                                        topLeft = Offset(0f, i * 5f),
                                        size = Size(size.width, 2.3f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Hint / UX Status Banner
                Spacer(modifier = Modifier.height(10.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = hintText,
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                        },
                        label = "hint_anim"
                    ) { activeHint ->
                        if (activeHint != null) {
                            Text(
                                text = activeHint,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            val msg = if (activeDraggingIdx != null) {
                                when (currentLang) {
                                    Language.RU -> "Отпустите фигуру на игровом поле"
                                    Language.UA -> "Відпустіть фігуру на полі"
                                    Language.KK -> "Фигураны алаңға жіберіңіз"
                                    Language.DE -> "Block auf dem Feld loslassen"
                                    Language.ZH -> "将方块释放于棋盘上"
                                    else -> "Release figure on the grid!"
                                }
                            } else if (selectedFigureIdx != null) {
                                when (currentLang) {
                                    Language.RU -> "Коснитесь клетки для размещения"
                                    Language.UA -> "Торкніться клітинки для розміщення"
                                    Language.KK -> "Орналастыру үшін ұяшықты басыңыз"
                                    Language.DE -> "Feld zum Platzieren berühren"
                                    Language.ZH -> "点击格子放置方块"
                                    else -> "Tap a grid cell to place!"
                                }
                            } else {
                                Translations.get("drag_drop_hint", currentLang)
                            }
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // POOL SHELF of 3 Figures (MD3 ElevatedCard)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            state.pool.forEachIndexed { idx, figure ->
                                val isSelected = selectedFigureIdx == idx
                                val isDragging = cardDragging.getOrNull(idx) ?: false
                                val offset = cardOffsets.getOrNull(idx) ?: Offset.Zero
                                val dragLiftY = if (isDragging) -180f else 0f

                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .onGloballyPositioned { coords ->
                                            val bounds = coords.boundsInWindow()
                                            if (cardBoundsList.size > idx) {
                                                cardBoundsList[idx] = bounds
                                            } else {
                                                cardBoundsList.add(bounds)
                                            }
                                        }
                                        .offset { IntOffset(offset.x.roundToInt(), (offset.y + dragLiftY).roundToInt()) }
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            if (isDragging) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f)
                                            else if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                        .border(
                                            width = if (isDragging || isSelected) 2.dp else 1.dp,
                                            color = if (isDragging || isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .pointerInput(idx) {
                                            detectDragGestures(
                                                onDragStart = { startOffset ->
                                                    cardDragging[idx] = true
                                                    activeDraggingIdx = idx
                                                    val currentBounds = cardBoundsList.getOrNull(idx)
                                                    if (currentBounds != null) {
                                                        cursorScreenPos = Offset(
                                                            currentBounds.left + currentBounds.width / 2f + startOffset.x,
                                                            currentBounds.top + currentBounds.height / 2f + startOffset.y
                                                        )
                                                    }
                                                    viewModel.triggerAudioFeedback("move")
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    cardOffsets[idx] = cardOffsets[idx] + dragAmount
                                                    cursorScreenPos = cursorScreenPos + dragAmount
                                                    
                                                    val liftedCursorPos = cursorScreenPos + Offset(0f, -180f)
                                                    val board = boardBounds
                                                    if (board != null && board.contains(liftedCursorPos)) {
                                                        val cellW = board.width / 8
                                                        val cellH = board.height / 8
                                                        
                                                        val localX = liftedCursorPos.x - board.left
                                                        val localY = liftedCursorPos.y - board.top
                                                        
                                                        val col = (localX / cellW).toInt().coerceIn(0, 7)
                                                        val row = (localY / cellH).toInt().coerceIn(0, 7)
                                                        
                                                        val poolFig = state.pool.getOrNull(idx)
                                                        if (poolFig != null) {
                                                            val offsetC = (col - poolFig.colsCount / 2).coerceIn(0, 8 - poolFig.colsCount)
                                                            val offsetR = (row - poolFig.rowsCount / 2).coerceIn(0, 8 - poolFig.rowsCount)
                                                            hoverRowCol = Pair(offsetR, offsetC)
                                                        } else {
                                                            hoverRowCol = null
                                                        }
                                                    } else {
                                                        hoverRowCol = null
                                                    }
                                                },
                                                onDragEnd = {
                                                    cardDragging[idx] = false
                                                    activeDraggingIdx = null
                                                    val target = hoverRowCol
                                                    if (target != null) {
                                                        val success = viewModel.placeBlockBlastFigure(idx, target.first, target.second)
                                                        if (success) {
                                                            viewModel.triggerAudioFeedback("land")
                                                            hintText = null
                                                        } else {
                                                            viewModel.triggerAudioFeedback("gameover")
                                                            hintText = when (currentLang) {
                                                                Language.RU -> "Фигура здесь не помещается"
                                                                Language.UA -> "Фігура тут не вміщується"
                                                                Language.KK -> "Фигура мұнда сыймайды"
                                                                Language.DE -> "Block passt hier nicht"
                                                                Language.ZH -> "此处无法放置该方块"
                                                                else -> "Figure does not fit here"
                                                            }
                                                        }
                                                    }
                                                    hoverRowCol = null
                                                    cardOffsets[idx] = Offset.Zero
                                                },
                                                onDragCancel = {
                                                    cardDragging[idx] = false
                                                    activeDraggingIdx = null
                                                    hoverRowCol = null
                                                    cardOffsets[idx] = Offset.Zero
                                                }
                                            )
                                        }
                                        .clickable {
                                            if (figure != null) {
                                                selectedFigureIdx = if (isSelected) null else idx
                                                hintText = null
                                                viewModel.triggerAudioFeedback("move")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (figure != null) {
                                        MiniFigureRenderer(
                                            figure = figure,
                                            color = blockBlastColors[figure.colorIndex % blockBlastColors.size]
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Placed",
                                                tint = themeColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Game Over Dialog
                if (state.isGameOver) {
                    val rewardedCC = (state.score / 12).coerceAtLeast(15)
                    AlertDialog(
                        onDismissRequest = {},
                        title = {
                            Text(
                                text = Translations.get("game_over", currentLang).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error,
                                letterSpacing = 1.sp
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${Translations.get("score", currentLang)}: ${state.score}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+$rewardedCC 🪙",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = themeColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    selectedFigureIdx = null
                                    viewModel.startBlockBlast()
                                    viewModel.triggerAudioFeedback("start")
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Translations.get("retry", currentLang),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = onBack,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(text = Translations.get("menu", currentLang))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MiniFigureRenderer(figure: BlockBlastFigure, color: Color) {
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        val totalRows = figure.rowsCount
        val totalCols = figure.colsCount
        val cellSize = if (totalRows > 3 || totalCols > 3) 9.dp else 12.dp

        Column(
            verticalArrangement = Arrangement.spacedBy(1.5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until totalRows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    for (c in 0 until totalCols) {
                        val hasBlock = figure.blocks.any { it.r == r && it.c == c }
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (hasBlock) color 
                                else Color.White.copy(alpha = 0.02f)
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (hasBlock) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(3.dp)
                            )
                        )
                    }
                }
            }
        }
    }
}
