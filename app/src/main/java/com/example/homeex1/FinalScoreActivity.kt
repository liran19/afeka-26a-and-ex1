package com.example.homeex1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
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
    
    private var distance: Int = 0
    private var score: Int = 0
    private var gameMode: GameMode = GameMode.BUTTONS
    private var isFastMode: Boolean = false
    private var totalScore: Int = 0
    private var scoreSaved: Boolean = false
    private var dialogShown: Boolean = false  // Flag to prevent showing dialog twice
    
    private var currentLocation: Location? = null

    // Permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Permission granted - get location
                getCurrentLocation()
            }
            else -> {
                // No location permission - show dialog with default location
                showNameDialog()
            }
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

        // Get data from intent
        distance = intent.getIntExtra(FINAL_DISTANCE_KEY, 0)
        score = intent.getIntExtra(FINAL_SCORE_KEY, 0)
        val modeString = intent.getStringExtra(GAME_MODE_KEY) ?: GameMode.BUTTONS.name
        gameMode = GameMode.valueOf(modeString)
        isFastMode = intent.getBooleanExtra(FAST_MODE_KEY, false)

        scoreManager = ScoreManager.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupBackPressHandler()
        initViews()
        
        // Request location permission and get location
        requestLocationAndShowDialog()
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
        totalScore = (baseScore * multiplier).toInt()
        
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

    private fun requestLocationAndShowDialog() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                getCurrentLocation()
            }
            else -> {
                // Request permission
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentLocation() {
        if (dialogShown) return // Already processed
        
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        currentLocation = location
                        if (!dialogShown) {
                            dialogShown = true
                            showNameDialog()
                        }
                    }
                    .addOnFailureListener {
                        if (!dialogShown) {
                            dialogShown = true
                            showNameDialog()
                        }
                    }
            } else {
                if (!dialogShown) {
                    dialogShown = true
                    showNameDialog()
                }
            }
        } catch (_: Exception) {
            if (!dialogShown) {
                dialogShown = true
                showNameDialog()
            }
        }
    }

    private fun showNameDialog() {
        // Double-check not already shown
        if (dialogShown && scoreSaved) {
            return
        }
        
        val input = EditText(this)
        input.hint = "Enter your name"
        input.setPadding(50, 20, 50, 20)

        AlertDialog.Builder(this)
            .setTitle("New High Score!")
            .setMessage("Your score: $totalScore\n\nEnter your name to save it to the leaderboard:")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { dialog, _ ->
                val playerName = input.text.toString().trim()
                if (playerName.isNotEmpty()) {
                    saveScore(playerName)
                } else {
                    saveScore("Anonymous")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Skip") { dialog, _ ->
                scoreSaved = true
                dialog.dismiss()
            }
            .show()
    }

    private fun saveScore(playerName: String) {
        val latitude = currentLocation?.latitude ?: 32.0853 // Default: Tel Aviv
        val longitude = currentLocation?.longitude ?: 34.7818
        
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
        scoreSaved = true

        // Show confirmation with location info
        val locationInfo = if (currentLocation != null) {
            "Location recorded!"
        } else {
            "Default location used (enable location for precise tracking)"
        }
        
        Toast.makeText(
            this,
            "Score saved! $locationInfo\nCheck the Top 10 leaderboard!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
