package com.example.homeex1.utilities

import android.content.Context
import android.content.SharedPreferences
import com.example.homeex1.Score
import org.json.JSONArray

class ScoreManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "space_dodger_scores"
        private const val KEY_SCORES = "top_scores"
        private const val MAX_SCORES = 10
        
        @Volatile
        private var instance: ScoreManager? = null
        
        fun getInstance(context: Context): ScoreManager {
            return instance ?: synchronized(this) {
                instance ?: ScoreManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // Add a new score and maintain top 10
    fun addScore(score: Score) {
        val scores = getTopScores().toMutableList()
        scores.add(score)

        scores.sortByDescending { it.totalScore }
        
        val topScores = scores.take(MAX_SCORES)
        
        saveScores(topScores)
    }
    
    // Get all top scores
    fun getTopScores(): List<Score> {
        val scoresJson = prefs.getString(KEY_SCORES, null) ?: return emptyList()
        
        return try {
            val jsonArray = JSONArray(scoresJson)
            val scores = mutableListOf<Score>()
            
            for (i in 0 until jsonArray.length()) {
                val scoreJson = jsonArray.getJSONObject(i)
                scores.add(Score.fromJson(scoreJson))
            }
            
            scores
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // Save scores to SharedPreferences
    private fun saveScores(scores: List<Score>) {
        val jsonArray = JSONArray()
        scores.forEach { score ->
            jsonArray.put(score.toJson())
        }
        
        prefs.edit()
            .putString(KEY_SCORES, jsonArray.toString())
            .apply()
    }
}

