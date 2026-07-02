package com.example.db

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FirebaseSync {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private fun fileToBase64(file: java.io.File): String? {
        if (!file.exists()) return null
        return try {
            val bytes = file.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun base64ToFile(base64Str: String, file: java.io.File) {
        try {
            val bytes = android.util.Base64.decode(base64Str, android.util.Base64.NO_WRAP)
            file.writeBytes(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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

        val data = mapOf(
            "player_name" to playerName,
            "equipped_avatar_frame" to profilePrefs.getString("equipped_avatar_frame", "standard"),
            "purchased_avatar_frames" to profilePrefs.getStringSet("purchased_avatar_frames", setOf("standard"))?.toList(),
            "equipped_title" to profilePrefs.getString("equipped_title", "none"),
            "purchased_titles" to profilePrefs.getStringSet("purchased_titles", setOf("none"))?.toList(),
            "equipped_sound_pack" to profilePrefs.getString("equipped_sound_pack", "arcade"),
            "purchased_sound_packs" to profilePrefs.getStringSet("purchased_sound_packs", setOf("arcade"))?.toList(),
            "purchased_themes" to profilePrefs.getStringSet("purchased_themes", setOf("indigo", "neon", "red"))?.toList(),
            "purchased_fonts" to profilePrefs.getStringSet("purchased_fonts", setOf("default", "monospace"))?.toList(),
            "purchased_control_button_styles" to profilePrefs.getStringSet("purchased_control_button_styles", setOf("classic", "neon"))?.toList(),
            "custom_avatar_emoji" to profilePrefs.getString("custom_avatar_emoji", ""),
            "custom_avatar_bg_color" to profilePrefs.getString("custom_avatar_bg_color", "3A3C44"),
            "online_tier" to profilePrefs.getString("online_tier", "BRONZE"),
            "credits" to tetrisPrefs.getInt("credits", 750),
            "purchased_cube_skins" to profilePrefs.getStringSet("purchased_cube_skins", setOf("neon"))?.toList(),
            "case_inventory" to profilePrefs.getStringSet("case_inventory", emptySet())?.toList(),
            "purchased_modes" to profilePrefs.getStringSet("purchased_modes", setOf("classic", "extended", "fast_run", "reverse", "block_blast"))?.toList(),
            "board_color_skin" to tetrisPrefs.getString("board_color_skin", "cyberpunk"),
            "block_style" to tetrisPrefs.getString("block_style", "glass"),
            "custom_avatar_base64" to avatarBase64,
            "custom_background_base64" to bgBase64,
            "has_nickname_gradient" to profilePrefs.getBoolean("has_nickname_gradient", false),
            "bonus_xp" to profilePrefs.getInt("bonus_xp", 0),
            "custom_tag" to profilePrefs.getString("custom_tag", ""),
            "custom_tag_unlocked" to profilePrefs.getBoolean("custom_tag_unlocked", false),
            
            // Settings Sync
            "setting_lang_code" to tetrisPrefs.getString("lang_code", "en"),
            "setting_theme_color" to tetrisPrefs.getString("theme_color", "indigo"),
            "setting_next_count" to tetrisPrefs.getInt("next_count", 3),
            "setting_ghost_visible" to tetrisPrefs.getBoolean("ghost_visible", true),
            "setting_control_style" to tetrisPrefs.getString("control_style", "split"),
            "setting_sound_enabled" to tetrisPrefs.getBoolean("sound_enabled", true),
            "setting_vibration_enabled" to tetrisPrefs.getBoolean("vibration_enabled", true),

            // Statistics
            "stats_games_played" to tetrisPrefs.getInt("stats_games_played", 0),
            "stats_spent_credits" to tetrisPrefs.getInt("stats_spent_credits", 0),
            "stats_cleared_lines" to tetrisPrefs.getInt("stats_cleared_lines", 0),
            "stats_high_score" to tetrisPrefs.getInt("stats_high_score", 0),
            "stats_max_speed_reached" to tetrisPrefs.getInt("stats_max_speed_reached", 0),
            "stats_tetrises_count" to tetrisPrefs.getInt("stats_tetrises_count", 0),
            "block_blast_high_score" to tetrisPrefs.getInt("block_blast_high_score", 0),
            "stats_avatar_changes" to tetrisPrefs.getInt("stats_avatar_changes", 0),
            "multiplayer_launches" to tetrisPrefs.getInt("multiplayer_launches", 0)
        )

        firestore.collection("users").document(user.uid).set(data)
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resume(false)
            }
    }

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
                (data["purchased_avatar_frames"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_avatar_frames", it.toSet()) }
                (data["equipped_title"] as? String)?.let { editorProfile.putString("equipped_title", it) }
                (data["purchased_titles"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_titles", it.toSet()) }
                (data["equipped_sound_pack"] as? String)?.let { editorProfile.putString("equipped_sound_pack", it) }
                (data["purchased_sound_packs"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_sound_packs", it.toSet()) }
                (data["purchased_themes"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_themes", it.toSet()) }
                (data["purchased_fonts"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_fonts", it.toSet()) }
                (data["purchased_control_button_styles"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_control_button_styles", it.toSet()) }
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
                (data["credits"] as? Long)?.let { editorTetris.putInt("credits", it.toInt()) }
                (data["purchased_cube_skins"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_cube_skins", it.toSet()) }
                (data["case_inventory"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("case_inventory", it.toSet()) }
                (data["purchased_modes"] as? List<*>)?.mapNotNull { it as? String }?.let { editorProfile.putStringSet("purchased_modes", it.toSet()) }
                (data["board_color_skin"] as? String)?.let { editorTetris.putString("board_color_skin", it) }
                (data["block_style"] as? String)?.let { editorTetris.putString("block_style", it) }
                (data["has_nickname_gradient"] as? Boolean)?.let { editorProfile.putBoolean("has_nickname_gradient", it) }
                (data["bonus_xp"] as? Long)?.let { editorProfile.putInt("bonus_xp", it.toInt()) }
                (data["custom_tag"] as? String)?.let { editorProfile.putString("custom_tag", it) }
                (data["custom_tag_unlocked"] as? Boolean)?.let { editorProfile.putBoolean("custom_tag_unlocked", it) }
                
                // Settings Sync
                (data["setting_lang_code"] as? String)?.let { editorTetris.putString("lang_code", it) }
                (data["setting_theme_color"] as? String)?.let { editorTetris.putString("theme_color", it) }
                (data["setting_next_count"] as? Long)?.let { editorTetris.putInt("next_count", it.toInt()) }
                (data["setting_ghost_visible"] as? Boolean)?.let { editorTetris.putBoolean("ghost_visible", it) }
                (data["setting_control_style"] as? String)?.let { editorTetris.putString("control_style", it) }
                (data["setting_sound_enabled"] as? Boolean)?.let { editorTetris.putBoolean("sound_enabled", it) }
                (data["setting_vibration_enabled"] as? Boolean)?.let { editorTetris.putBoolean("vibration_enabled", it) }

                // Statistics
                (data["stats_games_played"] as? Long)?.let { editorTetris.putInt("stats_games_played", it.toInt()) }
                (data["stats_spent_credits"] as? Long)?.let { editorTetris.putInt("stats_spent_credits", it.toInt()) }
                (data["stats_cleared_lines"] as? Long)?.let { editorTetris.putInt("stats_cleared_lines", it.toInt()) }
                (data["stats_high_score"] as? Long)?.let { editorTetris.putInt("stats_high_score", it.toInt()) }
                (data["stats_max_speed_reached"] as? Long)?.let { editorTetris.putInt("stats_max_speed_reached", it.toInt()) }
                (data["stats_tetrises_count"] as? Long)?.let { editorTetris.putInt("stats_tetrises_count", it.toInt()) }
                (data["block_blast_high_score"] as? Long)?.let { editorTetris.putInt("block_blast_high_score", it.toInt()) }
                (data["stats_avatar_changes"] as? Long)?.let { editorTetris.putInt("stats_avatar_changes", it.toInt()) }
                (data["multiplayer_launches"] as? Long)?.let { editorTetris.putInt("multiplayer_launches", it.toInt()) }

                editorProfile.apply()
                editorTetris.apply()
                continuation.resume(data)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}
