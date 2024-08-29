package com.example.dietadvisor

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
            val dialogView = layoutInflater.inflate(R.layout.progressbar_dialog, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            dialog.show()
            dialog.window?.setGravity(Gravity.CENTER)

            val isSuccessful = true

            dialogView.postDelayed({
                dialog.dismiss()
                if (isSuccessful) startActivity(Intent(this, RecognitionResult::class.java))
                else {
                    Toast.makeText(this, resources.getString(R.string.error_message), Toast.LENGTH_LONG).show()
                }
            }, 1000)
        }

        cameraButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.progressbar_dialog, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            dialog.show()
            dialog.window?.setGravity(Gravity.CENTER)

            val isSuccessful = false

            dialogView.postDelayed({
                dialog.dismiss()
                if (isSuccessful) startActivity(Intent(this, RecognitionResult::class.java))
                else {
                    Toast.makeText(this, resources.getString(R.string.error_message), Toast.LENGTH_LONG).show()
                }
            }, 1000)
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

            val intakeHistory = jsonObject.getJSONArray("intakeHistory")

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val c: Date = Calendar.getInstance().time
            val todayDate = dateFormatter.format(c)

            // Retrieve user-specific information
            val dailyCalories = jsonObject.getJSONObject("dietaryInfo").getDouble("TDEE").toFloat()
            val dailyProtein = calculateDailyProtein(jsonObject.getJSONObject("bodyMeasurements").getInt("weight"))
            val dailyCarbs = (dailyCalories * 0.5f) / 4f // 50% of calories from carbs, 4 calories per gram of carbs
            val dailyFat = (dailyCalories * 0.3f) / 9f // 30% of calories from fat, 9 calories per gram of fat

            // Load Today's Intake
            var todayCalories = 0f
            var todayProtein = 0f
            var todayCarbs = 0f
            var todayFat = 0f

            for (i in 0 until intakeHistory.length()) {
                val intakeObject = intakeHistory.getJSONObject(i)
                if (intakeObject.getString("date") == todayDate) {
                    todayCalories = intakeObject.getJSONObject("nutritionalInfo").getDouble("carb").toFloat() * 4 +
                            intakeObject.getJSONObject("nutritionalInfo").getDouble("protein").toFloat() * 4 +
                            intakeObject.getJSONObject("nutritionalInfo").getDouble("fat").toFloat() * 9
                    todayProtein = intakeObject.getJSONObject("nutritionalInfo").getDouble("protein").toFloat()
                    todayCarbs = intakeObject.getJSONObject("nutritionalInfo").getDouble("carb").toFloat()
                    todayFat = intakeObject.getJSONObject("nutritionalInfo").getDouble("fat").toFloat()
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

    private fun calculateDailyProtein(weight: Int): Float {
        return 1.6f * weight // 1.6g protein per kg body weight
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
