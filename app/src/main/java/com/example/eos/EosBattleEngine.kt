package com.example.eos

import androidx.compose.ui.graphics.Color
import com.example.game.Colors
import com.example.game.Position
import com.example.game.STANDARD_SHAPES
import com.example.game.Tetromino
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

enum class EosBattleStatus {
    IDLE,
    COUNTDOWN,
    PLAYING,
    ROUND_OVER,
    MATCH_FINISHED
}

data class EosLocalBattleState(
    val grid: List<IntArray> = List(22) { IntArray(10) },
    val currentPiece: Tetromino? = null,
    val currentPos: Position = Position(4, 1),
    val holdPiece: Tetromino? = null,
    val nextPieces: List<Tetromino> = emptyList(),
    val score: Int = 0,
    val lines: Int = 0,
    val combo: Int = 0,
    val isGameOver: Boolean = false,
    val hasHeldThisTurn: Boolean = false,
    val pendingGarbage: Int = 0,
    val attackBanner: String? = null
)

data class EosRemoteOpponentState(
    val puid: String = "",
    val name: String = "Opponent",
    val tier: String = "Bronze",
    val grid: List<IntArray> = List(20) { IntArray(10) },
    val score: Int = 0,
    val lines: Int = 0,
    val combo: Int = 0,
    val isGameOver: Boolean = false,
    val isToppedOut: Boolean = false
)

data class EosBattleMatchState(
    val status: EosBattleStatus = EosBattleStatus.IDLE,
    val countdownSeconds: Int = 3,
    val currentRound: Int = 1,
    val localRoundWins: Int = 0,
    val opponentRoundWins: Int = 0,
    val maxWinsNeeded: Int = 2,
    val isMatchWinner: Boolean? = null,
    val coinsWon: Int = 0,
    val ratingDelta: Int = 0,
    val rematchRequestedByLocal: Boolean = false,
    val rematchRequestedByOpponent: Boolean = false,
    val finishReason: String = "",
    val opponentLeft: Boolean = false
)

object EosBattleEngine {
    private const val TAG = "EosBattleEngine"

    private val engineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var gameLoopJob: Job? = null
    private var syncLoopJob: Job? = null
    private var countdownJob: Job? = null
    private var lockDelayJob: Job? = null
    private var pingLoopJob: Job? = null
    private var nextRoundJob: Job? = null
    private var roomObserverJob: Job? = null

    private val _localState = MutableStateFlow(EosLocalBattleState())
    val localState: StateFlow<EosLocalBattleState> = _localState.asStateFlow()

    private val _opponentState = MutableStateFlow(EosRemoteOpponentState())
    val opponentState: StateFlow<EosRemoteOpponentState> = _opponentState.asStateFlow()

    private val _matchState = MutableStateFlow(EosBattleMatchState())
    val matchState: StateFlow<EosBattleMatchState> = _matchState.asStateFlow()

    private var localPuid: String = ""
    private var localName: String = "Player"
    private var localTier: String = "Bronze"
    private var opponentPuid: String = ""
    private var roomBet: Int = 0

    private val bag = mutableListOf<Tetromino>()
    private var lockResets = 0

    // Audio & reward callbacks
    var onAudioTrigger: ((String) -> Unit)? = null
    var onMatchCompleted: ((isWinner: Boolean, localScore: Int, oppScore: Int, bet: Int) -> Unit)? = null

    fun initializeMatch(
        room: EosRoom,
        myPuid: String,
        myName: String,
        myTier: String
    ) {
        stopAllJobs()

        localPuid = myPuid
        localName = myName
        localTier = myTier
        roomBet = room.bet

        val isHost = (room.hostPuid == myPuid)
        opponentPuid = if (isHost) (room.guestPuid ?: "") else room.hostPuid
        val oppName = if (isHost) (room.guestName ?: "Guest") else room.hostName
        val oppTier = if (isHost) (room.guestTier ?: "Bronze") else room.hostTier

        _opponentState.value = EosRemoteOpponentState(
            puid = opponentPuid,
            name = oppName,
            tier = oppTier,
            grid = List(20) { IntArray(10) }
        )

        _matchState.value = EosBattleMatchState(
            status = EosBattleStatus.COUNTDOWN,
            countdownSeconds = 3,
            currentRound = 1,
            localRoundWins = 0,
            opponentRoundWins = 0,
            maxWinsNeeded = 2,
            isMatchWinner = null,
            opponentLeft = false
        )

        EosManager.battlePacketListener = { json ->
            handleIncomingBattlePacket(json)
        }

        roomObserverJob?.cancel()
        roomObserverJob = engineScope.launch {
            EosManager.currentRoom.collect { curRoom ->
                val oppMissing = if (isHost) (curRoom?.guestPuid == null) else (curRoom == null)
                if (oppMissing) {
                    if (_matchState.value.status != EosBattleStatus.IDLE && _matchState.value.status != EosBattleStatus.MATCH_FINISHED) {
                        finalizeMatch(isLocalWinner = true, reason = "FORFEIT_WIN")
                    } else if (_matchState.value.status == EosBattleStatus.MATCH_FINISHED) {
                        _matchState.update { it.copy(opponentLeft = true, rematchRequestedByOpponent = false) }
                    }
                }
            }
        }

        startPingLoop()
        startCountdownAndRound()
    }

    private fun startCountdownAndRound() {
        if (_matchState.value.status == EosBattleStatus.MATCH_FINISHED || _matchState.value.opponentLeft) return

        countdownJob?.cancel()
        countdownJob = engineScope.launch {
            _matchState.update { it.copy(status = EosBattleStatus.COUNTDOWN, countdownSeconds = 3) }
            resetLocalBoard()

            for (sec in 3 downTo 1) {
                if (_matchState.value.status == EosBattleStatus.MATCH_FINISHED || _matchState.value.opponentLeft) return@launch
                _matchState.update { it.copy(countdownSeconds = sec) }
                onAudioTrigger?.invoke("select")
                delay(1000)
            }

            if (_matchState.value.status == EosBattleStatus.MATCH_FINISHED || _matchState.value.opponentLeft) return@launch

            _matchState.update { it.copy(status = EosBattleStatus.PLAYING, countdownSeconds = 0) }
            onAudioTrigger?.invoke("start")

            startActiveGameLoops()
        }
    }

    private fun resetLocalBoard() {
        bag.clear()
        lastSentGridCsv = ""
        lastSentScore = -1
        lastSentLines = -1
        val nextList = List(3) { drawFromBag() }
        val firstPiece = drawFromBag()

        _localState.value = EosLocalBattleState(
            grid = List(22) { IntArray(10) },
            currentPiece = firstPiece,
            currentPos = Position(4, 1),
            holdPiece = null,
            nextPieces = nextList,
            score = 0,
            lines = 0,
            combo = 0,
            isGameOver = false,
            hasHeldThisTurn = false,
            pendingGarbage = 0,
            attackBanner = null
        )

        _opponentState.update {
            it.copy(
                isGameOver = false,
                isToppedOut = false,
                grid = List(20) { IntArray(10) }
            )
        }
    }

    private fun drawFromBag(): Tetromino {
        if (bag.isEmpty()) {
            bag.addAll(STANDARD_SHAPES)
            bag.shuffle()
        }
        return if (bag.isNotEmpty()) bag.removeAt(0) else STANDARD_SHAPES[0]
    }

    private fun startActiveGameLoops() {
        gameLoopJob?.cancel()
        gameLoopJob = engineScope.launch {
            while (isActive && _matchState.value.status == EosBattleStatus.PLAYING) {
                val dropDelay = calculateGravitySpeed()
                delay(dropDelay)

                if (_matchState.value.status != EosBattleStatus.PLAYING) break
                val state = _localState.value
                if (state.isGameOver) break

                tick()
            }
        }

        syncLoopJob?.cancel()
        syncLoopJob = engineScope.launch {
            while (isActive && _matchState.value.status != EosBattleStatus.IDLE) {
                sendLiveGridSnapshot()
                delay(160)
            }
        }
    }

    private fun startPingLoop() {
        pingLoopJob?.cancel()
        pingLoopJob = engineScope.launch {
            while (isActive) {
                val pingJson = JSONObject().apply {
                    put("type", "BATTLE_PING")
                    put("t", System.currentTimeMillis())
                }.toString()
                EosManager.broadcastP2p(pingJson, channel = 1, isReliable = false)
                delay(2000)
            }
        }
    }

    private fun calculateGravitySpeed(): Long {
        val lines = _localState.value.lines
        val level = 1 + lines / 10
        return when {
            level <= 2 -> 650L
            level <= 5 -> 520L
            level <= 8 -> 400L
            level <= 12 -> 300L
            else -> 220L
        }
    }

    // ── MOVEMENT & CONTROLS ──

    fun moveLeft() {
        val state = _localState.value
        if (_matchState.value.status != EosBattleStatus.PLAYING || state.isGameOver || state.currentPiece == null) return

        val nextPos = state.currentPos.copy(x = state.currentPos.x - 1)
        if (isValidMove(nextPos, state.currentPiece, state.grid)) {
            _localState.update { it.copy(currentPos = nextPos) }
            onAudioTrigger?.invoke("move")
            resetLockDelayIfOnGround()
            sendLiveGridSnapshot()
        }
    }

    fun moveRight() {
        val state = _localState.value
        if (_matchState.value.status != EosBattleStatus.PLAYING || state.isGameOver || state.currentPiece == null) return

        val nextPos = state.currentPos.copy(x = state.currentPos.x + 1)
        if (isValidMove(nextPos, state.currentPiece, state.grid)) {
            _localState.update { it.copy(currentPos = nextPos) }
            onAudioTrigger?.invoke("move")
            resetLockDelayIfOnGround()
            sendLiveGridSnapshot()
        }
    }

    fun softDrop() {
        val state = _localState.value
        if (_matchState.value.status != EosBattleStatus.PLAYING || state.isGameOver || state.currentPiece == null) return

        val nextPos = state.currentPos.copy(y = state.currentPos.y + 1)
        if (isValidMove(nextPos, state.currentPiece, state.grid)) {
            _localState.update { it.copy(currentPos = nextPos, score = it.score + 1) }
            sendLiveGridSnapshot()
        } else {
            lockPiece()
        }
    }

    fun hardDrop() {
        val state = _localState.value
        if (_matchState.value.status != EosBattleStatus.PLAYING || state.isGameOver || state.currentPiece == null) return

        var newY = state.currentPos.y
        while (isValidMove(Position(state.currentPos.x, newY + 1), state.currentPiece, state.grid)) {
            newY++
        }
        val dropDistance = newY - state.currentPos.y
        _localState.update {
            it.copy(
                currentPos = it.currentPos.copy(y = newY),
                score = it.score + (dropDistance * 2)
            )
        }
        onAudioTrigger?.invoke("hard_drop")
        lockPiece()
    }

    fun rotate() {
        val state = _localState.value
        val piece = state.currentPiece ?: return
        if (_matchState.value.status != EosBattleStatus.PLAYING || state.isGameOver) return

        // O-shape (colorIndex 4) doesn't rotate
        if (piece.colorIndex == 4) return

        val isTwoState = piece.colorIndex in listOf(1, 5, 7)
        val nextRotationState = if (isTwoState) {
            if (piece.rotationState == 0) 1 else 0
        } else {
            (piece.rotationState + 1) % 4
        }

        val rotatedShape = if (isTwoState && piece.rotationState == 1) {
            piece.shape.map { p ->
                val rx = p.x - piece.pivot.x
                val ry = p.y - piece.pivot.y
                Position(piece.pivot.x + ry, piece.pivot.y - rx)
            }
        } else {
            piece.shape.map { p ->
                val rx = p.x - piece.pivot.x
                val ry = p.y - piece.pivot.y
                Position(piece.pivot.x - ry, piece.pivot.y + rx)
            }
        }
        val rotatedPiece = piece.copy(shape = rotatedShape, rotationState = nextRotationState)

        if (isValidMove(state.currentPos, rotatedPiece, state.grid)) {
            _localState.update { it.copy(currentPiece = rotatedPiece) }
            onAudioTrigger?.invoke("rotate")
            resetLockDelayIfOnGround()
            sendLiveGridSnapshot()
        } else {
            val kicks = listOf(
                Position(-1, 0), Position(1, 0),
                Position(-2, 0), Position(2, 0),
                Position(0, -1), Position(-1, -1), Position(1, -1)
            )
            val successfulKick = kicks.firstOrNull { kick ->
                isValidMove(state.currentPos.copy(x = state.currentPos.x + kick.x, y = state.currentPos.y + kick.y), rotatedPiece, state.grid)
            }
            if (successfulKick != null) {
                _localState.update {
                    it.copy(
                        currentPiece = rotatedPiece,
                        currentPos = it.currentPos.copy(
                            x = it.currentPos.x + successfulKick.x,
                            y = it.currentPos.y + successfulKick.y
                        )
                    )
                }
                onAudioTrigger?.invoke("rotate")
                resetLockDelayIfOnGround()
                sendLiveGridSnapshot()
            }
        }
    }

    fun hold() {
        val state = _localState.value
        if (_matchState.value.status != EosBattleStatus.PLAYING || state.isGameOver || state.hasHeldThisTurn || state.currentPiece == null) return

        val nextP = state.holdPiece ?: state.nextPieces.firstOrNull() ?: drawFromBag()
        val remainingNext = if (state.holdPiece == null) {
            if (state.nextPieces.isNotEmpty()) state.nextPieces.drop(1) + drawFromBag() else List(3) { drawFromBag() }
        } else {
            state.nextPieces
        }

        val originalPiece = STANDARD_SHAPES.firstOrNull { it.colorIndex == state.currentPiece.colorIndex } ?: state.currentPiece

        _localState.update {
            it.copy(
                currentPiece = nextP,
                currentPos = Position(4, 1),
                holdPiece = originalPiece,
                nextPieces = remainingNext,
                hasHeldThisTurn = true
            )
        }
        onAudioTrigger?.invoke("hold")
        sendLiveGridSnapshot()
    }

    private fun tick() {
        val state = _localState.value
        val piece = state.currentPiece ?: return
        val nextPos = state.currentPos.copy(y = state.currentPos.y + 1)

        if (isValidMove(nextPos, piece, state.grid)) {
            _localState.update { it.copy(currentPos = nextPos) }
            lockDelayJob?.cancel()
        } else {
            // Reached ground, lock
            lockPiece()
        }
    }

    private fun resetLockDelayIfOnGround() {
        val state = _localState.value
        val piece = state.currentPiece ?: return
        if (!isValidMove(state.currentPos.copy(y = state.currentPos.y + 1), piece, state.grid)) {
            if (lockResets < 15) {
                lockResets++
                lockDelayJob?.cancel()
                lockDelayJob = engineScope.launch {
                    delay(500)
                    lockPiece()
                }
            }
        }
    }

    private fun lockPiece() {
        lockDelayJob?.cancel()
        lockResets = 0

        val state = _localState.value
        val piece = state.currentPiece ?: return
        val grid = state.grid.map { it.clone() }

        // 1. Stamp piece onto board
        for (p in piece.shape) {
            val nx = state.currentPos.x + p.x
            val ny = state.currentPos.y + p.y
            if (ny in 0 until 22 && nx in 0 until 10) {
                grid[ny][nx] = piece.colorIndex
            }
        }

        // 2. Clear full lines
        val remainingRows = grid.filter { row -> row.any { it == 0 } }.toMutableList()
        val clearedLines = 22 - remainingRows.size
        for (i in 0 until clearedLines) {
            remainingRows.add(0, IntArray(10))
        }

        // 3. Process combo and scoring
        val newCombo = if (clearedLines > 0) state.combo + 1 else 0
        val basePoints = when (clearedLines) {
            1 -> 50
            2 -> 150
            3 -> 250
            4 -> 400
            else -> 0
        }
        val comboBonus = if (newCombo > 1) (newCombo * 25) else 0
        val addedScore = (basePoints + comboBonus) * _matchState.value.currentRound

        // 4. Attack calculation & Garbage Cancellation
        var attackLines = when (clearedLines) {
            2 -> 1
            3 -> 2
            4 -> 4
            else -> 0
        }
        if (newCombo >= 2) {
            attackLines += (newCombo - 1)
        }

        var curPendingGarbage = state.pendingGarbage
        var bannerText: String? = null

        if (clearedLines > 0) {
            when (clearedLines) {
                1 -> onAudioTrigger?.invoke("clear_1")
                2 -> {
                    onAudioTrigger?.invoke("clear_2")
                    bannerText = "DOUBLE ATTACK! +1"
                }
                3 -> {
                    onAudioTrigger?.invoke("clear_3")
                    bannerText = "TRIPLE ATTACK! +2"
                }
                4 -> {
                    onAudioTrigger?.invoke("clear_4")
                    bannerText = "TETRIS ATTACK! +4"
                }
            }
            if (newCombo >= 2) {
                bannerText = (bannerText ?: "") + " COMBO x$newCombo!"
            }

            // Counter-cancel incoming garbage
            if (curPendingGarbage > 0) {
                if (attackLines >= curPendingGarbage) {
                    attackLines -= curPendingGarbage
                    curPendingGarbage = 0
                } else {
                    curPendingGarbage -= attackLines
                    attackLines = 0
                }
            }

            if (attackLines > 0) {
                sendGarbageAttack(attackLines)
            }
        } else {
            onAudioTrigger?.invoke("land")
        }

        // 5. Apply remaining pending garbage to local board
        val finalGrid = remainingRows.toMutableList()
        if (curPendingGarbage > 0) {
            for (g in 0 until curPendingGarbage) {
                if (finalGrid.isNotEmpty()) {
                    finalGrid.removeAt(0)
                }
                val garbageRow = IntArray(10) { (1..7).random() }
                garbageRow[(0..9).random()] = 0 // hole
                finalGrid.add(garbageRow)
            }
            curPendingGarbage = 0
            onAudioTrigger?.invoke("fall")
        }

        // 6. Death / Top-out check
        val nextPiece = state.nextPieces.firstOrNull() ?: drawFromBag()
        val blocksInTopRows = finalGrid[0].any { it != 0 } || finalGrid[1].any { it != 0 }
        val cannotSpawn = !isValidMove(Position(4, 1), nextPiece, finalGrid)

        val isOver = blocksInTopRows || cannotSpawn

        val updatedNext = if (state.nextPieces.isNotEmpty()) {
            state.nextPieces.drop(1) + drawFromBag()
        } else {
            List(3) { drawFromBag() }
        }

        _localState.update {
            it.copy(
                grid = finalGrid,
                currentPiece = nextPiece,
                currentPos = Position(4, 1),
                nextPieces = updatedNext,
                score = it.score + addedScore,
                lines = it.lines + clearedLines,
                combo = newCombo,
                isGameOver = isOver,
                hasHeldThisTurn = false,
                pendingGarbage = curPendingGarbage,
                attackBanner = bannerText
            )
        }

        sendLiveGridSnapshot()

        if (isOver) {
            onLocalPlayerToppedOut()
        }
    }

    private fun onLocalPlayerToppedOut() {
        onAudioTrigger?.invoke("gameover")
        val match = _matchState.value

        // Notify opponent that local topped out
        val payload = JSONObject().apply {
            put("type", "BATTLE_ROUND_OVER")
            put("round", match.currentRound)
            put("toppedOutPuid", localPuid)
        }.toString()
        EosManager.broadcastP2p(payload)

        handleRoundFinished(winnerIsLocal = false)
    }

    private fun handleRoundFinished(winnerIsLocal: Boolean) {
        gameLoopJob?.cancel()
        lockDelayJob?.cancel()

        val curMatch = _matchState.value
        val newLocalWins = if (winnerIsLocal) curMatch.localRoundWins + 1 else curMatch.localRoundWins
        val newOppWins = if (!winnerIsLocal) curMatch.opponentRoundWins + 1 else curMatch.opponentRoundWins

        if (newLocalWins >= curMatch.maxWinsNeeded) {
            // Local wins match!
            finalizeMatch(isLocalWinner = true, reason = "VICTORY")
        } else if (newOppWins >= curMatch.maxWinsNeeded) {
            // Opponent wins match
            finalizeMatch(isLocalWinner = false, reason = "DEFEAT")
        } else {
            // Round over, advance to next round!
            _matchState.update {
                it.copy(
                    status = EosBattleStatus.ROUND_OVER,
                    currentRound = it.currentRound + 1,
                    localRoundWins = newLocalWins,
                    opponentRoundWins = newOppWins
                )
            }
            nextRoundJob?.cancel()
            nextRoundJob = engineScope.launch {
                delay(2500)
                if (_matchState.value.status == EosBattleStatus.MATCH_FINISHED || _matchState.value.opponentLeft) return@launch
                startCountdownAndRound()
            }
        }
    }

    private fun finalizeMatch(isLocalWinner: Boolean, reason: String) {
        nextRoundJob?.cancel()
        countdownJob?.cancel()
        gameLoopJob?.cancel()
        lockDelayJob?.cancel()

        val isOpponentGone = (reason == "FORFEIT_WIN" || reason == "OPPONENT_LEFT")

        val localScore = _localState.value.score
        val oppScore = _opponentState.value.score

        val coinsAwarded = if (isLocalWinner) {
            val betReward = if (roomBet > 0) roomBet * 2 else 0
            val perfReward = if (localScore >= 1000) 100 else 50
            betReward + perfReward
        } else {
            0
        }
        val ratingDelta = if (isLocalWinner) +25 else -15

        _matchState.update {
            it.copy(
                status = EosBattleStatus.MATCH_FINISHED,
                isMatchWinner = isLocalWinner,
                coinsWon = coinsAwarded,
                ratingDelta = ratingDelta,
                finishReason = reason,
                opponentLeft = isOpponentGone || it.opponentLeft
            )
        }

        onMatchCompleted?.invoke(isLocalWinner, localScore, oppScore, roomBet)
    }

    // ── INCOMING EOS PACKET HANDLING ──

    private fun handleIncomingBattlePacket(json: JSONObject) {
        when (json.optString("type")) {
            "BATTLE_GRID" -> {
                val flatCsv = json.optString("grid")
                val score = json.optInt("score", 0)
                val lines = json.optInt("lines", 0)
                val combo = json.optInt("combo", 0)
                val isOver = json.optBoolean("isGameOver", false)

                if (flatCsv.isNotEmpty()) {
                    val tokens = flatCsv.split(",")
                    val parsedGrid = mutableListOf<IntArray>()
                    for (r in 0 until 20) {
                        val row = IntArray(10)
                        for (c in 0 until 10) {
                            val idx = r * 10 + c
                            row[c] = if (idx < tokens.size) tokens[idx].toIntOrNull() ?: 0 else 0
                        }
                        parsedGrid.add(row)
                    }
                    _opponentState.update {
                        it.copy(
                            grid = parsedGrid,
                            score = score,
                            lines = lines,
                            combo = combo,
                            isGameOver = isOver
                        )
                    }
                }
            }

            "BATTLE_ATTACK" -> {
                val rawCount = json.optInt("count", 0)
                val count = rawCount.coerceIn(0, 4)
                if (count > 0 && _matchState.value.status == EosBattleStatus.PLAYING) {
                    _localState.update { it.copy(pendingGarbage = (it.pendingGarbage + count).coerceAtMost(20)) }
                    onAudioTrigger?.invoke("fall")
                }
            }

            "BATTLE_ROUND_OVER" -> {
                val toppedOutPuid = json.optString("toppedOutPuid")
                if (toppedOutPuid == opponentPuid && _matchState.value.status == EosBattleStatus.PLAYING) {
                    // Opponent topped out! Local player wins round!
                    handleRoundFinished(winnerIsLocal = true)
                }
            }

            "BATTLE_REMATCH" -> {
                if (_matchState.value.opponentLeft) return
                val action = json.optString("action")
                if (action == "REQUEST") {
                    _matchState.update { it.copy(rematchRequestedByOpponent = true) }
                    if (_matchState.value.rematchRequestedByLocal) {
                        restartMatchForRematch()
                    }
                }
            }

            "LEAVE_ROOM" -> {
                val sender = json.optString("senderPuid")
                if (sender.isEmpty() || sender != localPuid) {
                    if (_matchState.value.status != EosBattleStatus.MATCH_FINISHED) {
                        // Opponent surrendered / disconnected -> Award forfeit win
                        finalizeMatch(isLocalWinner = true, reason = "FORFEIT_WIN")
                    } else {
                        // Match was already finished, record that opponent left
                        _matchState.update { it.copy(opponentLeft = true, rematchRequestedByOpponent = false) }
                    }
                }
            }
        }
    }

    fun requestRematch() {
        if (_matchState.value.opponentLeft) return

        _matchState.update { it.copy(rematchRequestedByLocal = true) }

        val payload = JSONObject().apply {
            put("type", "BATTLE_REMATCH")
            put("action", "REQUEST")
            put("senderPuid", localPuid)
        }.toString()
        EosManager.broadcastP2p(payload)

        if (_matchState.value.rematchRequestedByOpponent) {
            restartMatchForRematch()
        }
    }

    private fun restartMatchForRematch() {
        if (_matchState.value.opponentLeft) return

        _matchState.update {
            it.copy(
                status = EosBattleStatus.COUNTDOWN,
                countdownSeconds = 3,
                currentRound = 1,
                localRoundWins = 0,
                opponentRoundWins = 0,
                isMatchWinner = null,
                rematchRequestedByLocal = false,
                rematchRequestedByOpponent = false,
                finishReason = "",
                coinsWon = 0,
                ratingDelta = 0
            )
        }
        startCountdownAndRound()
    }

    private fun sendGarbageAttack(count: Int) {
        val payload = JSONObject().apply {
            put("type", "BATTLE_ATTACK")
            put("count", count)
            put("senderPuid", localPuid)
        }.toString()
        EosManager.broadcastP2p(payload)
    }

    // Frame deduplication for sendLiveGridSnapshot
    private var lastSentGridCsv: String = ""
    private var lastSentScore: Int = -1
    private var lastSentLines: Int = -1

    private fun sendLiveGridSnapshot() {
        val state = _localState.value
        // Extract 20 visible rows (rows 2..21)
        val matrix = state.grid.takeLast(20).map { it.clone() }
        val piece = state.currentPiece
        val pos = state.currentPos

        if (piece != null && !state.isGameOver) {
            for (p in piece.shape) {
                val r = pos.y + p.y - 2
                val c = pos.x + p.x
                if (r in 0 until 20 && c in 0 until 10) {
                    matrix[r][c] = piece.colorIndex
                }
            }
        }

        val csv = matrix.flatMap { it.toList() }.joinToString(",")

        // Skip if identical to last sent frame
        if (csv == lastSentGridCsv && state.score == lastSentScore && state.lines == lastSentLines) return
        lastSentGridCsv = csv
        lastSentScore = state.score
        lastSentLines = state.lines

        val payload = JSONObject().apply {
            put("type", "BATTLE_GRID")
            put("grid", csv)
            put("score", state.score)
            put("lines", state.lines)
            put("combo", state.combo)
            put("isGameOver", state.isGameOver)
        }.toString()
        EosManager.broadcastP2p(payload, channel = 1, isReliable = false)
    }

    fun getGhostY(): Int {
        val state = _localState.value
        val piece = state.currentPiece ?: return state.currentPos.y
        var ghostY = state.currentPos.y
        while (isValidMove(Position(state.currentPos.x, ghostY + 1), piece, state.grid)) {
            ghostY++
        }
        return ghostY
    }

    private fun isValidMove(pos: Position, piece: Tetromino, grid: List<IntArray>): Boolean {
        for (p in piece.shape) {
            val nx = pos.x + p.x
            val ny = pos.y + p.y
            if (nx !in 0..9) return false
            if (ny >= 22) return false
            if (ny >= 0 && nx >= 0 && nx < 10 && grid[ny][nx] != 0) return false
        }
        return true
    }

    fun forfeitMatch() {
        val payload = JSONObject().apply {
            put("type", "BATTLE_ROUND_OVER")
            put("toppedOutPuid", localPuid)
        }.toString()
        EosManager.broadcastP2p(payload)
        finalizeMatch(isLocalWinner = false, reason = "SURRENDER")
    }

    fun stopMatch() {
        stopAllJobs()
        EosManager.battlePacketListener = null
        _matchState.value = EosBattleMatchState(status = EosBattleStatus.IDLE)
        _localState.value = EosLocalBattleState()
        _opponentState.value = EosRemoteOpponentState()
    }

    private fun stopAllJobs() {
        nextRoundJob?.cancel()
        roomObserverJob?.cancel()
        gameLoopJob?.cancel()
        syncLoopJob?.cancel()
        countdownJob?.cancel()
        lockDelayJob?.cancel()
        pingLoopJob?.cancel()
    }
}
