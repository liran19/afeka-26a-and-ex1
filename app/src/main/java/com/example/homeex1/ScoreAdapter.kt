package com.example.homeex1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeex1.databinding.ItemScoreBinding

class ScoreAdapter(
    private val scores: List<Score>,
    private val callback: ScoreClickCallback?
) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    inner class ScoreViewHolder(private val binding: ItemScoreBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(score: Score, rank: Int) {
            binding.itemScoreRank.text = rank.toString()
            
            // Color medals for top 3
            binding.itemScoreRank.setTextColor(
                when (rank) {
                    1 -> 0xFFFFD700.toInt() // Gold
                    2 -> 0xFFC0C0C0.toInt() // Silver
                    3 -> 0xFFCD7F32.toInt() // Bronze
                    else -> 0xFF666666.toInt() // Gray
                }
            )

            binding.itemScoreName.text = score.playerName
            binding.itemScoreDetails.text = "Distance: ${score.distance} | Coins: ${score.score}"
            binding.itemScoreTime.text = score.getFormattedTime()
            binding.itemScoreTotal.text = score.totalScore.toString()

            // Set click listener
            binding.root.setOnClickListener {
                callback?.onScoreClicked(score)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemScoreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(scores[position], position + 1)
    }

    override fun getItemCount(): Int = scores.size
}

