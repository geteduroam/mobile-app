package app.eduroam.geteduroam.profile

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.response.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun rememberSelectProfileState(
    viewModel: SelectProfileViewModel,
    goToProfileSelection: (String) -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): SelectProfileState {
    val currentGoToProfileSelection by rememberUpdatedState(goToProfileSelection)
    return remember(viewModel, lifecycleOwner, context, coroutineScope) {
        SelectProfileState(
            viewModel = viewModel,
            coroutineScope = coroutineScope,
            lifecycleOwner = lifecycleOwner,
            goToProfileSelection = currentGoToProfileSelection,
        )
    }
}

@Stable
class SelectProfileState(
    private val viewModel: SelectProfileViewModel,
    coroutineScope: CoroutineScope,
    goToProfileSelection: (String) -> Unit,
    lifecycleOwner: LifecycleOwner,
) {
    var isMenuExpanded: Boolean by mutableStateOf(false)
    var selectLabel: String by mutableStateOf("")

    init {
        coroutineScope.launch {}
    }

    fun changeMenuExpandedState(newState: Boolean?) {
        //no explicit state was passed, then we just toggle it.
        isMenuExpanded = newState ?: !isMenuExpanded
    }

    fun onSelectProfile(profile: Profile) {
        isMenuExpanded = false
        selectLabel = profile.name
        viewModel.onSelectProfile(profile)
    }
}