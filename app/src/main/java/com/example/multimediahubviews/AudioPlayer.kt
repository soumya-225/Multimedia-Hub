package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.postDelayed
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.multimediahubviews.databinding.ActivityAudioPlayerBinding
import java.util.concurrent.TimeUnit

class AudioPlayer : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object{
        lateinit var musicListPA: ArrayList<AudioModel>
        var songPosition: Int = 0
        //var mediaPlayer: MediaPlayer? = null
        var isPlaying: Boolean = false
        var musicService: MusicService ?= null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityAudioPlayerBinding
        var repeat: Boolean = false

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data?.scheme.contentEquals("content")){
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            //setLayout()
            Glide.with(this)
                .asBitmap()
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.songImgPA)
            binding.songNamePA.text = musicListPA[songPosition].title
        }
        else initializeLayout()


        //setLayout()
        //initializeLayout()


        //initializeLayout()

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

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.repeatBtnPA.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            }
            else{
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        }

        binding.backBtnPA.setOnClickListener {
            finish()
        }

        binding.equalizerBtnPA.setOnClickListener {
            try {
                val EqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                EqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                EqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                EqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(EqIntent,101)
            }catch (e: Exception){
                Toast.makeText(this, "Equalizer Feature Not Supported!!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            //startService(Intent.createChooser(shareIntent, "Share Music File"))

            val chooserIntent = Intent.createChooser(shareIntent, "Share Music File")

            // Check if there are activities that can handle the shareIntent
            if (shareIntent.resolveActivity(packageManager) != null) {
                // Start the explicit chooserIntent
                startActivity(chooserIntent)
            } else {
                // Handle the case where no activity can handle the shareIntent
                // (You may want to show a message to the user)
            }

        }


    }

    private fun setLayout() {
        Glide.with(this)
            .asBitmap()
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(binding.songImgPA)

        binding.songNamePA.text = musicListPA[songPosition].title

        if (repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))

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
            musicService!!.showNotification(R.drawable.baseline_pause_24, 1F)

            binding.tvSeekBarStart.text = convertToMMSS(musicService!!.mediaPlayer!!.currentPosition.toString())
            binding.tvSeekBarEnd.text = convertToMMSS(musicService!!.mediaPlayer!!.duration.toString())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

        }catch (e: Exception) {
            return
        }
    }


    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index",0)
        when (intent.getStringExtra("class")){
            "AudioAdapter" -> {
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(AudioFragment.musicListMA)

                setLayout()
            }
            "NowPlaying" ->{
                Log.d("Tag2", songPosition.toString())
                setLayout()
                binding.tvSeekBarStart.text = convertToMMSS(musicService!!.mediaPlayer!!.currentPosition.toString())
                binding.tvSeekBarEnd.text = convertToMMSS(musicService!!.mediaPlayer!!.duration.toString())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration


            }
            "MainActivity" -> {
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(AudioFragment.musicListMA)
            }
        }
    }

    private fun getMusicDetails(contentUri: Uri): AudioModel{
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            cursor!!.moveToFirst()
            val path = cursor.getString(dataColumn!!)
            val duration = cursor.getLong(durationColumn!!)
            return AudioModel(path,path, duration.toString(), "","","Unknown".toUri() )
        }finally {
            cursor?.close()
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun playMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.baseline_pause_24)
        musicService!!.showNotification(R.drawable.baseline_pause_24, 1F)
        isPlaying = true
        musicService!!.mediaPlayer?.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pauseMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.baseline_play_arrow_24, 0F)
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
        musicService!!.seekBarSetup()
        musicService!!.showNotification(R.drawable.baseline_pause_24, 1F)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 || resultCode == RESULT_OK){
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e: Exception){
            return
        }
    }
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increament: Boolean){
    if (!AudioPlayer.repeat){
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
}