package com.example.homeex1

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homeex1.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private var isFastMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        // Speed toggle
        binding.menuSwitchSpeed.setOnCheckedChangeListener { _, isChecked ->
            isFastMode = isChecked
        }

        // Button mode
        binding.menuBtnButtons.setOnClickListener {
            startGame(GameMode.BUTTONS)
        }

        // Sensor mode
        binding.menuBtnSensors.setOnClickListener {
            startGame(GameMode.SENSORS)
        }

        // Top 10 leaderboard
        binding.menuBtnTopTen.setOnClickListener {
            val intent = Intent(this, TopTenActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startGame(gameMode: GameMode) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.GAME_MODE_KEY, gameMode.name)
        intent.putExtra(MainActivity.FAST_MODE_KEY, isFastMode)
        startActivity(intent)
    }
}

