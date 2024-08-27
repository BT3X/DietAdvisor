package com.kkt.dietadvisor

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ir.mahozad.android.PieChart
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomePage : AppCompatActivity() {

    private lateinit var currentPhotoPath: String

    // Init the image capture launcher & callback
    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captureSuccessful ->
        if (captureSuccessful) {
            Log.d(TAG, "Image Capture Successful - Current Path: $currentPhotoPath")
            val file = File(currentPhotoPath)
            val imageUri = Uri.fromFile(file)

            // Send the captured image's uri to the RecognitionResult activity
            Log.d(TAG, "Starting RecognitionResult with PHOTO_URI: $imageUri")
            val intent = Intent(this, RecognitionResult::class.java)
            intent.putExtra("PHOTO_URI", imageUri.toString())
            startActivity(intent)
        } else {
            Log.e(TAG, "Image Capture Failed")
        }
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

        // Init views
        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)
        val recommendationsButton = findViewById<FrameLayout>(R.id.recommendations_button)
        val profileButton = findViewById<FrameLayout>(R.id.profile_button)
        val uploadButton = findViewById<LinearLayout>(R.id.upload_photo)
        val cameraButton = findViewById<LinearLayout>(R.id.camera_button)

        // Init the Android 13 photo picker launcher & callback
        val selectImageLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { selectedImageUri ->
            selectedImageUri?.let {
                Log.d(TAG, "SelectMedia Successful - Uri: $selectedImageUri")
                // TODO: Send the Uri to the RecognitionResult activity and do work
                val intent = Intent(this, RecognitionResult::class.java)
                intent.putExtra("PHOTO_URI", selectedImageUri.toString())
                startActivity(intent)
            } ?: run {
                Log.e(TAG, "SelectMedia: Failed to retrieve Uri")
            }
        }

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
            startActivity(Intent(this, Tracking::class.java))
        }
        recommendationsButton.setOnClickListener {
            startActivity(Intent(this, Recommendations::class.java))
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, UserProfile::class.java))
        }

        loadUserInfo() // TODO: Load data dynamically from backend
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

    @SuppressLint("SimpleDateFormat")
    private fun loadUserInfo() {
        try {
            val inputStream: InputStream = assets.open("userInfo.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("Intake")

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val c: Date = Calendar.getInstance().time
            val todayDate = dateFormatter.format(c)

            // User Information
            val dob = jsonObject.getString("Date of Birth")
            val gender = jsonObject.getString("Gender")
            val height = jsonObject.getInt("Height")
            val weight = jsonObject.getInt("Weight")
            val dietGoal = jsonObject.getString("Dietary Goal")
            val activityLevel = jsonObject.getString("Activity Level")

            // Calculate Daily Requirements
            val age = getAgeFromDOB(dob)
            val dailyCalories = calculateDailyCalories(gender, age, height, weight, dietGoal, activityLevel)
            val dailyProtein = calculateDailyProtein(weight)
            val dailyCarbs = calculateDailyCarbs(dailyCalories)
            val dailyFat = calculateDailyFat(dailyCalories)

            // Load Today's Intake
            var todayCalories = 0f
            var todayProtein = 0f
            var todayCarbs = 0f
            var todayFat = 0f

            for (i in 0 until jsonArray.length()) {
                val intakeObject = jsonArray.getJSONObject(i)
                if (intakeObject.getString("Date") == todayDate) {
                    todayCalories = intakeObject.getDouble("Calorie").toFloat()
                    todayProtein = intakeObject.getDouble("Protein").toFloat()
                    todayCarbs = intakeObject.getDouble("Carb").toFloat()
                    todayFat = intakeObject.getDouble("Fat").toFloat()
                    break
                }
            }

            // Update Pie Charts
            updatePieChart(
                R.id.calorie_pc, R.id.calorie_info,
                todayCalories, dailyCalories, "kcal"
            )
            updatePieChart(
                R.id.carb_pc, R.id.carb_info,
                todayCarbs, dailyCarbs, "g"
            )
            updatePieChart(
                R.id.protein_pc, R.id.protein_info,
                todayProtein, dailyProtein, "g"
            )
            updatePieChart(
                R.id.fat_pc, R.id.fat_info,
                todayFat, dailyFat, "g"
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAgeFromDOB(dob: String): Int {
        val dobFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDate = dobFormatter.parse(dob)
        val today = Calendar.getInstance()

        val birthCalendar = Calendar.getInstance()
        birthCalendar.time = birthDate!!

        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun calculateDailyCalories(
        gender: String,
        age: Int,
        height: Int,
        weight: Int,
        dietGoal: String,
        activityLevel: String
    ): Float {
        val bmr: Float = when (gender.lowercase(Locale.getDefault())) {
            "male" -> 10f * weight + 6.25f * height - 5f * age + 5f
            "female" -> 10f * weight + 6.25f * height - 5f * age - 161f
            else -> (10f * weight + 6.25f * height - 5f * age - 161f + 10f * weight + 6.25f * height - 5f * age + 5f) / 2f
        }

        // Activity level adjustment
        val activityMultiplier = when (activityLevel.lowercase(Locale.getDefault())) {
            "sedentary" -> 1.2f
            "lightly active" -> 1.375f
            "moderately active" -> 1.55f
            "very active" -> 1.725f
            "extra active" -> 1.9f
            else -> 1.2f // Default to sedentary if no valid activity level is provided
        }

        val calorieAdjustment = when (dietGoal.lowercase(Locale.getDefault())) {
            "lose weight" -> -500f
            "gain weight" -> 500f
            else -> 0f
        }

        return bmr * activityMultiplier + calorieAdjustment
    }

    private fun calculateDailyProtein(weight: Int): Float {
        return 1.6f * weight // 1.6g protein per kg body weight
    }

    private fun calculateDailyCarbs(dailyCalories: Float): Float {
        return (dailyCalories * 0.5f) / 4f // 50% of calories from carbs, 4 calories per gram of carbs
    }

    private fun calculateDailyFat(dailyCalories: Float): Float {
        return (dailyCalories * 0.3f) / 9f // 30% of calories from fat, 9 calories per gram of fat
    }

    private fun updatePieChart(pieChartId: Int, infoTextId: Int, consumed: Float, daily: Float, unit: String) {
        val percentage = consumed / daily
        val displayedPercentage = if (percentage >= 1f) 1f else percentage
        val pieChart = findViewById<PieChart>(pieChartId)
        val infoText = findViewById<TextView>(infoTextId)

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
