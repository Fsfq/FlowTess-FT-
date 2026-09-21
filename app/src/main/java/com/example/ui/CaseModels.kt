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
    "button_skin" -> when (lang) {
        Language.RU -> "Стиль кнопок"
        Language.UA -> "Стиль кнопок"
        Language.KK -> "Батырмалар стилі"
        Language.DE -> "Tasten-Stil"
        Language.ZH -> "按键样式"
        else -> "Button Skin"
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

fun formatItemQuantity(count: Int, lang: Language): String {
    val unit = when (lang) {
        Language.RU -> "шт."
        Language.UA -> "шт."
        Language.KK -> "дана"
        Language.DE -> "Stk."
        Language.ZH -> "件"
        else -> "pcs"
    }
    return "$count $unit"
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

data class StackedInventoryItem(
    val masterItem: InventoryItem,
    val items: List<InventoryItem>,
    val count: Int = items.size
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
    LootItem("glass", "cube_skin", "Matte", "Матовый", DropRarity.COMMON),
    LootItem("flat", "cube_skin", "Flat", "Плоский", DropRarity.UNCOMMON),
    LootItem("material", "cube_skin", "Crystal", "Кристалл", DropRarity.UNCOMMON),
    LootItem("glowing_jewel", "cube_skin", "Grid", "Сетка", DropRarity.RARE),
    LootItem("steampunk", "cube_skin", "Carbon", "Карбон", DropRarity.EPIC),
    LootItem("red_gradient", "cube_skin", "Crimson", "Красный", DropRarity.RED),
    LootItem("green_gradient", "cube_skin", "Emerald", "Изумруд", DropRarity.RED),
    LootItem("blue_gradient", "cube_skin", "Cobalt", "Кобальт", DropRarity.RED),
    LootItem("purple_gradient", "cube_skin", "Amethyst", "Аметист", DropRarity.RED),

    // Avatar Frames (Exclusive Red Crate Dynamic Rainbow Gradient Frame)
    LootItem("chrono_gl", "avatar_frame", "Gradient", "Градиент", DropRarity.RED),

    // Special
    LootItem("nick_gradient", "nick_gradient", "OVERDRIVE NICK", "Градиент Ника", DropRarity.RED)
)

// ── 8 Exact Crates (Strict original IDs, names & prices) ──
val lootCrates = listOf(
    LootCrate(
        id = "wooden", name = "Wooden Crate", nameRu = "Деревянный Кейс",
        cost = ShopPrices.getCrateCost("wooden"), accentColor = Color(0xFF8D6E63), secondaryColor = Color(0xFFA1887F),
        icon = Icons.Default.Inventory2,
        dropChances = mapOf(
            DropRarity.COMMON to 88f,
            DropRarity.UNCOMMON to 11f,
            DropRarity.RARE to 1f
        )
    ),
    LootCrate(
        id = "iron", name = "Iron Crate", nameRu = "Железный Кейс",
        cost = ShopPrices.getCrateCost("iron"), accentColor = Color(0xFF78909C), secondaryColor = Color(0xFF90A4AE),
        icon = Icons.Default.Inventory2,
        dropChances = mapOf(
            DropRarity.COMMON to 68f,
            DropRarity.UNCOMMON to 24f,
            DropRarity.RARE to 7f,
            DropRarity.EPIC to 1f
        )
    ),
    LootCrate(
        id = "golden", name = "Golden Crate", nameRu = "Золотой Кейс",
        cost = ShopPrices.getCrateCost("golden"), accentColor = Color(0xFFFFB300), secondaryColor = Color(0xFFFFD54F),
        icon = Icons.Default.CardGiftcard,
        dropChances = mapOf(
            DropRarity.COMMON to 52f,
            DropRarity.UNCOMMON to 34f,
            DropRarity.RARE to 10f,
            DropRarity.EPIC to 3.2f,
            DropRarity.LEGENDARY to 0.8f
        )
    ),
    LootCrate(
        id = "platinum", name = "Platinum Crate", nameRu = "Платиновый Кейс",
        cost = ShopPrices.getCrateCost("platinum"), accentColor = Color(0xFF7E57C2), secondaryColor = Color(0xFFB39DDB),
        icon = Icons.Default.AutoAwesome,
        dropChances = mapOf(
            DropRarity.COMMON to 42f,
            DropRarity.UNCOMMON to 38f,
            DropRarity.RARE to 13.5f,
            DropRarity.EPIC to 5f,
            DropRarity.LEGENDARY to 1.5f
        )
    ),
    LootCrate(
        id = "legendary", name = "Legendary Crate", nameRu = "Легендарный Кейс",
        cost = ShopPrices.getCrateCost("legendary"), accentColor = Color(0xFFFF6F00), secondaryColor = Color(0xFFFFAB40),
        icon = Icons.Default.EmojiEvents,
        dropChances = mapOf(
            DropRarity.COMMON to 35f,
            DropRarity.UNCOMMON to 38f,
            DropRarity.RARE to 17.5f,
            DropRarity.EPIC to 7f,
            DropRarity.LEGENDARY to 2.5f
        )
    ),
    LootCrate(
        id = "diamond", name = "Diamond Crate", nameRu = "Алмазный Кейс",
        cost = ShopPrices.getCrateCost("diamond"), accentColor = Color(0xFF00E5FF), secondaryColor = Color(0xFF80D8FF),
        icon = Icons.Default.Diamond,
        dropChances = mapOf(
            DropRarity.COMMON to 28f,
            DropRarity.UNCOMMON to 40f,
            DropRarity.RARE to 20f,
            DropRarity.EPIC to 8.5f,
            DropRarity.LEGENDARY to 3f,
            DropRarity.RED to 0.5f
        )
    ),
    LootCrate(
        id = "red_crate_lite", name = "Lite Red Crate", nameRu = "Красный Лайт",
        cost = ShopPrices.getCrateCost("red_crate_lite"), accentColor = Color(0xFFE57373), secondaryColor = Color(0xFFFFCDD2),
        icon = Icons.Default.Whatshot,
        dropChances = mapOf(
            DropRarity.COMMON to 25f,
            DropRarity.UNCOMMON to 42f,
            DropRarity.RARE to 21.2f,
            DropRarity.EPIC to 8f,
            DropRarity.LEGENDARY to 2.8f,
            DropRarity.RED to 1f
        )
    ),
    LootCrate(
        id = "red_crate", name = "Red Crate", nameRu = "Красный Кейс",
        cost = ShopPrices.getCrateCost("red_crate"), accentColor = Color(0xFFD32F2F), secondaryColor = Color(0xFFFF5252),
        icon = Icons.Default.Whatshot,
        dropChances = mapOf(
            DropRarity.COMMON to 15f,
            DropRarity.UNCOMMON to 35f,
            DropRarity.RARE to 31.5f,
            DropRarity.EPIC to 12f,
            DropRarity.LEGENDARY to 4.5f,
            DropRarity.RED to 2f
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
    "button_skin" -> Icons.Default.SmartButton
    "sound_pack" -> Icons.AutoMirrored.Filled.VolumeUp
    "nick_gradient" -> Icons.Default.ElectricBolt
    "bonus_xp" -> Icons.Default.MilitaryTech
    else -> Icons.Default.Token
}

fun getItemSellPrice(rarity: DropRarity): Int = when (rarity) {
    DropRarity.COMMON -> 50
    DropRarity.UNCOMMON -> 180
    DropRarity.RARE -> 600
    DropRarity.EPIC -> 2000
    DropRarity.LEGENDARY -> 6000
    DropRarity.RED -> 18000
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
