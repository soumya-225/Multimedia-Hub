package com.example.multimediahubviews


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.multimediahubviews.databinding.FragmentImageBinding
import java.io.File
import java.util.Locale

var isGridImage: Boolean = false


class ImageFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var list: ArrayList<ImageModel>
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imageAdapterGrid: ImageAdapterGrid
    private lateinit var searchView: SearchView
    private lateinit var sortOrder: String
    private lateinit var imageList: ArrayList<ImageModel>
    private var spanCount: Int = 1

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        val context = activity?.applicationContext
        val binding = FragmentImageBinding.bind(view)
        val sortButton = binding.topAppBar.menu.findItem(R.id.sort_switch)


        sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.search_view)
        list = ArrayList()

        //val layoutManager =

        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setOnMenuItemClickListener {
            //binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.baseline_light_mode_24)
            darkModeState = !darkModeState
            if (darkModeState) {
                binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.baseline_dark_mode_24)
                lightMode()
            }
            else{
                binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.baseline_light_mode_24)
                binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(R.drawable.baseline_grid_view_24_light)
                darkMode()
            }
            true
        }

        binding.topAppBar.menu.findItem(R.id.view_switch).setOnMenuItemClickListener {
            isGridImage = !isGridImage
            spanCount = if (isGridImage) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6
                else 3
            } else 1
            recyclerView.layoutManager = GridLayoutManager(context, spanCount)
            imageAdapter = ImageAdapter(list, requireContext())
            recyclerView.adapter = imageAdapter
            imageAdapter.filterList(getAllImages(requireContext()))
            true
        }

        sortButton.setOnMenuItemClickListener {
            val menuItemView: View = view.findViewById(R.id.sort_switch)

            val popupMenu = PopupMenu(context, menuItemView)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                    menuItem ->
                when (menuItem.itemId) {
                    R.id.name -> sortOrder = MediaStore.Images.Media.DISPLAY_NAME
                    R.id.date_modified -> sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                    R.id.size -> sortOrder = MediaStore.Images.Media.SIZE + " DESC"
                }
                imageAdapter.filterList(getAllImages2())
                //Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT).show()
                true
            }
            popupMenu.show()
            true

        }




        /*binding.topAppBar.menu.findItem(R.id.sort_switch).setOnMenuItemClickListener {
            sortState = !sortState
            sortOrder = if (sortState) MediaStore.Images.Media.DISPLAY_NAME
            else MediaStore.Images.Media.DATE_MODIFIED + " DESC"
            true
        }*/
        setUpSearch()
        imageList = getAllImages2()
        imageAdapter = ImageAdapter(imageList,requireContext())

        /*recyclerView.layoutManager = GridLayoutManager(context, 1)
        imageAdapter = ImageAdapter(list, context!!)
        recyclerView.adapter = imageAdapter*/

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(10)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = imageAdapter

        getAllImages2()
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAllImages(context: Context): ArrayList<ImageModel> {
        val list = ArrayList<ImageModel>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        //val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        ).use { cursor ->
            val idColumn = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val lastModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            cursor.moveToFirst()

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val imageName = cursor.getString(displayNameColumn)
                val size = cursor.getString(sizeColumn)
                val lastModified = cursor.getString(lastModifiedColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                val imageModel = ImageModel(imageName, contentUri,size,lastModified)
                list.add(imageModel)
            }
            //imageAdapter.notifyDataSetChanged()
            cursor.close()
        }
        //imageAdapter.filterList(list)
        //imageAdapter.notifyDataSetChanged()
        return list
    }

    private fun getAllImages2(): ArrayList<ImageModel>{

        val tempList = ArrayList<ImageModel>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED,
        )
        val cursor = this.context?.contentResolver?.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        if(cursor != null)
            if(cursor.moveToNext())
                do {
                    val idC = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    val lastModifiedC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        idC.toLong()
                    )


                    try {
                        val file = File(pathC)
                        val image = ImageModel(title = titleC, size = sizeC,
                            path = contentUri, lastModified = lastModifiedC)
                        if(file.exists()) tempList.add(image)
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
        val list1: MutableList<ImageModel> = ArrayList()

        for(file: ImageModel in list){
            if (file.title.lowercase(Locale.getDefault()).contains(newText)){
                list1.add(file)
            }
        }
        imageAdapter.filterList(list1)
    }
}