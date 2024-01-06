package com.example.multimediahubviews


import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Locale

class PdfFragment() : Fragment() {

    private lateinit var recyclerView: RecyclerView
    lateinit var pdfAdapter: PdfAdapter
    private lateinit var list: List<File>
    private lateinit var progressBar: ProgressBar

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pdf, container, false)
        setuprv(view)
        return view
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setuprv(view: View){
        recyclerView = view.findViewById(R.id.recycler_view)!!
        progressBar = view.findViewById(R.id.progressBar)!!
        list = arrayListOf()
        progressBar.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        Thread {
            try {
                val files = getAllFiles()

                files.sortedWith { o1, o2 ->
                    o2.lastModified().compareTo(o1.lastModified()) }

                (list as ArrayList<File>).addAll(files)

                activity?.runOnUiThread {
                    pdfAdapter = this.activity?.let { PdfAdapter(list, it) }!!
                    recyclerView.adapter = pdfAdapter
                    handleUiRendering()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()



    }
    /*private fun setUpSearch() {
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
    }*/
    fun filter(newText: String) {
        val list1: MutableList<File> = ArrayList()

        for(file: File in list){
            if (file.name.lowercase(Locale.getDefault()).contains(newText)){
                list1.add(file)
            }
        }
        pdfAdapter.filterlist(list1)

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
        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
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