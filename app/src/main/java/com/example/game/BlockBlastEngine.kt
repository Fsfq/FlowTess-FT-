package com.example.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class BlockBlastPosition(val r: Int, val c: Int)

data class BlockBlastFigure(
    val id: String,
    val blocks: List<BlockBlastPosition>,
    val colorIndex: Int,
    val rowsCount: Int,
    val colsCount: Int,
    val category: String = "classic" // "small", "classic", "large", "special"
)

data class BlockBlastState(
    val grid: List<IntArray> = List(8) { IntArray(8) },
    val pool: List<BlockBlastFigure?> = listOf(null, null, null),
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val linesClearedTotal: Int = 0,
    val lastClearCount: Int = 0,
    val lastClearedRows: List<Int> = emptyList(),
    val lastClearedCols: List<Int> = emptyList(),
    val lastClearedCells: List<Triple<Int, Int, Int>> = emptyList(),
    val isGameOver: Boolean = false,
    val highScore: Int = 0
)

class BlockBlastEngine {
    private val _state = MutableStateFlow(BlockBlastState())
    val state: StateFlow<BlockBlastState> = _state.asStateFlow()

    private val figuresLibrary: List<BlockBlastFigure> = listOf(
        // === 1. SMALL & UTILITY (Rescuers & Flexible) ===
        // 1x1 Dot
        BlockBlastFigure("dot_1x1", listOf(BlockBlastPosition(0, 0)), 9, 1, 1, "small"),
        // 1x2 Horizontal & Vertical
        BlockBlastFigure("line_1x2_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1)), 2, 1, 2, "small"),
        BlockBlastFigure("line_2x1_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0)), 2, 2, 1, "small"),
        // 2x2 Small Corners (all 4 orientations)
        BlockBlastFigure("corner_2x2_tl", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1)), 4, 2, 2, "small"),
        BlockBlastFigure("corner_2x2_tr", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(1, 0)), 4, 2, 2, "small"),
        BlockBlastFigure("corner_2x2_bl", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(1, 0)), 4, 2, 2, "small"),
        BlockBlastFigure("corner_2x2_br", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(1, 1)), 4, 2, 2, "small"),
        // 2x2 Diagonals
        BlockBlastFigure("diag_2_fwd", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 1)), 5, 2, 2, "small"),
        BlockBlastFigure("diag_2_rev", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 0)), 5, 2, 2, "small"),

        // === 2. CLASSIC POLYOMINOES ===
        // 1x3 Lines
        BlockBlastFigure("line_1x3_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2)), 1, 1, 3, "classic"),
        BlockBlastFigure("line_3x1_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0)), 1, 3, 1, "classic"),
        // 2x2 Solid Square
        BlockBlastFigure("square_2x2", listOf(
            BlockBlastPosition(0, 0), BlockBlastPosition(0, 1),
            BlockBlastPosition(1, 0), BlockBlastPosition(1, 1)
        ), 6, 2, 2, "classic"),
        // 1x4 Long Beams
        BlockBlastFigure("line_1x4_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(0, 3)), 1, 1, 4, "classic"),
        BlockBlastFigure("line_4x1_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(3, 0)), 1, 4, 1, "classic"),
        // L-Shapes 3x2 (4 rotations)
        BlockBlastFigure("L_3x2_1", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(2, 1)), 3, 3, 2, "classic"),
        BlockBlastFigure("L_3x2_2", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(2, 1), BlockBlastPosition(2, 0)), 3, 3, 2, "classic"),
        BlockBlastFigure("L_2x3_3", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 0)), 3, 2, 3, "classic"),
        BlockBlastFigure("L_2x3_4", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2)), 3, 2, 3, "classic"),
        // J-Shapes 3x2 (4 rotations)
        BlockBlastFigure("J_3x2_1", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(0, 1)), 7, 3, 2, "classic"),
        BlockBlastFigure("J_3x2_2", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(2, 1), BlockBlastPosition(0, 0)), 7, 3, 2, "classic"),
        BlockBlastFigure("J_2x3_3", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 2)), 7, 2, 3, "classic"),
        BlockBlastFigure("J_2x3_4", listOf(BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2), BlockBlastPosition(0, 2)), 7, 2, 3, "classic"),
        // T-Shapes (4 rotations)
        BlockBlastFigure("T_2x3_down", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 1)), 5, 2, 3, "classic"),
        BlockBlastFigure("T_2x3_up", listOf(BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2), BlockBlastPosition(0, 1)), 5, 2, 3, "classic"),
        BlockBlastFigure("T_3x2_right", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(1, 1)), 5, 3, 2, "classic"),
        BlockBlastFigure("T_3x2_left", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(2, 1), BlockBlastPosition(1, 0)), 5, 3, 2, "classic"),
        // S and Z shapes
        BlockBlastFigure("S_2x3_h", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1)), 4, 2, 3, "classic"),
        BlockBlastFigure("S_3x2_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(2, 1)), 4, 3, 2, "classic"),
        BlockBlastFigure("Z_2x3_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2)), 8, 2, 3, "classic"),
        BlockBlastFigure("Z_3x2_v", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0)), 8, 3, 2, "classic"),

        // === 3. LARGE & SPECIAL (Big Rewards & Challenges) ===
        // 1x5 Super Beams
        BlockBlastFigure("line_1x5_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(0, 3), BlockBlastPosition(0, 4)), 1, 1, 5, "large"),
        BlockBlastFigure("line_5x1_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(3, 0), BlockBlastPosition(4, 0)), 1, 5, 1, "large"),
        // 3x3 Large Corners (5 blocks in all 4 rotations)
        BlockBlastFigure("corner_3x3_1", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(2, 1), BlockBlastPosition(2, 2)), 6, 3, 3, "large"),
        BlockBlastFigure("corner_3x3_2", listOf(BlockBlastPosition(0, 2), BlockBlastPosition(1, 2), BlockBlastPosition(2, 2), BlockBlastPosition(2, 1), BlockBlastPosition(2, 0)), 6, 3, 3, "large"),
        BlockBlastFigure("corner_3x3_3", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0)), 6, 3, 3, "large"),
        BlockBlastFigure("corner_3x3_4", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 2), BlockBlastPosition(2, 2)), 6, 3, 3, "large"),
        // 3x3 Cross (+)
        BlockBlastFigure("cross_3x3", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2), BlockBlastPosition(2, 1)), 7, 3, 3, "large"),
        // U-Shapes (Horseshoe 5 blocks)
        BlockBlastFigure("U_2x3_up", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2), BlockBlastPosition(0, 2)), 3, 2, 3, "large"),
        BlockBlastFigure("U_2x3_down", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 0), BlockBlastPosition(1, 2)), 3, 2, 3, "large"),
        BlockBlastFigure("U_3x2_left", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(2, 1)), 3, 3, 2, "large"),
        BlockBlastFigure("U_3x2_right", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(2, 1), BlockBlastPosition(2, 0)), 3, 3, 2, "large"),
        // 2x3 and 3x2 Solid Rectangles (6 blocks)
        BlockBlastFigure("rect_2x3_h", listOf(
            BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2),
            BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2)
        ), 2, 2, 3, "large"),
        BlockBlastFigure("rect_3x2_v", listOf(
            BlockBlastPosition(0, 0), BlockBlastPosition(0, 1),
            BlockBlastPosition(1, 0), BlockBlastPosition(1, 1),
            BlockBlastPosition(2, 0), BlockBlastPosition(2, 1)
        ), 2, 3, 2, "large"),
        // 3x3 Big Solid Square (9 blocks — Epic clear)
        BlockBlastFigure("square_3x3", listOf(
            BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2),
            BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2),
            BlockBlastPosition(2, 0), BlockBlastPosition(2, 1), BlockBlastPosition(2, 2)
        ), 8, 3, 3, "large"),
        // 3x3 Diagonal Stair
        BlockBlastFigure("diag_3_stair", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 1), BlockBlastPosition(2, 2)), 9, 3, 3, "large")
    )

    fun startGame(savedHighScore: Int = 0) {
        val initialGrid = List(8) { IntArray(8) }
        _state.update {
            BlockBlastState(
                grid = initialGrid,
                pool = generateNewPool(initialGrid),
                score = 0,
                combo = 0,
                maxCombo = 0,
                linesClearedTotal = 0,
                lastClearCount = 0,
                lastClearedRows = emptyList(),
                lastClearedCols = emptyList(),
                lastClearedCells = emptyList(),
                isGameOver = false,
                highScore = savedHighScore
            )
        }
    }

    private fun generateProceduralFigure(): BlockBlastFigure {
        val size = (3..5).random()
        val blocks = mutableListOf(BlockBlastPosition(0, 0))
        val directions = listOf(
            BlockBlastPosition(-1, 0),
            BlockBlastPosition(1, 0),
            BlockBlastPosition(0, -1),
            BlockBlastPosition(0, 1)
        )
        repeat(size - 1) {
            val candidates = mutableSetOf<BlockBlastPosition>()
            for (b in blocks) {
                for (d in directions) {
                    val next = BlockBlastPosition(b.r + d.r, b.c + d.c)
                    if (next !in blocks) candidates.add(next)
                }
            }
            if (candidates.isNotEmpty()) {
                blocks.add(candidates.random())
            }
        }
        val minR = blocks.minOf { it.r }
        val minC = blocks.minOf { it.c }
        val shiftedBlocks = blocks.map { BlockBlastPosition(it.r - minR, it.c - minC) }
        val maxR = shiftedBlocks.maxOf { it.r }
        val maxC = shiftedBlocks.maxOf { it.c }
        val colorIndex = (1..9).random()

        return BlockBlastFigure(
            id = "wildcard_${Random.nextInt(100000)}",
            blocks = shiftedBlocks,
            colorIndex = colorIndex,
            rowsCount = maxR + 1,
            colsCount = maxC + 1,
            category = "special"
        )
    }

    private fun generateNewPool(grid: List<IntArray>): List<BlockBlastFigure> {
        val filledCount = grid.sumOf { row -> row.count { it != 0 } }
        val isCrowded = filledCount > 32
        val isVeryCrowded = filledCount > 46

        val smalls = figuresLibrary.filter { it.category == "small" }
        val classics = figuresLibrary.filter { it.category == "classic" }
        val larges = figuresLibrary.filter { it.category == "large" }

        val pool = mutableListOf<BlockBlastFigure>()

        // Slot 0: Small rescuer / utility piece
        pool.add(smalls.random())

        // Slot 1: Classic piece (or another small if very crowded)
        if (isVeryCrowded) {
            pool.add(smalls.random())
        } else {
            pool.add(classics.random())
        }

        // Slot 2: Large / special / wildcard
        val slot2Candidates = if (isCrowded) {
            smalls + classics
        } else {
            classics + larges
        }
        val slot2 = if (!isCrowded && Random.nextDouble() < 0.15) {
            generateProceduralFigure()
        } else {
            slot2Candidates.random()
        }
        pool.add(slot2)

        // Safety guarantee: Make sure at least one figure in the pool CAN be placed on the board!
        val canFitAny = pool.any { fig -> canFigureFitAnywhere(fig, grid) }
        if (!canFitAny) {
            val fittingSmall = smalls.shuffled().firstOrNull { canFigureFitAnywhere(it, grid) }
            if (fittingSmall != null) {
                pool[0] = fittingSmall
            } else {
                pool[0] = figuresLibrary.first { it.id == "dot_1x1" }
            }
        }

        return pool
    }

    fun canFigureFitAnywhere(figure: BlockBlastFigure, grid: List<IntArray>): Boolean {
        for (r in 0..(8 - figure.rowsCount)) {
            for (c in 0..(8 - figure.colsCount)) {
                if (canPlaceFigure(figure, r, c, grid)) return true
            }
        }
        return false
    }

    fun canPlaceFigure(figure: BlockBlastFigure, startRow: Int, startCol: Int, grid: List<IntArray>): Boolean {
        if (startRow < 0 || startCol < 0 || startRow + figure.rowsCount > 8 || startCol + figure.colsCount > 8) {
            return false
        }
        for (pos in figure.blocks) {
            val targetRow = startRow + pos.r
            val targetCol = startCol + pos.c
            if (grid[targetRow][targetCol] != 0) {
                return false
            }
        }
        return true
    }

    fun placeFigure(idx: Int, targetRow: Int, targetCol: Int, onLinesCleared: (Int) -> Unit = {}): Boolean {
        val s = _state.value
        if (s.isGameOver) return false
        val figure = s.pool.getOrNull(idx) ?: return false

        if (!canPlaceFigure(figure, targetRow, targetCol, s.grid)) {
            return false
        }

        val newGrid = s.grid.map { it.clone() }
        for (pos in figure.blocks) {
            newGrid[targetRow + pos.r][targetCol + pos.c] = figure.colorIndex
        }

        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0..7) {
            if (newGrid[r].all { it != 0 }) {
                rowsToClear.add(r)
            }
        }
        for (c in 0..7) {
            var colComplete = true
            for (r in 0..7) {
                if (newGrid[r][c] == 0) {
                    colComplete = false
                    break
                }
            }
            if (colComplete) {
                colsToClear.add(c)
            }
        }

        val totalCleared = rowsToClear.size + colsToClear.size
        val clearedCellsWithColors = mutableListOf<Triple<Int, Int, Int>>()
        if (totalCleared > 0) {
            for (r in rowsToClear) {
                for (c in 0..7) {
                    clearedCellsWithColors.add(Triple(r, c, newGrid[r][c]))
                }
            }
            for (c in colsToClear) {
                for (r in 0..7) {
                    if (r !in rowsToClear) {
                        clearedCellsWithColors.add(Triple(r, c, newGrid[r][c]))
                    }
                }
            }
        }

        for (r in rowsToClear) {
            for (c in 0..7) {
                newGrid[r][c] = 0
            }
        }
        for (c in colsToClear) {
            for (r in 0..7) {
                newGrid[r][c] = 0
            }
        }

        val newPool = s.pool.toMutableList()
        newPool[idx] = null
        val isPoolEmpty = newPool.all { it == null }
        val finalPool = if (isPoolEmpty) generateNewPool(newGrid) else newPool

        val placementScore = figure.blocks.size * 5
        val lineClearScore = when (totalCleared) {
            0 -> 0
            1 -> 75
            2 -> 200
            3 -> 400
            4 -> 700
            else -> totalCleared * 200
        }
        val currentCombo = if (totalCleared > 0) s.combo + 1 else 0
        val comboBonus = if (currentCombo > 1) (currentCombo - 1) * 20 else 0
        val addedScore = placementScore + lineClearScore + comboBonus
        val finalScore = s.score + addedScore

        val isNowGameOver = checkIfNoValidMovesLeft(finalPool, newGrid)
        val newHighScore = if (finalScore > s.highScore) finalScore else s.highScore

        if (totalCleared > 0) {
            onLinesCleared(totalCleared)
        }

        _state.update {
            BlockBlastState(
                grid = newGrid,
                pool = finalPool,
                score = finalScore,
                combo = currentCombo,
                maxCombo = maxOf(s.maxCombo, currentCombo),
                linesClearedTotal = s.linesClearedTotal + totalCleared,
                lastClearCount = totalCleared,
                lastClearedRows = rowsToClear,
                lastClearedCols = colsToClear,
                lastClearedCells = clearedCellsWithColors,
                isGameOver = isNowGameOver,
                highScore = newHighScore
            )
        }
        return true
    }

    private fun checkIfNoValidMovesLeft(pool: List<BlockBlastFigure?>, grid: List<IntArray>): Boolean {
        val activeFigures = pool.filterNotNull()
        if (activeFigures.isEmpty()) return false

        for (figure in activeFigures) {
            if (canFigureFitAnywhere(figure, grid)) {
                return false
            }
        }
        return true
    }
}
