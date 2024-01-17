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
import android.app.Dialog
import android.os.Binder
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.example.multimediahubviews.databinding.FragmentImageBinding

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
                requestStoragePermissions()
            }
        }

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.Frame_Layout,fragment)
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
                Toast.makeText(this, "The application cannot work without storage permission", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}

class FragmentDialogBox : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction.
            val builder = AlertDialog.Builder(requireContext())
            builder
                .setTitle("Sort By:")
                .setPositiveButton("OK") { dialog, which ->
                    // START THE GAME!
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    // User cancelled the dialog.
                }
                .setSingleChoiceItems(
                    arrayOf("Name","Date Modified","Size"), 0
                ){
                    dialog, which ->
                }
            // Create the AlertDialog object and return it.
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
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

fun darkMode(){
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}
fun lightMode(){
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
}

fun darkModeButton(binding: FragmentImageBinding){
    darkModeState = !darkModeState
    if (darkModeState) {
        lightMode()
        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.baseline_dark_mode_24)
    }
    else{
        darkMode()
        binding.topAppBar.menu.findItem(R.id.dark_mode_switch).setIcon(R.drawable.baseline_light_mode_24)
    }

}