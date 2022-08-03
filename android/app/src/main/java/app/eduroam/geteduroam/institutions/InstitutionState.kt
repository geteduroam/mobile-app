package app.eduroam.geteduroam.institutions

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.shared.response.Institution
import app.eduroam.shared.select.SelectInstitutionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Composable
fun rememberSelectInstitutionState(
    viewModel: SelectInstitutionViewModel,
    goToProfileSelection: (String) -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): SelectInstitutionState {
    val currentGoToProfileSelection by rememberUpdatedState(goToProfileSelection)
    return remember(viewModel, lifecycleOwner, context, coroutineScope) {
        SelectInstitutionState(
            viewModel = viewModel,
            coroutineScope = coroutineScope,
            lifecycleOwner = lifecycleOwner,
            goToProfileSelection = currentGoToProfileSelection,
        )
    }
}

@Stable
class SelectInstitutionState(
    private val viewModel: SelectInstitutionViewModel,
    coroutineScope: CoroutineScope,
    goToProfileSelection: (String) -> Unit,
    lifecycleOwner: LifecycleOwner,
) {
    init {
        coroutineScope.launch {
            viewModel.currentInstitution.flowWithLifecycle(lifecycleOwner.lifecycle).collectLatest {
                if (it != null) {
                    val institutionArgument = Json.encodeToString(it)
                    goToProfileSelection(institutionArgument)
                }
            }
        }
    }

    fun onSelectInstitution(selectedInstitution: Institution) {
        viewModel.onInstitutionSelect(selectedInstitution)
    }
}