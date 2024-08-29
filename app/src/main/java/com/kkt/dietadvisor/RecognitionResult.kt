package com.kkt.dietadvisor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

data class FoodItem(var name: String, val index: Int)

class RecognitionResult : AppCompatActivity() {

    private lateinit var foodItems : List<FoodItem>
    private lateinit var uriString: String
    private lateinit var accessToken: String

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
//            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Change response timeout to 1 seconds
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recognition_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*
        TODO:
            Main work flow:
            /yolo -> {DET.json} -> /calorie -> /user [PUT] (Update)
        */
        // Retrieve the Photo Uri from the intent extras (as a string)
        uriString = intent.getStringExtra("PHOTO_URI")
            ?: throw IllegalArgumentException("Photo Uri is missing from the intent")

        // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
        // Retrieve access token from the intent extras
        accessToken = intent.getStringExtra("ACCESS_TOKEN")
            ?: throw IllegalArgumentException("Access token is missing from the intent")
        Log.d(TAG, "onCreate: Access Token Retrieved: $accessToken")

        // Convert back to Uri from string
        val photoUri = uriString.let { Uri.parse(it) }
        Log.d(TAG, "onCreate: Photo Uri Retrieved: $photoUri")

        // Load retrieved Uri into image view
        val resultImageView = findViewById<ImageView>(R.id.result_image)
        photoUri.let {
            Glide.with(this)
                .load(it)
                .into(resultImageView)
        }

        /*
        TODO:
            Send [POST] request to /yolo endpoint, retrieve preliminary recognition values
            then load into image view and continue with confirmation workflow
        */
        val requestBody = contentResolver.openInputStream(photoUri)?.use { inputStream ->
            inputStream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
        }

        requestBody?.let {
            val multipartBody = requestBody.let {
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "filename.jpg", it)
                    .build()
            }

            val request = multipartBody.let {
                Request.Builder()
                    .url(getString(R.string.DIET_ADVISOR_YOLO_ENDPOINT_URL))
                    .post(it)
                    .addHeader("accept", "text/plain")
                    .addHeader("Content-Type", "multipart/form-data")
                    .build()
            }

            request.let {
                client.newCall(it).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "onFailure: Request Failed: ${e.message}", )
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            // Handle the successful response
                            response.body?.let { responseBody ->
                                val jsonResponse = responseBody.string()
                                Log.d(TAG, "onResponse: Viewing JSON Response")
                                println(jsonResponse)

                                // Populate food items list
                                foodItems = loadFoodItems(jsonData = jsonResponse)
                                println("Food items count: ${foodItems.size}")

                                // Init UI with loaded food items
                                runOnUiThread {
                                    initUI(jsonData = jsonResponse)
                                }
                            }
                        } else {
                            Log.e(TAG, "onResponse: Request Failed: ${response.message}", )
                        }
                    }
                })
            }
        }

    }

    // Called after the foodItems list has been populated after receiving the network response
    private fun initUI(jsonData: String) {
        val tableLayout = findViewById<TableLayout>(R.id.recognized_food_items)

        for ((index, food) in foodItems.withIndex()) {
            val tableRow = TableRow(this)
            val font = ResourcesCompat.getFont(this, R.font.itim)
            val idTextView = TextView(this).apply {
                text = (index + 1).toString()
                gravity = android.view.Gravity.CENTER
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(16, 8, 16, 8)
            }

            val nameTextView = TextView(this).apply {
                text = food.name
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                setPadding(16, 8, 16, 8)
            }

            val changeTextView = TextView(this).apply {
                val changeText = resources.getString(R.string.change)
                val spannableString = SpannableString(changeText)
                spannableString.setSpan(UnderlineSpan(), 0, changeText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text = spannableString
                gravity = android.view.Gravity.CENTER
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(16, 8, 16, 8)

                setOnClickListener {
                    showChangeFoodDialog(food, nameTextView)
                }
            }

            tableRow.addView(idTextView)
            tableRow.addView(nameTextView)
            tableRow.addView(changeTextView)
            tableRow.dividerDrawable = ResourcesCompat.getDrawable(resources, R.drawable.divider_column, null)
            tableRow.showDividers = TableRow.SHOW_DIVIDER_MIDDLE

            println("Adding row $index with food name: ${food.name}")

            tableLayout.addView(tableRow)
        }

        val cancelButton = findViewById<Button>(R.id.cancel_button)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        cancelButton.setOnClickListener {
            // TODO: Change this to destroy the activity instead of starting a new intent
            startActivity(Intent(this, HomePage::class.java))
        }
        confirmButton.setOnClickListener {
            // Update JSON data
            val updatedJsonData = updateJsonData(foodItems, jsonData)

            //  Send to the AnalysisResult activity
            val intent = Intent(this, AnalysisResult::class.java)
            intent.putExtra("UPDATED_JSON_DATA", updatedJsonData)
            intent.putExtra("PHOTO_URI", uriString)
            intent.putExtra("ACCESS_TOKEN", accessToken)
            startActivity(intent)
        }
    }

    private fun loadFoodItems(jsonData: String): List<FoodItem> {
        try {
            val jsonArray = JSONArray(jsonData)
            val foodList = mutableListOf<FoodItem>()

            for (i in 0 until jsonArray.length()) {
                val foodJson = jsonArray.getJSONObject(i)
                val name = foodJson.getString("name")
                if (name != "coin") foodList.add(FoodItem(name, i))
            }

            return foodList
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun showChangeFoodDialog(foodItem: FoodItem, nameTextView: TextView) {
        val foodOptions = resources.getStringArray(R.array.food_items)

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.select_food_item))
            .setItems(foodOptions) { _, which ->
                val selectedName = foodOptions[which]
                nameTextView.text = selectedName
                foodItem.name = selectedName
            }
            .show()
    }

    private fun updateJsonData(foodItems: List<FoodItem>, jsonData: String): String {
        return try {
            val jsonArray = JSONArray(jsonData)

            for (foodItem in foodItems) {
                val jsonObject = jsonArray.getJSONObject(foodItem.index)
                jsonObject.put("name", foodItem.name)
            }

            jsonArray.toString() // Return updated JSON string
        } catch (e: Exception) {
            e.printStackTrace()
            jsonData // Return original JSON String in case of error
        }
    }
}