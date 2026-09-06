package com.example.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class SlideBlock(
    val id: String = UUID.randomUUID().toString(),
    val row: Int,
    val col: Int,
    val length: Int,
    val colorIndex: Int
)

data class SlidePuzzleState(
    val blocks: List<SlideBlock> = emptyList(),
    val score: Int = 0,
    val linesCleared: Int = 0,
    val combo: Int = 1,
    val isGameOver: Boolean = false,
    val lastClearedRowCount: Int = 0
)

class SlidePuzzleEngine {
    companion object {
        const val COLS = 8
        const val ROWS = 10
    }

    private val _state = MutableStateFlow(SlidePuzzleState())
    val state: StateFlow<SlidePuzzleState> = _state.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame() {
        val initialBlocks = mutableListOf<SlideBlock>()
        // Spawn 4 initial rows at the bottom (rows 6, 7, 8, 9)
        for (r in 6 until ROWS) {
            initialBlocks.addAll(generateRowBlocks(r))
        }
        _state.value = SlidePuzzleState(
            blocks = initialBlocks,
            score = 0,
            linesCleared = 0,
            combo = 1,
            isGameOver = false
        )
    }

    /**
     * Move a block horizontally to targetCol.
     * Returns true if move succeeded, dropped, and resolved.
     */
    fun moveBlock(blockId: String, targetCol: Int): Boolean {
        val curState = _state.value
        if (curState.isGameOver) return false

        val block = curState.blocks.firstOrNull { it.id == blockId } ?: return false
        if (targetCol == block.col) return false
        if (targetCol < 0 || targetCol + block.length > COLS) return false

        // Check horizontal collision along the row
        val otherBlocksInRow = curState.blocks.filter { it.id != blockId && it.row == block.row }
        for (other in otherBlocksInRow) {
            val otherEnd = other.col + other.length - 1
            if (maxOf(targetCol, other.col) <= minOf(targetCol + block.length - 1, otherEnd)) {
                return false // Blocked by another piece
            }
        }

        // Apply horizontal move
        val updatedBlocks = curState.blocks.map {
            if (it.id == blockId) it.copy(col = targetCol) else it
        }.toMutableList()

        // 1. Resolve gravity drop for falling blocks
        resolveGravity(updatedBlocks)

        // 2. Clear full rows & apply cascading gravity
        var totalCleared = 0
        var comboMultiplier = curState.combo
        while (true) {
            val cleared = clearFullLines(updatedBlocks)
            if (cleared > 0) {
                totalCleared += cleared
                comboMultiplier++
                resolveGravity(updatedBlocks)
            } else {
                break
            }
        }

        val addedScore = when (totalCleared) {
            0 -> 0
            1 -> 100 * comboMultiplier
            2 -> 300 * comboMultiplier
            3 -> 600 * comboMultiplier
            else -> 1000 * comboMultiplier
        }

        // 3. If no lines were cleared by player's turn, push a new row from bottom!
        var isOver = false
        if (totalCleared == 0) {
            comboMultiplier = 1
            // Shift all existing blocks UP by 1 row
            val shiftedBlocks = updatedBlocks.map { it.copy(row = it.row - 1) }.toMutableList()
            // Check if any block went above top ceiling (row < 1)
            if (shiftedBlocks.any { it.row < 1 }) {
                isOver = true
            } else {
                // Add new row at bottom (ROWS - 1 = row 9)
                shiftedBlocks.addAll(generateRowBlocks(ROWS - 1))
                resolveGravity(shiftedBlocks)
                // Check if new row created any clears immediately
                while (true) {
                    val c = clearFullLines(shiftedBlocks)
                    if (c > 0) {
                        resolveGravity(shiftedBlocks)
                    } else {
                        break
                    }
                }
                if (shiftedBlocks.any { it.row < 1 }) {
                    isOver = true
                }
            }
            updatedBlocks.clear()
            updatedBlocks.addAll(shiftedBlocks)
        }

        _state.update {
            it.copy(
                blocks = updatedBlocks,
                score = it.score + addedScore,
                linesCleared = it.linesCleared + totalCleared,
                combo = if (totalCleared > 0) comboMultiplier else 1,
                isGameOver = isOver,
                lastClearedRowCount = totalCleared
            )
        }

        return true
    }

    /**
     * Drops blocks downwards into empty spaces until supported.
     */
    private fun resolveGravity(blocks: MutableList<SlideBlock>) {
        var changed = true
        while (changed) {
            changed = false
            // Sort from bottom to top so lower blocks settle first
            blocks.sortByDescending { it.row }
            for (i in blocks.indices) {
                val b = blocks[i]
                if (b.row >= ROWS - 1) continue // already at the floor
                // Check if cells directly underneath (row + 1, col .. col + length - 1) are all free
                val isSupported = blocks.any { other ->
                    other.id != b.id && other.row == b.row + 1 &&
                            maxOf(b.col, other.col) <= minOf(b.col + b.length - 1, other.col + other.length - 1)
                }
                if (!isSupported) {
                    blocks[i] = b.copy(row = b.row + 1)
                    changed = true
                }
            }
        }
    }

    /**
     * Checks rows 0 until ROWS. If a row has all 8 cells filled, removes it.
     */
    private fun clearFullLines(blocks: MutableList<SlideBlock>): Int {
        var clearedRowsCount = 0
        for (r in ROWS - 1 downTo 0) {
            val filledCols = BooleanArray(COLS)
            for (b in blocks) {
                if (b.row == r) {
                    for (c in b.col until (b.col + b.length)) {
                        if (c in 0 until COLS) filledCols[c] = true
                    }
                }
            }
            if (filledCols.all { it }) {
                clearedRowsCount++
                // Remove all blocks occupying row r
                blocks.removeAll { it.row == r }
            }
        }
        return clearedRowsCount
    }

    /**
     * Generates a random set of blocks for a given row leaving 1 to 3 empty slots.
     */
    private fun generateRowBlocks(row: Int): List<SlideBlock> {
        val result = mutableListOf<SlideBlock>()
        var col = 0
        val colors = listOf(1, 2, 3, 4, 5, 6, 7) // theme block colors

        while (col < COLS) {
            val remaining = COLS - col
            if (remaining <= 1) {
                // Leave empty or place 1x1
                if ((0..1).random() == 0 && remaining == 1) {
                    result.add(SlideBlock(row = row, col = col, length = 1, colorIndex = colors.random()))
                }
                break
            }

            // Decide whether to leave a 1-cell gap
            if (col > 0 && (0..3).random() == 0) {
                col++
                continue
            }

            val maxLen = minOf(4, remaining)
            val len = (1..maxLen).random()
            result.add(SlideBlock(row = row, col = col, length = len, colorIndex = colors.random()))
            col += len
        }

        // Guarantee at least 1 empty spot so row is not full on spawn
        val filled = BooleanArray(COLS)
        for (b in result) {
            for (c in b.col until b.col + b.length) {
                if (c in 0 until COLS) filled[c] = true
            }
        }
        if (filled.all { it } && result.isNotEmpty()) {
            result.removeAt(result.size - 1)
        }

        return result
    }
}
