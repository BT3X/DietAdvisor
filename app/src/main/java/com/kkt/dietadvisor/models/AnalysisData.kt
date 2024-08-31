package com.kkt.dietadvisor.models

data class NutritionFacts(
    val serving_size: Double,
    val energy: Double,
    val fat: Double,
    val carbohydrates: Double,
    val protein: Double
)

data class AnalyzedFoodItem(
    val name: String,
    val mass: Double
)

data class CalculatedNutritionData(
    val calories: Double,
    val carbohydrates: Double,
    val protein: Double,
    val fat: Double
)

data class FinalizedNutritionData(
    val name: String,
    val nutrition: CalculatedNutritionData
)
