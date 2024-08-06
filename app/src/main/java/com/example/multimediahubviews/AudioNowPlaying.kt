package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.multimediahubviews.databinding.FragmentNowPlayingBinding


class AudioNowPlaying : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    private var isNowPlaying: Boolean = AudioPlayer.isPlaying

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)

        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.GONE

        binding.playPauseBtnNP.setOnClickListener {
            isNowPlaying = !isNowPlaying
            if (isNowPlaying) pauseMusic()
            else playMusic()
        }

        binding.nextBtnNP.setOnClickListener {
            setSongPosition(true)
            AudioPlayer.audioService!!.createMediaPlayer()

            Glide.with(this).asBitmap()
                .load(AudioPlayer.musicListPA[AudioPlayer.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.audio_thumbnail_2).centerCrop())
                .into(binding.songImgNP)

            binding.songNameNP.text = AudioPlayer.musicListPA[AudioPlayer.songPosition].title
            AudioPlayer.audioService!!.showNotification(R.drawable.pause_icon_dark)
            playMusic()

        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), AudioPlayer::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                context,
                androidx.appcompat.R.anim.abc_slide_in_bottom,
                androidx.appcompat.R.anim.abc_slide_out_bottom
            )
            intent.putExtra("index", AudioPlayer.songPosition)
            intent.putExtra("class", "AudioNowPlaying")
            ContextCompat.startActivity(requireContext(), intent, options.toBundle())
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (AudioPlayer.audioService != null) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true

            Glide.with(this).asBitmap()
                .load(AudioPlayer.musicListPA[AudioPlayer.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.audio_thumbnail_2).centerCrop())
                .into(binding.songImgNP)

            binding.songNameNP.text = AudioPlayer.musicListPA[AudioPlayer.songPosition].title

            if (AudioPlayer.isPlaying) binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon_dark)
            else binding.playPauseBtnNP.setImageResource(R.drawable.play_icon_dark)
        }
    }

    private fun playMusic() {
        AudioPlayer.audioService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon_dark)
        AudioPlayer.audioService!!.showNotification(R.drawable.pause_icon_dark)
        AudioPlayer.binding.nextBtnPA.setIconResource(R.drawable.pause_icon_dark)
        AudioPlayer.isPlaying = true
    }

    private fun pauseMusic() {
        AudioPlayer.audioService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setImageResource(R.drawable.play_icon_dark)
        AudioPlayer.audioService!!.showNotification(R.drawable.play_icon_dark)
        AudioPlayer.binding.nextBtnPA.setIconResource(R.drawable.play_icon_dark)
        AudioPlayer.isPlaying = true
    }
}

