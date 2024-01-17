package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Switch
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.core.view.MenuItemCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.multimediahubviews.databinding.FragmentVideoBinding
import java.io.File
import java.util.Locale
import java.util.jar.Attributes.Name


class VideoFragment : Fragment() {

    private lateinit var videoAdapter: VideoAdapter
    private lateinit var searchView: SearchView
    private lateinit var sortOrder: String
    //var darkModeState: Boolean = true

    companion object{
        lateinit var videoList: ArrayList<VideoModel>
        var search: Boolean = false
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.sort_switch -> {
                Toast.makeText(requireContext(),"SORT",Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.view_switch -> {
                Toast.makeText(requireContext(),"VIEW",Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.search_view -> {
                setUpSearch()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
        //return true
        //return super.onOptionsItemSelected(item)
    }*/

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //supportActionBar.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
    }*/

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        val binding = FragmentVideoBinding.bind(view)
        val sortButton = binding.topAppBar.menu.findItem(R.id.sort_switch)

        sortButton.setOnMenuItemClickListener {
            /*sortState = !sortState
            //Log.d("Tag",sortState.toString())
            //Toast.makeText(requireContext(), "Sort", Toast.LENGTH_SHORT).show()
            sortOrder = if (sortState) MediaStore.Video.Media.DISPLAY_NAME
            else MediaStore.Video.Media.DATE_MODIFIED + " DESC"
            videoAdapter.filterList(getAllVideos())*/
            //FragmentDialogBox().show(requireActivity().supportFragmentManager,"fragment Dialog")
            // Initializing the popup menu and giving the reference as current context
            val menuItemView: View = view.findViewById(R.id.sort_switch)
            //val popupMenu = PopupMenu(context, MenuItemCompat.getActionView(sortButton) )
            val popupMenu = PopupMenu(context, menuItemView)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                menuItem ->
                when (menuItem.itemId) {
                    R.id.name -> sortOrder = MediaStore.Video.Media.DISPLAY_NAME
                    R.id.date_modified -> sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"
                    R.id.size -> sortOrder = MediaStore.Video.Media.SIZE + " DESC"
                }
                videoAdapter.filterList(getAllVideos())
                //Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT).show()
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

        searchView = view.findViewById(R.id.search_view)
        setUpSearch()
        videoList = getAllVideos()
        videoAdapter = VideoAdapter(requireContext(), videoList)

        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.setItemViewCacheSize(10)
        binding.VideoRV.layoutManager = LinearLayoutManager(requireContext())
        binding.VideoRV.adapter = videoAdapter

        return view
    }

    private fun getAllVideos(): ArrayList<VideoModel>{

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

        if(cursor != null)
            if(cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))?:"Unknown"
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))?:"Unknown"
                    val folderC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))?:"Internal Storage"
                    val sizeC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))?:"0"
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))?:"Unknown"
                    val lastModifiedC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                    val durationC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))?.toLong()?:0L

                    try {
                        val file = File(pathC)
                        val artUriC = Uri.fromFile(file)
                        val video = VideoModel(title = titleC, id = idC, folderName = folderC, duration = durationC, size = sizeC,
                            path = pathC, artUri = artUriC, lastModified = lastModifiedC)
                        if(file.exists()) tempList.add(video)
                    }catch (_:Exception){}
                }while (cursor.moveToNext())
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

        for(file: VideoModel in videoList){
            if (file.title.lowercase(Locale.getDefault()).contains(newText)){
                list1.add(file)
            }
        }
        videoAdapter.filterList(list1)
    }
}