package com.example.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.ads.YandexAdsManager
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun RewardedAdDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Магазин"
                                Language.UA -> "Магазин"
                                Language.KK -> "Дүкен"
                                Language.DE -> "Shop"
                                Language.ZH -> "商店"
                                else -> "Shop"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Balance Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Баланс"
                                Language.UA -> "Баланс"
                                Language.KK -> "Баланс"
                                Language.DE -> "Guthaben"
                                Language.ZH -> "账户余额"
                                else -> "Balance"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%,d", credits),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val watchButtonText = when (currentLang) {
                    Language.RU -> "СМОТРЕТЬ"
                    Language.UA -> "ДИВИТИСЬ"
                    Language.KK -> "КӨРУ"
                    Language.DE -> "ANSEHEN"
                    Language.ZH -> "观看"
                    else -> "WATCH"
                }

                // Offers List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Offer 1: Short Video (+250 coins)
                    ModernOfferCard(
                        icon = Icons.Default.PlayArrow,
                        title = when (currentLang) {
                            Language.RU -> "Видеоролик"
                            Language.UA -> "Відеоролик"
                            Language.KK -> "Бейнеролик"
                            Language.DE -> "Werbevideo"
                            Language.ZH -> "视频广告"
                            else -> "Video Ad"
                        },
                        description = when (currentLang) {
                            Language.RU -> "Награда за просмотр"
                            Language.UA -> "Винагорода за перегляд"
                            Language.KK -> "Көру үшін сыйлық"
                            Language.DE -> "Belohnung für das Ansehen"
                            Language.ZH -> "观看获取奖励"
                            else -> "Reward for watching"
                        },
                        rewardBadge = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "+250",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        },
                        durationLabel = "~15s",
                        buttonText = watchButtonText,
                        onWatch = {
                            viewModel.triggerAudioFeedback("click")
                        }
                    )

                    // Offer 2: Crate Key
                    ModernOfferCard(
                        icon = Icons.Default.Key,
                        title = when (currentLang) {
                            Language.RU -> "Ключ к кейсу"
                            Language.UA -> "Ключ до кейсу"
                            Language.KK -> "Кейс кілті"
                            Language.DE -> "Kistenschlüssel"
                            Language.ZH -> "宝箱钥匙"
                            else -> "Crate Key"
                        },
                        description = when (currentLang) {
                            Language.RU -> "Железный ключ для кейсов"
                            Language.UA -> "Залізний ключ для кейсів"
                            Language.KK -> "Кейстерге арналған темір кілт"
                            Language.DE -> "Eisenschlüssel für Kisten"
                            Language.ZH -> "开启武器箱的钥匙"
                            else -> "Iron key for crates"
                        },
                        rewardBadge = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "1x Iron Key",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        durationLabel = "~30s",
                        buttonText = watchButtonText,
                        onWatch = {
                            viewModel.triggerAudioFeedback("click")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernOfferCard(
    icon: ImageVector,
    title: String,
    description: String,
    rewardBadge: @Composable () -> Unit,
    durationLabel: String,
    buttonText: String,
    onWatch: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = durationLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    rewardBadge()
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onWatch,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
