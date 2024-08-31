package com.kkt.dietadvisor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.kkt.dietadvisor.models.IntakeEntry
import com.kkt.dietadvisor.models.NutritionalInfo
import com.kkt.dietadvisor.models.UserData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class FoodItemAnalysis(
    val name: String,
    val calories: Float,
    val carbohydrates: Float,
    val protein: Float,
    val fat: Float
)

class AnalysisResult : AppCompatActivity() {

    private lateinit var foodItems: List<FoodItemAnalysis>
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

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analysis_result)
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

        // Retrieve the JSON data from the intent extras
        val analysisResults = intent.getStringExtra("ANALYSIS_RESULTS")
            ?: throw IllegalArgumentException("Analysis Data is missing from the intent")

        // Retrieve the Photo Uri from the intent extras (as a string)
        val uriString = intent.getStringExtra("URI_STRING")
            ?: throw IllegalArgumentException("Uri string is missing from the intent")

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

        // Populate food items list
        foodItems = loadFoodItems(jsonData = analysisResults)
        println("Food items count: ${foodItems.size}")

        // Initialize the rest of the UI as normal
        initUI()
    }

    private fun initUI() {
        val tableLayout = findViewById<TableLayout>(R.id.table)

        var totalCalories = 0.0
        var totalCarbs = 0.0
        var totalProtein = 0.0
        var totalFat = 0.0

        for (food in foodItems) {
            val tableRow = TableRow(this)
            val font = ResourcesCompat.getFont(this, R.font.itim)

            val nameTextView = TextView(this).apply {
                text = food.name
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(220, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(16, 8, 16, 8)
            }

            val caloriesTextView = TextView(this).apply {
                text = String.format("%.1f", food.calories)
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(16, 8, 16, 8)
            }

            val carbsTextView = TextView(this).apply {
                text = String.format("%.1f", food.carbohydrates)
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(16, 8, 16, 8)
            }

            val proteinTextView = TextView(this).apply {
                text = String.format("%.1f", food.protein)
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(16, 8, 16, 8)
            }

            val fatTextView = TextView(this).apply {
                text = String.format("%.1f", food.fat)
                typeface = font
                textSize = 17F
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(16, 8, 16, 8)
            }

            tableRow.addView(nameTextView)
            tableRow.addView(caloriesTextView)
            tableRow.addView(carbsTextView)
            tableRow.addView(proteinTextView)
            tableRow.addView(fatTextView)
            tableRow.showDividers = TableRow.SHOW_DIVIDER_MIDDLE
            tableRow.dividerDrawable = ResourcesCompat.getDrawable(resources, R.drawable.divider_column, null)

            tableLayout.addView(tableRow)

            totalCalories += food.calories
            totalCarbs += food.carbohydrates
            totalProtein += food.protein
            totalFat += food.fat
        }

        val totalRow = TableRow(this)
        val totalFont = ResourcesCompat.getFont(this, R.font.itim)

        val totalTextView = TextView(this).apply {
            text = resources.getString(R.string.total)
            typeface = totalFont
            setTypeface(typeface, Typeface.BOLD)
            textSize = 17F
            layoutParams = TableRow.LayoutParams(220, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(16, 8, 16, 8)
        }

        val totalCaloriesTextView = TextView(this).apply {
            text = String.format("%.1f", totalCalories)
            typeface = totalFont
            textSize = 17F
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(16, 8, 16, 8)
        }

        val totalCarbsTextView = TextView(this).apply {
            text = String.format("%.1f", totalCarbs)
            typeface = totalFont
            textSize = 17F
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(16, 8, 16, 8)
        }

        val totalProteinTextView = TextView(this).apply {
            text = String.format("%.1f", totalProtein)
            typeface = totalFont
            textSize = 17F
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(16, 8, 16, 8)
        }

        val totalFatTextView = TextView(this).apply {
            text = String.format("%.1f", totalFat)
            typeface = totalFont
            textSize = 17F
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(16, 8, 16, 8)
        }

        totalRow.addView(totalTextView)
        totalRow.addView(totalCaloriesTextView)
        totalRow.addView(totalCarbsTextView)
        totalRow.addView(totalProteinTextView)
        totalRow.addView(totalFatTextView)
        totalRow.showDividers = TableRow.SHOW_DIVIDER_MIDDLE
        totalRow.dividerDrawable = ResourcesCompat.getDrawable(resources, R.drawable.divider_column, null)

        tableLayout.addView(totalRow)

        val cancelButton = findViewById<Button>(R.id.cancel_button)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        cancelButton.setOnClickListener {
            // TODO: Change this to destroy the activity instead of starting a new intent
            startActivity(Intent(this, HomePage::class.java))
        }
        confirmButton.setOnClickListener {
            /*
            TODO:
                Send [PUT] request to /user endpoint, add current meal to the
                user's intake history in the database
            */
            // Create NutritionalInfo object from user's total intake values for this meal
            val totalIntake = NutritionalInfo(
                carb = totalCarbs,
                protein = totalProtein,
                fat = totalFat,
                calorie = totalCalories
            )

            // TODO: Add callback so that intent is started only if the update is successful
            updateUserIntake(accessToken = accessToken, userIntake = totalIntake)
            val intent = Intent(this, Tracking::class.java)
            intent.putExtra("ACCESS_TOKEN", accessToken)
            startActivity(intent)
        }
    }

    private fun updateUserIntake(accessToken: String, userIntake: NutritionalInfo) {
        // Retrieve the user info
        getUserInfo(accessToken) { userExists, userInfo ->
            if (userExists) {
                userInfo?.let {
                    // Convert the response body to UserData object
                    val gson = Gson()
                    val userData = gson.fromJson(it, UserData::class.java)

                    // Update intakeHistory
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    // Initialize flag to track if today's entry exists
                    var intakeUpdated = false

                    // Update intakeHistory with the new entry
                    val updatedIntakeHistory = userData.intakeHistory.toMutableList()

                    // Iterate over the intake history to find today's entry and update it
                    for (i in updatedIntakeHistory.indices) {
                        val intakeEntry = updatedIntakeHistory[i]
                        if (intakeEntry.date == currentDate) {
                            // Update the nutritional information by adding the new values
                            val updatedNutritionalInfo = NutritionalInfo(
                                carb = intakeEntry.nutritionalInfo.carb + userIntake.carb,
                                protein = intakeEntry.nutritionalInfo.protein + userIntake.protein,
                                fat = intakeEntry.nutritionalInfo.fat + userIntake.fat,
                                calorie = intakeEntry.nutritionalInfo.calorie + userIntake.calorie
                            )
                            // Replace the old entry with the updated one
                            updatedIntakeHistory[i] = intakeEntry.copy(nutritionalInfo = updatedNutritionalInfo)
                            intakeUpdated = true
                            break
                        }
                    }

                    // If today's entry was not found, add a new one
                    if (!intakeUpdated) {
                        val newIntakeEntry = IntakeEntry(date = currentDate, nutritionalInfo = userIntake)
                        updatedIntakeHistory.add(newIntakeEntry)
                    }

                    // Update lastMeal with the same data as userIntake
                    val updatedUserData = userData.copy(
                        intakeHistory = updatedIntakeHistory,
                        lastMeal = userIntake
                    )

                    // Convert updatedUserData back to JSON
                    val updatedJson = gson.toJson(updatedUserData)
                    Log.d(TAG, "Updated JSON: $updatedJson")

                    // Update the backend with the updated JSON
                    updateUser(accessToken, updatedJson) { updateSuccessful ->
                        if (updateSuccessful) {
                            runOnUiThread {
                                Toast.makeText(this, "Meal Added to Tracking!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } ?: run {
                    Log.e(TAG, "Failure: Unable to retrieve user")
                }
            } else {
                Log.e(TAG, "updateUserIntake: Failure! User does not exist", )
            }
        }
    }

    private fun updateUser(accessToken: String, updatedJson: String, onResult: (Boolean) -> Unit) {
        val url = getString(R.string.DIET_ADVISOR_USER_ENDPOINT_URL)

        // Create the request body with the updated JSON
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = updatedJson.toRequestBody(mediaType)

        // Build the PUT request
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("accept", "*/*")
            .addHeader("Authorization", "Bearer $accessToken") // Add the access token here
            .build()

        // Perform the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Update failed: ${e.message}")
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "User updated successfully")
                    onResult(true)
                } else {
                    Log.e(TAG, "Update failed with response code: ${response.code}")
                    onResult(false)
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

    private fun loadFoodItems(jsonData: String): List<FoodItemAnalysis> {
        return try {
            val jsonArray = JSONArray(jsonData)
            val foodList = mutableListOf<FoodItemAnalysis>()

            for (i in 0 until jsonArray.length()) {
                val foodJson = jsonArray.getJSONObject(i)
                val name = foodJson.getString("name")
                val nutrition = foodJson.getJSONObject("nutrition")
                val calories = nutrition.getDouble("calories").toFloat()
                val carbohydrates = nutrition.getDouble("carbohydrates").toFloat()
                val protein = nutrition.getDouble("protein").toFloat()
                val fat = nutrition.getDouble("fat").toFloat()
                foodList.add(FoodItemAnalysis(name, calories, carbohydrates, protein, fat))
            }

            foodList // Return populated food list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return empty list in case there's an error
        }
    }
}