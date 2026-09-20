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

    val isPerfectionistMode = gameState.gameMode == com.example.game.GameMode.PERFECTIONIST
    val perfectionistHint = remember(
        isPerfectionistMode,
        gameState.grid,
        gameState.currentPiece,
        gameState.holdPiece,
        gameState.hasHeldThisTurn,
        gameState.nextPieces.firstOrNull(),
        viewModel
    ) {
        if (isPerfectionistMode && viewModel != null && gameState.currentPiece != null) {
            viewModel.gameEngine.calculateOptimalPlacement(
                grid = gameState.grid,
                piece = gameState.currentPiece!!,
                holdPiece = gameState.holdPiece,
                nextPiece = gameState.nextPieces.firstOrNull(),
                canHold = !gameState.hasHeldThisTurn
            )
        } else null
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
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(bSize, bSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(x + pad + 1f, y + pad + 1f),
                size = Size(bSize - 2f, bSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
                style = Stroke(width = 1f)
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

