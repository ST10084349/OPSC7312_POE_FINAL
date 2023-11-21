package com.gigglegang.openedmaps

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class Bird : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)

        //variable
        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()
        // specify the url
        val url = "https://ebird.org/region/saf"
        webView.loadUrl(url)
    }
}