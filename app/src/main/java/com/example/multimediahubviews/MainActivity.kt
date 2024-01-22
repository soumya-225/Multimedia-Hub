package com.example.multimediahubviews

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.multimediahubviews.VideoPlayerActivity.Companion.position
import com.example.multimediahubviews.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


var darkModeState: Boolean = true
private var isLaunched = true

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val context = this
    private lateinit var pagerMain: ViewPager2
    private var fragmentArrList: ArrayList<Fragment> = ArrayList()
    private lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.topAppBar)
        setContentView(binding.root)

        bottomNav = findViewById(R.id.bottomNavigationView)

        pagerMain = findViewById(R.id.pagerMain)
        fragmentArrList.add(ImageFragment())
        fragmentArrList.add(VideoFragment())
        fragmentArrList.add(AudioFragment())
        fragmentArrList.add(PdfFragment())

        val adapterViewPager: AdapterViewPager = AdapterViewPager(this,fragmentArrList)
        pagerMain.adapter = adapterViewPager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                pagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        when (position){
                            0 -> bottomNav.selectedItemId = R.id.image
                            1 -> bottomNav.selectedItemId = R.id.video
                            2 -> bottomNav.selectedItemId = R.id.music
                            3 -> bottomNav.selectedItemId = R.id.pdf
                            else -> {}
                        }

                        super.onPageSelected(position)
                    }
                })
                bottomNav.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.image -> {
                            pagerMain.currentItem = 0
                            binding.nowPlaying.visibility = View.GONE
                        }

                        R.id.video -> {
                            pagerMain.currentItem = 1
                            binding.nowPlaying.visibility = View.GONE
                        }
                        R.id.music -> {
                            pagerMain.currentItem = 2
                            binding.nowPlaying.visibility = View.VISIBLE
                        }
                        R.id.pdf -> {
                            pagerMain.currentItem = 3
                            binding.nowPlaying.visibility = View.GONE
                        }
                        else -> {}
                    }
                    true
                }
            }else{
                requestStoragePermissions()
            }
        }




        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {


            if (isLaunched) {
                replaceFragment(ImageFragment())
                isLaunched = false
            }
            binding.bottomNavigationView.setOnItemSelectedListener {
                setFragment(it.itemId)
                true
            }
        } else {
            requestStoragePermissions()
        }
    }*/

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.bottomNavigationView.visibility = View.GONE
        } else {
            binding.bottomNavigationView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!AudioPlayer.isPlaying && AudioPlayer.audioService != null) {
            AudioPlayer.audioService!!.stopForeground(true)
            AudioPlayer.audioService!!.mediaPlayer!!.release()
            AudioPlayer.audioService = null
            exitProcess(1)
        }
    }

    private fun setFragment(itemId: Int) {
        when (itemId) {
            R.id.image -> {
                replaceFragment(ImageFragment())
                binding.nowPlaying.visibility = View.GONE
            }

            R.id.video -> {
                replaceFragment(VideoFragment())
                binding.nowPlaying.visibility = View.GONE
            }

            R.id.music -> {
                replaceFragment(AudioFragment())
                binding.nowPlaying.visibility = View.VISIBLE
            }

            R.id.pdf -> {
                replaceFragment(PdfFragment())
                binding.nowPlaying.visibility = View.GONE
            }

            else -> {}
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.Frame_Layout, fragment)
        fragmentTransaction.commit()
    }

    private fun requestStoragePermissions() {
        AlertDialog.Builder(this)
            .setTitle("Allow Access to Internal Storage")
            .setMessage("Internal Storage Permission is required to access the files")
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
                Toast.makeText(
                    this,
                    "The application cannot work without storage permission",
                    Toast.LENGTH_SHORT
                ).show()
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

fun convertToMMSS(duration: String): String {
    val millis = duration.toLong()
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
    )
}


fun darkMode() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}

fun lightMode() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
}
