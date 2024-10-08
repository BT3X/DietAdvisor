package com.kkt.dietadvisor

import android.app.DatePickerDialog
import android.content.Intent
import android.media.session.MediaSession.Token
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.kkt.dietadvisor.utility.AuthStateUtil
import com.kkt.dietadvisor.utility.TokenStateUtil
import com.kkt.dietadvisor.utility.UserInfoUtil
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Calendar
import java.util.concurrent.TimeUnit

class UserProfile : AppCompatActivity() {

    private lateinit var authState: AuthState
    private lateinit var authService: AuthorizationService

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
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize AuthorizationService & AuthState
        authService = AuthorizationService(this)
        authState = AuthStateUtil.readAuthState(this)

        TokenStateUtil.checkAndRenewAccessToken(this, authState, authService) { _ ->
            val accessToken = authState.accessToken
            accessToken?.let {
                // Fetch and display user information
                UserInfoUtil.getUserInfo(this, accessToken, client) { userExists, userInfo ->
                    if (userExists) {
                        userInfo?.let {
                            runOnUiThread {
                                Log.d(TAG, "onCreate: Loading UI with user info...")
                                loadUserInfo(jsonString = userInfo)
                            }
                        }
                    } else {
                        Log.e(TAG, "getUserInfoCallback: Failure! User does not exist", )
                    }
                }
            }
        }

        // Navigation buttons
        val homeButton = findViewById<FrameLayout>(R.id.home_button)
        val recommendationsButton = findViewById<FrameLayout>(R.id.recommendations_button)
        val trackingButton = findViewById<FrameLayout>(R.id.tracking_button)

        homeButton.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        recommendationsButton.setOnClickListener {
            val intent = Intent(this, Recommendations::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        trackingButton.setOnClickListener {
            val intent = Intent(this, Tracking::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Sign out button
        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            // Clear AuthState and erase tokens
            TokenStateUtil.signOut(this)

            // Redirect the user to the login screen and clear the back stack
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
        val changeActivityLevel = findViewById<ImageView>(R.id.change_activity_level)

        changeUsername.setOnClickListener {
            showEditTextDialog(resources.getString(R.string.edit_username), findViewById<TextView>(R.id.username).text.toString()) { newValue ->
                findViewById<TextView>(R.id.username).text = newValue
            }
        }

        changeDOB.setOnClickListener {
            showDatePickerDialog { newValue ->
                findViewById<TextView>(R.id.dob).text = newValue
            }
        }

        changeGender.setOnClickListener {
            val genderOptions = resources.getStringArray(R.array.gender_options)
            showDropdownMenuDialog(resources.getString(R.string.select_gender), genderOptions, findViewById<TextView>(R.id.gender).text.toString()) { newValue ->
                findViewById<TextView>(R.id.gender).text = newValue
            }
        }

        changeWeight.setOnClickListener {
            showNumberInputDialog(resources.getString(R.string.edit_weight), findViewById<TextView>(R.id.weight).text.toString()) { newValue ->
                findViewById<TextView>(R.id.weight).text = newValue
            }
        }

        changeHeight.setOnClickListener {
            showNumberInputDialog(resources.getString(R.string.edit_height), findViewById<TextView>(R.id.height).text.toString()) { newValue ->
                findViewById<TextView>(R.id.height).text = newValue
            }
        }

        changeDietGoal.setOnClickListener {
            val dietGoalOptions = resources.getStringArray(R.array.diet_goal_options)
            showDropdownMenuDialog(resources.getString(R.string.select_diet_goal), dietGoalOptions, findViewById<TextView>(R.id.diet_goal).text.toString()) { newValue ->
                findViewById<TextView>(R.id.diet_goal).text = newValue
            }
        }

        changeActivityLevel.setOnClickListener {
            val activityOptions = resources.getStringArray(R.array.activity_level_options)
            showDropdownMenuDialog(resources.getString(R.string.select_activity_level), activityOptions, findViewById<TextView>(R.id.activity_level).text.toString()) { newValue ->
                findViewById<TextView>(R.id.activity_level).text = newValue
            }
        }

        changeLanguage.setOnClickListener {
            val languageOptions = resources.getStringArray(R.array.language_options)
            showDropdownMenuDialog(resources.getString(R.string.select_language), languageOptions, findViewById<TextView>(R.id.language).text.toString()) { newValue ->
                findViewById<TextView>(R.id.language).text = newValue
            }
        }
    }

    private fun loadUserInfo(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)

            val personalInfo = jsonObject.getJSONObject("personalInfo")
            val bodyMeasurements = jsonObject.getJSONObject("bodyMeasurements")
            val dietaryInfo = jsonObject.getJSONObject("dietaryInfo")

            // Set the user info fields
            findViewById<TextView>(R.id.username).text = personalInfo.getString("userName")
            findViewById<TextView>(R.id.dob).text = personalInfo.getString("birthDate")
            findViewById<TextView>(R.id.gender).text = personalInfo.getString("gender")

            findViewById<TextView>(R.id.height).text = bodyMeasurements.getInt("height").toString()
            findViewById<TextView>(R.id.weight).text = bodyMeasurements.getInt("weight").toString()

            findViewById<TextView>(R.id.diet_goal).text = dietaryInfo.getString("dietaryGoal")
            findViewById<TextView>(R.id.activity_level).text = mapActivityLevelToString(bodyMeasurements.getInt("physicalActivity"))
            findViewById<TextView>(R.id.language).text = personalInfo.getString("language")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapActivityLevelToString(activityLevel: Int): String {
        return when (activityLevel) {
            0 -> "Sedentary"
            1 -> "Lightly Active"
            2 -> "Moderately Active"
            3 -> "Very Active"
            4 -> "Extra Active"
            else -> "Unknown"
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
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

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
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showDropdownMenuDialog(title: String, options: Array<String>, currentValue: String, callback: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setSingleChoiceItems(options, options.indexOf(currentValue)) { dialog, which ->
            callback(options[which])
            dialog.dismiss()
        }

        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    companion object {
        const val TAG = "UserProfile"
    }
}
