package com.example.multimediahubviews

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File


class PdfViewerActivity : AppCompatActivity() {
    private lateinit var pdfView: PDFView
    private lateinit var back: ImageView
    lateinit var name: String
    lateinit var path: String
    private var isHide = false
    private lateinit var toolbar: Toolbar
    private lateinit var fileName: TextView
    private lateinit var scroll: ImageView
    private var sType: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        initVar()
    }

    private fun initVar(){
        pdfView = findViewById(R.id.pdfView)
        toolbar = findViewById(R.id.toolbar)
        back = findViewById(R.id.back)
        fileName = findViewById(R.id.file_name)
        scroll = findViewById(R.id.scrollButton)

        getIntentData()
    }

    private fun fullscreen() {
        pdfView.setOnClickListener {
            if (isHide) {
                findViewById<View>(R.id.toolbar).visibility =
                    View.VISIBLE
                isHide = false
            } else {
                findViewById<View>(R.id.toolbar).visibility =
                    View.GONE
                isHide = true
            }
        }
    }

    private fun showPdf() {
        pdfView
            .fromFile(File(path))
            .nightMode(false)
            .swipeHorizontal(sType)
            .defaultPage(0)
            .enableAnnotationRendering(true)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(2)
            .load()
    }

    private fun back() {
        back.setOnClickListener {finish()}
    }

    private fun scrollType(){
        scroll.setOnClickListener {
            sType = !sType
            if (sType) scroll.setImageResource(R.drawable.baseline_swipe_vertical_24)
            else scroll.setImageResource(R.drawable.baseline_swipe_24)
            showPdf()
        }
    }

    private fun getIntentData() {
        val nameExtra = intent.getStringExtra("name")
        val pathExtra = intent.getStringExtra("path")

        if (nameExtra != null && pathExtra != null) {
            fileName.text = nameExtra
            path = pathExtra
        }
        else {
            Toast.makeText(this, "Error: Data not available", Toast.LENGTH_SHORT).show()
            finish()
        }

        back()
        scrollType()
        fullscreen()
        showPdf()
    }

}