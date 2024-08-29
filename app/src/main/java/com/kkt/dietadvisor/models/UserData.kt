package com.kkt.dietadvisor.models

// Classes to match the JSON schema on the backend
data class UserData(
    val personalInfo: PersonalInfo,
    val bodyMeasurements: BodyMeasurements,
    val dietaryInfo: DietaryInfo,
    val intakeHistory: List<IntakeEntry>,  // Added this to match the intakeHistory array
    val lastMeal: NutritionalInfo?,         // Updated this to reflect the new structure
)

data class PersonalInfo(
    val userID: String,
    val userName: String,
    val birthDate: String,
    val gender: String,
    val language: String,
)

data class BodyMeasurements(
    val weight: Double,
    val height: Double,
    val physicalActivity: Double,
)

data class DietaryInfo(
    val dietaryGoal: String,
    val dietaryGoalAmount: Double,
    val TMR: Double,
    val TDEE: Double,
)

data class IntakeEntry(
    // Added this class to match the intakeHistory array
    val date: String,
    val nutritionalInfo: NutritionalInfo,
)

data class NutritionalInfo(
    // Reused this class for both intakeHistory and lastMeal
    val carb: Double,
    val protein: Double,
    val fat: Double,
    val calorie: Double,
)
