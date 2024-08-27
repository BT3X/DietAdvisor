package com.kkt.dietadvisor

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.kkt.dietadvisor.models.BodyMeasurements
import com.kkt.dietadvisor.models.DietaryInfo
import com.kkt.dietadvisor.models.NutritionalInfo
import com.kkt.dietadvisor.models.PersonalInfo
import com.kkt.dietadvisor.models.UserData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

class SignUpwithOAuth : AppCompatActivity() {

    private lateinit var accessToken: String

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_upwith_oauth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve access token from the intent extras
        accessToken = intent.getStringExtra("ACCESS_TOKEN")
            ?: throw IllegalArgumentException("Access token is missing from the intent")
        Log.d(TAG, "onCreate: Access Token Retrieved: $accessToken")

        // Init normal layout views
        val dob = findViewById<EditText>(R.id.dob)
        dob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    dob.setText(selectedDate)
                },
                year,
                month,
                day
            )
            datePicker.show()
        }

        val genderDropdown = findViewById<AutoCompleteTextView>(R.id.gender_dropdown)
        val dietGoalDropdown = findViewById<AutoCompleteTextView>(R.id.diet_goal_dropdown)
        val languageDropdown = findViewById<AutoCompleteTextView>(R.id.language_dropdown)
        val activityDropdown = findViewById<AutoCompleteTextView>(R.id.activity_level_dropdown)

        // Define the data for the dropdown menus
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val dietGoalOptions = resources.getStringArray(R.array.diet_goal_options)
        val languageOptions = resources.getStringArray(R.array.language_options)
        val activityOptions = resources.getStringArray(R.array.activity_level_options)

        // Set up the adapters for the dropdowns
        val genderAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        setupDropdown(genderDropdown, genderAdapter)

        val dietGoalAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dietGoalOptions)
        setupDropdown(dietGoalDropdown, dietGoalAdapter)

        val languageAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languageOptions)
        setupDropdown(languageDropdown, languageAdapter)

        val activityLevelAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, activityOptions)
        setupDropdown(activityDropdown, activityLevelAdapter)

        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            // Do something here to save info to DB

            // Get user info
            getGoogleUserInfo(accessToken, object : UserInfoCallback {
                override fun onUserInfoRetrieved(googleUserInfo: GoogleUserInfo) {
                    val (userID, username) = googleUserInfo

                    // Gather user input data
                    val birthDate = dob.text.toString()
                    val gender = genderDropdown.text.toString()
                    val language = languageDropdown.text.toString()
                    val height = findViewById<EditText>(R.id.height).text.toString().toDoubleOrNull() ?: 0.0
                    val weight = findViewById<EditText>(R.id.weight).text.toString().toDoubleOrNull() ?: 0.0
                    val physicalActivity = activityDropdown.text.toString().toDoubleOrNull() ?: 0.0
                    val dietaryGoal = dietGoalDropdown.text.toString()
                    val dietaryGoalAmount = 0.0
                    val TMR = 0.0
                    val TDEE = 0.0
                    val carb = 0
                    val protein = 0
                    val fat = 0

                    // Build the UserData object
                    val userData = UserData(
                        personalInfo = PersonalInfo(
                            userID = userID,
                            userName = username,
                            birthDate = birthDate,
                            gender = gender,
                            language = language
                        ),
                        bodyMeasurements = BodyMeasurements(
                            weight = weight,
                            height = height,
                            physicalActivity = physicalActivity
                        ),
                        dietaryInfo = DietaryInfo(
                            dietaryGoal = dietaryGoal,
                            dietaryGoalAmount = dietaryGoalAmount,
                            TMR = TMR,
                            TDEE = TDEE
                        ),
                        intakeHistory = emptyList(),
                        lastMeal = NutritionalInfo(
                            carb = carb,
                            protein = protein,
                            fat = fat,
                        )
                    )

                    // Make Http Request to backend to create user
                    createUser(accessToken, userData) { userCreated ->
                        if (userCreated) {
                            runOnUiThread {
                                Log.d(TAG, "onUserInfoRetrieved: User Created Successfully!")
                                Toast.makeText(this@SignUpwithOAuth, "User Created Successfully!", Toast.LENGTH_SHORT).show()
                                Toast.makeText(this@SignUpwithOAuth, "Welcome to Diet Advisor!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@SignUpwithOAuth, HomePage::class.java))
                            }
                        } else {
                            runOnUiThread {
                                Log.d(TAG, "onUserInfoRetrieved: User Creation Failure!")
                                Toast.makeText(this@SignUpwithOAuth, "Something went wrong!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onError(error: String) {
                    Log.e(TAG, "onError: Error: $error", )
                }
            })

        }
    }

    private fun setupDropdown(
        autoCompleteTextView: AutoCompleteTextView,
        adapter: ArrayAdapter<String>,
    ) {
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.inputType = 0  // Set inputType to none programmatically

        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.showDropDown()
                hideSoftKeyboard(autoCompleteTextView)
            }
        }

        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
            hideSoftKeyboard(autoCompleteTextView)
        }
    }

    private fun hideSoftKeyboard(view: AutoCompleteTextView) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun createUser(accessToken: String, userData: UserData, onResult: (Boolean) -> Unit) {
        val url = getString(R.string.DIET_ADVISOR_USER_ENDPOINT_URL)

        // Convert the user data to JSON
        val jsonBody = Gson().toJson(userData)

        // Create a RequestBody from the JSON string using the non-deprecated method
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        // Build the request with POST method
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)  // Attach the requestBody
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle the response
                    val responseBody = response.body?.string()
                    println(responseBody)
                    onResult(true)
                } else {
                    // Handle the error
                    println("Request failed: ${response.message}")
                    onResult(false)
                }
            }
        })
    }

    private fun getGoogleUserInfo(accessToken: String, userInfoCallback: UserInfoCallback) {
        val url = "https://www.googleapis.com/oauth2/v3/userinfo"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Failed to retrieve Google user info", e)
                userInfoCallback.onError("Failed to retrieve Google user info")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody.string())
                        val userID = jsonResponse.getString("sub")
                        val username = jsonResponse.getString("name")
                        val email = jsonResponse.getString("email")

                        // Return to caller
                        Log.d(TAG, "UserID: $userID, UserName: $username, Email: $email")
                        val retrievedUserInfo = GoogleUserInfo(userID, username)
                        userInfoCallback.onUserInfoRetrieved(retrievedUserInfo)
                    } ?: run {
                        Log.e(TAG, "Failed to parse Google user info: empty response body")
                        userInfoCallback.onError("Failed to parse Google User Info: Empty response body")
                    }
                } else {
                    Log.e(TAG, "Failed to retrieve Google user info: ${response.message}")
                    userInfoCallback.onError("Failed to retrieve Google user info: ${response.message}")
                }
            }
        })
    }
}

interface UserInfoCallback {
    fun onUserInfoRetrieved(googleUserInfo: GoogleUserInfo)
    fun onError(error: String)
}

data class GoogleUserInfo(
    val userID: String,
    val username: String,
)


