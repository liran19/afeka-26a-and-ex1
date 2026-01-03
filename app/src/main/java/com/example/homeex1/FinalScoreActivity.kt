package com.example.homeex1

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homeex1.databinding.ActivityFinalScoreBinding

class FinalScoreActivity : AppCompatActivity() {

    companion object {
        const val FINAL_DISTANCE_KEY = "FINAL_DISTANCE_KEY"
        const val FINAL_SCORE_KEY = "FINAL_SCORE_KEY"
        const val GAME_MODE_KEY = "GAME_MODE_KEY"
        const val FAST_MODE_KEY = "FAST_MODE_KEY"
    }

    private lateinit var binding: ActivityFinalScoreBinding
    private var distance: Int = 0
    private var score: Int = 0
    private var gameMode: GameMode = GameMode.BUTTONS
    private var isFastMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFinalScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get data from intent
        distance = intent.getIntExtra(FINAL_DISTANCE_KEY, 0)
        score = intent.getIntExtra(FINAL_SCORE_KEY, 0)
        val modeString = intent.getStringExtra(GAME_MODE_KEY) ?: GameMode.BUTTONS.name
        gameMode = GameMode.valueOf(modeString)
        isFastMode = intent.getBooleanExtra(FAST_MODE_KEY, false)

        setupBackPressHandler()
        initViews()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMenu()
            }
        })
    }

    private fun initViews() {
        // Calculate final score (distance + coin score)
        val baseScore = distance + score
        
        // 1.5x bonus for playing on fast mode
        val multiplier = if (isFastMode) 1.5 else 1.0
        val totalScore = (baseScore * multiplier).toInt()
        
        // Display final score
        binding.finalScoreTotal.text = totalScore.toString()
        
        if (isFastMode) {
            binding.finalScoreMultiplier.visibility = android.view.View.VISIBLE
            binding.finalScoreMultiplier.text = "Fast Mode Bonus: x1.5"
        } else {
            binding.finalScoreMultiplier.visibility = android.view.View.GONE
        }
        
        // Display breakdown of final score (how it is calculated)
        binding.finalScoreDistance.text = distance.toString()
        binding.finalScoreCoins.text = score.toString()

        binding.finalScoreBtnMenu.setOnClickListener {
            navigateToMenu()
        }

        binding.finalScoreBtnPlayAgain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.GAME_MODE_KEY, gameMode.name)
            intent.putExtra(MainActivity.FAST_MODE_KEY, isFastMode)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}

