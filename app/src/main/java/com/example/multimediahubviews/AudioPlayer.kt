package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.multimediahubviews.databinding.ActivityAudioPlayerBinding

class AudioPlayer : AppCompatActivity(), ServiceConnection {

    companion object{
        lateinit var musicListPA: ArrayList<AudioModel>
        var songPosition: Int = 0
        //var mediaPlayer: MediaPlayer? = null
        var isPlaying: Boolean = false
        var musicService: MusicService ?= null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityAudioPlayerBinding

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //For Starting Service
        val intent = Intent(this,MusicService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        startService(intent)

        initializeLayout()

        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        binding.previousBtnPA.setOnClickListener {
            prevNextSong(false)

        }

        binding.nextBtnPA.setOnClickListener {
            prevNextSong(true)

        }


    }

    private fun setLayout() {
        Glide.with(this)
            .asBitmap()
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(binding.songImgPA)

        binding.songNamePA.text = musicListPA[songPosition].title
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMediaPlayer(){
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer?.reset()
            musicService!!.mediaPlayer?.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer?.prepare()
            musicService!!.mediaPlayer?.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.baseline_pause_24)
            musicService!!.showNotification(R.drawable.baseline_pause_24)

        }catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index",0)
        when (intent.getStringExtra("class")){
            "AudioAdapter" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(AudioFragment.musicListMA)
                setLayout()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.baseline_pause_24)
        musicService!!.showNotification(R.drawable.baseline_pause_24)
        isPlaying = true
        musicService!!.mediaPlayer?.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pauseMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        isPlaying = false
        musicService!!.mediaPlayer?.pause()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prevNextSong(increment: Boolean){
        if (increment){
            setSongPosition(true)
            ++songPosition
            setLayout()
            createMediaPlayer()
        }
        else{
            setSongPosition(false)
            --songPosition
            setLayout()
            createMediaPlayer()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.showNotification(R.drawable.baseline_pause_24)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increament: Boolean){
    if (increament)
    {
        if (AudioPlayer.musicListPA.size -1 == AudioPlayer.songPosition)
            AudioPlayer.songPosition = 0
        else ++AudioPlayer.songPosition
    }
    else{
        if (AudioPlayer.songPosition ==0)
            AudioPlayer.songPosition = AudioPlayer.musicListPA.size -1
        else --AudioPlayer.songPosition

    }
}