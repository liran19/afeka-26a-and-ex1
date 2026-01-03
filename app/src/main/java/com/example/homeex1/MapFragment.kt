package com.example.homeex1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.homeex1.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private var googleMap: GoogleMap? = null
    private var currentScore: Score? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map_view) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        
        // Initial state - no score selected
        showDefaultMessage()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Set default camera position (Israel/Tel Aviv area)
        val defaultPosition = LatLng(32.0853, 34.7818) // Tel Aviv
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 10f))
        
        // If we already have a score to show, display it
        currentScore?.let { showScoreOnMap(it) }
    }

    private fun showDefaultMessage() {
        binding.fragmentMapInfo.visibility = View.VISIBLE
        binding.fragmentMapDetails.visibility = View.GONE
    }

    fun showScoreLocation(score: Score) {
        currentScore = score
        
        // Hide default message
        binding.fragmentMapInfo.visibility = View.GONE
        binding.fragmentMapDetails.visibility = View.VISIBLE

        // Update details card
        binding.fragmentMapPlayerName.text = score.playerName
        binding.fragmentMapCoordinates.text = " Lat: ${String.format("%.4f", score.latitude)}, " +
                "Lon: ${String.format("%.4f", score.longitude)}"
        binding.fragmentMapScoreInfo.text = "Score: ${score.totalScore} | Distance: ${score.distance}"
        binding.fragmentMapTime.text = score.getFormattedTime()
        
        // Show on map if ready
        showScoreOnMap(score)
    }

    private fun showScoreOnMap(score: Score) {
        googleMap?.let { map ->
            val location = LatLng(score.latitude, score.longitude)
            
            // Clear previous markers
            map.clear()
            
            // Add marker for this score
            map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(score.playerName)
                    .snippet("Score: ${score.totalScore}")
            )
            
            // Move camera to location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
