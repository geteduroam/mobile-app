package app.eduroam.geteduroam

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import app.eduroam.geteduroam.config.WifiConfigScreen
import app.eduroam.geteduroam.config.WifiConfigViewModel
import app.eduroam.geteduroam.oauth.OAuthScreen
import app.eduroam.geteduroam.oauth.OAuthViewModel
import app.eduroam.geteduroam.organizations.SelectOrganizationScreen
import app.eduroam.geteduroam.organizations.SelectOrganizationViewModel
import app.eduroam.geteduroam.profile.SelectProfileScreen
import app.eduroam.geteduroam.profile.SelectProfileViewModel
import app.eduroam.geteduroam.webview_fallback.WebViewFallbackScreen
import app.eduroam.geteduroam.webview_fallback.WebViewFallbackViewModel

const val BASE_URI = "https://eduroam.org"
@Composable
fun MainGraph(
    navController: NavHostController = rememberNavController(),
    openFileUri: (Uri) -> Unit,
    closeApp: () -> Unit
) : NavController {
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
                    navController.navigate(Route.SelectProfile.encodeInstitutionIdArgument(institutionId))
                },
                goToOAuth = { configuration ->
                    navController.navigate(
                        Route.OAuth.encodeArguments(
                            configuration = configuration,
                            redirectUri = null
                        )
                    )
                },
                goToConfigScreen = { organizationId, wifiConfigData ->
                    navController.popBackStack()
                    navController.navigate(
                        Route.ConfigureWifi.encodeArguments(
                            organizationId, wifiConfigData
                        )
                    )
                },
                openFileUri = openFileUri,
                discoverUrl = {
                    navController.navigate(Route.SelectProfile.encodeCustomHostArgument(it))
                }
            )
        }
        composable(
            route = Route.SelectProfile.routeWithArgs,
            arguments = Route.SelectProfile.arguments,
            deepLinks = listOf(navDeepLink {
                uriPattern = Route.SelectProfile.deepLinkUrl
            })
        ) { entry ->
            val viewModel = hiltViewModel<SelectProfileViewModel>(entry)
            SelectProfileScreen(
                viewModel = viewModel,
                goToOAuth = { configuration ->
                    navController.navigate(Route.OAuth.encodeArguments(configuration, null))
                },
                goToConfigScreen = { organizationId, provider ->
                    navController.navigate(
                        Route.ConfigureWifi.encodeArguments(organizationId, provider)
                    )
                },
                goToPrevious = {
                    navController.popBackStack()
                })
        }
        composable(
            route = Route.OAuth.routeWithArgs, arguments = Route.OAuth.arguments
        ) { _ ->
            val viewModel = hiltViewModel<OAuthViewModel>()
            OAuthScreen(
                viewModel = viewModel,
                goToPrevious = {
                    navController.popBackStack()
                },
                goToWebViewFallback = { configuration, navigationUri ->
                    navController.navigate(
                        Route.WebViewFallback.encodeArguments(configuration, navigationUri)
                    )
                }
            )
        }
        composable(
            route = Route.WebViewFallback.routeWithArgs, arguments = Route.WebViewFallback.arguments
        ) { _ ->
            val viewModel = hiltViewModel<WebViewFallbackViewModel>()
            WebViewFallbackScreen(
                viewModel = viewModel,
                onRedirectUriFound = { configuration, uri ->
                    navController.popBackStack() // this screen
                    navController.popBackStack() // OAuth screen
                    navController.navigate(Route.OAuth.encodeArguments(configuration, uri)) // OAuth screen again
                },
                onCancel = {
                    navController.navigateUp() // OAuth screen
                    navController.navigateUp() // Profile screen
                }
            )
        }

        composable(
            route = Route.ConfigureWifi.routeWithArgs, arguments = Route.ConfigureWifi.arguments,
            deepLinks = listOf(navDeepLink {
                uriPattern = Route.ConfigureWifi.deepLinkUrl
            })
        ) { backStackEntry ->
            val wifiConfigData = Route.ConfigureWifi.decodeUrlArgument(backStackEntry.arguments)
            val organizationId = Route.ConfigureWifi.decodeOrganizationIdArgument(backStackEntry.arguments)
            val viewModel = hiltViewModel<WifiConfigViewModel>()
            viewModel.eapIdentityProviderList = wifiConfigData
            viewModel.organizationId = organizationId
            WifiConfigScreen(
                viewModel,
                closeApp = closeApp,
                goBack = {
                    navController.navigateUp()
                }
            )
        }
    }
    return navController
}