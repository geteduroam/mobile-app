package app.eduroam.geteduroam.profile

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.response.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


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
    init {
        coroutineScope.launch {
            viewModel.currentProfile.flowWithLifecycle(lifecycleOwner.lifecycle).collectLatest {
                if (it != null) {
                    val institutionArgument = Json.encodeToString(it)
                    goToProfileSelection(institutionArgument)
                }
            }
        }
    }

    fun onSelectProfile(profile: Profile) {
        viewModel.onProfileSelect(profile)
    }
}