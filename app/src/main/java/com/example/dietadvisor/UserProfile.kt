package com.example.dietadvisor

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.InputStream
import java.util.Calendar

class UserProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fetch and display user information
        loadUserInfo()

        // Navigation buttons
        val homeButton = findViewById<FrameLayout>(R.id.home_button)
        val recommendationsButton = findViewById<FrameLayout>(R.id.recommendations_button)
        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)

        homeButton.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
        }

        recommendationsButton.setOnClickListener {
            startActivity(Intent(this, Recommendations::class.java))
        }

        trackingButton.setOnClickListener {
            startActivity(Intent(this, Tracking::class.java))
        }

        // Sign out button
        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        // Edit information
        val changeUsername = findViewById<ImageView>(R.id.change_username)
        val changeDOB = findViewById<ImageView>(R.id.change_dob)
        val changeGender = findViewById<ImageView>(R.id.change_gender)
        val changeWeight = findViewById<ImageView>(R.id.change_weight)
        val changeHeight = findViewById<ImageView>(R.id.change_height)
        val changeDietGoal = findViewById<ImageView>(R.id.change_diet_goal)
        val changeLanguage = findViewById<ImageView>(R.id.change_language)

        changeUsername.setOnClickListener {
            showEditTextDialog("Edit Username", findViewById<TextView>(R.id.username).text.toString()) { newValue ->
                findViewById<TextView>(R.id.username).text = newValue
            }
        }

        changeDOB.setOnClickListener {
            showDatePickerDialog { newValue ->
                findViewById<TextView>(R.id.dob).text = newValue
            }
        }

        changeGender.setOnClickListener {
            val genderOptions = arrayOf("Male", "Female", "Other")
            showDropdownMenuDialog("Select Gender", genderOptions, findViewById<TextView>(R.id.gender).text.toString()) { newValue ->
                findViewById<TextView>(R.id.gender).text = newValue
            }
        }

        changeWeight.setOnClickListener {
            showNumberInputDialog("Edit Weight (kg)", findViewById<TextView>(R.id.weight).text.toString()) { newValue ->
                findViewById<TextView>(R.id.weight).text = newValue
            }
        }

        changeHeight.setOnClickListener {
            showNumberInputDialog("Edit Height (cm)", findViewById<TextView>(R.id.height).text.toString()) { newValue ->
                findViewById<TextView>(R.id.height).text = newValue
            }
        }

        changeDietGoal.setOnClickListener {
            val dietGoalOptions = arrayOf("Lose Weight", "Gain Weight", "Stay the same")
            showDropdownMenuDialog("Select Diet Goal", dietGoalOptions, findViewById<TextView>(R.id.diet_goal).text.toString()) { newValue ->
                findViewById<TextView>(R.id.diet_goal).text = newValue
            }
        }

        changeLanguage.setOnClickListener {
            val languageOptions = arrayOf("English", "Chinese")
            showDropdownMenuDialog("Select Language", languageOptions, findViewById<TextView>(R.id.language).text.toString()) { newValue ->
                findViewById<TextView>(R.id.language).text = newValue
            }
        }
    }

    private fun loadUserInfo() {
        try {
            val inputStream: InputStream = assets.open("userInfo.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            findViewById<TextView>(R.id.username).text = jsonObject.getString("Username")
            findViewById<TextView>(R.id.dob).text = jsonObject.getString("Date of Birth")
            findViewById<TextView>(R.id.gender).text = jsonObject.getString("Gender")
            findViewById<TextView>(R.id.height).text = jsonObject.getString("Height")
            findViewById<TextView>(R.id.weight).text = jsonObject.getString("Weight")
            findViewById<TextView>(R.id.diet_goal).text = jsonObject.getString("Dietary Goal")
            findViewById<TextView>(R.id.language).text = jsonObject.getString("Language")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Dialog methods for editing information
    private fun showEditTextDialog(title: String, currentValue: String, callback: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(currentValue)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            callback(input.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showDatePickerDialog(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$year-${month + 1}-$dayOfMonth"
                callback(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showNumberInputDialog(title: String, currentValue: String, callback: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(currentValue)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            callback(input.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showDropdownMenuDialog(title: String, options: Array<String>, currentValue: String, callback: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setSingleChoiceItems(options, options.indexOf(currentValue)) { dialog, which ->
            callback(options[which])
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
