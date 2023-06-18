package app.eduroam.geteduroam

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.eduroam.geteduroam.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduTopAppBar(
    onBackClicked: () -> Unit = {},
    withBackIcon: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets
        .exclude(WindowInsets.navigationBars)
        .exclude(WindowInsets.ime),
    content: @Composable (PaddingValues) -> Unit,
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topBarState)
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(modifier = Modifier
                    .padding(12.dp), action = {
                    TextButton(
                        onClick = data::performAction,
                    ) { Text(data.visuals.actionLabel ?: "") }
                }) {
                    Text(data.visuals.message)
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                navigationIcon = {
                    if (withBackIcon) {
                        IconButton(
                            onClick = onBackClicked, modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.button_back),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(width = 48.dp, height = 48.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.name),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = contentWindowInsets,
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Preview
@Composable
private fun Preview_TopAppBarWithBackButton() {
    AppTheme {
        EduTopAppBar(onBackClicked = { }) {}
    }
}
