package com.example.multimediahubviews

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlin.math.max
import kotlin.math.min


class ImageViewerActivity : AppCompatActivity() {
    private lateinit var fullImage: ImageView
    private lateinit var image: String
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var back: ImageView
    private lateinit var fileName: TextView
    private lateinit var title: String
    private lateinit var scaleListener: ScaleListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_full_screen)

        fullImage = findViewById(R.id.fullImage)
        fileName = findViewById(R.id.image_name)
        back = findViewById(R.id.backBtn)
        val intent: Intent = intent
        image = intent.getStringExtra("parseData")!!
        title = intent.getStringExtra("fileName")!!
        fileName.text = title

        scaleListener = ScaleListener(fullImage)
        scaleGestureDetector = ScaleGestureDetector(this, scaleListener)

        Glide.with(this).load(image).into(fullImage)
        back()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        scaleListener.onTouchEvent(event)
        return true
    }

    private class ScaleListener(private val fullImage: ImageView) :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var lastTouchX: Float = 0f
        private var lastTouchY: Float = 0f
        private var mode: Int = NONE

        companion object {
            private const val NONE = 0
            private const val DRAG = 1
            private const val ZOOM = 2
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (scaleFactor > 0) {
                fullImage.scaleX *= scaleFactor
                fullImage.scaleY *= scaleFactor
            }

            adjustTranslationToBounds()
            return true
        }

        fun onTouchEvent(event: MotionEvent) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                    mode = DRAG
                }

                MotionEvent.ACTION_MOVE -> {
                    if (event.action == MotionEvent.ACTION_MOVE && mode == DRAG) {
                            val dx = event.x - lastTouchX
                            val dy = event.y - lastTouchY
                            fullImage.translationX += dx
                            fullImage.translationY += dy
                            lastTouchX = event.x
                            lastTouchY = event.y
                        adjustTranslationToBounds()
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }
            }
        }

        private fun adjustTranslationToBounds() {
            val imageViewWidth = fullImage.width
            val imageViewHeight = fullImage.height
            val drawable = fullImage.drawable ?: return
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            val maxTransX = max(0f, (drawableWidth * fullImage.scaleX - imageViewWidth) / 2)
            val maxTransY = max(0f, (drawableHeight * fullImage.scaleY - imageViewHeight) / 2)

            fullImage.translationX = min(maxTransX, max(-maxTransX, fullImage.translationX))
            fullImage.translationY = min(maxTransY, max(-maxTransY, fullImage.translationY))
        }
    }

    private fun back() {
        back.setOnClickListener {finish()}
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        super.onConfigurationChanged(newConfig)
    }
}