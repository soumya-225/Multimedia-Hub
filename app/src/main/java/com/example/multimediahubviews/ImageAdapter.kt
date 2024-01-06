package com.example.multimediahubviews

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(private var list: ArrayList<DataModel>, private var context: Context) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    private var image: ArrayList<DataModel> = ArrayList<DataModel>()
    init {
        image = list
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.image_rv_item, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return image.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(list[position].path).into(holder.imageView)

        holder.itemView.setOnClickListener {
            val parseData = list[position].path.toString()
            val intent = Intent(context, ImageFullScreenActivity::class.java)
                .putExtra("parseData", parseData)
                .addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        val imageModel: DataModel = image[position]
        holder.title.text = imageModel.title
        holder.size.text = parseFileLength(imageModel.size!!.toLong())
        holder.lastModified.text = convertEpochToDate(imageModel.lastModified!!.toLong() * 1000)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var title: TextView
        var size: TextView
        var lastModified: TextView
        init {
            imageView = itemView.findViewById(R.id.imageView2)
            title = itemView.findViewById(R.id.image_file_name)
            size = itemView.findViewById(R.id.image_file_size)
            lastModified = itemView.findViewById(R.id.image_last_modified)
        }
    }
}
