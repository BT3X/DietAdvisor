package com.kkt.dietadvisor

import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ir.mahozad.android.PieChart
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomePage : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
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
        setContentView(R.layout.activity_home_page)
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
        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)
        val recommendationsButton = findViewById<FrameLayout>(R.id.recommendations_button)
        val profileButton = findViewById<FrameLayout>(R.id.profile_button)
        val uploadButton = findViewById<LinearLayout>(R.id.upload_photo)
        val cameraButton = findViewById<LinearLayout>(R.id.camera_button)

        /* Camera/Recognition Related */
        uploadButton.setOnClickListener {
            selectImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        cameraButton.setOnClickListener {
            dispatchCaptureImageIntent()
        }

        /* Navigation Related */
        trackingButton.setOnClickListener {
            val intent = Intent(this, Tracking::class.java)
            intent.putExtra("ACCESS_TOKEN", accessToken) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        recommendationsButton.setOnClickListener {
            val intent = Intent(this, Recommendations::class.java)
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

        // Get user info from backend and load into UI
        getUserInfo(accessToken) { userExists, userInfo ->
            if (userExists) {
                userInfo?.let {
                    Log.d(TAG, "onCreate: Loading UI with user info...")
                    loadUserInfo(jsonString = userInfo)
                }
            } else {
                Log.e(TAG, "getUserInfoCallback: Failure! User does not exist", )
            }
        }
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

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Launch the camera intent to take a photo
    private fun dispatchCaptureImageIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e(TAG, "dispatchTakePictureIntent: Image Capture Failure: ${ex.message}", )
            ex.printStackTrace()
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                it
            )
            captureImageLauncher.launch(photoURI)
        }
    }

    // Init the image capture launcher & callback
    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captureSuccessful ->
        if (captureSuccessful) {
            Log.d(TAG, "Image Capture Successful - Current Path: $currentPhotoPath")
            val file = File(currentPhotoPath)
            val capturedImageUri = Uri.fromFile(file)

            // Set up processing indicator
            val dialogView = layoutInflater.inflate(R.layout.progressbar_dialog, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            // NOTE: Dialog must be shown on screen for ${dialog.window} to produce a non-null value
            dialog.show()
            dialog.window?.setGravity(Gravity.CENTER)

            /*
            TODO:
                Send [POST] request to /yolo endpoint, retrieve preliminary recognition values
                then load into image view and continue with confirmation workflow
            */
            dispatchFoodRecognitionRequest(capturedImageUri) { isSuccessful, recognitionResults ->
                if (isSuccessful) {
                    dialog.dismiss()

                    // Send the captured image's uri to the RecognitionResult activity
                    Log.d(TAG, "Starting RecognitionResult with PHOTO_URI: $capturedImageUri")
                    val intent = Intent(this, RecognitionResult::class.java)
                    intent.putExtra("ACCESS_TOKEN", accessToken) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
                    intent.putExtra("RECOGNITION_RESULTS", recognitionResults)
                    intent.putExtra("URI_STRING", capturedImageUri.toString())
                    startActivity(intent)
                } else {
                    dialog.dismiss()
                    Toast.makeText(this, resources.getString(R.string.error_message), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Log.e(TAG, "Image Capture Failed")
        }
    }

    // Init the Android 13 photo picker launcher & callback
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { selectedImageUri ->
        selectedImageUri?.let {
            Log.d(TAG, "SelectMedia Successful - Uri: $selectedImageUri")

            // Set up processing indicator
            val dialogView = layoutInflater.inflate(R.layout.progressbar_dialog, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            // NOTE: Dialog must be shown on screen for ${dialog.window} to produce a non-null value
            dialog.show()
            dialog.window?.setGravity(Gravity.CENTER)

            /*
            TODO:
                Send [POST] request to /yolo endpoint, retrieve preliminary recognition values
                then load into image view and continue with confirmation workflow
            */
            dispatchFoodRecognitionRequest(selectedImageUri) { isSuccessful, recognitionResults ->
                if (isSuccessful) {
                    dialog.dismiss()

                    // Send to RecognitionResult activity
                    val intent = Intent(this, RecognitionResult::class.java)
                    intent.putExtra("ACCESS_TOKEN", accessToken) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
                    intent.putExtra("RECOGNITION_RESULTS", recognitionResults)
                    intent.putExtra("URI_STRING", selectedImageUri.toString())
                    startActivity(intent)
                }
                else {
                    dialog.dismiss()
                    Toast.makeText(this, resources.getString(R.string.error_message), Toast.LENGTH_LONG).show()
                }
            }
        } ?: run {
            Log.e(TAG, "SelectMedia: Failed to retrieve Uri")
        }
    }

    private fun dispatchFoodRecognitionRequest(photoUri: Uri, onResult: (Boolean, String?) -> Unit) {
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
                        runOnUiThread {
                            Log.e(TAG, "onFailure: Request Failed: ${e.message}")
                            e.printStackTrace()
                            onResult(false, null)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            // Handle the successful response
                            response.body?.let { responseBody ->
                                runOnUiThread {
                                    val jsonResponse = responseBody.string()
                                    Log.d(TAG, "onResponse: Viewing JSON Response")
                                    println(jsonResponse)
                                    onResult(true, jsonResponse)
                                }
                            }
                        } else {
                            runOnUiThread {
                                Log.e(TAG, "onResponse: Request Failed: ${response.message}")
                                onResult(false, null)
                            }
                        }
                    }
                })
            }
        }
    }

    private fun loadUserInfo(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)

            val intakeHistory = jsonObject.getJSONArray("intakeHistory")

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val c: Date = Calendar.getInstance().time
            val todayDate = dateFormatter.format(c)
            Log.d(TAG, "loadUserInfo: todayDate: $todayDate")
            Log.d(TAG, "---------------------------------")

            // Retrieve user-specific information
            val dailyCalories = jsonObject.getJSONObject("dietaryInfo").getDouble("TDEE").toFloat()
            val dailyProtein = calculateDailyProtein(jsonObject.getJSONObject("bodyMeasurements").getInt("weight"))
            val dailyCarbs = (dailyCalories * 0.5f) / 4f // 50% of calories from carbs, 4 calories per gram of carbs
            val dailyFat = (dailyCalories * 0.3f) / 9f // 30% of calories from fat, 9 calories per gram of fat

            Log.d(TAG, "Daily Intake:")
            Log.d(TAG, "---------------------------------")
            Log.d(TAG, "loadUserInfo: dailyCalories: $dailyCalories")
            Log.d(TAG, "loadUserInfo: dailyProtein: $dailyProtein")
            Log.d(TAG, "loadUserInfo: dailyCarbs: $dailyCarbs")
            Log.d(TAG, "loadUserInfo: dailyFat: $dailyFat")
            Log.d(TAG, "---------------------------------\n")

            // Load Today's Intake
            var todayCalories = 0f
            var todayProtein = 0f
            var todayCarbs = 0f
            var todayFat = 0f

            Log.d(TAG, "loadUserInfo: intakeHistory.length(): ${intakeHistory.length()}")
            Log.d(TAG, "---------------------------------")

            for (i in 0 until intakeHistory.length()) {
                val intakeObject = intakeHistory.getJSONObject(i)
                Log.d(TAG, "loadUserInfo: date: ${intakeObject.getString("date")}")
                Log.d(TAG, "loadUserInfo: Dates Match: ${intakeObject.getString("date") == todayDate}")
                Log.d(TAG, "---------------------------------")
                if (intakeObject.getString("date") == todayDate) {
                    todayCalories = intakeObject.getJSONObject("nutritionalInfo").getDouble("calorie").toFloat()
                    todayProtein = intakeObject.getJSONObject("nutritionalInfo").getDouble("protein").toFloat()
                    todayCarbs = intakeObject.getJSONObject("nutritionalInfo").getDouble("carb").toFloat()
                    todayFat = intakeObject.getJSONObject("nutritionalInfo").getDouble("fat").toFloat()

                    Log.d(TAG, "loadUserInfo: Iteration: $i")
                    Log.d(TAG, "---------------------------------")
                    Log.d(TAG, "loadUserInfo: IntakeObject[i]: $intakeObject")
                    Log.d(TAG, "---------------------------------")
                    Log.d(TAG, "loadUserInfo: todayCalories: $todayCalories")
                    Log.d(TAG, "loadUserInfo: todayProtein: $todayProtein")
                    Log.d(TAG, "loadUserInfo: todayCarbs: $todayCarbs")
                    Log.d(TAG, "loadUserInfo: todayFat: $todayFat")
                    Log.d(TAG, "---------------------------------")

                    break
                }
            }

            Log.d(TAG, "Total Intake:")
            Log.d(TAG, "---------------------------------")
            Log.d(TAG, "loadUserInfo: todayCalories: $todayCalories / $dailyCalories")
            Log.d(TAG, "loadUserInfo: todayProtein: $todayProtein / $dailyProtein")
            Log.d(TAG, "loadUserInfo: todayCarbs: $todayCarbs / $dailyCarbs")
            Log.d(TAG, "loadUserInfo: todayFat: $todayFat / $dailyFat")
            Log.d(TAG, "---------------------------------")

            // Update Pie Charts
            runOnUiThread {
                Log.d(TAG, "loadUserInfo: Update Calorie Chart")
                updatePieChart(
                    R.id.calorie_pc, R.id.calorie_info,
                    todayCalories, dailyCalories, "kcal"
                )
                Log.d(TAG, "loadUserInfo: Update Carbs Chart")
                updatePieChart(
                    R.id.carb_pc, R.id.carb_info,
                    todayCarbs, dailyCarbs, "g"
                )
                Log.d(TAG, "loadUserInfo: Update Protein Chart")
                updatePieChart(
                    R.id.protein_pc, R.id.protein_info,
                    todayProtein, dailyProtein, "g"
                )
                Log.d(TAG, "loadUserInfo: Update Fat Chart")
                updatePieChart(
                    R.id.fat_pc, R.id.fat_info,
                    todayFat, dailyFat, "g"
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateDailyProtein(weight: Int): Float {
        return 1.6f * weight // 1.6g protein per kg body weight
    }

    private fun updatePieChart(pieChartId: Int, infoTextId: Int, consumed: Float, daily: Float, unit: String) {
        val percentage = consumed / daily
        val displayedPercentage = if (percentage >= 1f) 1f else percentage
        val pieChart = findViewById<PieChart>(pieChartId)
        val infoText = findViewById<TextView>(infoTextId)

        Log.d(TAG, "updatePieChart: Percentage: $percentage")

        pieChart.apply {
            slices = listOf(
                PieChart.Slice(displayedPercentage, resources.getColor(R.color.greenButton)),
                PieChart.Slice(1 - displayedPercentage, resources.getColor(R.color.grayTextInTextView))
            )
            labelType = PieChart.LabelType.NONE
        }

        infoText.text = "${consumed.toInt()}$unit\n${(percentage * 100).toInt()}%"
    }
}

