package app.eduroam.geteduroam.profile

import androidx.compose.runtime.*
import app.eduroam.geteduroam.Screens
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.response.Profile


@Composable
fun rememberSelectProfileState(
    viewModel: SelectProfileViewModel,
): SelectProfileState {
    return remember(viewModel) {
        SelectProfileState(
            viewModel = viewModel
        )
    }
}

@Stable
class SelectProfileState(
    private val viewModel: SelectProfileViewModel,
) {
    var isMenuExpanded: Boolean by mutableStateOf(false)
    var selectLabel: String by mutableStateOf("")

    fun changeMenuExpandedState(newState: Boolean?) {
        //no explicit state was passed, then we just toggle it.
        isMenuExpanded = newState ?: !isMenuExpanded
    }

    fun onSelectProfile(profile: Profile) {
        isMenuExpanded = false
        selectLabel = profile.name
        viewModel.onSelectProfile(
            selectedProfile = profile,
            redirectUri = Screens.OAuth.redirectUrl,
            clientId = Screens.OAuth.APP_ID
        )
    }
}