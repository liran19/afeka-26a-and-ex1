package com.example.homeex1

data class GameConfig(
    val cols: Int = 5,
    val rows: Int = 8,
    val startLives: Int = 3,
    val spawnChance: Float = 0.7f
)