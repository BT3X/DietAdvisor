package com.kkt.dietadvisor

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Init Date Picker Dialogue for Date of Birth
        val dobEditText = findViewById<EditText>(R.id.dob)

        dobEditText.setOnClickListener{
            // Get current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Create and show Date Picker Dialogue
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Update the EditText with the selected date
                    val formatString = "%02d/%02d/%04d"
                    val formattedDate = String.format(formatString, selectedDay, selectedMonth + 1, selectedYear)
                    dobEditText.setText(formattedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Init Gender Dropdown
        val gendersArray = resources.getStringArray(R.array.gender_options)
        val genderAdapter = ArrayAdapter(this, R.layout.dropdown_item, gendersArray)
        val genderDropdown = findViewById<AutoCompleteTextView>(R.id.gender_dropdown)
        genderDropdown.setAdapter(genderAdapter)

        genderDropdown.setOnClickListener {
            genderDropdown.showDropDown()
        }

        // Init Diet Goal Dropdown
        val dietGoalsArray = resources.getStringArray(R.array.diet_goal_options)
        val dietGoalAdapter = ArrayAdapter(this, R.layout.dropdown_item, dietGoalsArray)
        val dietGoalDropdown = findViewById<AutoCompleteTextView>(R.id.diet_goal_dropdown)
        dietGoalDropdown.setAdapter(dietGoalAdapter)

        dietGoalDropdown.setOnClickListener {
            dietGoalDropdown.showDropDown()
        }

        // Init Language Dropdown
        val languageArray = resources.getStringArray(R.array.language_options)
        val languageAdapter = ArrayAdapter(this, R.layout.dropdown_item, languageArray)
        val languageDropdown = findViewById<AutoCompleteTextView>(R.id.language_dropdown)
        languageDropdown.setAdapter(languageAdapter)

        languageDropdown.setOnClickListener {
            languageDropdown.showDropDown()
        }
    }
}