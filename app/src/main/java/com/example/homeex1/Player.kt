package com.example.homeex1

class Player(
    private val cols: Int,
    private val startLives: Int
) {
    private var col: Int = 0
    private var lives: Int = 0

    init {
        reset()
    }

    fun reset() {
        col = cols / 2
        lives = startLives
    }

    fun getCol(): Int {
        return col
    }

    fun getLives(): Int {
        return lives
    }

    fun moveLeft() {
        // Only move if not at the left border
        if (col > 0) {
            col--
        }
    }

    fun moveRight() {
        // Only move if not at the right border
        if (col < cols - 1) {
            col++
        }
    }

    fun takeHit(): Boolean {
        if (lives > 0) {
            lives--
        }
        return lives <= 0
    }
}

