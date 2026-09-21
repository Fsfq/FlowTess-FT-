package com.example.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import java.util.Locale

@Composable
fun RewardedAdDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    val isAdConnected = viewModel.isAdSdkConnected
    val isAdLoaded by viewModel.isRewardedAdLoaded.collectAsStateWithLifecycle()
    val isAdLoading by viewModel.isAdLoading.collectAsStateWithLifecycle()

    var isPlayingAd by remember { mutableStateOf(false) }
    var adStatusText by remember { mutableStateOf("") }
    var completedRewardCoins by remember { mutableStateOf<Int?>(null) }
    var lastClickTimestamp by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        onDispose {
            isPlayingAd = false
            adStatusText = ""
            completedRewardCoins = null
        }
    }

    // Auto load ad when dialog opens if needed
    LaunchedEffect(Unit) {
        if (!isAdLoaded && !isAdLoading) {
            viewModel.loadRewardedAd()
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isPlayingAd) {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(0.90f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Balance pill + close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%,d", credits),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (!isPlayingAd) {
                                viewModel.triggerAudioFeedback("click")
                                onDismiss()
                            }
                        },
                        enabled = !isPlayingAd,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Clean MD3 Icon Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Value Text (+300)
                Text(
                    text = "+300",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Reward Received Toast Banner
                AnimatedVisibility(
                    visible = completedRewardCoins != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    completedRewardCoins?.let { coins ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.35f)),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "+$coins",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }
                    }
                }

                // Error or Status text with Retry
                AnimatedVisibility(
                    visible = adStatusText.isNotEmpty() && completedRewardCoins == null && !isPlayingAd,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Text(
                            text = adStatusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        if (!isAdLoading) {
                            IconButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    adStatusText = ""
                                    viewModel.loadRewardedAd()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Large action button
                Button(
                    onClick = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTimestamp < 1000L) {
                            return@Button
                        }
                        lastClickTimestamp = currentTime

                        if (isAdConnected && !isPlayingAd && !isAdLoading) {
                            val activity = context as? Activity
                            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                                viewModel.triggerAudioFeedback("click")
                                isPlayingAd = true
                                adStatusText = ""
                                viewModel.showRewardedAd(
                                    activity = activity,
                                    onRewarded = { rewardCoins, _ ->
                                        val finalReward = if (rewardCoins > 0) rewardCoins else 300
                                        viewModel.addRawCredits(finalReward)
                                        viewModel.triggerAudioFeedback("buy")
                                        completedRewardCoins = finalReward
                                    },
                                    onDismissed = {
                                        isPlayingAd = false
                                    },
                                    onError = { errorMsg ->
                                        isPlayingAd = false
                                        adStatusText = errorMsg
                                    }
                                )
                            }
                        }
                    },
                    enabled = isAdConnected && !isPlayingAd && !isAdLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(52.dp)
                ) {
                    if (isPlayingAd || isAdLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Смотреть"
                                Language.UA -> "Дивитись"
                                Language.KK -> "Көру"
                                Language.DE -> "Ansehen"
                                Language.ZH -> "观看"
                                else -> "Watch"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
