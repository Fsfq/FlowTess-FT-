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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



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

    val isPerfectionistMode = gameState.gameMode == com.example.game.GameMode.PERFECTIONIST
    val perfectionistHint by produceState<com.example.game.PlacementHint?>(
        initialValue = null,
        key1 = isPerfectionistMode,
        key2 = gameState.currentPiece,
        key3 = gameState.grid
    ) {
        if (isPerfectionistMode && viewModel != null && gameState.currentPiece != null) {
            value = withContext(Dispatchers.Default) {
                viewModel.gameEngine.calculateOptimalPlacement(
                    grid = gameState.grid,
                    piece = gameState.currentPiece!!,
                    holdPiece = gameState.holdPiece,
                    nextPiece = gameState.nextPieces.firstOrNull(),
                    canHold = !gameState.hasHeldThisTurn,
                    secondNextPiece = gameState.nextPieces.getOrNull(1)
                )
            }
        } else {
            value = null
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

                    val stencilFill = when {
                        isFilled -> Color(0xFFFFD700).copy(alpha = 0.35f)
                        gameState.gameMode == com.example.game.GameMode.MEMORY_PUZZLE -> Color(0xFF42A5F5).copy(alpha = 0.45f)
                        else -> Color.Gray.copy(alpha = 0.12f)
                    }
                    val stencilStroke = when {
                        isFilled -> Color(0xFFFFD700).copy(alpha = 0.80f)
                        gameState.gameMode == com.example.game.GameMode.MEMORY_PUZZLE -> Color(0xFF90CAF9).copy(alpha = 0.90f)
                        else -> Color.Gray.copy(alpha = 0.35f)
                    }
                    drawRoundRect(
                        color = stencilFill,
                        topLeft = Offset(drawTx * cellSize + 2f, ty * cellSize + 2f),
                        size = Size(cellSize - 4f, cellSize - 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = stencilStroke,
                        topLeft = Offset(drawTx * cellSize + 2f, ty * cellSize + 2f),
                        size = Size(cellSize - 4f, cellSize - 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = if (gameState.gameMode == com.example.game.GameMode.MEMORY_PUZZLE) 1.5.dp.toPx() else 0.8.dp.toPx()
                        )
                    )
                }
            }
        }

        // AI Perfect Placement Guide for Perfectionist Mode
        perfectionistHint?.let { optimal ->
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
            // Clean flat minimal block with crisp inset border
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.22f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(x + pad + 1.2f, y + pad + 1.2f),
                size = Size(bSize - 2.4f, bSize - 2.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                style = Stroke(width = 0.8f)
            )
        }
        style == "glass" -> {
            // Frosted matte surface with smooth soft highlight edge
            drawRoundRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Soft inner surface sheen
            drawRoundRect(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent, Color.Black.copy(alpha = 0.18f)),
                    start = Offset(x + pad, y + pad),
                    end = Offset(x + pad + bSize, y + pad + bSize)
                ),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(x + pad + 0.8f, y + pad + 0.8f),
                size = Size(bSize - 1.6f, bSize - 1.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f, 3.5f),
                style = Stroke(width = 1f)
            )
        }
        style == "material" -> {
            // Glass / Crystal Prism: Translucent glossy jewel with realistic top diagonal flare & refraction
            drawRoundRect(
                color = color.copy(alpha = 0.82f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Soft inner glass gradient (bright at top-left, depth shadow at bottom-right)
            drawRoundRect(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.30f)
                    ),
                    start = Offset(x + pad, y + pad),
                    end = Offset(x + pad + bSize, y + pad + bSize)
                ),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Top-left diagonal glass light sheen
            val flarePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(x + pad + 1.5f, y + pad + 1.5f)
                lineTo(x + pad + bSize * 0.75f, y + pad + 1.5f)
                lineTo(x + pad + 1.5f, y + pad + bSize * 0.75f)
                close()
            }
            drawPath(flarePath, color = Color.White.copy(alpha = 0.32f))

            // Thin crisp perimeter bevel
            drawRoundRect(
                color = Color.White.copy(alpha = 0.40f),
                topLeft = Offset(x + pad + 0.8f, y + pad + 0.8f),
                size = Size(bSize - 1.6f, bSize - 1.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f, 3.5f),
                style = Stroke(width = 1f)
            )
        }
        style == "glowing_jewel" -> {
            // Segmented 2x2 waffle / grid texture
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            val half = (bSize - 1.5f) / 2f
            val qSize = Size(half, half)
            val subPad = 0.5f
            // 4 mini square segments inside block
            val qOffsets = listOf(
                Offset(x + pad + subPad, y + pad + subPad),
                Offset(x + pad + half + subPad + 0.5f, y + pad + subPad),
                Offset(x + pad + subPad, y + pad + half + subPad + 0.5f),
                Offset(x + pad + half + subPad + 0.5f, y + pad + half + subPad + 0.5f)
            )
            for (offset in qOffsets) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.2f),
                    topLeft = offset,
                    size = qSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
                )
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = offset,
                    size = qSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f),
                    style = Stroke(width = 0.8f)
                )
            }
        }
        style == "steampunk" -> {
            // Carbon / Chiseled Plate: Precision brushed texture with metallic chamfer border
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            // Fine horizontal texture scanlines
            val linesCount = 4
            val step = bSize / (linesCount + 1)
            for (i in 1..linesCount) {
                val ly = y + pad + i * step
                drawLine(
                    color = Color.Black.copy(alpha = 0.16f),
                    start = Offset(x + pad + 2f, ly),
                    end = Offset(x + pad + bSize - 2f, ly),
                    strokeWidth = 1f
                )
            }
            // Crisp double frame
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.32f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                style = Stroke(width = 1.2f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(x + pad + 1.2f, y + pad + 1.2f),
                size = Size(bSize - 2.4f, bSize - 2.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                style = Stroke(width = 0.8f)
            )
        }
        style == "red_gradient" -> {
            // Structured crimson gradient with clean inner frame
            val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color(0xFFE53935), Color(0xFFC62828)),
                startY = y + pad,
                endY = y + pad + bSize
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(x + pad + 1.2f, y + pad + 1.2f),
                size = Size(bSize - 2.4f, bSize - 2.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.2f, 2.2f),
                style = Stroke(width = 1f)
            )
        }
        style == "green_gradient" -> {
            // Structured emerald gradient with clean inner frame
            val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
                startY = y + pad,
                endY = y + pad + bSize
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(x + pad + 1.2f, y + pad + 1.2f),
                size = Size(bSize - 2.4f, bSize - 2.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.2f, 2.2f),
                style = Stroke(width = 1f)
            )
        }
        style == "blue_gradient" -> {
            // Structured cobalt gradient with clean inner frame
            val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0)),
                startY = y + pad,
                endY = y + pad + bSize
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(x + pad + 1.2f, y + pad + 1.2f),
                size = Size(bSize - 2.4f, bSize - 2.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.2f, 2.2f),
                style = Stroke(width = 1f)
            )
        }
        style == "purple_gradient" -> {
            // Structured amethyst gradient with clean inner frame
            val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color(0xFF8E24AA), Color(0xFF6A1B9A)),
                startY = y + pad,
                endY = y + pad + bSize
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                topLeft = Offset(x + pad + 1.2f, y + pad + 1.2f),
                size = Size(bSize - 2.4f, bSize - 2.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.2f, 2.2f),
                style = Stroke(width = 1f)
            )
        }
        style.contains("gradient") -> {
            val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(color, color.copy(alpha = 0.72f)),
                startY = y + pad,
                endY = y + pad + bSize
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f),
                style = Stroke(width = 1f)
            )
        }
        else -> {
            // Classic Guideline Bevel: Authentic Tetris block with lighter top/left edge and darker bottom/right edge
            val b = bSize * 0.15f
            val left = x + pad
            val top = y + pad
            val right = left + bSize
            val bottom = top + bSize

            // Base center fill
            drawRect(color = color, topLeft = Offset(left, top), size = Size(bSize, bSize))

            // Top bevel trapezoid (lighter)
            val topPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right - b, top + b)
                lineTo(left + b, top + b)
                close()
            }
            drawPath(topPath, color = Color.White.copy(alpha = 0.38f))

            // Left bevel trapezoid (lighter)
            val leftPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top)
                lineTo(left + b, top + b)
                lineTo(left + b, bottom - b)
                lineTo(left, bottom)
                close()
            }
            drawPath(leftPath, color = Color.White.copy(alpha = 0.22f))

            // Bottom bevel trapezoid (darker)
            val bottomPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, bottom)
                lineTo(left + b, bottom - b)
                lineTo(right - b, bottom - b)
                lineTo(right, bottom)
                close()
            }
            drawPath(bottomPath, color = Color.Black.copy(alpha = 0.32f))

            // Right bevel trapezoid (darker)
            val rightPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(right, top)
                lineTo(right, bottom)
                lineTo(right - b, bottom - b)
                lineTo(right - b, top + b)
                close()
            }
            drawPath(rightPath, color = Color.Black.copy(alpha = 0.22f))

            // Subtle outer outline
            drawRect(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(left, top),
                size = Size(bSize, bSize),
                style = Stroke(width = 0.8f)
            )
        }
    }
}

