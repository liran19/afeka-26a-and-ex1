package com.example.homeex1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.homeex1.databinding.FragmentScoreListBinding
import com.example.homeex1.databinding.ItemScoreBinding
import com.example.homeex1.utilities.ScoreManager

class ScoreListFragment : Fragment() {

    private var _binding: FragmentScoreListBinding? = null
    private val binding get() = _binding!!

    private var callback: ScoreClickCallback? = null
    private lateinit var scoreManager: ScoreManager

    fun setCallback(callback: ScoreClickCallback) {
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScoreListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scoreManager = ScoreManager.getInstance(requireContext())
        
        loadScores()
    }

    private fun loadScores() {
        val scores = scoreManager.getTopScores()
        
        if (scores.isEmpty()) {
            binding.fragmentScoreListEmpty.visibility = View.VISIBLE
            binding.fragmentScoreListScroll.visibility = View.GONE
        } else {
            binding.fragmentScoreListEmpty.visibility = View.GONE
            binding.fragmentScoreListScroll.visibility = View.VISIBLE
            
            // Clear any existing views
            binding.fragmentScoreListContainer.removeAllViews()
            
            // Add a card for each score
            scores.forEachIndexed { index, score ->
                val scoreCard = createScoreCard(score, index + 1)
                binding.fragmentScoreListContainer.addView(scoreCard)
            }
        }
    }

    private fun createScoreCard(score: Score, rank: Int): View {
        // Use ViewBinding to inflate the card
        val cardBinding = ItemScoreBinding.inflate(
            LayoutInflater.from(requireContext()),
            binding.fragmentScoreListContainer,
            false
        )
        
        // Set data using binding
        cardBinding.itemScoreRank.text = rank.toString()
        
        // Color medals for top 3
        cardBinding.itemScoreRank.setTextColor(
            when (rank) {
                1 -> 0xFFFFD700.toInt() // Gold
                2 -> 0xFFC0C0C0.toInt() // Silver
                3 -> 0xFFCD7F32.toInt() // Bronze
                else -> 0xFF666666.toInt() // Gray
            }
        )

        cardBinding.itemScoreName.text = score.playerName
        cardBinding.itemScoreDetails.text = "Distance: ${score.distance} | Coins: ${score.score}"
        cardBinding.itemScoreTime.text = score.getFormattedTime()
        cardBinding.itemScoreTotal.text = score.totalScore.toString()

        // Set click listener
        cardBinding.root.setOnClickListener {
            callback?.onScoreClicked(score)
        }
        
        return cardBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

