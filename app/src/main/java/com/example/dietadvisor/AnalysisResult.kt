package com.example.dietadvisor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import java.io.InputStream
import java.lang.reflect.Type

data class FoodItemAnalysis(
    val name: String,
    val calories: Float,
    val carbohydrates: Float,
    val protein: Float,
    val fat: Float
)

class AnalysisResult : AppCompatActivity() {
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

        val foodItems = loadFoodItems()
        println("Food items count: ${foodItems.size}")

        val tableLayout = findViewById<TableLayout>(R.id.table)

        var totalCalories = 0f
        var totalCarbs = 0f
        var totalProtein = 0f
        var totalFat = 0f

        for (food in foodItems) {
            val tableRow = TableRow(this)
            val font = ResourcesCompat.getFont(this, R.font.itim)

            val nameTextView = TextView(this).apply {
                text = food.name
                typeface = font
                textSize = 17F
                setBackgroundResource(R.drawable.table_background)
                layoutParams = TableRow.LayoutParams(220, TableRow.LayoutParams.WRAP_CONTENT)
            }

            val caloriesTextView = TextView(this).apply {
                text = String.format("%.1f", food.calories)
                typeface = font
                textSize = 17F
                setBackgroundResource(R.drawable.table_background)
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            }

            val carbsTextView = TextView(this).apply {
                text = String.format("%.1f", food.carbohydrates)
                typeface = font
                textSize = 17F
                setBackgroundResource(R.drawable.table_background)
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            }

            val proteinTextView = TextView(this).apply {
                text = String.format("%.1f", food.protein)
                typeface = font
                textSize = 17F
                setBackgroundResource(R.drawable.table_background)
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            }

            val fatTextView = TextView(this).apply {
                text = String.format("%.1f", food.fat)
                typeface = font
                textSize = 17F
                setBackgroundResource(R.drawable.table_background)
                layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
            }

            tableRow.addView(nameTextView)
            tableRow.addView(caloriesTextView)
            tableRow.addView(carbsTextView)
            tableRow.addView(proteinTextView)
            tableRow.addView(fatTextView)

            tableLayout.addView(tableRow)

            totalCalories += food.calories
            totalCarbs += food.carbohydrates
            totalProtein += food.protein
            totalFat += food.fat
        }

        val totalRow = TableRow(this)
        val totalFont = ResourcesCompat.getFont(this, R.font.itim)

        val totalTextView = TextView(this).apply {
            text = "Total"
            typeface = totalFont
            setTypeface(typeface, Typeface.BOLD)
            textSize = 17F
            setBackgroundResource(R.drawable.table_background)
            layoutParams = TableRow.LayoutParams(220, TableRow.LayoutParams.WRAP_CONTENT)
        }

        val totalCaloriesTextView = TextView(this).apply {
            text = String.format("%.1f", totalCalories)
            typeface = totalFont
            textSize = 17F
            setBackgroundResource(R.drawable.table_background)
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
        }

        val totalCarbsTextView = TextView(this).apply {
            text = String.format("%.1f", totalCarbs)
            typeface = totalFont
            textSize = 17F
            setBackgroundResource(R.drawable.table_background)
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
        }

        val totalProteinTextView = TextView(this).apply {
            text = String.format("%.1f", totalProtein)
            typeface = totalFont
            textSize = 17F
            setBackgroundResource(R.drawable.table_background)
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
        }

        val totalFatTextView = TextView(this).apply {
            text = String.format("%.1f", totalFat)
            typeface = totalFont
            textSize = 17F
            setBackgroundResource(R.drawable.table_background)
            layoutParams = TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT)
        }

        totalRow.addView(totalTextView)
        totalRow.addView(totalCaloriesTextView)
        totalRow.addView(totalCarbsTextView)
        totalRow.addView(totalProteinTextView)
        totalRow.addView(totalFatTextView)

        tableLayout.addView(totalRow)

        val cancelButton = findViewById<Button>(R.id.cancel_button)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        cancelButton.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
        }
        confirmButton.setOnClickListener {
            startActivity(Intent(this, Tracking::class.java))
        }
    }

    private fun loadFoodItems(): List<FoodItemAnalysis> {
        val jsonString: String
        try {
            val inputStream: InputStream = assets.open("analysis_results.json")
            jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
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

            return foodList
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}
