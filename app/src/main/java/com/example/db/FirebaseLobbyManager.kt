package com.example.db

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseLobbyManager(private val externalScope: CoroutineScope) {

    private val auth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://fsfq-b840f-default-rtdb.europe-west1.firebasedatabase.app")
        } catch (e: Exception) {
            FirebaseDatabase.getInstance()
        }
    }

    private var presenceJob: Job? = null
    private var connectedListener: ValueEventListener? = null
    private var presenceCountListener: ValueEventListener? = null
    private var roomsListener: ValueEventListener? = null
    private var lobbyChatListener: ValueEventListener? = null
    private var currentRoomListener: ValueEventListener? = null
    private var roomChatListener: ValueEventListener? = null
    private var liveHostListener: ValueEventListener? = null
    private var liveOpponentListener: ValueEventListener? = null
    private var liveEmoteListener: ChildEventListener? = null

    private val _onlinePlayersCount = MutableStateFlow(1)
    val onlinePlayersCount: StateFlow<Int> = _onlinePlayersCount.asStateFlow()

    private val _activeRooms = MutableStateFlow<List<LobbyRoom>>(emptyList())
    val activeRooms: StateFlow<List<LobbyRoom>> = _activeRooms.asStateFlow()

    private val _lobbyChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val lobbyChatMessages: StateFlow<List<ChatMessage>> = _lobbyChatMessages.asStateFlow()

    private val _currentRoom = MutableStateFlow<LobbyRoom?>(null)
    val currentRoom: StateFlow<LobbyRoom?> = _currentRoom.asStateFlow()

    private val _roomChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val roomChatMessages: StateFlow<List<ChatMessage>> = _roomChatMessages.asStateFlow()

    private val _liveHostBattleState = MutableStateFlow(LiveBattleState())
    val liveHostBattleState: StateFlow<LiveBattleState> = _liveHostBattleState.asStateFlow()

    private val _liveOpponentBattleState = MutableStateFlow(LiveBattleState())
    val liveOpponentBattleState: StateFlow<LiveBattleState> = _liveOpponentBattleState.asStateFlow()

    private val _lastEmote = MutableStateFlow<BattleEmote?>(null)
    val lastEmote: StateFlow<BattleEmote?> = _lastEmote.asStateFlow()

    private val _incomingInvite = MutableStateFlow<RoomInvite?>(null)
    val incomingInvite: StateFlow<RoomInvite?> = _incomingInvite.asStateFlow()
    private var invitesListener: ChildEventListener? = null

    // ─────────────────────────────────────────────────────────────
    // 1. Presence System with RTDB onDisconnect() & Heartbeat
    // ─────────────────────────────────────────────────────────────
    fun startPresenceUpdates(username: String, tier: String, hasGradient: Boolean, credits: Int = 0) {
        presenceJob?.cancel()
        val uid = auth.currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
        val userPresenceRef = database.getReference("presence/$uid")
        val connectedRef = database.getReference(".info/connected")

        connectedListener?.let { connectedRef.removeEventListener(it) }
        connectedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    val presenceData = mapOf(
                        "uid" to uid,
                        "username" to username,
                        "tier" to tier,
                        "hasGradient" to hasGradient,
                        "credits" to credits,
                        "status" to (if (_currentRoom.value != null) "in_room" else "in_lobby"),
                        "lastActive" to ServerValue.TIMESTAMP
                    )
                    userPresenceRef.onDisconnect().removeValue()
                    userPresenceRef.setValue(presenceData)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        connectedRef.addValueEventListener(connectedListener!!)

        // Listen for total active online players count
        val allPresenceRef = database.getReference("presence")
        presenceCountListener?.let { allPresenceRef.removeEventListener(it) }
        presenceCountListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                _onlinePlayersCount.value = maxOf(1, count)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        allPresenceRef.addValueEventListener(presenceCountListener!!)

        // Periodic Heartbeat loop (every 30 seconds update RTDB & Firestore)
        presenceJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val currentUid = auth.currentUser?.uid
                    if (currentUid != null) {
                        database.getReference("presence/$currentUid/lastActive").setValue(ServerValue.TIMESTAMP)
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(currentUid)
                            .update(mapOf(
                                "is_online" to true,
                                "last_synced_timestamp" to System.currentTimeMillis()
                            ))
                    }
                } catch (e: Exception) {
                    // Ignore network fluctuations
                }
                delay(30_000L)
            }
        }
    }

    fun stopPresence() {
        presenceJob?.cancel()
        presenceJob = null
        connectedListener?.let { database.getReference(".info/connected").removeEventListener(it) }
        connectedListener = null
        presenceCountListener?.let { database.getReference("presence").removeEventListener(it) }
        presenceCountListener = null

        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("presence/$uid").removeValue()
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .update(mapOf(
                        "is_online" to false,
                        "last_synced_timestamp" to System.currentTimeMillis()
                    ))
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Lobby Chat with RTDB
    // ─────────────────────────────────────────────────────────────
    fun startLobbyChatSubscription() {
        val chatRef = database.getReference("lobby_chat").limitToLast(50)
        lobbyChatListener?.let { chatRef.removeEventListener(it) }
        lobbyChatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                for (child in snapshot.children) {
                    @Suppress("UNCHECKED_CAST")
                    val map = child.value as? Map<String, Any> ?: continue
                    val ts = (map["timestamp"] as? Number)?.toLong() ?: 0L
                    if (ts > 0 && ts < cutoff) {
                        child.ref.removeValue()
                        continue
                    }
                    list.add(ChatMessage.fromMap(map + mapOf("id" to (child.key ?: ""))))
                }
                _lobbyChatMessages.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        chatRef.addValueEventListener(lobbyChatListener!!)
    }

    fun sendLobbyChatMessage(
        text: String,
        username: String,
        hasGradient: Boolean,
        tier: String = "BRONZE",
        avatarEmoji: String = "",
        avatarBgColor: String = "",
        avatarFrame: String = "standard",
        replyToSender: String = "",
        replyToText: String = ""
    ) {
        val uid = auth.currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
        val chatRef = database.getReference("lobby_chat").push()
        val msgId = chatRef.key ?: System.currentTimeMillis().toString()

        val msg = ChatMessage(
            id = msgId,
            senderId = uid,
            senderName = username,
            text = text,
            timestamp = System.currentTimeMillis(),
            hasGradient = hasGradient,
            senderTier = tier,
            senderAvatarEmoji = avatarEmoji,
            senderAvatarBgColor = avatarBgColor,
            senderAvatarFrame = avatarFrame,
            replyToSender = replyToSender,
            replyToText = replyToText
        )
        chatRef.setValue(msg.toMap())
    }

    fun deleteLobbyChatMessage(messageId: String) {
        if (messageId.isEmpty()) return
        database.getReference("lobby_chat/$messageId").removeValue()
    }

    fun toggleLobbyMessageReaction(messageId: String, emoji: String, uidInput: String = "") {
        val uid = uidInput.ifEmpty { auth.currentUser?.uid ?: "" }
        if (messageId.isEmpty() || uid.isEmpty()) return
        val reactionRef = database.getReference("lobby_chat/$messageId/reactions/$emoji")
        reactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { list.add(it) }
                }
                if (list.contains(uid)) {
                    list.remove(uid)
                } else {
                    list.add(uid)
                }
                if (list.isEmpty()) {
                    reactionRef.removeValue()
                } else {
                    reactionRef.setValue(list)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    // ─────────────────────────────────────────────────────────────
    // 2.1 Invites & Quick Match
    // ─────────────────────────────────────────────────────────────
    fun startInvitesListener() {
        val uid = auth.currentUser?.uid ?: return
        val invitesRef = database.getReference("invites/$uid")
        invitesListener?.let { invitesRef.removeEventListener(it) }
        invitesListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                @Suppress("UNCHECKED_CAST")
                val map = snapshot.value as? Map<String, Any> ?: return
                val invite = RoomInvite.fromMap(map + mapOf("id" to (snapshot.key ?: "")))
                if (System.currentTimeMillis() - invite.timestamp < 60_000) {
                    _incomingInvite.value = invite
                } else {
                    snapshot.ref.removeValue()
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                if (_incomingInvite.value?.id == snapshot.key) {
                    _incomingInvite.value = null
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        invitesRef.addChildEventListener(invitesListener!!)
    }

    fun dismissInvite(inviteId: String) {
        val uid = auth.currentUser?.uid ?: return
        _incomingInvite.value = null
        if (inviteId.isNotEmpty()) {
            database.getReference("invites/$uid/$inviteId").removeValue()
        }
    }

    fun sendRoomInvite(
        targetUid: String,
        roomId: String,
        roomName: String,
        hostName: String,
        avatarEmoji: String,
        avatarBgColor: String,
        avatarFrame: String,
        hostTier: String
    ) {
        val inviteRef = database.getReference("invites/$targetUid").push()
        val invite = RoomInvite(
            id = inviteRef.key ?: System.currentTimeMillis().toString(),
            roomId = roomId,
            roomName = roomName,
            hostName = hostName,
            hostAvatarEmoji = avatarEmoji,
            hostAvatarBgColor = avatarBgColor,
            hostAvatarFrame = avatarFrame,
            hostTier = hostTier,
            betAmount = 0,
            timestamp = System.currentTimeMillis()
        )
        inviteRef.setValue(invite.toMap())
    }

    fun findOrCreateQuickMatch(
        playerName: String,
        playerTier: String,
        hasGradient: Boolean,
        avatarEmoji: String = "",
        avatarBgColor: String = "",
        avatarFrame: String = "standard",
        winStreak: Int = 0,
        rating: Int = 1000,
        customTag: String = "",
        onJoined: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val roomsRef = database.getReference("rooms")
        roomsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var targetRoom: LobbyRoom? = null
                for (child in snapshot.children) {
                    @Suppress("UNCHECKED_CAST")
                    val map = child.value as? Map<String, Any> ?: continue
                    val room = LobbyRoom.fromMap(map + mapOf("roomId" to (child.key ?: "")))
                    if (room.status == "waiting" && !room.isLocked && room.players.size == 1) {
                        targetRoom = room
                        break
                    }
                }
                if (targetRoom != null) {
                    joinRoom(
                        roomId = targetRoom.roomId,
                        passwordInput = "",
                        playerName = playerName,
                        playerTier = playerTier,
                        playerHasGradient = hasGradient,
                        avatarEmoji = avatarEmoji,
                        avatarBgColor = avatarBgColor,
                        avatarFrame = avatarFrame,
                        winStreak = winStreak,
                        rating = rating,
                        customTag = customTag,
                        onSuccess = { onJoined(targetRoom.roomId) },
                        onFailure = onFailure
                    )
                } else {
                    createRoom(
                        name = "Быстрый бой",
                        passwordInput = "",
                        hostName = playerName,
                        hostTier = playerTier,
                        hostHasGradient = hasGradient,
                        avatarEmoji = avatarEmoji,
                        avatarBgColor = avatarBgColor,
                        avatarFrame = avatarFrame,
                        winStreak = winStreak,
                        rating = rating,
                        customTag = customTag,
                        gameMode = "CLASSIC",
                        garbageIntensity = 1.0f,
                        roundTarget = 1,
                        onSuccess = onJoined
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    // ─────────────────────────────────────────────────────────────
    // 3. Rooms Subscription
    // ─────────────────────────────────────────────────────────────
    fun startRoomsSubscription() {
        val roomsRef = database.getReference("rooms")
        roomsListener?.let { roomsRef.removeEventListener(it) }
        roomsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<LobbyRoom>()
                val now = System.currentTimeMillis()
                for (child in snapshot.children) {
                    @Suppress("UNCHECKED_CAST")
                    val map = child.value as? Map<String, Any> ?: continue
                    val room = LobbyRoom.fromMap(map + mapOf("roomId" to (child.key ?: "")))

                    val isExpired = room.createdAt > 0L && (now - room.createdAt) > 2 * 60 * 60 * 1000L
                    val hasNoPlayers = room.players.isEmpty()
                    val hostMissing = room.players.none { it.uid == room.hostId }

                    // Automatically clean up 0-player or abandoned rooms from Firebase
                    if (hasNoPlayers || hostMissing || isExpired) {
                        child.ref.removeValue()
                        continue
                    }

                    if (room.status == "waiting") {
                        list.add(room)
                    }
                }
                _activeRooms.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        roomsRef.addValueEventListener(roomsListener!!)
    }

    // ─────────────────────────────────────────────────────────────
    // 4. Room Management
    // ─────────────────────────────────────────────────────────────
    fun createRoom(
        name: String,
        passwordInput: String,
        hostName: String,
        hostTier: String,
        hostHasGradient: Boolean,
        avatarEmoji: String = "",
        avatarBgColor: String = "",
        avatarFrame: String = "standard",
        winStreak: Int = 0,
        rating: Int = 1000,
        customTag: String = "",
        gameMode: String = "CLASSIC",
        garbageIntensity: Float = 1.0f,
        roundTarget: Int = 1,
        onSuccess: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        val roomId = (100000..999999).random().toString()
        val roomRef = database.getReference("rooms/$roomId")

        val hostPlayer = RoomPlayer(
            uid = uid,
            name = hostName,
            tier = hostTier,
            isReady = true,
            hasGradient = hostHasGradient,
            avatarEmoji = avatarEmoji,
            avatarBgColor = avatarBgColor,
            avatarFrame = avatarFrame,
            winStreak = winStreak,
            rating = rating,
            customTag = customTag
        )

        val passHash = if (passwordInput.isNotBlank()) PasswordHasher.hash(passwordInput) else ""
        val room = LobbyRoom(
            roomId = roomId,
            name = name,
            hostId = uid,
            hostName = hostName,
            hostTier = hostTier,
            password = "",
            passwordHash = passHash,
            isLocked = passHash.isNotBlank(),
            status = "waiting",
            gameMode = gameMode,
            garbageIntensity = garbageIntensity,
            roundTarget = roundTarget,
            hostWins = 0,
            opponentWins = 0,
            currentRound = 1,
            players = listOf(hostPlayer),
            createdAt = System.currentTimeMillis(),
            betAmount = 0
        )

        roomRef.setValue(room.toMap()).addOnSuccessListener {
            roomRef.onDisconnect().removeValue()
            onSuccess(roomId)
            listenToCurrentRoom(roomId)
        }
    }

    fun joinRoom(
        roomId: String,
        passwordInput: String,
        playerName: String,
        playerTier: String,
        playerHasGradient: Boolean,
        avatarEmoji: String = "",
        avatarBgColor: String = "",
        avatarFrame: String = "standard",
        winStreak: Int = 0,
        rating: Int = 1000,
        customTag: String = "",
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        val roomRef = database.getReference("rooms/$roomId")

        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onFailure("Комната не найдена")
                    return
                }
                @Suppress("UNCHECKED_CAST")
                val map = snapshot.value as? Map<String, Any> ?: return
                val room = LobbyRoom.fromMap(map + mapOf("roomId" to roomId))

                if (room.isLocked) {
                    val inputHash = if (passwordInput.isNotBlank()) PasswordHasher.hash(passwordInput) else ""
                    val isPassValid = when {
                        room.passwordHash.isNotEmpty() -> room.passwordHash == inputHash
                        room.password.isNotEmpty() -> room.password == passwordInput || room.password == inputHash
                        else -> true
                    }
                    if (!isPassValid) {
                        onFailure("Неверный пароль")
                        return
                    }
                }
                if (room.players.size >= 2 && room.players.none { it.uid == uid }) {
                    onFailure("Комната заполнена")
                    return
                }

                val newPlayer = RoomPlayer(
                    uid = uid,
                    name = playerName,
                    tier = playerTier,
                    isReady = false,
                    hasGradient = playerHasGradient,
                    avatarEmoji = avatarEmoji,
                    avatarBgColor = avatarBgColor,
                    avatarFrame = avatarFrame,
                    winStreak = winStreak,
                    rating = rating,
                    customTag = customTag
                )

                val playerRef = roomRef.child("players").child(uid)
                playerRef.onDisconnect().removeValue()
                playerRef.setValue(newPlayer.toMap())
                    .addOnSuccessListener {
                        onSuccess()
                        listenToCurrentRoom(roomId)
                    }
                    .addOnFailureListener { onFailure(it.localizedMessage ?: "Ошибка подключения") }
            }
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun leaveRoom() {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return

        currentRoomListener?.let { database.getReference("rooms/${room.roomId}").removeEventListener(it) }
        currentRoomListener = null
        roomChatListener?.let { database.getReference("rooms/${room.roomId}/chat").removeEventListener(it) }
        roomChatListener = null
        liveHostListener?.let { database.getReference("rooms/${room.roomId}/live/host").removeEventListener(it) }
        liveHostListener = null
        liveOpponentListener?.let { database.getReference("rooms/${room.roomId}/live/opponent").removeEventListener(it) }
        liveOpponentListener = null
        liveEmoteListener?.let { database.getReference("rooms/${room.roomId}/live/emotes").removeEventListener(it) }
        liveEmoteListener = null

        _currentRoom.value = null
        _roomChatMessages.value = emptyList()
        _liveHostBattleState.value = LiveBattleState()
        _liveOpponentBattleState.value = LiveBattleState()

        val roomRef = database.getReference("rooms/${room.roomId}")
        val otherPlayer = room.players.find { it.uid != uid }

        if (room.status == "playing" && otherPlayer != null) {
            // Player surrendered/left active battle -> Award forfeit win to remaining player
            val updates = mapOf(
                "status" to "finished",
                "winnerId" to otherPlayer.uid,
                "forfeitBy" to uid
            )
            roomRef.updateChildren(updates)
            roomRef.child("players").child(uid).removeValue()
        } else {
            if (room.hostId == uid) {
                roomRef.removeValue()
            } else {
                roomRef.child("players").child(uid).removeValue()
                roomRef.child("players").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                            roomRef.removeValue()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    fun toggleReady(isReady: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        database.getReference("rooms/${room.roomId}/players/$uid/isReady").setValue(isReady)
    }

    fun startGame() {
        val room = _currentRoom.value ?: return
        val roomRef = database.getReference("rooms/${room.roomId}")

        val initLiveState = LiveBattleState(
            grid = List(120) { 0 },
            score = 0,
            lines = 0,
            combo = 0,
            isGameOver = false,
            garbageToSend = 0,
            lastUpdate = System.currentTimeMillis()
        )

        val updates = mapOf(
            "status" to "playing",
            "hostWins" to 0,
            "opponentWins" to 0,
            "currentRound" to 1,
            "winnerId" to "",
            "live/host" to initLiveState.toMap(),
            "live/opponent" to initLiveState.toMap()
        )
        roomRef.updateChildren(updates)
    }

    fun sendRoomChatMessage(
        text: String,
        username: String,
        hasGradient: Boolean,
        tier: String = "BRONZE",
        avatarEmoji: String = "",
        avatarBgColor: String = "",
        avatarFrame: String = "standard",
        replyToSender: String = "",
        replyToText: String = ""
    ) {
        val room = _currentRoom.value ?: return
        val uid = auth.currentUser?.uid ?: return
        val chatRef = database.getReference("rooms/${room.roomId}/chat").push()
        val msgId = chatRef.key ?: System.currentTimeMillis().toString()

        val msg = ChatMessage(
            id = msgId,
            senderId = uid,
            senderName = username,
            text = text,
            timestamp = System.currentTimeMillis(),
            hasGradient = hasGradient,
            senderTier = tier,
            senderAvatarEmoji = avatarEmoji,
            senderAvatarBgColor = avatarBgColor,
            senderAvatarFrame = avatarFrame,
            replyToSender = replyToSender,
            replyToText = replyToText
        )
        chatRef.setValue(msg.toMap())
    }

    fun deleteRoomChatMessage(messageId: String) {
        val room = _currentRoom.value ?: return
        if (messageId.isEmpty()) return
        database.getReference("rooms/${room.roomId}/chat/$messageId").removeValue()
    }

    fun toggleRoomMessageReaction(messageId: String, emoji: String, uidInput: String = "") {
        val room = _currentRoom.value ?: return
        val uid = uidInput.ifEmpty { auth.currentUser?.uid ?: "" }
        if (messageId.isEmpty() || uid.isEmpty()) return
        val reactionRef = database.getReference("rooms/${room.roomId}/chat/$messageId/reactions/$emoji")
        reactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { list.add(it) }
                }
                if (list.contains(uid)) {
                    list.remove(uid)
                } else {
                    list.add(uid)
                }
                if (list.isEmpty()) {
                    reactionRef.removeValue()
                } else {
                    reactionRef.setValue(list)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ─────────────────────────────────────────────────────────────
    // 5. Realtime Battle Data Sync (High Speed)
    // ─────────────────────────────────────────────────────────────
    fun updatePlayerLiveState(grid: List<Int>, score: Int, lines: Int, combo: Int, isGameOver: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        val isHost = room.hostId == uid
        val nodePath = if (isHost) "host" else "opponent"

        val liveData = mapOf(
            "grid" to grid,
            "score" to score,
            "lines" to lines,
            "combo" to combo,
            "isGameOver" to isGameOver,
            "lastUpdate" to ServerValue.TIMESTAMP
        )
        database.getReference("rooms/${room.roomId}/live/$nodePath").updateChildren(liveData)
    }

    fun sendGarbageToOpponent(linesCount: Int) {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        val isHost = room.hostId == uid
        val targetNode = if (isHost) "opponent" else "host"

        val clampedLines = linesCount.coerceIn(1, 4)
        val scaledLines = maxOf(1, (clampedLines * room.garbageIntensity).toInt()).coerceAtMost(20)
        val garbageRef = database.getReference("rooms/${room.roomId}/live/$targetNode/garbageToSend")
        garbageRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = (currentData.value as? Number)?.toInt() ?: 0
                currentData.value = (current + scaledLines).coerceAtMost(20)
                return Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
        })
    }

    fun clearGarbageReceived(forHost: Boolean) {
        val room = _currentRoom.value ?: return
        val targetNode = if (forHost) "host" else "opponent"
        database.getReference("rooms/${room.roomId}/live/$targetNode/garbageToSend").setValue(0)
    }

    fun sendBattleEmote(emoji: String) {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        val emoteRef = database.getReference("rooms/${room.roomId}/live/emotes").push()
        val emote = BattleEmote(
            id = emoteRef.key ?: System.currentTimeMillis().toString(),
            senderId = uid,
            emoji = emoji,
            timestamp = System.currentTimeMillis()
        )
        emoteRef.setValue(emote.toMap())
    }

    fun setWinner(winnerId: String) {
        val room = _currentRoom.value ?: return
        val updates = mapOf(
            "status" to "finished",
            "winnerId" to winnerId
        )
        database.getReference("rooms/${room.roomId}").updateChildren(updates)
    }

    fun recordRoundWin(winnerId: String) {
        val room = _currentRoom.value ?: return
        val isHostWinner = room.hostId == winnerId
        val newHostWins = if (isHostWinner) room.hostWins + 1 else room.hostWins
        val newOpponentWins = if (!isHostWinner) room.opponentWins + 1 else room.opponentWins
        val winsToWinMatch = (room.roundTarget / 2) + 1

        val isMatchOver = newHostWins >= winsToWinMatch || newOpponentWins >= winsToWinMatch || room.roundTarget == 1

        val roomRef = database.getReference("rooms/${room.roomId}")
        if (isMatchOver) {
            val updates = mapOf(
                "status" to "finished",
                "winnerId" to winnerId,
                "hostWins" to newHostWins,
                "opponentWins" to newOpponentWins
            )
            roomRef.updateChildren(updates)
        } else {
            val initLive = LiveBattleState(
                grid = List(120) { 0 },
                score = 0,
                lines = 0,
                combo = 0,
                isGameOver = false,
                garbageToSend = 0,
                lastUpdate = System.currentTimeMillis()
            )
            val updates = mapOf(
                "status" to "playing",
                "hostWins" to newHostWins,
                "opponentWins" to newOpponentWins,
                "currentRound" to room.currentRound + 1,
                "live/host" to initLive.toMap(),
                "live/opponent" to initLive.toMap()
            )
            roomRef.updateChildren(updates)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 6. Subscriptions & Listeners
    // ─────────────────────────────────────────────────────────────
    fun listenToCurrentRoom(roomId: String) {
        val roomRef = database.getReference("rooms/$roomId")

        // 1. Room Metadata Listener
        currentRoomListener?.let { roomRef.removeEventListener(it) }
        currentRoomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val map = snapshot.value as? Map<String, Any>
                    if (map != null) {
                        _currentRoom.value = LobbyRoom.fromMap(map + mapOf("roomId" to roomId))
                    }
                } else {
                    _currentRoom.value = null
                    _roomChatMessages.value = emptyList()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        roomRef.addValueEventListener(currentRoomListener!!)

        // 2. Room Chat Listener
        val chatRef = database.getReference("rooms/$roomId/chat").limitToLast(30)
        roomChatListener?.let { chatRef.removeEventListener(it) }
        roomChatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    @Suppress("UNCHECKED_CAST")
                    val map = child.value as? Map<String, Any> ?: continue
                    list.add(ChatMessage.fromMap(map + mapOf("id" to (child.key ?: ""))))
                }
                _roomChatMessages.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        chatRef.addValueEventListener(roomChatListener!!)

        // 3. Live Host Battle State Listener
        val liveHostRef = database.getReference("rooms/$roomId/live/host")
        liveHostListener?.let { liveHostRef.removeEventListener(it) }
        liveHostListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                @Suppress("UNCHECKED_CAST")
                val map = snapshot.value as? Map<String, Any>
                _liveHostBattleState.value = LiveBattleState.fromMap(map)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        liveHostRef.addValueEventListener(liveHostListener!!)

        // 4. Live Opponent Battle State Listener
        val liveOpponentRef = database.getReference("rooms/$roomId/live/opponent")
        liveOpponentListener?.let { liveOpponentRef.removeEventListener(it) }
        liveOpponentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                @Suppress("UNCHECKED_CAST")
                val map = snapshot.value as? Map<String, Any>
                _liveOpponentBattleState.value = LiveBattleState.fromMap(map)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        liveOpponentRef.addValueEventListener(liveOpponentListener!!)

        // 5. Live In-Game Emote Reactions Listener
        val emotesRef = database.getReference("rooms/$roomId/live/emotes").limitToLast(1)
        liveEmoteListener?.let { emotesRef.removeEventListener(it) }
        liveEmoteListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                @Suppress("UNCHECKED_CAST")
                val map = snapshot.value as? Map<String, Any>
                if (map != null) {
                    val emote = BattleEmote.fromMap(map + mapOf("id" to (snapshot.key ?: "")))
                    // Only trigger if emitted recently (last 10 seconds)
                    if (System.currentTimeMillis() - emote.timestamp < 10000) {
                        _lastEmote.value = emote
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        emotesRef.addChildEventListener(liveEmoteListener!!)
    }

    fun cleanUpAllListeners() {
        stopPresence()
        lobbyChatListener?.let { database.getReference("lobby_chat").removeEventListener(it) }
        lobbyChatListener = null
        roomsListener?.let { database.getReference("rooms").removeEventListener(it) }
        roomsListener = null
        invitesListener?.let { database.getReference("invites").removeEventListener(it) }
        invitesListener = null
        currentRoomListener?.let {
            val roomId = _currentRoom.value?.roomId
            if (roomId != null) database.getReference("rooms/$roomId").removeEventListener(it)
        }
        currentRoomListener = null
        roomChatListener = null
        liveHostListener = null
        liveOpponentListener = null
        liveEmoteListener = null
    }
}
