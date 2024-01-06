package com.example.multimediahubviews

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide


class ImageFullScreenActivity : AppCompatActivity() {
    private lateinit var fullImage: ImageView
    private lateinit var image: String
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor: Float = 1.0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_full_screen)

        fullImage = findViewById(R.id.fullImage)

        val intent: Intent = intent
        image = intent.getStringExtra("parseData")!!
        Glide.with(this).load(image).into(fullImage)

        scaleGestureDetector = ScaleGestureDetector(
            this,
            ScaleListener(fullImage, scaleFactor)
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private class ScaleListener(private val fullImage: ImageView, var scaleFactor: Float) :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = 0.1f.coerceAtLeast(scaleFactor.coerceAtMost(10.0f))
            fullImage.scaleX = scaleFactor
            fullImage.scaleY = scaleFactor
            return true
        }
    }

}