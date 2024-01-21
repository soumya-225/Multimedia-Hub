package com.example.multimediahubviews

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.multimediahubviews.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


var darkModeState: Boolean = true
private var isLaunched = true

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector
    val context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                binding = ActivityMainBinding.inflate(layoutInflater)
                setSupportActionBar(binding.topAppBar)
                setContentView(binding.root)
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
        }

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
                Log.d("TagHere", "Image Frag")
                replaceFragment(ImageFragment())
                binding.nowPlaying.visibility = View.GONE
            }

            R.id.video -> {
                Log.d("TagHere", "Video Frag")
                replaceFragment(VideoFragment())
                binding.nowPlaying.visibility = View.GONE
            }

            R.id.music -> {
                Log.d("TagHere", "Music Frag")
                replaceFragment(AudioFragment())
                binding.nowPlaying.visibility = View.VISIBLE
            }

            R.id.pdf -> {
                Log.d("TagHere", "PDF Frag")
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
