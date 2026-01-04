package com.example.homeex1

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homeex1.databinding.ActivityTopTenBinding

class TopTenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopTenBinding
    private lateinit var scoreListFragment: ScoreListFragment
    private lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTopTenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupFragments()
    }

    private fun setupToolbar() {
        binding.toptenToolbar.setNavigationOnClickListener {
            navigateToMenu()
        }
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun setupFragments() {
        // Create fragments
        scoreListFragment = ScoreListFragment()
        mapFragment = MapFragment()

        scoreListFragment.setCallback(object : ScoreClickCallback {
            override fun onScoreClicked(score: Score) {
                mapFragment.showScoreLocation(score)
            }
        })

        // Add fragments
        supportFragmentManager
            .beginTransaction()
            .add(R.id.topten_fragment_scores, scoreListFragment)
            .add(R.id.topten_fragment_map, mapFragment)
            .commit()
    }
}

