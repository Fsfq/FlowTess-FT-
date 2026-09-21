package com.example.game

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Lightweight, isolated Tetris mini-engine for controls customization preview & sandbox testing.
 * Runs independently without affecting main game stats, high scores, coins or saved games.
 */
class ControlsSandboxEngine(private val scope: CoroutineScope) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var bag = mutableListOf<Tetromino>()
    private var loopJob: Job? = null

    init {
        resetGame()
    }

    fun start() {
        if (loopJob?.isActive == true) return
        loopJob = scope.launch {
            while (isActive) {
                delay(700L)
                tick()
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    fun resetGame() {
        bag.clear()
        val nexts = List(3) { getNextPieceFromBag() }
        val current = getNextPieceFromBag()
        _gameState.value = GameState(
            grid = List(22) { IntArray(10) },
            currentPiece = current,
            currentPos = Position(4, 2),
            nextPieces = nexts,
            gameMode = GameMode.RELAX
        )
    }

    fun clearBoard() {
        _gameState.update { it.copy(grid = List(22) { IntArray(10) }) }
    }

    private fun getNextPieceFromBag(): Tetromino {
        if (bag.isEmpty()) {
            bag.addAll(STANDARD_SHAPES)
            bag.shuffle()
        }
        return bag.removeAt(0)
    }

    fun tick() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        val nextPos = state.currentPos.copy(y = state.currentPos.y + 1)
        if (isValidMove(nextPos, piece, state.grid)) {
            _gameState.update { it.copy(currentPos = nextPos) }
        } else {
            lockPiece()
        }
    }

    fun moveLeft() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        val nextPos = state.currentPos.copy(x = state.currentPos.x - 1)
        if (isValidMove(nextPos, piece, state.grid)) {
            _gameState.update { it.copy(currentPos = nextPos) }
        }
    }

    fun moveRight() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        val nextPos = state.currentPos.copy(x = state.currentPos.x + 1)
        if (isValidMove(nextPos, piece, state.grid)) {
            _gameState.update { it.copy(currentPos = nextPos) }
        }
    }

    fun softDrop() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        val nextPos = state.currentPos.copy(y = state.currentPos.y + 1)
        if (isValidMove(nextPos, piece, state.grid)) {
            _gameState.update { it.copy(currentPos = nextPos) }
        } else {
            lockPiece()
        }
    }

    fun hardDrop() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        var dropY = state.currentPos.y
        while (isValidMove(state.currentPos.copy(y = dropY + 1), piece, state.grid)) {
            dropY++
        }
        _gameState.update { it.copy(currentPos = it.currentPos.copy(y = dropY)) }
        lockPiece()
    }

    fun rotate() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        if (piece.colorIndex == 4) return // O-квадрат не вращается

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
            _gameState.update { it.copy(currentPiece = rotatedPiece) }
        } else {
            val kicks = listOf(
                Position(-1, 0), Position(1, 0),
                Position(-2, 0), Position(2, 0),
                Position(0, -1)
            )
            val successfulKick = kicks.firstOrNull { kick ->
                isValidMove(state.currentPos.copy(x = state.currentPos.x + kick.x, y = state.currentPos.y + kick.y), rotatedPiece, state.grid)
            }
            if (successfulKick != null) {
                _gameState.update {
                    it.copy(
                        currentPiece = rotatedPiece,
                        currentPos = it.currentPos.copy(
                            x = it.currentPos.x + successfulKick.x,
                            y = it.currentPos.y + successfulKick.y
                        )
                    )
                }
            }
        }
    }

    fun hold() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        if (state.hasHeldThisTurn) return

        val originalPiece = STANDARD_SHAPES.firstOrNull { it.colorIndex == piece.colorIndex } ?: piece
        if (state.holdPiece == null) {
            val next = if (state.nextPieces.isNotEmpty()) state.nextPieces.first() else getNextPieceFromBag()
            val newNexts = if (state.nextPieces.size > 1) state.nextPieces.drop(1) + getNextPieceFromBag() else listOf(getNextPieceFromBag())
            _gameState.update {
                it.copy(
                    holdPiece = originalPiece,
                    currentPiece = next,
                    currentPos = Position(4, 2),
                    nextPieces = newNexts,
                    hasHeldThisTurn = true
                )
            }
        } else {
            val swapped = state.holdPiece
            _gameState.update {
                it.copy(
                    holdPiece = originalPiece,
                    currentPiece = swapped,
                    currentPos = Position(4, 2),
                    hasHeldThisTurn = true
                )
            }
        }
    }

    private fun isValidMove(pos: Position, piece: Tetromino, grid: List<IntArray>): Boolean {
        for (p in piece.shape) {
            val nx = pos.x + p.x
            val ny = pos.y + p.y
            if (nx !in 0..9) return false
            if (ny >= grid.size) return false
            if (ny >= 0 && nx >= 0 && nx < grid[ny].size && grid[ny][nx] != 0) return false
        }
        return true
    }

    private fun lockPiece() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        val grid = state.grid.map { it.clone() }

        for (p in piece.shape) {
            val nx = state.currentPos.x + p.x
            val ny = state.currentPos.y + p.y
            if (ny in 0 until grid.size && nx in 0 until grid[ny].size) {
                grid[ny][nx] = piece.colorIndex
            }
        }

        // Line clear
        val newGrid = grid.filter { row -> row.any { it == 0 } }.toMutableList()
        val cleared = maxOf(0, 22 - newGrid.size)
        for (i in 0 until cleared) {
            newGrid.add(0, IntArray(10))
        }

        // Auto-clean danger zone at top (rows 0..3) so sandbox runs forever
        val hitTop = (0..3).any { r -> newGrid[r].any { it != 0 } }
        val finalGrid = if (hitTop) {
            List(22) { IntArray(10) }
        } else {
            newGrid
        }

        val next = if (state.nextPieces.isNotEmpty()) state.nextPieces.first() else getNextPieceFromBag()
        val newNexts = if (state.nextPieces.size > 1) {
            state.nextPieces.drop(1) + getNextPieceFromBag()
        } else {
            listOf(getNextPieceFromBag(), getNextPieceFromBag(), getNextPieceFromBag())
        }

        _gameState.update {
            it.copy(
                grid = finalGrid,
                currentPiece = next,
                currentPos = Position(4, 2),
                nextPieces = newNexts,
                hasHeldThisTurn = false,
                lines = it.lines + cleared
            )
        }
    }
}
