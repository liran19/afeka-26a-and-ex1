package com.example.homeex1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.homeex1.databinding.FragmentMapBinding
import com.example.homeex1.utilities.ScoreManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private var googleMap: GoogleMap? = null
    private var allScores: List<Score> = emptyList()
    private val markerScoreMap = mutableMapOf<Marker, Score>()

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
        
        val scoreManager = ScoreManager.getInstance(requireContext())
        allScores = scoreManager.getTopScores()
        
        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map_view) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Show all top score location on the map
        showAllScoresOnMap()
        
        googleMap?.setOnMarkerClickListener { marker ->
            val score = markerScoreMap[marker]
            if (score != null) {
                showScoreDetails(score)
                true
            } else {
                false
            }
        }
        
        googleMap?.setOnMapClickListener {
            binding.fragmentMapInfo.visibility = View.GONE
            binding.fragmentMapDetails.visibility = View.GONE
        }
    }

    private fun showScoreDetails(score: Score) {
        binding.fragmentMapInfo.visibility = View.GONE
        binding.fragmentMapDetails.visibility = View.VISIBLE

        // Update details card
        binding.fragmentMapPlayerName.text = score.playerName
        binding.fragmentMapCoordinates.text = " Lat: ${String.format("%.4f", score.latitude)}, " +
                "Lon: ${String.format("%.4f", score.longitude)}"
        binding.fragmentMapScoreInfo.text = "Score: ${score.totalScore} | Distance: ${score.distance}"
        binding.fragmentMapTime.text = score.getFormattedTime()
        
        val location = LatLng(score.latitude, score.longitude)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    fun showScoreLocation(score: Score) {
        showScoreDetails(score)
        
        markerScoreMap.entries.find { it.value == score }?.key?.showInfoWindow()
    }

    private fun showAllScoresOnMap() {
        googleMap?.let { map ->
            map.clear()
            markerScoreMap.clear()
            
            if (allScores.isEmpty()) {
                return
            }
            
            val boundsBuilder = LatLngBounds.Builder()
            
            // Add a marker for each score
            allScores.forEachIndexed { index, score ->
                val location = LatLng(score.latitude, score.longitude)
                
                val markerColor = when (index) {
                    0 -> BitmapDescriptorFactory.HUE_YELLOW    // Gold
                    1 -> BitmapDescriptorFactory.HUE_AZURE     // Silver
                    2 -> BitmapDescriptorFactory.HUE_ORANGE    // Bronze
                    else -> BitmapDescriptorFactory.HUE_RED    // Standard
                }
                
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(score.playerName)
                        .snippet("${score.totalScore} pts")
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                )
                
                marker?.let { markerScoreMap[it] = score }
                
                boundsBuilder.include(location)
            }
            
            try {
                val bounds = boundsBuilder.build()
                val padding = 150
                map.setOnMapLoadedCallback {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
