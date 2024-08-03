package com.kkt.dietadvisor.screens

import android.content.Context
import android.content.Intent
import android.media.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkt.dietadvisor.activities.ImageConfirmationActivity
import com.kkt.dietadvisor.ui.theme.DietAdvisorTheme

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Home Screen",
            fontSize = 30.sp
        )

        Button(
            modifier = Modifier
                .padding(vertical = 20.dp),
            onClick = {
                context.startActivity(Intent(context, ImageConfirmationActivity::class.java))
            }
        ) {
            Text(text = "Go to New Activity")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DietAdvisorTheme {
        HomeScreen()
    }
}