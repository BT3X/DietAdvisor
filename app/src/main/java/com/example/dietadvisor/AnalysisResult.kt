package com.example.dietadvisor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import java.io.InputStream

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
            v.setPadding(systemBars.left, 40, systemBars.right, systemBars.bottom)
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
            val inflater = layoutInflater
            val tableRow = inflater.inflate(R.layout.analysis_row_template, tableLayout, false) as TableRow

            val nameTextView = tableRow.findViewById<TextView>(R.id.name)
            val caloriesTextView = tableRow.findViewById<TextView>(R.id.calories)
            val carbTextView = tableRow.findViewById<TextView>(R.id.carb)
            val proteinTextView = tableRow.findViewById<TextView>(R.id.protein)
            val fatTextView = tableRow.findViewById<TextView>(R.id.fat)

            Log.d("DEBUG", "nameTextView: $nameTextView")
            Log.d("DEBUG", "caloriesTextView: $caloriesTextView")
            Log.d("DEBUG", "carbTextView: $carbTextView")
            Log.d("DEBUG", "proteinTextView: $proteinTextView")
            Log.d("DEBUG", "fatTextView: $fatTextView")

            nameTextView.text = food.name
            caloriesTextView.text = String.format("%.1f", food.calories)
            carbTextView.text = String.format("%.1f", food.carbohydrates)
            proteinTextView.text = String.format("%.1f", food.protein)
            fatTextView.text = String.format("%.1f", food.fat)

            tableLayout.addView(tableRow, tableLayout.childCount - 1)

            totalCalories += food.calories
            totalCarbs += food.carbohydrates
            totalProtein += food.protein
            totalFat += food.fat
        }

        val totalCaloriesTextView = findViewById<TextView>(R.id.total_calories)
        val totalCarbsTextView = findViewById<TextView>(R.id.total_carb)
        val totalProteinTextView = findViewById<TextView>(R.id.total_protein)
        val totalFatTextView = findViewById<TextView>(R.id.total_fat)

        totalCaloriesTextView.text = String.format("%.1f", totalCalories)
        totalCarbsTextView.text = String.format("%.1f", totalCarbs)
        totalProteinTextView.text = String.format("%.1f", totalProtein)
        totalFatTextView.text = String.format("%.1f", totalFat)

        val tableContentSV = findViewById<HorizontalScrollView>(R.id.table_content_sv)
        val tableHeaderSV = findViewById<HorizontalScrollView>(R.id.table_header_sv)

        tableContentSV.setOnScrollChangeListener {_, scrollX, _, _, _ ->
            tableHeaderSV.scrollTo(scrollX, 0)
        }

        tableHeaderSV.setOnScrollChangeListener {_, scrollX, _, _, _ ->
            tableContentSV.scrollTo(scrollX, 0)
        }

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
