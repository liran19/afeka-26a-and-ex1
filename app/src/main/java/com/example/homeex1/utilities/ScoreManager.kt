package com.example.homeex1.utilities

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.homeex1.Score
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val TAG = "ScoreManager"
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

    fun addScore(score: Score) {
        try {
            val scores = getTopScores().toMutableList()
            scores.add(score)
            
            scores.sortByDescending { it.totalScore }
            
            val topScores = scores.take(MAX_SCORES)
            
            saveScores(topScores)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding score", e)
        }
    }

    fun getTopScores(): List<Score> {
        val json = prefs.getString(KEY_SCORES, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<Score>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading scores", e)
            emptyList()
        }
    }
    
    private fun saveScores(scores: List<Score>) {
        try {
            val json = gson.toJson(scores)
            prefs.edit {
                putString(KEY_SCORES, json)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving scores", e)
        }
    }
}
