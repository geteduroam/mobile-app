package app.eduroam.geteduroam

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.eduroam.geteduroam.institutions.SelectInstitutionScreen
import app.eduroam.geteduroam.profile.SelectProfileScreen
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger

@Composable
fun NavGraph(viewModel: SelectInstitutionViewModel, profileViewModel: SelectProfileViewModel, log: Logger) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screens.SelectInstitution.route
    ) {
        composable(Screens.SelectInstitution.route) {
            SelectInstitutionScreen(
                viewModel = viewModel,
                gotToProfileSelection = { it -> navController.navigate("${Screens.SelectProfile.route}/$it") }
            )
        }
        composable(
            route = Screens.SelectProfile.routeWithArgs,
            arguments = Screens.SelectProfile.arguments
        ) { backStackEntry ->
            val institution = backStackEntry.arguments?.getString(Screens.SelectProfile.institution).orEmpty()
            profileViewModel.profilesForInstitution(institution)
            SelectProfileScreen(viewModel = profileViewModel, gotToProfileSelection = {})
        }
    }
}