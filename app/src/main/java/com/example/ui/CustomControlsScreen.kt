package com.example.ui

import androidx.activity.compose.BackHandler

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Leaderboard
import java.util.Locale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.db.FriendUser
import com.example.db.PublicUserProfile
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.MainViewModel
import com.example.db.HighScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.TileMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomControlsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val themeColorVal = MaterialTheme.colorScheme.primary

    val controlButtonScale by viewModel.controlButtonScale.collectAsStateWithLifecycle()
    val controlButtonAlpha by viewModel.controlButtonAlpha.collectAsStateWithLifecycle()
    val controlStyle by viewModel.controlStyle.collectAsStateWithLifecycle()
    val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()
    val controlVerticalPosition by viewModel.controlVerticalPosition.collectAsStateWithLifecycle()
    val controlDas by viewModel.controlDas.collectAsStateWithLifecycle()
    val controlArr by viewModel.controlArr.collectAsStateWithLifecycle()
    val controlBottomPadding by viewModel.controlBottomPadding.collectAsStateWithLifecycle()
    val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()
    val purchasedControlButtonStyles by viewModel.purchasedControlButtonStyles.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCodeText by remember { mutableStateOf("") }
    var importErrorText by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Сбросить управление?"
                        Language.UA -> "Скинути керування?"
                        Language.KK -> "Басқаруды қайтару?"
                        Language.DE -> "Steuerung zurücksetzen?"
                        Language.ZH -> "重置按键设置？"
                        else -> "Reset controls?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Вернуть все параметры кнопок, таймингов и макета к рекомендованным настройкам?"
                        Language.UA -> "Повернути всі параметри кнопок, таймінгів та макета до рекомендованих налаштувань?"
                        Language.KK -> "Барлық баптауларды бастапқы қалыпқа қайтару керек пе?"
                        Language.DE -> "Alle Tasten-, Timing- und Layout-Einstellungen auf Standardwerte zurücksetzen?"
                        Language.ZH -> "是否将所有按键、延迟与布局参数恢复为推荐默认值？"
                        else -> "Reset all button sizes, timings, and layout to recommended defaults?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetControlConfigToDefaults()
                        viewModel.triggerAudioFeedback("click")
                        showResetDialog = false
                        android.widget.Toast.makeText(
                            context,
                            when (currentLang) {
                                Language.RU -> "Настройки управления сброшены к стандартным"
                                Language.UA -> "Налаштування керування скинуто"
                                Language.KK -> "Басқару баптаулары бастапқы күйге қайтарылды"
                                Language.DE -> "Steuerungseinstellungen zurückgesetzt"
                                Language.ZH -> "按键设置已恢复默认"
                                else -> "Control settings reset to default"
                            },
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Сбросить"
                            Language.UA -> "Скинути"
                            Language.KK -> "Қайтару"
                            Language.DE -> "Zurücksetzen"
                            Language.ZH -> "重置"
                            else -> "Reset"
                        },
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Отмена"
                            Language.UA -> "Скасувати"
                            Language.KK -> "Болдырмау"
                            Language.DE -> "Abbrechen"
                            Language.ZH -> "取消"
                            else -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importErrorText = null
            },
            title = {
                Text(
                    text = when (currentLang) {
                        Language.RU -> "Импорт кода управления"
                        Language.UA -> "Імпорт коду керування"
                        Language.KK -> "Басқару кодын импорттау"
                        Language.DE -> "Steuerungscode importieren"
                        Language.ZH -> "导入按键配置代码"
                        else -> "Import Control Code"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Вставьте код конфигурации (TTR-CTRL:... или Base64 JSON):"
                            Language.UA -> "Вставте код конфігурації (TTR-CTRL:... або Base64 JSON):"
                            Language.KK -> "Конфигурация кодын қойыңыз:"
                            Language.DE -> "Konfigurationscode einfügen (TTR-CTRL:... oder Base64):"
                            Language.ZH -> "粘贴配置代码（TTR-CTRL:... 或 Base64 JSON）："
                            else -> "Paste configuration code (TTR-CTRL:... or Base64 JSON):"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importCodeText,
                        onValueChange = {
                            importCodeText = it
                            importErrorText = null
                        },
                        placeholder = { Text("TTR-CTRL:...") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        isError = importErrorText != null
                    )
                    if (importErrorText != null) {
                        Text(
                            text = importErrorText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    TextButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                importCodeText = clip.trim()
                                importErrorText = null
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Вставить из буфера"
                                Language.UA -> "Вставити з буфера"
                                Language.KK -> "Буферден қою"
                                Language.DE -> "Aus Zwischenablage einfügen"
                                Language.ZH -> "从剪贴板粘贴"
                                else -> "Paste from clipboard"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importControlConfigCode(importCodeText)
                        if (success) {
                            viewModel.triggerAudioFeedback("click")
                            showImportDialog = false
                            importCodeText = ""
                            importErrorText = null
                            android.widget.Toast.makeText(
                                context,
                                when (currentLang) {
                                    Language.RU -> "Конфигурация управления успешно применена!"
                                    Language.UA -> "Конфігурацію успішно застосовано!"
                                    Language.KK -> "Басқару конфигурациясы сәтті қолданылды!"
                                    Language.DE -> "Steuerungskonfiguration erfolgreich übernommen!"
                                    Language.ZH -> "按键配置已成功应用！"
                                    else -> "Control configuration applied successfully!"
                                },
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.triggerAudioFeedback("error")
                            importErrorText = when (currentLang) {
                                Language.RU -> "Неверный формат кода управления!"
                                Language.UA -> "Невірний формат коду керування!"
                                Language.KK -> "Басқару кодының форматы қате!"
                                Language.DE -> "Ungültiges Code-Format!"
                                Language.ZH -> "配置代码格式错误！"
                                else -> "Invalid control code format!"
                            }
                        }
                    }
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Применить"
                            Language.UA -> "Застосувати"
                            Language.KK -> "Қолдану"
                            Language.DE -> "Anwenden"
                            Language.ZH -> "应用"
                            else -> "Apply"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        importErrorText = null
                    }
                ) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Отмена"
                            Language.UA -> "Скасувати"
                            Language.KK -> "Болдырмау"
                            Language.DE -> "Abbrechen"
                            Language.ZH -> "取消"
                            else -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "КАСТОМИЗАЦИЯ УПРАВЛЕНИЯ"
                            Language.UA -> "КАСТОМІЗАЦІЯ КЕРУВАННЯ"
                            Language.KK -> "БАСҚАРУДЫ ТЕҢШЕУ"
                            Language.DE -> "STEUERUNG ANPASSEN"
                            Language.ZH -> "自定义按键布局"
                            else -> "CUSTOMIZE CONTROLS"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset to defaults")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Modern MD3 Navigation Tab Row
            val tabs = listOf(
                when (currentLang) {
                    Language.RU -> "Макет"
                    Language.UA -> "Макет"
                    Language.KK -> "Макет"
                    Language.DE -> "Layout"
                    Language.ZH -> "布局"
                    else -> "Layout"
                } to Icons.Default.Gamepad,
                when (currentLang) {
                    Language.RU -> "Параметры"
                    Language.UA -> "Параметри"
                    Language.KK -> "Баптаулар"
                    Language.DE -> "Parameter"
                    Language.ZH -> "参数"
                    else -> "Settings"
                } to Icons.Default.Tune,
                when (currentLang) {
                    Language.RU -> "Код"
                    Language.UA -> "Код"
                    Language.KK -> "Код"
                    Language.DE -> "Code"
                    Language.ZH -> "代码"
                    else -> "Code"
                } to Icons.Default.Share
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            selectedTab = index
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.sp
                                ),
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }

            when (selectedTab) {
                // TAB 0: LAYOUT PRESETS, HANDEDNESS, VERTICAL POS & VISUAL STYLE
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Presets Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "СХЕМА РАСПОЛОЖЕНИЯ КНОПОК"
                                        Language.UA -> "СХЕМА РОЗТАШУВАННЯ КНОПОК"
                                        Language.KK -> "БАТЫРМАЛАР ОРНАЛАСУЫ"
                                        Language.DE -> "TASTEN-LAYOUT SCHEME"
                                        Language.ZH -> "按键布局方案"
                                        else -> "BUTTON LAYOUT SCHEME"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                data class ControlPresetItem(
                                    val key: String,
                                    val icon: androidx.compose.ui.graphics.vector.ImageVector,
                                    val title: String,
                                    val desc: String
                                )
                                val presetList = listOf(
                                    ControlPresetItem(
                                        "split",
                                        Icons.Default.SwapHoriz,
                                        when (currentLang) {
                                            Language.RU -> "Сплит"
                                            Language.UA -> "Спліт"
                                            Language.KK -> "Бөлінген"
                                            Language.DE -> "Split"
                                            Language.ZH -> "分列布局"
                                            else -> "Split"
                                        },
                                        when (currentLang) {
                                            Language.RU -> "2 пальца"
                                            Language.UA -> "2 пальці"
                                            Language.KK -> "2 бармақ"
                                            Language.DE -> "2 Daumen"
                                            Language.ZH -> "双指掌控"
                                            else -> "2 fingers"
                                        }
                                    ),
                                    ControlPresetItem(
                                        "classic",
                                        Icons.Default.Gamepad,
                                        when (currentLang) {
                                            Language.RU -> "Классика"
                                            Language.UA -> "Класика"
                                            Language.KK -> "Классика"
                                            Language.DE -> "Klassisch"
                                            Language.ZH -> "经典横排"
                                            else -> "Classic"
                                        },
                                        when (currentLang) {
                                            Language.RU -> "4 в ряд"
                                            Language.UA -> "4 в ряд"
                                            Language.KK -> "4 қатар"
                                            Language.DE -> "4 in Reihe"
                                            Language.ZH -> "并列4键"
                                            else -> "4 in line"
                                        }
                                    ),
                                    ControlPresetItem(
                                        "arcade",
                                        Icons.Default.SportsEsports,
                                        when (currentLang) {
                                            Language.RU -> "Аркада"
                                            Language.UA -> "Аркада"
                                            Language.KK -> "Аркада"
                                            Language.DE -> "Arcade"
                                            Language.ZH -> "街机十字"
                                            else -> "Arcade"
                                        },
                                        when (currentLang) {
                                            Language.RU -> "Крестовина"
                                            Language.UA -> "Хрестовина"
                                            Language.KK -> "Бағыттауыш"
                                            Language.DE -> "D-Pad"
                                            Language.ZH -> "D-Pad"
                                            else -> "D-Pad"
                                        }
                                    ),
                                    ControlPresetItem(
                                        "one_hand_right",
                                        Icons.Default.PhoneAndroid,
                                        when (currentLang) {
                                            Language.RU -> "Для правой"
                                            Language.UA -> "Для правої"
                                            Language.KK -> "Оң қолға"
                                            Language.DE -> "Rechts"
                                            Language.ZH -> "右手模式"
                                            else -> "Right hand"
                                        },
                                        when (currentLang) {
                                            Language.RU -> "Одна рука"
                                            Language.UA -> "Одна рука"
                                            Language.KK -> "Бір қолмен"
                                            Language.DE -> "Einhand"
                                            Language.ZH -> "单手操作"
                                            else -> "One-hand"
                                        }
                                    ),
                                    ControlPresetItem(
                                        "one_hand_left",
                                        Icons.Default.PhoneAndroid,
                                        when (currentLang) {
                                            Language.RU -> "Для левой"
                                            Language.UA -> "Для лівої"
                                            Language.KK -> "Сол қолға"
                                            Language.DE -> "Links"
                                            Language.ZH -> "左手模式"
                                            else -> "Left hand"
                                        },
                                        when (currentLang) {
                                            Language.RU -> "Одна рука"
                                            Language.UA -> "Одна рука"
                                            Language.KK -> "Бір қолмен"
                                            Language.DE -> "Einhand"
                                            Language.ZH -> "单手操作"
                                            else -> "One-hand"
                                        }
                                    ),
                                    ControlPresetItem(
                                        "claw_pro",
                                        Icons.Default.FlashOn,
                                        when (currentLang) {
                                            Language.RU -> "PRO Клешня"
                                            Language.UA -> "PRO Клешня"
                                            Language.KK -> "PRO Қармау"
                                            Language.DE -> "PRO Claw"
                                            Language.ZH -> "PRO爪握"
                                            else -> "PRO Claw"
                                        },
                                        when (currentLang) {
                                            Language.RU -> "6 клавиш"
                                            Language.UA -> "6 клавіш"
                                            Language.KK -> "6 батырма"
                                            Language.DE -> "6 Tasten"
                                            Language.ZH -> "6键电竞"
                                            else -> "6 buttons"
                                        }
                                    )
                                )


                                presetList.chunked(2).forEach { rowPresets ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowPresets.forEach { (key, icon, title, desc) ->
                                            val isSelected = controlStyle == key
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceContainer,
                                                border = BorderStroke(
                                                    if (isSelected) 1.8.dp else 1.dp,
                                                    if (isSelected) themeColorVal else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        viewModel.setControlStyle(key)
                                                        viewModel.triggerAudioFeedback("click")
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(26.dp)
                                                                .background(
                                                                    if (isSelected) themeColorVal.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                                                    CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                icon,
                                                                contentDescription = null,
                                                                tint = if (isSelected) themeColorVal else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                        }
                                                        if (isSelected) {
                                                            Icon(
                                                                Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint = themeColorVal,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = title,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = desc,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 13.sp),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Handedness & Vertical Position Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ОРИЕНТАЦИЯ И ПОЗИЦИЯ"
                                        Language.UA -> "ОРІЄНТАЦІЯ ТА ПОЗИЦІЯ"
                                        Language.KK -> "БАҒЫТ ПЕН ОРНЫ"
                                        Language.DE -> "AUSRICHTUNG & POSITION"
                                        Language.ZH -> "手位与垂直位置"
                                        else -> "ORIENTATION & POSITION"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                // Left handed switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Режим для левши"
                                                Language.UA -> "Режим для шульги"
                                                Language.KK -> "Солақай режимі"
                                                Language.DE -> "Linkshänder-Modus"
                                                Language.ZH -> "左手镜像模式"
                                                else -> "Left-handed mode"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Зеркальное расположение кнопок"
                                                Language.UA -> "Дзеркальне розташування кнопок"
                                                Language.KK -> "Батырмаларды айналы орналастыру"
                                                Language.DE -> "Tastenanordnung spiegeln"
                                                Language.ZH -> "对调移动与动作操作区"
                                                else -> "Swaps motion and action blocks"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Switch(
                                        checked = leftHandedControls,
                                        onCheckedChange = {
                                            viewModel.setLeftHandedControls(it)
                                            viewModel.triggerAudioFeedback("click")
                                        }
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Vertical Position
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Высота панели управления"
                                            Language.UA -> "Висота панелі керування"
                                            Language.KK -> "Басқару панелінің биіктігі"
                                            Language.DE -> "Vertikale Position"
                                            Language.ZH -> "操作面板垂直位置"
                                            else -> "Vertical Position"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val positions = listOf(
                                            "bottom" to when (currentLang) {
                                                Language.RU -> "Снизу"
                                                Language.UA -> "Знизу"
                                                Language.KK -> "Төменде"
                                                Language.DE -> "Unten"
                                                Language.ZH -> "底部"
                                                else -> "Bottom"
                                            },
                                            "middle" to when (currentLang) {
                                                Language.RU -> "По центру"
                                                Language.UA -> "По центру"
                                                Language.KK -> "Ортада"
                                                Language.DE -> "Mitte"
                                                Language.ZH -> "居中"
                                                else -> "Middle"
                                            },
                                            "top" to when (currentLang) {
                                                Language.RU -> "Сверху"
                                                Language.UA -> "Зверху"
                                                Language.KK -> "Жоғарыда"
                                                Language.DE -> "Oben"
                                                Language.ZH -> "顶部"
                                                else -> "Top"
                                            }
                                        )
                                        positions.forEach { (posKey, label) ->
                                            val isSel = controlVerticalPosition == posKey
                                            val animBg by animateColorAsState(
                                                targetValue = if (isSel) themeColorVal.copy(alpha = 0.22f) else Color.Transparent,
                                                label = "posBg"
                                            )
                                            val animBorder by animateColorAsState(
                                                targetValue = if (isSel) themeColorVal else Color.Transparent,
                                                label = "posBorder"
                                            )
                                            val animTextColor by animateColorAsState(
                                                targetValue = if (isSel) themeColorVal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                                label = "posTextColor"
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(animBg)
                                                    .border(if (isSel) 1.5.dp else 0.dp, animBorder, RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        viewModel.setControlVerticalPosition(posKey)
                                                        viewModel.triggerAudioFeedback("click")
                                                    }
                                                    .padding(vertical = 9.dp, horizontal = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontSize = 12.5.sp,
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = animTextColor,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Button Visual Style Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ВИЗУАЛЬНЫЙ СТИЛЬ КНОПОК"
                                        Language.UA -> "ВІЗУАЛЬНИЙ СТИЛЬ КНОПОК"
                                        Language.KK -> "БАТЫРМАЛАР СТИЛІ"
                                        Language.DE -> "TASTEN-DESIGN"
                                        Language.ZH -> "按键外观风格"
                                        else -> "BUTTON VISUAL THEME"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                val styles = listOf(
                                    "neon" to "Neon Glow",
                                    "classic" to "Classic Solid",
                                    "glass" to "Glassmorphism"
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    styles.forEach { (key, name) ->
                                        val isOwned = key == "classic" || key == "neon" || purchasedControlButtonStyles.contains(key)
                                        val isSel = controlButtonStyle == key
                                        val chipBg by animateColorAsState(
                                            targetValue = if (isSel) themeColorVal.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                                            label = "styleChipBg"
                                        )
                                        val chipBorder by animateColorAsState(
                                            targetValue = if (isSel) themeColorVal else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                            label = "styleChipBorder"
                                        )
                                        val chipTextColor by animateColorAsState(
                                            targetValue = if (isSel) themeColorVal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                            label = "styleChipText"
                                        )

                                        Surface(
                                            onClick = {
                                                if (isOwned) {
                                                    viewModel.setControlButtonStyle(key)
                                                    viewModel.triggerAudioFeedback("click")
                                                } else {
                                                    viewModel.triggerAudioFeedback("error")
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        when (currentLang) {
                                                            Language.RU -> "Купите этот стиль в магазине!"
                                                            Language.UA -> "Придбайте цей стиль у магазині!"
                                                            Language.KK -> "Бұл стильді дүкеннен алыңыз!"
                                                            Language.DE -> "Kaufe diesen Stil im Shop!"
                                                            Language.ZH -> "请在商店中解锁该风格！"
                                                            else -> "Unlock this style in store!"
                                                        },
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = chipBg,
                                            border = BorderStroke(if (isSel) 1.5.dp else 1.dp, chipBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (!isOwned) {
                                                    Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                } else if (isSel) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = themeColorVal,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Text(
                                                    text = name,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 13.sp
                                                    ),
                                                    color = chipTextColor,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    }
                }

                // TAB 1: GEOMETRY, SIZES, PADDING, DAS & ARR TIMINGS
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Button Scale & Opacity Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "РАЗМЕР И ПРОЗРАЧНОСТЬ"
                                        Language.UA -> "РОЗМІР ТА ПРОЗОРІСТЬ"
                                        Language.KK -> "ӨЛШЕМ МЕН МӨЛДІРЛІК"
                                        Language.DE -> "GRÖSSE & TRANSPARENZ"
                                        Language.ZH -> "按键尺寸与透明度"
                                        else -> "SIZE & OPACITY"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                // Scale Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Размер кнопок"
                                                Language.UA -> "Розмір кнопок"
                                                Language.KK -> "Батырмалар өлшемі"
                                                Language.DE -> "Tastengröße"
                                                Language.ZH -> "按键大小"
                                                else -> "Button Size"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${(controlButtonScale * 100).toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlButtonScale,
                                        onValueChange = { viewModel.setControlButtonScale(it) },
                                        valueRange = 0.5f..1.5f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(0.75f to "75%", 1.0f to "100%", 1.25f to "125%").forEach { (sc, label) ->
                                            val isSel = kotlin.math.abs(controlButtonScale - sc) < 0.02f
                                            Surface(
                                                onClick = {
                                                    viewModel.setControlButtonScale(sc)
                                                    viewModel.triggerAudioFeedback("click")
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSel) themeColorVal.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                                                border = BorderStroke(1.dp, if (isSel) themeColorVal else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                                modifier = Modifier.weight(1f)
                                             ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        color = if (isSel) themeColorVal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Alpha Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Прозрачность кнопок"
                                                Language.UA -> "Прозорість кнопок"
                                                Language.KK -> "Мөлдірлігі"
                                                Language.DE -> "Deckkraft"
                                                Language.ZH -> "按键透明度"
                                                else -> "Button Opacity"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${(controlButtonAlpha * 100).toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlButtonAlpha,
                                        onValueChange = { viewModel.setControlButtonAlpha(it) },
                                        valueRange = 0.1f..1.0f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Bottom Padding
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Отступ снизу (для навигации)"
                                                    Language.UA -> "Відступ знизу"
                                                    Language.KK -> "Төменнен шегініс"
                                                    Language.DE -> "Unterer Randabstand"
                                                    Language.ZH -> "底部防误触边距"
                                                    else -> "Bottom Screen Margin"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Защита от случайных срабатываний жестов"
                                                    Language.UA -> "Захист від випадкових жестів"
                                                    Language.KK -> "Ым-ишарадан қорғау"
                                                    Language.DE -> "Schutz vor Gestenleiste"
                                                    Language.ZH -> "防止被手势小白条误触"
                                                    else -> "Prevents accidental gesture trigger"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${controlBottomPadding} dp",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlBottomPadding.toFloat(),
                                        onValueChange = { viewModel.setControlBottomPadding(it.toInt()) },
                                        valueRange = 0f..60f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                }
                            }
                        }

                        // Timings Tuning Card (DAS & ARR)
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ЧУВСТВИТЕЛЬНОСТЬ И ТАЙМИНГИ (DAS / ARR)"
                                        Language.UA -> "ЧУТЛИВІСТЬ ТА ВІДГУК (DAS / ARR)"
                                        Language.KK -> "СЕЗІМТАЛДЫҚ ПЕН ЖЫЛДАМДЫҚ"
                                        Language.DE -> "ANSPRECHVERHALTEN (DAS / ARR)"
                                        Language.ZH -> "按键灵敏度 (DAS / ARR)"
                                        else -> "TIMINGS & RESPONSE (DAS / ARR)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                // DAS Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "DAS (Задержка зажатия)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Время до авто-повтора",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "$controlDas мс",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }

                                    Slider(
                                        value = controlDas.toFloat(),
                                        onValueChange = { viewModel.setControlDas(it.toInt()) },
                                        valueRange = 80f..300f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                    Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.spacedBy(6.dp)
                                     ) {
                                         listOf(120 to "Pro (120мс)", 160 to "Стандарт", 220 to "Мягкий").forEach { (d, label) ->
                                             val isSel = controlDas == d
                                             Surface(
                                                 onClick = {
                                                     viewModel.setControlDas(d)
                                                     viewModel.triggerAudioFeedback("click")
                                                 },
                                                 shape = RoundedCornerShape(10.dp),
                                                 color = if (isSel) themeColorVal.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                                                 border = BorderStroke(1.dp, if (isSel) themeColorVal else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                                 modifier = Modifier.weight(1f)
                                             ) {
                                                 Box(
                                                     modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Text(
                                                         text = label,
                                                         style = MaterialTheme.typography.labelMedium.copy(
                                                             fontSize = 11.5.sp,
                                                             fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                                         ),
                                                         color = if (isSel) themeColorVal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                         maxLines = 1,
                                                         softWrap = false,
                                                         overflow = TextOverflow.Ellipsis,
                                                         textAlign = TextAlign.Center
                                                     )
                                                 }
                                             }
                                         }
                                     }
                                 }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // ARR Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "ARR (Скорость сдвига)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Интервал между шагами",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "$controlArr мс",
                                            fontWeight = FontWeight.Bold,
                                            color = themeColorVal
                                        )
                                    }
                                    Slider(
                                        value = controlArr.toFloat(),
                                        onValueChange = { viewModel.setControlArr(it.toInt()) },
                                        valueRange = 16f..80f,
                                        colors = SliderDefaults.colors(thumbColor = themeColorVal, activeTrackColor = themeColorVal)
                                    )
                                    Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.spacedBy(6.dp)
                                     ) {
                                         listOf(20 to "Турбо (20мс)", 35 to "Стандарт", 50 to "Плавный").forEach { (a, label) ->
                                             val isSel = controlArr == a
                                             Surface(
                                                 onClick = {
                                                     viewModel.setControlArr(a)
                                                     viewModel.triggerAudioFeedback("click")
                                                 },
                                                 shape = RoundedCornerShape(10.dp),
                                                 color = if (isSel) themeColorVal.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                                                 border = BorderStroke(1.dp, if (isSel) themeColorVal else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                                 modifier = Modifier.weight(1f)
                                             ) {
                                                 Box(
                                                     modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Text(
                                                         text = label,
                                                         style = MaterialTheme.typography.labelMedium.copy(
                                                             fontSize = 11.5.sp,
                                                             fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                                         ),
                                                         color = if (isSel) themeColorVal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                         maxLines = 1,
                                                         softWrap = false,
                                                         overflow = TextOverflow.Ellipsis,
                                                         textAlign = TextAlign.Center
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

                // TAB 2: EXPORT / IMPORT CONFIG CODE
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Config Code Box
                        val currentCode = remember(controlStyle, controlButtonScale, controlButtonAlpha, controlButtonStyle, controlVerticalPosition, leftHandedControls, controlDas, controlArr, controlBottomPadding) {
                            viewModel.exportControlConfigCode()
                        }

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ТЕКУЩИЙ КОД УПРАВЛЕНИЯ"
                                        Language.UA -> "ПОТОЧНИЙ КОД КЕРУВАННЯ"
                                        Language.KK -> "АҒЫМДАҒЫ БАСҚАРУ КОДЫ"
                                        Language.DE -> "AKTUELLER STEUERUNGSCODE"
                                        Language.ZH -> "当前操作配置代码"
                                        else -> "CURRENT CONTROL CODE"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Этот код содержит всю вашу раскладку, масштаб, прозрачность, отступы и задержки DAS/ARR. Скопируйте и поделитесь с друзьями:"
                                        Language.UA -> "Цей код містить усі ваші налаштування. Скопіюйте та поділіться з друзями:"
                                        Language.KK -> "Бұл код барлық параметрлерді сақтайды. Көшіріп, достарыңызбен бөлісіңіз:"
                                        Language.DE -> "Dieser Code enthält alle Parameter. Kopieren und mit Freunden teilen:"
                                        Language.ZH -> "此代码包含您的全部按键布局、尺寸、底边距及DAS/ARR延迟，可一键分享："
                                        else -> "This code encapsulates all your layout, scale, opacity, margins and DAS/ARR timings. Copy to share with friends:"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentCode,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(12.dp),
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentCode))
                                            android.widget.Toast.makeText(
                                                context,
                                                when (currentLang) {
                                                    Language.RU -> "Код управления скопирован в буфер обмена!"
                                                    Language.UA -> "Код скопійовано в буфер!"
                                                    Language.KK -> "Басқару коды көшірілді!"
                                                    Language.DE -> "Code in Zwischenablage kopiert!"
                                                    Language.ZH -> "控制代码已复制到剪贴板！"
                                                    else -> "Control code copied to clipboard!"
                                                },
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Копировать"
                                                Language.UA -> "Копіювати"
                                                Language.KK -> "Көшіру"
                                                Language.DE -> "Kopieren"
                                                Language.ZH -> "复制"
                                                else -> "Copy"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            try {
                                                val sendIntent = android.content.Intent().apply {
                                                    action = android.content.Intent.ACTION_SEND
                                                    putExtra(
                                                        android.content.Intent.EXTRA_TEXT,
                                                        "Tetris Controls Config: $currentCode"
                                                    )
                                                    type = "text/plain"
                                                }
                                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                                context.startActivity(shareIntent)
                                            } catch (e: Exception) {
                                                // Fallback copy if no share handler
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentCode))
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "Поделиться"
                                                Language.UA -> "Поділитися"
                                                Language.KK -> "Бөлісу"
                                                Language.DE -> "Teilen"
                                                Language.ZH -> "分享"
                                                else -> "Share"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Import Code Section
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ИМПОРТ КОДА УПРАВЛЕНИЯ"
                                        Language.UA -> "ІМПОРТ КОДУ КЕРУВАННЯ"
                                        Language.KK -> "КОДТЫ ИМПОРТТАУ"
                                        Language.DE -> "CODE IMPORTIEREN"
                                        Language.ZH -> "导入已有配置代码"
                                        else -> "IMPORT CONTROL CODE"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColorVal
                                )

                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Вставьте код управления, полученный от другого игрока, чтобы мгновенно применить его настройки:"
                                        Language.UA -> "Вставте код керування від іншого гравця:"
                                        Language.KK -> "Басқа ойыншының кодын қойыңыз:"
                                        Language.DE -> "Füge einen Code ein, um die Steuerung zu übernehmen:"
                                        Language.ZH -> "粘贴其他玩家分享的控制代码以立即套用："
                                        else -> "Paste a code from another player to instantly apply their layout:"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = importCodeText,
                                    onValueChange = {
                                        importCodeText = it
                                        importErrorText = null
                                    },
                                    placeholder = { Text("TTR-CTRL:...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = importErrorText != null,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                val clip = clipboardManager.getText()?.text
                                                if (!clip.isNullOrBlank()) {
                                                    importCodeText = clip.trim()
                                                    importErrorText = null
                                                    viewModel.triggerAudioFeedback("click")
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste from clipboard")
                                        }
                                    }
                                )

                                if (importErrorText != null) {
                                    Text(
                                        text = importErrorText ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Button(
                                    onClick = {
                                        val ok = viewModel.importControlConfigCode(importCodeText)
                                        if (ok) {
                                            viewModel.triggerAudioFeedback("click")
                                            importCodeText = ""
                                            importErrorText = null
                                            android.widget.Toast.makeText(
                                                context,
                                                when (currentLang) {
                                                    Language.RU -> "Конфигурация успешно загружена!"
                                                    Language.UA -> "Конфігурацію успішно завантажено!"
                                                    Language.KK -> "Конфигурация сәтті жүктелді!"
                                                    Language.DE -> "Konfiguration erfolgreich geladen!"
                                                    Language.ZH -> "配置已成功套用！"
                                                    else -> "Configuration loaded successfully!"
                                                },
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            viewModel.triggerAudioFeedback("error")
                                            importErrorText = when (currentLang) {
                                                Language.RU -> "Неверный код! Проверьте формат."
                                                Language.UA -> "Невірний код! Перевірте формат."
                                                Language.KK -> "Қате код! Форматты тексеріңіз."
                                                Language.DE -> "Ungültiger Code! Prüfen."
                                                Language.ZH -> "代码无效！请检查格式。"
                                                else -> "Invalid code! Check format."
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLang) {
                                            Language.RU -> "Применить этот код"
                                            Language.UA -> "Застосувати цей код"
                                            Language.KK -> "Осы кодты қолдану"
                                            Language.DE -> "Diesen Code anwenden"
                                            Language.ZH -> "立即应用此代码"
                                            else -> "Apply this code"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Reset Button
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Сбросить управление к рекомендованному"
                                    Language.UA -> "Скинути до рекомендованого"
                                    Language.KK -> "Басқаруды стандартқа қайтару"
                                    Language.DE -> "Auf Standardwerte zurücksetzen"
                                    Language.ZH -> "恢复推荐默认按键设置"
                                    else -> "Reset controls to recommended defaults"
                                }
                            )
                        }
                    }
                }


            }
        }
    }
}

