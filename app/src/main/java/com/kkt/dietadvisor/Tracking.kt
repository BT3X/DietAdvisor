package com.kkt.dietadvisor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Tracking : AppCompatActivity() {

    private lateinit var authState: AuthState
    private lateinit var authService: AuthorizationService

    private lateinit var jsonString: String

    private lateinit var btnDatePicker: Button
    private lateinit var barChart: BarChart

    // Toggles
    private lateinit var calorieToggle: MaterialSwitch
    private lateinit var carbToggle: MaterialSwitch
    private lateinit var proteinToggle: MaterialSwitch
    private lateinit var fatToggle: MaterialSwitch

    private var intakeData: List<IntakeData> = listOf()

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

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracking)
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
                // Retrieve the user info from network request
                UserInfoUtil.getUserInfo(this, accessToken, client) { userExists, userInfo ->
                    if (userExists) {
                        userInfo?.let {
                            Log.d(TAG, "getUserInfoCallback: Successfully retrieved user info")
                            println(userInfo)

                            loadGreetingText(userInfo)
                            jsonString = userInfo
                        }
                    } else {
                        Log.e(TAG, "getUserInfoCallback: Failure! User does not exist")
                    }
                }
            }
        }

        btnDatePicker = findViewById(R.id.pick_date_button)
        calorieToggle = findViewById(R.id.calorie_toggle)
        carbToggle = findViewById(R.id.carb_toggle)
        proteinToggle = findViewById(R.id.protein_toggle)
        fatToggle = findViewById(R.id.fat_toggle)

        btnDatePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()
            datePicker.show(supportFragmentManager, "DateRangePicker")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection?.first ?: return@addOnPositiveButtonClickListener
                val endDate = selection.second ?: return@addOnPositiveButtonClickListener

                val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
                val startDateString = dateFormatter.format(Date(startDate))
                val endDateString = dateFormatter.format(Date(endDate))

                Toast.makeText(this, "${resources.getString(R.string.select_date_range)} $startDateString ${resources.getString(R.string.to)} $endDateString", Toast.LENGTH_LONG).show()

                // Load and filter the user data based on the selected date range
                intakeData = loadUserInfo(startDateString, endDateString, jsonString)
                updateBarChart(intakeData)
            }

            datePicker.addOnNegativeButtonClickListener {
                Toast.makeText(this, "${datePicker.headerText} ${resources.getString(R.string.is_cancelled)}", Toast.LENGTH_LONG).show()
            }

            datePicker.addOnCancelListener {
                Toast.makeText(this, resources.getString(R.string.date_picker_cancelled), Toast.LENGTH_LONG).show()
            }
        }

        setupChart()

        // Set up toggle listeners
        calorieToggle.setOnCheckedChangeListener { _, _ -> updateBarChart(intakeData) }
        carbToggle.setOnCheckedChangeListener { _, _ -> updateBarChart(intakeData) }
        proteinToggle.setOnCheckedChangeListener { _, _ -> updateBarChart(intakeData) }
        fatToggle.setOnCheckedChangeListener { _, _ -> updateBarChart(intakeData) }

        val homeButton = findViewById<FrameLayout>(R.id.home_button)
        val recommendationsButton = findViewById<FrameLayout>(R.id.recommendations_button)
        val profileButton = findViewById<FrameLayout>(R.id.profile_button)

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

        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun loadUserInfo(startDate: String, endDate: String, jsonString: String): List<IntakeData> {
        val intakeDataList = mutableListOf<IntakeData>()

        try {
            val jsonObject = JSONObject(jsonString)

            val intakeArray = jsonObject.getJSONArray("intakeHistory")

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = dateFormatter.parse(startDate) ?: Date()
            val end = dateFormatter.parse(endDate) ?: Date()

            // Create a map of existing data for quick lookup
            val intakeMap = mutableMapOf<String, IntakeData>()
            for (i in 0 until intakeArray.length()) {
                val intakeObject = intakeArray.getJSONObject(i)
                val intakeDate = intakeObject.getString("date")
                val nutritionalInfo = intakeObject.getJSONObject("nutritionalInfo")
                intakeMap[intakeDate] = IntakeData(
                    date = intakeDate,
                    calorie = nutritionalInfo.getDouble("calorie").toFloat(),
                    protein = nutritionalInfo.getDouble("protein").toFloat(),
                    carb = nutritionalInfo.getDouble("carb").toFloat(),
                    fat = nutritionalInfo.getDouble("fat").toFloat()
                )
            }

            // Generate the list of all dates in the range and fill with data or zeros
            val calendar = Calendar.getInstance()
            calendar.time = start
            while (calendar.time <= end) {
                val dateStr = dateFormatter.format(calendar.time)
                val intakeData = intakeMap[dateStr] ?: IntakeData(date = dateStr, calorie = 0f, protein = 0f, carb = 0f, fat = 0f)
                intakeDataList.add(intakeData)
                calendar.add(Calendar.DATE, 1)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return intakeDataList
    }

    private fun loadGreetingText(jsonString: String) {
        val jsonObject = JSONObject(jsonString)

        // Extract the userName from the personalInfo object
        val personalInfo = jsonObject.getJSONObject("personalInfo")
        val userName = personalInfo.getString("userName")

        // Find the TextView by ID (assuming it's the 'header' TextView in your layout)
        val trackingGreetingText = findViewById<TextView>(R.id.tracking_greeting_text)

        // Set the greeting message with the user's name
        val greetingMessage = "$userName, let's see your eating pattern!"
        runOnUiThread {
            trackingGreetingText.text = greetingMessage
        }
    }

    private fun updateBarChart(intakeData: List<IntakeData>) {
        val calorieEntries = ArrayList<BarEntry>()
        val proteinEntries = ArrayList<BarEntry>()
        val carbEntries = ArrayList<BarEntry>()
        val fatEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        intakeData.forEachIndexed { index, data ->
            if (calorieToggle.isChecked) {
                calorieEntries.add(BarEntry(index.toFloat(), data.calorie))
            }
            if (proteinToggle.isChecked) {
                proteinEntries.add(BarEntry(index.toFloat(), data.protein))
            }
            if (carbToggle.isChecked) {
                carbEntries.add(BarEntry(index.toFloat(), data.carb))
            }
            if (fatToggle.isChecked) {
                fatEntries.add(BarEntry(index.toFloat(), data.fat))
            }
            labels.add(data.date)
        }

        // Calculate group settings based on active toggles
        val activeDataSets = mutableListOf<BarDataSet>()
        var groupSpace = 0.3f
        val barSpace = 0.05f
        var barWidth = 0.125f

        if (calorieToggle.isChecked) activeDataSets.add(BarDataSet(calorieEntries, "Calories").apply {
            color = ContextCompat.getColor(this@Tracking, R.color.greenButton)
            setDrawValues(false)
        })

        if (proteinToggle.isChecked) activeDataSets.add(BarDataSet(proteinEntries, "Protein").apply {
            color = ContextCompat.getColor(this@Tracking, R.color.greenText)
            setDrawValues(false)
        })

        if (carbToggle.isChecked) activeDataSets.add(BarDataSet(carbEntries, "Carb").apply {
            color = ContextCompat.getColor(this@Tracking, R.color.grayText)
            setDrawValues(false)
        })

        if (fatToggle.isChecked) activeDataSets.add(BarDataSet(fatEntries, "Fat").apply {
            color = ContextCompat.getColor(this@Tracking, R.color.grayTextInTextView)
            setDrawValues(false)
        })

        when (activeDataSets.size) {
            3 -> {
                groupSpace = 0.34f
                barWidth = 0.17f
            }
            2 -> {
                groupSpace = 0.4f
                barWidth = 0.25f
            }
            1 -> {
                barWidth = 0.5f
            }
        }

        if (activeDataSets.isEmpty() or intakeData.isEmpty()) {
            barChart.clear() // Clear the chart if no datasets are active
            barChart.invalidate()
            return
        }

        val data = BarData(*activeDataSets.toTypedArray()).apply {
            this.barWidth = barWidth
        }

        barChart.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            xAxis.labelCount = labels.size
            if (activeDataSets.size > 1) {
                xAxis.setCenterAxisLabels(true)
                groupBars(0f, groupSpace, barSpace)
                xAxis.axisMinimum = 0f
                xAxis.axisMaximum = barChart.data.getGroupWidth(groupSpace, barSpace) * labels.size
            } else {
                xAxis.setCenterAxisLabels(false)
                xAxis.axisMinimum = -0.5f
                xAxis.axisMaximum = labels.size + 0f
            }

            setVisibleXRangeMaximum(3f)
            setScaleEnabled(false)
            isDragEnabled = true
            setPinchZoom(false)

            invalidate()
        }
    }

    private fun setupChart() {
        val itimFont = ResourcesCompat.getFont(this, R.font.itim)
        val greenTextColor = ContextCompat.getColor(this, R.color.greenText)

        barChart = findViewById(R.id.barChart)
        barChart.description.isEnabled = false
        barChart.isDragEnabled = true
        barChart.setNoDataTextColor(R.color.greenText)
        barChart.setNoDataTextTypeface(itimFont)

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            isGranularityEnabled = true
            axisMinimum = 0f
            textSize = 15f
            typeface = itimFont
            textColor = greenTextColor
            labelRotationAngle = 45f
            setCenterAxisLabels(true)
        }

        barChart.axisLeft.apply {
            textSize = 15f
            typeface = itimFont
            textColor = greenTextColor
            axisMinimum = 0f
        }

        barChart.axisRight.isEnabled = false

        barChart.legend.apply {
            textSize = 15f
            typeface = itimFont
            textColor = greenTextColor
        }

        barChart.animateY(1000)
        barChart.invalidate()
    }

    companion object {
        const val TAG = "Tracking"
    }
}

data class IntakeData(
    val date: String,
    val calorie: Float,
    val protein: Float,
    val carb: Float,
    val fat: Float
)
