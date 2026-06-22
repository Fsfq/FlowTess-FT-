package com.example.db

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseLobbyManager(private val externalScope: CoroutineScope) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var presenceJob: Job? = null
    private var onlineCountListener: ListenerRegistration? = null
    private var roomsListener: ListenerRegistration? = null
    private var lobbyChatListener: ListenerRegistration? = null
    private var currentRoomListener: ListenerRegistration? = null
    private var roomChatListener: ListenerRegistration? = null

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

    // 1. Presence Heartbeat
    fun startPresenceUpdates(username: String, tier: String, hasGradient: Boolean) {
        presenceJob?.cancel()
        val uid = auth.currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
        
        presenceJob = externalScope.launch(Dispatchers.IO) {
            while (isActive) {
                val data = mapOf(
                    "uid" to uid,
                    "username" to username,
                    "tier" to tier,
                    "hasGradient" to hasGradient,
                    "status" to (if (_currentRoom.value != null) "in_room" else "in_lobby"),
                    "lastActive" to System.currentTimeMillis()
                )
                firestore.collection("presence").document(uid).set(data)
                delay(10000) // 10 seconds heartbeat
            }
        }

        // Listen to active online count
        onlineCountListener?.remove()
        onlineCountListener = firestore.collection("presence")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val now = System.currentTimeMillis()
                    val count = snapshot.documents.count { doc ->
                        val lastActive = doc.getLong("lastActive") ?: 0L
                        (now - lastActive) < 30000 // 30 seconds threshold
                    }
                    _onlinePlayersCount.value = maxOf(1, count)
                }
            }
    }

    fun stopPresence() {
        presenceJob?.cancel()
        presenceJob = null
        onlineCountListener?.remove()
        onlineCountListener = null
        val uid = auth.currentUser?.uid
        if (uid != null) {
            externalScope.launch(Dispatchers.IO) {
                try {
                    firestore.collection("presence").document(uid).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 2. Lobby Chat
    fun startLobbyChatSubscription() {
        lobbyChatListener?.remove()
        lobbyChatListener = firestore.collection("lobby_chat")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        ChatMessage.fromMap(data + mapOf("id" to doc.id))
                    }.reversed()
                    _lobbyChatMessages.value = msgs
                }
            }
    }

    fun sendLobbyChatMessage(text: String, username: String, hasGradient: Boolean) {
        val uid = auth.currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
        val msg = ChatMessage(
            senderId = uid,
            senderName = username,
            text = text,
            timestamp = System.currentTimeMillis(),
            hasGradient = hasGradient
        )
        firestore.collection("lobby_chat").add(msg.toMap())
    }

    // 3. Rooms Subscription
    fun startRoomsSubscription() {
        roomsListener?.remove()
        roomsListener = firestore.collection("rooms")
            .whereEqualTo("status", "waiting")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        LobbyRoom.fromMap(data + mapOf("roomId" to doc.id))
                    }
                    _activeRooms.value = list
                }
            }
    }

    // 4. Room Management
    fun createRoom(
        name: String, 
        passwordInput: String, 
        hostName: String, 
        hostTier: String,
        hostHasGradient: Boolean,
        onSuccess: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        val roomId = (100000..999999).random().toString() // 6 digit room code
        val room = LobbyRoom(
            roomId = roomId,
            name = name,
            hostId = uid,
            hostName = hostName,
            hostTier = hostTier,
            password = passwordInput,
            isLocked = passwordInput.isNotBlank(),
            status = "waiting",
            players = listOf(RoomPlayer(uid = uid, name = hostName, tier = hostTier, isReady = true, hasGradient = hostHasGradient)),
            createdAt = System.currentTimeMillis()
        )
        
        firestore.collection("rooms").document(roomId).set(room.toMap())
            .addOnSuccessListener {
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
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("rooms").document(roomId).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onFailure("Room not found")
                    return@addOnSuccessListener
                }
                val room = LobbyRoom.fromMap(snapshot.data ?: return@addOnSuccessListener)
                if (room.isLocked && room.password != passwordInput) {
                    onFailure("Incorrect password")
                    return@addOnSuccessListener
                }
                if (room.players.size >= 2) {
                    onFailure("Room is full")
                    return@addOnSuccessListener
                }
                
                // Add player
                val newPlayers = room.players.toMutableList()
                newPlayers.add(RoomPlayer(uid = uid, name = playerName, tier = playerTier, isReady = false, hasGradient = playerHasGradient))
                
                firestore.collection("rooms").document(roomId).update("players", newPlayers.map { it.toMap() })
                    .addOnSuccessListener {
                        onSuccess()
                        listenToCurrentRoom(roomId)
                    }
                    .addOnFailureListener {
                        onFailure(it.localizedMessage ?: "Failed to join")
                    }
            }
            .addOnFailureListener {
                onFailure(it.localizedMessage ?: "Network error")
            }
    }

    fun leaveRoom() {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        
        currentRoomListener?.remove()
        currentRoomListener = null
        roomChatListener?.remove()
        roomChatListener = null
        _currentRoom.value = null
        _roomChatMessages.value = emptyList()

        if (room.hostId == uid) {
            // Delete room
            firestore.collection("rooms").document(room.roomId).delete()
        } else {
            // Remove player
            val newPlayers = room.players.filter { it.uid != uid }
            firestore.collection("rooms").document(room.roomId).update("players", newPlayers.map { it.toMap() })
        }
    }

    fun toggleReady(isReady: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        val newPlayers = room.players.map {
            if (it.uid == uid) it.copy(isReady = isReady) else it
        }
        firestore.collection("rooms").document(room.roomId).update("players", newPlayers.map { it.toMap() })
    }

    fun startGame() {
        val room = _currentRoom.value ?: return
        firestore.collection("rooms").document(room.roomId).update(
            mapOf(
                "status" to "playing",
                "hostGrid" to List(120) { 0 },
                "opponentGrid" to List(120) { 0 },
                "hostScore" to 0,
                "opponentScore" to 0,
                "hostCombo" to 0,
                "opponentCombo" to 0,
                "hostGameOver" to false,
                "opponentGameOver" to false,
                "hostGarbageToSend" to 0,
                "opponentGarbageToSend" to 0,
                "winnerId" to ""
            )
        )
    }

    fun sendRoomChatMessage(text: String, username: String, hasGradient: Boolean) {
        val room = _currentRoom.value ?: return
        val uid = auth.currentUser?.uid ?: return
        val msg = ChatMessage(
            senderId = uid,
            senderName = username,
            text = text,
            timestamp = System.currentTimeMillis(),
            hasGradient = hasGradient
        )
        firestore.collection("rooms").document(room.roomId).collection("chat").add(msg.toMap())
    }

    // 5. Game State Sync
    fun updatePlayerGameState(grid: List<Int>, score: Int, combo: Int, isGameOver: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        val isHost = room.hostId == uid
        
        val updateData = if (isHost) {
            mapOf(
                "hostGrid" to grid,
                "hostScore" to score,
                "hostCombo" to combo,
                "hostGameOver" to isGameOver
            )
        } else {
            mapOf(
                "opponentGrid" to grid,
                "opponentScore" to score,
                "opponentCombo" to combo,
                "opponentGameOver" to isGameOver
            )
        }
        firestore.collection("rooms").document(room.roomId).update(updateData)
    }

    fun sendGarbageToOpponent(linesCount: Int) {
        val uid = auth.currentUser?.uid ?: return
        val room = _currentRoom.value ?: return
        val isHost = room.hostId == uid
        
        val field = if (isHost) "hostGarbageToSend" to linesCount else "opponentGarbageToSend" to linesCount
        
        firestore.runTransaction { transaction ->
            val roomRef = firestore.collection("rooms").document(room.roomId)
            val snapshot = transaction.get(roomRef)
            val currentGarbage = snapshot.getLong(field.first)?.toInt() ?: 0
            transaction.update(roomRef, field.first, currentGarbage + field.second)
        }
    }

    fun clearGarbageReceived(fieldToClear: String) {
        val room = _currentRoom.value ?: return
        firestore.collection("rooms").document(room.roomId).update(fieldToClear, 0)
    }

    fun setWinner(winnerId: String) {
        val room = _currentRoom.value ?: return
        firestore.collection("rooms").document(room.roomId).update(
            mapOf(
                "status" to "finished",
                "winnerId" to winnerId
            )
        )
    }

    // 6. Subscriptions internal
    fun listenToCurrentRoom(roomId: String) {
        currentRoomListener?.remove()
        currentRoomListener = firestore.collection("rooms").document(roomId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _currentRoom.value = LobbyRoom.fromMap(snapshot.data ?: return@addSnapshotListener)
                } else {
                    _currentRoom.value = null
                    _roomChatMessages.value = emptyList()
                }
            }

        // Listen to room chat
        roomChatListener?.remove()
        roomChatListener = firestore.collection("rooms").document(roomId).collection("chat")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        ChatMessage.fromMap(data + mapOf("id" to doc.id))
                    }.reversed()
                    _roomChatMessages.value = msgs
                }
            }
    }

    fun cleanUpAllListeners() {
        stopPresence()
        lobbyChatListener?.remove()
        lobbyChatListener = null
        roomsListener?.remove()
        roomsListener = null
        currentRoomListener?.remove()
        currentRoomListener = null
        roomChatListener?.remove()
        roomChatListener = null
    }
}
