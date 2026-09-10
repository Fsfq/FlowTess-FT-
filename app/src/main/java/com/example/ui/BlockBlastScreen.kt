package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Replay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.BlockBlastFigure
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockBlastScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.blockBlastEngine.state.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Material 3 Solid Harmonic Palette
    val blockBlastColors = remember {
        listOf(
            Color.Transparent, // 0: Empty
            Color(0xFF2563EB), // 1: Royal Blue
            Color(0xFF059669), // 2: Emerald Green
            Color(0xFFD97706), // 3: Warm Amber
            Color(0xFF7C3AED), // 4: Violet
            Color(0xFFDC2626), // 5: Crimson Red
            Color(0xFF4F46E5), // 6: Indigo
            Color(0xFF0891B2), // 7: Deep Cyan
            Color(0xFFDB2777), // 8: Rose Pink
            Color(0xFFEA580C)  // 9: Vivid Orange
        )
    }

    var selectedFigureIdx by remember { mutableStateOf<Int?>(null) }
    var hintText by remember { mutableStateOf<String?>(null) }

    var rootBoxBounds by remember { mutableStateOf<Rect?>(null) }
    var boardBounds by remember { mutableStateOf<Rect?>(null) }
    val cardBoundsList = remember { mutableStateListOf<Rect?>(null, null, null) }

    var activeDraggingIdx by remember { mutableStateOf<Int?>(null) }
    var dragTouchScreenPos by remember { mutableStateOf(Offset.Zero) }
    var hoverRowCol by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val liftDistancePx = remember(density) { with(density) { 80.dp.toPx() } }

    // Smooth rapid score counter animation
    val animatedScore by animateIntAsState(
        targetValue = state.score,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "animated_score"
    )
    val scoreScale = remember { Animatable(1f) }

    // Line clearing blast animation state (4 distinct varieties)
    var activeClearingCells by remember { mutableStateOf<List<Triple<Int, Int, Int>>>(emptyList()) }
    val clearAnim = remember { Animatable(1f) }
    var clearAnimStyle by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.score) {
        if (state.score > 0) {
            scoreScale.snapTo(1.14f)
            scoreScale.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
        }
        if (state.lastClearedCells.isNotEmpty()) {
            clearAnimStyle = (clearAnimStyle + 1) % 4
            activeClearingCells = state.lastClearedCells
            clearAnim.snapTo(0f)
            clearAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
            activeClearingCells = emptyList()
        }
    }

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
                    Text(
                        text = "Zeta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                actions = {
                    IconButton(
                        onClick = {
                            selectedFigureIdx = null
                            hoverRowCol = null
                            viewModel.startBlockBlast()
                            viewModel.triggerAudioFeedback("start")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Restart",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                .onGloballyPositioned { coords ->
                    rootBoxBounds = coords.boundsInWindow()
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val totalH = maxHeight
                val totalW = maxWidth
                val isCompact = totalH < 720.dp
                val boardSize = minOf(totalW, totalH * (if (isCompact) 0.46f else 0.50f))
                val slotSize = if (isCompact) 78.dp else 96.dp

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // MD3 Tonal Score Dashboard (No Strikes, Clean Counting Score)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Score with animated rolling counter & subtle bounce
                        Column {
                            Text(
                                text = Translations.get("current_score", currentLang).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$animatedScore",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scoreScale.value
                                    scaleY = scoreScale.value
                                }
                            )
                        }

                        // Best Score
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Translations.get("high_score", currentLang).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.highScore}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 8x8 Grid Container (Clean MD3 Tonal Surface)
                Card(
                    modifier = Modifier
                        .size(boardSize)
                        .onGloballyPositioned { coords ->
                            boardBounds = coords.boundsInWindow()
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
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

                                        val currentDragIdx = activeDraggingIdx ?: selectedFigureIdx
                                        val targetHover = hoverRowCol

                                        // Forecast line clears
                                        val willClearRows = remember(state.grid, currentDragIdx, targetHover) {
                                            val dragIdx = currentDragIdx
                                            val hover = targetHover
                                            if (dragIdx == null || hover == null) emptySet<Int>()
                                            else {
                                                val figure = state.pool.getOrNull(dragIdx)
                                                if (figure != null && viewModel.blockBlastEngine.canPlaceFigure(figure, hover.first, hover.second, state.grid)) {
                                                    val hR = hover.first
                                                    val hC = hover.second
                                                    (0..7).filter { rowIdx ->
                                                        (0..7).all { colIdx ->
                                                            state.grid[rowIdx][colIdx] != 0 || figure.blocks.any { b -> (hR + b.r == rowIdx) && (hC + b.c == colIdx) }
                                                        }
                                                    }.toSet()
                                                } else emptySet()
                                            }
                                        }

                                        val willClearCols = remember(state.grid, currentDragIdx, targetHover) {
                                            val dragIdx = currentDragIdx
                                            val hover = targetHover
                                            if (dragIdx == null || hover == null) emptySet<Int>()
                                            else {
                                                val figure = state.pool.getOrNull(dragIdx)
                                                if (figure != null && viewModel.blockBlastEngine.canPlaceFigure(figure, hover.first, hover.second, state.grid)) {
                                                    val hR = hover.first
                                                    val hC = hover.second
                                                    (0..7).filter { colIdx ->
                                                        (0..7).all { rowIdx ->
                                                            state.grid[rowIdx][colIdx] != 0 || figure.blocks.any { b -> (hR + b.r == rowIdx) && (hC + b.c == colIdx) }
                                                        }
                                                    }.toSet()
                                                } else emptySet()
                                            }
                                        }

                                        val isWillClear = r in willClearRows || c in willClearCols

                                        var finalCellColor = if (isFilled) {
                                            blockBlastColors[cellValue % blockBlastColors.size]
                                        } else if (isWillClear) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                        } else {
                                            emptyCellColor
                                        }

                                        // Drag hover preview overlay
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
                                                    finalCellColor = if (fits) {
                                                        blockBlastColors[figure.colorIndex % blockBlastColors.size].copy(alpha = 0.65f)
                                                    } else {
                                                        MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                                                    }
                                                }
                                            }
                                        }

                                        // Active clearing animation check
                                        val clearingItem = activeClearingCells.firstOrNull { it.first == r && it.second == c }
                                        val isClearingNow = clearingItem != null && clearAnim.value < 1f

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isClearingNow) emptyCellColor else finalCellColor)
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
                                                                hoverRowCol = null
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // 4 Distinct Blast Disintegration Animations
                                            if (clearingItem != null && clearAnim.value < 1f) {
                                                val blockColor = blockBlastColors[clearingItem.third % blockBlastColors.size]
                                                val stagger = ((r + c) * 0.02f).coerceIn(0f, 0.16f)
                                                val p = ((clearAnim.value - stagger).coerceAtLeast(0f) / (1f - stagger)).coerceIn(0f, 1f)

                                                if (p < 1f) {
                                                    when (clearAnimStyle) {
                                                        0 -> {
                                                            // Variety 0: Burst & Sparks
                                                            val scale = if (p < 0.22f) 1f + (p / 0.22f) * 0.28f else (1.28f - ((p - 0.22f) / 0.78f) * 1.28f).coerceAtLeast(0f)
                                                            val alpha = (1f - p * p).coerceIn(0f, 1f)
                                                            val rot = sin(p * Math.PI.toFloat()) * ((r * 11 + c * 7) % 20 - 10) * 1.5f
                                                            val flashAlpha = if (p < 0.18f) (1f - p / 0.18f) * 0.85f else 0f

                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .graphicsLayer {
                                                                        scaleX = scale
                                                                        scaleY = scale
                                                                        rotationZ = rot
                                                                        this.alpha = alpha
                                                                    }
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(blockColor)
                                                            ) {
                                                                if (flashAlpha > 0f) {
                                                                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = flashAlpha)))
                                                                }
                                                            }

                                                            if (p > 0.08f && p < 0.92f) {
                                                                val partP = ((p - 0.08f) / 0.84f).coerceIn(0f, 1f)
                                                                val partAlpha = (1f - partP).coerceIn(0f, 1f)
                                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                                    val center = Offset(size.width / 2f, size.height / 2f)
                                                                    val dist = partP * size.width * 0.75f
                                                                    val rad = (2.5.dp * (1f - partP * 0.5f)).toPx()
                                                                    val offsets = listOf(
                                                                        Offset(-dist * 0.7f, -dist * 0.7f),
                                                                        Offset(dist * 0.7f, -dist * 0.7f),
                                                                        Offset(-dist * 0.7f, dist * 0.7f),
                                                                        Offset(dist * 0.7f, dist * 0.7f)
                                                                    )
                                                                    for (off in offsets) {
                                                                        drawCircle(
                                                                            color = blockColor.copy(alpha = partAlpha),
                                                                            radius = rad,
                                                                            center = center + off
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        1 -> {
                                                            // Variety 1: Glass Shatter into 4 Corner Shards
                                                            val shardAlpha = (1f - p * p).coerceIn(0f, 1f)
                                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                                val hw = size.width / 2f
                                                                val hh = size.height / 2f
                                                                val flyDist = p * size.width * 0.65f
                                                                val shardScale = (1f - p * 0.88f).coerceAtLeast(0f)
                                                                val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                                                                val shardConfigs = listOf(
                                                                    Triple(Offset(-flyDist, -flyDist), -35f * p, Offset(0f, 0f)),
                                                                    Triple(Offset(flyDist, -flyDist), 35f * p, Offset(hw, 0f)),
                                                                    Triple(Offset(-flyDist, flyDist), 35f * p, Offset(0f, hh)),
                                                                    Triple(Offset(flyDist, flyDist), -35f * p, Offset(hw, hh))
                                                                )

                                                                for ((fly, rotDeg, origin) in shardConfigs) {
                                                                    val shardCenter = origin + Offset(hw / 2f, hh / 2f) + fly
                                                                    rotate(rotDeg, pivot = shardCenter) {
                                                                        val shardW = (hw - 1.5f) * shardScale
                                                                        val shardH = (hh - 1.5f) * shardScale
                                                                        val left = shardCenter.x - shardW / 2f
                                                                        val top = shardCenter.y - shardH / 2f
                                                                        drawRoundRect(
                                                                            color = blockColor.copy(alpha = shardAlpha),
                                                                            topLeft = Offset(left, top),
                                                                            size = Size(shardW, shardH),
                                                                            cornerRadius = cornerRadius
                                                                        )
                                                                        if (p < 0.20f) {
                                                                            drawRoundRect(
                                                                                color = Color.White.copy(alpha = (1f - p / 0.20f) * 0.7f),
                                                                                topLeft = Offset(left, top),
                                                                                size = Size(shardW, shardH),
                                                                                cornerRadius = cornerRadius
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        2 -> {
                                                            // Variety 2: Vortex & Spiral Implosion with Shockwave Ring
                                                            val vortexScale = (1f - p * p).coerceAtLeast(0f)
                                                            val vortexRot = p * 360f
                                                            val vortexAlpha = (1f - p).coerceIn(0f, 1f)

                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .graphicsLayer {
                                                                        scaleX = vortexScale
                                                                        scaleY = vortexScale
                                                                        rotationZ = vortexRot
                                                                        alpha = vortexAlpha
                                                                    }
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(blockColor)
                                                            )

                                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                                val center = Offset(size.width / 2f, size.height / 2f)
                                                                val ringRad = p * size.width * 0.85f
                                                                val ringAlpha = (1f - p).coerceIn(0f, 1f)
                                                                drawCircle(
                                                                    color = blockColor.copy(alpha = ringAlpha),
                                                                    radius = ringRad,
                                                                    center = center,
                                                                    style = Stroke(width = (2.5.dp * (1f - p)).toPx())
                                                                )
                                                            }
                                                        }
                                                        else -> {
                                                            // Variety 3: Quantum Pulse / Laser Slice
                                                            val scaleXVal = if (p < 0.25f) 1f + (p / 0.25f) * 0.35f else (1.35f - ((p - 0.25f) / 0.75f) * 1.35f).coerceAtLeast(0f)
                                                            val scaleYVal = (1f - p * 1.4f).coerceAtLeast(0f)
                                                            val sliceAlpha = (1f - p * p).coerceIn(0f, 1f)

                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .graphicsLayer {
                                                                        scaleX = scaleXVal
                                                                        scaleY = scaleYVal
                                                                        alpha = sliceAlpha
                                                                    }
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(blockColor)
                                                            )

                                                            if (p < 0.7f) {
                                                                val lineAlpha = (1f - p / 0.7f).coerceIn(0f, 1f)
                                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                                    val y = size.height / 2f
                                                                    val lineExtend = p * size.width * 0.6f
                                                                    drawLine(
                                                                        color = Color.White.copy(alpha = lineAlpha),
                                                                        start = Offset(-lineExtend, y),
                                                                        end = Offset(size.width + lineExtend, y),
                                                                        strokeWidth = (2.5.dp * (1f - p)).toPx()
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Status / Instruction text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (hintText != null) {
                        Text(
                            text = hintText!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val msg = if (activeDraggingIdx != null) {
                            when (currentLang) {
                                Language.RU -> "Отпустите фигуру на сетке"
                                Language.UA -> "Відпустіть фігуру на сітці"
                                Language.KK -> "Фигураны торға қойыңыз"
                                Language.DE -> "Block auf dem Raster loslassen"
                                Language.ZH -> "释放以放置方块"
                                else -> "Release figure on the grid"
                            }
                        } else if (selectedFigureIdx != null) {
                            when (currentLang) {
                                Language.RU -> "Коснитесь клетки для установки"
                                Language.UA -> "Торкніться клітинки для встановлення"
                                Language.KK -> "Орнату үшін торды басыңыз"
                                Language.DE -> "Feld zum Platzieren berühren"
                                Language.ZH -> "点击网格格子放置方块"
                                else -> "Tap a grid cell to place"
                            }
                        } else {
                            when (currentLang) {
                                Language.RU -> "Перетащите фигуру на поле"
                                Language.UA -> "Перетягніть фігуру на поле"
                                Language.KK -> "Фигураны алаңға сүйреңіз"
                                Language.DE -> "Figur auf das Feld ziehen"
                                Language.ZH -> "拖拽方块至棋盘"
                                else -> "Drag figure onto the grid"
                            }
                        }
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // POOL SHELF (MD3 Tonal Card with Filled Slots)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.pool.forEachIndexed { idx, figure ->
                            val isSelected = selectedFigureIdx == idx
                            val isDragging = activeDraggingIdx == idx

                            Box(
                                modifier = Modifier
                                    .size(slotSize)
                                    .onGloballyPositioned { coords ->
                                        val bounds = coords.boundsInWindow()
                                        if (cardBoundsList.size > idx) {
                                            cardBoundsList[idx] = bounds
                                        } else {
                                            cardBoundsList.add(bounds)
                                        }
                                    }
                                    .scale(if (isSelected && !isDragging) 1.04f else 1.0f)
                                    .alpha(if (isDragging) 0.25f else 1.0f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected && !isDragging) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                    .then(
                                        if (figure != null) {
                                            Modifier.pointerInput(figure.id) {
                                                detectDragGestures(
                                                    onDragStart = { localOffset ->
                                                        activeDraggingIdx = idx
                                                        selectedFigureIdx = idx
                                                        val bounds = cardBoundsList.getOrNull(idx)
                                                        if (bounds != null) {
                                                            dragTouchScreenPos = Offset(
                                                                bounds.left + localOffset.x,
                                                                bounds.top + localOffset.y
                                                            )
                                                        }
                                                        viewModel.triggerAudioFeedback("move")
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragTouchScreenPos += dragAmount

                                                        val board = boardBounds
                                                        if (board != null) {
                                                            val liftedPos = dragTouchScreenPos - Offset(0f, liftDistancePx)
                                                            val cellW = board.width / 8f
                                                            val cellH = board.height / 8f
                                                            val activeArea = board.inflate(cellW * 1.5f)

                                                            if (activeArea.contains(liftedPos)) {
                                                                val localX = liftedPos.x - board.left
                                                                val localY = liftedPos.y - board.top
                                                                val targetCol = ((localX / cellW) - figure.colsCount / 2f).roundToInt().coerceIn(0, 8 - figure.colsCount)
                                                                val targetRow = ((localY / cellH) - figure.rowsCount / 2f).roundToInt().coerceIn(0, 8 - figure.rowsCount)
                                                                val newTarget = Pair(targetRow, targetCol)
                                                                if (hoverRowCol != newTarget) {
                                                                    hoverRowCol = newTarget
                                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                }
                                                            } else {
                                                                hoverRowCol = null
                                                            }
                                                        } else {
                                                            hoverRowCol = null
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        val target = hoverRowCol
                                                        if (target != null) {
                                                            val success = viewModel.placeBlockBlastFigure(idx, target.first, target.second)
                                                            if (success) {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                selectedFigureIdx = null
                                                                hintText = null
                                                            } else {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                hintText = when (currentLang) {
                                                                    Language.RU -> "Фигура здесь не помещается"
                                                                    Language.UA -> "Фігура тут не вміщується"
                                                                    Language.KK -> "Фигура мұнда сыймайды"
                                                                    Language.DE -> "Figur passt hier nicht hin"
                                                                    Language.ZH -> "此处无法放置该方块"
                                                                    else -> "Figure does not fit here"
                                                                }
                                                            }
                                                        }
                                                        activeDraggingIdx = null
                                                        hoverRowCol = null
                                                    },
                                                    onDragCancel = {
                                                        activeDraggingIdx = null
                                                        hoverRowCol = null
                                                    }
                                                )
                                            }
                                        } else Modifier
                                    )
                                    .clickable {
                                        if (figure != null) {
                                            selectedFigureIdx = if (isSelected) null else idx
                                            hintText = null
                                            viewModel.triggerAudioFeedback("move")
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Placed",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

            // Floating Dragged Figure Overlay (Clean overlay with no outer box shadow or ghost outlines)
            if (activeDraggingIdx != null) {
                val draggedFigure = state.pool.getOrNull(activeDraggingIdx!!)
                if (draggedFigure != null && rootBoxBounds != null) {
                    val rootBounds = rootBoxBounds!!
                    val board = boardBounds
                    val cellW = board?.let { it.width / 8f } ?: with(density) { 38.dp.toPx() }
                    val cellH = board?.let { it.height / 8f } ?: with(density) { 38.dp.toPx() }
                    val cellSizeDp = with(density) { (cellW - 4f).coerceAtLeast(12f).toDp() }

                    val localTouchX = dragTouchScreenPos.x - rootBounds.left
                    val localTouchY = dragTouchScreenPos.y - rootBounds.top
                    val liftedX = localTouchX
                    val liftedY = localTouchY - liftDistancePx

                    val figW = draggedFigure.colsCount * cellW
                    val figH = draggedFigure.rowsCount * cellH

                    val posX = (liftedX - figW / 2f).roundToInt()
                    val posY = (liftedY - figH / 2f).roundToInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(posX, posY) }
                            .zIndex(99f)
                    ) {
                        FloatingDraggedFigure(
                            figure = draggedFigure,
                            color = blockBlastColors[draggedFigure.colorIndex % blockBlastColors.size],
                            cellSizeDp = cellSizeDp
                        )
                    }
                }
            }
        }
    }

    // Modern Material 3 Game Over Dialog (No Strikes)
    if (state.isGameOver) {
        val rewardedCC = (state.score / 12).coerceAtLeast(15)
        val isNewRecord = state.score > 0 && state.score >= state.highScore

        AlertDialog(
            onDismissRequest = {},
            icon = {
                Surface(
                    shape = CircleShape,
                    color = if (isNewRecord) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isNewRecord) Icons.Default.EmojiEvents else Icons.Default.Replay,
                            contentDescription = null,
                            tint = if (isNewRecord) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = if (isNewRecord) {
                        when (currentLang) {
                            Language.RU -> "Новый рекорд!"
                            Language.UA -> "Новий рекорд!"
                            Language.KK -> "Жаңа рекорд!"
                            Language.DE -> "Neuer Rekord!"
                            Language.ZH -> "新纪录！"
                            else -> "New Record!"
                        }
                    } else {
                        Translations.get("game_over", currentLang)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
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
                                    text = "${state.score}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ЛИНИИ"
                                        Language.UA -> "ЛІНІЇ"
                                        Language.KK -> "СЫЗЫҚ"
                                        Language.DE -> "LINIEN"
                                        Language.ZH -> "消除行"
                                        else -> "LINES"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${state.linesClearedTotal}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "НАГРАДА"
                                        Language.UA -> "НАГОРОДА"
                                        Language.KK -> "СЫЙЛЫҚ"
                                        Language.DE -> "BELOHNUNG"
                                        Language.ZH -> "获得金币"
                                        else -> "REWARD"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+$rewardedCC 🪙",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFigureIdx = null
                        hoverRowCol = null
                        viewModel.startBlockBlast()
                        viewModel.triggerAudioFeedback("start")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = Translations.get("retry", currentLang),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = Translations.get("menu", currentLang),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
fun MiniFigureRenderer(figure: BlockBlastFigure, color: Color) {
    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        val totalRows = figure.rowsCount
        val totalCols = figure.colsCount
        val cellSize = if (totalRows > 3 || totalCols > 3) 10.dp else 13.dp

        Column(
            verticalArrangement = Arrangement.spacedBy(2.5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until totalRows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                ) {
                    for (c in 0 until totalCols) {
                        val hasBlock = figure.blocks.any { it.r == r && it.c == c }
                        if (hasBlock) {
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .clip(RoundedCornerShape(3.5.dp))
                                    .background(color)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(cellSize))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingDraggedFigure(
    figure: BlockBlastFigure,
    color: Color,
    cellSizeDp: androidx.compose.ui.unit.Dp
) {
    val totalRows = figure.rowsCount
    val totalCols = figure.colsCount

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (r in 0 until totalRows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (c in 0 until totalCols) {
                    val hasBlock = figure.blocks.any { it.r == r && it.c == c }
                    if (hasBlock) {
                        Box(
                            modifier = Modifier
                                .size(cellSizeDp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(cellSizeDp))
                    }
                }
            }
        }
    }
}
