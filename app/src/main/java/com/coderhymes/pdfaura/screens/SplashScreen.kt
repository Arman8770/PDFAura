package com.coderhymes.pdfaura.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.coderhymes.pdfaura.R

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private val splashTime = 2000
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var loadingLine: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Set the status bar color to white

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)

        // Make the icons on the status bar dark for better visibility on a light background
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // For versions before M, use ViewCompat to handle compatibility
            ViewCompat.getWindowInsetsController(window.decorView)?.isAppearanceLightStatusBars = true
        }

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        imageView = findViewById(R.id.splashScreen)
        textView = findViewById(R.id.splashAura)
        loadingLine = findViewById(R.id.loadingLine)

        // Apply the zoom-out animation to the ImageView and TextView
        imageView.animate()
            .scaleX(1.25f)
            .scaleY(1.25f)
            .setDuration(2000)
            .start()

        textView.alpha = 0f
        textView.scaleX = 0.5f
        textView.scaleY = 0.5f

        textView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(2000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Using a Handler to delay the screen transition
                val handler = Handler()
                handler.postDelayed({
                    // Start your main activity here
                    val intent = Intent(this, HomeScreen::class.java)
                    startActivity(intent)
                    finish()
                }, splashTime.toLong())
            }
            .start()

        loadingLine.scaleX = 0f
        loadingLine.animate()
            .scaleX(1f)
            .setDuration(2000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
}
