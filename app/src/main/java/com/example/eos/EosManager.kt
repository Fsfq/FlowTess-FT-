package com.example.eos

import android.app.Activity
import android.util.Log
import com.epicgames.mobile.eossdk.EOSSDK
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

data class EosRoom(
    val id: String,
    val name: String,
    val hostPuid: String,
    val hostName: String,
    val hostTier: String = "Bronze",
    val guestPuid: String? = null,
    val guestName: String? = null,
    val guestTier: String? = "Bronze",
    val bet: Int = 0,
    val isPrivate: Boolean = false,
    val isHostReady: Boolean = true,
    val isGuestReady: Boolean = false,
    val status: String = "waiting" // "waiting", "playing", "finished"
)

data class EosChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val senderPuid: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class EosPlayerPeer(
    val puid: String,
    val name: String,
    val tier: String = "Bronze",
    val pingMs: Int = 20,
    val isReady: Boolean = false
)

/**
 * Real Epic Online Services (EOS) Multiplayer Manager.
 * Handles cloud lobbies, P2P packet sync, chat, matchmaking, and game telemetry.
 */
object EosManager {
    private const val TAG = "EosManager"

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null
    private var packetPollJob: Job? = null

    private val isInitialized = AtomicBoolean(false)

    private val _isSdkReady = MutableStateFlow(false)
    val isSdkReady: StateFlow<Boolean> = _isSdkReady.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _localPuid = MutableStateFlow<String?>(null)
    val localPuid: StateFlow<String?> = _localPuid.asStateFlow()

    private val _localPlayerName = MutableStateFlow("Player")
    val localPlayerName: StateFlow<String> = _localPlayerName.asStateFlow()

    private val _localPlayerTier = MutableStateFlow("Bronze")
    val localPlayerTier: StateFlow<String> = _localPlayerTier.asStateFlow()

    // ── CURRENT ACTIVE ROOM ──
    private val _currentRoom = MutableStateFlow<EosRoom?>(null)
    val currentRoom: StateFlow<EosRoom?> = _currentRoom.asStateFlow()

    // ── AVAILABLE DISCOVERED ROOMS ──
    private val _availableRooms = MutableStateFlow<List<EosRoom>>(emptyList())
    val availableRooms: StateFlow<List<EosRoom>> = _availableRooms.asStateFlow()

    // ── ONLINE PEERS ──
    private val _onlinePeers = MutableStateFlow<List<EosPlayerPeer>>(emptyList())
    val onlinePeers: StateFlow<List<EosPlayerPeer>> = _onlinePeers.asStateFlow()

    // ── CHATS ──
    private val _roomChat = MutableStateFlow<List<EosChatMessage>>(emptyList())
    val roomChat: StateFlow<List<EosChatMessage>> = _roomChat.asStateFlow()

    private val _lobbyChat = MutableStateFlow<List<EosChatMessage>>(emptyList())
    val lobbyChat: StateFlow<List<EosChatMessage>> = _lobbyChat.asStateFlow()

    // ── IN-GAME TELEMETRY ──
    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore.asStateFlow()

    private val _opponentLines = MutableStateFlow(0)
    val opponentLines: StateFlow<Int> = _opponentLines.asStateFlow()

    private val _pendingGarbage = MutableStateFlow(0)
    val pendingGarbage: StateFlow<Int> = _pendingGarbage.asStateFlow()

    private val _gameStartSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val gameStartSignal: SharedFlow<Unit> = _gameStartSignal.asSharedFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun init(activity: Activity) {
        if (isInitialized.getAndSet(true)) return

        try {
            if (!EosBridge.isLoaded()) {
                Log.e(TAG, "Native libraries not loaded, aborting EOSSDK.init")
                _lastError.value = "Native EOS libraries failed to load"
                return
            }

            EOSSDK.init(activity)
            Log.i(TAG, "EOSSDK.init called with activity")

            val cachePath = activity.filesDir.absolutePath + "/"
            val initSuccess = EosBridge.initSdk(cachePath)
            if (!initSuccess) {
                Log.e(TAG, "EosBridge.initSdk failed")
                _lastError.value = "Failed to initialize EOS SDK native layer"
                return
            }

            val platformSuccess = EosBridge.createPlatform(
                productId = EosConstants.PRODUCT_ID,
                sandboxId = EosConstants.SANDBOX_ID,
                deploymentId = EosConstants.DEPLOYMENT_ID,
                clientId = EosConstants.CLIENT_ID,
                clientSecret = EosConstants.CLIENT_SECRET
            )

            if (!platformSuccess) {
                Log.e(TAG, "EosBridge.createPlatform failed")
                _lastError.value = "Failed to create EOS Platform instance"
                return
            }

            _isSdkReady.value = true
            startTickLoop()
            loginAnonymous(_localPlayerName.value)
            Log.i(TAG, "EOS SDK and Platform created successfully")
        } catch (e: Throwable) {
            Log.e(TAG, "Error initializing EOS SDK: ${e.message}", e)
            _lastError.value = e.message
        }
    }

    private fun startTickLoop() {
        tickJob?.cancel()
        tickJob = coroutineScope.launch {
            while (isActive) {
                try {
                    EosBridge.tick()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error in EOS tick: ${e.message}")
                }
                delay(50)
            }
        }
    }

    fun updatePlayerProfile(name: String, tier: String) {
        _localPlayerName.value = name
        _localPlayerTier.value = tier
    }

    fun loginAnonymous(playerName: String) {
        if (!_isSdkReady.value) return

        EosBridge.loginAnonymous(playerName, object : EosLoginCallback {
            override fun onLoginResult(success: Boolean, puid: String?) {
                if (success && puid != null) {
                    _isLoggedIn.value = true
                    _localPuid.value = puid
                    EosBridge.setupP2pNotification()
                    startPacketPolling()
                    Log.i(TAG, "EOS Login success! PUID: $puid")
                } else {
                    _isLoggedIn.value = false
                    Log.w(TAG, "EOS Login failed")
                }
            }
        })
    }

    // ── LOBBY ROOM ACTIONS ──

    fun createRoom(
        name: String,
        bet: Int = 0,
        isPrivate: Boolean = false,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val puid = _localPuid.value
        if (puid == null) {
            onComplete(false, "Not logged in to EOS")
            return
        }

        EosBridge.createLobby(name, bet, isPrivate, object : EosLobbyCallback {
            override fun onLobbyResult(success: Boolean, lobbyId: String?) {
                if (success && lobbyId != null) {
                    val newRoom = EosRoom(
                        id = lobbyId,
                        name = name,
                        hostPuid = puid,
                        hostName = _localPlayerName.value,
                        hostTier = _localPlayerTier.value,
                        bet = bet,
                        isPrivate = isPrivate,
                        isHostReady = true,
                        isGuestReady = false,
                        status = "waiting"
                    )
                    _currentRoom.value = newRoom
                    _roomChat.value = listOf(
                        EosChatMessage(
                            senderName = "System",
                            senderPuid = "SYSTEM",
                            text = "Комната создана! Ожидание подключения соперника."
                        )
                    )
                    onComplete(true, lobbyId)
                } else {
                    onComplete(false, "Failed to create EOS Lobby")
                }
            }
        })
    }

    fun joinRoom(
        roomId: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val puid = _localPuid.value
        if (puid == null) {
            onComplete(false, "Not logged in to EOS")
            return
        }

        EosBridge.joinLobby(roomId.trim(), object : EosLobbyCallback {
            override fun onLobbyResult(success: Boolean, lobbyId: String?) {
                if (success && lobbyId != null) {
                    val joinedRoom = EosRoom(
                        id = lobbyId,
                        name = "EOS Room ${lobbyId.takeLast(4)}",
                        hostPuid = "HOST",
                        hostName = "Host",
                        guestPuid = puid,
                        guestName = _localPlayerName.value,
                        guestTier = _localPlayerTier.value,
                        isHostReady = true,
                        isGuestReady = false,
                        status = "waiting"
                    )
                    _currentRoom.value = joinedRoom
                    _roomChat.value = listOf(
                        EosChatMessage(
                            senderName = "System",
                            senderPuid = "SYSTEM",
                            text = "Вы вошли в комнату $lobbyId"
                        )
                    )

                    // Send JOIN_REQUEST P2P packet
                    val joinPayload = JSONObject().apply {
                        put("type", "JOIN_REQUEST")
                        put("guestPuid", puid)
                        put("guestName", _localPlayerName.value)
                        put("guestTier", _localPlayerTier.value)
                    }.toString()
                    broadcastP2p(joinPayload)

                    onComplete(true, lobbyId)
                } else {
                    onComplete(false, "Failed to join EOS Lobby $roomId")
                }
            }
        })
    }

    fun leaveRoom() {
        val room = _currentRoom.value ?: return
        val puid = _localPuid.value ?: ""

        val leavePayload = JSONObject().apply {
            put("type", "LEAVE_ROOM")
            put("senderPuid", puid)
        }.toString()
        broadcastP2p(leavePayload)

        EosBridge.leaveLobby(room.id)
        _currentRoom.value = null
        _roomChat.value = emptyList()
        resetTelemetry()
    }

    fun toggleReady() {
        val room = _currentRoom.value ?: return
        val puid = _localPuid.value ?: return
        val isHost = (puid == room.hostPuid)

        val updatedRoom = if (isHost) {
            room.copy(isHostReady = !room.isHostReady)
        } else {
            room.copy(isGuestReady = !room.isGuestReady)
        }
        _currentRoom.value = updatedRoom

        val readyPayload = JSONObject().apply {
            put("type", "READY_TOGGLE")
            put("senderPuid", puid)
            put("isHost", isHost)
            put("isReady", if (isHost) updatedRoom.isHostReady else updatedRoom.isGuestReady)
        }.toString()
        broadcastP2p(readyPayload)
    }

    fun startMatch() {
        val room = _currentRoom.value ?: return
        _currentRoom.value = room.copy(status = "playing")

        val startPayload = JSONObject().apply {
            put("type", "GAME_START")
            put("roomId", room.id)
        }.toString()
        broadcastP2p(startPayload)
        _gameStartSignal.tryEmit(Unit)
    }

    fun sendRoomChatMessage(text: String) {
        val puid = _localPuid.value ?: return
        val msg = EosChatMessage(
            senderName = _localPlayerName.value,
            senderPuid = puid,
            text = text
        )
        _roomChat.value = _roomChat.value + msg

        val chatPayload = JSONObject().apply {
            put("type", "CHAT_ROOM")
            put("senderName", msg.senderName)
            put("senderPuid", msg.senderPuid)
            put("text", msg.text)
            put("timestamp", msg.timestamp)
        }.toString()
        broadcastP2p(chatPayload)
    }

    fun sendLobbyChatMessage(text: String) {
        val puid = _localPuid.value ?: return
        val msg = EosChatMessage(
            senderName = _localPlayerName.value,
            senderPuid = puid,
            text = text
        )
        _lobbyChat.value = _lobbyChat.value + msg

        val chatPayload = JSONObject().apply {
            put("type", "CHAT_LOBBY")
            put("senderName", msg.senderName)
            put("senderPuid", msg.senderPuid)
            put("text", msg.text)
            put("timestamp", msg.timestamp)
        }.toString()
        broadcastP2p(chatPayload)
    }

    // ── GAMEPLAY P2P PACKETS ──

    fun sendGameMove(score: Int, lines: Int) {
        val payload = JSONObject().apply {
            put("type", "GAME_MOVE")
            put("score", score)
            put("lines", lines)
        }.toString()
        broadcastP2p(payload)
    }

    fun sendGarbageLines(count: Int) {
        val payload = JSONObject().apply {
            put("type", "GARBAGE_LINES")
            put("count", count)
        }.toString()
        broadcastP2p(payload)
    }

    fun sendGameOver(score: Int) {
        val payload = JSONObject().apply {
            put("type", "GAME_OVER")
            put("score", score)
        }.toString()
        broadcastP2p(payload)
    }

    private fun broadcastP2p(jsonPayload: String) {
        val room = _currentRoom.value
        val myPuid = _localPuid.value ?: return
        val targetPuid = if (room != null) {
            if (myPuid == room.hostPuid) room.guestPuid else room.hostPuid
        } else null

        if (targetPuid != null && targetPuid.isNotBlank() && targetPuid != "HOST") {
            EosBridge.sendPacket(targetPuid, EosConstants.P2P_SOCKET_NAME, jsonPayload.toByteArray(Charsets.UTF_8))
        }
    }

    private fun startPacketPolling() {
        packetPollJob?.cancel()
        packetPollJob = coroutineScope.launch {
            while (isActive) {
                try {
                    val packet = EosBridge.receivePacket(EosConstants.P2P_SOCKET_NAME)
                    if (packet != null && packet.isNotEmpty()) {
                        handleIncomingPacket(packet)
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Error receiving packet: ${e.message}")
                }
                delay(25)
            }
        }
    }

    private fun handleIncomingPacket(data: ByteArray) {
        try {
            val jsonStr = String(data, Charsets.UTF_8)
            val json = JSONObject(jsonStr)
            val type = json.optString("type")

            when (type) {
                "JOIN_REQUEST" -> {
                    val guestPuid = json.optString("guestPuid")
                    val guestName = json.optString("guestName")
                    val guestTier = json.optString("guestTier", "Bronze")
                    val room = _currentRoom.value
                    if (room != null) {
                        val updated = room.copy(
                            guestPuid = guestPuid,
                            guestName = guestName,
                            guestTier = guestTier,
                            isGuestReady = false
                        )
                        _currentRoom.value = updated
                        _roomChat.value = _roomChat.value + EosChatMessage(
                            senderName = "System",
                            senderPuid = "SYSTEM",
                            text = "Игрок $guestName подключился к комнате!"
                        )

                        // Send acceptance back to guest
                        val acceptPayload = JSONObject().apply {
                            put("type", "JOIN_ACCEPTED")
                            put("roomName", room.name)
                            put("hostPuid", room.hostPuid)
                            put("hostName", room.hostName)
                            put("hostTier", room.hostTier)
                            put("bet", room.bet)
                        }.toString()
                        EosBridge.sendPacket(guestPuid, EosConstants.P2P_SOCKET_NAME, acceptPayload.toByteArray(Charsets.UTF_8))
                    }
                }

                "JOIN_ACCEPTED" -> {
                    val roomName = json.optString("roomName")
                    val hostPuid = json.optString("hostPuid")
                    val hostName = json.optString("hostName")
                    val hostTier = json.optString("hostTier", "Bronze")
                    val bet = json.optInt("bet", 0)

                    val room = _currentRoom.value
                    if (room != null) {
                        _currentRoom.value = room.copy(
                            name = roomName,
                            hostPuid = hostPuid,
                            hostName = hostName,
                            hostTier = hostTier,
                            bet = bet
                        )
                        _roomChat.value = _roomChat.value + EosChatMessage(
                            senderName = "System",
                            senderPuid = "SYSTEM",
                            text = "Соединение с хостом $hostName установлено!"
                        )
                    }
                }

                "READY_TOGGLE" -> {
                    val isHost = json.optBoolean("isHost")
                    val isReady = json.optBoolean("isReady")
                    val room = _currentRoom.value
                    if (room != null) {
                        _currentRoom.value = if (isHost) room.copy(isHostReady = isReady) else room.copy(isGuestReady = isReady)
                    }
                }

                "GAME_START" -> {
                    val room = _currentRoom.value
                    if (room != null) {
                        _currentRoom.value = room.copy(status = "playing")
                    }
                    _gameStartSignal.tryEmit(Unit)
                }

                "CHAT_ROOM" -> {
                    val senderName = json.optString("senderName")
                    val senderPuid = json.optString("senderPuid")
                    val text = json.optString("text")
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    _roomChat.value = _roomChat.value + EosChatMessage(
                        senderName = senderName,
                        senderPuid = senderPuid,
                        text = text,
                        timestamp = timestamp
                    )
                }

                "CHAT_LOBBY" -> {
                    val senderName = json.optString("senderName")
                    val senderPuid = json.optString("senderPuid")
                    val text = json.optString("text")
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    _lobbyChat.value = _lobbyChat.value + EosChatMessage(
                        senderName = senderName,
                        senderPuid = senderPuid,
                        text = text,
                        timestamp = timestamp
                    )
                }

                "GAME_MOVE" -> {
                    _opponentScore.value = json.optInt("score", 0)
                    _opponentLines.value = json.optInt("lines", 0)
                }

                "GARBAGE_LINES" -> {
                    val count = json.optInt("count", 0)
                    _pendingGarbage.value = _pendingGarbage.value + count
                }

                "GAME_OVER" -> {
                    val room = _currentRoom.value
                    if (room != null) {
                        _currentRoom.value = room.copy(status = "finished")
                    }
                }

                "LEAVE_ROOM" -> {
                    val room = _currentRoom.value
                    if (room != null) {
                        _currentRoom.value = room.copy(guestPuid = null, guestName = null, isGuestReady = false)
                        _roomChat.value = _roomChat.value + EosChatMessage(
                            senderName = "System",
                            senderPuid = "SYSTEM",
                            text = "Соперник покинул комнату"
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error handling incoming packet: ${e.message}", e)
        }
    }

    fun consumePendingGarbage(): Int {
        val g = _pendingGarbage.value
        _pendingGarbage.value = 0
        return g
    }

    private fun resetTelemetry() {
        _opponentScore.value = 0
        _opponentLines.value = 0
        _pendingGarbage.value = 0
    }

    fun onDestroy() {
        tickJob?.cancel()
        packetPollJob?.cancel()
        EosBridge.shutdown()
        isInitialized.set(false)
    }
}
