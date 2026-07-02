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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
    
    // Aesthetic theme colors
    val themeColor = when (activeThemeKey) {
        "indigo" -> Color(0xFF6366F1)
        "neon" -> Color(0xFF00FFCC)
        "emerald" -> Color(0xFF10B981)
        "amber" -> Color(0xFFF59E0B)
        "rose" -> Color(0xFFF43F5E)
        "sky" -> Color(0xFF0EA5E9)
        "orange" -> Color(0xFFFF5722)
        else -> Color(0xFF00FFCC)
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
    
    // Core Drag and Drop State properties
    var boardBounds by remember { mutableStateOf<Rect?>(null) }
    val cardOffsets = remember { mutableStateListOf(Offset.Zero, Offset.Zero, Offset.Zero) }
    val cardDragging = remember { mutableStateListOf(false, false, false) }
    val cardBoundsList = remember { mutableStateListOf<Rect?>(null, null, null) }
    
    var activeDraggingIdx by remember { mutableStateOf<Int?>(null) }
    var cursorScreenPos by remember { mutableStateOf(Offset.Zero) }
    var hoverRowCol by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Reset fallback selections on layout update
    LaunchedEffect(state.pool) {
        selectedFigureIdx?.let { idx ->
            if (state.pool.getOrNull(idx) == null) {
                selectedFigureIdx = null
            }
        }
    }

    // Auto clear error hints
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AdaptiveText(
                            text = if (currentLang == Language.RU) "ZETA АРЕНА" else "NEON ZETA MAX",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                shadow = Shadow(color = themeColor, blurRadius = 8f)
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AdaptiveText(
                            text = "SMOOTH TOUCH GESTURES DRAG-N-DROP",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Aesthetic Score Dashboard (Material 3 ElevatedCard)
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                                text = if (currentLang == Language.RU) "ТЕКУЩИЙ СЧЕТ" else "SCORE ENGINE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColor,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AdaptiveText(
                                text = state.score.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Interactive active Combo tag wrapper
                            Box(
                                modifier = Modifier.height(30.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = state.combo > 0,
                                    enter = scaleIn() + fadeIn(),
                                    exit = scaleOut() + fadeOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFFF165D), Color(0xFFFF9A00))
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🔥", fontSize = 10.sp)
                                            AdaptiveText(
                                                text = "ZETA COMBO x${state.combo}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            AdaptiveText(
                                text = if (currentLang == Language.RU) "АБСОЛЮТНЫЙ РЕКОРД" else "CYBER RECORD",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.highScore.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFFFD700))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$credits K",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            boardBounds = coords.boundsInWindow()
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = 2.dp
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
                                        
                                        // Evaluate design variables
                                        val emptyCellColor = MaterialTheme.colorScheme.surface
                                        val emptyCellBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

                                        var cellColor = if (isFilled) blockBlastColors[cellValue % blockBlastColors.size] else emptyCellColor
                                        var cellOpacity = 1f
                                        var cellBorderColor = if (isFilled) cellColor.copy(alpha = 0.5f) else emptyCellBorderColor
                                        var isPreviewCell = false

                                        // Dynamic preview highlight calculation
                                        // If dragging or clicked selected block is hovering over a valid Board coordinate,
                                        // draw the preview shape elements overlay directly!
                                        val currentDragIdx = activeDraggingIdx ?: selectedFigureIdx
                                        val targetHover = hoverRowCol
                                        
                                        if (currentDragIdx != null && targetHover != null) {
                                            val figure = state.pool.getOrNull(currentDragIdx)
                                            if (figure != null) {
                                                val hoverRow = targetHover.first
                                                val hoverCol = targetHover.second
                                                
                                                // Check if current cell is part of the hovering figure blocks relative to (hoverRow, hoverCol)
                                                val isPart = figure.blocks.any { b -> 
                                                    (hoverRow + b.r == r) && (hoverCol + b.c == c) 
                                                }
                                                if (isPart) {
                                                    val fits = viewModel.blockBlastEngine.canPlaceFigure(figure, hoverRow, hoverCol, state.grid)
                                                    isPreviewCell = true
                                                    cellColor = if (fits) themeColor else Color(0xFFFF2E93)
                                                    cellOpacity = 0.75f
                                                    cellBorderColor = Color.White
                                                }
                                            }
                                        }

                                        val cellBrush = if (isFilled || isPreviewCell) {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    cellColor.copy(alpha = cellOpacity),
                                                    cellColor.copy(alpha = cellOpacity * 0.7f)
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    emptyCellColor,
                                                    emptyCellColor
                                                )
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
                                                    // Fallback traditional touch-tap controls for accessibility
                                                    selectedFigureIdx?.let { idx ->
                                                        val figure = state.pool.getOrNull(idx)
                                                        if (figure != null) {
                                                            // Align figure center to the clicked cell
                                                            val targetR = (r - figure.rowsCount / 2).coerceIn(0, 8 - figure.rowsCount)
                                                            val targetC = (c - figure.colsCount / 2).coerceIn(0, 8 - figure.colsCount)
                                                            val success = viewModel.placeBlockBlastFigure(idx, targetR, targetC)
                                                            if (success) {
                                                                selectedFigureIdx = null
                                                                hintText = null
                                                                viewModel.triggerAudioFeedback("land")
                                                            } else {
                                                                hintText = if (currentLang == Language.RU) 
                                                                    "Сюда фигуру вставить нельзя!" 
                                                                else "Selected block cannot fit here!"
                                                                viewModel.triggerAudioFeedback("move")
                                                            }
                                                        }
                                                    }
                                                }
                                        ) {
                                            // Futuristic core cell shine dot or empty cell blueprint dot
                                            if (isFilled && !isPreviewCell) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .align(Alignment.Center)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Color.White.copy(alpha = 0.40f))
                                                )
                                            } else if (!isFilled && !isPreviewCell) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .align(Alignment.Center)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // CRT Scanlines visual effect layer filter
                        if (scanlinesFilter) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val count = size.height / 5f
                                for (i in 0..count.toInt()) {
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.15f),
                                        topLeft = Offset(0f, i * 5f),
                                        size = Size(size.width, 2.3f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive User Status Banner Instructions
                Spacer(modifier = Modifier.height(10.dp))
                
                Box(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = hintText,
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                        }
                    ) { activeHint ->
                        if (activeHint != null) {
                            Text(
                                text = activeHint,
                                color = Color(0xFFFF2E93),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            val msg = if (activeDraggingIdx != null) {
                                if (currentLang == Language.RU) "Перетащите фигуру и отпустите на игровом поле" else "Drag and drop the figure over the board!"
                            } else if (selectedFigureIdx != null) {
                                if (currentLang == Language.RU) "Нажмите на клетку поля, чтобы разместить блок" else "Tap on any grid area to place block!"
                            } else {
                                if (currentLang == Language.RU) "ПЕРЕТАСКИВАЙТЕ блоки или нажмите для выбора" else "DRAG AND DROP figures, or tap to choose!"
                            }
                            Text(
                                text = "$msg",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // POOL of 3 Figures supporting interactive touch gestures (Material 3 ElevatedCard)
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (currentLang == Language.RU) "БЛОК-ПОЛКА (СЕНСОРНЫЙ ПЕРЕНОС)" else "BLOCK SHELF (TOUCH DRAG)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
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
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isDragging) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                            else if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            width = if (isDragging) 2.dp else if (isSelected) 2.dp else 1.dp,
                                            color = if (isDragging) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(16.dp)
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
                                                    
                                                    // Map mouse/finger coordinates directly to grid board indices
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
                                                            // Center figure boundary relative to cell
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
                                                            hintText = if (currentLang == Language.RU) "Фигура здесь не помещается!" else "Figure doesn't fit here!"
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
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "PLACED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
 
                // Game Over Screen Modal panel 
                if (state.isGameOver) {
                    val rewardedCC = (state.score / 12).coerceAtLeast(15)
                    AlertDialog(
                        onDismissRequest = {},
                        title = {
                            Text(
                                text = if (currentLang == Language.RU) "ИГРА ОКОНЧЕНА" else "GAME OVER",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error,
                                letterSpacing = 1.5.sp
                            )
                        },
                        text = {
                            Text(
                                text = if (currentLang == Language.RU) {
                                    "Ваш финальный счёт: ${state.score}\nНаграда: +$rewardedCC 🪙"
                                } else {
                                    "Your final score: ${state.score}\nReward earned: +$rewardedCC 🪙"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    selectedFigureIdx = null
                                    viewModel.startBlockBlast()
                                    viewModel.triggerAudioFeedback("start")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (currentLang == Language.RU) "ИГРАТЬ СНОВА" else "PLAY AGAIN",
                                    fontWeight = FontWeight.Black
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onBack) {
                                Text(text = if (currentLang == Language.RU) "В МЕНЮ" else "TO MENU")
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
                                    color = if (hasBlock) Color.White.copy(alpha = 0.25f) else Color.Transparent,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}
