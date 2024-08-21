package com.example.dietadvisor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }
}