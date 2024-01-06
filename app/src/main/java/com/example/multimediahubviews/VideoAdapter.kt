package com.example.multimediahubviews

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VideoAdapter(videos: ArrayList<DataModel>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var video: ArrayList<DataModel> = ArrayList<DataModel>()

    init {
        video = videos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.video_rv_item,parent,false)
        return VideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return video.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoModel: DataModel = video[position]
        val thumbnail = getThumbnailFromVideo(videoModel.path.toString(), holder.itemView.context)

        holder.title.text = videoModel.title
        holder.size.text = videoModel.size?.let { parseFileLength(it.toLong()) }
        holder.lastModified.text = videoModel.lastModified?.let { convertEpochToDate(it.toLong() * 1000) }
        holder.thumbnail.setImageBitmap(thumbnail)
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var title: TextView
        var size: TextView
        var lastModified: TextView
        var thumbnail: ImageView
        init {
            title = itemView.findViewById(R.id.video_file_name)
            size = itemView.findViewById(R.id.video_file_size)
            lastModified = itemView.findViewById(R.id.video_last_modified)
            thumbnail = itemView.findViewById(R.id.thumbnail)
        }
    }

    private fun getThumbnailFromVideo(videoPath: String, context: Context): Bitmap {
        val mediaStoreId = getMediaStoreIdFromPath(videoPath, context.contentResolver)
        val thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
            context.contentResolver,
            mediaStoreId,
            MediaStore.Video.Thumbnails.MICRO_KIND,
            null
        )
        return thumbnail ?: BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground)
    }

    private fun getMediaStoreIdFromPath(videoPath: String, contentResolver: ContentResolver): Long {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.TITLE
        )
        val selection = "${MediaStore.Video.Media.DATA} = ?"
        val selectionArgs = arrayOf(videoPath)
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                return cursor.getLong(idColumn)
            }
        }
        return -1 // Return a default value if not found
    }
}
