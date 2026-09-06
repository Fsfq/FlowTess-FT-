package com.example.db

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Синхронизация данных с Firestore — профиль, инвентарь, настройки, статистика, рекорды
// Cloud sync: push/pull profile, inventory, settings, stats to/from Firestore
object FirebaseSync {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            // Already initialized or settings locked
        }
        db
    }

    // Конвертация файлов в base64 для хранения аватаров/фонов в Firestore
    fun fileToBase64(file: java.io.File): String? {
        if (!file.exists()) return null
        return try {
            val bytes = file.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun base64ToFile(base64Str: String, file: java.io.File) {
        try {
            val bytes = android.util.Base64.decode(base64Str, android.util.Base64.NO_WRAP)
            file.writeBytes(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Пушим ВСЕ данные юзера в Firestore
    suspend fun pushUserData(context: Context): Boolean = suspendCoroutine { continuation ->
        val user = auth.currentUser
        if (user == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }
        val profilePrefs = context.getSharedPreferences("block_tetris_prefs", Context.MODE_PRIVATE)
        val tetrisPrefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)
        val playerName = tetrisPrefs.getString("player_name", "Player 1") ?: "Player 1"

        val avatarFile = java.io.File(context.filesDir, "custom_avatar_${playerName}.jpg")
        val bgFile = java.io.File(context.filesDir, "custom_background_${playerName}.jpg")
        val avatarBase64 = fileToBase64(avatarFile)
        val bgBase64 = fileToBase64(bgFile)

        // Extract unlocked achievements
        val allPrefs = tetrisPrefs.all
        val unlockedAchievements = allPrefs.filter { it.key.startsWith("ach_") && it.key.endsWith("_unlocked") && it.value == true }
            .map { it.key.removePrefix("ach_").removeSuffix("_unlocked") }

        val data = mutableMapOf<String, Any>(
            "uid" to user.uid,
            "player_name" to playerName,
            "equipped_avatar_frame" to (profilePrefs.getString("equipped_avatar_frame", "standard") ?: "standard"),
            "purchased_avatar_frames" to (profilePrefs.getStringSet("purchased_avatar_frames", setOf("standard"))?.toList() ?: listOf("standard")),
            "equipped_title" to (profilePrefs.getString("equipped_title", "none") ?: "none"),
            "purchased_titles" to (profilePrefs.getStringSet("purchased_titles", setOf("none"))?.toList() ?: listOf("none")),
            "equipped_sound_pack" to (profilePrefs.getString("equipped_sound_pack", "arcade") ?: "arcade"),
            "purchased_sound_packs" to (profilePrefs.getStringSet("purchased_sound_packs", setOf("arcade"))?.toList() ?: listOf("arcade")),
            "purchased_themes" to (profilePrefs.getStringSet("purchased_themes", setOf("indigo", "neon", "red"))?.toList() ?: listOf("indigo", "neon", "red")),
            "purchased_fonts" to (profilePrefs.getStringSet("purchased_fonts", setOf("default", "monospace"))?.toList() ?: listOf("default", "monospace")),
            "purchased_control_button_styles" to (profilePrefs.getStringSet("purchased_control_button_styles", setOf("classic", "neon"))?.toList() ?: listOf("classic", "neon")),
            "custom_avatar_emoji" to (profilePrefs.getString("custom_avatar_emoji", "") ?: ""),
            "custom_avatar_bg_color" to (profilePrefs.getString("custom_avatar_bg_color", "3A3C44") ?: "3A3C44"),
            "online_tier" to (profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE"),
            "credits" to tetrisPrefs.getInt("credits", 750),
            "purchased_cube_skins" to (profilePrefs.getStringSet("purchased_cube_skins", setOf("neon"))?.toList() ?: listOf("neon")),
            "case_inventory" to (profilePrefs.getStringSet("case_inventory", emptySet())?.toList() ?: emptyList()),
            "purchased_modes" to (profilePrefs.getStringSet("purchased_modes", setOf("classic"))?.toList() ?: listOf("classic")),
            "purchased_ranks" to (profilePrefs.getStringSet("purchased_ranks", setOf("BRONZE"))?.toList() ?: listOf("BRONZE")),
            "board_color_skin" to (tetrisPrefs.getString("board_color_skin", "cyberpunk") ?: "cyberpunk"),
            "block_style" to (tetrisPrefs.getString("block_style", "glass") ?: "glass"),
            "has_nickname_gradient" to profilePrefs.getBoolean("has_nickname_gradient", false),
            "bonus_xp" to profilePrefs.getInt("bonus_xp", 0),
            "prestige_level" to profilePrefs.getInt("prestige_level", 0),
            "custom_tag" to (profilePrefs.getString("custom_tag", "") ?: ""),
            "custom_tag_unlocked" to profilePrefs.getBoolean("custom_tag_unlocked", false),
            "crate_keys" to run {
                val map = mutableMapOf<String, Int>()
                val crates = listOf("wooden", "iron", "golden", "platinum", "legendary", "diamond", "red_crate_lite", "red_crate")
                for (c in crates) {
                    val k = tetrisPrefs.getInt("crate_key_$c", 0)
                    if (k > 0) map[c] = k
                }
                map
            },
            "unlocked_achievements" to unlockedAchievements,
            "last_synced_timestamp" to System.currentTimeMillis(),
            "is_online" to true,
            
            // Настройки UI/игры
            "setting_lang_code" to (tetrisPrefs.getString("lang_code", "en") ?: "en"),
            "setting_theme_color" to (tetrisPrefs.getString("theme_color", "indigo") ?: "indigo"),
            "setting_next_count" to tetrisPrefs.getInt("next_count", 3),
            "setting_ghost_visible" to tetrisPrefs.getBoolean("ghost_visible", true),
            "setting_control_style" to (tetrisPrefs.getString("control_style", "split") ?: "split"),
            "setting_sound_enabled" to tetrisPrefs.getBoolean("sound_enabled", true),
            "setting_vibration_enabled" to tetrisPrefs.getBoolean("vibration_enabled", true),
            "setting_smooth_falling_enabled" to tetrisPrefs.getBoolean("smooth_falling_enabled", true),
            "setting_grid_opacity" to tetrisPrefs.getFloat("grid_opacity", 0.6f),
            "setting_custom_start_level" to tetrisPrefs.getInt("custom_start_level", 1),
            "setting_game_speed_multiplier" to tetrisPrefs.getFloat("game_speed_multiplier", 1.0f),
            "setting_control_button_scale" to tetrisPrefs.getFloat("control_button_scale", 1.0f),
            "setting_control_button_alpha" to tetrisPrefs.getFloat("control_button_alpha", 1.0f),
            "setting_control_button_style" to (tetrisPrefs.getString("control_button_style", "neon") ?: "neon"),
            "setting_custom_font_key" to (tetrisPrefs.getString("custom_font_key", "default") ?: "default"),
            "setting_grid_line_density" to (tetrisPrefs.getString("grid_line_density", "standard") ?: "standard"),
            "setting_control_vertical_position" to (tetrisPrefs.getString("control_vertical_position", "bottom") ?: "bottom"),
            "setting_screen_shake_intensity" to tetrisPrefs.getFloat("screen_shake_intensity", 1.0f),
            "setting_scanlines_filter" to tetrisPrefs.getBoolean("scanlines_filter", false),
            "setting_graphics_quality" to (tetrisPrefs.getString("graphics_quality", "medium") ?: "medium"),
            "setting_sound_volume" to tetrisPrefs.getFloat("sound_volume", 1.0f),
            "setting_lobby_music_volume" to tetrisPrefs.getFloat("lobby_music_volume", 0.8f),
            "setting_lobby_music_enabled" to tetrisPrefs.getBoolean("lobby_music_enabled", true),
            "setting_relax_immortal" to tetrisPrefs.getBoolean("relax_immortal", true),
            "setting_relax_speed" to (tetrisPrefs.getString("relax_speed", "slow") ?: "slow"),
            "setting_relax_block_set" to (tetrisPrefs.getString("relax_block_set", "ideal") ?: "ideal"),
            "setting_relax_ghost_enabled" to tetrisPrefs.getBoolean("relax_ghost_enabled", true),

            // Игровая статистика
            "stats_games_played" to tetrisPrefs.getInt("stats_games_played", 0),
            "stats_spent_credits" to tetrisPrefs.getInt("stats_spent_credits", 0),
            "stats_cleared_lines" to tetrisPrefs.getInt("stats_cleared_lines", 0),
            "stats_high_score" to tetrisPrefs.getInt("stats_high_score", 0),
            "stats_max_speed_reached" to tetrisPrefs.getInt("stats_max_speed_reached", 0),
            "stats_tetrises_count" to tetrisPrefs.getInt("stats_tetrises_count", 0),
            "block_blast_high_score" to tetrisPrefs.getInt("block_blast_high_score", 0),
            "stats_avatar_changes" to tetrisPrefs.getInt("stats_avatar_changes", 0),
            "multiplayer_launches" to tetrisPrefs.getInt("multiplayer_launches", 0),
            "stats_pattern_level" to tetrisPrefs.getInt("stats_pattern_level", 1),
            "stats_pattern_score" to tetrisPrefs.getInt("stats_pattern_score", 0),
            "stats_sculptor_level" to tetrisPrefs.getInt("stats_sculptor_level", 1),
            "stats_sculptor_score" to tetrisPrefs.getInt("stats_sculptor_score", 0)
        )

        data["custom_avatar_base64"] = avatarBase64 ?: ""
        data["custom_background_base64"] = bgBase64 ?: ""
        if (user.uid == "ge9Lzx5EkCfbINDZEG6I8vYcJCd2" || user.email == "ezik02021@gmail.com" || user.email == "eziko04@gmail.com" || playerName == "FsFq") {
            data["is_admin"] = true
        }

        firestore.collection("users").document(user.uid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                android.util.Log.d("FirebaseSync", "pushUserData SUCCESS for ${user.uid}, credits: ${data["credits"]}")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FirebaseSync", "pushUserData FAILED for ${user.uid}: ${e.message}", e)
                continuation.resume(false)
            }
    }

    // Тянем данные из Firestore и пишем в SharedPrefs
    suspend fun pullUserData(context: Context): Map<String, Any>? = suspendCoroutine { continuation ->
        val user = auth.currentUser
        if (user == null) {
            continuation.resume(null)
            return@suspendCoroutine
        }
        val profilePrefs = context.getSharedPreferences("block_tetris_prefs", Context.MODE_PRIVATE)
        val tetrisPrefs = context.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)

        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    continuation.resume(null)
                    return@addOnSuccessListener
                }
                val data = snapshot.data
                if (data == null) {
                    continuation.resume(null)
                    return@addOnSuccessListener
                }

                val editorProfile = profilePrefs.edit()
                val editorTetris = tetrisPrefs.edit()

                (data["equipped_avatar_frame"] as? String)?.let { editorProfile.putString("equipped_avatar_frame", it) }
                val localFrames = profilePrefs.getStringSet("purchased_avatar_frames", setOf("standard")) ?: setOf("standard")
                val cloudFrames = (data["purchased_avatar_frames"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_avatar_frames", localFrames + cloudFrames)

                (data["equipped_title"] as? String)?.let { editorProfile.putString("equipped_title", it) }
                val localTitles = profilePrefs.getStringSet("purchased_titles", setOf("none")) ?: setOf("none")
                val cloudTitles = (data["purchased_titles"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_titles", localTitles + cloudTitles)

                (data["equipped_sound_pack"] as? String)?.let { editorProfile.putString("equipped_sound_pack", it) }
                val localSounds = profilePrefs.getStringSet("purchased_sound_packs", setOf("arcade")) ?: setOf("arcade")
                val cloudSounds = (data["purchased_sound_packs"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_sound_packs", localSounds + cloudSounds)

                val localThemes = profilePrefs.getStringSet("purchased_themes", setOf("indigo", "neon", "red")) ?: setOf("indigo", "neon", "red")
                val cloudThemes = (data["purchased_themes"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_themes", localThemes + cloudThemes)

                val localFonts = profilePrefs.getStringSet("purchased_fonts", setOf("default", "monospace")) ?: setOf("default", "monospace")
                val cloudFonts = (data["purchased_fonts"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_fonts", localFonts + cloudFonts)

                val localBtnStyles = profilePrefs.getStringSet("purchased_control_button_styles", setOf("classic", "neon")) ?: setOf("classic", "neon")
                val cloudBtnStyles = (data["purchased_control_button_styles"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_control_button_styles", localBtnStyles + cloudBtnStyles)

                val resolvedName = (data["player_name"] as? String) ?: tetrisPrefs.getString("player_name", "Player 1") ?: "Player 1"
                (data["player_name"] as? String)?.let { editorTetris.putString("player_name", it) }

                if (data.containsKey("custom_avatar_base64")) {
                    val base64 = data["custom_avatar_base64"] as? String
                    val file = java.io.File(context.filesDir, "custom_avatar_${resolvedName}.jpg")
                    if (base64 != null && base64.isNotEmpty()) {
                        base64ToFile(base64, file)
                        editorProfile.putBoolean("has_custom_avatar_${resolvedName}", true)
                    } else {
                        if (file.exists()) file.delete()
                        editorProfile.putBoolean("has_custom_avatar_${resolvedName}", false)
                    }
                }

                if (data.containsKey("custom_background_base64")) {
                    val base64 = data["custom_background_base64"] as? String
                    val file = java.io.File(context.filesDir, "custom_background_${resolvedName}.jpg")
                    if (base64 != null && base64.isNotEmpty()) {
                        base64ToFile(base64, file)
                        editorProfile.putBoolean("has_custom_background_${resolvedName}", true)
                    } else {
                        if (file.exists()) file.delete()
                        editorProfile.putBoolean("has_custom_background_${resolvedName}", false)
                    }
                }

                (data["custom_avatar_emoji"] as? String)?.let { editorProfile.putString("custom_avatar_emoji", it) }
                (data["custom_avatar_bg_color"] as? String)?.let { editorProfile.putString("custom_avatar_bg_color", it) }
                (data["online_tier"] as? String)?.let { 
                    editorProfile.putString("online_tier", it)
                    editorTetris.putString("online_tier", it)
                }

                val cloudCredits = (data["credits"] as? Number)?.toInt()
                if (cloudCredits != null) {
                    editorTetris.putInt("credits", cloudCredits)
                }

                val localCubeSkins = profilePrefs.getStringSet("purchased_cube_skins", setOf("neon")) ?: setOf("neon")
                val cloudCubeSkins = (data["purchased_cube_skins"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_cube_skins", localCubeSkins + cloudCubeSkins)

                val localInventory = profilePrefs.getStringSet("case_inventory", emptySet()) ?: emptySet()
                val cloudInventory = (data["case_inventory"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("case_inventory", localInventory + cloudInventory)

                val localModes = profilePrefs.getStringSet("purchased_modes", setOf("classic")) ?: setOf("classic")
                val cloudModes = (data["purchased_modes"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_modes", localModes + cloudModes)

                val localRanks = profilePrefs.getStringSet("purchased_ranks", setOf("BRONZE")) ?: setOf("BRONZE")
                val cloudRanks = (data["purchased_ranks"] as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                editorProfile.putStringSet("purchased_ranks", localRanks + cloudRanks)

                (data["board_color_skin"] as? String)?.let { editorTetris.putString("board_color_skin", it) }
                (data["block_style"] as? String)?.let { editorTetris.putString("block_style", it) }
                val hasGradCloud = data["has_nickname_gradient"] as? Boolean ?: false
                val hasGradLocal = profilePrefs.getBoolean("has_nickname_gradient", false)
                editorProfile.putBoolean("has_nickname_gradient", hasGradCloud || hasGradLocal)

                val cloudXp = (data["bonus_xp"] as? Number)?.toInt()
                if (cloudXp != null) {
                    editorProfile.putInt("bonus_xp", cloudXp)
                }
                val cloudPrestige = (data["prestige_level"] as? Number)?.toInt()
                if (cloudPrestige != null) {
                    editorProfile.putInt("prestige_level", cloudPrestige)
                }

                (data["custom_tag"] as? String)?.let { editorProfile.putString("custom_tag", it) }
                val tagUnlockedCloud = data["custom_tag_unlocked"] as? Boolean ?: false
                val tagUnlockedLocal = profilePrefs.getBoolean("custom_tag_unlocked", false)
                editorProfile.putBoolean("custom_tag_unlocked", tagUnlockedCloud || tagUnlockedLocal)

                // Ключи к кейсам
                (data["crate_keys"] as? Map<*, *>)?.forEach { (k, v) ->
                    val crateId = k as? String ?: return@forEach
                    val count = (v as? Number)?.toInt() ?: return@forEach
                    val localCount = tetrisPrefs.getInt("crate_key_$crateId", 0)
                    editorTetris.putInt("crate_key_$crateId", maxOf(localCount, count))
                }
                
                // Достижения
                (data["unlocked_achievements"] as? List<*>)?.mapNotNull { it as? String }?.forEach { achId ->
                    editorTetris.putBoolean("ach_${achId}_unlocked", true)
                }

                // Настройки с облака
                (data["setting_lang_code"] as? String)?.let { editorTetris.putString("lang_code", it) }
                (data["setting_theme_color"] as? String)?.let { editorTetris.putString("theme_color", it) }
                (data["setting_next_count"] as? Number)?.let { editorTetris.putInt("next_count", it.toInt()) }
                (data["setting_ghost_visible"] as? Boolean)?.let { editorTetris.putBoolean("ghost_visible", it) }
                (data["setting_control_style"] as? String)?.let { editorTetris.putString("control_style", it) }
                (data["setting_sound_enabled"] as? Boolean)?.let { editorTetris.putBoolean("sound_enabled", it) }
                (data["setting_vibration_enabled"] as? Boolean)?.let { editorTetris.putBoolean("vibration_enabled", it) }
                (data["setting_smooth_falling_enabled"] as? Boolean)?.let { editorTetris.putBoolean("smooth_falling_enabled", it) }
                (data["setting_grid_opacity"] as? Number)?.let { editorTetris.putFloat("grid_opacity", it.toFloat()) }
                (data["setting_custom_start_level"] as? Number)?.let { editorTetris.putInt("custom_start_level", it.toInt()) }
                (data["setting_game_speed_multiplier"] as? Number)?.let { editorTetris.putFloat("game_speed_multiplier", it.toFloat()) }
                (data["setting_control_button_scale"] as? Number)?.let { editorTetris.putFloat("control_button_scale", it.toFloat()) }
                (data["setting_control_button_alpha"] as? Number)?.let { editorTetris.putFloat("control_button_alpha", it.toFloat()) }
                (data["setting_control_button_style"] as? String)?.let { editorTetris.putString("control_button_style", it) }
                (data["setting_custom_font_key"] as? String)?.let { editorTetris.putString("custom_font_key", it) }
                (data["setting_grid_line_density"] as? String)?.let { editorTetris.putString("grid_line_density", it) }
                (data["setting_control_vertical_position"] as? String)?.let { editorTetris.putString("control_vertical_position", it) }
                (data["setting_screen_shake_intensity"] as? Number)?.let { editorTetris.putFloat("screen_shake_intensity", it.toFloat()) }
                (data["setting_scanlines_filter"] as? Boolean)?.let { editorTetris.putBoolean("scanlines_filter", it) }
                (data["setting_graphics_quality"] as? String)?.let { editorTetris.putString("graphics_quality", it) }
                (data["setting_sound_volume"] as? Number)?.let { editorTetris.putFloat("sound_volume", it.toFloat()) }
                (data["setting_lobby_music_volume"] as? Number)?.let { editorTetris.putFloat("lobby_music_volume", it.toFloat()) }
                (data["setting_lobby_music_enabled"] as? Boolean)?.let { editorTetris.putBoolean("lobby_music_enabled", it) }
                (data["setting_relax_immortal"] as? Boolean)?.let { editorTetris.putBoolean("relax_immortal", it) }
                (data["setting_relax_speed"] as? String)?.let { editorTetris.putString("relax_speed", it) }
                (data["setting_relax_block_set"] as? String)?.let { editorTetris.putString("relax_block_set", it) }
                (data["setting_relax_ghost_enabled"] as? Boolean)?.let { editorTetris.putBoolean("relax_ghost_enabled", it) }

                // Статистика с облака (smart max merge)
                val localGames = tetrisPrefs.getInt("stats_games_played", 0)
                val cloudGames = (data["stats_games_played"] as? Number)?.toInt() ?: localGames
                editorTetris.putInt("stats_games_played", maxOf(localGames, cloudGames))

                val localSpent = tetrisPrefs.getInt("stats_spent_credits", 0)
                val cloudSpent = (data["stats_spent_credits"] as? Number)?.toInt() ?: localSpent
                editorTetris.putInt("stats_spent_credits", maxOf(localSpent, cloudSpent))

                val localLines = tetrisPrefs.getInt("stats_cleared_lines", 0)
                val cloudLines = (data["stats_cleared_lines"] as? Number)?.toInt() ?: localLines
                editorTetris.putInt("stats_cleared_lines", maxOf(localLines, cloudLines))

                val localHigh = tetrisPrefs.getInt("stats_high_score", 0)
                val cloudHigh = (data["stats_high_score"] as? Number)?.toInt() ?: localHigh
                editorTetris.putInt("stats_high_score", maxOf(localHigh, cloudHigh))

                val localSpeed = tetrisPrefs.getInt("stats_max_speed_reached", 0)
                val cloudSpeed = (data["stats_max_speed_reached"] as? Number)?.toInt() ?: localSpeed
                editorTetris.putInt("stats_max_speed_reached", maxOf(localSpeed, cloudSpeed))

                val localTetrises = tetrisPrefs.getInt("stats_tetrises_count", 0)
                val cloudTetrises = (data["stats_tetrises_count"] as? Number)?.toInt() ?: localTetrises
                editorTetris.putInt("stats_tetrises_count", maxOf(localTetrises, cloudTetrises))

                val localBlockBlast = tetrisPrefs.getInt("block_blast_high_score", 0)
                val cloudBlockBlast = (data["block_blast_high_score"] as? Number)?.toInt() ?: localBlockBlast
                editorTetris.putInt("block_blast_high_score", maxOf(localBlockBlast, cloudBlockBlast))

                val localAvatarChanges = tetrisPrefs.getInt("stats_avatar_changes", 0)
                val cloudAvatarChanges = (data["stats_avatar_changes"] as? Number)?.toInt() ?: localAvatarChanges
                editorTetris.putInt("stats_avatar_changes", maxOf(localAvatarChanges, cloudAvatarChanges))

                val localMp = tetrisPrefs.getInt("multiplayer_launches", 0)
                val cloudMp = (data["multiplayer_launches"] as? Number)?.toInt() ?: localMp
                editorTetris.putInt("multiplayer_launches", maxOf(localMp, cloudMp))

                val localPatternLevel = tetrisPrefs.getInt("stats_pattern_level", 1)
                val cloudPatternLevel = (data["stats_pattern_level"] as? Number)?.toInt() ?: localPatternLevel
                editorTetris.putInt("stats_pattern_level", maxOf(localPatternLevel, cloudPatternLevel))

                val localPatternScore = tetrisPrefs.getInt("stats_pattern_score", 0)
                val cloudPatternScore = (data["stats_pattern_score"] as? Number)?.toInt() ?: localPatternScore
                editorTetris.putInt("stats_pattern_score", maxOf(localPatternScore, cloudPatternScore))

                val localSculptorLevel = tetrisPrefs.getInt("stats_sculptor_level", 1)
                val cloudSculptorLevel = (data["stats_sculptor_level"] as? Number)?.toInt() ?: localSculptorLevel
                editorTetris.putInt("stats_sculptor_level", maxOf(localSculptorLevel, cloudSculptorLevel))

                val localSculptorScore = tetrisPrefs.getInt("stats_sculptor_score", 0)
                val cloudSculptorScore = (data["stats_sculptor_score"] as? Number)?.toInt() ?: localSculptorScore
                editorTetris.putInt("stats_sculptor_score", maxOf(localSculptorScore, cloudSculptorScore))

                editorProfile.apply()
                editorTetris.apply()
                continuation.resume(data)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    // Atomic cloud credits update via FieldValue.increment to prevent race conditions
    suspend fun adjustCloudCredits(delta: Long): Boolean = suspendCoroutine { continuation ->
        val user = auth.currentUser
        if (user == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }
        firestore.collection("users").document(user.uid)
            .update("credits", com.google.firebase.firestore.FieldValue.increment(delta))
            .addOnSuccessListener {
                android.util.Log.d("FirebaseSync", "adjustCloudCredits SUCCESS delta: $delta")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FirebaseSync", "adjustCloudCredits FAILED delta: $delta: ${e.message}", e)
                continuation.resume(false)
            }
    }
}
