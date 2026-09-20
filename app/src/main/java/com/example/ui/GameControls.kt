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
import androidx.compose.foundation.layout.sizeIn
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

    val buttonSize = (56 * scale).dp.coerceAtLeast(48.dp)
    val buttonCorner = (buttonSize / 2f)

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
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
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

