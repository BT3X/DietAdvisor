package com.kkt.dietadvisor.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kkt.dietadvisor.R
import com.kkt.dietadvisor.screens.ImageConfirmationScreen
import com.kkt.dietadvisor.ui.theme.DietAdvisorTheme

@OptIn(ExperimentalMaterial3Api::class)
class ImageConfirmationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DietAdvisorTheme {
                ImageConfirmationScreen()
            }
        }
    }
}