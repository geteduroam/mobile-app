package app.eduroam.geteduroam.institutions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.Screens
import app.eduroam.shared.config.WifiConfigData
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.ItemDataSummary
import app.eduroam.shared.response.Institution
import app.eduroam.shared.response.Profile
import app.eduroam.shared.select.SelectInstitutionViewModel
import app.eduroam.shared.select.Step

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectInstitutionScreen(
    viewModel: SelectInstitutionViewModel,
    goToOAuth: (String, Profile) -> Unit,
    gotToProfileSelection: (Institution) -> Unit,
    goToConfigScreen: (WifiConfigData) -> Unit,
) {
    val uiDataState: DataState<ItemDataSummary> by viewModel.uiDataState.collectAsStateWithLifecycle()
    val step by viewModel.step.collectAsStateWithLifecycle(Step.Start)

    LaunchedEffect(step) {
        when (step) {
            is Step.DoOAuthFor -> {
                viewModel.onStepCompleted()
                val doAuth = step as Step.DoOAuthFor
                goToOAuth(doAuth.authorizationUrl, doAuth.profile)
            }
            is Step.DoConfig -> {
                viewModel.onStepCompleted()
                goToConfigScreen((step as Step.DoConfig).wifiConfigData)
            }
            is Step.PickProfileFrom -> {
                viewModel.onStepCompleted()
                gotToProfileSelection((step as Step.PickProfileFrom).institution)
            }
            Step.Start -> {
                //Do nothing
            }
        }
    }

    SelectInstitutionContent(
        institutionsState = uiDataState,
        onSelectInstitution = { institution ->
            viewModel.onInstitutionSelect(
                institution, Screens.OAuth.redirectUrl, Screens.OAuth.APP_ID
            )
        },
        searchText = uiDataState.data?.filterOn.orEmpty(),
        onSearchTextChange = { viewModel.onSearchTextChange(it) },
    )
}

@Composable
fun LoginDialog(
    onConfirmClicked: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Login Required")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(weight = 1f, fill = false)
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Please enter your username and password"
                    )

                    OutlinedTextField(value = "", onValueChange = {
                    }, Modifier.padding(top = 8.dp), label = { Text(text = "Username") })

                    OutlinedTextField(value = "", onValueChange = {
                    }, Modifier.padding(top = 8.dp), label = { Text(text = "Password") })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel")
                    }
                    TextButton(onClick = onConfirmClicked) {
                        Text(text = "Log in")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectInstitutionContent(
    institutionsState: DataState<ItemDataSummary>,
    onSelectInstitution: (Institution) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit = {},
) = Scaffold(topBar = {
    EduTopAppBar(stringResource(R.string.name))
}) { paddingValues ->
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

            if (institutionsState.loading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (institutionsState.exception != null) {
                item {
                    Text(
                        text = institutionsState.exception.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                if (institutionsState.empty) {
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

                    institutionsState.data?.institutions?.forEach { institution ->
                        item {
                            InstitutionRow(institution, onSelectInstitution)
                        }
                    }
                }
            }
        }
    }
}