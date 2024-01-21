package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.multimediahubviews.databinding.FragmentAudioBinding
import java.io.File
import java.util.Locale


var isGridAudio: Boolean = false

class AudioFragment : Fragment() {
    companion object {
        var musicListMA: ArrayList<AudioModel> = ArrayList()
    }

    private var audioList: ArrayList<AudioModel> = ArrayList()
    private lateinit var searchView: SearchView
    private lateinit var audioAdapter: AudioAdapter
    private lateinit var sortOrder: String
    private lateinit var recyclerView: RecyclerView
    private var spanCount: Int = 1
    private lateinit var binding: FragmentAudioBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio, container, false)
        binding = FragmentAudioBinding.bind(view)
        setHasOptionsMenu(true)

        recyclerView = view.findViewById(R.id.recyclerView)
        sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"
        searchView = view.findViewById(R.id.search_view)

        if (darkModeState) {
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.baseline_dark_mode_24)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.baseline_grid_view_24)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(R.drawable.baseline_sort_24)
        }
        else{
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.baseline_light_mode_24)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.baseline_grid_view_24_light)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(R.drawable.baseline_sort_24_light)
        }

        val sortButton = binding.topAppBar.menu.findItem(R.id.sort_switch)
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
                audioAdapter.filterList(getAllAudios())
                true
            }
            popupMenu.show()
            true
        }

        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setOnMenuItemClickListener {
            darkModeState = !darkModeState
            if (darkModeState) lightMode()
            else darkMode()
            true
        }

        binding.topAppBar.menu.findItem(R.id.view_switch).setOnMenuItemClickListener {
            isGridAudio = !isGridAudio
            setUpView()
            true
        }

        setUpSearch()
        musicListMA = getAllAudios()
        audioAdapter = AudioAdapter(musicListMA, requireContext())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(10)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = audioAdapter
        setUpView()

        return view
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUpView()
    }

    private fun setUpView() {
        spanCount = if (isGridAudio) {
            if (darkModeState) binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.baseline_view_list_24)
            else binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.baseline_view_list_24_light)
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6
            else 3
        } else{
            if (darkModeState) binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.baseline_grid_view_24)
            else binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.baseline_grid_view_24_light)
            1
        }
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        audioAdapter = AudioAdapter(audioList, requireContext())
        recyclerView.adapter = audioAdapter
        audioAdapter.filterList(getAllAudios())
    }

    private fun getAllAudios(): ArrayList<AudioModel> {
        val tempList = ArrayList<AudioModel>()

        val contentResolver = requireContext().contentResolver
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = contentResolver.query(audioUri, projection, null, null, sortOrder)

        if (cursor != null)
            if (cursor.moveToFirst())
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val sizeC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                    val pathC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val lastModifiedC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                    val durationC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC)

                    try {
                        val file = File(pathC)
                        val audio =
                            AudioModel(pathC, titleC, durationC, sizeC, lastModifiedC, artUriC)
                        if (file.exists()) tempList.add(audio)
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
        val list1: MutableList<AudioModel> = ArrayList()

        for (file: AudioModel in musicListMA) {
            if (file.title.lowercase(Locale.getDefault()).contains(newText)) {
                list1.add(file)
            }
        }
        audioAdapter.filterList(list1)
    }
}