package com.example.ui

import androidx.activity.compose.BackHandler

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ── Drop Rarity Tiers ──
enum class DropRarity(val label: String, val labelRu: String, val color: Color) {
    COMMON("Common", "Обычный", Color(0xFF9E9E9E)),
    UNCOMMON("Uncommon", "Необычный", Color(0xFF4CAF50)),
    RARE("Rare", "Редкий", Color(0xFF2196F3)),
    EPIC("Epic", "Эпический", Color(0xFF9C27B0)),
    LEGENDARY("Legendary", "Легендарный", Color(0xFFFF9800)),
    RED("Mystic Red", "Красный", Color(0xFFD32F2F))
}

// ── Loot Item Definition ──
data class LootItem(
    val id: String,
    val type: String, // "skin", "cube_skin", "avatar_frame", "sound_pack", "credits"
    val displayName: String,
    val displayNameRu: String,
    val rarity: DropRarity,
    val creditValue: Int = 0 // only for credits type
)

// ── Crate Definition ──
data class LootCrate(
    val id: String,
    val name: String,
    val nameRu: String,
    val cost: Int,
    val accentColor: Color,
    val secondaryColor: Color,
    val icon: ImageVector,
    val dropChances: Map<DropRarity, Float> // percentage chances per rarity
)

// ── All possible loot items (NO TITLES!) ──
val allLootItems = listOf(
    // Grid Skins
    LootItem("retro_amber", "skin", "Amber", "Янтарь", DropRarity.COMMON),
    LootItem("emerald_matrix", "skin", "Matrix", "Матрица", DropRarity.COMMON),
    LootItem("vaporwave_pink", "skin", "Neon Pink", "Розовый Неон", DropRarity.UNCOMMON),
    LootItem("midnight_gold", "skin", "Gold", "Золото", DropRarity.RARE),
    LootItem("carbon_neutral", "skin", "Titan", "Титан", DropRarity.RARE),
    LootItem("plasma_storm", "skin", "Plasma", "Плазма", DropRarity.EPIC),
    LootItem("glacial_frost", "skin", "Ice", "Лёд", DropRarity.LEGENDARY),

    // Cube Skins
    LootItem("glass", "cube_skin", "Glass", "Стекло", DropRarity.COMMON),
    LootItem("retro", "cube_skin", "Retro", "Ретро", DropRarity.COMMON),
    LootItem("flat", "cube_skin", "Flat", "Плоский", DropRarity.UNCOMMON),
    LootItem("material", "cube_skin", "Material", "Материал", DropRarity.UNCOMMON),
    LootItem("glowing_jewel", "cube_skin", "Crystal", "Кристалл", DropRarity.RARE),
    LootItem("steampunk", "cube_skin", "Steampunk", "Стимпанк", DropRarity.EPIC),
    LootItem("red_gradient", "cube_skin", "Red Gradient", "Красный Градиент", DropRarity.RED),
    LootItem("green_gradient", "cube_skin", "Green Gradient", "Зелёный Градиент", DropRarity.RED),
    LootItem("blue_gradient", "cube_skin", "Blue Gradient", "Синий Градиент", DropRarity.RED),
    LootItem("purple_gradient", "cube_skin", "Purple Gradient", "Фиолетовый Градиент", DropRarity.RED),

    // Avatar Frames
    LootItem("neon_ae", "avatar_frame", "Neon", "Неон", DropRarity.UNCOMMON),
    LootItem("gold_ma", "avatar_frame", "Gold", "Золото", DropRarity.RARE),
    LootItem("chrono_gl", "avatar_frame", "Gradient Frame", "Градиент", DropRarity.RED),
    LootItem("omega_ti", "avatar_frame", "Titan Frame", "Титан", DropRarity.RARE),


    // Credit Drops
    LootItem("credits_50", "credits", "+50 🪙", "+50 🪙", DropRarity.COMMON, 50),
    LootItem("credits_100", "credits", "+100 🪙", "+100 🪙", DropRarity.COMMON, 100),
    LootItem("credits_250", "credits", "+250 🪙", "+250 🪙", DropRarity.UNCOMMON, 250),
    LootItem("credits_500", "credits", "+500 🪙", "+500 🪙", DropRarity.RARE, 500),
    LootItem("credits_1000", "credits", "+1000 🪙", "+1000 🪙", DropRarity.EPIC, 1000),
    LootItem("credits_2500", "credits", "+2500 🪙", "+2500 🪙", DropRarity.LEGENDARY, 2500),
    LootItem("nick_gradient", "nick_gradient", "NICK GRADIENT", "Градиент Ника", DropRarity.RED),
    LootItem("bonus_xp_5000", "bonus_xp", "+5000 XP", "+5000 Опыта", DropRarity.RED, 5000)
)

// ── 5 Crate Tiers ──
val lootCrates = listOf(
    LootCrate(
        id = "wooden", name = "Wooden Crate", nameRu = "Деревянный Кейс",
        cost = 125, accentColor = Color(0xFF8D6E63), secondaryColor = Color(0xFFA1887F),
        icon = Icons.Default.Inventory2,
        dropChances = mapOf(
            DropRarity.COMMON to 70f,
            DropRarity.UNCOMMON to 22f,
            DropRarity.RARE to 6f,
            DropRarity.EPIC to 1.8f,
            DropRarity.LEGENDARY to 0.2f
        )
    ),
    LootCrate(
        id = "iron", name = "Iron Crate", nameRu = "Железный Кейс",
        cost = 375, accentColor = Color(0xFF78909C), secondaryColor = Color(0xFF90A4AE),
        icon = Icons.Default.Inventory2,
        dropChances = mapOf(
            DropRarity.COMMON to 40f,
            DropRarity.UNCOMMON to 32f,
            DropRarity.RARE to 20f,
            DropRarity.EPIC to 7f,
            DropRarity.LEGENDARY to 1f
        )
    ),
    LootCrate(
        id = "golden", name = "Golden Crate", nameRu = "Золотой Кейс",
        cost = 750, accentColor = Color(0xFFFFB300), secondaryColor = Color(0xFFFFD54F),
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
        cost = 1250, accentColor = Color(0xFF7E57C2), secondaryColor = Color(0xFFB39DDB),
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
        cost = 2500, accentColor = Color(0xFFFF6F00), secondaryColor = Color(0xFFFFAB40),
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
        cost = 4500, accentColor = Color(0xFF00E5FF), secondaryColor = Color(0xFF80D8FF),
        icon = Icons.Default.Diamond,
        dropChances = mapOf(
            DropRarity.COMMON to 0f,
            DropRarity.UNCOMMON to 10f,
            DropRarity.RARE to 35f,
            DropRarity.EPIC to 35f,
            DropRarity.LEGENDARY to 20f
        )
    ),
    LootCrate(
        id = "red_crate_lite", name = "Lite Red Crate", nameRu = "Красный Лайт",
        cost = 7500, accentColor = Color(0xFFE57373), secondaryColor = Color(0xFFFFCDD2),
        icon = Icons.Default.Whatshot,
        dropChances = mapOf(
            DropRarity.COMMON to 31.5f,
            DropRarity.UNCOMMON to 32f,
            DropRarity.RARE to 27f,
            DropRarity.EPIC to 7f,
            DropRarity.LEGENDARY to 2f,
            DropRarity.RED to 0.5f
        )
    ),
    LootCrate(
        id = "red_crate", name = "Red Crate", nameRu = "Красный Кейс",
        cost = 25000, accentColor = Color(0xFFD32F2F), secondaryColor = Color(0xFFFF5252),
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

data class InventoryItem(
    val uuid: String,
    val item: LootItem
)

fun deserializeInventoryItem(raw: String): InventoryItem? {
    val parts = raw.split(";")
    if (parts.size < 6) return null
    val uuid = parts[0]
    val id = parts[1]
    val type = parts[2]
    var displayName = parts[3]
    var displayNameRu = parts[4]
    val rarityName = parts[5]
    val creditValue = parts.getOrNull(6)?.toIntOrNull() ?: 0

    // Re-resolve from allLootItems so renamed/rebalanced items update immediately in inventory
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
        )
    )
}

fun serializeInventoryItem(uuid: String, item: LootItem): String {
    return "$uuid;${item.id};${item.type};${item.displayName};${item.displayNameRu};${item.rarity.name};${item.creditValue}"
}

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

    var openingCrate by remember { mutableStateOf<LootCrate?>(null) }
    var droppedItem by remember { mutableStateOf<LootItem?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var previewCrate by remember { mutableStateOf<LootCrate?>(null) }

    val sharedPrefs = remember {
        viewModel.getApplication<android.app.Application>()
            .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
    }

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Crates, 1 = Inventory

    var inventorySet by remember {
        mutableStateOf(sharedPrefs.getStringSet("case_inventory", emptySet()) ?: emptySet())
    }

    val inventoryItems = remember(inventorySet) {
        inventorySet.mapNotNull { deserializeInventoryItem(it) }
            .sortedByDescending { it.item.rarity.ordinal }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2000)
            toastMessage = null
        }
    }

    fun sellInventoryItem(invItem: InventoryItem) {
        val item = invItem.item
        val sellPrice = when (item.rarity) {
            DropRarity.COMMON -> 35
            DropRarity.UNCOMMON -> 100
            DropRarity.RARE -> 250
            DropRarity.EPIC -> 600
            DropRarity.LEGENDARY -> 1500
            DropRarity.RED -> 5000
        }
        viewModel.addCredits(sellPrice)
        viewModel.triggerAudioFeedback("selling")
        toastMessage = if (currentLang == Language.RU) "Продано за $sellPrice 🪙!" else "Sold for $sellPrice 🪙!"

        val currentSet = sharedPrefs.getStringSet("case_inventory", emptySet()) ?: emptySet()
        val updatedInv = currentSet.filter { !it.startsWith(invItem.uuid + ";") }.toSet()
        sharedPrefs.edit().putStringSet("case_inventory", updatedInv).apply()
        inventorySet = updatedInv
        viewModel.saveCurrentProfileToDb()
    }

    fun useInventoryItem(invItem: InventoryItem) {
        val item = invItem.item
        when (item.type) {
            "skin" -> {
                val current = sharedPrefs.getStringSet("purchased_skins", setOf("cyberpunk")) ?: setOf("cyberpunk")
                val updated = current.toMutableSet().apply { add(item.id) }
                sharedPrefs.edit().putStringSet("purchased_skins", updated).apply()
                viewModel.setBoardColorSkin(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = if (currentLang == Language.RU) "Применен скин: ${item.displayNameRu}" else "Equipped skin: ${item.displayName}"
            }
            "cube_skin" -> {
                val current = sharedPrefs.getStringSet("purchased_cube_skins", setOf("neon")) ?: setOf("neon")
                val updated = current.toMutableSet().apply { add(item.id) }
                sharedPrefs.edit().putStringSet("purchased_cube_skins", updated).apply()
                viewModel.setBlockStyle(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = if (currentLang == Language.RU) "Применен стиль блоков: ${item.displayNameRu}" else "Equipped block style: ${item.displayName}"
            }
            "avatar_frame" -> {
                val current = viewModel.purchasedAvatarFrames.value
                val updated = current.toMutableSet().apply { add(item.id) }
                viewModel.setPurchasedAvatarFrames(updated)
                viewModel.setEquippedAvatarFrame(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = if (currentLang == Language.RU) "Применена рамка: ${item.displayNameRu}" else "Equipped frame: ${item.displayName}"
            }
            "sound_pack" -> {
                val current = viewModel.purchasedSoundPacks.value
                val updated = current.toMutableSet().apply { add(item.id) }
                viewModel.setPurchasedSoundPacks(updated)
                viewModel.setEquippedSoundPack(item.id)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = if (currentLang == Language.RU) "Применен звук. пакет: ${item.displayNameRu}" else "Equipped sound pack: ${item.displayName}"
            }
            "nick_gradient" -> {
                sharedPrefs.edit().putBoolean("has_nickname_gradient", true).apply()
                viewModel.setHasNicknameGradient(true)
                viewModel.triggerAudioFeedback("equip")
                toastMessage = if (currentLang == Language.RU) "Активирован градиент никнейма!" else "Activated nickname gradient!"
            }
        }

        viewModel.saveCurrentProfileToDb()
    }

    fun applyDrop(item: LootItem) {
        val refundByRarity = when (item.rarity) {
            DropRarity.COMMON -> 50
            DropRarity.UNCOMMON -> 150
            DropRarity.RARE -> 400
            DropRarity.EPIC -> 900
            DropRarity.LEGENDARY -> 2000
            DropRarity.RED -> 7500
        }
        when (item.type) {
            "credits" -> {
                viewModel.addCredits(item.creditValue)
                resultMessage = if (currentLang == Language.RU) "Получено ${item.creditValue} 🪙!" else "Received ${item.creditValue} 🪙!"
            }
            "skin" -> {
                val current = sharedPrefs.getStringSet("purchased_skins", setOf("cyberpunk")) ?: setOf("cyberpunk")
                if (current.contains(item.id)) {
                    // Duplicate — give credits instead
                    val refund = refundByRarity
                    viewModel.addCredits(refund)
                    resultMessage = if (currentLang == Language.RU) "Дубликат! Компенсация: $refund 🪙" else "Duplicate! Refund: $refund 🪙"
                } else {
                    val updated = current.toMutableSet().apply { add(item.id) }
                    sharedPrefs.edit().putStringSet("purchased_skins", updated).apply()
                    resultMessage = if (currentLang == Language.RU) "Получен скин: ${item.displayNameRu}" else "Unlocked skin: ${item.displayName}"
                }
            }
            "cube_skin" -> {
                val current = sharedPrefs.getStringSet("purchased_cube_skins", setOf("neon")) ?: setOf("neon")
                if (current.contains(item.id)) {
                    val refund = refundByRarity
                    viewModel.addCredits(refund)
                    resultMessage = if (currentLang == Language.RU) "Дубликат! Компенсация: $refund 🪙" else "Duplicate! Refund: $refund 🪙"
                } else {
                    val updated = current.toMutableSet().apply { add(item.id) }
                    sharedPrefs.edit().putStringSet("purchased_cube_skins", updated).apply()
                    resultMessage = if (currentLang == Language.RU) "Получен стиль блоков: ${item.displayNameRu}" else "Unlocked block style: ${item.displayName}"
                }
            }
            "avatar_frame" -> {
                val current = viewModel.purchasedAvatarFrames.value
                if (current.contains(item.id)) {
                    val refund = refundByRarity
                    viewModel.addCredits(refund)
                    resultMessage = if (currentLang == Language.RU) "Дубликат! Компенсация: $refund 🪙" else "Duplicate! Refund: $refund 🪙"
                } else {
                    val updated = current.toMutableSet().apply { add(item.id) }
                    viewModel.setPurchasedAvatarFrames(updated)
                    resultMessage = if (currentLang == Language.RU) "Получена рамка: ${item.displayNameRu}" else "Unlocked frame: ${item.displayName}"
                }
            }
            "sound_pack" -> {
                val current = viewModel.purchasedSoundPacks.value
                if (current.contains(item.id)) {
                    val refund = refundByRarity
                    viewModel.addCredits(refund)
                    resultMessage = if (currentLang == Language.RU) "Дубликат! Компенсация: $refund 🪙" else "Duplicate! Refund: $refund 🪙"
                } else {
                    val updated = current.toMutableSet().apply { add(item.id) }
                    viewModel.setPurchasedSoundPacks(updated)
                    resultMessage = if (currentLang == Language.RU) "Получен звук. пакет: ${item.displayNameRu}" else "Unlocked sound pack: ${item.displayName}"
                }
            }
            "nick_gradient" -> {
                val alreadyHas = sharedPrefs.getBoolean("has_nickname_gradient", false)
                if (alreadyHas) {
                    val refund = 5000
                    viewModel.addCredits(refund)
                    resultMessage = if (currentLang == Language.RU) "Дубликат! Компенсация: $refund 🪙" else "Duplicate! Refund: $refund 🪙"
                } else {
                    sharedPrefs.edit().putBoolean("has_nickname_gradient", true).apply()
                    viewModel.setHasNicknameGradient(true)
                    resultMessage = if (currentLang == Language.RU) "Получен градиент ника!" else "Unlocked nickname gradient!"
                }
            }
            "bonus_xp" -> {
                viewModel.addBonusXp(5000)
                resultMessage = if (currentLang == Language.RU) "Получено +5000 опыта!" else "Received +5000 XP!"
            }
        }
    }

    // ── Roulette state ──
    val stripItemCount = 35
    val winIndex = 28
    var stripItems by remember { mutableStateOf<List<LootItem>>(emptyList()) }
    var rouletteRunning by remember { mutableStateOf(false) }
    var rouletteFinished by remember { mutableStateOf(false) }
    val scrollOffset = remember { Animatable(0f) }
    val scrollState = rememberScrollState()

    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val itemWidthDp = 90.dp
    val itemSpacingDp = 4.dp
    val strideDp = itemWidthDp + itemSpacingDp // 94.dp
    val stridePx = with(density) { strideDp.toPx() }
    val screenWidthPx = with(density) { screenWidthDp.toPx() }
    val stripHeightDp = 120.dp

    // Helper to generate a random item weighted by crate's drop chances
    fun generateRandomItem(crate: LootCrate): LootItem {
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

    // Helper to get icon per item type
    fun iconForItemType(type: String): ImageVector = when (type) {
        "credits" -> Icons.Default.Star
        "skin" -> Icons.Default.Palette
        "cube_skin" -> Icons.Default.Category
        "avatar_frame" -> Icons.Default.Portrait
        "sound_pack" -> Icons.Default.VolumeUp
        else -> Icons.Default.Star
    }

    // Opening animation — builds strip, rolls the winning item, drives roulette
    LaunchedEffect(openingCrate) {
        if (openingCrate != null) {
            isAnimating = true
            showResult = false
            rouletteRunning = false
            rouletteFinished = false

            // 1. Roll the winning item
            val item = rollDrop(openingCrate!!)
            droppedItem = item

            // 2. Build a strip of random items, placing the winner at winIndex
            val crate = openingCrate!!
            val strip = MutableList(stripItemCount) { generateRandomItem(crate) }
            strip[winIndex] = item
            stripItems = strip

            // 3. Reset scroll and start roulette
            scrollOffset.snapTo(0f)
            scrollState.scrollTo(0)
            rouletteRunning = true
            viewModel.triggerAudioFeedback("case_spin")

            // 4. Calculate target offset: center the winning item under the indicator
            val randomVariation = (-15..15).random().toFloat() // small random jitter in px
            val targetOffset = (winIndex * stridePx) - (screenWidthPx / 2f) + (stridePx / 2f) + randomVariation

            // 5. Tick sound tracking
            var lastTickIndex = -1
            var lastScrollVal = 0f

            // 6. Animate with deceleration (optimized speed)
            scrollOffset.animateTo(
                targetValue = targetOffset,
                animationSpec = tween(
                    durationMillis = 2500,
                    easing = CubicBezierEasing(0.1f, 1.0f, 0.15f, 1.0f)
                )
            ) {
                val delta = this.value - lastScrollVal
                lastScrollVal = this.value
                scrollState.dispatchRawDelta(delta)

                // Tick sound as items cross the center
                val currentCenterIndex = ((this.value + screenWidthPx / 2f) / stridePx).toInt()
                if (currentCenterIndex != lastTickIndex && currentCenterIndex in stripItems.indices) {
                    lastTickIndex = currentCenterIndex
                    viewModel.triggerAudioFeedback("tick")
                }
            }

            // 7. Roulette finished — highlight briefly
            rouletteRunning = false
            rouletteFinished = true
            val raritySound = when (item.rarity) {
                DropRarity.COMMON -> "drop_common"
                DropRarity.UNCOMMON -> "drop_uncommon"
                DropRarity.RARE -> "drop_rare"
                DropRarity.EPIC -> "drop_epic"
                DropRarity.LEGENDARY -> "drop_legendary"
                DropRarity.RED -> "drop_red"
                else -> "success"
            }
            viewModel.triggerAudioFeedback(raritySound)
            delay(400)

            // 8. Apply the drop and show result card
            if (item.type == "credits" || item.type == "bonus_xp") {
                applyDrop(item)
            } else {
                val uuid = java.util.UUID.randomUUID().toString()
                val currentSet = sharedPrefs.getStringSet("case_inventory", emptySet()) ?: emptySet()
                val updated = currentSet.toMutableSet().apply {
                    add(serializeInventoryItem(uuid, item))
                }
                sharedPrefs.edit().putStringSet("case_inventory", updated).apply()
                inventorySet = updated
                viewModel.saveCurrentProfileToDb()
                resultMessage = if (currentLang == Language.RU) "Предмет добавлен в инвентарь!" else "Item added to inventory!"
            }
            rouletteFinished = false
            isAnimating = false
            showResult = true

            // 9. Auto-dismiss after timeout
            delay(2500)
            if (showResult) {
                showResult = false
                openingCrate = null
                droppedItem = null
                resultMessage = null
                stripItems = emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "КЕЙСЫ" else "CASES",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                actions = {
                    Surface(
                        modifier = Modifier.padding(end = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Credits",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AdaptiveText(
                                text = "$credits",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val widthDp = maxWidth
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Modern Unified Sliding Pill Tab Selector
                val caseTabs = listOf(
                    Triple(0, if (currentLang == Language.RU) "Кейсы" else "Cases", Icons.Default.CardGiftcard),
                    Triple(1, if (currentLang == Language.RU) "Инвентарь (${inventoryItems.size})" else "Inventory (${inventoryItems.size})", Icons.Default.Inventory2)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabWidth = maxWidth / caseTabs.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * activeSubTab,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                            label = "caseTabIndicator"
                        )

                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(44.dp)
                                .offset(x = indicatorOffset)
                                .clip(RoundedCornerShape(18.dp))
                                .background(themeColor)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            caseTabs.forEach { (index, title, icon) ->
                                val isSelected = activeSubTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(18.dp))
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
                                            contentDescription = title,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Smooth Directional Tab Transition
                AnimatedContent(
                    targetState = activeSubTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.35f * direction).toInt() },
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeIn(tween(220))).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> (fullWidth * 0.35f * -direction).toInt() },
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(tween(180))
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    label = "CasesTabContent"
                ) { currentTab ->
                    if (currentTab == 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AdaptiveText(
                                text = if (currentLang == Language.RU) "ВЫБЕРИ ЯЩИК" else "CHOOSE YOUR CRATE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val itemModifier = if (widthDp >= 900.dp) {
                                Modifier.width((widthDp - 64.dp) / 3) // 3 columns
                            } else if (widthDp >= 600.dp) {
                                Modifier.width((widthDp - 48.dp) / 2) // 2 columns
                            } else {
                                Modifier.fillMaxWidth() // 1 column
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                lootCrates.forEach { crate ->
                                    CrateCard(
                                        crate = crate,
                                        currentLang = currentLang,
                                        credits = credits,
                                        isOpening = openingCrate == crate,
                                        modifier = itemModifier,
                                        onOpen = {
                                            if (openingCrate == null) {
                                                previewCrate = crate
                                            }
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        // Inventory view
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AdaptiveText(
                                text = if (currentLang == Language.RU) "МОЙ ИНВЕНТАРЬ" else "MY INVENTORY",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (inventoryItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AdaptiveText(
                                        text = if (currentLang == Language.RU)
                                            "Ваш инвентарь пуст.\nОткрывайте ящики, чтобы получить скины!"
                                        else
                                            "Your inventory is empty.\nOpen crates to drop unique skins!",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            } else {
                                val columnsCount = if (widthDp >= 1000.dp) 4 else if (widthDp >= 600.dp) 3 else 2
                                val cardModifier = Modifier.width((widthDp - 32.dp - (12.dp * (columnsCount - 1))) / columnsCount)

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    inventoryItems.forEach { invItem ->
                                        InventoryItemCard(
                                            invItem = invItem,
                                            currentLang = currentLang,
                                            onUse = { useInventoryItem(invItem) },
                                            onSell = { sellInventoryItem(invItem) },
                                            modifier = cardModifier
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Floating Toast notification
            toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        AdaptiveText(
                            text = msg,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Compact MD3 Crate contents preview full-screen overlay
            AnimatedVisibility(
                visible = previewCrate != null,
                enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(250)),
                exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(200))
            ) {
                previewCrate?.let { crate ->
                    BackHandler { previewCrate = null }
                    val canAfford = credits >= crate.cost
                    val possibleItems = allLootItems.filter { item ->
                        crate.dropChances.getOrDefault(item.rarity, 0f) > 0f
                    }.sortedBy { it.rarity.ordinal }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            topBar = {
                                CenterAlignedTopAppBar(
                                    modifier = Modifier.statusBarsPadding(),
                                    title = {
                                        AdaptiveText(
                                            text = if (currentLang == Language.RU) crate.nameRu else crate.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { previewCrate = null }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close"
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
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shadowElevation = 8.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (!canAfford) {
                                            AdaptiveText(
                                                text = if (currentLang == Language.RU) "Недостаточно 🪙 для открытия!" else "Insufficient credits to open!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (openingCrate == null && canAfford) {
                                                    previewCrate = null
                                                    viewModel.spendCredits(crate.cost)
                                                    viewModel.triggerAudioFeedback("buy")
                                                    openingCrate = crate
                                                }
                                            },
                                            enabled = canAfford && (openingCrate == null),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(54.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = crate.accentColor,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CardGiftcard,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AdaptiveText(
                                                text = if (currentLang == Language.RU) "ОТКРЫТЬ ЗА ${crate.cost} 🪙" else "OPEN FOR ${crate.cost} 🪙",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        ) { paddingValues ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Crate Hero Header Card
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
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
                                            Surface(
                                                modifier = Modifier.size(54.dp),
                                                shape = RoundedCornerShape(18.dp),
                                                color = crate.accentColor.copy(alpha = 0.15f)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = crate.icon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(30.dp),
                                                        tint = crate.accentColor
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                AdaptiveText(
                                                    text = if (currentLang == Language.RU) crate.nameRu else crate.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                AdaptiveText(
                                                    text = if (currentLang == Language.RU) "Стоимость: ${crate.cost} 🪙" else "Cost: ${crate.cost} 🪙",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = crate.accentColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Drop Chances Chips Row without text names below
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            crate.dropChances.forEach { (rarity, chance) ->
                                                if (chance > 0f) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = rarity.color.copy(alpha = 0.15f),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            AdaptiveText(
                                                                text = "${chance.toInt()}%",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = rarity.color
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                AdaptiveText(
                                    text = if (currentLang == Language.RU) "ВОЗМОЖНЫЕ НАГРАДЫ (${possibleItems.size})" else "POSSIBLE DROPS (${possibleItems.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Compact Grid of items
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    possibleItems.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowItems.forEach { item ->
                                                ElevatedCard(
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(10.dp),
                                                            color = item.rarity.color.copy(alpha = 0.15f),
                                                            modifier = Modifier.size(34.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                    imageVector = iconForItemType(item.type),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(18.dp),
                                                                    tint = item.rarity.color
                                                                )
                                                            }
                                                        }

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            AdaptiveText(
                                                                text = if (currentLang == Language.RU) item.displayNameRu else item.displayName,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            AdaptiveText(
                                                                text = if (currentLang == Language.RU) item.rarity.labelRu else item.rarity.label,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                color = item.rarity.color,
                                                                fontWeight = FontWeight.SemiBold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (rowItems.size < 2) {
                                                Box(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            // ═══════════════════════════════════════════════════
            // CS:GO-STYLE ROULETTE OVERLAY
            // ═══════════════════════════════════════════════════
            if (isAnimating || showResult) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(enabled = showResult) {
                            showResult = false
                            openingCrate = null
                            droppedItem = null
                            resultMessage = null
                            stripItems = emptyList()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // ── ROULETTE SPINNER ──
                    if (isAnimating && stripItems.isNotEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // Title text
                            AdaptiveText(
                                text = if (currentLang == Language.RU) "ОТКРЫТИЕ..." else "OPENING...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Roulette container with center indicator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(stripHeightDp + 24.dp), // extra space for indicator arrows
                                contentAlignment = Alignment.Center
                            ) {
                                // Clipped roulette strip
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(stripHeightDp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0D0D1B))
                                        .border(
                                            width = 2.dp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    openingCrate?.accentColor ?: Color(0xFFFFD700),
                                                    openingCrate?.secondaryColor ?: Color(0xFFFFAB40)
                                                )
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .horizontalScroll(scrollState, enabled = false),
                                        horizontalArrangement = Arrangement.spacedBy(itemSpacingDp)
                                    ) {
                                        stripItems.forEachIndexed { index, lootItem ->
                                            val isWinner = rouletteFinished && index == winIndex
                                            val cardScale = if (isWinner) 1.08f else 1f

                                            // Individual item card
                                            Box(
                                                modifier = Modifier
                                                    .width(itemWidthDp)
                                                    .height(stripHeightDp)
                                                    .scale(cardScale)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF1A1A2E))
                                                    .then(
                                                        if (isWinner) Modifier
                                                            .border(
                                                                width = 2.dp,
                                                                color = lootItem.rarity.color,
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    listOf(
                                                                        lootItem.rarity.color.copy(alpha = 0.35f),
                                                                        Color(0xFF1A1A2E)
                                                                    )
                                                                )
                                                            )
                                                        else Modifier
                                                    )
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    // Colored rarity bar at top
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(4.dp)
                                                            .background(lootItem.rarity.color)
                                                    )

                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    // Item icon
                                                    Icon(
                                                        imageVector = iconForItemType(lootItem.type),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(32.dp),
                                                        tint = lootItem.rarity.color
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    // Item name
                                                    AdaptiveText(
                                                        text = if (currentLang == Language.RU) lootItem.displayNameRu else lootItem.displayName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )

                                                    Spacer(modifier = Modifier.weight(1f))

                                                    // Rarity label at bottom
                                                    AdaptiveText(
                                                        text = if (currentLang == Language.RU) lootItem.rarity.labelRu else lootItem.rarity.label,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                        color = lootItem.rarity.color.copy(alpha = 0.7f),
                                                        modifier = Modifier.padding(bottom = 6.dp),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Center indicator — top triangle
                                Canvas(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-12).dp)
                                        .size(20.dp)
                                ) {
                                    val path = Path().apply {
                                        moveTo(size.width / 2f, size.height)
                                        lineTo(0f, 0f)
                                        lineTo(size.width, 0f)
                                        close()
                                    }
                                    drawPath(path, color = Color(0xFFFFD700), style = Fill)
                                }

                                // Center indicator — bottom triangle
                                Canvas(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .offset(y = 12.dp)
                                        .size(20.dp)
                                ) {
                                    val path = Path().apply {
                                        moveTo(size.width / 2f, 0f)
                                        lineTo(0f, size.height)
                                        lineTo(size.width, size.height)
                                        close()
                                    }
                                    drawPath(path, color = Color(0xFFFFD700), style = Fill)
                                }

                                // Center vertical line
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .width(2.dp)
                                        .height(stripHeightDp + 4.dp)
                                        .background(Color(0xFFFFD700).copy(alpha = 0.8f))
                                )
                            }
                        }
                    }

                    // ── RESULT CARD ──
                    if (!isAnimating && showResult && droppedItem != null) {
                        val item = droppedItem!!

                        // Scale-in animation for the result card
                        val resultScale = remember { Animatable(0.7f) }
                        LaunchedEffect(Unit) {
                            resultScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .wrapContentHeight()
                                .scale(resultScale.value),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Rarity badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = item.rarity.color.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, item.rarity.color.copy(alpha = 0.4f))
                                ) {
                                    AdaptiveText(
                                        text = if (currentLang == Language.RU) item.rarity.labelRu.uppercase() else item.rarity.label.uppercase(),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = item.rarity.color,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Icon(
                                    imageVector = iconForItemType(item.type),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = item.rarity.color
                                )

                                AdaptiveText(
                                    text = if (currentLang == Language.RU) item.displayNameRu else item.displayName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                resultMessage?.let { msg ->
                                    AdaptiveText(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 3
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        showResult = false
                                        openingCrate = null
                                        droppedItem = null
                                        resultMessage = null
                                        stripItems = emptyList()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    AdaptiveText(
                                        text = if (currentLang == Language.RU) "ОТЛИЧНО" else "OK",
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun CrateCard(
    crate: LootCrate,
    currentLang: Language,
    credits: Int,
    isOpening: Boolean,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit
) {
    val canAfford = credits >= crate.cost

    ElevatedCard(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Crate icon avatar with gradient background
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = crate.accentColor.copy(alpha = 0.15f),
                    tonalElevation = 1.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = crate.icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = crate.accentColor
                        )
                    }
                }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AdaptiveText(
                            text = if (currentLang == Language.RU) crate.nameRu else crate.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        var showInfoDialog by remember { mutableStateOf(false) }
                        
                        IconButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Drop chances",
                                tint = crate.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        if (showInfoDialog) {
                            DropChancesDialog(crate = crate, currentLang = currentLang, onDismiss = { showInfoDialog = false })
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        crate.dropChances.forEach { (rarity, chance) ->
                            if (chance > 0f) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = rarity.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${chance.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = rarity.color,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Price and action bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (canAfford) crate.accentColor.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (canAfford) crate.accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        AdaptiveText(
                            text = "${crate.cost} 🪙",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = if (canAfford) crate.accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                // Inspect button
                Button(
                    onClick = onOpen,
                    enabled = !isOpening,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = crate.accentColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    AdaptiveText(
                        text = if (isOpening) "..." else if (currentLang == Language.RU) "ОТКРЫТЬ" else "OPEN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DropChancesDialog(
    crate: LootCrate,
    currentLang: Language,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdaptiveText(
                        text = if (currentLang == Language.RU) "${crate.nameRu} - Шансы" else "${crate.name} Chances",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = crate.accentColor
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                
                crate.dropChances.forEach { (rarity, chance) ->
                    if (chance > 0f) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(rarity.color)
                                )
                                AdaptiveText(
                                    text = if (currentLang == Language.RU) rarity.labelRu else rarity.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            AdaptiveText(
                                text = "${chance}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = rarity.color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    invItem: InventoryItem,
    currentLang: Language,
    onUse: () -> Unit,
    onSell: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = invItem.item
    val rarityColor = item.rarity.color
    val sellPrice = when (item.rarity) {
        DropRarity.COMMON -> 20
        DropRarity.UNCOMMON -> 50
        DropRarity.RARE -> 120
        DropRarity.EPIC -> 250
        DropRarity.LEGENDARY -> 600
        DropRarity.RED -> 3000
    }

    val typeLabel = when (item.type) {
        "skin" -> if (currentLang == Language.RU) "Скин поля" else "Board Skin"
        "cube_skin" -> if (currentLang == Language.RU) "Стиль блоков" else "Block Skin"
        "avatar_frame" -> if (currentLang == Language.RU) "Рамка аватара" else "Avatar Frame"
        "sound_pack" -> if (currentLang == Language.RU) "Звуковой пакет" else "Sound Pack"
        "nick_gradient" -> if (currentLang == Language.RU) "Градиент ника" else "Nick Gradient"
        else -> item.type
    }

    val categoryIcon = when (item.type) {
        "skin" -> Icons.Default.Palette
        "cube_skin" -> Icons.Default.Category
        "avatar_frame" -> Icons.Default.Portrait
        "sound_pack" -> Icons.Default.VolumeUp
        else -> Icons.Default.Star
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            rarityColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header (Rarity tag + Category Icon)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = rarityColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (currentLang == Language.RU) item.rarity.labelRu.uppercase() else item.rarity.label.uppercase(),
                            color = rarityColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    
                    Surface(
                        color = rarityColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = rarityColor,
                            modifier = Modifier
                                .padding(5.dp)
                                .size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Item Name
                Text(
                    text = if (currentLang == Language.RU) item.displayNameRu else item.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Equip button
                    Button(
                        onClick = onUse,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = rarityColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = if (currentLang == Language.RU) "НАДЕТЬ" else "EQUIP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    // Sell button
                    FilledTonalButton(
                        onClick = onSell,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFFB300).copy(alpha = 0.18f),
                            contentColor = Color(0xFFFF8F00)
                        ),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = if (currentLang == Language.RU) "ПРОДАТЬ $sellPrice 🪙" else "SELL $sellPrice 🪙",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
