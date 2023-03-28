package app.eduroam.geteduroam.institutions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
        onClearDialog = viewModel::clearDialog,
        onCredsAvailable = { username, password ->
            viewModel.creds.value = Pair(username, password)
        }
    )
}

@Composable
fun LoginDialog(
    onConfirmClicked: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val openDialog = remember { mutableStateOf(true) }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    if (openDialog.value) {
        Dialog(
            onDismissRequest = onDismiss,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.login_dialog_title))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .weight(weight = 1f, fill = false)
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_dialog_text)
                        )

                        OutlinedTextField(value = username.value, onValueChange = {
                            username.value = it
                        }, Modifier.padding(top = 8.dp), label = { Text(text = stringResource(R.string.login_dialog_username)) },  textStyle = TextStyle(color = Color.White))

                        OutlinedTextField(value = password.value, onValueChange = {
                            password.value = it
                        }, Modifier.padding(top = 8.dp),
                            label = { Text(text = stringResource(R.string.login_dialog_password)) },
                            textStyle = TextStyle(color = Color.White),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            openDialog.value = false
                            onDismiss()
                        }) {
                            Text(text = stringResource(R.string.login_dialog_cancel))
                        }
                        TextButton(onClick = {
                            openDialog.value = false
                            onDismiss()
                            onConfirmClicked(username.value, password.value)
                        }) {
                            Text(text = stringResource(R.string.login_dialog_login))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermsOfUseDialog(
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
                Text(text = stringResource(R.string.terms_of_use_dialog_title))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(weight = 1f, fill = false)
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.terms_of_use_dialog_text)
                    )
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.terms_of_use_dialog_agree))
                    }
                    TextButton(onClick = onConfirmClicked) {
                        Text(text = stringResource(R.string.terms_of_use_dialog_read_tou))
                    }
                    TextButton(onClick = onConfirmClicked) {
                        Text(text = stringResource(R.string.terms_of_use_dialog_disagree))
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
    onClearDialog: () -> Unit = {},
    onCredsAvailable: (String, String) -> Unit = { _, _ -> }
) = Scaffold(topBar = {
    EduTopAppBar(stringResource(R.string.name))
}) { paddingValues ->
    if (institutionsState.showDialog == true) {
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

}