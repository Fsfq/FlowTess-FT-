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
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.84f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Награда"
                            Language.UA -> "Нагорода"
                            Language.KK -> "Сыйлық"
                            Language.DE -> "Belohnung"
                            Language.ZH -> "福利奖励"
                            else -> "Reward"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            if (!isPlayingAd) {
                                viewModel.triggerAudioFeedback("click")
                                onDismiss()
                            }
                        },
                        enabled = !isPlayingAd,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Minimal Icon Badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD700).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // +300
                Text(
                    text = "+300",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )

                // Current balance text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Баланс:"
                            Language.UA -> "Баланс:"
                            Language.KK -> "Баланс:"
                            Language.DE -> "Guthaben:"
                            Language.ZH -> "余额:"
                            else -> "Balance:"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,d", credits),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Reward Confirmation Banner
                AnimatedVisibility(
                    visible = completedRewardCoins != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    completedRewardCoins?.let { coins ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.12f),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "+$coins получено"
                                        Language.UA -> "+$coins отримано"
                                        Language.KK -> "+$coins алынды"
                                        Language.DE -> "+$coins erhalten"
                                        Language.ZH -> "+$coins 已到账"
                                        else -> "+$coins received"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
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
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = adStatusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        if (!isAdLoading) {
                            TextButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    adStatusText = ""
                                    viewModel.loadRewardedAd()
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Повторить"
                                        Language.UA -> "Повторити"
                                        Language.KK -> "Қайталау"
                                        Language.DE -> "Wiederholen"
                                        Language.ZH -> "重试"
                                        else -> "Retry"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Compact MD3 Button (not full width)
                Button(
                    onClick = {
                        if (isAdConnected && !isPlayingAd) {
                            val activity = context as? Activity
                            if (activity != null) {
                                viewModel.triggerAudioFeedback("click")
                                isPlayingAd = true
                                adStatusText = ""
                                viewModel.showRewardedAd(
                                    activity = activity,
                                    onRewarded = { rewardCoins, _ ->
                                        val finalReward = if (rewardCoins > 0) rewardCoins else 300
                                        viewModel.addCredits(finalReward)
                                        viewModel.triggerAudioFeedback("buy")
                                        completedRewardCoins = finalReward
                                        isPlayingAd = false
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    if (isPlayingAd || isAdLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Загрузка..."
                                Language.UA -> "Завантаження..."
                                Language.KK -> "Жүктелуде..."
                                Language.DE -> "Laden..."
                                Language.ZH -> "加载中..."
                                else -> "Loading..."
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Смотреть"
                                Language.UA -> "Дивитись"
                                Language.KK -> "Көру"
                                Language.DE -> "Ansehen"
                                Language.ZH -> "观看"
                                else -> "Watch"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
