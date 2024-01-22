package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class PdfAdapter(private var list: List<File>, private var activity: Activity) :
    RecyclerView.Adapter<PdfAdapter.ViewHolder>() {

    private var layoutFile = R.layout.pdf_rv_item_list
    private var lastPosition: Int = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var size: TextView
        var lastModified: TextView

        init {
            name = itemView.findViewById(R.id.pdf_file_name)
            size = itemView.findViewById(R.id.pdf_file_size)
            lastModified = itemView.findViewById(R.id.pdf_last_modified)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layoutFile = if (isGridPdf) R.layout.pdf_rv_item_grid
        else R.layout.pdf_rv_item_list
        val view = LayoutInflater.from(parent.context).inflate(layoutFile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = list[position]
        holder.name.text = file.name
        holder.size.text = parseFileLength(file.length())
        holder.lastModified.text = convertEpochToDate(file.lastModified())

        setAnimation(holder.itemView,position)

        holder.itemView.setOnClickListener {
            val intent = Intent(activity, PdfViewerActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                activity,
                androidx.appcompat.R.anim.abc_slide_in_bottom,
                androidx.appcompat.R.anim.abc_slide_out_bottom
            )
            intent.putExtra("name", file.name)
            intent.putExtra("path", file.path)
            activity.startActivity(intent,options.toBundle())
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(list: List<File>) {
        this.list = list
        this.notifyDataSetChanged()
    }

    private fun setAnimation(viewToAnimate: View, position: Int){
        if (position > lastPosition) {
            val slideIn: Animation = AnimationUtils.loadAnimation(activity, R.anim.rv_anim)
            viewToAnimate.startAnimation(slideIn)
            lastPosition = position
        }

    }
}