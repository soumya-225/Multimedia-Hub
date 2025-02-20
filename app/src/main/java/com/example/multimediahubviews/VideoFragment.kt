package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.multimediahubviews.databinding.FragmentVideoBinding
import java.io.File
import java.util.Locale


var isGridVideo: Boolean = false

class VideoFragment : Fragment() {
    companion object {
        lateinit var videoList: ArrayList<VideoModel>
        var search: Boolean = false
    }

    private lateinit var videoAdapter: VideoAdapter
    private lateinit var binding: FragmentVideoBinding
    private lateinit var searchView: SearchView
    private lateinit var sortOrder: String
    private var spanCount: Int = 1

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        setHasOptionsMenu(true)

        binding = FragmentVideoBinding.bind(view)
        sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"
        val sortButton = binding.topAppBar.menu.findItem(R.id.sort_switch)

        if (notDarkModeState) {
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.night_mode_icon)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.grid_icon_dark)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(R.drawable.sort_icon_dark)
            searchView = view.findViewById(R.id.search_view1)
            binding.searchView2.visibility = View.GONE
            binding.searchView1.visibility = View.VISIBLE
            binding.VideoRV.verticalScrollbarThumbDrawable =
                ResourcesCompat.getDrawable(resources, R.drawable.scroll_icon_dark, activity?.theme)
        } else {
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.light_mode_icon)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.grid_icon_light)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(R.drawable.sort_icon_light)
            searchView = view.findViewById(R.id.search_view2)
            binding.searchView1.visibility = View.GONE
            binding.searchView2.visibility = View.VISIBLE
            binding.VideoRV.verticalScrollbarThumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.scroll_icon_light,
                activity?.theme
            )
        }

        sortButton.setOnMenuItemClickListener {
            val menuItemView: View = view.findViewById(R.id.sort_switch)
            val popupMenu = PopupMenu(context, menuItemView)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.name -> sortOrder = MediaStore.Video.Media.DISPLAY_NAME
                    R.id.date_modified -> sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"
                    R.id.size -> sortOrder = MediaStore.Video.Media.SIZE + " DESC"
                }
                videoAdapter.filterList(getAllVideos())
                updateVideoListAndAdapter()
                true
            }
            popupMenu.show()
            true
        }

        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setOnMenuItemClickListener {
            notDarkModeState = !notDarkModeState
            if (notDarkModeState) {
                Toast.makeText(context, "Turning Off Dark Mode...", Toast.LENGTH_SHORT).show()
                lightMode()
            } else {
                Toast.makeText(context, "Turning On Dark Mode...", Toast.LENGTH_SHORT).show()
                darkMode()
            }
            true
        }

        binding.topAppBar.menu.findItem(R.id.view_switch).setOnMenuItemClickListener {
            isGridVideo = !isGridVideo
            setUpView()
            true
        }

        setUpSearch()
        videoList = getAllVideos()
        videoAdapter = VideoAdapter(requireContext(), videoList)
        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.setItemViewCacheSize(10)
        binding.VideoRV.layoutManager = LinearLayoutManager(requireContext())
        binding.VideoRV.adapter = videoAdapter
        setUpView()

        return view
    }

    private fun getAllVideos(): ArrayList<VideoModel> {
        val tempList = ArrayList<VideoModel>()

        val projection = arrayOf(
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID
        )
        val cursor = this.context?.contentResolver?.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val lastModifiedC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                    val durationC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)).toLong()

                    try {
                        val file = File(pathC)
                        val artUriC = Uri.fromFile(file)
                        val video = VideoModel(
                            title = titleC,
                            id = idC,
                            folderName = folderC,
                            duration = durationC,
                            size = sizeC,
                            path = pathC,
                            artUri = artUriC,
                            lastModified = lastModifiedC
                        )
                        if (file.exists()) tempList.add(video)
                    } catch (_: Exception) {
                    }
                } while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }

    private fun setUpSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filter(newText)
                } else {
                    Toast.makeText(context, "No File Found", Toast.LENGTH_SHORT).show()
                }
                return false
            }
        })
    }

    fun filter(newText: String) {
        val list1: MutableList<VideoModel> = ArrayList()

        for (file: VideoModel in videoList) {
            if (file.title.lowercase(Locale.getDefault()).contains(newText)) {
                list1.add(file)
            }
        }
        videoAdapter.filterList(list1)
    }

    private fun setUpView() {
        spanCount = if (isGridVideo) {
            if (notDarkModeState) binding.topAppBar.menu.findItem(R.id.view_switch)
                .setIcon(R.drawable.list_icon_dark)
            else binding.topAppBar.menu.findItem(R.id.view_switch)
                .setIcon(R.drawable.list_icon_light)
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6
            else 3
        } else {
            if (notDarkModeState) binding.topAppBar.menu.findItem(R.id.view_switch)
                .setIcon(R.drawable.grid_icon_dark)
            else binding.topAppBar.menu.findItem(R.id.view_switch)
                .setIcon(R.drawable.grid_icon_light)
            1
        }
        videoList = getAllVideos()
        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.setItemViewCacheSize(10)
        binding.VideoRV.layoutManager = GridLayoutManager(context, spanCount)
        videoAdapter = VideoAdapter(requireContext(), videoList)
        binding.VideoRV.adapter = videoAdapter
        videoAdapter.filterList(getAllVideos())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUpView()
    }

    private fun updateVideoListAndAdapter() {
        videoList = getAllVideos()
        videoAdapter.filterList(videoList)
    }
}