package com.kkt.dietadvisor.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kkt.dietadvisor.R
import com.kkt.dietadvisor.models.DetectedFoodItem
import com.kkt.dietadvisor.ui.theme.DietAdvisorTheme
import com.kkt.dietadvisor.views.DetectedFoodListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageConfirmationScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.confirmation_recognize_food_items)) // TODO: Replace with string resource
                    }
                },
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_confirm_static),
                contentDescription = "Example Confirmation Image"
            )

            Spacer(modifier = Modifier.size(10.dp))

            // For testing purposes only. This is actually read from some JSON file from the server
            val detectedFoods = List(10) { index ->
                DetectedFoodItem(
                    image = painterResource(id = R.drawable.ic_launcher_foreground), // Change res
                    foodName = "Food Item ${index + 1}"
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                items(detectedFoods) { foodItem ->
                    DetectedFoodListItem(foodItem = foodItem)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                    ) {
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageConfirmationPreview() {
    DietAdvisorTheme {
        ImageConfirmationScreen()
    }
}