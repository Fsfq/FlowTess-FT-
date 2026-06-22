package com.example.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class BlockBlastPosition(val r: Int, val c: Int)

data class BlockBlastFigure(
    val id: String,
    val blocks: List<BlockBlastPosition>, // positions relative to top-left of its conceptual bounding box
    val colorIndex: Int,
    val rowsCount: Int,
    val colsCount: Int
)

data class BlockBlastState(
    val grid: List<IntArray> = List(8) { IntArray(8) },
    val pool: List<BlockBlastFigure?> = listOf(null, null, null),
    val score: Int = 0,
    val combo: Int = 0,
    val isGameOver: Boolean = false,
    val highScore: Int = 0
)

class BlockBlastEngine {
    private val _state = MutableStateFlow(BlockBlastState())
    val state: StateFlow<BlockBlastState> = _state.asStateFlow()

    private val figuresLibrary = listOf(
        // 1x1 Dot
        BlockBlastFigure("1x1", listOf(BlockBlastPosition(0, 0)), 1, 1, 1),
        // 1x2 vertical
        BlockBlastFigure("1x2_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0)), 2, 2, 1),
        // 2x1 horizontal
        BlockBlastFigure("2x1_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1)), 2, 1, 2),
        // 1x3 vertical
        BlockBlastFigure("1x3_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0)), 3, 3, 1),
        // 3x1 horizontal
        BlockBlastFigure("3x1_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2)), 3, 1, 3),
        // 2x2 Block
        BlockBlastFigure("2x2", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1)), 4, 2, 2),
        // L-Shape Corner 2x2
        BlockBlastFigure("L_2x2", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(1, 1)), 5, 2, 2),
        // Rev L-Shape Corner 2x2
        BlockBlastFigure("rL_2x2", listOf(BlockBlastPosition(0, 1), BlockBlastPosition(1, 1), BlockBlastPosition(1, 0)), 6, 2, 2),
        // T-Shape 3x2
        BlockBlastFigure("T_3x2", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(1, 1)), 7, 2, 3),
        // 1x4 vertical
        BlockBlastFigure("1x4_v", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(1, 0), BlockBlastPosition(2, 0), BlockBlastPosition(3, 0)), 8, 4, 1),
        // 4x1 horizontal
        BlockBlastFigure("4x1_h", listOf(BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2), BlockBlastPosition(0, 3)), 8, 1, 4),
        // 3x3 big square
        BlockBlastFigure("3x3", listOf(
            BlockBlastPosition(0, 0), BlockBlastPosition(0, 1), BlockBlastPosition(0, 2),
            BlockBlastPosition(1, 0), BlockBlastPosition(1, 1), BlockBlastPosition(1, 2),
            BlockBlastPosition(2, 0), BlockBlastPosition(2, 1), BlockBlastPosition(2, 2)
        ), 9, 3, 3)
    )

    fun startGame(savedHighScore: Int = 0) {
        _state.update {
            BlockBlastState(
                grid = List(8) { IntArray(8) },
                pool = generateNewPool(),
                score = 0,
                combo = 0,
                isGameOver = false,
                highScore = savedHighScore
            )
        }
    }

    private fun generateTrulyRandomFigure(): BlockBlastFigure {
        val size = (1..5).random() // polyomino size (1 to 5 blocks)
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
                    if (next !in blocks) {
                        candidates.add(next)
                    }
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
        val rowsCount = maxR + 1
        val colsCount = maxC + 1
        
        val colorIndex = (1..9).random()
        return BlockBlastFigure(
            id = "procedural_${Random.nextInt(1000000)}",
            blocks = shiftedBlocks,
            colorIndex = colorIndex,
            rowsCount = rowsCount,
            colsCount = colsCount
        )
    }

    private fun generateNewPool(): List<BlockBlastFigure> {
        return List(3) {
            // Guarantee 100% variety and infinite novelty with procedural figures!
            generateTrulyRandomFigure()
        }
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

        val newPool = s.pool.toMutableList()
        newPool[idx] = null

        val isPoolEmpty = newPool.all { it == null }
        val finalPool = if (isPoolEmpty) generateNewPool() else newPool

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

        val placementScore = figure.blocks.size * 10
        val lineClearScore = when (totalCleared) {
            0 -> 0
            1 -> 150
            2 -> 400
            3 -> 800
            4 -> 1400
            else -> totalCleared * 400
        }
        val currentCombo = if (totalCleared > 0) s.combo + 1 else 0
        val comboBonus = if (currentCombo > 1) (currentCombo - 1) * 30 else 0
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
            for (r in 0..7) {
                for (c in 0..7) {
                    if (canPlaceFigure(figure, r, c, grid)) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
