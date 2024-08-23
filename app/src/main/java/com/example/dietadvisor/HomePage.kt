package com.example.dietadvisor

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ir.mahozad.android.PieChart
import org.json.JSONObject
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomePage : AppCompatActivity() {
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

        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)
        val recommendationsButton = findViewById<FrameLayout>(R.id.recommendations_button)
        val profileButton = findViewById<FrameLayout>(R.id.profile_button)
        val uploadButton = findViewById<LinearLayout>(R.id.upload_photo)
        val cameraButton = findViewById<LinearLayout>(R.id.camera_button)

        uploadButton.setOnClickListener {
            startActivity(Intent(this, RecognitionResult::class.java))
        }
        cameraButton.setOnClickListener {
            startActivity(Intent(this, RecognitionResult::class.java))
        }
        trackingButton.setOnClickListener {
            startActivity(Intent(this, Tracking::class.java))
        }
        recommendationsButton.setOnClickListener {
            startActivity(Intent(this, Recommendations::class.java))
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, UserProfile::class.java))
        }

        loadUserInfo()
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
