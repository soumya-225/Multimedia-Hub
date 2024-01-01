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

class PdfFragment() : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pdfAdapter: PdfAdapter
    private lateinit var list: List<File>
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pdf, container, false)
        val context = activity?.applicationContext

        setuprv(view)

        return view
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setuprv(view: View){
        recyclerView = view.findViewById(R.id.recycler_view)!!
        progressBar = view.findViewById(R.id.progressBar)!!
        list = ArrayList()
        progressBar.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        //setupsearch()

        Thread {
            try {
                val files = getAllFiles()

                files.sortedWith { o1, o2 ->
                    o2.lastModified().compareTo(o1.lastModified()) }

                (list as ArrayList<File>).addAll(files)

                activity?.runOnUiThread {
                    pdfAdapter = this.activity?.let { PdfAdapter(it.applicationContext, list, it) }!!
                    recyclerView.adapter = pdfAdapter
                    handleUiRendering()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
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
        val cursor: Cursor? = context?.contentResolver?.query(uri, projection, selection, selectionArgs, null)
        val list = ArrayList<File>()
        val pdfPathIndex = cursor?.getColumnIndex(MediaStore.Files.FileColumns.DATA)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (pdfPathIndex != -1) {
                    val pdfPath = pdfPathIndex?.let { cursor.getString(it) }
                    val pdfFile = pdfPath?.let { File(it) }
                    if (pdfFile != null) {
                        if (pdfFile.exists() && pdfFile.isFile) {
                            list.add(pdfFile)
                        }
                    }
                }
            }
        }
        cursor?.close()
        return list
    }
}