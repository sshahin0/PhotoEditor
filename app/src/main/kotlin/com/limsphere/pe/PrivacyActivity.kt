package com.limsphere.pe

import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class PrivacyActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var headerTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        
        // Hide the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        findViewById<WebView>(R.id.webview).apply {
            settings.javaScriptEnabled = true
            loadUrl("file:///android_asset/privacy_policy.html")
        }

        headerTxt = findViewById(R.id.headerTxt)
        
        backBtn = findViewById<ImageView>(R.id.backBtn).apply {
            setOnClickListener { onBackPressed() }
        }
    }
} 