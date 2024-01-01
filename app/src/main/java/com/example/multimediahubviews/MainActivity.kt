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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.multimediahubviews.databinding.ActivityMainBinding
import java.io.File
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var list: List<File> = ArrayList()
    private var pdfAdapter: PdfAdapter? = null
    val context = this

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

                binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(binding.root)
                replaceFragment(ImageFragment())
                setUpSearch()

                binding.bottomNavigationView.setOnItemSelectedListener {
                    when (it.itemId) {
                        R.id.image -> replaceFragment(ImageFragment())
                        R.id.video -> replaceFragment(VideoFragment())
                        R.id.music -> replaceFragment(AudioFragment())
                        R.id.pdf -> replaceFragment(PdfFragment())
                        else -> {}
                    }
                    true
                }
            }
            else{
                try{
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.setData(Uri.parse(String.format("package: %s",applicationContext,packageName)))
                    startActivityIfNeeded(intent,101)
                }catch (e: Exception){
                    val intent = Intent()
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityIfNeeded(intent,101)
                }
            }
        }

    }

    /*val searchView = findViewById<SearchView>(R.id.search_view)
    list.let { adapter?.let { it1 -> setupsearch(this,searchView, it, it1) } }
    adapter?.let { setupsearch(this,searchView,list, it) }*/



    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.Frame_Layout,fragment)
        fragmentTransaction.commit()
    }

    private fun setUpSearch() {
        val searchView: SearchView = findViewById(R.id.search_view)
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

    /*private fun filter(newText: String) {
        val filterlist: MutableList<File> = ArrayList()
        for (item in list) {
            if (item.name.lowercase(Locale.getDefault()).contains(newText)) {
                filterlist.add(item)
            }
 `       }
        adapter?.filterlist(filterlist)
    }*/


    fun filter(newText:String){
        val list1: MutableList<File> = ArrayList()

        for(file: File in list){
            if (file.name.lowercase(Locale.ROOT).contains(newText)){
                list1.add(file)
            }
        }
        pdfAdapter?.filterlist(list1)

    }
}


