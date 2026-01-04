package com.example.homeex1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homeex1.databinding.FragmentScoreListBinding
import com.example.homeex1.utilities.ScoreManager

class ScoreListFragment : Fragment() {

    private var _binding: FragmentScoreListBinding? = null
    private val binding get() = _binding!!

    private var callback: ScoreClickCallback? = null
    private lateinit var scoreManager: ScoreManager
    private lateinit var adapter: ScoreAdapter

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
        
        setupRecyclerView()
        loadScores()
    }

    private fun setupRecyclerView() {
        binding.fragmentScoreListRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadScores() {
        val scores = scoreManager.getTopScores()
        
        if (scores.isEmpty()) {
            binding.fragmentScoreListEmpty.visibility = View.VISIBLE
            binding.fragmentScoreListRecycler.visibility = View.GONE
        } else {
            binding.fragmentScoreListEmpty.visibility = View.GONE
            binding.fragmentScoreListRecycler.visibility = View.VISIBLE
            
            adapter = ScoreAdapter(scores, callback)
            binding.fragmentScoreListRecycler.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

