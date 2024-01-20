package com.example.multimediahubviews


import android.annotation.SuppressLint
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.multimediahubviews.databinding.FragmentPdfBinding
import com.example.multimediahubviews.databinding.FragmentVideoBinding
import java.io.File
import java.util.Locale

var isGridPdf: Boolean = false
class PdfFragment() : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var pdfAdapter: PdfAdapter
    private lateinit var list: ArrayList<File>
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var sortOrder: String
    private var spanCount: Int = 1


    @SuppressLint("ResourceType", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pdf, container, false)
        val binding = FragmentPdfBinding.bind(view)
        //setupRecyclerView(view)

        recyclerView = view.findViewById(R.id.recycler_view)!!
        searchView = view.findViewById(R.id.search_view)
        progressBar = view.findViewById(R.id.progressBar)!!
        list = arrayListOf()

        sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"


        val sortButton = binding.topAppBar.menu.findItem(R.id.sort_switch)
        // recyclerView.adapter = AudioAdapter(audioList, requireContext())

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
                pdfAdapter.filterList(getAllFiles())
                true
            }
            popupMenu.show()
            true
        }

        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setOnMenuItemClickListener {
            darkModeState = !darkModeState
            //Toast.makeText(requireContext(), "Dark Mode", Toast.LENGTH_SHORT).show()
            if (darkModeState) lightMode()
            else darkMode()
            true
        }

        binding.topAppBar.menu.findItem(R.id.view_switch).setOnMenuItemClickListener {
            isGridPdf = !isGridPdf
            spanCount = if (isGridImage) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6
                else 3
            } else 1
            recyclerView.layoutManager = GridLayoutManager(context, spanCount)
            pdfAdapter = PdfAdapter(list, requireActivity())
            recyclerView.adapter = pdfAdapter
            pdfAdapter.filterList(getAllFiles())
            true
        }

        setUpSearch()

        val files = getAllFiles()
        list.addAll(files)
        pdfAdapter = PdfAdapter(list,requireActivity())
        recyclerView.adapter = pdfAdapter



        progressBar.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        getAllFiles()
        //pdfAdapter = PdfAdapter(list,requireActivity())
        return view
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setupRecyclerView(view: View){
        recyclerView = view.findViewById(R.id.recycler_view)!!
        searchView = view.findViewById(R.id.search_view)
        progressBar = view.findViewById(R.id.progressBar)!!
        list = arrayListOf()
        progressBar.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        setUpSearch()

        //val binding = FragmentPdfBinding.bind(view)


        Thread {
            try {
                val files = getAllFiles()

                files.sortedWith { o1, o2 ->
                    o2.lastModified().compareTo(o1.lastModified()) }

                list.addAll(files)

                activity?.runOnUiThread {
                    pdfAdapter = this.activity?.let { PdfAdapter(list, it) }!!
                    recyclerView.adapter = pdfAdapter
                    handleUiRendering()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        /*
        binding.topAppBar.menu.findItem(R.id.sort_switch).setOnMenuItemClickListener {
            Toast.makeText(requireContext(), "Sort Files", Toast.LENGTH_SHORT).show()
            sortOrder = MediaStore.Video.Media.DISPLAY_NAME
            pdfAdapter.filterList(getAllFiles())
            true
        }

        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setOnMenuItemClickListener {
            darkModeState = !darkModeState
            //Toast.makeText(requireContext(), "Dark Mode", Toast.LENGTH_SHORT).show()
            if (darkModeState) lightMode()
            else darkMode()
            true
        }*/
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

        for(file: File in list){
            if (file.name.lowercase(Locale.getDefault()).contains(newText)){
                list1.add(file)
            }
        }
        pdfAdapter.filterList(list1)
    }

    private fun handleUiRendering() {
        progressBar.visibility = View.GONE
        if (pdfAdapter.itemCount == 0) {
            Toast.makeText(context, "No Pdf File In Phone", Toast.LENGTH_SHORT).show()
        } else {
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun getAllFiles(): List<File>{
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val selectionArgs = arrayOf("application/pdf")
        val cursor: Cursor? = context?.contentResolver?.query(uri, projection, selection, selectionArgs, sortOrder)
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
}