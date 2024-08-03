package com.kkt.dietadvisor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kkt.dietadvisor.models.BottomNavigationItem
import com.kkt.dietadvisor.models.NavBarScreen
import com.kkt.dietadvisor.screens.HomeScreen
import com.kkt.dietadvisor.screens.RecommendationScreen
import com.kkt.dietadvisor.screens.TrackingScreen
import com.kkt.dietadvisor.ui.theme.DietAdvisorTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DietAdvisorTheme {
                // Init Navbar items
                val navItems = listOf(
                    BottomNavigationItem(
                        title = stringResource(id = R.string.navbar_home),
                        destinationScreen = NavBarScreen.HomeScreen,
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        title = stringResource(id = R.string.navbar_Tracking),
                        destinationScreen = NavBarScreen.TrackingScreen,
                        selectedIcon = Icons.Filled.BookmarkAdd,
                        unselectedIcon = Icons.Outlined.BookmarkAdd,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        title = stringResource(id = R.string.navbar_Recommend),
                        destinationScreen = NavBarScreen.RecommendationScreen,
                        selectedIcon = Icons.AutoMirrored.Filled.Chat,
                        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
                        hasNews = false,
                    ),
                )

                var selectedItemIndex by rememberSaveable {
                    mutableIntStateOf(0) // Select home tab on start
                }

                // Instantiate Nav Controller (TODO: Encapsulate with NavHost into a class)
                val navController = rememberNavController()

                // TODO: Move NavBar & NavHost init code into a Factory Class
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = R.string.app_name)
                                )
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedItemIndex == index,
                                    onClick = {
                                        selectedItemIndex = index
                                        navController.navigate(item.destinationScreen)
                                    },
                                    label = {
                                        Text(text = item.title)
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (item.badgeCount != null) {
                                                    Badge {
                                                        Text(text = item.badgeCount.toString())
                                                    }
                                                } else if (item.hasNews) {
                                                    Badge() // Empty badge
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (index == selectedItemIndex) {
                                                    item.selectedIcon
                                                } else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Init NavHost
                    NavHost(
                        navController = navController,
                        startDestination = NavBarScreen.HomeScreen,
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        composable<NavBarScreen.HomeScreen> {
                            HomeScreen()
                        }
                        composable<NavBarScreen.TrackingScreen> {
                            TrackingScreen()
                        }
                        composable<NavBarScreen.RecommendationScreen> {
                            RecommendationScreen()
                        }
                    }
                }
            }
        }
    }
}