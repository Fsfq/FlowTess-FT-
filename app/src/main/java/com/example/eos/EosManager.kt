package com.example.eos

import android.app.Activity
import android.util.Log
import com.epicgames.mobile.eossdk.EOSSDK
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
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
    val code: String = "",
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
    private var searchLoopJob: Job? = null
    private var joinHandshakeJob: Job? = null
    private var disconnectTimeoutJob: Job? = null

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

    private val _p2pPingMs = MutableStateFlow(24)
    val p2pPingMs: StateFlow<Int> = _p2pPingMs.asStateFlow()

    var battlePacketListener: ((JSONObject) -> Unit)? = null

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
        tickJob = coroutineScope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                try {
                    EosBridge.tick()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error in EOS tick: ${e.message}")
                }
                delay(20)
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
                    setupMemberStatusListener()
                    startSearchLoop()
                    Log.i(TAG, "EOS Login success! PUID: $puid")
                } else {
                    _isLoggedIn.value = false
                    Log.w(TAG, "EOS Login failed")
                }
            }
        })
    }

    private fun setupMemberStatusListener() {
        EosBridge.setupMemberStatusNotification(object : EosMemberStatusCallback {
            override fun onMemberStatusChanged(lobbyId: String, memberPuid: String, status: String) {
                val room = _currentRoom.value ?: return
                if (room.id != lobbyId) return
                val local = _localPuid.value ?: return

                Log.i(TAG, "Member status notification: lobby=$lobbyId, member=$memberPuid, status=$status")

                if (status == "JOINED") {
                    if (room.hostPuid == local && memberPuid != local) {
                        // Host sees guest joined
                        val updated = room.copy(
                            guestPuid = memberPuid,
                            guestName = room.guestName ?: "Guest",
                            isGuestReady = false
                        )
                        _currentRoom.value = updated

                        // Proactively send JOIN_ACCEPTED with retries to ensure delivery over WAN/Relay
                        coroutineScope.launch {
                            val acceptPayload = JSONObject().apply {
                                put("type", "JOIN_ACCEPTED")
                                put("roomName", room.name)
                                put("hostPuid", room.hostPuid)
                                put("hostName", room.hostName)
                                put("hostTier", room.hostTier)
                                put("bet", room.bet)
                            }.toString()
                            val bytes = acceptPayload.toByteArray(Charsets.UTF_8)
                            var retries = 0
                            while (isActive && retries < 8 && _currentRoom.value?.guestPuid == memberPuid) {
                                retries++
                                EosBridge.sendPacket(memberPuid, EosConstants.P2P_SOCKET_NAME, bytes)
                                delay(450)
                            }
                        }
                    }
                } else if (status == "LEFT" || status == "CLOSED") {
                    // Definitive leave — clear immediately
                    disconnectTimeoutJob?.cancel()
                    if (room.hostPuid == local) {
                        _currentRoom.value = room.copy(guestPuid = null, guestName = null, isGuestReady = false)
                        _roomChat.value = _roomChat.value + EosChatMessage(
                            senderName = "System",
                            senderPuid = "SYSTEM",
                            text = "Соперник покинул лобби"
                        )
                    } else if (memberPuid == room.hostPuid) {
                        _currentRoom.value = null
                        _roomChat.value = emptyList()
                    }
                } else if (status == "DISCONNECTED") {
                    // Transient — give 8s grace period before wiping
                    disconnectTimeoutJob?.cancel()
                    disconnectTimeoutJob = coroutineScope.launch {
                        Log.w(TAG, "Peer $memberPuid disconnected, waiting 8s for reconnect...")
                        delay(8000)
                        val curRoom = _currentRoom.value ?: return@launch
                        if (curRoom.hostPuid == local && curRoom.guestPuid == memberPuid) {
                            _currentRoom.value = curRoom.copy(guestPuid = null, guestName = null, isGuestReady = false)
                            _roomChat.value = _roomChat.value + EosChatMessage(
                                senderName = "System",
                                senderPuid = "SYSTEM",
                                text = "Соперник отключился (таймаут)"
                            )
                        } else if (memberPuid == curRoom.hostPuid) {
                            _currentRoom.value = null
                            _roomChat.value = emptyList()
                        }
                    }
                }
            }
        })
    }

    private fun startSearchLoop() {
        searchLoopJob?.cancel()
        searchLoopJob = coroutineScope.launch {
            while (isActive) {
                if (_isLoggedIn.value && _currentRoom.value == null) {
                    refreshRooms()
                }
                delay(4000)
            }
        }
    }

    fun refreshRooms(onComplete: (Boolean) -> Unit = {}) {
        if (!_isSdkReady.value || !_isLoggedIn.value) {
            onComplete(false)
            return
        }

        EosBridge.searchLobbies(object : EosSearchCallback {
            override fun onSearchResult(success: Boolean, roomsJson: String?) {
                if (success && !roomsJson.isNullOrBlank()) {
                    try {
                        val jsonArr = JSONArray(roomsJson)
                        val list = mutableListOf<EosRoom>()
                        for (i in 0 until jsonArr.length()) {
                            val obj = jsonArr.getJSONObject(i)
                            val rId = obj.getString("id")
                            val rName = obj.optString("name", "EOS Room")
                            val hPuid = obj.optString("hostPuid", "")
                            val hName = obj.optString("hostName", "Host")
                            val hTier = obj.optString("hostTier", "Bronze")
                            val bet = obj.optInt("bet", 0)
                            val code = obj.optString("code", "")
                            list.add(
                                EosRoom(
                                    id = rId,
                                    name = rName,
                                    hostPuid = hPuid,
                                    hostName = hName,
                                    hostTier = hTier,
                                    bet = bet,
                                    code = code,
                                    status = "waiting"
                                )
                            )
                        }
                        _availableRooms.value = list
                        onComplete(true)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error parsing search results JSON: ${e.message}")
                        onComplete(false)
                    }
                } else {
                    _availableRooms.value = emptyList()
                    onComplete(false)
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

        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val shortCode = (1..6).map { chars.random() }.joinToString("")

        EosBridge.createLobby(
            roomName = name,
            bet = bet,
            isPrivate = isPrivate,
            shortCode = shortCode,
            hostName = _localPlayerName.value,
            hostTier = _localPlayerTier.value,
            callback = object : EosLobbyCallback {
                override fun onLobbyResult(
                    success: Boolean,
                    lobbyId: String?,
                    hostPuid: String?,
                    roomName: String?,
                    hostName: String?,
                    hostTier: String?,
                    bet: Int
                ) {
                    if (success && lobbyId != null) {
                        val newRoom = EosRoom(
                            id = lobbyId,
                            name = name,
                            hostPuid = puid,
                            hostName = _localPlayerName.value,
                            hostTier = _localPlayerTier.value,
                            bet = bet,
                            isPrivate = isPrivate,
                            code = shortCode,
                            isHostReady = true,
                            isGuestReady = false,
                            status = "waiting"
                        )
                        _currentRoom.value = newRoom
                        _roomChat.value = listOf(
                            EosChatMessage(
                                senderName = "System",
                                senderPuid = "SYSTEM",
                                text = "Комната создана! Код: $shortCode. Ожидание соперника."
                            )
                        )
                        onComplete(true, lobbyId)
                    } else {
                        onComplete(false, "Failed to create EOS Lobby")
                    }
                }
            }
        )
    }

    fun joinRoom(
        roomInput: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val puid = _localPuid.value
        if (puid == null) {
            onComplete(false, "Not logged in to EOS")
            return
        }

        val clean = roomInput.trim()
        if (clean.isBlank()) {
            onComplete(false, "Room ID / Code cannot be blank")
            return
        }

        // 1. Check local list first
        val matchingRoom = _availableRooms.value.firstOrNull {
            it.code.equals(clean, ignoreCase = true) || it.id.endsWith(clean, ignoreCase = true)
        }

        if (matchingRoom != null) {
            executeJoinLobby(matchingRoom.id, clean, puid, onComplete)
        } else if (clean.length in 4..8 && !clean.contains("-")) {
            // 2. Query Epic Cloud Search Index directly by CODE (works across cities & private rooms)
            Log.i(TAG, "Searching Epic Cloud for room code: $clean")
            EosBridge.searchLobbyByCode(clean.uppercase(), object : EosSearchCallback {
                override fun onSearchResult(success: Boolean, roomsJson: String?) {
                    if (success && !roomsJson.isNullOrBlank()) {
                        try {
                            val arr = JSONArray(roomsJson)
                            if (arr.length() > 0) {
                                val obj = arr.getJSONObject(0)
                                val realLobbyId = obj.getString("id")
                                Log.i(TAG, "Found Epic lobby $realLobbyId for code $clean")
                                executeJoinLobby(realLobbyId, clean, puid, onComplete)
                                return
                            }
                        } catch (e: Throwable) {
                            Log.e(TAG, "Error parsing search by code result: ${e.message}")
                        }
                    }
                    onComplete(false, "Комната с кодом $clean не найдена")
                }
            })
        } else {
            // 3. Direct UUID join
            executeJoinLobby(clean.lowercase(), clean, puid, onComplete)
        }
    }

    private fun executeJoinLobby(
        targetLobbyId: String,
        clean: String,
        puid: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        EosBridge.joinLobby(targetLobbyId, object : EosLobbyCallback {
            override fun onLobbyResult(
                success: Boolean,
                lobbyId: String?,
                hostPuid: String?,
                roomName: String?,
                hostName: String?,
                hostTier: String?,
                bet: Int
            ) {
                if (success && lobbyId != null && hostPuid != null && hostPuid.isNotBlank()) {
                    val joinedRoom = EosRoom(
                        id = lobbyId,
                        name = roomName ?: "EOS Room",
                        hostPuid = hostPuid,
                        hostName = hostName ?: "Host",
                        hostTier = hostTier ?: "Bronze",
                        guestPuid = puid,
                        guestName = _localPlayerName.value,
                        guestTier = _localPlayerTier.value,
                        bet = bet,
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

                    // Launch persistent P2P join request handshake across WAN/Relay
                    startJoinHandshake(hostPuid, puid)

                    onComplete(true, lobbyId)
                } else {
                    onComplete(false, "Не удалось подключиться к лобби $clean")
                }
            }
        })
    }

    private fun startJoinHandshake(hostPuid: String, guestPuid: String) {
        joinHandshakeJob?.cancel()
        joinHandshakeJob = coroutineScope.launch {
            val joinPayload = JSONObject().apply {
                put("type", "JOIN_REQUEST")
                put("guestPuid", guestPuid)
                put("guestName", _localPlayerName.value)
                put("guestTier", _localPlayerTier.value)
            }.toString()

            // Over WAN/Relay across cities, TURN allocation takes 2-5s. Attempt up to 25 times (~11s).
            var attempts = 0
            while (isActive && attempts < 25) {
                attempts++
                EosBridge.sendPacket(hostPuid, EosConstants.P2P_SOCKET_NAME, joinPayload.toByteArray(Charsets.UTF_8))
                delay(450)
                // If host acknowledged and sent accepted payload, stop handshake
                if (_currentRoom.value?.hostName != "Host" && _currentRoom.value?.hostName != null) {
                    Log.i(TAG, "Join handshake completed successfully with host $hostPuid")
                    break
                }
            }
        }
    }

    fun leaveRoom() {
        val room = _currentRoom.value
        val puid = _localPuid.value ?: ""

        joinHandshakeJob?.cancel()

        if (room != null) {
            val leavePayload = JSONObject().apply {
                put("type", "LEAVE_ROOM")
                put("senderPuid", puid)
            }.toString()
            broadcastP2p(leavePayload)
            EosBridge.leaveLobby(room.id)
        }

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

    fun broadcastP2p(jsonPayload: String, channel: Int = 0, isReliable: Boolean = true) {
        val room = _currentRoom.value
        val myPuid = _localPuid.value ?: return
        val targetPuid = if (room != null) {
            if (myPuid == room.hostPuid) room.guestPuid else room.hostPuid
        } else null

        if (targetPuid != null && targetPuid.isNotBlank() && targetPuid.length >= 16) {
            EosBridge.sendPacket(targetPuid, EosConstants.P2P_SOCKET_NAME, jsonPayload.toByteArray(Charsets.UTF_8), channel, isReliable)
        }
    }

    private fun startPacketPolling() {
        packetPollJob?.cancel()
        packetPollJob = coroutineScope.launch {
            while (isActive) {
                try {
                    // Drain ALL pending packets per tick to avoid backlog lag
                    var drained = 0
                    while (drained < 200) { // safety cap
                        val packet = EosBridge.receivePacket(EosConstants.P2P_SOCKET_NAME)
                        if (packet == null || packet.isEmpty()) break
                        drained++
                        // Any packet from peer means they're alive — cancel disconnect timeout
                        disconnectTimeoutJob?.cancel()
                        disconnectTimeoutJob = null
                        handleIncomingPacket(packet)
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Error receiving packet: ${e.message}")
                }
                delay(15)
            }
        }
    }

    private fun handleIncomingPacket(data: ByteArray) {
        try {
            val jsonStr = String(data, Charsets.UTF_8)
            val json = JSONObject(jsonStr)
            val type = json.optString("type")

            // Forward to battle packet listener
            battlePacketListener?.invoke(json)

            if (type == "BATTLE_PING") {
                val t = json.optLong("t")
                val pong = JSONObject().apply {
                    put("type", "BATTLE_PONG")
                    put("t", t)
                }.toString()
                broadcastP2p(pong, channel = 1, isReliable = false)
                return
            } else if (type == "BATTLE_PONG") {
                val t = json.optLong("t")
                if (t > 0) {
                    val rtt = (System.currentTimeMillis() - t).toInt().coerceIn(1, 999)
                    _p2pPingMs.value = rtt
                }
                return
            }

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
                    joinHandshakeJob?.cancel()
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
                    val rawCount = json.optInt("count", 0)
                    val count = rawCount.coerceIn(0, 4)
                    if (count > 0) {
                        _pendingGarbage.value = (_pendingGarbage.value + count).coerceAtMost(20)
                    }
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
        searchLoopJob?.cancel()
        joinHandshakeJob?.cancel()
        EosBridge.shutdown()
        isInitialized.set(false)
    }
}
