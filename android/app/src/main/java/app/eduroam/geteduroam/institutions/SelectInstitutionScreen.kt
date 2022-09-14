package app.eduroam.geteduroam.institutions

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.util.Oauth2
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.ItemDataSummary
import app.eduroam.shared.response.Institution
import app.eduroam.shared.select.SelectInstitutionViewModel
import io.ktor.http.*
import java.lang.StringBuilder
import java.net.URI

@Composable
fun SelectInstitutionScreen(
    viewModel: SelectInstitutionViewModel,
    gotToProfileSelection: (String) -> Unit,
    selectInstitutionState: SelectInstitutionState = rememberSelectInstitutionState(viewModel, gotToProfileSelection),
) {
    val uriHandler = LocalUriHandler.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleAwareUiDataStateFlow = remember(viewModel.uiDataState, lifecycleOwner) {
        viewModel.uiDataState.flowWithLifecycle(lifecycleOwner.lifecycle)
    }

    @SuppressLint("StateFlowValueCalledInComposition") // False positive lint check when used inside collectAsState()
    val uiDataState: DataState<ItemDataSummary> by lifecycleAwareUiDataStateFlow.collectAsState(viewModel.uiDataState.value)

    SelectInstitutionContent(
        institutionsState = uiDataState,
        onSelectInstitution = { institution ->
            if (institution.requiresAuth()) {
                uriHandler.openUri(Oauth2.getAuthorizationUrl(institution))
            } else {
                selectInstitutionState.onSelectInstitution(institution)
            }
        },
        searchText = uiDataState.data?.filterOn.orEmpty(),
        onSearchTextChange = { viewModel.onSearchTextChange(it) },
    )
}

@Composable
fun SelectInstitutionContent(
    institutionsState: DataState<ItemDataSummary>,
    onSelectInstitution:(Institution) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit = {},
) = Scaffold(
    topBar = {
        EduTopAppBar(stringResource(R.string.name))
    }
) { paddingValues ->
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

