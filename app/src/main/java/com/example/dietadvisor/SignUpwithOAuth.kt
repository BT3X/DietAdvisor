package com.example.dietadvisor

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpwithOAuth : AppCompatActivity() {
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

        val dob = findViewById<EditText>(R.id.dob)

        dob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
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

        // Define the data for the dropdown menus
        val genderOptions = arrayOf("Male", "Female", "Non-binary")
        val dietGoalOptions = arrayOf("Lose Weight", "Gain Weight", "Stay the Same")
        val languageOptions = arrayOf("English", "Chinese")

        // Set up the adapters for the dropdowns
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        setupDropdown(genderDropdown, genderAdapter)

        val dietGoalAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dietGoalOptions)
        setupDropdown(dietGoalDropdown, dietGoalAdapter)

        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languageOptions)
        setupDropdown(languageDropdown, languageAdapter)

        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            // Do something here to save info to DB

            startActivity(Intent(this, HomePage::class.java))
        }
    }

    private fun setupDropdown(autoCompleteTextView: AutoCompleteTextView, adapter: ArrayAdapter<String>) {
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
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
