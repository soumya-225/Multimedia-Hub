package com.example.multimediahubviews


import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.concurrent.TimeUnit


class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var totalTimeTv: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var titleTv: TextView
    private lateinit var currentTimeTv: TextView
    private lateinit var pausePlay: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var previousBtn: ImageView
    lateinit var musicIcon: ImageView
    private lateinit var songsList: ArrayList<AudioModel>
    private lateinit var currentSong: AudioModel
    private var mediaPlayer: MediaPlayer? = MyMediaPlayer.getInstance()
    private lateinit var back: ImageView
    var x: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        val mediaPlayer: MediaPlayer? = MyMediaPlayer.getInstance()

        titleTv = findViewById(R.id.song_title)
        currentTimeTv = findViewById(R.id.current_time)
        totalTimeTv = findViewById(R.id.total_time)
        seekBar = findViewById(R.id.seek_bar)
        pausePlay = findViewById(R.id.pause_play)
        nextBtn = findViewById(R.id.next)
        previousBtn = findViewById(R.id.previous)
        musicIcon = findViewById(R.id.music_icon_big)
        back = findViewById(R.id.back)

        titleTv.isSelected = true
        songsList = (intent.getSerializableExtra("LIST") as ArrayList<AudioModel>?)!!

        setResourcesWithMusic()

        runOnUiThread(object : Runnable {
            override fun run() {
                seekBar.progress = mediaPlayer!!.currentPosition
                currentTimeTv.text = convertToMMSS(mediaPlayer.currentPosition.toString() + "")
                if (mediaPlayer.isPlaying) {
                    pausePlay.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                    musicIcon.rotation = (x++).toFloat()
                } else {
                    pausePlay.setImageResource(R.drawable.baseline_play_circle_outline_24)
                    musicIcon.rotation = x.toFloat()
                }
                Handler(mainLooper).postDelayed(this, 100)
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // This method is called when the user starts touching the SeekBar.
                // You can perform any actions needed when the user starts interacting with the SeekBar.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // This method is called when the user stops touching the SeekBar.
                // You can perform any actions needed when the user stops interacting with the SeekBar.
            }
        })
    }

    private fun setResourcesWithMusic(){
        currentSong = songsList[MyMediaPlayer.currentIndex]
        titleTv.text = (currentSong.title)
        totalTimeTv.text = convertToMMSS(currentSong.duration)
        pausePlay.setOnClickListener{pausePlay()}
        nextBtn.setOnClickListener{playNextSong()}
        previousBtn.setOnClickListener{playPreviousSong()}

        back()
        playMusic()
    }

    private fun playMusic() {
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(currentSong.path)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            seekBar.progress = 0
            seekBar.max = mediaPlayer?.duration!!
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun playNextSong() {
        if(MyMediaPlayer.currentIndex == songsList.size -1)
            return
        MyMediaPlayer.currentIndex +=1
        mediaPlayer?.reset()
        setResourcesWithMusic()
    }

    private fun playPreviousSong() {
        if(MyMediaPlayer.currentIndex== 0)
            return
        MyMediaPlayer.currentIndex -=1
        mediaPlayer?.reset()
        setResourcesWithMusic()
    }

    private fun pausePlay(): View.OnClickListener? {
        if(mediaPlayer!!.isPlaying)
            mediaPlayer!!.pause()
        else
            mediaPlayer!!.start()
        return null
    }


    private fun back() {
        back.setOnClickListener { finish()}
    }

    private fun convertToMMSS(duration: String): String {
        val millis = duration.toLong()
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1))
    }
}