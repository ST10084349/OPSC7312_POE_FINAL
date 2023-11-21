package com.gigglegang.openedmaps

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar

class Splash : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val loadingBar = findViewById<ProgressBar>(R.id.loading_bar)
        val totalTime = 5000 // 5 seconds in milliseconds

        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = totalTime.toLong()
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            loadingBar.progress = progress
        }
        animator.start()

        // Delay navigation to the main activity after 5 seconds (or the duration of the splash screen animation).
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMainActivity()
        }, totalTime.toLong())

    }

    private fun navigateToMainActivity() {
        // Create an Intent to start the main activity.
        val intent = Intent(this,Login::class.java)
        startActivity(intent)
        finish() // Close the splash screen activity.
    }

}