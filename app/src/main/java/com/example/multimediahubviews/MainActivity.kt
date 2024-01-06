package com.example.multimediahubviews

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.multimediahubviews.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var list: List<File> = ArrayList()
    val context = this

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(binding.root)
                replaceFragment(ImageFragment())

                binding.bottomNavigationView.setOnItemSelectedListener {
                    when (it.itemId) {
                        R.id.image -> replaceFragment(ImageFragment())
                        R.id.video -> replaceFragment(VideoFragment())
                        R.id.music -> replaceFragment(AudioFragment())
                        R.id.pdf -> {
                            val pdfFragment = PdfFragment()
                            replaceFragment(pdfFragment)
                            setUpSearch(pdfFragment)
                        }
                        else -> {}
                    }
                    true
                }
            }
            else{
                requestStoragePermissions()
                /*try{
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.setData(Uri.parse(String.format("package: %s",applicationContext,packageName)))
                    startActivityIfNeeded(intent,101)
                }catch (e: Exception){
                    val intent = Intent()
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityIfNeeded(intent,101)
                }*/

            }
        }

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.Frame_Layout,fragment)
        fragmentTransaction.commit()
    }

    private fun setUpSearch(pdfFragment: PdfFragment) {
        val searchView: SearchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    pdfFragment.filter(newText)
                } else {
                    Toast.makeText(context, "No File Found", Toast.LENGTH_SHORT).show()
                }
                return false
            }
        })
    }

    fun filter(adapter: PdfAdapter, newText:String){
        val list1 = mutableListOf<File>()

        for(file in list) {
            if (file.name.lowercase(Locale.getDefault()).contains(newText)){
                list1.add(file)
            }
        }
        adapter.filterlist(list1)
    }

    private fun requestStoragePermissions() {
        AlertDialog.Builder(this)
            .setTitle("Allow Access to Internal Storage")
            .setMessage("Internal Stroage Permission is required to access the files")
            .setPositiveButton("OK") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                Toast.makeText(this, "The application cannot work without storage permission", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}

fun parseFileLength(length: Long): String {
    val units = listOf("B", "KB", "MB", "GB")
    var size: Double = length.toDouble()
    var index = 0
    while (size >= 1024 && index < units.size - 1) {
        size /= 1024
        ++index
    }
    val formattedString = String.format("%.2f", size)
    return "$formattedString ${units[index]}"
}

fun convertEpochToDate(epochTime: Long): String {
    return try {
        val date = Date(epochTime)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        "Invalid Date"
    }
}

