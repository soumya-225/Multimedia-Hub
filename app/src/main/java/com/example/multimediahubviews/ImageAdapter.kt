package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ImageAdapter(private var imageList: ArrayList<ImageModel>, private var context: Context) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var layoutFile: Int = R.layout.image_rv_item_list
    private var lastPosition: Int = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbnailImageView: ImageView
        var titleTextView: TextView
        var sizeTextView: TextView
        var lastModifiedTextView: TextView

        init {
            thumbnailImageView = itemView.findViewById(R.id.image_thumbnail)
            titleTextView = itemView.findViewById(R.id.image_file_name)
            sizeTextView = itemView.findViewById(R.id.image_file_size)
            lastModifiedTextView = itemView.findViewById(R.id.image_last_modified)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layoutFile = if (isGridImage) R.layout.image_rv_item_grid
        else R.layout.image_rv_item_list
        val view: View = LayoutInflater.from(parent.context).inflate(layoutFile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(imageList[position].path)
            .apply(RequestOptions().centerCrop())
            .into(holder.thumbnailImageView)

        val imageModel: ImageModel = imageList[position]
        holder.titleTextView.text = imageModel.title
        holder.sizeTextView.text = parseFileLength(imageModel.size.toLong())
        holder.lastModifiedTextView.text = convertEpochToDate(imageModel.lastModified.toLong() * 1000)

        setAnimation(holder.itemView,position)

        holder.itemView.setOnClickListener {
            val parseData = imageList[position].path.toString()
            val fileName = imageList[position].title

            val options = ActivityOptions.makeCustomAnimation(
                context,
                androidx.appcompat.R.anim.abc_fade_in,
                androidx.appcompat.R.anim.abc_fade_out
            )
            val intent = Intent(context, ImageViewerActivity::class.java)
                .putExtra("parseData", parseData)
                .putExtra("fileName", fileName)
                .addFlags(FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent,options.toBundle())
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(list: List<ImageModel>) {
        this.imageList = list as ArrayList<ImageModel>
        this.notifyDataSetChanged()
    }

    private fun setAnimation(viewToAnimate: View, position: Int){
        if (position > lastPosition) {
            val slideIn: Animation = AnimationUtils.loadAnimation(context, R.anim.recycler_view_item_anim_2)
            viewToAnimate.startAnimation(slideIn)
            lastPosition = position
        }
    }
}