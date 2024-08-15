package com.example.nits

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultImageView: ImageView
    private lateinit var serpApiService: SerpApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        searchEditText = findViewById(R.id.search_edit_text)
        searchButton = findViewById(R.id.search_button)
        resultImageView = findViewById(R.id.result_image_view)

        // Initialize Retrofit
        serpApiService = RetrofitClient.getClient("https://serpapi.com/").create(SerpApiService::class.java)

        searchButton.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
            return
        }

        // Make API call
        serpApiService.getImages("3db35132b5ece1f427ad1bb3a42e54cbd99ec920db4979bd6544fae2f3e44c8c", query, "isch")
            .enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            val results = responseBody.images_results
                            if (results.isNotEmpty()) {
                                val imageUrl = results[0].image_url
                                if (imageUrl != null && imageUrl.isNotEmpty()) {
                                    Log.d("Image URL", imageUrl) // Log the image URL
                                    // Fetch image and set to ImageView
                                    fetchImage(imageUrl)
                                } else {
                                    Toast.makeText(this@MainActivity, "No image URL found", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@MainActivity, "No results found", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Empty response body", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("API Error", "Response code: ${response.code()}") // Log response code
                        Toast.makeText(this@MainActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    Log.e("API Failure", "Error: ${t.message}") // Log failure message
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchImage(imageUrl: String) {
        Thread {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.inputStream.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    Handler(Looper.getMainLooper()).post {
                        resultImageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                Log.e("Image Fetch Error", "Error: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
