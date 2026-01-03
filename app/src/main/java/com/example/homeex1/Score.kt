package com.example.homeex1

import org.json.JSONException
import org.json.JSONObject
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
        // JSON keys as constants for type safety
        private const val KEY_PLAYER_NAME = "playerName"
        private const val KEY_DISTANCE = "distance"
        private const val KEY_SCORE = "score"
        private const val KEY_TOTAL_SCORE = "totalScore"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_GAME_MODE = "gameMode"
        private const val KEY_FAST_MODE = "isFastMode"
        
        // Date format as constant
        private const val DATE_FORMAT_PATTERN = "dd/MM/yyyy HH:mm"
        
        /**
         * Create Score from JSON object
         * @throws JSONException if JSON parsing fails
         */
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): Score {
            return Score(
                playerName = json.getString(KEY_PLAYER_NAME),
                distance = json.getInt(KEY_DISTANCE),
                score = json.getInt(KEY_SCORE),
                totalScore = json.getInt(KEY_TOTAL_SCORE),
                timestamp = json.getLong(KEY_TIMESTAMP),
                latitude = json.getDouble(KEY_LATITUDE),
                longitude = json.getDouble(KEY_LONGITUDE),
                gameMode = json.getString(KEY_GAME_MODE),
                isFastMode = json.getBoolean(KEY_FAST_MODE)
            )
        }
    }

    /**
     * Convert Score to JSON object for storage
     */
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_PLAYER_NAME, playerName)
            put(KEY_DISTANCE, distance)
            put(KEY_SCORE, score)
            put(KEY_TOTAL_SCORE, totalScore)
            put(KEY_TIMESTAMP, timestamp)
            put(KEY_LATITUDE, latitude)
            put(KEY_LONGITUDE, longitude)
            put(KEY_GAME_MODE, gameMode)
            put(KEY_FAST_MODE, isFastMode)
        }
    }

    /**
     * Get formatted timestamp for display
     * @return Formatted date string (dd/MM/yyyy HH:mm)
     */
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
        return formatter.format(date)
    }
}

