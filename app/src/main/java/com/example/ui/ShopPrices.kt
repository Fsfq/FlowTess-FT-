package com.example.ui

/**
 * Single source of truth for all store item prices and economy requirements.
 * Any price modification here automatically reflects across UI and purchase handlers.
 */
object ShopPrices {

    // ── Grid / Board Skins ──
    const val SKIN_CYBERPUNK = 0
    const val SKIN_RETRO_AMBER = 300
    const val SKIN_EMERALD_MATRIX = 400
    const val SKIN_VAPORWAVE_PINK = 600
    const val SKIN_MIDNIGHT_GOLD = 1000
    const val SKIN_CARBON_NEUTRAL = 1200
    const val SKIN_PLASMA_STORM = 1500
    const val SKIN_GLACIAL_FROST = 1850

    fun getSkinCost(skinId: String): Int = when (skinId.lowercase()) {
        "cyberpunk" -> SKIN_CYBERPUNK
        "retro_amber" -> SKIN_RETRO_AMBER
        "emerald_matrix" -> SKIN_EMERALD_MATRIX
        "vaporwave_pink" -> SKIN_VAPORWAVE_PINK
        "midnight_gold" -> SKIN_MIDNIGHT_GOLD
        "carbon_neutral" -> SKIN_CARBON_NEUTRAL
        "plasma_storm" -> SKIN_PLASMA_STORM
        "glacial_frost" -> SKIN_GLACIAL_FROST
        else -> 0
    }

    // ── Cube / Block Skins ──
    const val CUBE_NEON = 0
    const val CUBE_GLASS = 200
    const val CUBE_FLAT = 400
    const val CUBE_MATERIAL = 500
    const val CUBE_GLOWING_JEWEL = 700
    const val CUBE_STEAMPUNK = 900

    fun getCubeSkinCost(cubeSkinId: String): Int = when (cubeSkinId.lowercase()) {
        "neon" -> CUBE_NEON
        "glass" -> CUBE_GLASS
        "flat" -> CUBE_FLAT
        "material" -> CUBE_MATERIAL
        "glowing_jewel" -> CUBE_GLOWING_JEWEL
        "steampunk" -> CUBE_STEAMPUNK
        else -> 0
    }

    // ── Game Modes ──
    const val MODE_CLASSIC = 0
    const val MODE_TIME_ATTACK = 800
    const val MODE_FAST_RUN = 1000
    const val MODE_RELAX = 1000
    const val MODE_MIRROR = 1200
    const val MODE_EXTENDED = 1500
    const val MODE_BLOCK_BLAST = 2000
    const val MODE_PATTERN = 2500
    const val MODE_MEMORY = 2000
    const val MODE_PERFECTIONIST = 5000

    fun getModeCost(modeId: String): Int = when (modeId.lowercase()) {
        "classic" -> MODE_CLASSIC
        "time_attack" -> MODE_TIME_ATTACK
        "fast_run", "hyper" -> MODE_FAST_RUN
        "relax" -> MODE_RELAX
        "mirror" -> MODE_MIRROR
        "extended" -> MODE_EXTENDED
        "block_blast" -> MODE_BLOCK_BLAST
        "pattern", "pattern_puzzle" -> MODE_PATTERN
        "memory", "memory_puzzle" -> MODE_MEMORY
        "perfectionist" -> MODE_PERFECTIONIST
        else -> 1000
    }

    // ── Avatar Frames ──
    const val FRAME_STANDARD = 0
    const val FRAME_WHITE = 400
    const val FRAME_BLUE = 600
    const val FRAME_GREEN = 800
    const val FRAME_YELLOW = 1000
    const val FRAME_ORANGE = 1200
    const val FRAME_RED = 1500
    const val FRAME_PURPLE = 1800
    const val FRAME_DARK = 2200

    fun getAvatarFrameCost(frameId: String): Int = when (frameId.lowercase()) {
        "standard" -> FRAME_STANDARD
        "frame_white" -> FRAME_WHITE
        "frame_blue" -> FRAME_BLUE
        "frame_green" -> FRAME_GREEN
        "frame_yellow" -> FRAME_YELLOW
        "frame_orange" -> FRAME_ORANGE
        "frame_red" -> FRAME_RED
        "frame_purple" -> FRAME_PURPLE
        "frame_dark" -> FRAME_DARK
        // Legacy fallback
        "neon_frame" -> 1500
        "gold_frame" -> 2000
        "cyber_frame" -> 2200
        "fire_frame" -> 2400
        "ice_frame" -> 2500
        "matrix_frame" -> 2600
        "galaxy_frame" -> 3000
        "rainbow_frame" -> 3500
        else -> 0
    }

    // ── Player Titles / Badges ──
    const val TITLE_NONE = 0
    const val TITLE_NODE = 2500
    const val TITLE_LORD = 5000
    const val TITLE_COSMIC_OVERLORD = 8000
    const val TITLE_AI_CONSENSUS = 12000

    fun getTitleCost(titleId: String): Int = when (titleId.lowercase()) {
        "none" -> TITLE_NONE
        "node" -> TITLE_NODE
        "lord" -> TITLE_LORD
        "cosmic_overlord" -> TITLE_COSMIC_OVERLORD
        "ai_consensus" -> TITLE_AI_CONSENSUS
        else -> 0
    }

    // ── Control Button Styles ──
    const val BUTTON_CLASSIC = 0
    const val BUTTON_NEON = 0
    const val BUTTON_GLASS = 600

    fun getButtonCost(buttonStyleId: String): Int = when (buttonStyleId.lowercase()) {
        "classic" -> BUTTON_CLASSIC
        "neon" -> BUTTON_NEON
        "glass" -> BUTTON_GLASS
        else -> 0
    }

    // ── Ranks ──
    const val RANK_BRONZE = 0
    const val RANK_SILVER = 1500
    const val RANK_GOLD = 3000
    const val RANK_PLATINUM = 4500
    const val RANK_DIAMOND = 7500
    const val RANK_MASTER = 12000
    const val RANK_GRANDMASTER = 16500
    const val RANK_CHALLENGER = 24000

    fun getRankCost(rankId: String): Int = when (rankId.uppercase()) {
        "BRONZE" -> RANK_BRONZE
        "SILVER" -> RANK_SILVER
        "GOLD" -> RANK_GOLD
        "PLATINUM" -> RANK_PLATINUM
        "DIAMOND" -> RANK_DIAMOND
        "MASTER" -> RANK_MASTER
        "GRANDMASTER" -> RANK_GRANDMASTER
        "CHALLENGER" -> RANK_CHALLENGER
        else -> 0
    }

    // ── Custom Leaderboard Tag ──
    const val CUSTOM_TAG_COST = 500000

    // ── Prestige System ──
    const val PRESTIGE_I_REQUIREMENT = 100000
    const val PRESTIGE_II_REQUIREMENT = 400000
    const val PRESTIGE_III_REQUIREMENT = 1000000

    // ── Loot Crates / Cases ──
    const val CRATE_WOODEN = 250
    const val CRATE_IRON = 500
    const val CRATE_GOLDEN = 1000
    const val CRATE_PLATINUM = 1250
    const val CRATE_LEGENDARY = 2500
    const val CRATE_DIAMOND = 4500
    const val CRATE_RED_LITE = 7500
    const val CRATE_RED = 15000

    fun getCrateCost(crateId: String): Int = when (crateId.lowercase()) {
        "wooden" -> CRATE_WOODEN
        "iron" -> CRATE_IRON
        "golden" -> CRATE_GOLDEN
        "platinum" -> CRATE_PLATINUM
        "legendary" -> CRATE_LEGENDARY
        "diamond" -> CRATE_DIAMOND
        "red_crate_lite" -> CRATE_RED_LITE
        "red_crate" -> CRATE_RED
        else -> 0
    }

    /**
     * Unified rarity resolver matching Cases drop rarities across all game cosmetics.
     */
    fun getCosmeticRarity(category: String, id: String): DropRarity {
        val lowerId = id.lowercase()
        return when (category.uppercase()) {
            "SKIN", "SKINS", "THEME" -> when (lowerId) {
                "cyberpunk", "retro_amber", "emerald_matrix" -> DropRarity.COMMON
                "vaporwave_pink" -> DropRarity.UNCOMMON
                "midnight_gold", "carbon_neutral" -> DropRarity.RARE
                "plasma_storm" -> DropRarity.EPIC
                "glacial_frost" -> DropRarity.LEGENDARY
                else -> DropRarity.COMMON
            }
            "CUBE", "CUBE_SKIN", "BLOCKS" -> when (lowerId) {
                "neon", "glass" -> DropRarity.COMMON
                "flat", "material" -> DropRarity.UNCOMMON
                "glowing_jewel" -> DropRarity.RARE
                "steampunk" -> DropRarity.EPIC
                "red_gradient", "green_gradient", "blue_gradient", "purple_gradient" -> DropRarity.RED
                else -> DropRarity.COMMON
            }
            "FRAME", "AVATAR_FRAME", "FRAMES" -> when (lowerId) {
                "standard" -> DropRarity.COMMON
                "frame_white", "frame_blue" -> DropRarity.UNCOMMON
                "frame_green", "frame_yellow" -> DropRarity.RARE
                "frame_orange", "frame_red", "frame_purple" -> DropRarity.EPIC
                "frame_dark" -> DropRarity.LEGENDARY
                "chrono_gl", "gradient_frame" -> DropRarity.RED
                // Legacy fallback
                "neon_frame" -> DropRarity.RARE
                "gold_frame", "cyber_frame", "fire_frame", "ice_frame", "matrix_frame" -> DropRarity.EPIC
                "galaxy_frame", "rainbow_frame" -> DropRarity.LEGENDARY
                else -> DropRarity.COMMON
            }
            "BUTTON", "BUTTONS", "BUTTON_SKIN" -> when (lowerId) {
                "classic", "neon" -> DropRarity.COMMON
                "glass" -> DropRarity.UNCOMMON
                else -> DropRarity.COMMON
            }
            "TITLE", "TITLES", "PLAYER_BADGES" -> when (lowerId) {
                "none" -> DropRarity.COMMON
                "node" -> DropRarity.RARE
                "lord" -> DropRarity.EPIC
                "cosmic_overlord", "ai_consensus" -> DropRarity.LEGENDARY
                else -> DropRarity.COMMON
            }
            "MODE", "MODES" -> when (lowerId) {
                "classic", "time_attack" -> DropRarity.COMMON
                "fast_run", "hyper", "relax" -> DropRarity.UNCOMMON
                "mirror", "extended" -> DropRarity.RARE
                "block_blast", "pattern", "pattern_puzzle", "memory", "memory_puzzle" -> DropRarity.EPIC
                "perfectionist" -> DropRarity.LEGENDARY
                else -> DropRarity.COMMON
            }
            "RANK", "RANKS" -> when (lowerId) {
                "bronze", "silver" -> DropRarity.COMMON
                "gold" -> DropRarity.UNCOMMON
                "platinum", "diamond" -> DropRarity.RARE
                "master", "grandmaster" -> DropRarity.EPIC
                "challenger" -> DropRarity.LEGENDARY
                else -> DropRarity.COMMON
            }
            "SPECIAL" -> when (lowerId) {
                "nick_gradient" -> DropRarity.RED
                "bonus_xp", "bonus_xp_5000", "bonus_xp_15000" -> DropRarity.RED
                else -> DropRarity.COMMON
            }
            else -> DropRarity.COMMON
        }
    }

    /**
     * Check if an item is strictly exclusive to Cases and must NEVER be sold in the direct store.
     */
    fun isCaseExclusive(id: String): Boolean {
        return when (id.lowercase()) {
            "red_gradient", "green_gradient", "blue_gradient", "purple_gradient",
            "chrono_gl", "gradient_frame", "nick_gradient", "bonus_xp_5000", "bonus_xp_15000" -> true
            else -> false
        }
    }
}
