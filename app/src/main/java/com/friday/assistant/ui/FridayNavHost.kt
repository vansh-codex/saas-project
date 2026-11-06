package com.friday.assistant.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.friday.assistant.di.AppContainer
import com.friday.assistant.ui.home.HomeScreen
import com.friday.assistant.ui.settings.SettingsScreen

object FridayRoutes {
    const val HOME: String = "home"
    const val SETTINGS: String = "settings"
}

@Composable
fun FridayNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FridayRoutes.HOME,
        modifier = modifier,
    ) {
        composable(FridayRoutes.HOME) {
            HomeScreen(
                container = container,
                onOpenSettings = { navController.navigate(FridayRoutes.SETTINGS) },
            )
        }
        composable(FridayRoutes.SETTINGS) {
            SettingsScreen(
                container = container,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
