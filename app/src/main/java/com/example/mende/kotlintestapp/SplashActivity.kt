package com.example.mende.kotlintestapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.mende.kotlintestapp.services.OnClearFromRecentService

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * TODO: The icon for app is displaying oddly, foreground behind background maybe?
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(Intent(baseContext, OnClearFromRecentService::class.java))

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
