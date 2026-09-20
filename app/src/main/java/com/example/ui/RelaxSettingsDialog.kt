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
fun RelaxSettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val relaxImmortal by viewModel.relaxImmortal.collectAsStateWithLifecycle()
    val relaxBlockSet by viewModel.relaxBlockSet.collectAsStateWithLifecycle()
    val relaxSpeed by viewModel.relaxSpeed.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (currentLang) {
                        Language.RU -> "РЕЛАКС ХАБ"
                        Language.UA -> "РЕЛАКС ХАБ"
                        Language.KK -> "РЕЛАКС ХАБ"
                        Language.DE -> "RELAX-HUB"
                        Language.ZH -> "轻松解压中心"
                        else -> "RELAX HUB"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Бессмертие
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Бессмертие (без Game Over)"
                                    Language.UA -> "Безсмертя (без Game Over)"
                                    Language.KK -> "Өлместік (Game Over жоқ)"
                                    Language.DE -> "Unsterblichkeit (Kein Game Over)"
                                    Language.ZH -> "无限永生 (无游戏结束)"
                                    else -> "Immortal Mode (No Game Over)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Очистка поля при переполнении"
                                    Language.UA -> "Очищення поля при переповненні"
                                    Language.KK -> "Толған кезде алаңды тазалау"
                                    Language.DE -> "Automatisches Leeren bei Überlauf"
                                    Language.ZH -> "顶部溢出时自动清空顶格"
                                    else -> "Auto cleans board on overflow"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = relaxImmortal,
                            onCheckedChange = { viewModel.setRelaxImmortal(it) }
                        )
                    }
                }

                // 2. Набор фигур
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Набор фигур:"
                            Language.UA -> "Набір фігур:"
                            Language.KK -> "Пішіндер жиынтығы:"
                            Language.DE -> "Figuren-Set:"
                            Language.ZH -> "方块组合:"
                            else -> "Block Set:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val blockSets = listOf(
                        "only_i" to when (currentLang) {
                            Language.RU -> "Только палки (I-Only)"
                            Language.UA -> "Тільки палиці (I-Only)"
                            Language.KK -> "Тек таяқшалар"
                            Language.DE -> "Nur I-Balken"
                            Language.ZH -> "纯长条天堂"
                            else -> "Only I-Bars"
                        },
                        "ideal" to when (currentLang) {
                            Language.RU -> "Идеальный баланс (I, O, T)"
                            Language.UA -> "Ідеальний баланс"
                            Language.KK -> "Керемет баланс"
                            Language.DE -> "Ideale Balance"
                            Language.ZH -> "极简易搭组合"
                            else -> "Ideal Balance"
                        },
                        "standard" to when (currentLang) {
                            Language.RU -> "Классика (7 фигур)"
                            Language.UA -> "Класика (7 фігур)"
                            Language.KK -> "Классика (7 пішін)"
                            Language.DE -> "Standard (7 Figuren)"
                            Language.ZH -> "标准7种图形"
                            else -> "Standard 7"
                        },
                        "all" to when (currentLang) {
                            Language.RU -> "Все 10 фигур (с пентамино)"
                            Language.UA -> "Всі 10 фігур"
                            Language.KK -> "Барлық 10 пішін"
                            Language.DE -> "Alle 10 Figuren"
                            Language.ZH -> "全部10种异形"
                            else -> "All 10 Shapes"
                        }
                    )
                    blockSets.forEach { (key, label) ->
                        val isSelected = relaxBlockSet == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRelaxBlockSet(key)
                                    viewModel.triggerAudioFeedback("click")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setRelaxBlockSet(key)
                                        viewModel.triggerAudioFeedback("click")
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 3. Скорость гравитации
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Гравитация падения:"
                            Language.UA -> "Гравітація падіння:"
                            Language.KK -> "Құлау жылдамдығы:"
                            Language.DE -> "Fall-Geschwindigkeit:"
                            Language.ZH -> "下落重力:"
                            else -> "Gravity Speed:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val speedOptions = listOf(
                        "static" to when (currentLang) {
                            Language.RU -> "Без гравитации (ручной сброс)"
                            Language.UA -> "Без гравітації"
                            Language.KK -> "Гравитациясыз"
                            Language.DE -> "Keine Gravitation"
                            Language.ZH -> "悬浮静止模式"
                            else -> "Zero Gravity (Manual)"
                        },
                        "slow" to when (currentLang) {
                            Language.RU -> "Медитативная (1.5 сек)"
                            Language.UA -> "Медитативна"
                            Language.KK -> "Медитативті"
                            Language.DE -> "Meditativ (Langsam)"
                            Language.ZH -> "冥想超缓流速"
                            else -> "Meditative (Slow)"
                        },
                        "flow" to when (currentLang) {
                            Language.RU -> "Плавный поток (0.9 сек)"
                            Language.UA -> "Плавний потік"
                            Language.KK -> "Бірқалыпты ағын"
                            Language.DE -> "Sanfter Fluss"
                            Language.ZH -> "平缓禅意流速"
                            else -> "Smooth Flow (Normal)"
                        }
                    )
                    speedOptions.forEach { (key, label) ->
                        val isSelected = relaxSpeed == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRelaxSpeed(key)
                                    viewModel.triggerAudioFeedback("click")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setRelaxSpeed(key)
                                        viewModel.triggerAudioFeedback("click")
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 4. Манипуляции с полем
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentLang) {
                            Language.RU -> "Манипуляции с полем:"
                            Language.UA -> "Дії з полем:"
                            Language.KK -> "Алаң әрекеттері:"
                            Language.DE -> "Feld-Aktionen:"
                            Language.ZH -> "棋盘快捷操作:"
                            else -> "Field Actions:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearRelaxLowerRows()
                                viewModel.triggerAudioFeedback("clear")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Срезать низ (8)"
                                    Language.UA -> "Зрізати низ (8)"
                                    Language.KK -> "Астын кесу (8)"
                                    Language.DE -> "Unten (8) leeren"
                                    Language.ZH -> "清空底部8行"
                                    else -> "Cut Lower 8"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.clearRelaxBoard()
                                viewModel.triggerAudioFeedback("clear")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Очистить всё"
                                    Language.UA -> "Очистити все"
                                    Language.KK -> "Барлығын тазалау"
                                    Language.DE -> "Alles leeren"
                                    Language.ZH -> "清空整盘"
                                    else -> "Clear Board"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) {
                Text(Translations.get("done", currentLang), fontWeight = FontWeight.Bold)
            }
        }
    )
}

