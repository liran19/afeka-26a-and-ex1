package com.example.homeex1.utilities

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

/**
 * Singleton for playing background music using MediaPlayer
 */
object BackgroundMusicPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentMusicRes: Int? = null
    private var isInitialized = false
    private var isPaused = false

    /**
     * Play background music (smart method that resumes if same track is paused)
     */
    fun play(context: Context, @RawRes musicRes: Int, volume: Float = 0.5f) {
        if (musicRes == currentMusicRes && isPaused) {
            resume()
            return
        }

        if (musicRes == currentMusicRes && mediaPlayer?.isPlaying == true) {
            return
        }

        if (musicRes != currentMusicRes) {
            release()
        }

        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(context.applicationContext, musicRes)
                mediaPlayer?.apply {
                    isLooping = true
                    setVolume(volume, volume)
                }
                currentMusicRes = musicRes
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        }

        mediaPlayer?.start()
        isPaused = false
    }


    fun pause() {
        if (isInitialized && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
        }
    }


    fun resume() {
        if (isInitialized && isPaused && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPaused = false
        }
    }


    fun stop() {
        pause()
        release()
    }


    private fun release() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            currentMusicRes = null
            isInitialized = false
            isPaused = false
        }
    }


    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clampedVolume, clampedVolume)
    }


    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun isPaused(): Boolean {
        return isPaused && isInitialized
    }
}

