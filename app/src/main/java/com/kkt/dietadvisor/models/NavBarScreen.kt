package com.kkt.dietadvisor.models

import kotlinx.serialization.Serializable

sealed class NavBarScreen {
    @Serializable
    data object HomeScreen : NavBarScreen()

    @Serializable
    data object TrackingScreen : NavBarScreen()

    @Serializable
    data object RecommendationScreen : NavBarScreen()
}
