package com.example.homeex1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.homeex1.databinding.ActivityMainBinding

enum class GameMode {
    BUTTONS,
    SENSORS
}

class MainActivity : AppCompatActivity() {

    companion object {
        const val GAME_MODE_KEY = "GAME_MODE_KEY"
        const val FAST_MODE_KEY = "FAST_MODE_KEY"
    }

    private lateinit var binding: ActivityMainBinding
    private  lateinit var game: GameState
    private var hitToast: Toast? = null

    private lateinit var obstacleViews: Array<Array<ImageView>>
    private lateinit var playerViews: Array<ImageView>
    private lateinit var heartViews: List<ImageView>
    private lateinit var btnLeft: FloatingActionButton
    private lateinit var btnRight: FloatingActionButton

    // Game settings from menu
    private var gameMode: GameMode = GameMode.BUTTONS
    private var isFastMode: Boolean = false

    // Game loop
    private val handler = Handler(Looper.getMainLooper())
    private var tickMillis = 1000L

    private val gameRunnable = object : Runnable {
        override fun run() {
            val event = game.step()
            render()
            handleEvent(event)

            // Endless game
            handler.postDelayed(this, tickMillis)
        }
    }

    // Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get settings from menu
        val modeString = intent.getStringExtra(GAME_MODE_KEY) ?: GameMode.BUTTONS.name
        gameMode = GameMode.valueOf(modeString)
        isFastMode = intent.getBooleanExtra(FAST_MODE_KEY, false)
        
        // Set game speed based on mode
        tickMillis = if (isFastMode) 500L else 1000L

        val config = GameConfig()
        game = GameState(config)
        initViews()
        initButtons()
        render()
    }

    override fun onResume() {
        super.onResume()
        startGameLoop()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(gameRunnable)
    }

    private fun initViews() {
        // hearts
        heartViews = listOf(
            binding.heart1,
            binding.heart2,
            binding.heart3
        )

        // 8x5 obstacle matrix
        obstacleViews = arrayOf(
            arrayOf(
                binding.imgInfo00,
                binding.imgInfo01,
                binding.imgInfo02,
                binding.imgInfo03,
                binding.imgInfo04
            ),
            arrayOf(
                binding.imgInfo10,
                binding.imgInfo11,
                binding.imgInfo12,
                binding.imgInfo13,
                binding.imgInfo14
                ),
            arrayOf(
                binding.imgInfo20,
                binding.imgInfo21,
                binding.imgInfo22,
                binding.imgInfo23,
                binding.imgInfo24
            ),
            arrayOf(
                binding.imgInfo30,
                binding.imgInfo31,
                binding.imgInfo32,
                binding.imgInfo33,
                binding.imgInfo34
            ),
            arrayOf(
                binding.imgInfo40,
                binding.imgInfo41,
                binding.imgInfo42,
                binding.imgInfo43,
                binding.imgInfo44
            ),
            arrayOf(
                binding.imgInfo50,
                binding.imgInfo51,
                binding.imgInfo52,
                binding.imgInfo53,
                binding.imgInfo54
            ),
            arrayOf(
                binding.imgInfo60,
                binding.imgInfo61,
                binding.imgInfo62,
                binding.imgInfo63,
                binding.imgInfo64
            ),
            arrayOf(
                binding.imgInfo70,
                binding.imgInfo71,
                binding.imgInfo72,
                binding.imgInfo73,
                binding.imgInfo74
            )
        )

        // bottom row – player positions
        playerViews = arrayOf(
            binding.imgPlayer00,
            binding.imgPlayer01,
            binding.imgPlayer02,
            binding.imgPlayer03,
            binding.imgPlayer04
        )

        // buttons
        btnLeft = binding.btnLeft
        btnRight = binding.btnRight
    }

    private fun initButtons() {
        when (gameMode) {
            GameMode.BUTTONS -> {
                binding.btnLeft.visibility = View.VISIBLE
                binding.btnRight.visibility = View.VISIBLE
                
                binding.btnLeft.setOnClickListener {
                    game.movePlayerLeft()
                    renderPlayer()
                }

                binding.btnRight.setOnClickListener {
                    game.movePlayerRight()
                    renderPlayer()
                }
            }
            GameMode.SENSORS -> {
                binding.btnLeft.visibility = View.GONE
                binding.btnRight.visibility = View.GONE
                // TODO: Add sensor support
            }
        }
    }

    private fun startGameLoop() {
        handler.removeCallbacks(gameRunnable)
        handler.postDelayed(gameRunnable, tickMillis)
    }

    private fun render() {
        renderObstacles()
        renderPlayer()
        renderHearts()
    }

    private fun renderObstacles(){
        for ( r in 0 until game.config.rows){
            for (c in 0 until game.config.cols){

                val view = obstacleViews[r][c]
                val cellType = game.grid[r][c]

                if (cellType.isVisible){
                    view.visibility = View.VISIBLE
                }else {
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun renderPlayer(){
        val playerCol = game.player.getCol()

        for (c in 0 until game.config.cols){
            if (c == playerCol){
                playerViews[c].visibility = View.VISIBLE
            } else {
                playerViews[c].visibility = View.INVISIBLE
            }
        }
    }

    private fun renderHearts() {
        val lives = game.player.getLives()

        for (i in heartViews.indices) {
            if (i < lives) {
                heartViews[i].visibility = View.VISIBLE
            } else {
                heartViews[i].visibility = View.INVISIBLE
            }
        }
    }

    private fun vibrate(milliseconds: Long = 100) {
        val vibratorManager = getSystemService(VibratorManager::class.java)
        val vibrator: Vibrator = vibratorManager.defaultVibrator

        if (!vibrator.hasVibrator()) {
            Toast.makeText(this, "No vibrator on this device", Toast.LENGTH_SHORT).show()
            return
        }

        vibrator.vibrate(
            VibrationEffect.createOneShot(
                milliseconds,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    private fun handleEvent(event: GameEvent) {
        when (event) {
            GameEvent.NONE -> {}
            GameEvent.HIT -> {
                vibrate(80)
                val message = "Crash! Lives left: ${game.player.getLives()}"
                hitToast?.cancel() // Cancel the previous toast
                hitToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                hitToast?.show()
            }
            GameEvent.GAME_OVER -> {
                vibrate(250)
                val message = "Game over! Restarting..."
                hitToast?.cancel() // Cancel the previous toast
                hitToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                hitToast?.show()

                handler.postDelayed({
                    game.reset()
                    render()
                }, 1000)
            }
        }
    }

}