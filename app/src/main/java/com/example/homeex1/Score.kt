package com.example.homeex1

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a player's game score with location
 */
data class Score(
    val playerName: String,
    val distance: Int,
    val score: Int,
    val totalScore: Int,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val gameMode: String,
    val isFastMode: Boolean
) {
    companion object {
        private const val DATE_FORMAT_PATTERN = "dd/MM/yyyy HH:mm"
        private val gson = Gson()

        fun fromJson(json: String): Score {
            return gson.fromJson(json, Score::class.java)
        }
    }

    fun toJson(): String {
        return gson.toJson(this)
    }

    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
        return formatter.format(date)
    }
}

