package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.multimediahubviews.databinding.ActivityAudioPlayerBinding

class AudioPlayer : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityAudioPlayerBinding
        lateinit var musicListPA: ArrayList<AudioModel>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var audioService: AudioService? = null
        var repeat: Boolean = false

    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data?.scheme.contentEquals("content")) {
            val intentService = Intent(this, AudioService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))

            Glide.with(this).asBitmap().load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.audio_thumbnail_2).centerCrop())
                .into(binding.songImgPA)

            binding.songNamePA.text = musicListPA[songPosition].title

        } else initializeLayout()

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

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.repeatBtnPA.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            } else {
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        }

        binding.backBtnPA.setOnClickListener {
            finish()
        }

        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION, audioService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 101)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer Feature Not Supported!!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            val chooserIntent = Intent.createChooser(shareIntent, "Share Music File")

            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(chooserIntent)
            }
        }
    }

    private fun setLayout() {
        Glide.with(this).asBitmap().load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.audio_thumbnail_2).centerCrop())
            .into(binding.songImgPA)

        binding.songNamePA.text = musicListPA[songPosition].title

        if (repeat) binding.repeatBtnPA.setColorFilter(
            ContextCompat.getColor(
                this, R.color.purple_500
            )
        )
    }

    private fun createMediaPlayer() {
        try {
            if (audioService!!.mediaPlayer == null) audioService!!.mediaPlayer = MediaPlayer()
            audioService!!.mediaPlayer?.reset()
            audioService!!.mediaPlayer?.setDataSource(musicListPA[songPosition].path)
            audioService!!.mediaPlayer?.prepare()
            audioService!!.mediaPlayer?.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon_dark)
            audioService!!.showNotification(R.drawable.pause_icon_dark)

            binding.tvSeekBarStart.text =
                convertToMMSS(audioService!!.mediaPlayer!!.currentPosition.toString())
            binding.tvSeekBarEnd.text =
                convertToMMSS(audioService!!.mediaPlayer!!.duration.toString())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = audioService!!.mediaPlayer!!.duration
            audioService!!.mediaPlayer!!.setOnCompletionListener(this)

        } catch (e: Exception) {
            return
        }
    }


    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "AudioAdapter" -> {
                val intent = Intent(this, AudioService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(AudioFragment.musicListMA)
                setLayout()
            }

            "AudioNowPlaying" -> {
                Log.d("Tag2", songPosition.toString())
                setLayout()
                binding.tvSeekBarStart.text =
                    convertToMMSS(audioService!!.mediaPlayer!!.currentPosition.toString())
                binding.tvSeekBarEnd.text =
                    convertToMMSS(audioService!!.mediaPlayer!!.duration.toString())
                binding.seekBarPA.progress = audioService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = audioService!!.mediaPlayer!!.duration
            }

            "MainActivity" -> {
                val intent = Intent(this, AudioService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(AudioFragment.musicListMA)
            }
        }
    }

    private fun getMusicDetails(contentUri: Uri): AudioModel {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            cursor!!.moveToFirst()
            val path = cursor.getString(dataColumn!!)
            val duration = cursor.getLong(durationColumn!!)
            return AudioModel(path, path, duration.toString(), "", "", "Unknown".toUri())
        } finally {
            cursor?.close()
        }
    }

    private fun playMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon_dark)
        audioService!!.showNotification(R.drawable.pause_icon_dark)
        isPlaying = true
        audioService!!.mediaPlayer?.start()
    }

    private fun pauseMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon_dark)
        audioService!!.showNotification(R.drawable.play_icon_dark)
        isPlaying = false
        audioService!!.mediaPlayer?.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(true)
            ++songPosition
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(false)
            --songPosition
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as AudioService.MyBinder
        audioService = binder.currentService()
        createMediaPlayer()
        audioService!!.seekBarSetup()
        audioService!!.showNotification(R.drawable.pause_icon_dark)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        audioService = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 || resultCode == RESULT_OK) {
            return
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
            return
        }
    }
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!AudioPlayer.repeat) {
        if (increment) {
            if (AudioPlayer.musicListPA.size - 1 == AudioPlayer.songPosition) AudioPlayer.songPosition =
                0
            else ++AudioPlayer.songPosition
        } else {
            if (AudioPlayer.songPosition == 0) AudioPlayer.songPosition =
                AudioPlayer.musicListPA.size - 1
            else --AudioPlayer.songPosition

        }
    }
}