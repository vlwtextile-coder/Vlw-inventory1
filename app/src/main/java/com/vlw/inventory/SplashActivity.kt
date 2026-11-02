package com.vlw.inventory

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.view.View>(R.id.logo)
        val brand = findViewById<android.view.View>(R.id.brand)

        logo.alpha = 0f
        logo.scaleX = 0.9f
        logo.scaleY = 0.9f
        brand.alpha = 0f

        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).start()
        brand.animate().alpha(1f).setStartDelay(400).setDuration(600).withEndAction {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, 500)
        }.start()
    }
}
