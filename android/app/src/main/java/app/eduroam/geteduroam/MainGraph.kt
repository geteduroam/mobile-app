package app.eduroam.geteduroam

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import app.eduroam.geteduroam.config.WifiConfigScreen
import app.eduroam.geteduroam.config.WifiConfigViewModel
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.oauth.OAuthScreen
import app.eduroam.geteduroam.oauth.OAuthViewModel
import app.eduroam.geteduroam.organizations.SelectOrganizationScreen
import app.eduroam.geteduroam.organizations.SelectOrganizationViewModel
import app.eduroam.geteduroam.profile.SelectProfileScreen
import app.eduroam.geteduroam.profile.SelectProfileViewModel
import app.eduroam.geteduroam.status.ConfigSource
import app.eduroam.geteduroam.status.StatusScreen
import app.eduroam.geteduroam.status.StatusScreenViewModel
import app.eduroam.geteduroam.webview_fallback.WebViewFallbackScreen
import app.eduroam.geteduroam.webview_fallback.WebViewFallbackViewModel
import kotlin.reflect.typeOf

@Composable
fun MainGraph(
    navController: NavHostController = rememberNavController(),
    openFileUri: (Uri) -> Unit,
    closeApp: () -> Unit
) : NavController {
    NavHost(
        navController = navController, startDestination = Route.StatusScreen
    ) {
        composable<Route.StatusScreen>(NavTypes.allTypesMap) { entry ->
            val viewModel = hiltViewModel<StatusScreenViewModel>(entry)
            StatusScreen(
                viewModel = viewModel,
                goToInstitutionSelection = {
                    navController.navigate(Route.SelectInstitution)
                },
                renewAccount = { organizationId ->
                    navController.navigate(Route.SelectProfile(institutionId = organizationId, customHostUri = null))
                },
                repairConfig = { source, organizationId, organizationName, eapIdentityProviderList ->
                    navController.navigate(Route.ConfigureWifi(
                        source = source,
                        organizationId = organizationId,
                        organizationName = organizationName,
                        eapIdentityProviderList = eapIdentityProviderList
                    ))
                })

        }
        composable<Route.SelectInstitution>(NavTypes.allTypesMap) { entry ->
            val viewModel = hiltViewModel<SelectOrganizationViewModel>(entry)
            val focusManager = LocalFocusManager.current
            SelectOrganizationScreen(
                viewModel = viewModel,
                openProfileModal = { institutionId ->
                    // Remove the focus from the search field (if it was there)
                    focusManager.clearFocus(force = true)
                    navController.navigate(Route.SelectProfile(institutionId = institutionId, customHostUri = null))
                },
                goToOAuth = { configuration ->
                    navController.navigate(
                        Route.OAuth(
                            configuration = configuration,
                            redirectUri = null
                        )
                    )
                },
                goToConfigScreen = { source, organizationId, organizationName, wifiConfigData ->
                    navController.popBackStack()
                    navController.navigate(
                        Route.ConfigureWifi(
                            source, organizationId, organizationName, wifiConfigData
                        )
                    )
                },
                openFileUri = openFileUri,
                discoverUrl = {
                    navController.navigate(Route.SelectProfile(institutionId =  null, customHostUri = it.toString()))
                }
            )
        }
        composable<Route.SelectProfile>(NavTypes.allTypesMap) { entry ->
            val viewModel = hiltViewModel<SelectProfileViewModel>(entry)
            SelectProfileScreen(
                viewModel = viewModel,
                goToOAuth = { configuration ->
                    navController.navigate(Route.OAuth(configuration, null))
                },
                goToConfigScreen = { source, organizationId, organizationName, provider ->
                    navController.navigate(
                        Route.ConfigureWifi(
                            source,
                            organizationId,
                            organizationName,
                            provider
                        )
                    )
                },
                goToPrevious = {
                    navController.popBackStack()
                })
        }
        composable<Route.OAuth>(NavTypes.allTypesMap) { entry ->
            val viewModel = hiltViewModel<OAuthViewModel>(entry)
            OAuthScreen(
                viewModel = viewModel,
                goToPrevious = {
                    navController.popBackStack()
                },
                goToWebViewFallback = { configuration, navigationUri ->
                    navController.navigate(
                        Route.WebViewFallback(configuration, navigationUri.toString())
                    )
                }
            )
        }
        composable<Route.WebViewFallback>(NavTypes.allTypesMap) { entry ->
            val viewModel = hiltViewModel<WebViewFallbackViewModel>(entry)
            WebViewFallbackScreen(
                viewModel = viewModel,
                onRedirectUriFound = { configuration, uri ->
                    navController.popBackStack() // this screen
                    navController.popBackStack() // OAuth screen
                    navController.navigate(Route.OAuth(configuration, uri.toString())) // OAuth screen again
                },
                onCancel = {
                    navController.navigateUp() // OAuth screen
                    navController.navigateUp() // Profile screen
                }
            )
        }

        composable<Route.ConfigureWifi>(NavTypes.allTypesMap) { entry ->
            val viewModel = hiltViewModel<WifiConfigViewModel>(entry)

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