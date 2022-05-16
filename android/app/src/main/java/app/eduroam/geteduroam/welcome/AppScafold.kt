package app.eduroam.geteduroam.welcome

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    topBarTitle: String,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { SampleTopAppBar(title = topBarTitle) },
        content = content
    )
}
