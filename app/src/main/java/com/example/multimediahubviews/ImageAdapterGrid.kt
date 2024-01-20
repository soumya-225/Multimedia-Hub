package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ImageAdapterGrid(private var imageList: ArrayList<ImageModel>, private var context: Context) :
    RecyclerView.Adapter<ImageAdapterGrid.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(list: MutableList<ImageModel>) {
        this.imageList = list as ArrayList<ImageModel>
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.image_rv_item_old, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(imageList[position].path)
            .apply(RequestOptions().placeholder(R.drawable.photo_gallery).centerCrop())
            .into(holder.imageView)

        imageList[position]

        holder.itemView.setOnClickListener {
            val parseData = imageList[position].path.toString()
            val fileName = imageList[position].title
            val intent = Intent(context, ImageViewerActivity::class.java)
                .putExtra("parseData", parseData)
                .putExtra("fileName", fileName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        //var title: TextView
        //var size: TextView
        //var lastModified: TextView
        init {
            imageView = itemView.findViewById(R.id.imageView2)
            //title = itemView.findViewById(R.id.image_file_name)
            //size = itemView.findViewById(R.id.image_file_size)
            //lastModified = itemView.findViewById(R.id.image_last_modified)
        }
    }
}