package com.example.multimediahubviews

import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AudioFragment : Fragment() {
    private var audioList: ArrayList<DataModel> = ArrayList<DataModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = AudioAdapter(audioList)


        // To get all audio files from device
        val contentResolver = requireContext().contentResolver
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID
        )
        val sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC"

        contentResolver.query(audioUri, projection, null, null, sortOrder).use { cursor ->
            if (cursor == null) return@use
            while (cursor.moveToNext()) {
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val size =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                val lastModified =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                val audioModel = DataModel(title, path.toUri(), size, lastModified)
                audioList.add(audioModel)
            }
        }
        return view
    }
}