package com.example.game

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

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
    MEMORY_PUZZLE("memory", "Memory", "Память"),
    SLIDE_PUZZLE("slide", "Slide", "Слайдер")
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
    var relaxBlockSet: String = "ideal" // only_i, ideal, standard, all
    
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
        var newPos = state.currentPos
        val piece = state.currentPiece
        if (piece != null) {
            while (!isValidMove(newPos, piece, grid) && newPos.y > -2) {
                newPos = newPos.copy(y = newPos.y - 1)
            }
        }
        _gameState.update { it.copy(grid = grid, currentPos = newPos) }
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

    // Движение влево (инверсия для MIRROR) / move left (reversed in mirror mode)
    fun moveLeft() {
        val state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        val isReverse = state.gameMode == GameMode.MIRROR_DIMENSION
        val nextX = if (isReverse) state.currentPos.x + 1 else state.currentPos.x - 1
        if (isValidMove(state.currentPos.copy(x = nextX), state.currentPiece, state.grid)) {
            _gameState.update { it.copy(currentPos = it.currentPos.copy(x = nextX)) }
        }
    }

    // Движение вправо / move right
    fun moveRight() {
        val state = _gameState.value
        if (state.isGameOver || state.currentPiece == null) return
        val isReverse = state.gameMode == GameMode.MIRROR_DIMENSION
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
        val piece = state.currentPiece ?: return
        if (state.isGameOver) return

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

        if (isValidMove(state.currentPos, rotatedPiece, state.grid)) {
            _gameState.update { it.copy(currentPiece = rotatedPiece) }
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
            else -> 0
        }
        val addedScore = basePoints * state.level
        val addedTime = if (state.gameMode == GameMode.TIME_ATTACK) cleared * 10 else 0

        // Проверка смерти — блоки в top-3 рядах = game over (кроме Relax)
        // death check: blocks in top 3 rows = game over (except Relax)
        var isOver = false
        val nextP = state.nextPieces.firstOrNull() ?: nextPiece(state.isExtendedMode)
        val blocksAtTop = newGrid[0].any { it != 0 } || newGrid[1].any { it != 0 } || newGrid[2].any { it != 0 }
        if (!isValidMove(Position(4, 0), nextP, newGrid) || blocksAtTop) {
            if (state.gameMode == GameMode.RELAX && relaxImmortal) {
                // Relax бессмертие — очищаем поле при заполнении / immortal: clear board on fill
                newGrid.clear()
                for (i in 0 until 22) {
                    newGrid.add(0, IntArray(10))
                }
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
                        score = it.score + (state.puzzleLevel * 2000),
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

    // ── AI-ПОДСКАЗЧИК: 2-PLY LOOKAHEAD + DELLACHERIE + T-SPIN + PANIC MODE ──
    fun calculateOptimalPlacement(
        grid: List<IntArray>,
        piece: Tetromino,
        holdPiece: Tetromino? = null,
        nextPiece: Tetromino? = null,
        canHold: Boolean = true
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

        // 1. Оцениваем текущую фигуру (топ-3 лучших кандидата для 2-ply проверки)
        val candidates = evaluateCandidates(gridMasks, piece, topN = 3)
        if (candidates.isEmpty()) return null

        var bestHint = candidates[0]
        var bestCombinedScore = -1_000_000.0

        // 2-PLY LOOKAHEAD: для каждого из лучших ходов симулируем доску и проверяем следующий блок (nextPiece)
        for (cand in candidates) {
            val simMasks = simulateBoardAfterPlacement(gridMasks, cand.shape, cand.targetPos)
            val nextBestScore = if (nextPiece != null) {
                val nextCand = evaluateCandidates(simMasks, nextPiece, topN = 1)
                nextCand.firstOrNull()?.score ?: 0.0
            } else 0.0

            val combined = cand.score + (nextBestScore * 0.65)
            if (combined > bestCombinedScore) {
                bestCombinedScore = combined
                bestHint = cand.copy(score = combined)
            }
        }

        // 2. Оценка Hold кандидата
        if (canHold) {
            val candidateHoldPiece = holdPiece ?: nextPiece
            if (candidateHoldPiece != null) {
                val holdCandidates = evaluateCandidates(gridMasks, candidateHoldPiece, topN = 1)
                val holdBest = holdCandidates.firstOrNull()
                if (holdBest != null) {
                    val currentCreatesHoles = bestHint.holes > 0 && holdBest.holes == 0
                    val holdGivesTetris = holdPiece != null && holdBest.linesCleared == 4 && bestHint.linesCleared < 4
                    val holdGivesCleanClear = holdPiece != null && bestHint.linesCleared == 0 && holdBest.linesCleared >= 2 && (holdBest.score - bestHint.score > 250.0)

                    if (currentCreatesHoles || holdGivesTetris || holdGivesCleanClear) {
                        return bestHint.copy(
                            shouldHold = true,
                            actionLabel = "ХОЛД",
                            holdReason = when {
                                currentCreatesHoles -> "Текущая фигура создает просвет"
                                holdGivesTetris -> "В холде фигура для Тетриса"
                                else -> "Фигура из холда дает комбо"
                            }
                        )
                    }
                }
            }
        }

        val label = when {
            bestHint.linesCleared == 4 -> "ТЕТРИС!"
            bestHint.linesCleared in 2..3 -> "ЧИСТКА"
            bestHint.actionLabel.isNotEmpty() -> bestHint.actionLabel
            bestHint.holes == 0 && bestHint.score > 150.0 -> "СТЭК"
            else -> "ХОД"
        }
        return bestHint.copy(actionLabel = label)
    }

    private fun simulateBoardAfterPlacement(masks: IntArray, shape: List<Position>, pos: Position): IntArray {
        val res = masks.clone()
        for (p in shape) {
            val ny = pos.y + p.y
            val nx = pos.x + p.x
            if (ny in 0..21 && nx in 0..9) {
                res[ny] = res[ny] or (1 shl nx)
            }
        }
        // Очистка заполненных линий (row == 0x3FF)
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

    private fun evaluateCandidates(gridMasks: IntArray, piece: Tetromino, topN: Int = 3): List<PlacementHint> {
        val maxRotations = when (piece.colorIndex) {
            4 -> 1
            1, 5, 7, 10 -> 2
            else -> 4
        }

        val rotations = mutableListOf<List<Position>>()
        var curShape = piece.shape
        for (r in 0 until maxRotations) {
            if (!rotations.any { rot -> rot.toSet() == curShape.toSet() }) {
                rotations.add(curShape)
            }
            curShape = curShape.map { p ->
                val rx = p.x - piece.pivot.x
                val ry = p.y - piece.pivot.y
                Position(piece.pivot.x - ry, piece.pivot.y + rx)
            }
        }

        val simMasks = IntArray(22)
        val colHeights = IntArray(10)
        val evaluatedList = mutableListOf<PlacementHint>()

        for (rotShape in rotations) {
            val minX = rotShape.minOf { it.x }
            val maxX = rotShape.maxOf { it.x }

            for (posX in (0 - minX)..(9 - maxX)) {
                var landingY = -1
                for (posY in 0..21) {
                    var collision = false
                    for (p in rotShape) {
                        val nx = posX + p.x
                        val ny = posY + p.y
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

                if (landingY >= 0) {
                    val targetPos = Position(posX, landingY)
                    System.arraycopy(gridMasks, 0, simMasks, 0, 22)

                    for (p in rotShape) {
                        val ny = targetPos.y + p.y
                        val nx = targetPos.x + p.x
                        if (ny in 0..21 && nx in 0..9) {
                            simMasks[ny] = simMasks[ny] or (1 shl nx)
                        }
                    }

                    // 1. Полные линии
                    var completeLines = 0
                    for (r in 0..21) {
                        if (simMasks[r] == 0x3FF) completeLines++
                    }

                    // 2. Высоты столбцов
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

                    // 3. Дырки и глубина
                    var holes = 0
                    var holeDepth = 0
                    for (c in 0..9) {
                        val bit = 1 shl c
                        var blocksAbove = 0
                        for (r in 0..21) {
                            if ((simMasks[r] and bit) != 0) {
                                blocksAbove++
                            } else if (blocksAbove > 0) {
                                holes++
                                holeDepth += blocksAbove
                            }
                        }
                    }

                    // 4. Переходы строк
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

                    // 5. Переходы столбцов
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

                    // 6. Неровность
                    var bumpiness = 0
                    for (c in 0..8) {
                        bumpiness += kotlin.math.abs(colHeights[c] - colHeights[c + 1])
                    }

                    // 7. Контроль глубоких щелей (Crevices)
                    var crevicePenalty = 0
                    for (c in 0..9) {
                        val leftH = if (c > 0) colHeights[c - 1] else 22
                        val rightH = if (c < 9) colHeights[c + 1] else 22
                        val minAdj = kotlin.math.min(leftH, rightH)
                        val depth = minAdj - colHeights[c]
                        if (depth >= 3) {
                            crevicePenalty += depth * 22
                        }
                    }

                    // 8. T-spin слот бонус
                    var isTspinSlot = false
                    if (piece.colorIndex == 6) { // T-фигура
                        var cornersFilled = 0
                        val py = targetPos.y + piece.pivot.y
                        val px = targetPos.x + piece.pivot.x
                        for (dx in listOf(-1, 1)) {
                            for (dy in listOf(-1, 1)) {
                                val cy = py + dy
                                val cx = px + dx
                                if (cx !in 0..9 || cy >= 22 || (cy >= 0 && (gridMasks[cy] and (1 shl cx)) != 0)) {
                                    cornersFilled++
                                }
                            }
                        }
                        if (cornersFilled >= 3) isTspinSlot = true
                    }

                    // 9. Tetris-колодец (Колонка 9 чистая при низкой высоте)
                    var tetrisWellBonus = 0.0
                    if (maxHeight < 12 && colHeights[9] < colHeights[8] - 1) {
                        tetrisWellBonus = 90.0
                    }

                    // Базовые очки за линии
                    val isPanic = maxHeight >= 13
                    val lineClearBonus = when (completeLines) {
                        4 -> 950.0
                        3 -> if (isPanic) 500.0 else 380.0
                        2 -> if (isPanic) 280.0 else 160.0
                        1 -> if (isPanic) 120.0 else 40.0
                        else -> 0.0
                    }

                    val heightPenalty = if (isPanic) (maxHeight * 16.0) + (aggregateHeight * 3.5)
                                        else (maxHeight * 5.0) + (aggregateHeight * 2.0)

                    val evalScore = lineClearBonus -
                            (holes * 140.0) -
                            (holeDepth * 35.0) -
                            (rowTransitions * 8.0) -
                            (colTransitions * 12.0) -
                            (bumpiness * 6.0) -
                            heightPenalty -
                            crevicePenalty +
                            (landingY * 3.5) +
                            tetrisWellBonus +
                            (if (isTspinSlot) 150.0 else 0.0)

                    evaluatedList.add(
                        PlacementHint(
                            shape = rotShape,
                            targetPos = targetPos,
                            score = evalScore,
                            holes = holes,
                            linesCleared = completeLines,
                            actionLabel = if (isTspinSlot) "T-SPIN" else ""
                        )
                    )
                }
            }
        }

        evaluatedList.sortByDescending { it.score }
        return evaluatedList.take(topN)
    }
}
