package com.example.multimediahubviews

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class NotificationReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action){
            ApplicationClass.PREVIOUS -> prevNextSong(false, context!!)
            ApplicationClass.PLAY -> if (AudioPlayer.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextSong(true, context!!)
            ApplicationClass.EXIT -> {
                AudioPlayer.musicService!!.stopForeground(true)
                AudioPlayer.musicService!!.mediaPlayer!!.release()
                AudioPlayer.musicService = null
                exitProcess(1)
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playMusic(){
        AudioPlayer.isPlaying = true
        AudioPlayer.musicService!!.mediaPlayer!!.start()
        AudioPlayer.musicService!!.showNotification(R.drawable.baseline_pause_24, 1F)
        AudioPlayer.binding.playPauseBtnPA.setIconResource(R.drawable.baseline_pause_24)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pauseMusic(){
        AudioPlayer.isPlaying = false
        AudioPlayer.musicService!!.mediaPlayer!!.pause()
        AudioPlayer.musicService!!.showNotification(R.drawable.baseline_play_arrow_24, 0F)
        AudioPlayer.binding.playPauseBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prevNextSong(increment: Boolean, context: Context){
        setSongPosition(increment)
        AudioPlayer.musicService!!.createMediaPlayer()

        Glide.with(context)
            .asBitmap()
            .load(AudioPlayer.musicListPA[AudioPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(NowPlaying.binding.songImgNP)

        NowPlaying.binding.songNameNP.text = AudioPlayer.musicListPA[AudioPlayer.songPosition].title


        Glide.with(context)
            .asBitmap()
            .load(AudioPlayer.musicListPA[AudioPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(AudioPlayer.binding.songImgPA)

        AudioPlayer.binding.songNamePA.text = AudioPlayer.musicListPA[AudioPlayer.songPosition].title

        playMusic()

    }
}