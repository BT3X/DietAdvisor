package com.kkt.dietadvisor.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.kkt.dietadvisor.ui.theme.DietAdvisorTheme

@Composable
fun RecommendationScreen() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Recommendation Screen",
            fontSize = 30.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecommendationScreenPreview() {
    DietAdvisorTheme {
        RecommendationScreen()
    }
}

