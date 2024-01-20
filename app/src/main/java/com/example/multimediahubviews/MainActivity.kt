package com.example.multimediahubviews

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.multimediahubviews.databinding.ActivityMainBinding
import com.example.multimediahubviews.databinding.FragmentImageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

var darkModeState: Boolean = true

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val context = this

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                binding = ActivityMainBinding.inflate(layoutInflater)
                setSupportActionBar(binding.topAppBar)
                setContentView(binding.root)
                replaceFragment(ImageFragment())
                binding.bottomNavigationView.setOnItemSelectedListener {
                    when (it.itemId) {
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
                    true
                }
            } else {
                requestStoragePermissions()
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) binding.bottomNavigationView.visibility =
            View.GONE
        else binding.bottomNavigationView.visibility = View.VISIBLE

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!AudioPlayer.isPlaying && AudioPlayer.musicService != null) {
            AudioPlayer.musicService!!.stopForeground(true)
            AudioPlayer.musicService!!.mediaPlayer!!.release()
            AudioPlayer.musicService = null
            exitProcess(1)
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

fun darkModeButton(binding: FragmentImageBinding) {
    darkModeState = !darkModeState
    if (darkModeState) {
        lightMode()
        binding.topAppBar.menu.findItem(R.id.dark_mode_switch)
            .setIcon(R.drawable.baseline_dark_mode_24)
    } else {
        darkMode()
        binding.topAppBar.menu.findItem(R.id.dark_mode_switch)
            .setIcon(R.drawable.baseline_light_mode_24)
    }

}