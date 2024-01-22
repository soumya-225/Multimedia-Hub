package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.multimediahubviews.databinding.VideoRvItemBinding

class VideoAdapter(private val context: Context, private var videoList: List<VideoModel>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private lateinit var thumbnail: ImageView
    private var lastPosition: Int = -1

    class ViewHolder(binding: VideoRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.videoFileName
        val size = binding.videoFileSize
        val lastModified = binding.videoLastModified
        val imageList = binding.thumbnail1
        val imageGrid = binding.thumbnail2
        val root = binding.root
        val listCard = binding.videoListCard
        val gridCard = binding.videoGridCard
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(VideoRvItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isGridVideo) {
            holder.listCard.visibility = View.GONE
            holder.gridCard.visibility = View.VISIBLE
            thumbnail = holder.imageGrid
        } else {
            holder.listCard.visibility = View.VISIBLE
            holder.gridCard.visibility = View.GONE
            thumbnail = holder.imageList
        }
        holder.title.text = videoList[position].title
        holder.size.text = parseFileLength(videoList[position].size.toLong())
        holder.lastModified.text =
            convertEpochToDate(videoList[position].lastModified.toLong() * 1000)
        Glide.with(context)
            .asBitmap()
            .load(videoList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.play).centerCrop())
            .into(thumbnail)

        setAnimation(holder.itemView,position)

        holder.root.setOnClickListener {
            when {
                VideoFragment.search -> sendIntent(position, ref = "Searched Videos")
                else -> sendIntent(position, ref = "All Videos")
            }
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(videoList: List<VideoModel>) {
        this.videoList = videoList
        this.notifyDataSetChanged()
    }

    private fun sendIntent(pos: Int, ref: String) {
        VideoPlayerActivity.position = pos
        val intent = Intent(context, VideoPlayerActivity::class.java)

        val options = ActivityOptions.makeCustomAnimation(
            context,
            androidx.appcompat.R.anim.abc_slide_in_bottom,
            androidx.appcompat.R.anim.abc_slide_out_bottom
        )
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, options.toBundle())
    }

    private fun setAnimation(viewToAnimate: View, position: Int){
        if (position > lastPosition) {
            val slideIn: Animation = AnimationUtils.loadAnimation(context, R.anim.rv_anim)
            viewToAnimate.startAnimation(slideIn)
            lastPosition = position
        }

    }



}