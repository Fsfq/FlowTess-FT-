package com.example.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.GameMode
import kotlinx.coroutines.delay

/**
 * Screen for choosing between Server-based Multiplayer and Epic Games Online Services (EOS) P2P mode.
 * Minimalist, compact, Material Design 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerSelectScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSelectServer: () -> Unit,
    onSelectEosP2p: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val activeThemeKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = themeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "ВЫБОР СЕТИ"
                                    Language.UA -> "ВИБІР МЕРЕЖІ"
                                    Language.KK -> "ЖЕЛІ ТАҢДАУ"
                                    Language.DE -> "NETZWERKWAHL"
                                    Language.ZH -> "网络模式选择"
                                    else -> "MULTIPLAYER"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }
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
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── CARD 1: DEDICATED SERVER ──
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectServer()
                        },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = themeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Игровой Сервер"
                                        Language.UA -> "Ігровий Сервер"
                                        Language.KK -> "Ойын Сервері"
                                        Language.DE -> "Dedizierter Server"
                                        Language.ZH -> "官方云端服务器"
                                        else -> "Dedicated Server"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Комнаты, подбор, чат и рейтинг"
                                        Language.UA -> "Кімнати, підбір, чат та рейтинг"
                                        Language.KK -> "Бөлмелер, іздеу, чат және рейтинг"
                                        Language.DE -> "Räume, Matchmaking, Chat & Rangliste"
                                        Language.ZH -> "公开房间、快速匹配、大厅聊天与排位"
                                        else -> "Lobbies, matchmaking, chat & ranks"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectServer()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Войти на Сервер"
                                        Language.UA -> "Увійти на Сервер"
                                        Language.KK -> "Серверге кіру"
                                        Language.DE -> "Server beitreten"
                                        Language.ZH -> "进入服务器大厅"
                                        else -> "Enter Server Lobby"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // ── CARD 2: EPIC GAMES ONLINE SERVICES (EOS P2P) ──
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.triggerAudioFeedback("click")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectEosP2p()
                        },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = themeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "P2P Epic Games EOS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Прямое соединение, минимум пинга и коды сессий"
                                        Language.UA -> "Пряме з'єднання, мінімум пінгу та коди сесій"
                                        Language.KK -> "Тікелей байланыс, ең төмен пинг және сессия коды"
                                        Language.DE -> "Direkte Peer-to-Peer Verbindung via EOS"
                                        Language.ZH -> "超低延迟 P2P 直连与房间码对决"
                                        else -> "Direct peer mesh & session codes"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.triggerAudioFeedback("click")
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectEosP2p()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lan, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Запустить P2P EOS"
                                        Language.UA -> "Запустити P2P EOS"
                                        Language.KK -> "P2P EOS іске қосу"
                                        Language.DE -> "P2P EOS starten"
                                        Language.ZH -> "开启 P2P EOS 模式"
                                        else -> "Launch P2P EOS"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
