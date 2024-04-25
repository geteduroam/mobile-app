package app.eduroam.geteduroam.organizations

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.models.Organization
import app.eduroam.geteduroam.ui.ErrorData
import app.eduroam.geteduroam.ui.theme.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun SelectOrganizationScreen(
    viewModel: SelectOrganizationViewModel,
    openProfileModal: (String) -> Unit,
    goToOAuth: (Configuration) -> Unit,
    goToConfigScreen: (String, EAPIdentityProviderList) -> Unit,
    openFileUri: (Uri) -> Unit
) {
    val step: Step by remember { mutableStateOf(Step.Start) }
    var waitForVmEvent by rememberSaveable { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(step) {
        when (step) {
            is Step.DoOAuthFor -> {
                viewModel.onStepCompleted()
                val doAuth = step as Step.DoOAuthFor
                goToOAuth(doAuth.configuration)
            }

            is Step.DoConfig -> {
                viewModel.onStepCompleted()
                goToConfigScreen((step as Step.DoConfig).organizationId, (step as Step.DoConfig).eapIdentityProviderList)
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
                .filter { it.selectedOrganization != null }.flowWithLifecycle(lifecycle).collect {
                    waitForVmEvent = false
                    currentOpenProfileModal(
                        it.selectedOrganization?.id.orEmpty(),
                    )
                    viewModel.clearSelection()
                }
        }
    }

    SelectOrganizationContent(
        organizations = viewModel.uiState.organizations,
        isLoading = viewModel.uiState.isLoading,
        onSelectOrganization = { organization ->
            waitForVmEvent = true
            viewModel.onOrganizationSelect(organization)
        },
        searchText = viewModel.uiState.filter,
        onSearchTextChange = { viewModel.onSearchTextChange(it) },
        onClearDialog = viewModel::clearDialog,
        onCredsAvailable = { username, password ->
            viewModel.creds.value = Pair(username, password)
        },
        errorData = viewModel.uiState.errorData,
        openFileUri = openFileUri
    )
}


@Composable
fun SelectOrganizationContent(
    organizations: List<Organization> = emptyList(),
    isLoading: Boolean = false,
    showDialog: Boolean = false,
    errorData: ErrorData? = null,
    onSelectOrganization: (Organization) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit = {},
    onClearDialog: () -> Unit = {},
    onCredsAvailable: (String, String) -> Unit = { _, _ -> },
    openFileUri: (Uri) -> Unit = {}
) = Surface(color = MaterialTheme.colorScheme.surface) {
    val context = LocalContext.current
    var showExtraActionsPopup by remember { mutableStateOf(false) }
    var popupPosition by remember { mutableStateOf(IntOffset(0, 0)) }
    val pickEapConfigFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        if (fileUri != null) {
            openFileUri(fileUri)
        }
    }
    if (showDialog) {
        LoginDialog({ username, password ->
            onCredsAvailable(username, password)
            onClearDialog()
        }, {})
    } else {
        // Center heart icon
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.fillMaxHeight(fraction = 0.3f))
            Icon(
                painterResource(R.drawable.ic_home_center),
                contentDescription = "App logo",
                tint = Color(0xFFBDD6E5),
                modifier = Modifier.size(150.dp)
            )
        }
        // Bottom right eduroam icon
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(Modifier.fillMaxHeight(fraction = 0.8f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50))
                    .background(Color.White)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Image(
                    painterResource(R.drawable.ic_home_bottom_right),
                    contentDescription = "App logo",
                    modifier = Modifier.width(120.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OrganizationSearchHeader(
                searchText = searchText,
                onSearchTextChange = onSearchTextChange,
                onPositionDetermined = { position ->
                    popupPosition = position
                },
                showExtraActionsPopup = {
                    showExtraActionsPopup = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.weight(1f)) {
                 if (errorData != null) {
                    item {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = errorData.title(context),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else if (!isLoading) {
                    if (organizations.isEmpty() && searchText.isNotEmpty()) {
                        item {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                text = stringResource(id = R.string.organizations_no_results),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        items(organizations) { organization ->
                            OrganizationRow(organization, onSelectOrganization)
                        }
                    }
                }
            }
        }
    }
    if (showExtraActionsPopup) {
        Popup(
            offset = popupPosition,
            onDismissRequest = {
                showExtraActionsPopup = false
            }
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    TextButton(
                        onClick = {
                            showExtraActionsPopup = false
                            // Open file chooser
                            pickEapConfigFileLauncher.launch("*/*") // It would be nice to filter on .eap-config files, but it is currently not possible
                        }
                    ) {
                        Text(text = stringResource(id = R.string.open_eap_config_file))
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun Preview_SelectOrganizationContent() {
    AppTheme {
        SelectOrganizationContent(
            onSelectOrganization = {},
            searchText = ""
        )
    }
}