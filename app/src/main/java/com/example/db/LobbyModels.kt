package com.example.db

data class LobbyRoom(
    val roomId: String = "",
    val name: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostTier: String = "BRONZE",
    val password: String = "",
    val passwordHash: String = "",
    val isLocked: Boolean = false,
    val status: String = "waiting", // "waiting", "playing", "finished"
    val players: List<RoomPlayer> = emptyList(),
    val createdAt: Long = 0L,
    val gameMode: String = "CLASSIC", // "CLASSIC", "SCORE_RACE", "SPRINT", "BLITZ", "HYPER"
    val garbageIntensity: Float = 1.0f,
    val roundTarget: Int = 1,
    val hostWins: Int = 0,
    val opponentWins: Int = 0,
    val currentRound: Int = 1,
    val hostGrid: List<Int> = List(200) { 0 },
    val opponentGrid: List<Int> = List(200) { 0 },
    val hostScore: Int = 0,
    val opponentScore: Int = 0,
    val hostCombo: Int = 0,
    val opponentCombo: Int = 0,
    val hostGameOver: Boolean = false,
    val opponentGameOver: Boolean = false,
    val hostGarbageToSend: Int = 0,
    val opponentGarbageToSend: Int = 0,
    val winnerId: String = "",
    val betAmount: Int = 0
) {
    fun toMap(): Map<String, Any> {
        val playersMap = players.associate { it.uid to it.toMap() }
        val resolvedHash = passwordHash.ifEmpty {
            if (password.isNotBlank()) PasswordHasher.hash(password) else ""
        }
        return mapOf(
            "roomId" to roomId,
            "name" to name,
            "hostId" to hostId,
            "hostName" to hostName,
            "hostTier" to hostTier,
            "password" to "", // Never store plaintext password in RTDB
            "passwordHash" to resolvedHash,
            "isLocked" to (isLocked || resolvedHash.isNotEmpty()),
            "status" to status,
            "players" to playersMap,
            "createdAt" to createdAt,
            "gameMode" to gameMode,
            "garbageIntensity" to garbageIntensity.toDouble(),
            "roundTarget" to roundTarget,
            "hostWins" to hostWins,
            "opponentWins" to opponentWins,
            "currentRound" to currentRound,
            "hostGrid" to hostGrid,
            "opponentGrid" to opponentGrid,
            "hostScore" to hostScore,
            "opponentScore" to opponentScore,
            "hostCombo" to hostCombo,
            "opponentCombo" to opponentCombo,
            "hostGameOver" to hostGameOver,
            "opponentGameOver" to opponentGameOver,
            "hostGarbageToSend" to hostGarbageToSend,
            "opponentGarbageToSend" to opponentGarbageToSend,
            "winnerId" to winnerId,
            "betAmount" to betAmount
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): LobbyRoom {
            val playersList = when (val p = map["players"]) {
                is Map<*, *> -> p.values.mapNotNull { item ->
                    (item as? Map<*, *>)?.let { RoomPlayer.fromMap(it.mapKeys { e -> e.key.toString() } as Map<String, Any>) }
                }
                is List<*> -> p.mapNotNull { item ->
                    (item as? Map<*, *>)?.let { RoomPlayer.fromMap(it.mapKeys { e -> e.key.toString() } as Map<String, Any>) }
                }
                else -> emptyList()
            }
            return LobbyRoom(
                roomId = map["roomId"] as? String ?: "",
                name = map["name"] as? String ?: "",
                hostId = map["hostId"] as? String ?: "",
                hostName = map["hostName"] as? String ?: "",
                hostTier = map["hostTier"] as? String ?: "BRONZE",
                password = map["password"] as? String ?: "",
                passwordHash = map["passwordHash"] as? String ?: "",
                isLocked = (map["isLocked"] as? Boolean) ?: ((map["passwordHash"] as? String)?.isNotEmpty() == true) ?: false,
                status = map["status"] as? String ?: "waiting",
                players = playersList,
                createdAt = (map["createdAt"] as? Long) ?: (map["createdAt"] as? Number)?.toLong() ?: 0L,
                gameMode = map["gameMode"] as? String ?: "CLASSIC",
                garbageIntensity = ((map["garbageIntensity"] as? Number)?.toFloat()) ?: 1.0f,
                roundTarget = (map["roundTarget"] as? Number)?.toInt() ?: 1,
                hostWins = (map["hostWins"] as? Number)?.toInt() ?: 0,
                opponentWins = (map["opponentWins"] as? Number)?.toInt() ?: 0,
                currentRound = (map["currentRound"] as? Number)?.toInt() ?: 1,
                hostGrid = (map["hostGrid"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: List(200) { 0 },
                opponentGrid = (map["opponentGrid"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: List(200) { 0 },
                hostScore = (map["hostScore"] as? Number)?.toInt() ?: 0,
                opponentScore = (map["opponentScore"] as? Number)?.toInt() ?: 0,
                hostCombo = (map["hostCombo"] as? Number)?.toInt() ?: 0,
                opponentCombo = (map["opponentCombo"] as? Number)?.toInt() ?: 0,
                hostGameOver = map["hostGameOver"] as? Boolean ?: false,
                opponentGameOver = map["opponentGameOver"] as? Boolean ?: false,
                hostGarbageToSend = (map["hostGarbageToSend"] as? Number)?.toInt() ?: 0,
                opponentGarbageToSend = (map["opponentGarbageToSend"] as? Number)?.toInt() ?: 0,
                winnerId = map["winnerId"] as? String ?: "",
                betAmount = (map["betAmount"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

data class RoomPlayer(
    val uid: String = "",
    val name: String = "",
    val tier: String = "BRONZE",
    val isReady: Boolean = false,
    val hasGradient: Boolean = false,
    val avatarEmoji: String = "",
    val avatarBgColor: String = "",
    val avatarFrame: String = "standard",
    val winStreak: Int = 0,
    val rating: Int = 1000,
    val customTag: String = "",
    val avatarBase64: String = "",
    val credits: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "tier" to tier,
            "isReady" to isReady,
            "hasGradient" to hasGradient,
            "avatarEmoji" to avatarEmoji,
            "avatarBgColor" to avatarBgColor,
            "avatarFrame" to avatarFrame,
            "winStreak" to winStreak,
            "rating" to rating,
            "customTag" to customTag,
            "avatarBase64" to avatarBase64,
            "credits" to credits
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): RoomPlayer {
            return RoomPlayer(
                uid = map["uid"] as? String ?: "",
                name = map["name"] as? String ?: "",
                tier = map["tier"] as? String ?: "BRONZE",
                isReady = map["isReady"] as? Boolean ?: false,
                hasGradient = (map["hasGradient"] as? Boolean) ?: (map["hasNicknameGradient"] as? Boolean) ?: false,
                avatarEmoji = (map["avatarEmoji"] as? String) ?: (map["customAvatarEmoji"] as? String) ?: (map["custom_avatar_emoji"] as? String) ?: "",
                avatarBgColor = (map["avatarBgColor"] as? String) ?: (map["customAvatarBgColor"] as? String) ?: (map["custom_avatar_bg_color"] as? String) ?: "",
                avatarFrame = (map["avatarFrame"] as? String) ?: (map["equippedAvatarFrame"] as? String) ?: (map["equipped_avatar_frame"] as? String) ?: "standard",
                winStreak = (map["winStreak"] as? Number)?.toInt() ?: 0,
                rating = (map["rating"] as? Number)?.toInt() ?: 1000,
                customTag = map["customTag"] as? String ?: "",
                avatarBase64 = (map["avatarBase64"] as? String) ?: (map["custom_avatar_base64"] as? String) ?: "",
                credits = (map["credits"] as? Number)?.toInt() ?: (map["balance"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val hasGradient: Boolean = false,
    val senderTier: String = "BRONZE",
    val senderAvatarEmoji: String = "",
    val senderAvatarBgColor: String = "",
    val senderAvatarFrame: String = "standard",
    val senderAvatarBase64: String = "",
    val replyToSender: String = "",
    val replyToText: String = "",
    val reactions: Map<String, List<String>> = emptyMap() // emoji -> list of UIDs
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to text,
            "timestamp" to timestamp,
            "hasGradient" to hasGradient,
            "senderTier" to senderTier,
            "senderAvatarEmoji" to senderAvatarEmoji,
            "senderAvatarBgColor" to senderAvatarBgColor,
            "senderAvatarFrame" to senderAvatarFrame,
            "senderAvatarBase64" to senderAvatarBase64,
            "replyToSender" to replyToSender,
            "replyToText" to replyToText,
            "reactions" to reactions
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): ChatMessage {
            val rawReactions = map["reactions"] as? Map<*, *>
            val parsedReactions = rawReactions?.mapNotNull { (k, v) ->
                val emojiKey = k?.toString() ?: return@mapNotNull null
                val uids = when (v) {
                    is List<*> -> v.mapNotNull { it?.toString() }
                    is Map<*, *> -> v.values.mapNotNull { it?.toString() }
                    else -> emptyList()
                }
                emojiKey to uids
            }?.toMap() ?: emptyMap()

            return ChatMessage(
                id = map["id"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                text = map["text"] as? String ?: "",
                timestamp = (map["timestamp"] as? Long) ?: (map["timestamp"] as? Number)?.toLong() ?: 0L,
                hasGradient = map["hasGradient"] as? Boolean ?: false,
                senderTier = map["senderTier"] as? String ?: "BRONZE",
                senderAvatarEmoji = (map["senderAvatarEmoji"] as? String) ?: (map["custom_avatar_emoji"] as? String) ?: "",
                senderAvatarBgColor = (map["senderAvatarBgColor"] as? String) ?: (map["custom_avatar_bg_color"] as? String) ?: "",
                senderAvatarFrame = (map["senderAvatarFrame"] as? String) ?: (map["equipped_avatar_frame"] as? String) ?: "standard",
                senderAvatarBase64 = (map["senderAvatarBase64"] as? String) ?: (map["custom_avatar_base64"] as? String) ?: "",
                replyToSender = map["replyToSender"] as? String ?: "",
                replyToText = map["replyToText"] as? String ?: "",
                reactions = parsedReactions
            )
        }
    }
}

data class FriendUser(
    val uid: String = "",
    val username: String = "",
    val onlineTier: String = "BRONZE",
    val avatarEmoji: String = "",
    val avatarBgColor: String = "",
    val avatarFrame: String = "standard",
    val avatarBase64: String = "",
    val hasGradient: Boolean = false,
    val isOnline: Boolean = false,
    val status: String = "accepted", // "accepted", "pending_incoming", "pending_outgoing"
    val lastSeen: Long = 0L,
    val customTag: String = "",
    val credits: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "onlineTier" to onlineTier,
            "avatarEmoji" to avatarEmoji,
            "avatarBgColor" to avatarBgColor,
            "avatarFrame" to avatarFrame,
            "avatarBase64" to avatarBase64,
            "hasGradient" to hasGradient,
            "isOnline" to isOnline,
            "status" to status,
            "lastSeen" to lastSeen,
            "customTag" to customTag,
            "credits" to credits
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): FriendUser {
            val rawLastSeen = (map["lastSeen"] as? Number)?.toLong()
                ?: (map["last_synced_timestamp"] as? Number)?.toLong()
                ?: (map["lastActive"] as? Number)?.toLong()
                ?: 0L
            val isRecentlyActive = (System.currentTimeMillis() - rawLastSeen) < 120_000L
            val rawOnline = (map["isOnline"] as? Boolean) ?: (map["is_online"] as? Boolean) ?: false
            val computedOnline = rawOnline && (rawLastSeen == 0L || isRecentlyActive)
            val credits = (map["credits"] as? Number)?.toInt()
                ?: (map["balance"] as? Number)?.toInt()
                ?: (map["coins"] as? Number)?.toInt()
                ?: 0

            return FriendUser(
                uid = map["uid"] as? String ?: "",
                username = (map["username"] as? String) ?: (map["playerName"] as? String) ?: (map["player_name"] as? String) ?: "",
                onlineTier = (map["onlineTier"] as? String) ?: (map["rank"] as? String) ?: (map["online_tier"] as? String) ?: "BRONZE",
                avatarEmoji = (map["avatarEmoji"] as? String) ?: (map["custom_avatar_emoji"] as? String) ?: (map["avatar_emoji"] as? String) ?: "",
                avatarBgColor = (map["avatarBgColor"] as? String) ?: (map["custom_avatar_bg_color"] as? String) ?: (map["avatar_bg_color"] as? String) ?: "",
                avatarFrame = (map["avatarFrame"] as? String) ?: (map["equipped_avatar_frame"] as? String) ?: (map["avatar_frame"] as? String) ?: "standard",
                avatarBase64 = (map["avatarBase64"] as? String) ?: (map["custom_avatar_base64"] as? String) ?: (map["avatar_base64"] as? String) ?: "",
                hasGradient = (map["hasGradient"] as? Boolean) ?: (map["has_nickname_gradient"] as? Boolean) ?: false,
                isOnline = computedOnline,
                status = map["status"] as? String ?: "accepted",
                lastSeen = rawLastSeen,
                customTag = (map["customTag"] as? String) ?: (map["custom_tag"] as? String) ?: "",
                credits = credits
            )
        }
    }
}

data class PublicUserProfile(
    val uid: String = "",
    val username: String = "",
    val onlineTier: String = "BRONZE",
    val avatarEmoji: String = "",
    val avatarBgColor: String = "",
    val avatarFrame: String = "standard",
    val avatarBase64: String = "",
    val customBackgroundBase64: String = "",
    val hasGradient: Boolean = false,
    val highScore: Int = 0,
    val userLevel: Int = 1,
    val title: String = "",
    val isOnline: Boolean = false,
    val createdAt: Long = 0L,
    val credits: Int = 0,
    val rating: Int = 1000,
    val winStreak: Int = 0,
    val gamesPlayed: Int = 0,
    val clearedLines: Int = 0,
    val tetrisesCount: Int = 0,
    val maxSpeedReached: Int = 0,
    val prestigeLevel: Int = 0,
    val customTag: String = "",
    val themeColor: String = "indigo",
    val boardSkin: String = "cyberpunk",
    val blockStyle: String = "material",
    val unlockedAchievements: List<String> = emptyList()
) {
    companion object {
        fun fromMap(uid: String, map: Map<String, Any>): PublicUserProfile {
            val rawLastSeen = (map["last_synced_timestamp"] as? Number)?.toLong()
                ?: (map["lastActive"] as? Number)?.toLong()
                ?: (map["lastSeen"] as? Number)?.toLong()
                ?: (map["creationTime"] as? Number)?.toLong()
                ?: 0L
            val isRecentlyActive = (System.currentTimeMillis() - rawLastSeen) < 120_000L
            val rawOnline = (map["isOnline"] as? Boolean) ?: (map["is_online"] as? Boolean) ?: false
            val computedOnline = rawOnline && (rawLastSeen == 0L || isRecentlyActive)
            val credits = (map["credits"] as? Number)?.toInt()
                ?: (map["balance"] as? Number)?.toInt()
                ?: (map["coins"] as? Number)?.toInt()
                ?: (map["user_credits"] as? Number)?.toInt()
                ?: 0

            val achievementsList = (map["unlocked_achievements"] as? List<*>)?.mapNotNull { it as? String }
                ?: (map["achievements"] as? List<*>)?.mapNotNull { it as? String }
                ?: emptyList()

            return PublicUserProfile(
                uid = uid,
                username = (map["username"] as? String)
                    ?: (map["playerName"] as? String)
                    ?: (map["name"] as? String)
                    ?: "Игрок",
                onlineTier = (map["onlineTier"] as? String)
                    ?: (map["rank"] as? String)
                    ?: (map["tier"] as? String)
                    ?: "BRONZE",
                avatarEmoji = (map["custom_avatar_emoji"] as? String)
                    ?: (map["avatarEmoji"] as? String)
                    ?: (map["customAvatarEmoji"] as? String)
                    ?: (map["emoji"] as? String)
                    ?: "",
                avatarBgColor = (map["custom_avatar_bg_color"] as? String)
                    ?: (map["avatarBgColor"] as? String)
                    ?: (map["customAvatarBgColor"] as? String)
                    ?: (map["bgColor"] as? String)
                    ?: "",
                avatarFrame = (map["equipped_avatar_frame"] as? String)
                    ?: (map["avatarFrame"] as? String)
                    ?: (map["equippedAvatarFrame"] as? String)
                    ?: (map["frame"] as? String)
                    ?: "standard",
                avatarBase64 = (map["custom_avatar_base64"] as? String)
                    ?: (map["avatarBase64"] as? String)
                    ?: "",
                customBackgroundBase64 = (map["custom_background_base64"] as? String)
                    ?: (map["customBackgroundBase64"] as? String)
                    ?: (map["backgroundBase64"] as? String)
                    ?: "",
                hasGradient = (map["has_nickname_gradient"] as? Boolean)
                    ?: (map["hasGradient"] as? Boolean)
                    ?: (map["hasNicknameGradient"] as? Boolean)
                    ?: false,
                highScore = (map["highScore"] as? Number)?.toInt()
                    ?: (map["score"] as? Number)?.toInt()
                    ?: (map["stats_high_score"] as? Number)?.toInt()
                    ?: 0,
                userLevel = (map["user_level"] as? Number)?.toInt()
                    ?: (map["userLevel"] as? Number)?.toInt()
                    ?: 1,
                title = (map["equipped_title"] as? String)
                    ?: (map["title"] as? String)
                    ?: (map["equippedTitle"] as? String)
                    ?: "",
                isOnline = computedOnline,
                createdAt = (map["creationTime"] as? Number)?.toLong()
                    ?: (map["createdAt"] as? Number)?.toLong()
                    ?: (map["timestamp"] as? Number)?.toLong()
                    ?: 0L,
                credits = credits,
                rating = (map["online_rating"] as? Number)?.toInt()
                    ?: (map["rating"] as? Number)?.toInt()
                    ?: (map["elo"] as? Number)?.toInt()
                    ?: 1000,
                winStreak = (map["win_streak"] as? Number)?.toInt()
                    ?: (map["winStreak"] as? Number)?.toInt()
                    ?: 0,
                gamesPlayed = (map["stats_games_played"] as? Number)?.toInt()
                    ?: (map["gamesPlayed"] as? Number)?.toInt()
                    ?: 0,
                clearedLines = (map["stats_cleared_lines"] as? Number)?.toInt()
                    ?: (map["clearedLines"] as? Number)?.toInt()
                    ?: 0,
                tetrisesCount = (map["stats_tetrises_count"] as? Number)?.toInt()
                    ?: (map["tetrisesCount"] as? Number)?.toInt()
                    ?: 0,
                maxSpeedReached = (map["stats_max_speed_reached"] as? Number)?.toInt()
                    ?: (map["maxSpeed"] as? Number)?.toInt()
                    ?: 0,
                prestigeLevel = (map["prestige_level"] as? Number)?.toInt()
                    ?: (map["prestige"] as? Number)?.toInt()
                    ?: 0,
                customTag = (map["custom_tag"] as? String)
                    ?: (map["tag"] as? String)
                    ?: "",
                themeColor = (map["setting_theme_color"] as? String)
                    ?: (map["themeColor"] as? String)
                    ?: "indigo",
                boardSkin = (map["board_color_skin"] as? String)
                    ?: (map["boardSkin"] as? String)
                    ?: "cyberpunk",
                blockStyle = (map["block_style"] as? String)
                    ?: (map["blockStyle"] as? String)
                    ?: "material",
                unlockedAchievements = achievementsList
            )
        }
    }
}


data class LiveBattleState(
    val grid: List<Int> = List(120) { 0 },
    val score: Int = 0,
    val lines: Int = 0,
    val combo: Int = 0,
    val isGameOver: Boolean = false,
    val garbageToSend: Int = 0,
    val lastUpdate: Long = 0L
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "grid" to grid,
            "score" to score,
            "lines" to lines,
            "combo" to combo,
            "isGameOver" to isGameOver,
            "garbageToSend" to garbageToSend,
            "lastUpdate" to lastUpdate
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>?): LiveBattleState {
            if (map == null) return LiveBattleState()
            return LiveBattleState(
                grid = (map["grid"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: List(120) { 0 },
                score = (map["score"] as? Number)?.toInt() ?: 0,
                lines = (map["lines"] as? Number)?.toInt() ?: 0,
                combo = (map["combo"] as? Number)?.toInt() ?: 0,
                isGameOver = map["isGameOver"] as? Boolean ?: false,
                garbageToSend = (map["garbageToSend"] as? Number)?.toInt() ?: 0,
                lastUpdate = (map["lastUpdate"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

data class BattleEmote(
    val id: String = "",
    val senderId: String = "",
    val emoji: String = "",
    val timestamp: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "senderId" to senderId,
        "emoji" to emoji,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any>?): BattleEmote {
            if (map == null) return BattleEmote()
            return BattleEmote(
                id = map["id"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                emoji = map["emoji"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}


data class RoomInvite(
    val id: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val hostName: String = "",
    val hostAvatarEmoji: String = "",
    val hostAvatarBgColor: String = "",
    val hostAvatarFrame: String = "standard",
    val hostTier: String = "BRONZE",
    val betAmount: Int = 0,
    val timestamp: Long = 0L,
    val hostAvatarBase64: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "roomId" to roomId,
        "roomName" to roomName,
        "hostName" to hostName,
        "hostAvatarEmoji" to hostAvatarEmoji,
        "hostAvatarBgColor" to hostAvatarBgColor,
        "hostAvatarFrame" to hostAvatarFrame,
        "hostTier" to hostTier,
        "betAmount" to betAmount,
        "timestamp" to timestamp,
        "hostAvatarBase64" to hostAvatarBase64
    )

    companion object {
        fun fromMap(map: Map<String, Any>): RoomInvite = RoomInvite(
            id = map["id"] as? String ?: "",
            roomId = map["roomId"] as? String ?: "",
            roomName = map["roomName"] as? String ?: "",
            hostName = map["hostName"] as? String ?: "",
            hostAvatarEmoji = (map["hostAvatarEmoji"] as? String) ?: (map["custom_avatar_emoji"] as? String) ?: "",
            hostAvatarBgColor = (map["hostAvatarBgColor"] as? String) ?: (map["custom_avatar_bg_color"] as? String) ?: "",
            hostAvatarFrame = (map["hostAvatarFrame"] as? String) ?: (map["equipped_avatar_frame"] as? String) ?: "standard",
            hostTier = map["hostTier"] as? String ?: "BRONZE",
            betAmount = (map["betAmount"] as? Number)?.toInt() ?: 0,
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
            hostAvatarBase64 = (map["hostAvatarBase64"] as? String) ?: (map["custom_avatar_base64"] as? String) ?: ""
        )
    }
}

data class PresenceUser(
    val uid: String = "",
    val username: String = "",
    val onlineTier: String = "BRONZE",
    val hasGradient: Boolean = false,
    val avatarEmoji: String = "",
    val avatarBgColor: String = "",
    val avatarFrame: String = "standard",
    val winStreak: Int = 0,
    val rating: Int = 1000,
    val title: String = "",
    val isOnline: Boolean = true,
    val lastSeen: Long = 0L,
    val credits: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "onlineTier" to onlineTier,
            "hasGradient" to hasGradient,
            "avatarEmoji" to avatarEmoji,
            "avatarBgColor" to avatarBgColor,
            "avatarFrame" to avatarFrame,
            "winStreak" to winStreak,
            "rating" to rating,
            "title" to title,
            "isOnline" to isOnline,
            "lastSeen" to lastSeen,
            "credits" to credits
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): PresenceUser {
            val rawLastSeen = (map["lastSeen"] as? Number)?.toLong()
                ?: (map["lastActive"] as? Number)?.toLong()
                ?: (map["last_synced_timestamp"] as? Number)?.toLong()
                ?: 0L
            val isRecentlyActive = (System.currentTimeMillis() - rawLastSeen) < 120_000L
            val rawOnline = (map["isOnline"] as? Boolean) ?: true
            val computedOnline = rawOnline && (rawLastSeen == 0L || isRecentlyActive)
            val credits = (map["credits"] as? Number)?.toInt()
                ?: (map["balance"] as? Number)?.toInt()
                ?: (map["coins"] as? Number)?.toInt()
                ?: 0

            return PresenceUser(
                uid = map["uid"] as? String ?: "",
                username = (map["username"] as? String) ?: (map["playerName"] as? String) ?: "Player",
                onlineTier = (map["onlineTier"] as? String) ?: (map["rank"] as? String) ?: "BRONZE",
                hasGradient = (map["hasGradient"] as? Boolean) ?: (map["hasNicknameGradient"] as? Boolean) ?: false,
                avatarEmoji = (map["avatarEmoji"] as? String) ?: (map["customAvatarEmoji"] as? String) ?: (map["custom_avatar_emoji"] as? String) ?: "",
                avatarBgColor = (map["avatarBgColor"] as? String) ?: (map["customAvatarBgColor"] as? String) ?: (map["custom_avatar_bg_color"] as? String) ?: "",
                avatarFrame = (map["avatarFrame"] as? String) ?: (map["equippedAvatarFrame"] as? String) ?: (map["equipped_avatar_frame"] as? String) ?: "standard",
                winStreak = (map["winStreak"] as? Number)?.toInt() ?: 0,
                rating = (map["rating"] as? Number)?.toInt() ?: 1000,
                title = (map["title"] as? String) ?: (map["equippedTitle"] as? String) ?: "",
                isOnline = computedOnline,
                lastSeen = rawLastSeen,
                credits = credits
            )
        }
    }
}