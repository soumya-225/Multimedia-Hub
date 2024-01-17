package com.example.multimediahubviews

import android.net.Uri
import java.io.Serializable

class ImageModel(
    var title: String,
    var path: Uri,
    var size: String,
    var lastModified: String
)

data class VideoModel(
    val id: String,
    val title: String,
    val duration: Long = 0,
    val folderName: String,
    val size: String,
    val path: String,
    val artUri: Uri,
    val lastModified: String
)

data class AudioModel(
    var path: String,
    var title: String,
    var duration: String,
    var size: String,
    var lastModified: String,
    val artUri: Uri
) : Serializable