package app.eduroam.geteduroam.welcome

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.ItemDataSummary
import app.eduroam.shared.response.Institution
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectInstitutionScreen(viewModel: SelectInstitutionViewModel, log: Logger) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleAwareInstitutionsFlow = remember(viewModel.institutions, lifecycleOwner) {
        viewModel.institutions.flowWithLifecycle(lifecycleOwner.lifecycle)
    }

    @SuppressLint("StateFlowValueCalledInComposition") // False positive lint check when used inside collectAsState()
    val institutionsState: DataState<ItemDataSummary> by lifecycleAwareInstitutionsFlow.collectAsState(viewModel.institutions.value)

    SelectInstitutionContent(
        institutionsState = institutionsState,
        onSelect = viewModel::onInstitutionSelect
    )
}

@Composable
fun SelectInstitutionContent(
    institutionsState: DataState<ItemDataSummary>,
    onSelect: (Institution) -> Unit = {},
) = Scaffold(
    topBar = {
        EduTopAppBar("")
    }
) {
    Spacer(modifier = Modifier.height(56.dp))
    if (institutionsState.empty) {
        Empty(Modifier.padding(it))
    }
    val data = institutionsState.data
    if (data != null) {
        Success(successData = data, onSelect = onSelect, modifier = Modifier.padding(it))
    }
    val exception = institutionsState.exception
    if (exception != null) {
        Error(exception, modifier = Modifier.padding(it))
    }
}


@Composable
fun Empty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.empty_institutions))
    }
}


@Composable
fun Error(error: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = error)
    }
}


@Composable
fun Success(
    successData: ItemDataSummary,
    onSelect: (Institution) -> Unit,
    modifier: Modifier = Modifier,
) {
    InstitutionList(institutions = successData.institutions, onSelect, modifier)
}

@Composable
fun InstitutionList(institutions: List<Institution>, onItemClick: (Institution) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(8.dp)) {
        items(institutions) { institution ->
            Column(
                Modifier
                    .clickable {
                        onItemClick(institution)
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = institution.name,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Divider()
        }
    }
}

