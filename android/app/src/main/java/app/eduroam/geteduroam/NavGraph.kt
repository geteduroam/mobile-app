package app.eduroam.geteduroam

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.eduroam.geteduroam.welcome.Welcome

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = OnboardingRoute
    ) {
        composable(OnboardingRoute) { Welcome() }
    }
}

private const val OnboardingRoute = "onboarding"
