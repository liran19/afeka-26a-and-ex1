package com.example.homeex1

import kotlin.random.Random


enum class CellType(val isVisible: Boolean) {
    EMPTY(false),
    OBSTACLE(true),
    // Can add here stuff like COINS, HEARTS, BOOSTS, etc - for future use.
}

enum class GameEvent {
    NONE,
    HIT,
    GAME_OVER
}

class GameState(val config: GameConfig) {
    // grid[row][col] -> what is inside that cell
    val grid: Array<Array<CellType>> =
        Array(config.rows) { Array(config.cols) { CellType.EMPTY } }
    val player = Player(cols = config.cols, startLives = config.startLives)

    var isGameOver: Boolean = false

    fun reset() {
        for (r in 0 until config.rows){
            for (c in 0 until config.cols) {
                grid[r][c] = CellType.EMPTY
            }
        }
        isGameOver = false
        player.reset()
    }


    fun movePlayerLeft() {
        player.moveLeft()
    }

    fun movePlayerRight() {
        player.moveRight()
    }

    // Game "tick" logic
    fun step(): GameEvent {
        if (isGameOver) return GameEvent.NONE

        var event = GameEvent.NONE

        // Check bottom row cell
        val bottomRowIndex = config.rows - 1
        val playerCol = player.getCol()
        val cellUnderPlayer = grid[bottomRowIndex][playerCol]

        if(cellUnderPlayer == CellType.OBSTACLE){
            val dead = player.takeHit()

            grid[bottomRowIndex][playerCol] = CellType.EMPTY

            event = if (dead) GameEvent.GAME_OVER else GameEvent.HIT
            if (dead) {
                isGameOver = true
            }
        }

        // Move down
        moveCellsDown()

        // Spawn new row at the top
        spawnNewRow()

        return event

    }
    private fun moveCellsDown() {
        for (r in config.rows - 1 downTo 1) {
            for (c in 0 until config.cols) {
                grid[r][c] = grid[r - 1][c]
            }
        }
        // clear the top row
        for (c in 0 until config.cols) {
            grid[0][c] = CellType.EMPTY
        }
    }

    private fun spawnNewRow() {
        if (Random.nextFloat() < config.spawnChance) {
            val lane = Random.nextInt(config.cols)
            grid[0][lane] = CellType.OBSTACLE
        }
    }

}