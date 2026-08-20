package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Drop Rarity Tiers (Tactical CS2 / Cyberpunk Grading) ──
enum class DropRarity(val label: String, val labelRu: String, val color: Color, val tierIndex: Int) {
    COMMON("Basic", "Базовый", Color(0xFF8E9297), 0),
    UNCOMMON("Special", "Особый", Color(0xFF43B581), 1),
    RARE("Rare", "Редкий", Color(0xFF00B0FF), 2),
    EPIC("Epic", "Эпик", Color(0xFFAA00FF), 3),
    LEGENDARY("Mythic", "Мифик", Color(0xFFFF6D00), 4),
    RED("Relic", "Реликт", Color(0xFFFF1744), 5)
}

fun DropRarity.getLocalizedName(lang: Language): String = when (this) {
    DropRarity.COMMON -> when (lang) {
        Language.RU -> "Базовый"
        Language.UA -> "Базовий"
        Language.KK -> "Базалық"
        Language.DE -> "Basis"
        Language.ZH -> "基础"
        else -> "Basic"
    }
    DropRarity.UNCOMMON -> when (lang) {
        Language.RU -> "Особый"
        Language.UA -> "Особливий"
        Language.KK -> "Арнайы"
        Language.DE -> "Spezial"
        Language.ZH -> "特殊"
        else -> "Special"
    }
    DropRarity.RARE -> when (lang) {
        Language.RU -> "Редкий"
        Language.UA -> "Рідкісний"
        Language.KK -> "Сирек"
        Language.DE -> "Selten"
        Language.ZH -> "稀有"
        else -> "Rare"
    }
    DropRarity.EPIC -> when (lang) {
        Language.RU -> "Эпик"
        Language.UA -> "Епічний"
        Language.KK -> "Эпикалық"
        Language.DE -> "Episch"
        Language.ZH -> "史诗"
        else -> "Epic"
    }
    DropRarity.LEGENDARY -> when (lang) {
        Language.RU -> "Мифик"
        Language.UA -> "Міфічний"
        Language.KK -> "Мифтік"
        Language.DE -> "Mythisch"
        Language.ZH -> "神话"
        else -> "Mythic"
    }
    DropRarity.RED -> when (lang) {
        Language.RU -> "Реликт"
        Language.UA -> "Релікт"
        Language.KK -> "Реликт"
        Language.DE -> "Relikt"
        Language.ZH -> "遗物"
        else -> "Relic"
    }
}

// ── Loot Item Definition ──
data class LootItem(
    val id: String,
    val type: String, // "skin", "cube_skin", "avatar_frame", "sound_pack", "credits", "nick_gradient", "bonus_xp"
    val displayName: String,
    val displayNameRu: String,
    val rarity: DropRarity,
    val creditValue: Int = 0
)

fun LootItem.getLocalizedName(lang: Language): String = when (type) {
    "skin" -> Translations.getLocalizedSkinTitle(id, lang)
    "cube_skin" -> when (id) {
        "red_gradient" -> when (lang) {
            Language.RU -> "Кровавый Пульс"
            Language.UA -> "Кривавий Пульс"
            Language.KK -> "Қанды Импульс"
            Language.DE -> "Purpurroter Puls"
            Language.ZH -> "猩红脉冲"
            else -> displayName
        }
        "green_gradient" -> when (lang) {
            Language.RU -> "Токсичный Пульс"
            Language.UA -> "Токсичний Пульс"
            Language.KK -> "Улы Импульс"
            Language.DE -> "Toxischer Puls"
            Language.ZH -> "剧毒脉冲"
            else -> displayName
        }
        "blue_gradient" -> when (lang) {
            Language.RU -> "Кобальтовый Пульс"
            Language.UA -> "Кобальтовий Пульс"
            Language.KK -> "Кобальт Импульсі"
            Language.DE -> "Kobalt Puls"
            Language.ZH -> "钴蓝脉冲"
            else -> displayName
        }
        "purple_gradient" -> when (lang) {
            Language.RU -> "Пульс Бездны"
            Language.UA -> "Пульс Безодні"
            Language.KK -> "Тұңғиық Импульсі"
            Language.DE -> "Leere Puls"
            Language.ZH -> "虚空脉冲"
            else -> displayName
        }
        else -> Translations.getLocalizedCubeSkinTitle(id, lang)
    }
    "avatar_frame" -> Translations.getLocalizedAvatarFrameTitle(id, lang)
    else -> when (id) {
        "chrono_gl" -> when (lang) {
            Language.RU -> "Сингулярность"
            Language.UA -> "Сингулярність"
            Language.KK -> "Сингулярлық"
            Language.DE -> "Singularität"
            Language.ZH -> "奇点渐变"
            else -> displayName
        }
        "nick_gradient" -> when (lang) {
            Language.RU -> "Градиент Ника"
            Language.UA -> "Градієнт Ніка"
            Language.KK -> "Ник Градиенті"
            Language.DE -> "Spitznamen-Farbverlauf"
            Language.ZH -> "昵称炫彩渐变"
            else -> displayName
        }
        "bonus_xp_5000" -> when (lang) {
            Language.RU -> "+5000 XP Опыта"
            Language.UA -> "+5000 XP Досвіду"
            Language.KK -> "+5000 XP Тәжірибе"
            Language.DE -> "+5000 XP Erfahrung"
            Language.ZH -> "+5000 XP 经验值"
            else -> displayName
        }
        else -> when (lang) {
            Language.RU -> displayNameRu
            Language.UA -> displayNameRu
            Language.KK -> displayNameRu
            Language.DE -> displayName
            Language.ZH -> displayName
            else -> displayName
        }
    }
}

fun getLocalizedItemTypeName(type: String, lang: Language): String = when (type) {
    "skin" -> when (lang) {
        Language.RU -> "Скин игрового поля"
        Language.UA -> "Скін ігрового поля"
        Language.KK -> "Ойын алаңының скині"
        Language.DE -> "Spielfeld-Skin"
        Language.ZH -> "棋盘网格皮肤"
        else -> "Board Skin"
    }
    "cube_skin" -> when (lang) {
        Language.RU -> "Стиль блоков"
        Language.UA -> "Стиль блоків"
        Language.KK -> "Блоктар стилі"
        Language.DE -> "Block-Stil"
        Language.ZH -> "方块样式"
        else -> "Block Skin"
    }
    "avatar_frame" -> when (lang) {
        Language.RU -> "Рамка аватара"
        Language.UA -> "Рамка аватара"
        Language.KK -> "Аватар жақтауы"
        Language.DE -> "Avatar-Rahmen"
        Language.ZH -> "头像边框"
        else -> "Avatar Frame"
    }
    "sound_pack" -> when (lang) {
        Language.RU -> "Звуковой пакет"
        Language.UA -> "Звуковий пакет"
        Language.KK -> "Дыбыстық топтама"
        Language.DE -> "Sound-Paket"
        Language.ZH -> "音效包"
        else -> "Sound Pack"
    }
    "nick_gradient" -> when (lang) {
        Language.RU -> "Градиент никнейма"
        Language.UA -> "Градієнт нікнейма"
        Language.KK -> "Никнейм градиенті"
        Language.DE -> "Spitznamen-Farbverlauf"
        Language.ZH -> "昵称渐变"
        else -> "Nickname Gradient"
    }
    "credits" -> when (lang) {
        Language.RU -> "Золотые монеты"
        Language.UA -> "Золоті монети"
        Language.KK -> "Алтын тиындар"
        Language.DE -> "Goldmünzen"
        Language.ZH -> "金币奖励"
        else -> "Credits Reward"
    }
    "bonus_xp" -> when (lang) {
        Language.RU -> "Очки опыта"
        Language.UA -> "Очки досвіду"
        Language.KK -> "Тәжірибе ұпайлары"
        Language.DE -> "Erfahrungspunkte"
        Language.ZH -> "经验值奖励"
        else -> "XP Reward"
    }
    else -> type
}

// ── Crate Definition ──
data class LootCrate(
    val id: String,
    val name: String,
    val nameRu: String,
    val cost: Int,
    val accentColor: Color,
    val secondaryColor: Color,
    val icon: ImageVector,
    val dropChances: Map<DropRarity, Float>
)

fun LootCrate.getLocalizedName(lang: Language): String = when (id) {
    "wooden" -> when (lang) {
        Language.RU -> "Деревянный Кейс"
        Language.UA -> "Дерев'яний Кейс"
        Language.KK -> "Ағаш Кейс"
        Language.DE -> "Holzkiste"
        Language.ZH -> "木质箱"
        else -> name
    }
    "iron" -> when (lang) {
        Language.RU -> "Железный Кейс"
        Language.UA -> "Залізний Кейс"
        Language.KK -> "Темір Кейс"
        Language.DE -> "Eisenkiste"
        Language.ZH -> "铁质箱"
        else -> name
    }
    "golden" -> when (lang) {
        Language.RU -> "Золотой Кейс"
        Language.UA -> "Золотий Кейс"
        Language.KK -> "Алтын Кейс"
        Language.DE -> "Goldkiste"
        Language.ZH -> "黄金箱"
        else -> name
    }
    "platinum" -> when (lang) {
        Language.RU -> "Платиновый Кейс"
        Language.UA -> "Платиновий Кейс"
        Language.KK -> "Платина Кейс"
        Language.DE -> "Platinkiste"
        Language.ZH -> "铂金箱"
        else -> name
    }
    "legendary" -> when (lang) {
        Language.RU -> "Легендарный Кейс"
        Language.UA -> "Легендарний Кейс"
        Language.KK -> "Аңызға айналған Кейс"
        Language.DE -> "Legendäre Kiste"
        Language.ZH -> "传奇箱"
        else -> name
    }
    "diamond" -> when (lang) {
        Language.RU -> "Алмазный Кейс"
        Language.UA -> "Діамантовий Кейс"
        Language.KK -> "Гауһар Кейс"
        Language.DE -> "Diamantkiste"
        Language.ZH -> "钻石箱"
        else -> name
    }
    "red_crate_lite" -> when (lang) {
        Language.RU -> "Красный Лайт"
        Language.UA -> "Червоний Лайт"
        Language.KK -> "Қызыл Лайт"
        Language.DE -> "Rote Kiste Lite"
        Language.ZH -> "精简红箱"
        else -> name
    }
    "red_crate" -> when (lang) {
        Language.RU -> "Красный Кейс"
        Language.UA -> "Червоний Кейс"
        Language.KK -> "Қызыл Кейс"
        Language.DE -> "Rote Kiste"
        Language.ZH -> "红色箱"
        else -> name
    }
    else -> when (lang) {
        Language.RU -> nameRu
        Language.UA -> nameRu
        Language.KK -> nameRu
        Language.DE -> name
        Language.ZH -> name
        else -> name
    }
}

// ── Inventory Item with Tracking ──
data class InventoryItem(
    val uuid: String,
    val item: LootItem,
    val acquiredTimestamp: Long = System.currentTimeMillis(),
    var isLocked: Boolean = false
)

// ── Master Loot Pool ──
val allLootItems = listOf(
    // Grid Skins
    LootItem("retro_amber", "skin", "Gold", "Золото", DropRarity.COMMON),
    LootItem("emerald_matrix", "skin", "Emerald", "Изумруд", DropRarity.COMMON),
    LootItem("vaporwave_pink", "skin", "Synthwave", "Синтвейв", DropRarity.UNCOMMON),
    LootItem("midnight_gold", "skin", "Obsidian", "Обсидиан", DropRarity.RARE),
    LootItem("carbon_neutral", "skin", "Graphite", "Графит", DropRarity.RARE),
    LootItem("plasma_storm", "skin", "Plasma", "Плазма", DropRarity.EPIC),
    LootItem("glacial_frost", "skin", "Crystal", "Кристалл", DropRarity.LEGENDARY),

    // Cube Skins
    LootItem("glass", "cube_skin", "Glass", "Стекло", DropRarity.COMMON),
    LootItem("retro", "cube_skin", "Retro", "Ретро", DropRarity.COMMON),
    LootItem("flat", "cube_skin", "Flat", "Флэт", DropRarity.UNCOMMON),
    LootItem("material", "cube_skin", "Material", "Материал", DropRarity.UNCOMMON),
    LootItem("glowing_jewel", "cube_skin", "Sapphire", "Сапфир", DropRarity.RARE),
    LootItem("steampunk", "cube_skin", "Brass", "Латунь", DropRarity.EPIC),
    LootItem("red_gradient", "cube_skin", "Crimson Pulse", "Кровавый Пульс", DropRarity.RED),
    LootItem("green_gradient", "cube_skin", "Toxic Pulse", "Токсичный Пульс", DropRarity.RED),
    LootItem("blue_gradient", "cube_skin", "Cobalt Pulse", "Кобальтовый Пульс", DropRarity.RED),
    LootItem("purple_gradient", "cube_skin", "Void Pulse", "Пульс Бездны", DropRarity.RED),

    // Avatar Frame
    LootItem("chrono_gl", "avatar_frame", "OVERDRIVE FRAME", "Градиентная Рамка", DropRarity.RED),

    // Credit Drops
    LootItem("credits_50", "credits", "+50 🪙", "+50 🪙", DropRarity.COMMON, 50),
    LootItem("credits_100", "credits", "+100 🪙", "+100 🪙", DropRarity.COMMON, 100),
    LootItem("credits_250", "credits", "+250 🪙", "+250 🪙", DropRarity.UNCOMMON, 250),
    LootItem("credits_500", "credits", "+500 🪙", "+500 🪙", DropRarity.RARE, 500),
    LootItem("credits_1000", "credits", "+1000 🪙", "+1000 🪙", DropRarity.EPIC, 1000),
    LootItem("credits_2500", "credits", "+2500 🪙", "+2500 🪙", DropRarity.LEGENDARY, 2500),

    // Special
    LootItem("nick_gradient", "nick_gradient", "OVERDRIVE NICK", "Градиент Ника", DropRarity.RED),
    LootItem("bonus_xp_5000", "bonus_xp", "+5000 XP BEACON", "+5000 XP Опыта", DropRarity.RED, 5000)
)

// ── 8 Exact Crates (Strict original IDs, names & prices) ──
val lootCrates = listOf(
    LootCrate(
        id = "wooden", name = "Wooden Crate", nameRu = "Деревянный Кейс",
        cost = ShopPrices.getCrateCost("wooden"), accentColor = Color(0xFF8D6E63), secondaryColor = Color(0xFFA1887F),
        icon = Icons.Default.Inventory2,
        dropChances = mapOf(
            DropRarity.COMMON to 70f,
            DropRarity.UNCOMMON to 22f,
            DropRarity.RARE to 8f
        )
    ),
    LootCrate(
        id = "iron", name = "Iron Crate", nameRu = "Железный Кейс",
        cost = ShopPrices.getCrateCost("iron"), accentColor = Color(0xFF78909C), secondaryColor = Color(0xFF90A4AE),
        icon = Icons.Default.Inventory2,
        dropChances = mapOf(
            DropRarity.COMMON to 40f,
            DropRarity.UNCOMMON to 32f,
            DropRarity.RARE to 20f,
            DropRarity.EPIC to 8f
        )
    ),
    LootCrate(
        id = "golden", name = "Golden Crate", nameRu = "Золотой Кейс",
        cost = ShopPrices.getCrateCost("golden"), accentColor = Color(0xFFFFB300), secondaryColor = Color(0xFFFFD54F),
        icon = Icons.Default.CardGiftcard,
        dropChances = mapOf(
            DropRarity.COMMON to 20f,
            DropRarity.UNCOMMON to 30f,
            DropRarity.RARE to 28f,
            DropRarity.EPIC to 17f,
            DropRarity.LEGENDARY to 5f
        )
    ),
    LootCrate(
        id = "platinum", name = "Platinum Crate", nameRu = "Платиновый Кейс",
        cost = ShopPrices.getCrateCost("platinum"), accentColor = Color(0xFF7E57C2), secondaryColor = Color(0xFFB39DDB),
        icon = Icons.Default.AutoAwesome,
        dropChances = mapOf(
            DropRarity.COMMON to 8f,
            DropRarity.UNCOMMON to 17f,
            DropRarity.RARE to 30f,
            DropRarity.EPIC to 32f,
            DropRarity.LEGENDARY to 13f
        )
    ),
    LootCrate(
        id = "legendary", name = "Legendary Crate", nameRu = "Легендарный Кейс",
        cost = ShopPrices.getCrateCost("legendary"), accentColor = Color(0xFFFF6F00), secondaryColor = Color(0xFFFFAB40),
        icon = Icons.Default.EmojiEvents,
        dropChances = mapOf(
            DropRarity.COMMON to 2f,
            DropRarity.UNCOMMON to 8f,
            DropRarity.RARE to 20f,
            DropRarity.EPIC to 40f,
            DropRarity.LEGENDARY to 30f
        )
    ),
    LootCrate(
        id = "diamond", name = "Diamond Crate", nameRu = "Алмазный Кейс",
        cost = ShopPrices.getCrateCost("diamond"), accentColor = Color(0xFF00E5FF), secondaryColor = Color(0xFF80D8FF),
        icon = Icons.Default.Diamond,
        dropChances = mapOf(
            DropRarity.COMMON to 5f,
            DropRarity.UNCOMMON to 15f,
            DropRarity.RARE to 35f,
            DropRarity.EPIC to 30f,
            DropRarity.LEGENDARY to 15f
        )
    ),
    LootCrate(
        id = "red_crate_lite", name = "Lite Red Crate", nameRu = "Красный Лайт",
        cost = ShopPrices.getCrateCost("red_crate_lite"), accentColor = Color(0xFFE57373), secondaryColor = Color(0xFFFFCDD2),
        icon = Icons.Default.Whatshot,
        dropChances = mapOf(
            DropRarity.COMMON to 25f,
            DropRarity.UNCOMMON to 30f,
            DropRarity.RARE to 25f,
            DropRarity.EPIC to 15f,
            DropRarity.LEGENDARY to 2f,
            DropRarity.RED to 3f
        )
    ),
    LootCrate(
        id = "red_crate", name = "Red Crate", nameRu = "Красный Кейс",
        cost = ShopPrices.getCrateCost("red_crate"), accentColor = Color(0xFFD32F2F), secondaryColor = Color(0xFFFF5252),
        icon = Icons.Default.Whatshot,
        dropChances = mapOf(
            DropRarity.COMMON to 0f,
            DropRarity.UNCOMMON to 0f,
            DropRarity.RARE to 10f,
            DropRarity.EPIC to 30f,
            DropRarity.LEGENDARY to 40f,
            DropRarity.RED to 20f
        )
    )
)

fun rollDrop(crate: LootCrate): LootItem {
    val roll = (0..10000).random() / 100f
    var cumulative = 0f
    var selectedRarity = DropRarity.COMMON

    for ((rarity, chance) in crate.dropChances) {
        cumulative += chance
        if (roll <= cumulative) {
            selectedRarity = rarity
            break
        }
    }

    val pool = allLootItems.filter { it.rarity == selectedRarity }
    return if (pool.isNotEmpty()) pool.random() else allLootItems.filter { it.rarity == DropRarity.COMMON }.random()
}

fun iconForItemType(type: String): ImageVector = when (type) {
    "credits" -> Icons.Default.AttachMoney
    "skin" -> Icons.Default.GridOn
    "cube_skin" -> Icons.Default.Widgets
    "avatar_frame" -> Icons.Default.AccountBox
    "sound_pack" -> Icons.AutoMirrored.Filled.VolumeUp
    "nick_gradient" -> Icons.Default.ElectricBolt
    "bonus_xp" -> Icons.Default.MilitaryTech
    else -> Icons.Default.Token
}

fun getItemSellPrice(rarity: DropRarity): Int = when (rarity) {
    DropRarity.COMMON -> 35
    DropRarity.UNCOMMON -> 100
    DropRarity.RARE -> 250
    DropRarity.EPIC -> 600
    DropRarity.LEGENDARY -> 1500
    DropRarity.RED -> 5000
}

fun deserializeInventoryItem(serialized: String): InventoryItem? {
    val parts = serialized.split(";")
    if (parts.size < 6) return null
    val uuid = parts[0]
    val id = parts[1]
    val type = parts[2]
    var displayName = parts[3]
    var displayNameRu = parts[4]
    val rarityName = parts[5]
    val creditValue = parts.getOrNull(6)?.toIntOrNull() ?: 0
    val ts = parts.getOrNull(7)?.toLongOrNull() ?: System.currentTimeMillis()
    val isLocked = parts.getOrNull(8)?.toBooleanStrictOrNull() ?: false

    val masterItem = allLootItems.find { it.id == id }
    if (masterItem != null) {
        displayName = masterItem.displayName
        displayNameRu = masterItem.displayNameRu
    }

    val rarity = masterItem?.rarity ?: try {
        DropRarity.valueOf(rarityName)
    } catch (e: Exception) {
        DropRarity.COMMON
    }

    return InventoryItem(
        uuid = uuid,
        item = LootItem(
            id = id,
            type = type,
            displayName = displayName,
            displayNameRu = displayNameRu,
            rarity = rarity,
            creditValue = if (masterItem != null && masterItem.creditValue > 0) masterItem.creditValue else creditValue
        ),
        acquiredTimestamp = ts,
        isLocked = isLocked
    )
}

fun serializeInventoryItem(inv: InventoryItem): String {
    return "${inv.uuid};${inv.item.id};${inv.item.type};${inv.item.displayName};${inv.item.displayNameRu};${inv.item.rarity.name};${inv.item.creditValue};${inv.acquiredTimestamp};${inv.isLocked}"
}

// ══════════════════════════════════════════════════════════════════
// MAIN CASES SCREEN (Tactical CS2 & Cyberpunk Overhaul)
// ══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CasesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    val themeColor = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Crates, 1 = Inventory
    var openingCrate by remember { mutableStateOf<LootCrate?>(null) }
    var droppedItem by remember { mutableStateOf<LootItem?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var previewCrate by remember { mutableStateOf<LootCrate?>(null) }
    var lastOpenedCrate by remember { mutableStateOf<LootCrate?>(null) }
    var fastOpenMode by remember { mutableStateOf(false) }
    var inspectingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showMassRecycleDialog by remember { mutableStateOf(false) }

    // State of equipped cosmetics to show active badge in inventory
    val currentBoardSkin by viewModel.boardColorSkin.collectAsStateWithLifecycle()
    val currentBlockSkin by viewModel.blockStyle.collectAsStateWithLifecycle()
    val currentAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
    val currentHasGradient by viewModel.hasNicknameGradient.collectAsStateWithLifecycle()
    val currentSoundPack by viewModel.equippedSoundPack.collectAsStateWithLifecycle()
    val crateKeys by viewModel.crateKeys.collectAsStateWithLifecycle()

    val sharedPrefs = remember {
        viewModel.getApplication<android.app.Application>()
            .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
    }

    var inventorySet by remember {
        mutableStateOf(sharedPrefs.getStringSet("case_inventory", emptySet()) ?: emptySet())
    }

    val inventoryItems = remember(inventorySet) {
        inventorySet.mapNotNull { deserializeInventoryItem(it) }
    }

    // Inventory Filtering & Sorting
    var selectedInvCategory by remember { mutableStateOf("ALL") }
    var selectedSortMode by remember { mutableStateOf("RARITY_DESC") } // RARITY_DESC, PRICE_DESC, NEWEST

    val filteredInventory = remember(inventoryItems, selectedInvCategory, selectedSortMode) {
        val list = when (selectedInvCategory) {
            "SKINS" -> inventoryItems.filter { it.item.type == "skin" }
            "BLOCKS" -> inventoryItems.filter { it.item.type == "cube_skin" }
            "FRAMES" -> inventoryItems.filter { it.item.type == "avatar_frame" }
            "SPECIAL" -> inventoryItems.filter { it.item.type in listOf("nick_gradient", "sound_pack", "bonus_xp") }
            else -> inventoryItems
        }
        when (selectedSortMode) {
            "PRICE_DESC" -> list.sortedByDescending { getItemSellPrice(it.item.rarity) }
            "NEWEST" -> list.sortedByDescending { it.acquiredTimestamp }
            else -> list.sortedByDescending { it.item.rarity.ordinal }
        }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2200)
            toastMessage = null
        }
    }

    fun persistInventory(newSet: Set<String>) {
        sharedPrefs.edit().putStringSet("case_inventory", newSet).apply()
        inventorySet = newSet
        viewModel.saveCurrentProfileToDb()
    }

    fun isItemCurrentlyEquipped(item: LootItem): Boolean {
        return when (item.type) {
            "skin" -> currentBoardSkin == item.id
            "cube_skin" -> currentBlockSkin == item.id
            "avatar_frame" -> currentAvatarFrame == item.id
            "nick_gradient" -> currentHasGradient
            "sound_pack" -> currentSoundPack == item.id
            else -> false
        }
    }

    fun equipInventoryItem(invItem: InventoryItem) {
        val item = invItem.item
        val localizedName = item.getLocalizedName(currentLang)
        when (item.type) {
            "skin" -> {
                val current = sharedPrefs.getStringSet("purchased_skins", setOf("cyberpunk")) ?: setOf("cyberpunk")
                val updated = current.toMutableSet().apply { add(item.id) }
                sharedPrefs.edit().putStringSet("purchased_skins", updated).apply()
                viewModel.setBoardColorSkin(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = when (currentLang) {
                    Language.RU -> "Экипирован скин: $localizedName"
                    Language.UA -> "Одягнено скін: $localizedName"
                    Language.KK -> "Скин киілді: $localizedName"
                    Language.DE -> "Skin ausgerüstet: $localizedName"
                    Language.ZH -> "已装备皮肤: $localizedName"
                    else -> "Equipped: $localizedName"
                }
            }
            "cube_skin" -> {
                val current = sharedPrefs.getStringSet("purchased_cube_skins", setOf("neon")) ?: setOf("neon")
                val updated = current.toMutableSet().apply { add(item.id) }
                sharedPrefs.edit().putStringSet("purchased_cube_skins", updated).apply()
                viewModel.setBlockStyle(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = when (currentLang) {
                    Language.RU -> "Экипированы блоки: $localizedName"
                    Language.UA -> "Одягнено блоки: $localizedName"
                    Language.KK -> "Блоктар киілді: $localizedName"
                    Language.DE -> "Blöcke ausgerüstet: $localizedName"
                    Language.ZH -> "已装备方块: $localizedName"
                    else -> "Equipped: $localizedName"
                }
            }
            "avatar_frame" -> {
                val current = viewModel.purchasedAvatarFrames.value
                val updated = current.toMutableSet().apply { add(item.id) }
                viewModel.setPurchasedAvatarFrames(updated)
                viewModel.setEquippedAvatarFrame(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = when (currentLang) {
                    Language.RU -> "Экипирована рамка: $localizedName"
                    Language.UA -> "Одягнено рамку: $localizedName"
                    Language.KK -> "Жақтау киілді: $localizedName"
                    Language.DE -> "Rahmen ausgerüstet: $localizedName"
                    Language.ZH -> "已装备边框: $localizedName"
                    else -> "Equipped: $localizedName"
                }
            }
            "sound_pack" -> {
                val current = viewModel.purchasedSoundPacks.value
                val updated = current.toMutableSet().apply { add(item.id) }
                viewModel.setPurchasedSoundPacks(updated)
                viewModel.setEquippedSoundPack(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = when (currentLang) {
                    Language.RU -> "Экипирован звук: $localizedName"
                    Language.UA -> "Одягнено звук: $localizedName"
                    Language.KK -> "Дыбыс қосылды: $localizedName"
                    Language.DE -> "Sound ausgerüstet: $localizedName"
                    Language.ZH -> "已装备音效: $localizedName"
                    else -> "Equipped: $localizedName"
                }
            }
            "nick_gradient" -> {
                sharedPrefs.edit().putBoolean("has_nickname_gradient", true).apply()
                viewModel.setHasNicknameGradient(true)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = when (currentLang) {
                    Language.RU -> "Градиент ника активирован!"
                    Language.UA -> "Градієнт ніка активовано!"
                    Language.KK -> "Ник градиенті қосылды!"
                    Language.DE -> "Farbverlauf aktiviert!"
                    Language.ZH -> "昵称炫彩渐变已激活！"
                    else -> "Nickname gradient active!"
                }
            }
        }
        viewModel.saveCurrentProfileToDb()
    }

    fun sellInventoryItem(invItem: InventoryItem) {
        if (invItem.isLocked) {
            toastMessage = when (currentLang) {
                Language.RU -> "Предмет заблокирован от продажи!"
                Language.UA -> "Предмет заблоковано від продажу!"
                Language.KK -> "Зат сатылымнан құлыпталған!"
                Language.DE -> "Item ist gesperrt!"
                Language.ZH -> "物品已被锁定不可出售！"
                else -> "Item is locked from sale!"
            }
            return
        }
        val price = getItemSellPrice(invItem.item.rarity)
        viewModel.addCredits(price)
        viewModel.triggerAudioFeedback("selling")
        toastMessage = "+$price 🪙"

        val updated = inventorySet.filter { !it.startsWith(invItem.uuid + ";") }.toSet()
        persistInventory(updated)
        if (inspectingItem?.uuid == invItem.uuid) {
            inspectingItem = null
        }
    }

    fun toggleLockItem(invItem: InventoryItem) {
        val updatedList = inventoryItems.map {
            if (it.uuid == invItem.uuid) it.copy(isLocked = !it.isLocked) else it
        }
        val newSet = updatedList.map { serializeInventoryItem(it) }.toSet()
        persistInventory(newSet)
        inspectingItem = updatedList.find { it.uuid == invItem.uuid }
        viewModel.triggerAudioFeedback("click")
        toastMessage = if (inspectingItem?.isLocked == true) {
            when (currentLang) {
                Language.RU -> "Предмет защищен от продажи"
                Language.UA -> "Предмет захищено від продажу"
                Language.KK -> "Зат қорғалды"
                Language.DE -> "Item gesperrt"
                Language.ZH -> "物品已锁定保护"
                else -> "Item locked from sale"
            }
        } else {
            when (currentLang) {
                Language.RU -> "Защита снята"
                Language.UA -> "Захист знято"
                Language.KK -> "Қорғау алынды"
                Language.DE -> "Entsperrt"
                Language.ZH -> "已解除保护锁定"
                else -> "Item unlocked"
            }
        }
    }

    fun executeMassRecycle() {
        val recyclable = inventoryItems.filter { !it.isLocked && (it.item.rarity == DropRarity.COMMON || it.item.rarity == DropRarity.UNCOMMON) }
        if (recyclable.isEmpty()) {
            toastMessage = when (currentLang) {
                Language.RU -> "Нет незаблокированных обычных предметов"
                Language.UA -> "Немає незаблокованих звичайних предметів"
                Language.KK -> "Құлыпталмаған қарапайым заттар жоқ"
                Language.DE -> "Keine entsperrten Standard-Items gefunden"
                Language.ZH -> "未找到可回收的未锁定普通物品"
                else -> "No unlocked common items found"
            }
            showMassRecycleDialog = false
            return
        }
        val totalEarned = recyclable.sumOf { getItemSellPrice(it.item.rarity) }
        val recyclableUuids = recyclable.map { it.uuid }.toSet()
        val remaining = inventoryItems.filter { !recyclableUuids.contains(it.uuid) }
        val newSet = remaining.map { serializeInventoryItem(it) }.toSet()

        viewModel.addCredits(totalEarned)
        viewModel.triggerAudioFeedback("selling")
        persistInventory(newSet)
        showMassRecycleDialog = false
        toastMessage = "+$totalEarned 🪙 (${recyclable.size})"
    }

    // ── CS2 Roulette Engine Setup ──
    val stripItemCount = 45
    val winIndex = 36
    var stripItems by remember { mutableStateOf<List<LootItem>>(emptyList()) }
    val scrollOffset = remember { Animatable(0f) }
    val scrollState = rememberScrollState()

    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val itemWidthDp = 100.dp
    val itemSpacingDp = 6.dp
    val strideDp = itemWidthDp + itemSpacingDp
    val stridePx = with(density) { strideDp.toPx() }
    val screenWidthPx = with(density) { screenWidthDp.toPx() }
    val stripHeightDp = 135.dp

    fun generateWeightedItem(crate: LootCrate): LootItem {
        val roll = (0..10000).random() / 100f
        var cumulative = 0f
        var selectedRarity = DropRarity.COMMON
        for ((rarity, chance) in crate.dropChances) {
            cumulative += chance
            if (roll <= cumulative) {
                selectedRarity = rarity
                break
            }
        }
        val pool = allLootItems.filter { it.rarity == selectedRarity }
        return if (pool.isNotEmpty()) pool.random() else allLootItems.filter { it.rarity == DropRarity.COMMON }.random()
    }

    fun startCrateOpening(crate: LootCrate) {
        val keyCount = crateKeys[crate.id.lowercase()] ?: 0
        val hasKey = keyCount > 0
        if (!hasKey && credits < crate.cost) return
        if (openingCrate != null) return
        previewCrate = null
        if (hasKey) {
            viewModel.useCrateKey(crate.id)
        } else {
            viewModel.spendCredits(crate.cost)
        }
        viewModel.triggerAudioFeedback("buy")
        openingCrate = crate
    }

    // Crate opening animation driver
    LaunchedEffect(openingCrate) {
        if (openingCrate != null) {
            val crate = openingCrate!!
            lastOpenedCrate = crate
            isAnimating = true
            showResult = false

            val winningItem = rollDrop(crate)
            droppedItem = winningItem

            if (fastOpenMode) {
                delay(200)
            } else {
                val strip = MutableList(stripItemCount) { generateWeightedItem(crate) }
                strip[winIndex] = winningItem
                stripItems = strip

                scrollOffset.snapTo(0f)
                scrollState.scrollTo(0)
                viewModel.triggerAudioFeedback("case_spin")

                val randomJitter = (-15..15).random().toFloat()
                val targetOffset = (winIndex * stridePx) - (screenWidthPx / 2f) + (stridePx / 2f) + randomJitter

                var lastTickIndex = -1
                var lastScrollVal = 0f

                scrollOffset.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(
                        durationMillis = 4400,
                        easing = CubicBezierEasing(0.05f, 0.98f, 0.12f, 1.0f)
                    )
                ) {
                    val delta = this.value - lastScrollVal
                    lastScrollVal = this.value
                    scrollState.dispatchRawDelta(delta)

                    val currentCenterIndex = ((this.value + screenWidthPx / 2f) / stridePx).toInt()
                    if (currentCenterIndex != lastTickIndex && currentCenterIndex in stripItems.indices) {
                        lastTickIndex = currentCenterIndex
                        viewModel.triggerAudioFeedback("tick")
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                delay(400)
            }

            // Drop Sound FX
            val sound = when (winningItem.rarity) {
                DropRarity.COMMON -> "drop_common"
                DropRarity.UNCOMMON -> "drop_uncommon"
                DropRarity.RARE -> "drop_rare"
                DropRarity.EPIC -> "drop_epic"
                DropRarity.LEGENDARY -> "drop_legendary"
                DropRarity.RED -> "drop_red"
            }
            viewModel.triggerAudioFeedback(sound)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            // Auto-grant credits & XP or add to inventory
            if (winningItem.type == "credits") {
                viewModel.addCredits(winningItem.creditValue)
                resultMessage = "+${winningItem.creditValue} 🪙"
            } else if (winningItem.type == "bonus_xp") {
                viewModel.addBonusXp(5000)
                resultMessage = "+5000 XP"
            } else {
                val newInvItem = InventoryItem(
                    uuid = java.util.UUID.randomUUID().toString(),
                    item = winningItem,
                    acquiredTimestamp = System.currentTimeMillis()
                )
                val updated = inventorySet.toMutableSet().apply {
                    add(serializeInventoryItem(newInvItem))
                }
                persistInventory(updated)
                resultMessage = when (currentLang) {
                    Language.RU -> "Предмет добавлен в инвентарь"
                    Language.UA -> "Предмет додано в інвентар"
                    Language.KK -> "Зат инвентарьге қосылды"
                    Language.DE -> "Item dem Inventar hinzugefügt"
                    Language.ZH -> "物品已存入仓库"
                    else -> "Item delivered to inventory"
                }
            }

            isAnimating = false
            showResult = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {},
                actions = {
                    Surface(
                        modifier = Modifier.padding(end = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            AdaptiveText(
                                text = String.format(Locale.getDefault(), "%,d", credits),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val widthDp = maxWidth

            Column(modifier = Modifier.fillMaxSize()) {
                // Tactical Sliding Tab Switcher
                val mainTabs = listOf(
                    Triple(0, Translations.get("cases", currentLang), Icons.Default.Inventory2),
                    Triple(1, "${Translations.get("inventory", currentLang)} (${inventoryItems.size})", Icons.Default.Workspaces)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabWidth = maxWidth / mainTabs.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * activeSubTab,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                            label = "caseTabIndicator"
                        )

                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(42.dp)
                                .offset(x = indicatorOffset)
                                .clip(RoundedCornerShape(16.dp))
                                .background(themeColor)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            mainTabs.forEach { (index, title, icon) ->
                                val isSelected = activeSubTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            activeSubTab = index
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sub-screens
                AnimatedContent(
                    targetState = activeSubTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (slideInHorizontally { (it * 0.3f * direction).toInt() } + fadeIn()).togetherWith(
                            slideOutHorizontally { (it * 0.3f * -direction).toInt() } + fadeOut()
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    label = "CasesTabTransition"
                ) { tab ->
                    if (tab == 0) {
                        // ── CRATES LIST ──
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Fast Open Toggle Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = Translations.get("fast_open", currentLang),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Switch(
                                    checked = fastOpenMode,
                                    onCheckedChange = { fastOpenMode = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = themeColor,
                                        checkedTrackColor = themeColor.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            // Crates Grid
                            val colCount = if (widthDp >= 900.dp) 3 else if (widthDp >= 600.dp) 2 else 1
                            val cardWidth = if (colCount > 1) (widthDp - 32.dp - (14.dp * (colCount - 1))) / colCount else widthDp

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                lootCrates.forEach { crate ->
                                    CrateTacticalCard(
                                        crate = crate,
                                        currentLang = currentLang,
                                        credits = credits,
                                        keyCount = crateKeys[crate.id.lowercase()] ?: 0,
                                        modifier = Modifier.width(cardWidth),
                                        onInspect = { previewCrate = crate },
                                        onOpen = { startCrateOpening(crate) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } else {
                        // ── INVENTORY VIEW ──
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Inventory Filter Chips Row
                            val categories = listOf(
                                "ALL" to when (currentLang) {
                                    Language.RU -> "Все"
                                    Language.UA -> "Все"
                                    Language.KK -> "Барлығы"
                                    Language.DE -> "Alle"
                                    Language.ZH -> "全部"
                                    else -> "All"
                                },
                                "SKINS" to when (currentLang) {
                                    Language.RU -> "Сетки"
                                    Language.UA -> "Сітки"
                                    Language.KK -> "Торлар"
                                    Language.DE -> "Gitter"
                                    Language.ZH -> "网格"
                                    else -> "Grids"
                                },
                                "BLOCKS" to when (currentLang) {
                                    Language.RU -> "Блоки"
                                    Language.UA -> "Блоки"
                                    Language.KK -> "Блоктар"
                                    Language.DE -> "Blöcke"
                                    Language.ZH -> "方块"
                                    else -> "Blocks"
                                },
                                "FRAMES" to when (currentLang) {
                                    Language.RU -> "Рамки"
                                    Language.UA -> "Рамки"
                                    Language.KK -> "Жақтаулар"
                                    Language.DE -> "Rahmen"
                                    Language.ZH -> "边框"
                                    else -> "Frames"
                                },
                                "SPECIAL" to when (currentLang) {
                                    Language.RU -> "Особое"
                                    Language.UA -> "Особливе"
                                    Language.KK -> "Ерекше"
                                    Language.DE -> "Spezial"
                                    Language.ZH -> "特殊"
                                    else -> "Special"
                                }
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { (key, title) ->
                                    val isSelected = selectedInvCategory == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            selectedInvCategory = key
                                        },
                                        label = {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = themeColor,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }

                            // Mass Recycle Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AdaptiveText(
                                    text = "${Translations.get("inventory", currentLang).uppercase()} (${filteredInventory.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                FilledTonalButton(
                                    onClick = { showMassRecycleDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFFF5252).copy(alpha = 0.15f),
                                        contentColor = Color(0xFFFF5252)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Translations.get("recycle_commons", currentLang),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Inventory Items Grid
                            if (filteredInventory.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = Translations.get("inventory_empty", currentLang),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = Translations.get("open_crates_hint", currentLang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    filteredInventory.chunked(2).forEach { rowPair ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            for (invItem in rowPair) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    InventoryTacticalCard(
                                                        invItem = invItem,
                                                        isEquipped = isItemCurrentlyEquipped(invItem.item),
                                                        currentLang = currentLang,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onInspect = { inspectingItem = invItem },
                                                        onEquip = { equipInventoryItem(invItem) },
                                                        onSell = { sellInventoryItem(invItem) }
                                                    )
                                                }
                                            }
                                            if (rowPair.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════
            // CRATE INSPECT FULL MODAL OVERLAY
            // ══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = previewCrate != null,
                enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 3 }),
                exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 3 })
            ) {
                previewCrate?.let { crate ->
                    BackHandler { previewCrate = null }
                    val previewKeyCount = crateKeys[crate.id.lowercase()] ?: 0
                    val previewHasKey = previewKeyCount > 0
                    val canAfford = previewHasKey || (credits >= crate.cost)
                    val dropsList = allLootItems.filter { item ->
                        crate.dropChances.getOrDefault(item.rarity, 0f) > 0f
                    }.sortedByDescending { it.rarity.ordinal }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                        AdaptiveText(
                                            text = crate.getLocalizedName(currentLang).uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { previewCrate = null }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close")
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            },
                            bottomBar = {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    tonalElevation = 8.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { startCrateOpening(crate) },
                                            enabled = canAfford && (openingCrate == null),
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = crate.accentColor,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            if (previewHasKey) {
                                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (currentLang == Language.RU) "Открыть за Ключ (Осталось: $previewKeyCount)"
                                                           else if (currentLang == Language.UA) "Відкрити за Ключ (Залишилось: $previewKeyCount)"
                                                           else if (currentLang == Language.KK) "Кілтпен ашу (Қалды: $previewKeyCount)"
                                                           else "Open with Key ($previewKeyCount left)",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                                                )
                                            } else {
                                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${Translations.get("open_for", currentLang)} ${crate.cost} 🪙",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        ) { contentPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(contentPadding)
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Hero Crate Banner
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(1.5.dp, crate.accentColor.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(72.dp),
                                            shape = RoundedCornerShape(24.dp),
                                            color = crate.accentColor.copy(alpha = 0.18f),
                                            border = BorderStroke(2.dp, crate.accentColor.copy(alpha = 0.6f))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = crate.icon,
                                                    contentDescription = null,
                                                    tint = crate.accentColor,
                                                    modifier = Modifier.size(38.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = crate.getLocalizedName(currentLang),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // Drop Probabilities Breakdown
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            crate.dropChances.forEach { (rarity, chance) ->
                                                if (chance > 0f) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = rarity.color.copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, rarity.color.copy(alpha = 0.3f)),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(vertical = 6.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Text(
                                                                text = "${chance}%",
                                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                                color = rarity.color
                                                            )
                                                            Text(
                                                                text = rarity.getLocalizedName(currentLang),
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                                color = rarity.color.copy(alpha = 0.8f),
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // List of Drops
                                Text(
                                    text = "${Translations.get("crate_contents", currentLang)} (${dropsList.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    dropsList.forEach { item ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            border = BorderStroke(1.dp, item.rarity.color.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = item.rarity.color.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(38.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = iconForItemType(item.type),
                                                            contentDescription = null,
                                                            tint = item.rarity.color,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.getLocalizedName(currentLang),
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = item.rarity.getLocalizedName(currentLang),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        color = item.rarity.color,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                                ) {
                                                    Text(
                                                        text = "≈ ${getItemSellPrice(item.rarity)} 🪙",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                                        color = Color(0xFFFFD700),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
            }

            // ══════════════════════════════════════════════════════
            // CS2 ROULETTE & DROP RESULT MODAL OVERLAY
            // ══════════════════════════════════════════════════════
            if (isAnimating || showResult) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF090A10).copy(alpha = 0.95f))
                        .clickable(enabled = showResult) {
                            showResult = false
                            openingCrate = null
                            droppedItem = null
                            resultMessage = null
                            stripItems = emptyList()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Roulette Spinner
                    if (isAnimating && stripItems.isNotEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "ВЗЛОМ ЗАМКА КЕЙСА..."
                                    Language.UA -> "ЗЛАМ ЗАМКА КЕЙСА..."
                                    Language.KK -> "КЕЙС ҚҰЛПЫН АШУ..."
                                    Language.DE -> "KISTE WIRD GEÖFFNET..."
                                    Language.ZH -> "正在破译开启军械箱..."
                                    else -> "OPENING CRATE..."
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            // Roulette container with neon laser indicator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(stripHeightDp + 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(stripHeightDp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF10121A))
                                        .border(
                                            width = 2.dp,
                                            brush = Brush.horizontalGradient(
                                                listOf(
                                                    openingCrate?.accentColor ?: Color(0xFFFFD700),
                                                    openingCrate?.secondaryColor ?: Color(0xFFFF6D00)
                                                )
                                            ),
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.horizontalScroll(scrollState, enabled = false),
                                        horizontalArrangement = Arrangement.spacedBy(itemSpacingDp)
                                    ) {
                                        stripItems.forEach { lootItem ->
                                            Box(
                                                modifier = Modifier
                                                    .width(itemWidthDp)
                                                    .height(stripHeightDp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF181B26))
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(4.dp)
                                                            .background(lootItem.rarity.color)
                                                    )
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    Icon(
                                                        imageVector = iconForItemType(lootItem.type),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(38.dp),
                                                        tint = lootItem.rarity.color
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = lootItem.getLocalizedName(currentLang),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Text(
                                                        text = lootItem.rarity.getLocalizedName(currentLang),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                                        color = lootItem.rarity.color,
                                                        modifier = Modifier.padding(bottom = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Edge Dark Fades
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(60.dp)
                                            .align(Alignment.CenterStart)
                                            .background(Brush.horizontalGradient(listOf(Color(0xFF10121A), Color.Transparent)))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(60.dp)
                                            .align(Alignment.CenterEnd)
                                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF10121A))))
                                    )
                                }

                                // Center Glowing Laser
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .width(3.dp)
                                        .height(stripHeightDp + 16.dp)
                                        .background(Color(0xFFFFD700))
                                )
                            }
                        }
                    }

                    // ── VICTORY DROP SHOWCASE ──
                    if (!isAnimating && showResult && droppedItem != null) {
                        val item = droppedItem!!
                        val openedCrate = lastOpenedCrate
                        val resultScale = remember { Animatable(0.75f) }

                        LaunchedEffect(Unit) {
                            resultScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .scale(resultScale.value)
                                .clickable(enabled = false) {},
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF141722)
                            ),
                            border = BorderStroke(2.dp, item.rarity.color.copy(alpha = 0.7f)),
                            elevation = CardDefaults.cardElevation(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Rarity Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = item.rarity.color.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, item.rarity.color)
                                ) {
                                    Text(
                                        text = item.rarity.getLocalizedName(currentLang).uppercase(),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        ),
                                        color = item.rarity.color
                                    )
                                }

                                // Interactive Item Preview
                                Surface(
                                    modifier = Modifier.size(90.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    color = item.rarity.color.copy(alpha = 0.15f),
                                    border = BorderStroke(2.dp, item.rarity.color.copy(alpha = 0.5f))
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = iconForItemType(item.type),
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp),
                                            tint = item.rarity.color
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = item.getLocalizedName(currentLang),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = getLocalizedItemTypeName(item.type, currentLang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                resultMessage?.let { msg ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Buttons Row
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            showResult = false
                                            openingCrate = null
                                            droppedItem = null
                                            resultMessage = null
                                            stripItems = emptyList()
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = item.rarity.color,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                Language.RU -> "ЗАБРАТЬ В ИНВЕНТАРЬ"
                                                Language.UA -> "ЗАБРАТИ В ІНВЕНТАР"
                                                Language.KK -> "ИНВЕНТАРЬГЕ АЛУ"
                                                Language.DE -> "INS INVENTAR"
                                                Language.ZH -> "收入仓库"
                                                else -> "COLLECT TO BAG"
                                            },
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }

                                    val resultKeyCount = openedCrate?.let { crateKeys[it.id.lowercase()] ?: 0 } ?: 0
                                    val resultHasKey = resultKeyCount > 0
                                    if (openedCrate != null && (resultHasKey || credits >= openedCrate.cost)) {
                                        OutlinedButton(
                                            onClick = {
                                                showResult = false
                                                droppedItem = null
                                                resultMessage = null
                                                stripItems = emptyList()
                                                startCrateOpening(openedCrate)
                                            },
                                            modifier = Modifier.fillMaxWidth().height(46.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.5.dp, openedCrate.accentColor)
                                        ) {
                                            Text(
                                                text = if (resultHasKey) {
                                                    if (currentLang == Language.RU) "🔑 Открыть ещё раз (Ключ)"
                                                    else if (currentLang == Language.UA) "🔑 Відкрити ще раз (Ключ)"
                                                    else if (currentLang == Language.KK) "🔑 Қайта ашу (Кілт)"
                                                    else "🔑 Open Again (Key)"
                                                } else "${Translations.get("open_for", currentLang)} ${openedCrate.cost} 🪙",
                                                fontWeight = FontWeight.Bold,
                                                color = openedCrate.accentColor,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════
            // ITEM INSPECTOR MODAL
            // ══════════════════════════════════════════════════════
            inspectingItem?.let { invItem ->
                ItemInspectDialog(
                    invItem = invItem,
                    isEquipped = isItemCurrentlyEquipped(invItem.item),
                    currentLang = currentLang,
                    onDismiss = { inspectingItem = null },
                    onEquip = {
                        equipInventoryItem(invItem)
                        inspectingItem = null
                    },
                    onToggleLock = { toggleLockItem(invItem) },
                    onSell = { sellInventoryItem(invItem) }
                )
            }

            // Mass Recycle Confirmation Dialog
            if (showMassRecycleDialog) {
                val recyclable = inventoryItems.filter { !it.isLocked && (it.item.rarity == DropRarity.COMMON || it.item.rarity == DropRarity.UNCOMMON) }
                val totalValue = recyclable.sumOf { getItemSellPrice(it.item.rarity) }

                AlertDialog(
                    onDismissRequest = { showMassRecycleDialog = false },
                    title = {
                        Text(
                            text = Translations.get("mass_recycle", currentLang),
                            fontWeight = FontWeight.Black
                        )
                    },
                    text = {
                        Text(
                            text = when (currentLang) {
                                Language.RU -> "Будет продано ${recyclable.size} предметов (Базовый / Особый, кроме заблокированных).\n\nВы получите: +$totalValue 🪙"
                                Language.UA -> "Буде продано ${recyclable.size} предметів (Базовий / Особливий, крім заблокованих).\n\nВи отримаєте: +$totalValue 🪙"
                                Language.KK -> "${recyclable.size} зат сатылады (Базалық / Арнайы, құлыпталғандардан басқа).\n\nСіз аласыз: +$totalValue 🪙"
                                Language.DE -> "${recyclable.size} Items werden verwertet (Basis / Spezial, außer gesperrte).\n\nErtrag: +$totalValue 🪙"
                                Language.ZH -> "将回收 ${recyclable.size} 件普通/特殊物品（不含已锁定物品）。\n\n您将获得: +$totalValue 🪙"
                                else -> "Recycle ${recyclable.size} basic/special items (excluding locked items).\n\nYou will receive: +$totalValue 🪙"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { executeMassRecycle() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text(Translations.get("mass_recycle_confirm", currentLang), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMassRecycleDialog = false }) {
                            Text(Translations.get("cancel", currentLang))
                        }
                    }
                )
            }

            // Toast Floating Notification
            toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// TACTICAL CRATE CARD COMPOSABLE
// ══════════════════════════════════════════════════════════════════
@Composable
private fun CrateTacticalCard(
    crate: LootCrate,
    currentLang: Language,
    credits: Int,
    keyCount: Int = 0,
    modifier: Modifier = Modifier,
    onInspect: () -> Unit,
    onOpen: () -> Unit
) {
    val hasKey = keyCount > 0
    val canAfford = hasKey || (credits >= crate.cost)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, crate.accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Crate Icon Badge
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = crate.accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, crate.accentColor.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = crate.icon,
                            contentDescription = null,
                            tint = crate.accentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = crate.getLocalizedName(currentLang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${crate.cost} 🪙",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = crate.accentColor
                        )
                        if (hasKey) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "🔑 $keyCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onInspect,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Inspect",
                        tint = crate.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Probability Segment Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                crate.dropChances.forEach { (rarity, chance) ->
                    if (chance > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(chance)
                                .fillMaxHeight()
                                .background(rarity.color)
                        )
                    }
                }
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = Translations.get("chances", currentLang),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onOpen,
                    enabled = canAfford,
                    modifier = Modifier.weight(1.2f).height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = crate.accentColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (hasKey) "🔑 ${Translations.get("open", currentLang).uppercase()}" else Translations.get("open", currentLang).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// TACTICAL INVENTORY CARD COMPOSABLE
// ══════════════════════════════════════════════════════════════════
@Composable
private fun InventoryTacticalCard(
    invItem: InventoryItem,
    isEquipped: Boolean,
    currentLang: Language,
    modifier: Modifier = Modifier,
    onInspect: () -> Unit,
    onEquip: () -> Unit,
    onSell: () -> Unit
) {
    val item = invItem.item
    val rarityColor = item.rarity.color
    val sellPrice = getItemSellPrice(item.rarity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = if (isEquipped) 1.8.dp else 1.dp,
            color = if (isEquipped) Color(0xFF00E676) else rarityColor.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Rarity & Lock/Equipped status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = rarityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = item.rarity.getLocalizedName(currentLang).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Black),
                        color = rarityColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (invItem.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = Translations.get("active", currentLang),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Black),
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Thumbnail
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                color = rarityColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = iconForItemType(item.type),
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Name
            Column {
                Text(
                    text = item.getLocalizedName(currentLang),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "≈ $sellPrice 🪙",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                    color = Color(0xFFFFD700)
                )
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onEquip,
                    modifier = Modifier.weight(1f).height(30.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEquipped) Color(0xFF00E676).copy(alpha = 0.2f) else rarityColor,
                        contentColor = if (isEquipped) Color(0xFF00E676) else Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isEquipped) Translations.get("active", currentLang) else Translations.get("equip", currentLang),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black)
                    )
                }

                FilledTonalButton(
                    onClick = onSell,
                    enabled = !invItem.isLocked,
                    modifier = Modifier.weight(0.7f).height(30.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFF5252).copy(alpha = 0.15f),
                        contentColor = Color(0xFFFF5252)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = Translations.get("sell", currentLang),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// ITEM INSPECTOR DIALOG COMPOSABLE
// ══════════════════════════════════════════════════════════════════
@Composable
private fun ItemInspectDialog(
    invItem: InventoryItem,
    isEquipped: Boolean,
    currentLang: Language,
    onDismiss: () -> Unit,
    onEquip: () -> Unit,
    onToggleLock: () -> Unit,
    onSell: () -> Unit
) {
    val item = invItem.item
    val rarityColor = item.rarity.color
    val sellPrice = getItemSellPrice(item.rarity)
    val dateStr = remember(invItem.acquiredTimestamp) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(invItem.acquiredTimestamp))
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.5.dp, rarityColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Action Bar in Modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = rarityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = item.rarity.getLocalizedName(currentLang).uppercase(),
                            color = rarityColor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Row {
                        IconButton(onClick = onToggleLock) {
                            Icon(
                                imageVector = if (invItem.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (invItem.isLocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Big Visual Showcase
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = rarityColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = iconForItemType(item.type),
                            contentDescription = null,
                            tint = rarityColor,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                // Name & Type
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.getLocalizedName(currentLang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = getLocalizedItemTypeName(item.type, currentLang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Technical Metadata Table
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Стоимость утилизации"
                                    Language.UA -> "Вартість утилізації"
                                    Language.KK -> "Утилизация құны"
                                    Language.DE -> "Verkaufswert"
                                    Language.ZH -> "回收价格"
                                    else -> "Recycle Value"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(text = "$sellPrice 🪙", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = Color(0xFFFFD700))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Получен"
                                    Language.UA -> "Отримано"
                                    Language.KK -> "Алынған күні"
                                    Language.DE -> "Erhalten am"
                                    Language.ZH -> "获取时间"
                                    else -> "Acquired"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(text = dateStr, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = when (currentLang) {
                                    Language.RU -> "Статус экипировки"
                                    Language.UA -> "Статус екіпірування"
                                    Language.KK -> "Киілу күйі"
                                    Language.DE -> "Status"
                                    Language.ZH -> "装备状态"
                                    else -> "Equip Status"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isEquipped) Translations.get("active", currentLang) else when (currentLang) {
                                    Language.RU -> "В инвентаре"
                                    Language.UA -> "В інвентарі"
                                    Language.KK -> "Инвентарьде"
                                    Language.DE -> "Im Inventar"
                                    Language.ZH -> "在仓库中"
                                    else -> "In Bag"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isEquipped) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onEquip,
                        modifier = Modifier.weight(1.2f).height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) Color(0xFF00E676).copy(alpha = 0.2f) else rarityColor,
                            contentColor = if (isEquipped) Color(0xFF00E676) else Color.White
                        )
                    ) {
                        Text(
                            text = if (isEquipped) Translations.get("active", currentLang) else Translations.get("equip", currentLang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }

                    FilledTonalButton(
                        onClick = onSell,
                        enabled = !invItem.isLocked,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFF5252).copy(alpha = 0.15f),
                            contentColor = Color(0xFFFF5252)
                        )
                    ) {
                        Text(
                            text = "${Translations.get("sell", currentLang)} $sellPrice🪙",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}
