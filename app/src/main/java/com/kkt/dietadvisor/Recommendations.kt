package com.kkt.dietadvisor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

class Recommendations : AppCompatActivity() {

    private lateinit var accessToken: String

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
//            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Change response timeout to 30 seconds
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recommendations)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
        // Retrieve access token from the intent extras
        accessToken = intent.getStringExtra("ACCESS_TOKEN")
            ?: throw IllegalArgumentException("Access token is missing from the intent")
        Log.d(TAG, "onCreate: Access Token Retrieved: $accessToken")

        val recommendationRequest = findViewById<Button>(R.id.request_recommendations)
        val recommendationText = findViewById<TextView>(R.id.recommendations_text)
        val recommendationSV = findViewById<ScrollView>(R.id.recommendations_scroll_view)

        // DUMMY DATA
//        recommendationText.text = resources.getString(R.string.random_text)

        // Get recommendation from the backend
        getUserInfo(accessToken) { userExists, userInfo ->
            if (userExists) {
                userInfo?.let {
                    // Make the request to the /recommendation endpoint to get a generated response
                    dispatchRecommendationRequest(userInfo) { hasText, messageText ->
                        if (hasText) {
                            messageText?.let { recommendationText.text = it }
                        } else {
                            Log.e(TAG, "onCreate: Error: Recommendation could not be generated", )
                        }
                    }
                } ?: run {
                    Log.e(TAG, "getUserInfo: Failure! User does not exist")
                }
            }
        }

        // Show the message text on the screen
        recommendationRequest.setOnClickListener {
            recommendationSV.visibility = View.VISIBLE
        }

        /* UI Navigation Init*/
        val homepageButton = findViewById<FrameLayout>(R.id.home_button)
        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)
        val profileButton = findViewById<FrameLayout>(R.id.profile_button)
        homepageButton.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra("ACCESS_TOKEN", accessToken) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        trackingButton.setOnClickListener {
            val intent = Intent(this, Tracking::class.java)
            intent.putExtra("ACCESS_TOKEN", accessToken) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            intent.putExtra("ACCESS_TOKEN", accessToken) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun dispatchRecommendationRequest(userInfo: String, onResult: (Boolean, String?) -> Unit) {
        val url = getString(R.string.DIET_ADVISOR_RECOMMENDATION_ENDPOINT_URL)

        // Create the request body with the userInfo JSON string
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody: RequestBody = userInfo.toRequestBody(mediaType)

        // Build the POST request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("accept", "application/json")
            .build()

        // Perform the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RecommendationRequest", "Request failed: ${e.message}")
                e.printStackTrace()
                onResult(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        // Handle successful response, e.g., show a toast or update UI
                        Log.d("RecommendationRequest", "Response: $responseBody")
                        println(responseBody)
                    }
                    onResult(true, responseBody)
                } else {
                    Log.e("RecommendationRequest", "Request failed with response code: ${response.code}")
                    onResult(false, null)
                }
            }
        })
    }

    private fun getUserInfo(accessToken: String, onResult: (Boolean, String?) -> Unit) {
        val url = getString(R.string.DIET_ADVISOR_USER_ENDPOINT_URL)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get() // GET request to retrieve user data
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onResult(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle the response
                    val responseBody = response.body?.string()
                    println(responseBody)
                    // Here, you might want to parse the JSON response to a UserData object using Gson
                    Log.d(TAG, "onResponse: Success! Retrieved user information!")
                    onResult(true, responseBody)
                } else {
                    // Handle the error
                    println("Request failed: ${response.message}")
                    Log.d(TAG, "onResponse: Failure! No User to Retrieve!")
                    onResult(false, null)
                }
            }
        })
    }
}