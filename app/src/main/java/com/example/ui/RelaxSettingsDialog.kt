package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.game.Colors
import com.example.game.STANDARD_SHAPES
import com.example.game.Tetromino
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxSettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    RelaxSettingsScreen(
        viewModel = viewModel,
        onBack = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val sandboxImmortal by viewModel.sandboxImmortal.collectAsStateWithLifecycle()
    val sandboxGravitySpeed by viewModel.sandboxGravitySpeed.collectAsStateWithLifecycle()
    val sandboxLockDelay by viewModel.sandboxLockDelay.collectAsStateWithLifecycle()
    val sandboxGhostEnabled by viewModel.sandboxGhostEnabled.collectAsStateWithLifecycle()
    val sandboxGridLinesEnabled by viewModel.sandboxGridLinesEnabled.collectAsStateWithLifecycle()
    val sandboxBagGenerator by viewModel.sandboxBagGenerator.collectAsStateWithLifecycle()
    val sandboxSinglePieceIdx by viewModel.sandboxSinglePieceIdx.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var spawnedFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

    fun showFeedback(msg: String) {
        actionFeedbackMessage = msg
        scope.launch {
            delay(1800)
            if (actionFeedbackMessage == msg) {
                actionFeedbackMessage = null
            }
        }
    }

    BackHandler { onBack() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "ПЕСОЧНИЦА"
                                            Language.UA -> "ПІСОЧНИЦЯ"
                                            Language.KK -> "ҚҰМСАЛҒЫШ"
                                            Language.DE -> "SANDBOX-HUB"
                                            Language.ZH -> "沙盒实验室"
                                            else -> "SANDBOX HUB"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "ИЗОЛИРОВАННЫЕ НАСТРОЙКИ"
                                            Language.UA -> "ІЗОЛЬОВАНІ НАЛАШТУВАННЯ"
                                            Language.KK -> "ОҚШАУЛАНҒАН БАПТАУЛАР"
                                            Language.DE -> "ISOLIERTER MODUS"
                                            Language.ZH -> "独立沙盒模式专属配置"
                                            else -> "ISOLATED PROFILE"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    onBack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    viewModel.resetSandboxSettingsToDefaults()
                                    showFeedback(
                                        when (currentLang) {
                                            Language.RU -> "Настройки сброшены"
                                            Language.UA -> "Налаштування скинуто"
                                            Language.KK -> "Баптаулар қалпына келтірілді"
                                            Language.DE -> "Zurückgesetzt"
                                            Language.ZH -> "已恢复默认"
                                            else -> "Reset to Defaults"
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Defaults",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (actionFeedbackMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = actionFeedbackMessage ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            Button(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    onBack()
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Применить"
                                        Language.UA -> "Застосувати"
                                        Language.KK -> "Қолдану"
                                        Language.DE -> "Übernehmen"
                                        Language.ZH -> "保存并返回"
                                        else -> "Apply & Return"
                                    },
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── SECTION 1: Immortality & Visual Helpers ──
                    SandboxSectionHeader(
                        icon = Icons.Default.Shield,
                        title = when (currentLang) {
                            Language.RU -> "ПРАВИЛА И ВИЗУАЛ"
                            Language.UA -> "ПРАВИЛА ТА ВІЗУАЛ"
                            Language.KK -> "ЕРЕЖЕЛЕР МЕН КӨРІНІС"
                            Language.DE -> "REGELN & VISUELLES"
                            Language.ZH -> "模式规则与辅助线"
                            else -> "RULES & VISUALS"
                        }
                    )

                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Immortality
                            SandboxToggleRow(
                                title = when (currentLang) {
                                    Language.RU -> "Бессмертие (без Game Over)"
                                    Language.UA -> "Безсмертя (без Game Over)"
                                    Language.KK -> "Өлместік (Game Over жоқ)"
                                    Language.DE -> "Unsterblichkeit (Kein Game Over)"
                                    Language.ZH -> "无限永生 (永不游戏结束)"
                                    else -> "Immortality (No Game Over)"
                                },
                                subtitle = when (currentLang) {
                                    Language.RU -> "При переполнении потолка блоки срезаются без поражения"
                                    Language.UA -> "При переповненні стелі блоки зрізаються без поразки"
                                    Language.KK -> "Төбе толғанда блоктар жеңіліссіз тазартылады"
                                    Language.DE -> "Automatischer Überlauf-Schutz ohne Spielende"
                                    Language.ZH -> "方块堆砌封顶时自动切除顶层，永不判定失败"
                                    else -> "Clears upper overflow rows automatically on top-out"
                                },
                                checked = sandboxImmortal,
                                onCheckedChange = {
                                    viewModel.setSandboxImmortal(it)
                                    viewModel.triggerAudioFeedback("click")
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )

                            // Ghost Piece
                            SandboxToggleRow(
                                title = when (currentLang) {
                                    Language.RU -> "Проекция фигуры (Ghost)"
                                    Language.UA -> "Проекція фігури (Ghost)"
                                    Language.KK -> "Көлеңке сұлбасы (Ghost)"
                                    Language.DE -> "Geister-Vorschau (Ghost)"
                                    Language.ZH -> "落点全息幻影 (Ghost Piece)"
                                    else -> "Ghost Piece Silhouette"
                                },
                                subtitle = when (currentLang) {
                                    Language.RU -> "Отображает контур приземления активного блока"
                                    Language.UA -> "Відображає контур приземлення активного блоку"
                                    Language.KK -> "Белсенді блоктың түсетін орнын көрсетеді"
                                    Language.DE -> "Zeigt die Kontur der Landeposition am Boden"
                                    Language.ZH -> "在底部实时指引当前方块的预投影落点"
                                    else -> "Shows landing shadow outline on the floor"
                                },
                                checked = sandboxGhostEnabled,
                                onCheckedChange = {
                                    viewModel.setSandboxGhostEnabled(it)
                                    viewModel.triggerAudioFeedback("click")
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )

                            // Grid Guidelines
                            SandboxToggleRow(
                                title = when (currentLang) {
                                    Language.RU -> "Направляющие линии сетки"
                                    Language.UA -> "Напрямні лінії сітки"
                                    Language.KK -> "Тордың бағыттаушы сызықтары"
                                    Language.DE -> "Gitter-Führungslinien"
                                    Language.ZH -> "网格坐标引导线"
                                    else -> "Grid Alignment Guidelines"
                                },
                                subtitle = when (currentLang) {
                                    Language.RU -> "Сетка между столбцами и рядами для точного прицела"
                                    Language.UA -> "Сітка між стовпцями та рядами для точного прицілу"
                                    Language.KK -> "Дәл көздеу үшін бағандар мен жолдар арасындағы тор"
                                    Language.DE -> "Dezente Ausrichtungslinien auf dem Spielfeld"
                                    Language.ZH -> "绘制纵向与横向微光网格便于微调定位"
                                    else -> "Displays subtle matrix grid lines on the playfield"
                                },
                                checked = sandboxGridLinesEnabled,
                                onCheckedChange = {
                                    viewModel.setSandboxGridLinesEnabled(it)
                                    viewModel.triggerAudioFeedback("click")
                                }
                            )
                        }
                    }

                    // ── SECTION 2: Gravity Speed Slider/Stepper ──
                    SandboxSectionHeader(
                        icon = Icons.Default.Speed,
                        title = when (currentLang) {
                            Language.RU -> "СКОРОСТЬ ГРАВИТАЦИИ ПАДЕНИЯ"
                            Language.UA -> "ШВИДКІСТЬ ГРАВІТАЦІЇ"
                            Language.KK -> "ҚҰЛАУ ЖЫЛДАМДЫҒЫ"
                            Language.DE -> "FALL-GESCHWINDIGKEIT"
                            Language.ZH -> "下落重力流速"
                            else -> "GRAVITY SPEED & TIMING"
                        }
                    )

                    val gravitySpeeds = listOf(
                        Triple(
                            0,
                            when (currentLang) {
                                Language.RU -> "Без гравитации (Ручной сброс)"
                                Language.UA -> "Без гравітації (Ручний спуск)"
                                Language.KK -> "Гравитациясыз (Қолмен)"
                                Language.DE -> "Null-Gravitation (Manuell)"
                                Language.ZH -> "零重力静止 (纯手动下落)"
                                else -> "Zero Gravity (Manual Drop)"
                            },
                            "∞"
                        ),
                        Triple(1, "3.0s", when (currentLang) {
                            Language.RU -> "Ультра-медленная (3.0 сек)"
                            else -> "Ultra Slow (3.0s)"
                        }),
                        Triple(2, "2.0s", when (currentLang) {
                            Language.RU -> "Очень медленная (2.0 сек)"
                            else -> "Very Slow (2.0s)"
                        }),
                        Triple(3, "1.5s", when (currentLang) {
                            Language.RU -> "Медитативная (1.5 сек)"
                            Language.UA -> "Медитативна (1.5 сек)"
                            Language.DE -> "Meditativ (1.5s)"
                            Language.ZH -> "禅意缓流 (1.5秒)"
                            else -> "Meditative (1.5s)"
                        }),
                        Triple(4, "1.0s", when (currentLang) {
                            Language.RU -> "Размеренная (1.0 сек)"
                            Language.UA -> "Розмірена (1.0 сек)"
                            Language.DE -> "Entspannt (1.0s)"
                            Language.ZH -> "闲适流速 (1.0秒)"
                            else -> "Relaxed (1.0s)"
                        }),
                        Triple(5, "0.5s", when (currentLang) {
                            Language.RU -> "Стандартная (0.5 сек)"
                            Language.UA -> "Стандартна (0.5 сек)"
                            Language.DE -> "Standard (0.5s)"
                            Language.ZH -> "经典流速 (0.5秒)"
                            else -> "Standard (0.5s)"
                        }),
                        Triple(6, "0.2s", when (currentLang) {
                            Language.RU -> "Быстрая (0.2 сек)"
                            Language.UA -> "Швидка (0.2 сек)"
                            Language.DE -> "Flott (0.2s)"
                            Language.ZH -> "敏捷极速 (0.2秒)"
                            else -> "Brisk (0.2s)"
                        }),
                        Triple(7, "0.05s", when (currentLang) {
                            Language.RU -> "Гипер-дроп (0.05 сек)"
                            Language.UA -> "Гіпер-дроп (0.05 сек)"
                            Language.DE -> "Hyperschall (0.05s)"
                            Language.ZH -> "光速冲击 (0.05秒)"
                            else -> "Hyper Sonic (0.05s)"
                        })
                    )

                    val currentSpeedTriple = gravitySpeeds.getOrElse(sandboxGravitySpeed) { gravitySpeeds[3] }

                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Active speed highlight card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentSpeedTriple.second,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = currentSpeedTriple.third,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "#${sandboxGravitySpeed + 1}/8",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Stepper Slider
                            Slider(
                                value = sandboxGravitySpeed.toFloat(),
                                onValueChange = {
                                    val idx = it.toInt().coerceIn(0, 7)
                                    if (idx != sandboxGravitySpeed) {
                                        viewModel.setSandboxGravitySpeed(idx)
                                        viewModel.triggerAudioFeedback("click")
                                    }
                                },
                                valueRange = 0f..7f,
                                steps = 6,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(0 to "Zero G", 3 to "1.5s", 5 to "0.5s", 7 to "0.05s").forEach { (idx, chipLabel) ->
                                    val isCurrent = sandboxGravitySpeed == idx
                                    FilterChip(
                                        selected = isCurrent,
                                        onClick = {
                                            viewModel.setSandboxGravitySpeed(idx)
                                            viewModel.triggerAudioFeedback("click")
                                        },
                                        label = {
                                            Text(
                                                text = chipLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── SECTION 3: Lock Delay Mechanics ──
                    SandboxSectionHeader(
                        icon = Icons.Default.Timer,
                        title = when (currentLang) {
                            Language.RU -> "ЗАДЕРЖКА ФИКСАЦИИ (LOCK DELAY)"
                            Language.UA -> "ЗАТРИМКА ФІКСАЦІЇ"
                            Language.KK -> "БЕКІТУ КІДІРІСІ"
                            Language.DE -> "LOCK-DELAY (VERZÖGERUNG)"
                            Language.ZH -> "触底锁定延迟 (Lock Delay)"
                            else -> "LOCK DELAY MECHANICS"
                        }
                    )

                    val lockDelayOptions = listOf(
                        "instant" to ("0 ms" to when (currentLang) {
                            Language.RU -> "Мгновенная (0мс)"
                            else -> "Instant (0ms)"
                        }),
                        "200ms" to ("200 ms" to when (currentLang) {
                            Language.RU -> "Быстрая (200мс)"
                            else -> "Fast (200ms)"
                        }),
                        "500ms" to ("500 ms" to when (currentLang) {
                            Language.RU -> "Стандарт (500мс)"
                            else -> "Standard (500ms)"
                        }),
                        "1000ms" to ("1.0 s" to when (currentLang) {
                            Language.RU -> "Увеличенная (1.0с)"
                            else -> "Generous (1.0s)"
                        }),
                        "2000ms" to ("2.0 s" to when (currentLang) {
                            Language.RU -> "Свободная (2.0с)"
                            else -> "Forgiving (2.0s)"
                        }),
                        "infinite" to ("∞" to when (currentLang) {
                            Language.RU -> "Бесконечная (Ручная)"
                            else -> "Infinite (Manual)"
                        })
                    )

                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Время скольжения и вращения фигуры после касания пола:"
                                    Language.UA -> "Час ковзання фігури після торкання дна:"
                                    Language.KK -> "Еденге тигеннен кейінгі сырғу уақыты:"
                                    Language.DE -> "Verzögerung für Drehung/Bewegung auf dem Boden:"
                                    Language.ZH -> "方块触底后允许继续滑动旋转的缓冲时限："
                                    else -> "Allows sliding and rotation before piece is locked permanently:"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Grid of lock delay chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                lockDelayOptions.take(3).forEach { (key, pair) ->
                                    val isSel = sandboxLockDelay == key
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        border = if (isSel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.setSandboxLockDelay(key)
                                                viewModel.triggerAudioFeedback("click")
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = pair.first,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = pair.second,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                lockDelayOptions.drop(3).forEach { (key, pair) ->
                                    val isSel = sandboxLockDelay == key
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        border = if (isSel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.setSandboxLockDelay(key)
                                                viewModel.triggerAudioFeedback("click")
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = pair.first,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = pair.second,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── SECTION 4: Bag Generator Picker ──
                    SandboxSectionHeader(
                        icon = Icons.Default.Shuffle,
                        title = when (currentLang) {
                            Language.RU -> "ГЕНЕРАТОР ОЧЕРЕДИ ФИГУР (BAG RANDOMIZER)"
                            Language.UA -> "ГЕНЕРАТОР ЧЕРГИ ФІГУР"
                            Language.KK -> "ПІШІНДЕР КЕЗЕГІНІҢ ГЕНЕРАТОРЫ"
                            Language.DE -> "FIGUREN-GENERATOR"
                            Language.ZH -> "方块出样生成算法"
                            else -> "BAG GENERATOR & RANDOMIZER"
                        }
                    )

                    val bagGenerators = listOf(
                        "standard_7" to (
                            when (currentLang) {
                                Language.RU -> "Стандартный 7-Bag"
                                Language.UA -> "Стандартний 7-Bag"
                                Language.KK -> "Стандартты 7-Bag"
                                Language.DE -> "Standard 7-Bag"
                                Language.ZH -> "标准7-Bag循环算法"
                                else -> "Standard 7-Bag"
                            } to when (currentLang) {
                                Language.RU -> "Каноничная честная тасовка 7 тетромино без длинных засух"
                                else -> "Canonical fair shuffle of all 7 tetrominoes"
                            }
                        ),
                        "full_random" to (
                            when (currentLang) {
                                Language.RU -> "Абсолютный рандом"
                                Language.UA -> "Повний рандом"
                                Language.KK -> "Толық кездейсоқ"
                                Language.DE -> "Vollkommen Zufällig"
                                Language.ZH -> "完全混沌真随机"
                                else -> "Full Random (No Bag)"
                            } to when (currentLang) {
                                Language.RU -> "Каждая фигура выпадает с независимой вероятностью 1/7"
                                else -> "Each piece chosen uniformly at random"
                            }
                        ),
                        "single_piece" to (
                            when (currentLang) {
                                Language.RU -> "Одна фигура (Custom Single)"
                                Language.UA -> "Одна фігура"
                                Language.KK -> "Бір пішін ғана"
                                Language.DE -> "Einzelne Figur (Endlos)"
                                Language.ZH -> "专属单体方块狂欢"
                                else -> "Custom Single Piece"
                            } to when (currentLang) {
                                Language.RU -> "Бесконечно генерируется только выбранная вами фигура"
                                else -> "Only the chosen tetromino will spawn repeatedly"
                            }
                        ),
                        "pentaminoes" to (
                            when (currentLang) {
                                Language.RU -> "Все 10 фигур (+ пентамино)"
                                Language.UA -> "Всі 10 фігур (+ пентаміно)"
                                Language.KK -> "Барлық 10 пішін (+ пентамино)"
                                Language.DE -> "Alle 10 Formen (+ Pentamino)"
                                Language.ZH -> "异形全家桶 (+五连方块)"
                                else -> "Pentaminoes & Extended (10 Shapes)"
                            } to when (currentLang) {
                                Language.RU -> "Классические 7 фигур + крест, U-форма и моно-точка"
                                else -> "Standard 7 + Plus-cross, U-shape, and Dot mino"
                            }
                        )
                    )

                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bagGenerators.forEach { (key, pair) ->
                                val isSelected = sandboxBagGenerator == key
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSandboxBagGenerator(key)
                                            viewModel.triggerAudioFeedback("click")
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.setSandboxBagGenerator(key)
                                                viewModel.triggerAudioFeedback("click")
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pair.first,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = pair.second,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // If single_piece is selected, show mini selector of which piece
                            if (sandboxBagGenerator == "single_piece") {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Выберите фигуру для бесконечного спавна:"
                                                Language.UA -> "Оберіть фігуру для нескінченного спавну:"
                                                Language.KK -> "Шексіз шығару үшін пішінді таңдаңыз:"
                                                Language.DE -> "Wähle die Figur für Endlos-Spawn:"
                                                Language.ZH -> "请指定需要无限连续掉落的目标方块："
                                                else -> "Select target tetromino for single-piece generator:"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        val pieceLabels = listOf("I", "J", "L", "O", "S", "T", "Z")
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            STANDARD_SHAPES.forEachIndexed { idx, shape ->
                                                val isCur = sandboxSinglePieceIdx == idx
                                                val pieceColor = Colors.getOrElse(shape.colorIndex) { Color.Cyan }
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isCur) pieceColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    border = if (isCur) BorderStroke(2.dp, pieceColor) else null,
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clickable {
                                                            viewModel.setSandboxSinglePieceIdx(idx)
                                                            viewModel.triggerAudioFeedback("click")
                                                        }
                                                ) {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        MiniTetrominoCanvas(
                                                            piece = shape,
                                                            sizeDp = 28.dp
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

                    // ── SECTION 5: Live Piece Spawner Palette ──
                    SandboxSectionHeader(
                        icon = Icons.Default.AddBox,
                        title = when (currentLang) {
                            Language.RU -> "ПАЛИТРА МГНОВЕННОГО СПАВНА"
                            Language.UA -> "ПАЛІТРА МИТТЄВОГО СПАВНУ"
                            Language.KK -> "ЛЕЗДІК СПАВН ПАЛИТРАСЫ"
                            Language.DE -> "LIVE PIECE-SPAWNER"
                            Language.ZH -> "实时方块召唤面板"
                            else -> "INTERACTIVE PIECE SPAWNER"
                        }
                    )

                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Нажмите на фигуру, чтобы мгновенно сбросить её на игровое поле:"
                                    Language.UA -> "Натисніть на фігуру, щоб миттєво викликати її на поле:"
                                    Language.KK -> "Фигураны ойын алаңына лезде шығару үшін түртіңіз:"
                                    Language.DE -> "Tippe auf eine Form, um sie sofort ins Feld zu spawnen:"
                                    Language.ZH -> "点击下方任意方块即可立即召唤并替换当前下落方块："
                                    else -> "Tap any polyomino to immediately spawn and control it on the active board:"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Row of 7 mini clickable pieces (I, O, T, S, Z, J, L)
                            // Canonical standard order mapped for intuitive palette
                            val paletteIndices = listOf(0, 3, 5, 4, 6, 1, 2) // I, O, T, S, Z, J, L
                            val paletteNames = listOf("I", "O", "T", "S", "Z", "J", "L")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                paletteIndices.forEachIndexed { i, pieceIdx ->
                                    val shape = STANDARD_SHAPES.getOrNull(pieceIdx) ?: STANDARD_SHAPES[0]
                                    val pieceName = paletteNames[i]
                                    val pieceColor = Colors.getOrElse(shape.colorIndex) { Color.Cyan }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        border = BorderStroke(1.dp, pieceColor.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(0.85f)
                                            .clickable {
                                                viewModel.spawnRelaxPiece(pieceIdx)
                                                viewModel.triggerAudioFeedback("select")
                                                spawnedFeedbackMessage = "$pieceName-Piece"
                                                showFeedback(
                                                    when (currentLang) {
                                                        Language.RU -> "Фигура $pieceName добавлена на поле!"
                                                        Language.UA -> "Фігуру $pieceName додано на поле!"
                                                        Language.KK -> "$pieceName пішіні алаңға қосылды!"
                                                        Language.DE -> "$pieceName-Figur gespawnt!"
                                                        Language.ZH -> "已在棋盘生成 $pieceName 方块！"
                                                        else -> "Spawned $pieceName piece!"
                                                    }
                                                )
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                MiniTetrominoCanvas(
                                                    piece = shape,
                                                    sizeDp = 26.dp
                                                )
                                            }
                                            Text(
                                                text = pieceName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = pieceColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── SECTION 6: Field Manipulation Board Tools ──
                    SandboxSectionHeader(
                        icon = Icons.Default.Build,
                        title = when (currentLang) {
                            Language.RU -> "ИНСТРУМЕНТЫ МАНИПУЛЯЦИИ ПОЛЕМ"
                            Language.UA -> "ІНСТРУМЕНТИ МАНІПУЛЯЦІЇ ПОЛЕМ"
                            Language.KK -> "АЛАҢ БАПТАУ ҚҰРАЛДАРЫ"
                            Language.DE -> "FELD-WERKZEUGE & AKTIONEN"
                            Language.ZH -> "棋盘编辑与操控工坊"
                            else -> "BOARD MANIPULATION TOOLS"
                        }
                    )

                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Прямое изменение матричной сетки текущего раунда:"
                                    Language.UA -> "Пряма зміна матричної сітки раунду:"
                                    Language.KK -> "Ойын алаңының торын тікелей өзгерту:"
                                    Language.DE -> "Direkte Manipulation des Spielfelds:"
                                    Language.ZH -> "即时修改与编辑当前棋盘方格状态："
                                    else -> "Directly transform or clear blocks in the active game matrix:"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Row 1: Cut Lower 4 & Cut Lower 8
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        viewModel.cutRelaxLowerRows(4)
                                        viewModel.triggerAudioFeedback("clear")
                                        showFeedback(
                                            when (currentLang) {
                                                Language.RU -> "Срезано 4 нижних ряда"
                                                else -> "Cut Lower 4 Rows"
                                            }
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerticalAlignBottom,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Срез низа (4)"
                                            Language.UA -> "Зріз низу (4)"
                                            Language.DE -> "Cut unten (4)"
                                            Language.ZH -> "切底4行"
                                            else -> "Cut 4 Rows"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        viewModel.cutRelaxLowerRows(8)
                                        viewModel.triggerAudioFeedback("clear")
                                        showFeedback(
                                            when (currentLang) {
                                                Language.RU -> "Срезано 8 нижних рядов"
                                                else -> "Cut Lower 8 Rows"
                                            }
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerticalAlignBottom,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Срез низа (8)"
                                            Language.UA -> "Зріз низу (8)"
                                            Language.DE -> "Cut unten (8)"
                                            Language.ZH -> "切底8行"
                                            else -> "Cut 8 Rows"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Row 2: Fill Bottom 2 Random Lines & Invert Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.fillRelaxBottomRandomLines()
                                        viewModel.triggerAudioFeedback("select")
                                        showFeedback(
                                            when (currentLang) {
                                                Language.RU -> "Добавлены 2 случайные линии снизу"
                                                else -> "Filled 2 Bottom Lines"
                                            }
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "+2 линии снизу"
                                            Language.UA -> "+2 лінії знизу"
                                            Language.DE -> "+2 Zeilen unten"
                                            Language.ZH -> "+2底线"
                                            else -> "+2 Lines"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.invertRelaxField()
                                        viewModel.triggerAudioFeedback("select")
                                        showFeedback(
                                            when (currentLang) {
                                                Language.RU -> "Поле инвертировано!"
                                                else -> "Field Inverted!"
                                            }
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Инверсия поля"
                                            Language.UA -> "Інверсія поля"
                                            Language.DE -> "Invertieren"
                                            Language.ZH -> "棋盘反转"
                                            else -> "Invert Field"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Row 3: Clear Entire Board
                            Button(
                                onClick = {
                                    viewModel.clearRelaxBoard()
                                    viewModel.triggerAudioFeedback("clear")
                                    showFeedback(
                                        when (currentLang) {
                                            Language.RU -> "Всё поле очищено"
                                            else -> "Board Cleared"
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Очистить всё поле целиком"
                                        Language.UA -> "Очистити все поле"
                                        Language.KK -> "Бүкіл алаңды тазалау"
                                        Language.DE -> "Komplettes Feld leeren"
                                        Language.ZH -> "清空整盘所有方块"
                                        else -> "Clear Entire Board"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
}

@Composable
private fun SandboxSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SandboxToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun MiniTetrominoCanvas(
    piece: Tetromino,
    sizeDp: Dp = 26.dp
) {
    val pieceColor = Colors.getOrElse(piece.colorIndex) { Color.Cyan }
    Canvas(modifier = Modifier.size(sizeDp)) {
        val minX = piece.shape.minOf { it.x }
        val maxX = piece.shape.maxOf { it.x }
        val minY = piece.shape.minOf { it.y }
        val maxY = piece.shape.maxOf { it.y }

        val spanX = maxX - minX + 1
        val spanY = maxY - minY + 1
        val maxSpan = maxOf(spanX, spanY, 3)

        val blockSize = (size.minDimension / maxSpan.toFloat()) * 0.88f
        val totalW = spanX * blockSize
        val totalH = spanY * blockSize
        val startX = (size.width - totalW) / 2f - minX * blockSize
        val startY = (size.height - totalH) / 2f - minY * blockSize

        piece.shape.forEach { p ->
            val bx = startX + p.x * blockSize
            val by = startY + p.y * blockSize
            drawRoundRect(
                color = pieceColor,
                topLeft = Offset(bx + 1f, by + 1f),
                size = Size(blockSize - 2f, blockSize - 2f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
