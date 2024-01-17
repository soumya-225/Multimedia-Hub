package com.example.multimediahubviews

import android.media.MediaPlayer


object MyMediaPlayer {
    private var mediaPlayer: MediaPlayer? = null
    @JvmStatic
    fun getInstance(): MediaPlayer? {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }
        return mediaPlayer
    }
    var currentIndex = -1
}