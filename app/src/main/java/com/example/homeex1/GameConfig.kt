package com.example.homeex1

data class GameConfig(
    val cols: Int = 3,
    val rows: Int = 3,
    val startLives: Int = 3,
    val spawnChance: Float = 0.7f
)