package app.eduroam.geteduroam

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.eduroam.geteduroam.config.WifiConfigScreen
import app.eduroam.geteduroam.config.WifiConfigViewModel
import app.eduroam.geteduroam.institutions.SelectInstitutionScreen
import app.eduroam.geteduroam.oauth.OAuthScreen
import app.eduroam.geteduroam.oauth.OAuthViewModel
import app.eduroam.geteduroam.oauth.OpenIdOAuthScreen
import app.eduroam.geteduroam.profile.SelectProfileScreen
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger

@Composable
fun NavGraph(
    viewModel: SelectInstitutionViewModel,
    profileViewModel: SelectProfileViewModel,
    oauthViewModel: OAuthViewModel,
    log: Logger
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController, startDestination = Screens.SelectInstitution.route
    ) {
        composable(Screens.SelectInstitution.route) {
            SelectInstitutionScreen(
                viewModel = viewModel,
                goToOAuth = { authorizationUrl, profile ->
                    navController.navigate(
                        Screens.OAuth.encodeArguments(
                            url = authorizationUrl,
                            profile = profile
                        )
                    )
                },
                gotToProfileSelection = { institutionJson ->
                    navController.navigate(
                        Screens.SelectProfile.encodeArguments(
                            institutionJson
                        )
                    )
                },
                goToConfigScreen = { wifiConfigData ->
                    navController.navigate(
                        Screens.ConfigureWifi.encodeArguments(
                            wifiConfigData
                        )
                    )
                })
        }
        composable(
            route = Screens.SelectProfile.routeWithArgs, arguments = Screens.SelectProfile.arguments
        ) { backStackEntry ->
            val institution = Screens.SelectProfile.decodeUrlArgument(backStackEntry.arguments)
            profileViewModel.profilesForInstitution(institution)
            SelectProfileScreen(
                viewModel = profileViewModel,
                goToOAuth = { authorizationUrl, profile ->
                    navController.navigate(
                        Screens.OAuth.encodeArguments(
                            url = authorizationUrl,
                            profile = profile
                        )
                    )
                },
                goToConfigScreen = { wifiConfigData ->
                    navController.navigate(
                        Screens.ConfigureWifi.encodeArguments(
                            wifiConfigData
                        )
                    )
                })
        }
        composable(
            route = Screens.ConfigureWifi.routeWithArgs, arguments = Screens.ConfigureWifi.arguments
        ) { backStackEntry ->
            val wifiConfigData = Screens.ConfigureWifi.decodeUrlArgument(backStackEntry.arguments)
            val viewModel = WifiConfigViewModel(wifiConfigData)
            WifiConfigScreen(viewModel)
        }
        composable(
            route = Screens.OAuth.routeWithArgs, arguments = Screens.OAuth.arguments
        ) { backStackEntry ->
            OpenIdOAuthScreen(
                institutionId = "cat_7016",
                url = Screens.OAuth.decodeUrlArgument(
                    backStackEntry.arguments
                ),
                profile = Screens.OAuth.decodeProfileArgument(
                    backStackEntry.arguments
                ),
                viewModel = oauthViewModel,
                goToConfigScreen = { wifiConfigData ->
                    navController.navigate(
                        Screens.ConfigureWifi.encodeArguments(
                            wifiConfigData
                        )
                    )
                }
            )
        }
    }
}