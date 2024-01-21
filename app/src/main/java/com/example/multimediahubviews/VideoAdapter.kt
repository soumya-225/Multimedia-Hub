package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.multimediahubviews.databinding.VideoRvItemBinding

class VideoAdapter(private val context: Context, private var videoList: ArrayList<VideoModel>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private lateinit var thumbnail: ImageView

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
    fun filterList(list: List<VideoModel>) {
        this.videoList = list as ArrayList<VideoModel>
        this.notifyDataSetChanged()
    }

    private fun sendIntent(pos: Int, ref: String) {
        VideoPlayerActivity.position = pos
        val intent = Intent(context, VideoPlayerActivity::class.java)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

}