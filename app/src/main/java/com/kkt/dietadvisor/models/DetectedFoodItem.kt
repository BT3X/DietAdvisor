package com.kkt.dietadvisor.models

import androidx.compose.ui.graphics.painter.Painter

data class DetectedFoodItem(
//    val imageUrl: String,  // This is what would actually be used when pulled from DB
    val image: Painter,
    val foodName: String,
)