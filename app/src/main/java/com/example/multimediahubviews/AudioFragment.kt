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
import androidx.recyclerview.widget.RecyclerView
import com.example.multimediahubviews.databinding.FragmentAudioBinding
import java.io.File
import java.util.Locale


var isGridAudio: Boolean = false

class AudioFragment : Fragment() {
    companion object {
        var musicListMA: ArrayList<AudioModel> = ArrayList()
    }

    private lateinit var audioAdapter: AudioAdapter
    private lateinit var binding: FragmentAudioBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var sortOrder: String
    private var spanCount: Int = 1

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio, container, false)
        setHasOptionsMenu(true)

        binding = FragmentAudioBinding.bind(view)
        recyclerView = view.findViewById(R.id.recyclerView)
        sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"

        if (notDarkModeState) {
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch)
                .setIcon(R.drawable.night_mode_icon)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.grid_icon_dark)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(R.drawable.sort_icon_dark)
            searchView = view.findViewById(R.id.search_view1)
            binding.searchView2.visibility = View.GONE
            binding.searchView1.visibility = View.VISIBLE
            binding.recyclerView.verticalScrollbarThumbDrawable =
                ResourcesCompat.getDrawable(resources, R.drawable.scroll_icon_dark, activity?.theme)
        } else {
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch)
                .setIcon(R.drawable.light_mode_icon)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.grid_icon_light)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(R.drawable.sort_icon_light)
            searchView = view.findViewById(R.id.search_view1)
            binding.searchView1.visibility = View.GONE
            binding.searchView2.visibility = View.VISIBLE
            binding.recyclerView.verticalScrollbarThumbDrawable = ResourcesCompat.getDrawable(
                resources, R.drawable.scroll_icon_light, activity?.theme
            )
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
                updateAudioListAndAdapter()
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
            isGridAudio = !isGridAudio
            setUpView()
            true
        }

        setUpSearch()
        musicListMA = getAllAudios()
        audioAdapter = AudioAdapter(musicListMA, requireContext())
        //binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(10)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = audioAdapter
        setUpView()

        return view
    }

    private fun setUpView() {
        spanCount = if (isGridAudio) {
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
        musicListMA = getAllAudios()
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        audioAdapter = AudioAdapter(musicListMA, requireContext())
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

        if (cursor != null) if (cursor.moveToFirst()) do {
            val titleC =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
            val sizeC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
            val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
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
                val audio = AudioModel(pathC, titleC, durationC, sizeC, lastModifiedC, artUriC)
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

    private fun updateAudioListAndAdapter() {
        musicListMA = getAllAudios()
        audioAdapter.filterList(musicListMA)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUpView()
    }
}