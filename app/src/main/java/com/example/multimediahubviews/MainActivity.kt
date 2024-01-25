package com.example.multimediahubviews

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.multimediahubviews.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


var notDarkModeState: Boolean = true

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val context = this
    private lateinit var pagerMain: ViewPager2
    private var fragmentArrList: ArrayList<Fragment> = ArrayList()
    lateinit var bottomNav: BottomNavigationView
    private var isPermissionGranted: Boolean = false
    private var isPermissionGrantedOnResume: Boolean = false

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notDarkModeState = !resources.configuration.isNightModeActive

        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.topAppBar)
        setContentView(binding.root)

        bottomNav = findViewById(R.id.bottomNavigationView)
        pagerMain = findViewById(R.id.pagerMain)
        isPermissionGranted = Environment.isExternalStorageManager()

        fragmentArrList.add(ImageFragment())
        fragmentArrList.add(VideoFragment())
        fragmentArrList.add(AudioFragment())
        fragmentArrList.add(PdfFragment())

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            bottomNav.visibility =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                    (keypadHeight > screenHeight * 0.15)
                )
                    View.GONE
                else
                    View.VISIBLE
        }

        val adapterViewPager = AdapterViewPager(this, fragmentArrList)
        pagerMain.adapter = adapterViewPager



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isPermissionGranted) {
                onPermissionGranted()
            } else {
                requestStoragePermissions()
            }
        }
    }

    private fun onPermissionGranted(){
        pagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
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
                Toast.makeText(this, "The application cannot work without storage permission", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        isPermissionGrantedOnResume = Environment.isExternalStorageManager()
        if (isPermissionGranted != isPermissionGrantedOnResume)
            recreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNav.visibility = View.GONE
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            bottomNav.visibility = View.VISIBLE
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        super.onConfigurationChanged(newConfig)
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