package com.example.multimediahubviews


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AudioAdapter(private var songsList: ArrayList<AudioModel>, var context: Context) :
    RecyclerView.Adapter<AudioAdapter.ViewHolder>() {

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
        val view: View = LayoutInflater.from(context).inflate(R.layout.audio_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songData = songsList[position]
        holder.titleTextView.text = songData.title
        holder.size.text = parseFileLength(songData.size.toLong())
        holder.lastModified.text = convertEpochToDate(songData.lastModified.toLong()*1000)
        if (MyMediaPlayer.currentIndex == position) {
            holder.titleTextView.setTextColor(Color.parseColor("#FF0000"))
        } else {
            holder.titleTextView.setTextColor(Color.parseColor("#000000"))
        }
        Glide.with(context)
            .asBitmap()
            .load(songsList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(holder.image)

        /*holder.itemView.setOnClickListener {
            MyMediaPlayer.getInstance()!!.reset()
            MyMediaPlayer.currentIndex = position
            val intent = Intent(context, AudioPlayerActivity::class.java)
            intent.putExtra("LIST", songsList)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }*/

        holder.itemView.setOnClickListener{
            val intent = Intent(context,AudioPlayer::class.java)
            intent.putExtra("index",position)
            intent.putExtra("class","AudioAdapter")
            ContextCompat.startActivity(context, intent,null)
        }
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(list: List<AudioModel>) {
        this.songsList = list as ArrayList<AudioModel>
        this.notifyDataSetChanged()
    }
}