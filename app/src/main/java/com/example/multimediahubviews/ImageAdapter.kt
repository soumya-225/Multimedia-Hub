package com.example.multimediahubviews

import android.annotation.SuppressLint
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
import com.bumptech.glide.request.RequestOptions

class ImageAdapter(private var imageList: ArrayList<ImageModel>, private var context: Context) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        private var layoutFile: Int = R.layout.image_rv_item

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layoutFile = if (isGridImage) R.layout.image_rv_item_old
        else R.layout.image_rv_item
        val view: View = LayoutInflater.from(parent.context).inflate(layoutFile, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(imageList[position].path)
            .apply(RequestOptions().placeholder(R.drawable.photo_gallery).centerCrop())
            .into(holder.imageView)

        val imageModel: ImageModel = imageList[position]
        holder.title.text = imageModel.title
        holder.size.text = parseFileLength(imageModel.size.toLong())
        holder.lastModified.text = convertEpochToDate(imageModel.lastModified.toLong() * 1000)

        holder.itemView.setOnClickListener {
            val parseData = imageList[position].path.toString()
            val fileName = imageList[position].title
            val intent = Intent(context, ImageViewerActivity::class.java)
                .putExtra("parseData", parseData)
                .putExtra("fileName",fileName)
                .addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(list: List<ImageModel>) {
        this.imageList = list as ArrayList<ImageModel>
        this.notifyDataSetChanged()
    }
}