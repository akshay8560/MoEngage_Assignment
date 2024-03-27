package com.example.moengageassignment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.moengageassignment.R
import java.net.URLDecoder

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val URL_EXTRA = "url"
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // Initialize WebView and ProgressBar
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar2)

        // Get the URL from the intent
        val encodedUrl = intent.getStringExtra(URL_EXTRA)
        val url = URLDecoder.decode(encodedUrl, "UTF-8")

        // Open the WebView with the given URL
        if (!url.isNullOrBlank()) {
            openWebView(url)
        } else {
            // Finish the activity if URL is null or blank
            finish()
        }
    }

    /**
     * Opens the WebView with the given URL.
     * @param url The URL to be loaded in the WebView.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebView(url: String) {
        try {
            // Convert HTTP to HTTPS if necessary
            var newUrl = url
            val httpId = "http://"
            val httpsId = "https://"
            if (url.startsWith(httpId)) {
                newUrl = url.replace(httpId, httpsId)
            }

            // Configure WebView settings
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

            // Load the URL in the WebView
            webView.loadUrl(newUrl)

            // Set a WebViewClient to handle page loading events
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Hide the progress bar when page loading is finished
                    progressBar.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            // Handle input exceptions
            e.printStackTrace()
        }
    }


}
