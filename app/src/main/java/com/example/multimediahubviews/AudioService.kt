package com.example.multimediahubviews

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class AudioService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }
    inner class MyBinder : Binder() {
        fun currentService(): AudioService {
            return this@AudioService
        }
    }

    fun showNotification(playPauseBtn: Int, playbackSpeed: Float) {

        val intent = Intent(baseContext, AudioPlayer::class.java)
        intent.putExtra("index", AudioPlayer.songPosition)
        intent.putExtra("class", "AudioNowPlaying")

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(
            baseContext,
            AudioNotificationReceiver::class.java
        ).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent =
            Intent(
                baseContext,
                AudioNotificationReceiver::class.java
            ).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent =
            Intent(
                baseContext,
                AudioNotificationReceiver::class.java
            ).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val exitIntent =
            Intent(
                baseContext,
                AudioNotificationReceiver::class.java
            ).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val imgArt = getImgArt(AudioPlayer.musicListPA[AudioPlayer.songPosition].path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.audio_notification_background)
        }

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(AudioPlayer.musicListPA[AudioPlayer.songPosition].title)
            .setContentText(AudioPlayer.musicListPA[AudioPlayer.songPosition].title)
            .setSmallIcon(R.drawable.video_thumbnail)
            .setLargeIcon(image)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.skip_previous_icon, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.skip_next_icon, "Next", nextPendingIntent)
            .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
            .build()

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())

            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())
        }*/
        startForeground(2, notification)
    }

    fun createMediaPlayer() {
        try {
            if (AudioPlayer.audioService!!.mediaPlayer == null) AudioPlayer.audioService!!.mediaPlayer =
                MediaPlayer()
            AudioPlayer.audioService!!.mediaPlayer?.reset()
            AudioPlayer.audioService!!.mediaPlayer?.setDataSource(AudioPlayer.musicListPA[AudioPlayer.songPosition].path)
            AudioPlayer.audioService!!.mediaPlayer?.prepare()
            AudioPlayer.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon_dark)
            AudioPlayer.audioService!!.showNotification(R.drawable.pause_icon_dark, 0F)

            AudioPlayer.binding.tvSeekBarStart.text =
                convertToMMSS(mediaPlayer!!.currentPosition.toString())
            AudioPlayer.binding.tvSeekBarEnd.text = convertToMMSS(mediaPlayer!!.duration.toString())
            AudioPlayer.binding.seekBarPA.progress = 0
            AudioPlayer.binding.seekBarPA.max = mediaPlayer!!.duration

        } catch (e: Exception) {
            return
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            AudioPlayer.binding.tvSeekBarStart.text =
                convertToMMSS(mediaPlayer!!.currentPosition.toString())
            AudioPlayer.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }
}