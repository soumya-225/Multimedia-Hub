package com.example.multimediahubviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioAdapter (audios : ArrayList<DataModel>):
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var audio: ArrayList<DataModel> = ArrayList<DataModel>()
    init {
        audio = audios
    }
    class AudioViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        var title : TextView
        var size: TextView
        var lastModified: TextView
        init {
            title = itemView.findViewById(R.id.file_name)
            size = itemView.findViewById(R.id.file_size)
            lastModified = itemView.findViewById(R.id.last_modified)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view : View = LayoutInflater.from(parent.context)
            .inflate(R.layout.audio_rv_item,parent,false)
        return AudioViewHolder(view)
    }

    override fun getItemCount(): Int {
        return audio.size
    }
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioModel : DataModel = audio[position]
        holder.title.text = audioModel.title
        holder.size.text = parseFileLength(audioModel.size!!.toLong())
        holder.lastModified.text = convertEpochToDate(audioModel.lastModified!!.toLong()*1000)
    }
}