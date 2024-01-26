package com.example.multimediahubviews

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
    private var dType: Boolean = false
    private lateinit var darkMode: ImageView

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        initVar()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initVar() {
        pdfView = findViewById(R.id.pdfView)
        toolbar = findViewById(R.id.toolbar)
        back = findViewById(R.id.back)
        fileName = findViewById(R.id.file_name)
        scroll = findViewById(R.id.scrollButton)
        darkMode = findViewById(R.id.dark_pdf)
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
            .fromUri(if (intent.action == Intent.ACTION_VIEW) intent?.data else Uri.fromFile(File(path)))
            .nightMode(false)
            .swipeHorizontal(sType)
            .defaultPage(0)
            .enableAnnotationRendering(true)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(2)
            .nightMode(dType)
            .load()
    }

    private fun darkPdf() {
        darkMode.setOnClickListener {
            dType = !dType
            if (dType) darkMode.setImageResource(R.drawable.dark_mode_icon_dark)
            else darkMode.setImageResource(R.drawable.night_mode_icon)
            showPdf()
        }
    }

    private fun back() {
        back.setOnClickListener { finish() }
    }

    private fun scrollType() {
        scroll.setOnClickListener {
            sType = !sType
            if (sType) scroll.setImageResource(R.drawable.swipe_vertical_icon)
            else scroll.setImageResource(R.drawable.swipe_horizontal_icon)
            showPdf()
        }
    }

    private fun getIntentData() {
        val nameExtra = intent.getStringExtra("name")
        val pathExtra = intent.getStringExtra("path")

        if (intent.data?.scheme.contentEquals("content")) {
            val cursor = contentResolver.query(
                intent.data!!,
                arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.let {
                it.moveToFirst()
                val fileNamePdf = it.getString(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
                fileName.text = fileNamePdf
                cursor.close()
            }
        }

        if (nameExtra != null && pathExtra != null) {
            fileName.text = nameExtra
            path = pathExtra
        } else if (intent?.action != Intent.ACTION_VIEW) {
            Toast.makeText(this, "Error: Data not available", Toast.LENGTH_SHORT).show()
            finish()
        }

        back()
        scrollType()
        darkPdf()
        fullscreen()
        showPdf()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        super.onConfigurationChanged(newConfig)
    }

}