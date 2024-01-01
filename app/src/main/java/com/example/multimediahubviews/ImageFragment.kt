package com.example.multimediahubviews


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ImageFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var list: ArrayList<ImageModel>? = null
    private var adapter: ImageAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_image, container, false)
        val context = activity?.applicationContext

        recyclerView = view.findViewById(R.id.recyclerView)
        list = ArrayList()

        val layoutManager = GridLayoutManager(context, 2)
        recyclerView!!.layoutManager = layoutManager
        adapter = context?.let { ImageAdapter(list!!, it) }

        recyclerView!!.adapter = adapter

        if (context != null) {
            readSdcard(context)
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun readSdcard(context: Context) {

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(MediaStore.Images.Media._ID)

        context.contentResolver.query(
            collection,
            projection,
            null,
            null
        ).use { cursor ->
            val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID) ?:

            cursor?.moveToFirst()

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn as Int)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    list?.add(ImageModel(contentUri))
                }
            }

            adapter!!.notifyDataSetChanged()
            cursor?.close()
        }
    }
}
