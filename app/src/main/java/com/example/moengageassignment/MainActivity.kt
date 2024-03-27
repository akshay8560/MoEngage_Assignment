package com.example.moengageassignment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var articleListAdapter: ArticleListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ContentLoadingProgressBar
    private var articles: List<ArticleData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Subscribe to the "news" topic for Firebase Messaging
        FirebaseMessaging.getInstance().subscribeToTopic("news")

        // Initialize RecyclerView and ProgressBar
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Setup RecyclerView
        setupRecyclerView()

        // Load data from API
        loadData()
    }

    // Setup RecyclerView
    private fun setupRecyclerView() {
        articleListAdapter = ArticleListAdapter { url ->
            openInBrowser(url)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = articleListAdapter
    }

    // Load data from API
    private fun loadData() {
        if (isNetworkAvailable()) {
            progressBar.show()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val apiUrl = "https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json"
                    val url = URL(apiUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000 // Set connection timeout
                    connection.readTimeout = 15000 // Set read timeout

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.use { it.readText() }
                        reader.close()

                        val responseObject = Gson().fromJson(response, ApiResponse::class.java)
                        articles = responseObject.articles
                        updateArticleList(articles)
                    } else {
                        Log.e(TAG, "Error: Unable to fetch data from the API. Response code: $responseCode")
                        showErrorToast()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during API call", e)
                    showErrorToast()
                } finally {
                    progressBar.hide()
                }
            }
        } else {
            showNoInternetToast()
        }
    }

    // Update RecyclerView with new data
    private fun updateArticleList(articleList: List<ArticleData>) {
        runOnUiThread {
            articleListAdapter.submitList(articleList)
        }
    }

    // Show error toast message
    private fun showErrorToast() {
        runOnUiThread {
            Toast.makeText(this, "Error loading data. Please try again later.", Toast.LENGTH_SHORT).show()
        }
    }

    // Show no internet connection toast message
    private fun showNoInternetToast() {
        runOnUiThread {
            Toast.makeText(this, "No internet connection. Please connect to the internet and try again.", Toast.LENGTH_SHORT).show()
        }
    }

    // Check if network is available
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    // Open article URL in browser
    private fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // Inflate menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    // Handle menu item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadData()
                true
            }
            R.id.action_sort_old_to_new -> {
                sortByDateOldToNew()
                true
            }
            R.id.action_sort_new_to_old -> {
                sortByDateNewToOld()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Sort articles by date - old to new
    private fun sortByDateOldToNew() {
        articles = articles.sortedBy { it.publishedAt }
        updateArticleList(articles)
        scrollToTop()
    }

    // Sort articles by date - new to old
    private fun sortByDateNewToOld() {
        articles = articles.sortedByDescending { it.publishedAt }
        updateArticleList(articles)
        scrollToTop()
    }

    // Scroll RecyclerView to top
    private fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    // Handle back button press
    override fun onBackPressed() {
        // Show exit confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Exit Confirmation")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { dialog, which ->
                super.onBackPressed() // Exit activity if user confirms
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss() // Dismiss dialog if user cancels
            }
            .show()
    }
}
