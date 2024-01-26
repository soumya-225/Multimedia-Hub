package com.example.multimediahubviews


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.res.Configuration
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
import com.example.multimediahubviews.databinding.FragmentImageBinding
import java.io.File
import java.util.Locale

var isGridImage: Boolean = false

class ImageFragment : Fragment() {
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var binding: FragmentImageBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var list: ArrayList<ImageModel>
    private lateinit var searchView: SearchView
    private lateinit var sortOrder: String
    private lateinit var imageList: ArrayList<ImageModel>
    private var spanCount = 1


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        val context = activity?.applicationContext
        binding = FragmentImageBinding.bind(view)
        val sortButton = binding.topAppBar.menu.findItem(R.id.sort_switch)


        sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        recyclerView = view.findViewById(R.id.recyclerView)
        list = ArrayList()

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
            searchView = view.findViewById(R.id.search_view2)
            binding.searchView1.visibility = View.GONE
            binding.searchView2.visibility = View.VISIBLE
            binding.recyclerView.verticalScrollbarThumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.scroll_icon_light,
                activity?.theme
            )
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
            isGridImage = !isGridImage
            setupView()
            true
        }

        sortButton.setOnMenuItemClickListener {
            val menuItemView: View = view.findViewById(R.id.sort_switch)

            val popupMenu = PopupMenu(context, menuItemView)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.name -> sortOrder = MediaStore.Images.Media.DISPLAY_NAME
                    R.id.date_modified -> sortOrder =
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC"

                    R.id.size -> sortOrder = MediaStore.Images.Media.SIZE + " DESC"
                }
                imageAdapter.filterList(getAllImages2())
                true
            }
            popupMenu.show()
            true

        }

        setUpSearch(searchView)
        imageList = getAllImages2()
        imageAdapter = ImageAdapter(imageList, requireContext())
        //binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(10)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = imageAdapter

        getAllImages2()
        setupView()
        return view
    }

    private fun setupView() {
        spanCount = if (isGridImage) {
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
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        imageAdapter = ImageAdapter(list, requireContext())
        recyclerView.adapter = imageAdapter
        imageAdapter.filterList(getAllImages2())
    }

    private fun getAllImages2(): ArrayList<ImageModel> {
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

        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val idC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val titleC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                    val sizeC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    val pathC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    val lastModifiedC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        idC
                    )


                    try {
                        val file = File(pathC)
                        val image = ImageModel(
                            title = titleC, size = sizeC,
                            path = contentUri, lastModified = lastModifiedC
                        )
                        if (file.exists()) tempList.add(image)
                    } catch (_: Exception) {
                    }
                } while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }

    private fun setUpSearch(searchView: SearchView) {
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupView()
    }

    fun filter(newText: String) {
        val list1: MutableList<ImageModel> = ArrayList()

        for (file: ImageModel in imageList) {
            if (file.title.lowercase(Locale.getDefault()).contains(newText)) {
                list1.add(file)
            }
        }
        imageAdapter.filterList(list1)
    }
}