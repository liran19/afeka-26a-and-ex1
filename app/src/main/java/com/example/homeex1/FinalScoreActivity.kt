package com.example.homeex1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homeex1.databinding.ActivityFinalScoreBinding
import com.example.homeex1.utilities.ScoreManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class FinalScoreActivity : AppCompatActivity() {

    companion object {
        const val FINAL_DISTANCE_KEY = "FINAL_DISTANCE_KEY"
        const val FINAL_SCORE_KEY = "FINAL_SCORE_KEY"
        const val GAME_MODE_KEY = "GAME_MODE_KEY"
        const val FAST_MODE_KEY = "FAST_MODE_KEY"
    }

    private lateinit var binding: ActivityFinalScoreBinding
    private lateinit var scoreManager: ScoreManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    
    private var distance: Int = 0
    private var score: Int = 0
    private var gameMode: GameMode = GameMode.BUTTONS
    private var isFastMode: Boolean = false
    private var totalScore: Int = 0
    
    private var currentLocation: Location? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getLocationAndShowDialog()
        } else {
            showNameDialog()
        }
    }

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

        distance = intent.getIntExtra(FINAL_DISTANCE_KEY, 0)
        score = intent.getIntExtra(FINAL_SCORE_KEY, 0)
        val modeString = intent.getStringExtra(GAME_MODE_KEY) ?: GameMode.BUTTONS.name
        gameMode = GameMode.valueOf(modeString)
        isFastMode = intent.getBooleanExtra(FAST_MODE_KEY, false)

        scoreManager = ScoreManager.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupBackPressHandler()
        initViews()
        
        handler.postDelayed({
            requestLocationPermission()
        }, 500)
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMenu()
            }
        })
    }

    private fun initViews() {
        val baseScore = distance + score
        val multiplier = if (isFastMode) 1.5 else 1.0
        totalScore = (baseScore * multiplier).toInt()
        
        binding.finalScoreTotal.text = totalScore.toString()
        
        if (isFastMode) {
            binding.finalScoreMultiplier.visibility = android.view.View.VISIBLE
            binding.finalScoreMultiplier.text = "Fast Mode Bonus: x1.5"
        } else {
            binding.finalScoreMultiplier.visibility = android.view.View.GONE
        }
        
        binding.finalScoreDistance.text = distance.toString()
        binding.finalScoreCoins.text = score.toString()

        binding.finalScoreBtnTopTen.setOnClickListener {
            navigateToLeaderboard()
        }

        binding.finalScoreBtnPlayAgain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.GAME_MODE_KEY, gameMode.name)
            intent.putExtra(MainActivity.FAST_MODE_KEY, isFastMode)
            startActivity(intent)
            finish()
        }

        binding.finalScoreBtnMenu.setOnClickListener {
            navigateToMenu()
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocationAndShowDialog()
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getLocationAndShowDialog() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    currentLocation = location
                    showNameDialog()
                }
                .addOnFailureListener {
                    showNameDialog()
                }
        } catch (e: SecurityException) {
            showNameDialog()
        }
    }

    private fun showNameDialog() {
        if (isFinishing || isDestroyed) return
        
        val input = EditText(this)
        input.hint = "Enter your name"
        input.setPadding(50, 20, 50, 20)

        val dialog = AlertDialog.Builder(this)
            .setTitle("New High Score!")
            .setMessage("Your score: $totalScore\n\nEnter your name to save it to the leaderboard:")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { dialog, _ ->
                val playerName = input.text.toString().trim()
                saveScore(if (playerName.isNotEmpty()) playerName else "Anonymous")
                dialog.dismiss()
            }
            .setNegativeButton("Skip") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        
        dialog.show()
        input.requestFocus()
    }

    private fun saveScore(playerName: String) {
        val latitude = currentLocation?.latitude ?: 0.0
        val longitude = currentLocation?.longitude ?: 0.0
        
        val newScore = Score(
            playerName = playerName,
            distance = distance,
            score = score,
            totalScore = totalScore,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            gameMode = gameMode.name,
            isFastMode = isFastMode
        )

        scoreManager.addScore(newScore)
    }

    private fun navigateToLeaderboard() {
        val intent = Intent(this, TopTenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
