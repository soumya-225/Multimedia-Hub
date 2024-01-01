package com.example.multimediahubviews

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_full_screen)

        fullImage = findViewById(R.id.fullImage)

        val intent = intent
        image = intent.getStringExtra("parseData")!!
        //Glide.with(this).load(image).into(fullImage)

        val image = intent.getStringExtra("parseData")
        image?.let {
            val imageView = fullImage
            Glide.with(this).load(image).into(imageView)
        }

        scaleGestureDetector = ScaleGestureDetector(
            this,
            ScaleListener()
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event!!)
        return true
    }


    private class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = 1.0f
            scaleFactor *= detector.scaleFactor
            scaleFactor = 0.1f.coerceAtLeast(scaleFactor.coerceAtMost(10.0f))
            //fullImage.scaleX = scaleFactor
            //fullImage.scaleY = scaleFactor
            return true
        }
    }

}