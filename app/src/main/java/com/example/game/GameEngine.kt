package com.example.game

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

// Палитра цветов фигур — 0=прозрачный, остальные для тетромин / piece color palette
val Colors = listOf(
    Color.Transparent,
    Color(0xFF00FFFF),
    Color(0xFF0000FF),
    Color(0xFFFFA500),
    Color(0xFFFFFF00),
    Color(0xFF00FF00),
    Color(0xFF800080),
    Color(0xFFFF0000),
    Color(0xFFFF1493),
    Color(0xFF8B4513),
    Color(0xFFDDDDDD)
)

data class Position(val x: Int, val y: Int)

data class Tetromino(
    val shape: List<Position>,
    val colorIndex: Int,
    val pivot: Position = Position(0, 0)
)

val STANDARD_SHAPES = listOf(
    Tetromino(listOf(Position(-1, 0), Position(0, 0), Position(1, 0), Position(2, 0)), 1, Position(0, 0)),
    Tetromino(listOf(Position(-1, -1), Position(-1, 0), Position(0, 0), Position(1, 0)), 2, Position(0, 0)),
    Tetromino(listOf(Position(1, -1), Position(-1, 0), Position(0, 0), Position(1, 0)), 3, Position(0, 0)),
    Tetromino(listOf(Position(0, -1), Position(1, -1), Position(0, 0), Position(1, 0)), 4, Position(0, 2)),
    Tetromino(listOf(Position(0, -1), Position(1, -1), Position(-1, 0), Position(0, 0)), 5, Position(0, 0)),
    Tetromino(listOf(Position(0, -1), Position(-1, 0), Position(0, 0), Position(1, 0)), 6, Position(0, 0)),
    Tetromino(listOf(Position(-1, -1), Position(0, -1), Position(0, 0), Position(1, 0)), 7, Position(0, 0))
)

val EXTENDED_SHAPES = listOf(
    Tetromino(listOf(Position(0, -1), Position(-1, 0), Position(0, 0), Position(1, 0), Position(0, 1)), 8, Position(0, 0)),
    Tetromino(listOf(Position(-1, -1), Position(-1, 0), Position(0, 0), Position(1, 0), Position(1, -1)), 9, Position(0, 0)),
    Tetromino(listOf(Position(0, 0)), 10, Position(0, 0))
)

enum class GameMode(val code: String, val displayNameEn: String, val displayNameRu: String) {
    CLASSIC("classic", "Classic Match", "Классический"),
    EXTENDED("extended", "Extended Shapes", "Расширенный"),
    FAST_RUN("fast_run", "Hyper Blast (Lvl 10)", "Гипер-Режим (Ур 10)"),
    REVERSE_CONTROLS("reverse", "Chaos Controls", "Хаос-Управление"),
    BLOCK_BLAST("block_blast", "ZETA Arena", "ZETA Арена"),
    ZEN_FLOW("zen", "Zen Cosmic Flow", "Дзен Космо-Поток"),
    TIME_ATTACK("time_attack", "Time Attack Protocol", "Протокол Тайм-Атак"),
    PULSE_EXTREME("pulse_extreme", "Vortex Pulse Mode", "Импульсный Вихрь"),
    MIRROR_DIMENSION("mirror", "Mirror Dimension", "Зеркальный Мир"),
    PENTARY_CHAOS("penta", "Pentary Chaos", "Пента-Хаос"),
    RELAX("relax", "Relax Sandbox", "Релакс-Песочница")
}

data class GameState(
    val grid: List<IntArray> = List(22) { IntArray(10) },
    val currentPiece: Tetromino? = null,
    val currentPos: Position = Position(4, 0),
    val holdPiece: Tetromino? = null,
    val nextPieces: List<Tetromino> = emptyList(),
    val score: Int = 0,
    val lines: Int = 0,
    val level: Int = 1,
    val isGameOver: Boolean = false,
    val hasHeldThisTurn: Boolean = false,
    val isExtendedMode: Boolean = false,
    val gameMode: GameMode = GameMode.CLASSIC,
    val timeRemainingSeconds: Int = 60,
    val piecesPlaced: Int = 0,
    val tetrisesCleared: Int = 0
)

// Мотор игры — вся логика тетриса / core game engine, handles all tetris mechanics
class GameEngine {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    var lineClearChallenge: Boolean = false
    var fastDropLockSpeed: Boolean = false
    
    // 7-bag рандомайзер / 7-bag randomizer for piece distribution
    private var bag = mutableListOf<Tetromino>()
    
    // Настройки Relax-режима / Relax sandbox settings
    var relaxImmortal: Boolean = true
    var relaxBlockSet: String = "ideal" // only_i, ideal, standard, all
    
    init {
        startGame(GameMode.CLASSIC)
    }

    // Запуск/рестарт — сбрасываем сетку 22x10, генерим фигуры / start new game, reset grid
    fun startGame(mode: GameMode, startingLevel: Int = 1) {
        bag.clear()
        val isExt = mode == GameMode.EXTENDED || mode == GameMode.PENTARY_CHAOS
        val finalLevel = if (mode == GameMode.FAST_RUN) 10 else startingLevel
        _gameState.update {
            GameState(
                grid = List(22) { IntArray(10) },
                nextPieces = List(3) { nextPiece(isExt) },
                currentPiece = nextPiece(isExt),
                isExtendedMode = isExt,
                level = finalLevel,
                gameMode = mode,
                timeRemainingSeconds = if (mode == GameMode.TIME_ATTACK) 60 else 60,
                piecesPlaced = 0
            )
        }
    }

    fun restoreState(
        grid: List<IntArray>,
        currentPieceColorIndex: Int,
        currentPos: Position,
        holdPieceColorIndex: Int?,
        nextPiecesColorIndices: List<Int>,
        score: Int,
        lines: Int,
        level: Int,
        isExtendedMode: Boolean,
        isGameOver: Boolean,
        gameMode: GameMode = GameMode.CLASSIC,
        timeRemainingSeconds: Int = 60,
        piecesPlaced: Int = 0
    ) {
        val allShapes = STANDARD_SHAPES + EXTENDED_SHAPES
        val currentPiece = allShapes.firstOrNull { it.colorIndex == currentPieceColorIndex } ?: allShapes[0]
        val holdPiece = holdPieceColorIndex?.let { idx -> allShapes.firstOrNull { it.colorIndex == idx } }
        val parsedNext = nextPiecesColorIndices.map { idx -> allShapes.firstOrNull { it.colorIndex == idx } ?: allShapes[0] }
        val nextPieces = if (parsedNext.isNotEmpty()) parsedNext else List(3) { nextPiece(isExtendedMode) }

        _gameState.update {
            GameState(
                grid = grid,
                currentPiece = currentPiece,
                currentPos = currentPos,
                holdPiece = holdPiece,
                nextPieces = nextPieces,
                score = score,
                lines = lines,
                level = level,
                isGameOver = isGameOver,
                isExtendedMode = isExtendedMode,
                gameMode = gameMode,
                timeRemainingSeconds = timeRemainingSeconds,
                piecesPlaced = piecesPlaced
            )
        }
    }

    // Выбираем следующую фигуру из bag — Relax имеет свои наборы / next piece from bag system
    private fun nextPiece(extendedMode: Boolean): Tetromino {
        if (_gameState.value.gameMode == GameMode.RELAX) {
            val relaxShapes = when (relaxBlockSet) {
                "only_i" -> listOf(STANDARD_SHAPES[0])
                "ideal" -> listOf(STANDARD_SHAPES[0], STANDARD_SHAPES[3], STANDARD_SHAPES[5])
                "standard" -> STANDARD_SHAPES
                "all" -> STANDARD_SHAPES + EXTENDED_SHAPES
                else -> listOf(STANDARD_SHAPES[0], STANDARD_SHAPES[3], STANDARD_SHAPES[5])
            }
            if (bag.isEmpty()) {
                bag.addAll(relaxShapes)
                bag.shuffle()
            }
            if (bag.isNotEmpty() && !relaxShapes.contains(bag.firstOrNull())) {
                bag.clear()
                bag.addAll(relaxShapes)
                bag.shuffle()
            }
            return if (bag.isNotEmpty()) bag.removeAt(0) else STANDARD_SHAPES[0]
        }
        if (bag.isEmpty()) {
            bag.addAll(STANDARD_SHAPES)
            if (extendedMode) bag.addAll(EXTENDED_SHAPES)
            bag.shuffle()
        }
        return bag.removeAt(0)
    }

    // Мусорные линии снизу для мультиплеера / garbage lines from opponent
    fun addGarbageLines(count: Int) {
        val state = _gameState.value
        val grid = state.grid.map { it.clone() }.toMutableList()
        for (i in 0 until count) {
            if (grid.isNotEmpty()) {
                grid.removeAt(0)
            }
            val garbageRow = IntArray(10) { (1..7).random() }
            garbageRow[(0..9).random()] = 0 // дырка в мусоре / hole in garbage
            grid.add(garbageRow)
        }
        _gameState.update { it.copy(grid = grid) }
    }

    // Тик — опускаем фигуру на 1 ряд, если стенка — лочим / gravity tick, move piece down 1
    fun tick() {
        val state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        
        val nextPos = state.currentPos.copy(y = state.currentPos.y + 1)
        if (isValidMove(nextPos, state.currentPiece, state.grid)) {
            _gameState.update { it.copy(currentPos = nextPos) }
            // Быстрая фиксация — сразу лочим если ниже некуда / fast lock: lock if no room below
            if (fastDropLockSpeed && !isValidMove(nextPos.copy(y = nextPos.y + 1), state.currentPiece, state.grid)) {
                lockPiece()
            }
        } else {
            lockPiece()
        }
    }

    // Движение влево (инверсия для REVERSE/MIRROR) / move left (reversed in chaos modes)
    fun moveLeft() {
        val state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        val isReverse = state.gameMode == GameMode.REVERSE_CONTROLS || state.gameMode == GameMode.MIRROR_DIMENSION
        val nextX = if (isReverse) state.currentPos.x + 1 else state.currentPos.x - 1
        if (isValidMove(state.currentPos.copy(x = nextX), state.currentPiece, state.grid)) {
            _gameState.update { it.copy(currentPos = it.currentPos.copy(x = nextX)) }
        }
    }

    // Движение вправо / move right
    fun moveRight() {
        val state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        val isReverse = state.gameMode == GameMode.REVERSE_CONTROLS || state.gameMode == GameMode.MIRROR_DIMENSION
        val nextX = if (isReverse) state.currentPos.x - 1 else state.currentPos.x + 1
        if (isValidMove(state.currentPos.copy(x = nextX), state.currentPiece, state.grid)) {
            _gameState.update { it.copy(currentPos = it.currentPos.copy(x = nextX)) }
        }
    }
    
    fun softDrop() {
        tick()
    }

    // Хард-дроп — моментальное падение до дна / instant drop to bottom
    fun hardDrop() {
        var state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        
        var newY = state.currentPos.y
        while (isValidMove(Position(state.currentPos.x, newY + 1), state.currentPiece, state.grid)) {
            newY++
        }
        _gameState.update { it.copy(currentPos = it.currentPos.copy(y = newY)) }
        lockPiece()
    }

    // Поворот + wall kick — пробуем сдвиг если не влезает / rotation with wall kick attempts
    fun rotate() {
        val state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        
        val rotatedShape = state.currentPiece.shape.map { p ->
            Position(-p.y, p.x)
        }
        val rotatedPiece = state.currentPiece.copy(shape = rotatedShape)
        
        if (isValidMove(state.currentPos, rotatedPiece, state.grid)) {
            _gameState.update { it.copy(currentPiece = rotatedPiece) }
        } else {
            if (isValidMove(state.currentPos.copy(x = state.currentPos.x - 1), rotatedPiece, state.grid)) {
                _gameState.update { it.copy(currentPiece = rotatedPiece, currentPos = it.currentPos.copy(x = it.currentPos.x - 1)) }
            } else if (isValidMove(state.currentPos.copy(x = state.currentPos.x + 1), rotatedPiece, state.grid)) {
                _gameState.update { it.copy(currentPiece = rotatedPiece, currentPos = it.currentPos.copy(x = it.currentPos.x + 1)) }
            }
        }
    }

    // Hold-механика — обмен текущей фигуры с запасной / swap current piece with hold slot
    fun hold() {
        val state = _gameState.value
        if (state.isGameOver || state.hasHeldThisTurn || state.currentPiece == null) return

        val nextP = state.holdPiece ?: state.nextPieces.firstOrNull() ?: nextPiece(state.isExtendedMode)
        val remainingNext = if (state.holdPiece == null) {
            if (state.nextPieces.isNotEmpty()) {
                state.nextPieces.drop(1) + nextPiece(state.isExtendedMode)
            } else {
                List(3) { nextPiece(state.isExtendedMode) }
            }
        } else {
            state.nextPieces
        }
        
        val originalPiece = if (state.currentPiece.colorIndex > 10) state.currentPiece
        else (STANDARD_SHAPES + EXTENDED_SHAPES).firstOrNull { it.colorIndex == state.currentPiece.colorIndex } ?: state.currentPiece

        _gameState.update {
            it.copy(
                currentPiece = nextP,
                currentPos = Position(4, 0),
                holdPiece = originalPiece,
                nextPieces = remainingNext,
                hasHeldThisTurn = true
            )
        }
    }

    // Проверка валидности позиции — не вылезаем за границы и не залезаем в занятые клетки
    // collision check: bounds + occupied cells
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

    // Фиксация фигуры в сетку + очистка линий + подсчёт очков + проверка game over
    // lock piece, clear lines, score, check death
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

        // Удаляем заполненные ряды, добавляем пустые сверху / remove full rows, add empty on top
        val newGrid = grid.filter { row -> row.any { it == 0 } }.toMutableList()
        val cleared = maxOf(0, 22 - newGrid.size)
        for (i in 0 until cleared) {
            newGrid.add(0, IntArray(10))
        }

        // Vortex Pulse — мусорная строка каждые 4 фигуры / garbage row every 4 pieces
        val newPiecesPlaced = state.piecesPlaced + 1
        if (state.gameMode == GameMode.PULSE_EXTREME && newPiecesPlaced % 4 == 0) {
            newGrid.removeAt(0)
            val garbageRow = IntArray(10) { (1..6).random() }
            garbageRow[(0..9).random()] = 0
            newGrid.add(garbageRow)
        }

        // Подсчёт очков — 1 линия=100, 2=300, 3=500, tetris=800, умножаем на уровень
        // scoring: 1=100, 2=300, 3=500, tetris=800, multiplied by level
        val newLines = state.lines + cleared
        val startingLevel = maxOf(1, state.level - state.lines / 10)
        val newLevel = startingLevel + newLines / 10
        val basePoints = when (cleared) {
            1 -> if (lineClearChallenge) 0 else 100
            2 -> 300
            3 -> 500
            4 -> 800
            else -> 0
        }
        val addedScore = basePoints * state.level
        val addedTime = if (state.gameMode == GameMode.TIME_ATTACK) cleared * 10 else 0

        // Проверка смерти — блоки в top-3 рядах = game over (кроме Zen/Relax)
        // death check: blocks in top 3 rows = game over (except Zen/Relax)
        var isOver = false
        val nextP = state.nextPieces.firstOrNull() ?: nextPiece(state.isExtendedMode)
        val blocksAtTop = newGrid[0].any { it != 0 } || newGrid[1].any { it != 0 } || newGrid[2].any { it != 0 }
        if (!isValidMove(Position(4, 0), nextP, newGrid) || blocksAtTop) {
            if (state.gameMode == GameMode.ZEN_FLOW || (state.gameMode == GameMode.RELAX && relaxImmortal)) {
                // Zen/Relax бессмертие — очищаем поле при заполнении / immortal: clear board on fill
                newGrid.clear()
                for (i in 0 until 22) {
                    newGrid.add(0, IntArray(10))
                }
            } else {
                isOver = true
            }
        }

        _gameState.update {
            val updatedNext = if (it.nextPieces.isNotEmpty()) {
                it.nextPieces.drop(1) + nextPiece(state.isExtendedMode)
            } else {
                List(3) { nextPiece(state.isExtendedMode) }
            }
            val isTetris = cleared == 4
            it.copy(
                grid = newGrid,
                currentPiece = nextP,
                currentPos = Position(4, 0),
                nextPieces = updatedNext,
                score = it.score + addedScore,
                lines = newLines,
                level = newLevel,
                isGameOver = isOver,
                hasHeldThisTurn = false,
                timeRemainingSeconds = if (state.gameMode == GameMode.TIME_ATTACK) (it.timeRemainingSeconds + addedTime).coerceAtMost(180) else it.timeRemainingSeconds,
                piecesPlaced = newPiecesPlaced,
                tetrisesCleared = it.tetrisesCleared + (if (isTetris) 1 else 0)
            )
        }
    }

    fun clearBoard() {
        val currentGrid = List(22) { IntArray(10) }
        _gameState.update { it.copy(grid = currentGrid) }
    }

    // Таймер для Time Attack — уменьшаем время / Time Attack countdown
    fun decrementTime(sec: Int) {
        _gameState.update {
            val newTime = maxOf(0, it.timeRemainingSeconds - sec)
            it.copy(
                timeRemainingSeconds = newTime,
                isGameOver = if (newTime <= 0) true else it.isGameOver
            )
        }
    }
}
