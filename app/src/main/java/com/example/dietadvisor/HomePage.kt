package com.example.dietadvisor

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ir.mahozad.android.PieChart

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        getSupportActionBar()?.hide()
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
            startActivity(Intent(this, profile::class.java))
        }

        val caloriePC = findViewById<PieChart>(R.id.calorie_pc)
        caloriePC.apply {
            slices = listOf(
                PieChart.Slice(0.56f, resources.getColor(R.color.greenButton)),
                PieChart.Slice(0.44f, resources.getColor(R.color.grayTextInTextView))
            )
            labelType = PieChart.LabelType.NONE
        }
        val calorieInfo = findViewById<TextView>(R.id.calorie_info)
        calorieInfo.text = "1590kcal\n56%"

        val carbPC = findViewById<PieChart>(R.id.carb_pc)
        carbPC.apply {
            slices = listOf(
                PieChart.Slice(0.7f, resources.getColor(R.color.greenButton)),
                PieChart.Slice(0.3f, resources.getColor(R.color.grayTextInTextView))
            )
            labelType = PieChart.LabelType.NONE
        }
        val carbInfo = findViewById<TextView>(R.id.carb_info)
        carbInfo.text = "227.5g\n70%"

        val proteinPC = findViewById<PieChart>(R.id.protein_pc)
        proteinPC.apply {
            slices = listOf(
                PieChart.Slice(0.61f, resources.getColor(R.color.greenButton)),
                PieChart.Slice(0.39f, resources.getColor(R.color.grayTextInTextView))
            )
            labelType = PieChart.LabelType.NONE
        }
        val proteinInfo = findViewById<TextView>(R.id.protein_info)
        proteinInfo.text = "90.7g\n61%"

        val fatPC = findViewById<PieChart>(R.id.fat_pc)
        fatPC.apply {
            slices = listOf(
                PieChart.Slice(0.75f, resources.getColor(R.color.greenButton)),
                PieChart.Slice(0.25f, resources.getColor(R.color.grayTextInTextView))
            )
            labelType = PieChart.LabelType.NONE
        }
        val fatInfo = findViewById<TextView>(R.id.fat_info)
        fatInfo.text = "40g\n75%"
    }
}