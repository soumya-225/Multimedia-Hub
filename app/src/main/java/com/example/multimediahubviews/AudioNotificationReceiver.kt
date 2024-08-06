package com.example.multimediahubviews

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class AudioNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> prevNextSong(false, context!!)
            ApplicationClass.PLAY -> if (AudioPlayer.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextSong(true, context!!)
            ApplicationClass.EXIT -> {
                AudioPlayer.audioService!!.stopForeground(true)
                AudioPlayer.audioService!!.mediaPlayer!!.release()
                AudioPlayer.audioService = null
                exitProcess(1)
            }
        }
    }

    private fun playMusic() {
        AudioPlayer.isPlaying = true
        AudioPlayer.audioService!!.mediaPlayer!!.start()
        AudioPlayer.audioService!!.showNotification(R.drawable.pause_icon_dark)
        AudioPlayer.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon_dark)
        AudioNowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon_dark)
    }

    private fun pauseMusic() {
        AudioPlayer.isPlaying = false
        AudioPlayer.audioService!!.mediaPlayer!!.pause()
        AudioPlayer.audioService!!.showNotification(R.drawable.play_icon_dark)
        AudioPlayer.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon_dark)
        AudioNowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.play_icon_dark)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment)
        AudioPlayer.audioService!!.createMediaPlayer()

        Glide.with(context).asBitmap()
            .load(AudioPlayer.musicListPA[AudioPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.audio_thumbnail).fitCenter())
            .into(AudioNowPlaying.binding.songImgNP)

        AudioNowPlaying.binding.songNameNP.text =
            AudioPlayer.musicListPA[AudioPlayer.songPosition].title

        Glide.with(context).asBitmap()
            .load(AudioPlayer.musicListPA[AudioPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.audio_thumbnail).fitCenter())
            .into(AudioPlayer.binding.songImgPA)

        AudioPlayer.binding.songNamePA.text =
            AudioPlayer.musicListPA[AudioPlayer.songPosition].title

        playMusic()
    }
}