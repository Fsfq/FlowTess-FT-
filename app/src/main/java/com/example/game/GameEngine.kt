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
    EXTENDED("extended", "Spectrum", "Спектр"),
    FAST_RUN("fast_run", "Sprint", "Спринт"),
    REVERSE_CONTROLS("reverse", "Inversion", "Инверсия"),
    BLOCK_BLAST("block_blast", "Zeta", "Zeta"),
    ZEN_FLOW("zen", "Zen", "Дзен"),
    TIME_ATTACK("time_attack", "Blitz", "Блиц"),
    PULSE_EXTREME("pulse_extreme", "Tide", "Прилив"),
    MIRROR_DIMENSION("mirror", "Mirror", "Зеркало"),
    PENTARY_CHAOS("penta", "Chaos", "Хаос"),
    RELAX("relax", "Sandbox", "Песочница"),
    PERFECTIONIST("perfectionist", "Perfection", "Идеал")
}

data class PlacementHint(
    val shape: List<Position>,
    val targetPos: Position,
    val score: Double,
    val shouldHold: Boolean = false,
    val holdReason: String = "",
    val holes: Int = 0,
    val linesCleared: Int = 0
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

    // AI-подсказчик для режима Перфекционист — глубокая эвристика Dellacherie + Hold recommendation
    fun calculateOptimalPlacement(
        grid: List<IntArray>,
        piece: Tetromino,
        holdPiece: Tetromino? = null,
        nextPiece: Tetromino? = null,
        canHold: Boolean = true
    ): PlacementHint? {
        // Конвертируем grid в 10-битные маски строк (rows 0..21)
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

        // 1. Оцениваем текущую фигуру
        val currentBest = evaluateBestPlacementForPiece(gridMasks, piece) ?: return null

        // 2. Если можно делать Hold, оцениваем кандидата из Hold
        if (canHold) {
            val candidateHoldPiece = holdPiece ?: nextPiece
            if (candidateHoldPiece != null) {
                val holdBest = evaluateBestPlacementForPiece(gridMasks, candidateHoldPiece)
                if (holdBest != null) {
                    // Строгие условия рекомендации Hold:
                    // 1. Текущая фигура создает новые дыры (holes > 0), а фигура из холда ставится идеально чисто (0 дыр)
                    val currentCreatesHoles = currentBest.holes > 0 && holdBest.holes == 0
                    // 2. В реальном холде есть фигура (holdPiece != null), которая дает Tetris (4 линии), а текущая нет
                    val holdGivesTetris = holdPiece != null && holdBest.linesCleared == 4 && currentBest.linesCleared < 4
                    // 3. В реальном холде есть фигура, которая дает очистку линий при отсутствии очистки у текущей и значительном преимуществе скора (> 350.0)
                    val holdGivesCleanClear = holdPiece != null && currentBest.linesCleared == 0 && holdBest.linesCleared >= 2 && (holdBest.score - currentBest.score > 350.0)

                    if (currentCreatesHoles || holdGivesTetris || holdGivesCleanClear) {
                        return currentBest.copy(
                            shouldHold = true,
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

        return currentBest
    }

    private fun evaluateBestPlacementForPiece(gridMasks: IntArray, piece: Tetromino): PlacementHint? {
        val maxRotations = when (piece.colorIndex) {
            4 -> 1 // Квадрат не вращается
            1, 5, 7, 10 -> 2 // I, S, Z, dot: 2 положения
            else -> 4 // T, L, J, и др.: 4 положения
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

        var bestHint: PlacementHint? = null
        var maxScore = -1_000_000.0
        val simMasks = IntArray(22)
        val colHeights = IntArray(10)

        for (rotShape in rotations) {
            val minX = rotShape.minOf { it.x }
            val maxX = rotShape.maxOf { it.x }

            for (posX in (0 - minX)..(9 - maxX)) {
                // Ищем точку падения hardDrop с битовой проверкой коллизий
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

                    // Копируем исходные маски
                    System.arraycopy(gridMasks, 0, simMasks, 0, 22)

                    // Накладываем фигуру
                    for (p in rotShape) {
                        val ny = targetPos.y + p.y
                        val nx = targetPos.x + p.x
                        if (ny in 0..21 && nx in 0..9) {
                            simMasks[ny] = simMasks[ny] or (1 shl nx)
                        }
                    }

                    // 1. Считаем заполненные линии (rowMask == 0x3FF)
                    var completeLines = 0
                    for (r in 0..21) {
                        if (simMasks[r] == 0x3FF) {
                            completeLines++
                        }
                    }

                    // 2. Высоты столбцов (0..9)
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

                    // 3. Подсчет дырок (Holes) и глубины захоронения (Hole Depth)
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

                    // 4. Переходы строк (Row Transitions)
                    var rowTransitions = 0
                    val startRow = (22 - maxHeight).coerceAtLeast(0)
                    for (r in startRow..21) {
                        val m = simMasks[r]
                        // граница слева
                        if ((m and 1) == 0) rowTransitions++
                        // биты между собой
                        for (c in 0..8) {
                            val b1 = (m shr c) and 1
                            val b2 = (m shr (c + 1)) and 1
                            if (b1 != b2) rowTransitions++
                        }
                        // граница справа
                        if ((m and (1 shl 9)) == 0) rowTransitions++
                    }

                    // 5. Переходы столбцов (Column Transitions)
                    var colTransitions = 0
                    for (c in 0..9) {
                        val bit = 1 shl c
                        var prevBit = 0 // верхняя граница пустая
                        for (r in 0..21) {
                            val curBit = if ((simMasks[r] and bit) != 0) 1 else 0
                            if (curBit != prevBit) colTransitions++
                            prevBit = curBit
                        }
                        // нижняя граница (пол) заполнена (1)
                        if (prevBit != 1) colTransitions++
                    }

                    // 6. Неровность рельефа (Bumpiness)
                    var bumpiness = 0
                    for (c in 0..8) {
                        bumpiness += kotlin.math.abs(colHeights[c] - colHeights[c + 1])
                    }

                    // 7. Контроль колодцев (Wells)
                    var wellsPenalty = 0
                    for (c in 0..9) {
                        val leftH = if (c > 0) colHeights[c - 1] else 22
                        val rightH = if (c < 9) colHeights[c + 1] else 22
                        val minAdjacent = kotlin.math.min(leftH, rightH)
                        val wellDepth = minAdjacent - colHeights[c]
                        if (wellDepth > 2) {
                            val isEdgeWell = (c == 0 || c == 9)
                            wellsPenalty += if (isEdgeWell) wellDepth * 4 else wellDepth * 18
                        }
                    }

                    // Бонус за сбор линий
                    val lineClearBonus = when (completeLines) {
                        4 -> 850.0 // TETRIS
                        3 -> 380.0
                        2 -> 160.0
                        1 -> 40.0
                        else -> 0.0
                    }

                    // Формула Dellacherie Enhanced
                    val evalScore = lineClearBonus -
                            (holes * 130.0) -
                            (holeDepth * 35.0) -
                            (rowTransitions * 8.0) -
                            (colTransitions * 12.0) -
                            (bumpiness * 6.0) -
                            (aggregateHeight * 2.2) -
                            (maxHeight * 5.0) -
                            (wellsPenalty * 3.0) +
                            (landingY * 3.5)

                    if (evalScore > maxScore) {
                        maxScore = evalScore
                        bestHint = PlacementHint(
                            shape = rotShape,
                            targetPos = targetPos,
                            score = evalScore,
                            holes = holes,
                            linesCleared = completeLines
                        )
                    }
                }
            }
        }

        return bestHint
    }
}
