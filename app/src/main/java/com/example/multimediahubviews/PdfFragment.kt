package com.example.multimediahubviews


import android.annotation.SuppressLint
import android.content.res.Configuration
import android.database.Cursor
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
import com.example.multimediahubviews.R.*
import com.example.multimediahubviews.databinding.FragmentPdfBinding
import java.io.File
import java.util.Locale

var isGridPdf: Boolean = false

class PdfFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var pdfAdapter: PdfAdapter
    private lateinit var pdfList: ArrayList<File>
    private lateinit var searchView: SearchView
    private lateinit var sortOrder: String
    private var spanCount: Int = 1
    private lateinit var binding: FragmentPdfBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layout.fragment_pdf, container, false)
        binding = FragmentPdfBinding.bind(view)

        recyclerView = view.findViewById(R.id.recycler_view)

        pdfList = arrayListOf()
        sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"

        if (notDarkModeState) {
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(drawable.night_mode_icon)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(drawable.grid_icon_dark)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(drawable.sort_icon_dark)
            searchView = view.findViewById(R.id.search_view1)
            binding.searchView2.visibility = View.GONE
            binding.searchView1.visibility = View.VISIBLE
            binding.recyclerView.verticalScrollbarThumbDrawable = ResourcesCompat.getDrawable(resources, drawable.scroll_icon_dark, activity?.theme)


        }
        else{
            binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(drawable.light_mode_icon)
            binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(drawable.grid_icon_light)
            binding.topAppBar.menu.findItem(R.id.sort_switch).setIcon(drawable.sort_icon_light)
            searchView = view.findViewById(R.id.search_view2)
            binding.searchView1.visibility = View.GONE
            binding.searchView2.visibility = View.VISIBLE
            binding.recyclerView.verticalScrollbarThumbDrawable = ResourcesCompat.getDrawable(resources, drawable.scroll_icon_light, activity?.theme)
        }


        val sortButton = binding.topAppBar.menu.findItem(R.id.sort_switch)
        sortButton.setOnMenuItemClickListener {
            val menuItemView: View = view.findViewById(R.id.sort_switch)
            val popupMenu = PopupMenu(context, menuItemView)
            popupMenu.menuInflater.inflate(menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.name -> sortOrder = MediaStore.Video.Media.DISPLAY_NAME
                    R.id.date_modified -> sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"
                    R.id.size -> sortOrder = MediaStore.Video.Media.SIZE + " DESC"
                }
                pdfAdapter.filterList(getAllFiles())
                true
            }
            popupMenu.show()
            true
        }

        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setOnMenuItemClickListener {
            notDarkModeState = !notDarkModeState
            if (notDarkModeState) {
                Toast.makeText(context,"Turning Off Dark Mode...",Toast.LENGTH_SHORT).show()
                lightMode()
            }
            else {
                Toast.makeText(context,"Turning On Dark Mode...",Toast.LENGTH_SHORT).show()
                darkMode()
            }
            true
        }

        binding.topAppBar.menu.findItem(R.id.view_switch).setOnMenuItemClickListener {
            isGridPdf = !isGridPdf
            setUpView()
            true
        }

        setUpSearch()
        val files = getAllFiles()
        pdfList.addAll(files)
        pdfAdapter = PdfAdapter(pdfList, requireActivity())
        recyclerView.adapter = pdfAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        //recyclerView.setHasFixedSize(true)

        getAllFiles()
        setUpView()
        return view
    }

    private fun setUpView() {
        spanCount = if (isGridPdf) {
            if (notDarkModeState) binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(drawable.list_icon_dark)
            else binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(drawable.list_icon_light)
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6
            else 3
        } else {
            if (notDarkModeState) binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(drawable.grid_icon_dark)
            else binding.topAppBar.menu.findItem(R.id.view_switch).setIcon(drawable.grid_icon_light)
            1
        }
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        pdfAdapter = PdfAdapter(pdfList, requireActivity())
        recyclerView.adapter = pdfAdapter
        pdfAdapter.filterList(getAllFiles())
    }

    private fun getAllFiles(): List<File> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val selectionArgs = arrayOf("application/pdf")
        val cursor: Cursor? =
            context?.contentResolver?.query(uri, projection, selection, selectionArgs, sortOrder)
        val list = arrayListOf<File>()
        val pdfPathIndex = cursor?.getColumnIndex(MediaStore.Files.FileColumns.DATA)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (pdfPathIndex != -1) {
                    val pdfPath = cursor.getString(pdfPathIndex!!)
                    val pdfFile = File(pdfPath)
                    if (pdfFile.exists() && pdfFile.isFile) {
                        list.add(pdfFile)
                    }
                }
            }
        }
        cursor?.close()
        return list
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
        val list1: MutableList<File> = ArrayList()

        for (file: File in pdfList) {
            if (file.name.lowercase(Locale.getDefault()).contains(newText)) {
                list1.add(file)
            }
        }
        pdfAdapter.filterList(list1)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUpView()
    }
}