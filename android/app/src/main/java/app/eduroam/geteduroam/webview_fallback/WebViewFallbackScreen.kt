package app.eduroam.geteduroam.webview_fallback

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.flowWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.ui.OAuthWebView
import kotlinx.coroutines.flow.filter
import net.openid.appauth.AuthorizationManagementActivity
import net.openid.appauth.RedirectUriReceiverActivity

@Composable
fun WebViewFallbackScreen(
    viewModel: WebViewFallbackViewModel,
    onRedirectUriFound: (Configuration, Uri) -> Unit,
    onCancel: () -> Unit
) = EduTopAppBar(onBackClicked = onCancel, title = stringResource(id = R.string.oauth_title)) {

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel, lifecycle) {
        snapshotFlow { viewModel.uiState }
            .filter { it.didNavigateToRedirectUri != null }
            .flowWithLifecycle(lifecycle)
            .collect { state ->
                val navigatedUri = state.didNavigateToRedirectUri!!
                onRedirectUriFound(viewModel.configuration, navigatedUri)
            }
    }


    WebViewFallbackContent(
        uiState = viewModel.uiState,
        padding = it,
        onRedirectUriFound = {
            viewModel.uiState = viewModel.uiState.copy(didNavigateToRedirectUri = it)
        })
}

@Composable
private fun WebViewFallbackContent(
    uiState: UiState,
    padding: PaddingValues,
    onRedirectUriFound: (Uri) -> Unit
) {

    Box(modifier = Modifier.padding(padding)) {
        OAuthWebView(
            startUrl = uiState.startUri,
            onRedirectUriFound = onRedirectUriFound
        )
    }
}

@Preview
@Composable
fun PreviewWebviewFallbackContent() = WebViewFallbackContent(UiState(), PaddingValues(), onRedirectUriFound = {})
