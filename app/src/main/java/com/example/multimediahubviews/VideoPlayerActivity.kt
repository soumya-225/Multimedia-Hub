package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.multimediahubviews.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter.Configuration
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import java.io.File

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    companion object {
        lateinit var player: SimpleExoPlayer
        lateinit var playerList: ArrayList<VideoModel>
        var position: Int = -1
        var repeat: Boolean = false
        var isFullscreen: Boolean = false
        private var isLocked = false
    }

    @SuppressLint("InlinedApi", "WrongConstant", "Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        binding = ActivityPlayerBinding.inflate(layoutInflater)

        //for immersive mode
        setTheme(R.style.playerActivityTheme)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            //controller.hide(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
            //controller.systemBarsBehavior =
                //WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        //for handling video file intent
        try {
            if (intent.data?.scheme.contentEquals("content")) {
                playerList = ArrayList()
                position = 0
                val cursor = contentResolver.query(
                    intent.data!!,
                    arrayOf(MediaStore.Video.Media.DATA),
                    null,
                    null,
                    null
                )
                cursor?.let {
                    it.moveToFirst()
                    val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val file = File(path)
                    val video = VideoModel(
                        id = "",
                        title = file.name,
                        duration = 0L,
                        artUri = Uri.fromFile(file),
                        path = path,
                        size = "",
                        folderName = "",
                        lastModified = ""
                    )
                    playerList.add(video)
                    cursor.close()
                }
                createPlayer()
                initializeBinding()
            } else {
                initializeLayout()
                initializeBinding()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeLayout() {
        playerList = ArrayList()
        playerList.addAll(VideoFragment.videoList)
        createPlayer()
        /*when (intent.getStringExtra("class")) {
            "All Videos" -> {
                playerList = ArrayList()
                playerList.addAll(VideoFragment.videoList)
                createPlayer()
            }
            "SearchedVideos" -> {
                playerList = ArrayList()
                playerList.addAll(VideoFragment.searchList)
                createPlayer()
            }
        }*/

        if (repeat) binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
        else binding.repeatBtn.setImageResource(com.google.android.exoplayer2.R.drawable.exo_controls_repeat_off)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initializeBinding() {

        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.playPauseBtn.setOnClickListener {
            if (player.isPlaying) pauseVideo()
            else playVideo()

        }
        binding.nextBtn.setOnClickListener {
            nextPrevVideo()
        }
        binding.prevBtn.setOnClickListener {
            nextPrevVideo(isNext = false)
        }
        binding.repeatBtn.setOnClickListener {
            if (repeat) {
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)
            } else {
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_ONE
                binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
            }
        }
        binding.fullScreenBtn.setOnClickListener {
            if (isFullscreen) {
                isFullscreen = false
                playInFullScreen(enable = false)
            } else {
                isFullscreen = true
                playInFullScreen(enable = true)
            }
        }
        binding.lockBtn.setOnClickListener {
            if (!isLocked) {
                isLocked = true
                binding.playerView.hideController()
                binding.playerView.useController = false
                binding.lockBtn.setImageResource(R.drawable.baseline_lock_24)
            } else {
                isLocked = false
                binding.playerView.hideController()
                binding.playerView.useController = true
                binding.lockBtn.setImageResource(R.drawable.baseline_lock_open_24)
            }
        }

        findViewById<ImageButton>(R.id.orientationBtn).setOnClickListener {
            requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT)
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        }
    }

    private fun createPlayer() {
        try {
            player.release()
        } catch (e: Exception) {
        }
        player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = player

        binding.videoTitle.text = playerList[position].title
        binding.videoTitle.isSelected = true

        val mediaItem = com.google.android.exoplayer2.MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) nextPrevVideo()
            }
        })
        playInFullScreen(enable = isFullscreen)
        setVisibility()
    }

    private fun playVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.baseline_pause_24)
        player.play()
    }

    private fun pauseVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        player.pause()
    }

    private fun nextPrevVideo(isNext: Boolean = true) {
        if (isNext) setPosition()
        else setPosition(isIncrement = false)
        createPlayer()
    }

    private fun setPosition(isIncrement: Boolean = true) {
        if (!repeat) {
            if (isIncrement) {
                if (playerList.size - 1 == position)
                    position = 0
                else ++position
            } else {
                if (position == 0)
                    position = playerList.size - 1
                else --position
            }
        }
    }

    private fun playInFullScreen(enable: Boolean) {
        if (enable) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullScreenBtn.setImageResource(R.drawable.baseline_fullscreen_exit_24)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.hide(WindowInsetsCompat.Type.statusBars())
                //controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } }else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullScreenBtn.setImageResource(R.drawable.baseline_fullscreen_24)
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.show(WindowInsetsCompat.Type.navigationBars())
                controller.show(WindowInsetsCompat.Type.statusBars())}
        }
    }

    private fun setVisibility() {
        runnable = Runnable {
            if (binding.playerView.isControllerVisible) changeVisibility(View.VISIBLE)
            else changeVisibility(View.INVISIBLE)
            Handler(Looper.getMainLooper()).postDelayed(runnable, 100)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun changeVisibility(visibility: Int) {
        binding.topController.visibility = visibility
        binding.bottomController.visibility = visibility
        binding.playPauseBtn.visibility = visibility
        if (isLocked) binding.lockBtn.visibility = View.VISIBLE
        else binding.lockBtn.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}