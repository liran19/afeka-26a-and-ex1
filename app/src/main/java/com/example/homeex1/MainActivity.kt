package com.example.homeex1

import android.content.Intent
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
import com.example.homeex1.utilities.SoundEffectPlayer
import com.example.homeex1.utilities.AccSensorApi
import com.example.homeex1.utilities.AccSensorCallBack

enum class GameMode {
    BUTTONS,
    SENSORS
}

class MainActivity : AppCompatActivity() {

    companion object {
        const val GAME_MODE_KEY = "GAME_MODE_KEY"
        const val FAST_MODE_KEY = "FAST_MODE_KEY"
        private const val MAX_TILT = 5.5f
        private const val TILT_SPEED_THRESHOLD = 2.5f
        private const val SPEED_BOOST_DURATION = 2000L
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

    // Sensor support
    private var accSensorApi: AccSensorApi? = null

    // Game loop
    private val handler = Handler(Looper.getMainLooper())
    private var tickMillis = 1000L
    private var baseTickMillis = 1000L
    private var isSpeedBoosted = false
    
    private var lastY = 0f
    private var tiltDirection = 0
    private var lastTiltTime = 0L

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
        
        baseTickMillis = if (isFastMode) 500L else 1000L
        tickMillis = baseTickMillis

        val config = GameConfig()
        game = GameState(config)
        
        SoundEffectPlayer.init(this)
        SoundEffectPlayer.load(this, R.raw.crash)
        
        initViews()
        initButtons()
        render()
    }

    override fun onResume() {
        super.onResume()
        // Start sensor if in sensor mode
        if (gameMode == GameMode.SENSORS) {
            accSensorApi?.start()
        }
        startGameLoop()
    }

    override fun onPause() {
        super.onPause()
        // Stop sensor if in sensor mode
        if (gameMode == GameMode.SENSORS) {
            accSensorApi?.stop()
        }
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
                
                // Initialize accelerometer sensor
                accSensorApi = AccSensorApi(this, object : AccSensorCallBack {
                    override fun data(x: Float, y: Float, z: Float) {
                        handleSensorData(x, y, z)
                    }
                })
            }
        }
    }

    /**
     * Handle accelerometer sensor data for player movement.
     * Maps the tilt angle directly to a lane index.
     */
    private fun handleSensorData(x: Float, y: Float, z: Float) {
        if (game.isGameOver) return
        
        val numCols = game.config.cols
        
        val normalizedX = ((x + MAX_TILT) / (2 * MAX_TILT)).coerceIn(0f, 1f)
        val targetCol = ((1f - normalizedX) * (numCols - 1) + 0.5f).toInt().coerceIn(0, numCols - 1)
        
        if (targetCol != game.player.getCol()) {
            game.movePlayerToCol(targetCol)
            renderPlayer()
        }
        
        val currentTime = System.currentTimeMillis()
        val yDiff = y - lastY
        
        if (kotlin.math.abs(yDiff) > TILT_SPEED_THRESHOLD) {
            val currentDirection = if (yDiff > 0) 1 else -1
            
            if (currentDirection != tiltDirection && tiltDirection != 0) {
                lastTiltTime = currentTime
                if (!isSpeedBoosted) {
                    isSpeedBoosted = true
                    tickMillis = baseTickMillis / 2
                }
            }
            tiltDirection = currentDirection
        }
        
        lastY = y
        
        if (isSpeedBoosted && currentTime - lastTiltTime > SPEED_BOOST_DURATION) {
            isSpeedBoosted = false
            tickMillis = baseTickMillis
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
        renderScore()
    }

    private fun renderObstacles(){
        for ( r in 0 until game.config.rows){
            for (c in 0 until game.config.cols){

                val view = obstacleViews[r][c]
                val cellType = game.grid[r][c]

                when (cellType) {
                    CellType.OBSTACLE -> {
                        view.visibility = View.VISIBLE
                        view.setImageResource(R.drawable.img_space_meteor)
                    }
                    CellType.COIN -> {
                        view.visibility = View.VISIBLE
                        view.setImageResource(R.drawable.img_spaceship_rocket_fuel)
                    }
                    CellType.EMPTY -> {
                        view.visibility = View.INVISIBLE
                    }
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

    private fun renderScore() {
        binding.txtDistance.text = "Distance: ${game.distance}"
        binding.txtScore.text = "Score: ${game.score}"
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
                SoundEffectPlayer.play(R.raw.crash)
                
                val message = "Crash! Lives left: ${game.player.getLives()}"
                hitToast?.cancel() // Cancel the previous toast
                hitToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                hitToast?.show()
            }
            GameEvent.COIN_COLLECTED -> {
                // TODO: add here
            }
            GameEvent.GAME_OVER -> {
                vibrate(250)
                SoundEffectPlayer.play(R.raw.crash)
                
                val message = "Game Over!"
                hitToast?.cancel()
                hitToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                hitToast?.show()

                // Stop game loop
                handler.removeCallbacks(gameRunnable)

                // Navigate to Final Score Activity after short delay
                handler.postDelayed({
                    val intent = Intent(this, FinalScoreActivity::class.java)
                    intent.putExtra(FinalScoreActivity.FINAL_DISTANCE_KEY, game.distance)
                    intent.putExtra(FinalScoreActivity.FINAL_SCORE_KEY, game.score)
                    intent.putExtra(FinalScoreActivity.GAME_MODE_KEY, gameMode.name)
                    intent.putExtra(FinalScoreActivity.FAST_MODE_KEY, isFastMode)
                    startActivity(intent)
                    finish()
                }, 1500)
            }
        }
    }

}
