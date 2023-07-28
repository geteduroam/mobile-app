package app.eduroam.geteduroam.institutions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.models.Institution
import app.eduroam.geteduroam.models.Profile
import app.eduroam.geteduroam.ui.ErrorData
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun SelectInstitutionScreen(
    viewModel: SelectInstitutionViewModel,
    openProfileModal: (String) -> Unit,
    goToOAuth: (Profile) -> Unit,
    goToConfigScreen: (EAPIdentityProviderList) -> Unit,
) {
    val step: Step by remember { mutableStateOf(Step.Start) }
    var waitForVmEvent by rememberSaveable { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(step) {
        when (step) {
            is Step.DoOAuthFor -> {
                viewModel.onStepCompleted()
                val doAuth = step as Step.DoOAuthFor
                goToOAuth(doAuth.profile)
            }

            is Step.DoConfig -> {
                viewModel.onStepCompleted()
                goToConfigScreen((step as Step.DoConfig).eapIdentityProviderList)
            }

            is Step.PickProfileFrom -> {
                viewModel.onStepCompleted()
            }

            Step.Start -> {
                //Do nothing
            }
        }
    }
    if (waitForVmEvent) {
        val currentOpenProfileModal by rememberUpdatedState(newValue = openProfileModal)
        LaunchedEffect(viewModel, lifecycle) {
            snapshotFlow { viewModel.uiState }.distinctUntilChanged()
                .filter { it.selectedInstitution != null }.flowWithLifecycle(lifecycle).collect {
                    waitForVmEvent = false
                    currentOpenProfileModal(
                        it.selectedInstitution?.id.orEmpty(),
                    )
                    viewModel.clearSelection()
                }
        }
    }

    SelectInstitutionContent(institutions = viewModel.uiState.institutions,
        onSelectInstitution = { institution ->
            waitForVmEvent = true
            viewModel.onInstitutionSelect(institution)
        },
        searchText = viewModel.uiState.filter,
        onSearchTextChange = { viewModel.onSearchTextChange(it) },
        onClearDialog = viewModel::clearDialog,
        onCredsAvailable = { username, password ->
            viewModel.creds.value = Pair(username, password)
        })
}


@Composable
fun SelectInstitutionContent(
    institutions: List<Institution> = emptyList(),
    isLoading: Boolean = false,
    showDialog: Boolean = false,
    errorData: ErrorData? = null,
    onSelectInstitution: (Institution) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit = {},
    onClearDialog: () -> Unit = {},
    onCredsAvailable: (String, String) -> Unit = { _, _ -> },
) = EduTopAppBar(withBackIcon = false) { paddingValues ->
    val context = LocalContext.current
    if (showDialog) {
        LoginDialog({ username, password ->
            onCredsAvailable(username, password)
            onClearDialog()
        }, {})
    } else {
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn {
                item {
                    InstitutionSearchHeader(
                        searchText = searchText,
                        onSearchTextChange = onSearchTextChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                }

                if (isLoading) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (errorData != null) {
                    item {
                        Text(
                            text = errorData.title(context),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    if (institutions.isEmpty()) {
                        item {
                            Text(stringResource(id = R.string.institutions_no_results))
                        }
                    } else {

                        item {
                            Text(
                                text = stringResource(id = R.string.institutions_choose_one),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        institutions.forEach { institution ->
                            item {
                                InstitutionRow(institution, onSelectInstitution)
                            }
                        }
                    }
                }
            }
        }
    }
}