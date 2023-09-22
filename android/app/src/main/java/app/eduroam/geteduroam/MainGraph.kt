package app.eduroam.geteduroam

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.eduroam.geteduroam.config.WifiConfigScreen
import app.eduroam.geteduroam.config.WifiConfigViewModel
import app.eduroam.geteduroam.institutions.SelectInstitutionScreen
import app.eduroam.geteduroam.institutions.SelectInstitutionViewModel
import app.eduroam.geteduroam.oauth.OAuthScreen
import app.eduroam.geteduroam.oauth.OAuthViewModel
import app.eduroam.geteduroam.profile.SelectProfileModal
import app.eduroam.geteduroam.profile.SelectProfileViewModel
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainGraph(
    bottomSheetNavigator: BottomSheetNavigator = rememberBottomSheetNavigator(skipHalfExpanded = true),
    navController: NavHostController = rememberNavController(bottomSheetNavigator),
) {
    ModalBottomSheetLayout(
        modifier = Modifier,
        bottomSheetNavigator = bottomSheetNavigator,
        sheetBackgroundColor = MaterialTheme.colors.surface,
        sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    ) {
        NavHost(
            navController = navController, startDestination = Route.SelectInstitution.route
        ) {
            composable(Route.SelectInstitution.route) { entry ->
                val viewModel = hiltViewModel<SelectInstitutionViewModel>(entry)
                val focusManager = LocalFocusManager.current
                SelectInstitutionScreen(
                    viewModel = viewModel,
                    openProfileModal = { institutionId ->
                        // Remove the focus from the search field (if it was there)
                        focusManager.clearFocus(force = true)
                        navController.navigate(Route.SelectProfile.encodeArgument(institutionId))
                    },
                    goToOAuth = { profile ->
                        navController.navigate(
                            Route.OAuth.encodeArguments(
                                authEndpoint = profile.authorizationEndpoint.orEmpty(),
                                tokenEndpoint = profile.tokenEndpoint.orEmpty(),
                            ),
                        )
                    },
                    goToConfigScreen = { wifiConfigData ->
                        navController.navigate(
                            Route.ConfigureWifi.encodeArguments(
                                wifiConfigData,
                            ),
                        )
                    },
                )
            }
            bottomSheet(
                route = Route.SelectProfile.routeWithArgs,
                arguments = Route.SelectProfile.arguments,
            ) { entry ->
                val viewModel = hiltViewModel<SelectProfileViewModel>(entry)
                SelectProfileModal(viewModel = viewModel,
                    goToOAuth = { auth, token ->
                        navController.navigate(Route.OAuth.encodeArguments(auth, token))
                    },
                    goToConfigScreen = { provider ->
                        navController.navigate(
                            Route.ConfigureWifi.encodeArguments(provider),
                        )
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
}


/**
 * Temporary work-around
 * see https://github.com/google/accompanist/issues/657
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun rememberBottomSheetNavigator(
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    skipHalfExpanded: Boolean = false,
): BottomSheetNavigator {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = animationSpec,
        skipHalfExpanded = skipHalfExpanded
    )
    return remember(sheetState) {
        BottomSheetNavigator(sheetState = sheetState)
    }
}