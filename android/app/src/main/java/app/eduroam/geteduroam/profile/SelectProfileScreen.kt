package app.eduroam.geteduroam.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.SelectProfileSummary
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.response.Profile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectProfileScreen(
    viewModel: SelectProfileViewModel,
    selectInstitutionState: SelectProfileState = rememberSelectProfileState(
        viewModel
    ),
    goToOAuth: (String, Profile) -> Unit,
    goToConfigScreen: (String) -> Unit,
) {
    val uiDataState: DataState<SelectProfileSummary> by viewModel.uiDataState.collectAsStateWithLifecycle()
    val authorizationUrl by viewModel.authorizationUrl.collectAsStateWithLifecycle(null)
    val configData by viewModel.configData.collectAsStateWithLifecycle(null)

    authorizationUrl?.let {
        LaunchedEffect(it) {
            val selectedProfile = viewModel.currentSelectedProfile()
            if (selectedProfile != null) {
                viewModel.handledAuthorization()
                goToOAuth(it, selectedProfile)
            }
        }
    }
    configData?.let {
        LaunchedEffect(it) {
            viewModel.clearWifiConfigData()
            val wifiConfigData = Json.encodeToString(it)
            goToConfigScreen(wifiConfigData)
        }
    }
    SelectProfileContent(
        uiDataState = uiDataState,
        state = selectInstitutionState,
    )
}

@Composable
fun SelectProfileContent(
    uiDataState: DataState<SelectProfileSummary>,
    state: SelectProfileState,
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
        val icon = if (state.isMenuExpanded) {
            Icons.Filled.KeyboardArrowUp
        } else {
            Icons.Filled.KeyboardArrowDown
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = { state.changeMenuExpandedState(null) })
                .border(
                    width = 1.dp,
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(12.dp)
                ), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.selectLabel.ifEmpty { stringResource(R.string.profile_select_profile) },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(8.dp)
            )
            Icon(
                imageVector = icon,
                contentDescription = "",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
        Spacer(Modifier.height(16.dp))

        DropdownMenu(
            expanded = state.isMenuExpanded,
            onDismissRequest = { state.changeMenuExpandedState(false) },
            modifier = Modifier.wrapContentSize()
        ) {
            uiDataState.data?.profiles?.forEach { choiceItem ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = choiceItem.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    onClick = {
                        state.onSelectProfile(choiceItem)
                    },
                )
            }
        }

    }
}

