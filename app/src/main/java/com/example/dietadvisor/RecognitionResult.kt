package com.example.dietadvisor

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class FoodItem(var name: String, val index: Int)

class RecognitionResult : AppCompatActivity() {
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

        val foodItems = loadFoodItems()
        println("Food items count: ${foodItems.size}")

        val tableLayout = findViewById<TableLayout>(R.id.recognized_food_items)

        for ((index, food) in foodItems.withIndex()) {
            val tableRow = TableRow(this)
            val font = ResourcesCompat.getFont(this, R.font.itim)
            val idTextView = TextView(this).apply {
                text = (index + 1).toString()
                gravity = android.view.Gravity.CENTER
                typeface = font
                textSize = 17F
//                setBackgroundResource(R.drawable.table_background)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(16, 8, 16, 8)
            }

            val nameTextView = TextView(this).apply {
                text = food.name
                typeface = font
                textSize = 17F
//                setBackgroundResource(R.drawable.table_background)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                setPadding(16, 8, 16, 8)
            }

            val changeTextView = TextView(this).apply {
                val changeText = "Change"
                val spannableString = SpannableString(changeText)
                spannableString.setSpan(UnderlineSpan(), 0, changeText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text = spannableString
                gravity = android.view.Gravity.CENTER
//                setBackgroundResource(R.drawable.table_background)
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
            startActivity(Intent(this, HomePage::class.java))
        }
        confirmButton.setOnClickListener {
            updateJsonFile(foodItems)
            startActivity(Intent(this, AnalysisResult::class.java))
        }
    }

    private fun loadFoodItems(): List<FoodItem> {
        val jsonString: String
        try {
            val inputStream: InputStream = assets.open("recognizedFoodItems.json")
            jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
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
        val foodOptions = arrayOf(
            "Kabayaki sea bream fillet", "Spam", "apple -sliced-", "cabbage", "creamy tofu",
            "creamy tofu -without sauce-", "cucumber", "egg tofu", "firm tofu", "fish cake",
            "fried chicken cutlet", "fried potato", "grilled pork", "guava -sliced-", "mustard greens",
            "pig blood curd", "pig liver", "pineapple", "pumpkin", "red grilled pork", "soy egg",
            "sweet potato leaves"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Food Item")
            .setItems(foodOptions) { _, which ->
                val selectedName = foodOptions[which]
                nameTextView.text = selectedName
                foodItem.name = selectedName
            }
            .show()
    }

    private fun updateJsonFile(foodItems: List<FoodItem>) {
        try {
            val file = File(filesDir, "recognizedFoodItems.json")
            val inputStream: InputStream = assets.open("recognizedFoodItems.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (foodItem in foodItems) {
                val jsonObject = jsonArray.getJSONObject(foodItem.index)
                jsonObject.put("name", foodItem.name)
            }

            val outputStream = FileOutputStream(file)
            outputStream.write(jsonArray.toString().toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
