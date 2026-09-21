package com.example.game

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

// FlowTess уникальная палитра цветов фигур (отличие от классического Тетриса во избежание страйков)
val Colors = listOf(
    Color.Transparent,
    Color(0xFF7C4DFF), // 1: Фиолетовый индиго (I-форма)
    Color(0xFF00E676), // 2: Изумрудный лайм (J-форма)
    Color(0xFFFF007F), // 3: Кибер-маджента (L-форма)
    Color(0xFFFF6D00), // 4: Огненно-оранжевый (O-квадрат)
    Color(0xFFFFD600), // 5: Солнечное золото (S-форма)
    Color(0xFF00E5FF), // 6: Электрический циан (T-форма)
    Color(0xFF3D5AFE), // 7: Королевский кобальт (Z-форма)
    Color(0xFFFF3D00), // 8: Плазменный кримсон (Extended +)
    Color(0xFF1DE9B6), // 9: Бирюзовая аква (Extended U)
    Color(0xFFE0E0E0)  // 10: Платиновый хром (Extended dot)
)

data class Position(val x: Int, val y: Int)

data class Tetromino(
    val shape: List<Position>,
    val colorIndex: Int,
    val pivot: Position = Position(0, 0),
    val rotationState: Int = 0
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
    CLASSIC("classic", "Standard", "Стандарт"),
    EXTENDED("extended", "Extended", "Расширенный"),
    FAST_RUN("fast_run", "Sprint", "Спринт"),
    BLOCK_BLAST("block_blast", "Zeta", "Zeta"),
    TIME_ATTACK("time_attack", "Blitz", "Блиц"),
    MIRROR_DIMENSION("mirror", "Mirror", "Зеркало"),
    RELAX("relax", "Sandbox", "Песочница"),
    PERFECTIONIST("perfectionist", "Perfection", "Идеал"),
    PATTERN_PUZZLE("pattern", "Blueprint", "Шаблон"),
    MEMORY_PUZZLE("memory", "Memory", "Память")
}

data class PlacementHint(
    val shape: List<Position>,
    val targetPos: Position,
    val score: Double,
    val shouldHold: Boolean = false,
    val holdReason: String = "",
    val holes: Int = 0,
    val linesCleared: Int = 0,
    val actionLabel: String = ""
)

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
    val tetrisesCleared: Int = 0,
    val patternTargets: Set<Position> = emptySet(),
    val puzzleLevel: Int = 1,
    val puzzleGoalDescription: String = "",
    val puzzleTargetCount: Int = 0,
    val puzzleFilledCount: Int = 0,
    val isPuzzleCompleted: Boolean = false,
    val memoryCountdownSeconds: Int = 0,
    val isMemoryHidden: Boolean = false
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
    var relaxBlockSet: String = "standard_7" // standard_7, full_random, single_piece, pentaminoes, ideal, only_i
    var relaxSinglePieceIndex: Int = 0 // 0=I, 1=J, 2=L, 3=O, 4=S, 5=T, 6=Z
    var relaxLockDelay: Long = 500L // 0L=instant, 200L, 500L, 1000L, 2000L, -1L=infinite
    private var lockDelayStartTime: Long = 0L
    private var isPieceOnGround: Boolean = false
    
    init {
        startGame(GameMode.CLASSIC)
    }

    // Запуск/рестарт — сбрасываем сетку 22x10, генерим фигуры / start new game, reset grid
    fun startGame(mode: GameMode, startingLevel: Int = 1) {
        bag.clear()
        val isExt = mode == GameMode.EXTENDED
        val finalLevel = if (mode == GameMode.FAST_RUN) 10 else startingLevel

        if (mode == GameMode.PATTERN_PUZZLE || mode == GameMode.MEMORY_PUZZLE) {
            val (patternTitle, targets) = getPatternForLevel(1)
            _gameState.update {
                GameState(
                    grid = List(22) { IntArray(10) },
                    nextPieces = List(3) { nextPiece(false) },
                    currentPiece = nextPiece(false),
                    isExtendedMode = false,
                    level = 1,
                    gameMode = mode,
                    patternTargets = targets,
                    puzzleLevel = 1,
                    puzzleGoalDescription = patternTitle,
                    puzzleTargetCount = targets.size,
                    puzzleFilledCount = 0,
                    memoryCountdownSeconds = if (mode == GameMode.MEMORY_PUZZLE) 3 else 0,
                    isMemoryHidden = false
                )
            }
            return
        }

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

    // ── РЕЖИМ «ШАБЛОН» (BLUEPRINT PUZZLE) ──
    fun getPatternForLevel(lvl: Int): Pair<String, Set<Position>> {
        return when ((lvl - 1) % 10 + 1) {
            1 -> "АЛМАЗ" to setOf(
                Position(4, 18), Position(5, 18),
                Position(3, 19), Position(4, 19), Position(5, 19), Position(6, 19),
                Position(4, 20), Position(5, 20)
            )
            2 -> "ПИРАМИДА" to setOf(
                Position(4, 19), Position(5, 19),
                Position(3, 20), Position(4, 20), Position(5, 20), Position(6, 20),
                Position(2, 21), Position(3, 21), Position(4, 21), Position(5, 21), Position(6, 21), Position(7, 21)
            )
            3 -> "СЕРДЦЕ" to setOf(
                Position(2, 18), Position(3, 18), Position(6, 18), Position(7, 18),
                Position(2, 19), Position(3, 19), Position(4, 19), Position(5, 19), Position(6, 19), Position(7, 19),
                Position(3, 20), Position(4, 20), Position(5, 20), Position(6, 20),
                Position(4, 21), Position(5, 21)
            )
            4 -> "КОРОНА" to setOf(
                Position(2, 18), Position(4, 18), Position(5, 18), Position(7, 18),
                Position(2, 19), Position(3, 19), Position(4, 19), Position(5, 19), Position(6, 19), Position(7, 19),
                Position(3, 20), Position(4, 20), Position(5, 20), Position(6, 20)
            )
            5 -> "КРЕСТ" to setOf(
                Position(4, 17), Position(5, 17),
                Position(2, 18), Position(3, 18), Position(4, 18), Position(5, 18), Position(6, 18), Position(7, 18),
                Position(4, 19), Position(5, 19),
                Position(4, 20), Position(5, 20)
            )
            6 -> "РАКЕТА" to setOf(
                Position(4, 16), Position(5, 16),
                Position(4, 17), Position(5, 17),
                Position(3, 18), Position(4, 18), Position(5, 18), Position(6, 18),
                Position(3, 19), Position(4, 19), Position(5, 19), Position(6, 19),
                Position(2, 20), Position(4, 20), Position(5, 20), Position(7, 20)
            )
            7 -> "КУБОК" to setOf(
                Position(2, 17), Position(3, 17), Position(6, 17), Position(7, 17),
                Position(2, 18), Position(3, 18), Position(4, 18), Position(5, 18), Position(6, 18), Position(7, 18),
                Position(4, 19), Position(5, 19),
                Position(4, 20), Position(5, 20),
                Position(3, 21), Position(4, 21), Position(5, 21), Position(6, 21)
            )
            8 -> "ЗВЕЗДА" to setOf(
                Position(4, 17), Position(5, 17),
                Position(1, 18), Position(2, 18), Position(3, 18), Position(4, 18), Position(5, 18), Position(6, 18), Position(7, 18), Position(8, 18),
                Position(3, 19), Position(4, 19), Position(5, 19), Position(6, 19),
                Position(2, 20), Position(7, 20)
            )
            9 -> "КРЕПОСТЬ" to setOf(
                Position(1, 17), Position(2, 17), Position(7, 17), Position(8, 17),
                Position(1, 18), Position(2, 18), Position(4, 18), Position(5, 18), Position(7, 18), Position(8, 18),
                Position(1, 19), Position(2, 19), Position(3, 19), Position(4, 19), Position(5, 19), Position(6, 19), Position(7, 19), Position(8, 19),
                Position(2, 20), Position(3, 20), Position(4, 20), Position(5, 20), Position(6, 20), Position(7, 20)
            )
            else -> "БЕСКОНЕЧНОСТЬ" to setOf(
                Position(2, 18), Position(3, 18), Position(6, 18), Position(7, 18),
                Position(1, 19), Position(4, 19), Position(5, 19), Position(8, 19),
                Position(2, 20), Position(3, 20), Position(6, 20), Position(7, 20)
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
        piecesPlaced: Int = 0,
        puzzleLevel: Int = 1,
        puzzleGoalDescription: String = "",
        puzzleTargetCount: Int = 0,
        puzzleFilledCount: Int = 0
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
                piecesPlaced = piecesPlaced,
                puzzleLevel = puzzleLevel,
                puzzleGoalDescription = puzzleGoalDescription,
                puzzleTargetCount = puzzleTargetCount,
                puzzleFilledCount = puzzleFilledCount
            )
        }
    }

    // Выбираем следующую фигуру из bag — Relax имеет свои наборы / next piece from bag system
    private fun nextPiece(extendedMode: Boolean): Tetromino {
        if (_gameState.value.gameMode == GameMode.RELAX) {
            if (relaxBlockSet == "full_random") {
                return STANDARD_SHAPES.random()
            }
            val relaxShapes = when (relaxBlockSet) {
                "only_i" -> listOf(STANDARD_SHAPES[0])
                "single_piece" -> listOf(STANDARD_SHAPES.getOrElse(relaxSinglePieceIndex) { STANDARD_SHAPES[0] })
                "ideal" -> listOf(STANDARD_SHAPES[0], STANDARD_SHAPES[3], STANDARD_SHAPES[5])
                "pentaminoes", "all" -> STANDARD_SHAPES + EXTENDED_SHAPES
                "standard", "standard_7" -> STANDARD_SHAPES
                else -> STANDARD_SHAPES
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
        if (_gameState.value.gameMode == GameMode.PERFECTIONIST) {
            val idealShapes = listOf(STANDARD_SHAPES[0], STANDARD_SHAPES[3], STANDARD_SHAPES[5], STANDARD_SHAPES[1], STANDARD_SHAPES[2], STANDARD_SHAPES[4], STANDARD_SHAPES[6])
            if (bag.isEmpty()) {
                bag.addAll(idealShapes)
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
        if (count <= 0) return
        _gameState.update { state ->
            val grid = state.grid.map { it.clone() }.toMutableList()
            for (i in 0 until count) {
                if (grid.isNotEmpty()) {
                    grid.removeAt(0)
                }
                val garbageRow = IntArray(10) { (1..7).random() }
                garbageRow[(0..9).random()] = 0 // дырка в мусоре / hole in garbage
                grid.add(garbageRow)
            }
            var newPos = state.currentPos
            val piece = state.currentPiece
            if (piece != null) {
                while (!isValidMove(newPos, piece, grid) && newPos.y > -2) {
                    newPos = newPos.copy(y = newPos.y - 1)
                }
            }
            state.copy(grid = grid, currentPos = newPos)
        }
    }

    // Тик — опускаем фигуру на 1 ряд, если стенка — лочим / gravity tick, move piece down 1
    fun tick() {
        val state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        
        val nextPos = state.currentPos.copy(y = state.currentPos.y + 1)
        if (isValidMove(nextPos, state.currentPiece, state.grid)) {
            isPieceOnGround = false
            lockDelayStartTime = 0L
            _gameState.update { it.copy(currentPos = nextPos) }
            // Быстрая фиксация — сразу лочим если ниже некуда / fast lock: lock if no room below
            if (fastDropLockSpeed && !isValidMove(nextPos.copy(y = nextPos.y + 1), state.currentPiece, state.grid)) {
                lockPiece()
            }
        } else {
            if (state.gameMode == GameMode.RELAX) {
                if (relaxLockDelay == 0L) {
                    lockPiece()
                } else if (relaxLockDelay < 0L) {
                    // Infinite lock delay: never auto-locks on gravity tick
                    isPieceOnGround = true
                } else {
                    val now = System.currentTimeMillis()
                    if (!isPieceOnGround || lockDelayStartTime == 0L) {
                        isPieceOnGround = true
                        lockDelayStartTime = now
                    } else if (now - lockDelayStartTime >= relaxLockDelay) {
                        isPieceOnGround = false
                        lockDelayStartTime = 0L
                        lockPiece()
                    }
                }
            } else {
                lockPiece()
            }
        }
    }

    private fun isControlsBlocked(): Boolean {
        val state = _gameState.value
        return state.isGameOver || (state.gameMode == GameMode.MEMORY_PUZZLE && state.memoryCountdownSeconds > 0)
    }

    // Движение влево (инверсия для MIRROR) / move left (reversed in mirror mode)
    fun moveLeft() {
        val state = _gameState.value
        if (isControlsBlocked() || state.currentPiece == null) return
        val isReverse = state.gameMode == GameMode.MIRROR_DIMENSION
        val nextX = if (isReverse) state.currentPos.x + 1 else state.currentPos.x - 1
        if (isValidMove(state.currentPos.copy(x = nextX), state.currentPiece, state.grid)) {
            if (isPieceOnGround) lockDelayStartTime = System.currentTimeMillis()
            _gameState.update { it.copy(currentPos = it.currentPos.copy(x = nextX)) }
        }
    }

    // Движение вправо / move right
    fun moveRight() {
        val state = _gameState.value
        if (isControlsBlocked() || state.currentPiece == null) return
        val isReverse = state.gameMode == GameMode.MIRROR_DIMENSION
        val nextX = if (isReverse) state.currentPos.x - 1 else state.currentPos.x + 1
        if (isValidMove(state.currentPos.copy(x = nextX), state.currentPiece, state.grid)) {
            if (isPieceOnGround) lockDelayStartTime = System.currentTimeMillis()
            _gameState.update { it.copy(currentPos = it.currentPos.copy(x = nextX)) }
        }
    }
    
    fun softDrop() {
        if (isControlsBlocked()) return
        val state = _gameState.value
        if (state.currentPiece != null && isValidMove(Position(state.currentPos.x, state.currentPos.y + 1), state.currentPiece, state.grid)) {
            _gameState.update { it.copy(score = it.score + 1) }
        }
        tick()
    }

    // Хард-дроп — моментальное падение до дна / instant drop to bottom
    fun hardDrop() {
        var state = _gameState.value
        if (isControlsBlocked() || state.currentPiece == null) return
        
        var newY = state.currentPos.y
        while (isValidMove(Position(state.currentPos.x, newY + 1), state.currentPiece, state.grid)) {
            newY++
        }
        val droppedCells = newY - state.currentPos.y
        _gameState.update { it.copy(currentPos = it.currentPos.copy(y = newY), score = it.score + droppedCells * 2) }
        lockPiece()
    }

    // Поворот + wall kick — пробуем сдвиг если не влезает / rotation with wall kick attempts
    fun rotate() {
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        if (isControlsBlocked()) return

        // 1. Квадрат (O-форма) не вращается
        if (piece.colorIndex == 4) return

        // 2. 2-позиционные фигуры (I, S, Z, dot) переключаются туда-обратно (0 <-> 1)
        val isTwoState = piece.colorIndex in listOf(1, 5, 7, 10)
        val nextRotationState = if (isTwoState) {
            if (piece.rotationState == 0) 1 else 0
        } else {
            (piece.rotationState + 1) % 4
        }

        val rotatedShape = if (isTwoState && piece.rotationState == 1) {
            // Поворот обратно (-90 градусов: (y, -x))
            piece.shape.map { p ->
                val rx = p.x - piece.pivot.x
                val ry = p.y - piece.pivot.y
                Position(piece.pivot.x + ry, piece.pivot.y - rx)
            }
        } else {
            // Обычный поворот (+90 градусов: (-y, x))
            piece.shape.map { p ->
                val rx = p.x - piece.pivot.x
                val ry = p.y - piece.pivot.y
                Position(piece.pivot.x - ry, piece.pivot.y + rx)
            }
        }
        val rotatedPiece = piece.copy(shape = rotatedShape, rotationState = nextRotationState)

        // 3. Wall kick checks
        val kicks = if (piece.colorIndex == 1) {
            listOf(Position(0, 0), Position(-1, 0), Position(1, 0), Position(-2, 0), Position(2, 0), Position(0, -1))
        } else {
            listOf(Position(0, 0), Position(-1, 0), Position(1, 0), Position(0, -1), Position(-1, -1), Position(1, -1))
        }

        val successfulKick = kicks.firstOrNull { kick ->
            isValidMove(
                Position(state.currentPos.x + kick.x, state.currentPos.y + kick.y),
                rotatedPiece,
                state.grid
            )
        }

        if (successfulKick != null) {
            if (isPieceOnGround) lockDelayStartTime = System.currentTimeMillis()
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

    // Hold-механика — обмен текущей фигуры с запасной / swap current piece with hold slot
    fun hold() {
        val state = _gameState.value
        if (isControlsBlocked() || state.hasHeldThisTurn || state.currentPiece == null) return

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
        isPieceOnGround = false
        lockDelayStartTime = 0L
        val state = _gameState.value
        val piece = state.currentPiece ?: return
        val grid = state.grid.map { it.clone() }
        
        var lockedOut = false
        for (p in piece.shape) {
            val nx = state.currentPos.x + p.x
            val ny = state.currentPos.y + p.y
            if (ny < 0) {
                lockedOut = true
            } else if (ny in 0 until grid.size && nx in 0 until grid[ny].size) {
                grid[ny][nx] = piece.colorIndex
            }
        }

        // В режимах головоломок (PATTERN_PUZZLE, MEMORY_PUZZLE) очистка линий не производится,
        // чтобы не разрушать статичные координаты целей patternTargets
        val isPuzzleMode = state.gameMode == GameMode.PATTERN_PUZZLE || state.gameMode == GameMode.MEMORY_PUZZLE
        val newGrid: MutableList<IntArray>
        val cleared: Int
        if (!isPuzzleMode) {
            // Удаляем заполненные ряды, добавляем пустые сверху / remove full rows, add empty on top
            val filteredGrid = grid.filter { row -> row.any { it == 0 } }.toMutableList()
            cleared = maxOf(0, 22 - filteredGrid.size)
            for (i in 0 until cleared) {
                filteredGrid.add(0, IntArray(10))
            }
            newGrid = filteredGrid
        } else {
            newGrid = grid.toMutableList()
            cleared = 0
        }

        val newPiecesPlaced = state.piecesPlaced + 1

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
            5 -> 1200
            else -> if (cleared > 5) cleared * 250 else 0
        }
        val addedScore = basePoints * state.level
        val addedTime = if (state.gameMode == GameMode.TIME_ATTACK) cleared * 10 else 0

        // Проверка смерти — Lock Out (блок зафиксирован выше поля ny < 0) или Block Out (спавн заблокирован)
        // death check: Lock Out (mino locked above ceiling ny < 0) or Block Out (spawn collision)
        var isOver = false
        val nextP = state.nextPieces.firstOrNull() ?: nextPiece(state.isExtendedMode)
        val spawnBlocked = !isValidMove(Position(4, 0), nextP, newGrid)
        if (lockedOut || spawnBlocked) {
            if (state.gameMode == GameMode.RELAX && relaxImmortal) {
                // Relax бессмертие: очищаем верхние забитые ряды или переполненные блоки
                for (r in 0..5) {
                    if (r < newGrid.size) newGrid[r] = IntArray(10)
                }
                if (!isValidMove(Position(4, 0), nextP, newGrid)) {
                    for (r in 0..11) {
                        if (r < newGrid.size) newGrid[r] = IntArray(10)
                    }
                }
                isOver = false
            } else {
                isOver = true
            }
        }

        // Обработка режима «Шаблон» и «Головоломка на память»
        if ((state.gameMode == GameMode.PATTERN_PUZZLE || state.gameMode == GameMode.MEMORY_PUZZLE) && state.patternTargets.isNotEmpty()) {
            val filled = state.patternTargets.count { target ->
                target.y in 0 until newGrid.size && target.x in 0 until newGrid[target.y].size && newGrid[target.y][target.x] != 0
            }
            if (filled >= state.patternTargets.size) {
                val nextLvl = state.puzzleLevel + 1
                val (nextTitle, nextTargets) = getPatternForLevel(nextLvl)
                newGrid.clear()
                for (i in 0 until 22) newGrid.add(0, IntArray(10))
                _gameState.update {
                    it.copy(
                        grid = newGrid,
                        score = it.score + (state.puzzleLevel * 1000),
                        puzzleLevel = nextLvl,
                        patternTargets = nextTargets,
                        puzzleGoalDescription = nextTitle,
                        puzzleTargetCount = nextTargets.size,
                        puzzleFilledCount = 0,
                        currentPiece = nextPiece(false),
                        currentPos = Position(4, 0),
                        hasHeldThisTurn = false,
                        memoryCountdownSeconds = if (state.gameMode == GameMode.MEMORY_PUZZLE) 3 else 0,
                        isMemoryHidden = false
                    )
                }
                return
            } else {
                _gameState.update {
                    it.copy(
                        puzzleFilledCount = filled
                    )
                }
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

    // Таймер для Time Attack и Memory Puzzle
    fun decrementTime(sec: Int) {
        _gameState.update {
            val newTime = maxOf(0, it.timeRemainingSeconds - sec)
            val newMemCountdown = if (it.gameMode == GameMode.MEMORY_PUZZLE && it.memoryCountdownSeconds > 0) {
                maxOf(0, it.memoryCountdownSeconds - sec)
            } else it.memoryCountdownSeconds
            val isMemHidden = if (it.gameMode == GameMode.MEMORY_PUZZLE) newMemCountdown == 0 else false
            it.copy(
                timeRemainingSeconds = newTime,
                memoryCountdownSeconds = newMemCountdown,
                isMemoryHidden = isMemHidden,
                isGameOver = if (it.gameMode == GameMode.TIME_ATTACK && newTime <= 0) true else it.isGameOver
            )
        }
    }

    // Очистка нижней части поля (для Relax-режима)
    fun clearLowerRows(count: Int = 8) {
        val state = _gameState.value
        val currentGrid = state.grid.map { it.clone() }.toMutableList()
        val removeCount = minOf(count, currentGrid.size)
        for (i in 0 until removeCount) {
            if (currentGrid.isNotEmpty()) currentGrid.removeAt(currentGrid.size - 1)
        }
        for (i in 0 until removeCount) {
            currentGrid.add(0, IntArray(10))
        }
        _gameState.update { it.copy(grid = currentGrid) }
    }

    // Заполнить нижние случайные линии (для Relax-режима)
    fun fillBottomRandomLines(count: Int = 2) {
        val state = _gameState.value
        val grid = state.grid.map { it.clone() }.toMutableList()
        val addCount = minOf(count, 10)
        for (i in 0 until addCount) {
            if (grid.isNotEmpty()) grid.removeAt(0)
            val row = IntArray(10) { (1..7).random() }
            val hole1 = (0..9).random()
            var hole2 = (0..9).random()
            while (hole2 == hole1) hole2 = (0..9).random()
            row[hole1] = 0
            row[hole2] = 0
            grid.add(row)
        }
        var newPos = state.currentPos
        val piece = state.currentPiece
        if (piece != null) {
            while (!isValidMove(newPos, piece, grid) && newPos.y > -2) {
                newPos = newPos.copy(y = newPos.y - 1)
            }
        }
        _gameState.update { it.copy(grid = grid, currentPos = newPos) }
    }

    // Инвертировать поле: пустые клетки становятся цветными блоками, блоки — пустотой
    fun invertField() {
        val state = _gameState.value
        val grid = state.grid.map { it.clone() }.toMutableList()
        var highestOccupied = 21
        for (y in 0 until grid.size) {
            if (grid[y].any { it != 0 }) {
                highestOccupied = y
                break
            }
        }
        val startY = maxOf(4, highestOccupied)
        for (y in startY until grid.size) {
            for (x in 0 until grid[y].size) {
                grid[y][x] = if (grid[y][x] == 0) ((x + y) % 7) + 1 else 0
            }
        }
        _gameState.update { it.copy(grid = grid) }
    }

    // Мгновенный спавн фигуры на поле для палитры спавнера
    fun spawnPiece(piece: Tetromino) {
        val state = _gameState.value
        val newGrid = state.grid.map { it.clone() }.toMutableList()
        if (state.gameMode == GameMode.RELAX) {
            for (r in 0..3) {
                if (r < newGrid.size) newGrid[r] = IntArray(10)
            }
        }
        _gameState.update {
            it.copy(
                grid = newGrid,
                currentPiece = piece,
                currentPos = Position(4, 0),
                isGameOver = false
            )
        }
    }

    fun spawnPieceByIndex(index: Int) {
        val piece = STANDARD_SHAPES.getOrNull(index) ?: STANDARD_SHAPES[0]
        spawnPiece(piece)
    }

    // ── AI-ПОДСКАЗЧИК: 3-PLY LOOKAHEAD + ADVANCED HEURISTICS + T-SPIN + PERFECTIONIST ──

    private class PieceOrientation(
        val shape: List<Position>,
        val xs: IntArray,
        val ys: IntArray,
        val minX: Int,
        val maxX: Int
    )

    private class CandidateMove(
        val shape: List<Position>,
        val targetPos: Position,
        val score: Double,
        val holes: Int,
        val linesCleared: Int,
        val actionLabel: String,
        val resultingMasks: IntArray
    )

    private fun getOrientations(piece: Tetromino): List<PieceOrientation> {
        val maxRotations = when (piece.colorIndex) {
            4 -> 1
            1, 5, 7, 10 -> 2
            else -> 4
        }
        val orientations = ArrayList<PieceOrientation>(maxRotations)
        val seen = HashSet<Set<Position>>(maxRotations)
        var curShape = piece.shape
        for (r in 0 until maxRotations) {
            val set = curShape.toSet()
            if (seen.add(set)) {
                val xs = IntArray(curShape.size) { curShape[it].x }
                val ys = IntArray(curShape.size) { curShape[it].y }
                var minX = Int.MAX_VALUE
                var maxX = Int.MIN_VALUE
                for (p in curShape) {
                    if (p.x < minX) minX = p.x
                    if (p.x > maxX) maxX = p.x
                }
                orientations.add(PieceOrientation(curShape, xs, ys, minX, maxX))
            }
            curShape = curShape.map { p ->
                val rx = p.x - piece.pivot.x
                val ry = p.y - piece.pivot.y
                Position(piece.pivot.x - ry, piece.pivot.y + rx)
            }
        }
        return orientations
    }

    private fun countHoles(masks: IntArray): Int {
        var holes = 0
        for (c in 0..9) {
            val bit = 1 shl c
            var hasBlock = false
            for (r in 0..21) {
                if ((masks[r] and bit) != 0) {
                    hasBlock = true
                } else if (hasBlock) {
                    holes++
                }
            }
        }
        return holes
    }

    private fun computeBoardScore(
        simMasks: IntArray,
        completeLines: Int,
        targetPos: Position,
        piece: Tetromino,
        orientation: PieceOrientation,
        baseHoles: Int,
        colHeights: IntArray,
        outMetrics: IntArray? = null
    ): Double {
        // 1. Column heights
        var maxHeight = 0
        var aggregateHeight = 0
        for (c in 0..9) {
            val bit = 1 shl c
            var h = 0
            for (r in 0..21) {
                if ((simMasks[r] and bit) != 0) {
                    h = 22 - r
                    break
                }
            }
            colHeights[c] = h
            aggregateHeight += h
            if (h > maxHeight) maxHeight = h
        }

        // 2. Holes & Blockades (blocks above holes)
        var holes = 0
        var blockades = 0
        for (c in 0..9) {
            val bit = 1 shl c
            var blocksAbove = 0
            for (r in 0..21) {
                if ((simMasks[r] and bit) != 0) {
                    blocksAbove++
                } else if (blocksAbove > 0) {
                    holes++
                    blockades += blocksAbove
                }
            }
        }
        val newHoles = if (holes > baseHoles) holes - baseHoles else 0

        // 3. Row Transitions
        var rowTransitions = 0
        val startRow = (22 - maxHeight).coerceAtLeast(0)
        for (r in startRow..21) {
            val m = simMasks[r]
            if ((m and 1) == 0) rowTransitions++
            for (c in 0..8) {
                val b1 = (m shr c) and 1
                val b2 = (m shr (c + 1)) and 1
                if (b1 != b2) rowTransitions++
            }
            if ((m and (1 shl 9)) == 0) rowTransitions++
        }

        // 4. Column Transitions
        var colTransitions = 0
        for (c in 0..9) {
            val bit = 1 shl c
            var prevBit = 0
            for (r in 0..21) {
                val curBit = if ((simMasks[r] and bit) != 0) 1 else 0
                if (curBit != prevBit) colTransitions++
                prevBit = curBit
            }
            if (prevBit != 1) colTransitions++
        }

        // 5. Bumpiness / Roughness (sum of absolute differences between adjacent columns)
        var stackBumpiness = 0
        for (c in 0..7) {
            stackBumpiness += abs(colHeights[c] - colHeights[c + 1])
        }
        val col8to9Diff = abs(colHeights[8] - colHeights[9])

        // 6. Deep Crevices (columns 0..8 only, column 9 is the dedicated Tetris well)
        var crevicePenalty = 0.0
        for (c in 0..8) {
            val leftH = if (c > 0) colHeights[c - 1] else 22
            val rightH = colHeights[c + 1]
            val minAdj = min(leftH, rightH)
            val depth = minAdj - colHeights[c]
            if (depth >= 3) {
                crevicePenalty += depth * 28.0
            }
        }

        // 7. Rightmost Column (col 9) open for I-tetramino & Flat surface reward
        val isStackClean = holes == 0 && maxHeight <= 14
        var tetrisWellBonus = 0.0
        var col9WastePenalty = 0.0

        if (isStackClean) {
            val wellDepth = colHeights[8] - colHeights[9]
            if (colHeights[9] == 0) {
                // Pristine empty column 9 ready for Tetris
                tetrisWellBonus = if (colHeights[8] >= 3) 320.0 else 180.0
            } else if (wellDepth >= 2) {
                tetrisWellBonus = 220.0
            } else if (wellDepth == 1) {
                tetrisWellBonus = 90.0
            }

            // Heavily penalize placing non-I pieces in column 9 when clean well is preserved
            if (piece.colorIndex != 1 && completeLines < 4) {
                for (i in 0 until orientation.xs.size) {
                    if (targetPos.x + orientation.xs[i] == 9) {
                        col9WastePenalty = 350.0
                        break
                    }
                }
            }
        }

        // Flat surface reward for clean stack
        val flatSurfaceBonus = if (isStackClean && stackBumpiness <= 4) {
            (5 - stackBumpiness) * 45.0
        } else 0.0

        // 8. T-Spin bonus
        var isTspin = false
        var tSpinBonus = 0.0
        if (piece.colorIndex == 6) { // T-piece
            var cornersFilled = 0
            val py = targetPos.y + piece.pivot.y
            val px = targetPos.x + piece.pivot.x
            for (dx in -1..1 step 2) {
                for (dy in -1..1 step 2) {
                    val cx = px + dx
                    val cy = py + dy
                    if (cx !in 0..9 || cy >= 22 || (cy >= 0 && (simMasks[cy] and (1 shl cx)) != 0)) {
                        cornersFilled++
                    }
                }
            }
            if (cornersFilled >= 3) {
                isTspin = true
                tSpinBonus = when (completeLines) {
                    2 -> 950.0 // T-Spin Double
                    1 -> 500.0 // T-Spin Single
                    else -> 250.0 // T-Spin Setup
                }
            }
        }

        // 9. Line clear reward & height penalties
        val isPanic = maxHeight >= 13
        val lineClearBonus = when (completeLines) {
            4 -> 1200.0 // Reward Tetris (+1200.0)
            3 -> if (isPanic) 480.0 else 140.0
            2 -> if (isPanic) 280.0 else 60.0
            1 -> if (isPanic) 120.0 else -30.0 // Discourage burning single lines while building Tetris
            else -> 0.0
        }

        val heightPenalty = if (isPanic) {
            (maxHeight * 28.0) + (aggregateHeight * 4.5) + ((maxHeight - 12) * (maxHeight - 12) * 20.0)
        } else {
            (maxHeight * 6.0) + (aggregateHeight * 2.2)
        }

        val evalScore = lineClearBonus -
            (newHoles * 1000.0) -             // Heavily penalize new holes (-1000.0)
            (holes * 250.0) -                 // Penalize all holes
            (blockades * 45.0) -              // Penalize blockades (blocks above holes)
            (stackBumpiness * 14.0) -         // Penalize roughness/bumpiness
            (if (isStackClean && colHeights[9] <= colHeights[8]) 0.0 else col8to9Diff * 10.0) -
            (rowTransitions * 6.0) -
            (colTransitions * 10.0) -
            heightPenalty -
            crevicePenalty -
            col9WastePenalty +
            flatSurfaceBonus +
            tetrisWellBonus +
            tSpinBonus +
            (targetPos.y * 3.5)

        if (outMetrics != null) {
            outMetrics[0] = holes
            outMetrics[1] = if (isTspin && completeLines > 0) 1 else 0
        }

        return evalScore
    }

    private fun evaluateCandidateMoves(
        gridMasks: IntArray,
        piece: Tetromino,
        baseHoles: Int,
        topN: Int = 4
    ): List<CandidateMove> {
        val orientations = getOrientations(piece)
        val simMasks = IntArray(22)
        val colHeights = IntArray(10)
        val metrics = IntArray(2)
        val candidates = ArrayList<CandidateMove>(32)

        for (ori in orientations) {
            val minX = ori.minX
            val maxX = ori.maxX
            val nPoints = ori.xs.size

            for (posX in (0 - minX)..(9 - maxX)) {
                var landingY = -1
                for (posY in 0..21) {
                    var collision = false
                    for (i in 0 until nPoints) {
                        val nx = posX + ori.xs[i]
                        val ny = posY + ori.ys[i]
                        if (nx !in 0..9 || ny >= 22 || (ny >= 0 && (gridMasks[ny] and (1 shl nx)) != 0)) {
                            collision = true
                            break
                        }
                    }
                    if (!collision) {
                        landingY = posY
                    } else {
                        break
                    }
                }
                if (landingY < 0) continue

                System.arraycopy(gridMasks, 0, simMasks, 0, 22)
                for (i in 0 until nPoints) {
                    val ny = landingY + ori.ys[i]
                    val nx = posX + ori.xs[i]
                    if (ny in 0..21 && nx in 0..9) {
                        simMasks[ny] = simMasks[ny] or (1 shl nx)
                    }
                }

                var completeLines = 0
                for (r in 0..21) {
                    if (simMasks[r] == 0x3FF) completeLines++
                }

                if (completeLines > 0) {
                    var writeRow = 21
                    for (r in 21 downTo 0) {
                        val m = simMasks[r]
                        if (m != 0x3FF) {
                            simMasks[writeRow] = m
                            writeRow--
                        }
                    }
                    while (writeRow >= 0) {
                        simMasks[writeRow] = 0
                        writeRow--
                    }
                }

                val score = computeBoardScore(
                    simMasks, completeLines, Position(posX, landingY), piece, ori, baseHoles, colHeights, metrics
                )

                candidates.add(
                    CandidateMove(
                        shape = ori.shape,
                        targetPos = Position(posX, landingY),
                        score = score,
                        holes = metrics[0],
                        linesCleared = completeLines,
                        actionLabel = if (metrics[1] == 1) "T-SPIN" else "",
                        resultingMasks = simMasks.clone()
                    )
                )
            }
        }

        candidates.sortByDescending { it.score }
        return if (candidates.size <= topN) candidates else candidates.subList(0, topN)
    }

    private fun evaluateBestScoreOnly(
        gridMasks: IntArray,
        piece: Tetromino,
        baseHoles: Int
    ): Double {
        val orientations = getOrientations(piece)
        val simMasks = IntArray(22)
        val colHeights = IntArray(10)
        var bestScore = -1_000_000.0

        for (ori in orientations) {
            val minX = ori.minX
            val maxX = ori.maxX
            val nPoints = ori.xs.size

            for (posX in (0 - minX)..(9 - maxX)) {
                var landingY = -1
                for (posY in 0..21) {
                    var collision = false
                    for (i in 0 until nPoints) {
                        val nx = posX + ori.xs[i]
                        val ny = posY + ori.ys[i]
                        if (nx !in 0..9 || ny >= 22 || (ny >= 0 && (gridMasks[ny] and (1 shl nx)) != 0)) {
                            collision = true
                            break
                        }
                    }
                    if (!collision) {
                        landingY = posY
                    } else {
                        break
                    }
                }
                if (landingY < 0) continue

                System.arraycopy(gridMasks, 0, simMasks, 0, 22)
                for (i in 0 until nPoints) {
                    val ny = landingY + ori.ys[i]
                    val nx = posX + ori.xs[i]
                    if (ny in 0..21 && nx in 0..9) {
                        simMasks[ny] = simMasks[ny] or (1 shl nx)
                    }
                }

                var completeLines = 0
                for (r in 0..21) {
                    if (simMasks[r] == 0x3FF) completeLines++
                }

                if (completeLines > 0) {
                    var writeRow = 21
                    for (r in 21 downTo 0) {
                        val m = simMasks[r]
                        if (m != 0x3FF) {
                            simMasks[writeRow] = m
                            writeRow--
                        }
                    }
                    while (writeRow >= 0) {
                        simMasks[writeRow] = 0
                        writeRow--
                    }
                }

                val score = computeBoardScore(
                    simMasks, completeLines, Position(posX, landingY), piece, ori, baseHoles, colHeights
                )
                if (score > bestScore) {
                    bestScore = score
                }
            }
        }
        return bestScore
    }

    fun evaluateCandidates(gridMasks: IntArray, piece: Tetromino, topN: Int = 3): List<PlacementHint> {
        val baseHoles = countHoles(gridMasks)
        return evaluateCandidateMoves(gridMasks, piece, baseHoles, topN).map {
            PlacementHint(
                shape = it.shape,
                targetPos = it.targetPos,
                score = it.score,
                holes = it.holes,
                linesCleared = it.linesCleared,
                actionLabel = it.actionLabel
            )
        }
    }

    fun simulateBoardAfterPlacement(masks: IntArray, shape: List<Position>, pos: Position): IntArray {
        val res = masks.clone()
        for (p in shape) {
            val ny = pos.y + p.y
            val nx = pos.x + p.x
            if (ny in 0..21 && nx in 0..9) {
                res[ny] = res[ny] or (1 shl nx)
            }
        }
        var writeRow = 21
        val finalMasks = IntArray(22)
        for (r in 21 downTo 0) {
            if (res[r] != 0x3FF) {
                finalMasks[writeRow] = res[r]
                writeRow--
            }
        }
        return finalMasks
    }

    // ── AI-ПОДСКАЗЧИК: 3-PLY LOOKAHEAD + DELLACHERIE + T-SPIN + PANIC MODE ──
    fun calculateOptimalPlacement(
        grid: List<IntArray>,
        piece: Tetromino,
        holdPiece: Tetromino? = null,
        nextPiece: Tetromino? = null,
        canHold: Boolean = true,
        secondNextPiece: Tetromino? = null
    ): PlacementHint? {
        val gridMasks = IntArray(22)
        for (r in 0..21) {
            var mask = 0
            if (r < grid.size) {
                val rowArr = grid[r]
                for (c in 0..9) {
                    if (c < rowArr.size && rowArr[c] != 0) {
                        mask = mask or (1 shl c)
                    }
                }
            }
            gridMasks[r] = mask
        }

        val baseHoles = countHoles(gridMasks)

        // 1. Ply 1: Оцениваем текущую фигуру (топ-4 лучших кандидата для мультипли-проверки)
        val candidates1 = evaluateCandidateMoves(gridMasks, piece, baseHoles, topN = 4)
        if (candidates1.isEmpty()) return null

        var bestHint: CandidateMove = candidates1[0]
        var bestCombinedScore = -1_000_000.0

        // MULTI-PLY LOOKAHEAD: Ply 1 (piece) -> Ply 2 (nextPiece) -> Ply 3 (secondNextPiece)
        for (cand1 in candidates1) {
            var branchScore = cand1.score

            if (nextPiece != null) {
                val baseHoles2 = cand1.holes
                val candidates2 = evaluateCandidateMoves(cand1.resultingMasks, nextPiece, baseHoles2, topN = 2)

                if (candidates2.isNotEmpty()) {
                    var bestPly2Combined = -1_000_000.0
                    for (cand2 in candidates2) {
                        val score3 = if (secondNextPiece != null) {
                            val baseHoles3 = cand2.holes
                            evaluateBestScoreOnly(cand2.resultingMasks, secondNextPiece, baseHoles3)
                        } else 0.0

                        val ply2Combined = cand2.score + (score3 * 0.35)
                        if (ply2Combined > bestPly2Combined) {
                            bestPly2Combined = ply2Combined
                        }
                    }
                    branchScore = cand1.score + (bestPly2Combined * 0.60)
                } else {
                    // Блокировка / Game Over на следующем ходу
                    branchScore -= 20_000.0
                }
            }

            if (branchScore > bestCombinedScore) {
                bestCombinedScore = branchScore
                bestHint = cand1
            }
        }

        // 2. Оценка Hold кандидата с lookahead
        if (canHold) {
            val candidateHoldPiece = holdPiece ?: nextPiece
            if (candidateHoldPiece != null) {
                val holdCandidates = evaluateCandidateMoves(gridMasks, candidateHoldPiece, baseHoles, topN = 2)
                val holdBest = holdCandidates.firstOrNull()
                if (holdBest != null) {
                    var holdCombinedScore = holdBest.score
                    val followingPiece = if (holdPiece != null) piece else (secondNextPiece ?: piece)
                    val followingScore = evaluateBestScoreOnly(holdBest.resultingMasks, followingPiece, holdBest.holes)
                    holdCombinedScore += followingScore * 0.55

                    val currentCreatesHoles = bestHint.holes > baseHoles && holdBest.holes <= baseHoles
                    val holdGivesTetris = holdPiece != null && holdBest.linesCleared == 4 && bestHint.linesCleared < 4
                    val holdMuchBetter = holdCombinedScore > bestCombinedScore + 200.0

                    if (currentCreatesHoles || holdGivesTetris || holdMuchBetter) {
                        return PlacementHint(
                            shape = holdBest.shape,
                            targetPos = holdBest.targetPos,
                            score = holdCombinedScore,
                            shouldHold = true,
                            holdReason = when {
                                currentCreatesHoles -> "Текущая фигура создает просвет"
                                holdGivesTetris -> "В холде фигура для Тетриса"
                                else -> "Фигура из холда дает лучшее размещение"
                            },
                            holes = holdBest.holes,
                            linesCleared = holdBest.linesCleared,
                            actionLabel = "ХОЛД"
                        )
                    }
                }
            }
        }

        val label = when {
            bestHint.linesCleared == 4 -> "ТЕТРИС!"
            bestHint.actionLabel.isNotEmpty() -> bestHint.actionLabel
            bestHint.linesCleared in 2..3 -> "ЧИСТКА"
            bestHint.holes == 0 && bestCombinedScore > 200.0 -> "СТЭК"
            else -> "ХОД"
        }
        return PlacementHint(
            shape = bestHint.shape,
            targetPos = bestHint.targetPos,
            score = bestCombinedScore,
            shouldHold = false,
            holes = bestHint.holes,
            linesCleared = bestHint.linesCleared,
            actionLabel = label
        )
    }
}
