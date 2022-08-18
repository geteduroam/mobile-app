package app.eduroam.geteduroam.profile

import android.annotation.SuppressLint
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.SelectProfileSummary
import app.eduroam.shared.profile.SelectProfileViewModel

@Composable
fun SelectProfileScreen(
    viewModel: SelectProfileViewModel,
    goToAuth: (String) -> Unit,
    selectInstitutionState: SelectProfileState = rememberSelectProfileState(
        viewModel, goToAuth
    ),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleAwareUiDataStateFlow = remember(viewModel.uiDataState, lifecycleOwner) {
        viewModel.uiDataState.flowWithLifecycle(lifecycleOwner.lifecycle)
    }

    @SuppressLint("StateFlowValueCalledInComposition") // False positive lint check when used inside collectAsState()
    val uiDataState: DataState<SelectProfileSummary> by lifecycleAwareUiDataStateFlow.collectAsState(
        viewModel.uiDataState.value
    )

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

