package com.example.multimediahubviews

import android.net.Uri


class ImageModel(private var path: Uri) {

    fun getPath(): Uri {
        return path
    }

    fun setPath(path: Uri) {
        this.path = path
    }
}