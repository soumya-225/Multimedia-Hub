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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AudioAdapter(private var songsList: List<AudioModel>, var context: Context) :
    RecyclerView.Adapter<AudioAdapter.ViewHolder>() {

    private var layoutFile = R.layout.audio_rv_item
    private var lastPosition: Int = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView
        var size: TextView
        var lastModified: TextView
        var image: ImageView

        init {
            titleTextView = itemView.findViewById(R.id.file_name)
            size = itemView.findViewById(R.id.file_size)
            lastModified = itemView.findViewById(R.id.last_modified)
            image = itemView.findViewById(R.id.thumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layoutFile = if (isGridAudio) R.layout.audio_rv_item_grid
        else R.layout.audio_rv_item
        val view: View = LayoutInflater.from(context).inflate(layoutFile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songData = songsList[position]
        holder.titleTextView.text = songData.title
        holder.size.text = parseFileLength(songData.size.toLong())
        holder.lastModified.text = convertEpochToDate(songData.lastModified.toLong() * 1000)


        Glide.with(context)
            .asBitmap()
            .load(songsList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(holder.image)

        setAnimation(holder.itemView,position)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, AudioPlayer::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                context,
                androidx.appcompat.R.anim.abc_slide_in_bottom,
                androidx.appcompat.R.anim.abc_slide_out_bottom
            )
            intent.putExtra("index", position)
            intent.putExtra("class", "AudioAdapter")
            ContextCompat.startActivity(context, intent, options.toBundle())
        }
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(list: List<AudioModel>) {
        this.songsList = list
        this.notifyDataSetChanged()
    }

    private fun setAnimation(viewToAnimate: View, position: Int){
        if (position > lastPosition) {
            val slideIn: Animation = AnimationUtils.loadAnimation(context, R.anim.rv_anim)
            viewToAnimate.startAnimation(slideIn)
            lastPosition = position
        }

    }
}