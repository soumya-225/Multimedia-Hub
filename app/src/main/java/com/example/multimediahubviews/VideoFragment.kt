package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class VideoFragment : Fragment() {
    private var videoList: ArrayList<DataModel> = ArrayList<DataModel>()

    @SuppressLint("Recycle")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = VideoAdapter(videoList)

        //To get All Video Files From Device
        val contentResolver = requireContext().contentResolver
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED
        )
        val sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"
        val cursor = contentResolver.query(videoUri,projection,null,null,sortOrder)

        if (cursor != null){
            while (cursor.moveToNext()){
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                val lastModified = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                //val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                //ContentUris.withAppendedId(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, id)

                val videoModel = DataModel(title,path.toUri(), size, lastModified)
                videoList.add(videoModel)
            }
        }
        return view
    }
}