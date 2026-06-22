package com.example.db

data class LobbyRoom(
    val roomId: String = "",
    val name: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostTier: String = "BRONZE",
    val password: String = "",
    val isLocked: Boolean = false,
    val status: String = "waiting", // "waiting", "playing", "finished"
    val players: List<RoomPlayer> = emptyList(),
    val createdAt: Long = 0L,
    val hostGrid: List<Int> = List(120) { 0 }, // 12x10 flat board
    val opponentGrid: List<Int> = List(120) { 0 }, // 12x10 flat board
    val hostScore: Int = 0,
    val opponentScore: Int = 0,
    val hostCombo: Int = 0,
    val opponentCombo: Int = 0,
    val hostGameOver: Boolean = false,
    val opponentGameOver: Boolean = false,
    val hostGarbageToSend: Int = 0,
    val opponentGarbageToSend: Int = 0,
    val winnerId: String = ""
) {
    // Map conversion functions helper for Firestore compatibility
    fun toMap(): Map<String, Any> {
        return mapOf(
            "roomId" to roomId,
            "name" to name,
            "hostId" to hostId,
            "hostName" to hostName,
            "hostTier" to hostTier,
            "password" to password,
            "isLocked" to isLocked,
            "status" to status,
            "players" to players.map { it.toMap() },
            "createdAt" to createdAt,
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
            "winnerId" to winnerId
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): LobbyRoom {
            val playersList = (map["players"] as? List<Map<String, Any>>)?.map { RoomPlayer.fromMap(it) } ?: emptyList()
            return LobbyRoom(
                roomId = map["roomId"] as? String ?: "",
                name = map["name"] as? String ?: "",
                hostId = map["hostId"] as? String ?: "",
                hostName = map["hostName"] as? String ?: "",
                hostTier = map["hostTier"] as? String ?: "BRONZE",
                password = map["password"] as? String ?: "",
                isLocked = map["isLocked"] as? Boolean ?: false,
                status = map["status"] as? String ?: "waiting",
                players = playersList,
                createdAt = (map["createdAt"] as? Long) ?: (map["createdAt"] as? Number)?.toLong() ?: 0L,
                hostGrid = (map["hostGrid"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: List(120) { 0 },
                opponentGrid = (map["opponentGrid"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: List(120) { 0 },
                hostScore = (map["hostScore"] as? Number)?.toInt() ?: 0,
                opponentScore = (map["opponentScore"] as? Number)?.toInt() ?: 0,
                hostCombo = (map["hostCombo"] as? Number)?.toInt() ?: 0,
                opponentCombo = (map["opponentCombo"] as? Number)?.toInt() ?: 0,
                hostGameOver = map["hostGameOver"] as? Boolean ?: false,
                opponentGameOver = map["opponentGameOver"] as? Boolean ?: false,
                hostGarbageToSend = (map["hostGarbageToSend"] as? Number)?.toInt() ?: 0,
                opponentGarbageToSend = (map["opponentGarbageToSend"] as? Number)?.toInt() ?: 0,
                winnerId = map["winnerId"] as? String ?: ""
            )
        }
    }
}

data class RoomPlayer(
    val uid: String = "",
    val name: String = "",
    val tier: String = "BRONZE",
    val isReady: Boolean = false,
    val hasGradient: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "tier" to tier,
            "isReady" to isReady,
            "hasGradient" to hasGradient
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): RoomPlayer {
            return RoomPlayer(
                uid = map["uid"] as? String ?: "",
                name = map["name"] as? String ?: "",
                tier = map["tier"] as? String ?: "BRONZE",
                isReady = map["isReady"] as? Boolean ?: false,
                hasGradient = map["hasGradient"] as? Boolean ?: false
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
    val hasGradient: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to text,
            "timestamp" to timestamp,
            "hasGradient" to hasGradient
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): ChatMessage {
            return ChatMessage(
                id = map["id"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                text = map["text"] as? String ?: "",
                timestamp = (map["timestamp"] as? Long) ?: (map["timestamp"] as? Number)?.toLong() ?: 0L,
                hasGradient = map["hasGradient"] as? Boolean ?: false
            )
        }
    }
}
