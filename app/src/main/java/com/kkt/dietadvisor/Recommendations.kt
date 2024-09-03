package com.kkt.dietadvisor

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.compose.Visibility
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import io.noties.prism4j.annotations.PrismBundle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class Recommendations : AppCompatActivity() {

    private lateinit var accessToken: String
    private var renderedText: Spanned? = null
    private val MAX_TYPING_DURATION = 10_000L

    private lateinit var recommendationRequest: Button
    private lateinit var recommendationText: TextView
    private lateinit var recommendationSV: ScrollView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var successLayout: FrameLayout
    private lateinit var failureLayout: FrameLayout

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
//            .addInterceptor(logging)
            .connectTimeout(1, TimeUnit.MINUTES) // Change response timeout to 1 seconds
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
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

        // Init views
        recommendationRequest = findViewById(R.id.request_recommendations)
        recommendationText = findViewById(R.id.recommendations_text)
        recommendationSV = findViewById(R.id.recommendations_scroll_view)
        loadingIndicator = findViewById(R.id.loading_indicator)
        successLayout = findViewById(R.id.success_layout)
        failureLayout = findViewById(R.id.failure_layout)

        // Get recommendation from the /recommendation endpoint on the backend
        fetchRecommendations()

        /* UI Navigation Init */
        initNavigationElements()
    }

    private fun fetchRecommendations() {
        runOnUiThread {
            setSuccessAndFailureVisibility(View.GONE, View.GONE) // Hide success/failure layouts
            loadingIndicator.visibility = View.VISIBLE // Hide loading indicator
            recommendationRequest.isEnabled = false // Stop user from making more requests
            recommendationRequest.text = getString(R.string.loading_text)
        }

        // Get recommendation from the /recommendation endpoint on the backend
        getUserInfo(accessToken) { userExists, userInfo ->
            userInfo.takeIf { userExists }?.let {
                dispatchRecommendationRequest(it) { hasText, responseData ->
                    responseData.takeIf { hasText }?.let { data ->
                        runOnUiThread {
                            processRecommendationText(data)

                            // Re-enable "Get Recommendations" button
                            loadingIndicator.visibility = View.GONE
                            recommendationRequest.isEnabled = true
                            recommendationRequest.text = getString(R.string.get_recommendations_text)

                            onRecommendationRequestSuccess() // Handle successful response
                        }
                    } ?: runOnUiThread { onRecommendationRequestFailure() } // Handle failure response
                }
            } ?: Log.e(TAG, "getUserInfo: Failure! User does not exist")
        }
    }

    private fun setSuccessAndFailureVisibility(successVisibility: Int, failureVisibility: Int) {
        successLayout.visibility = successVisibility
        failureLayout.visibility = failureVisibility
    }

    private fun onRecommendationRequestSuccess() {
        // Show the success layout until the user clicks on the button
        setSuccessAndFailureVisibility(successVisibility = View.VISIBLE, failureVisibility = View.GONE)

        recommendationRequest.setOnClickListener {
            // Hide both layouts when clicked
            setSuccessAndFailureVisibility(successVisibility = View.GONE, failureVisibility = View.GONE)

            // Show the scrollview to display the text
            recommendationSV.visibility = View.VISIBLE
            renderedText?.let {
                recommendationText.typeWrite(
                    lifecycleOwner = this@Recommendations,
                    spannedText = it,
                    typingSpeedMs = calculateTypingInterval(it.length),
                    scrollView = recommendationSV
                )
            }
        }
    }

    private fun onRecommendationRequestFailure() {
        setSuccessAndFailureVisibility(successVisibility = View.GONE, failureVisibility = View.VISIBLE)

        loadingIndicator.visibility = View.GONE
        recommendationRequest.isEnabled = true
        recommendationRequest.text = getString(R.string.retry_text) // Change the text to retry

        // Retry the recommendation request
        recommendationRequest.setOnClickListener {
            fetchRecommendations()
        }
        Toast.makeText(this, "Failed to retrieve recommendations. Please try again.", Toast.LENGTH_LONG).show()
    }

    private fun initNavigationElements() {
        val homepageButton = findViewById<FrameLayout>(R.id.home_button)
        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)
        val profileButton = findViewById<FrameLayout>(R.id.profile_button)
        homepageButton.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            intent.putExtra(
                "ACCESS_TOKEN",
                accessToken
            ) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        trackingButton.setOnClickListener {
            val intent = Intent(this, Tracking::class.java)
            intent.putExtra(
                "ACCESS_TOKEN",
                accessToken
            ) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            intent.putExtra(
                "ACCESS_TOKEN",
                accessToken
            ) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    // A function to squeeze the typing speed of the full recommendation to fit within
    private fun calculateTypingInterval(textLength: Int, maxDurationMs: Long = MAX_TYPING_DURATION): Long {
        return maxDurationMs / textLength.coerceAtLeast(1)
    }

    private fun processRecommendationText(responseData: String) {
        val messageText = extractContentFromResponse(responseData).toString()
        val prism4j = Prism4j(RecommendationRequestGrammarLocator())
        val syntaxHighlightPlugin = SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDefault.create())

        val imagesPlugin = ImagesPlugin.create { plugin ->
            plugin.addSchemeHandler(OkHttpNetworkSchemeHandler.create())
        }

        val markwon = Markwon.builder(this)
            .usePlugin(imagesPlugin)
            .usePlugin(syntaxHighlightPlugin)
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .build()

        // Convert string to markdown text and store it
        renderedText = markwon.toMarkdown(messageText)
    }

    // Extension function to allow for a type writer effect for the text view
    private fun TextView.typeWrite(
        lifecycleOwner: LifecycleOwner,
        spannedText: Spanned,
        typingSpeedMs: Long,
        scrollView: ScrollView? = null,
    ) {
        this@typeWrite.text = String()
        val spannableStringBuilder = SpannableStringBuilder()

        lifecycleOwner.lifecycleScope.launch {
            for (i in spannedText.indices) {
                val currentChar = spannedText.subSequence(i, i + 1)
                spannableStringBuilder.append(currentChar)

                // Apply all spans for the current character
                val spans = spannedText.getSpans(i, i + 1, Any::class.java)
                for (span in spans) {
                    val spanStart = spannedText.getSpanStart(span)
                    val spanEnd = spannedText.getSpanEnd(span)

                    // Only apply span if it covers the current character
                    if (spanStart <= i && spanEnd >= i + 1) {
                        spannableStringBuilder.setSpan(
                            span,
                            spanStart.coerceAtMost(spannableStringBuilder.length - 1),
                            spannableStringBuilder.length,
                            spannedText.getSpanFlags(span)
                        )
                    }
                }

                this@typeWrite.text = spannableStringBuilder // Apply the styled text to the UI view

                // Scroll the ScrollView to make the new text visible as it is displayed
                scrollView?.post {
                    scrollView.smoothScrollTo(0, this@typeWrite.bottom)
                }

                delay(typingSpeedMs)
            }
        }
    }

    private fun extractContentFromResponse(jsonString: String): String? {
        return try {
            // Parse the JSON string into a JSONObject
            val jsonObject = JSONObject(jsonString)

            // Navigate to the "message" object
            val messageObject = jsonObject.getJSONObject("message")

            // Return the value of the "content" key
            messageObject.getString("content")
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if there's an error parsing the JSON
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

@PrismBundle(include = ["kotlin", "java"],
    grammarLocatorClassName = ".RecommendationRequestGrammarLocator")
class GrammarLocator { }