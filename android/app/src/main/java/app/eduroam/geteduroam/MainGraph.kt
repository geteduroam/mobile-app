package app.eduroam.geteduroam

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.eduroam.geteduroam.config.WifiConfigScreen
import app.eduroam.geteduroam.config.WifiConfigViewModel
import app.eduroam.geteduroam.oauth.OAuthScreen
import app.eduroam.geteduroam.oauth.OAuthViewModel
import app.eduroam.geteduroam.organizations.SelectOrganizationScreen
import app.eduroam.geteduroam.organizations.SelectOrganizationViewModel
import app.eduroam.geteduroam.profile.SelectProfileScreen
import app.eduroam.geteduroam.profile.SelectProfileViewModel

@Composable
fun MainGraph(
    navController: NavHostController = rememberNavController(),
) {

    NavHost(
        navController = navController, startDestination = Route.SelectInstitution.route
    ) {
        composable(Route.SelectInstitution.route) { entry ->
            val viewModel = hiltViewModel<SelectOrganizationViewModel>(entry)
            val focusManager = LocalFocusManager.current
            SelectOrganizationScreen(
                viewModel = viewModel,
                openProfileModal = { institutionId ->
                    // Remove the focus from the search field (if it was there)
                    focusManager.clearFocus(force = true)
                    navController.navigate(Route.SelectProfile.encodeArgument(institutionId))
                },
                goToOAuth = { configuration ->
                    navController.navigate(
                        Route.OAuth.encodeArguments(
                            configuration = configuration
                        )
                    )
                },
                goToConfigScreen = { wifiConfigData ->
                    navController.navigate(
                        Route.ConfigureWifi.encodeArguments(
                            wifiConfigData,
                        )
                    )
                },
            )
        }
        composable(
            route = Route.SelectProfile.routeWithArgs,
            arguments = Route.SelectProfile.arguments,
        ) { entry ->
            val viewModel = hiltViewModel<SelectProfileViewModel>(entry)
            SelectProfileScreen(
                viewModel = viewModel,
                goToOAuth = { configuration ->
                    navController.navigate(Route.OAuth.encodeArguments(configuration))
                },
                goToConfigScreen = { provider ->
                    navController.navigate(
                        Route.ConfigureWifi.encodeArguments(provider)
                    )
                },
                goToPrevious = {
                    navController.popBackStack()
                })
        }
        composable(
            route = Route.OAuth.routeWithArgs, arguments = Route.OAuth.arguments
        ) { entry ->
            val viewModel = hiltViewModel<OAuthViewModel>()
            OAuthScreen(viewModel = viewModel, goToPrevious = {
                navController.popBackStack()
            })
        }
        composable(
            route = Route.ConfigureWifi.routeWithArgs, arguments = Route.ConfigureWifi.arguments
        ) { backStackEntry ->
            val wifiConfigData = Route.ConfigureWifi.decodeUrlArgument(backStackEntry.arguments)
            val viewModel = WifiConfigViewModel(wifiConfigData)
            WifiConfigScreen(viewModel)
        }
    }
}