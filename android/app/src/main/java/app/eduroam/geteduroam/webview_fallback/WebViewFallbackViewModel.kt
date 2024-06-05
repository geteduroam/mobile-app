package app.eduroam.geteduroam.webview_fallback

import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.eduroam.geteduroam.Route
import app.eduroam.geteduroam.di.api.GetEduroamApi
import app.eduroam.geteduroam.di.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import net.openid.appauth.AuthorizationRequest
import javax.inject.Inject

@HiltViewModel
class WebViewFallbackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StorageRepository,
    val api: GetEduroamApi,
): ViewModel() {

    var uiState by mutableStateOf(UiState())

    val configuration = Route.WebViewFallback.decodeConfigurationArgument(savedStateHandle.get(Route.WebViewFallback.configurationArg) ?: "")
    init {
        val requestUriArg = savedStateHandle.get<String>(Route.WebViewFallback.urlArg)!!
        uiState = UiState(startUri =  Uri.parse(requestUriArg))
    }

    fun getAuthRequest(): Flow<AuthorizationRequest?> {
        return repository.authRequest
    }
}

data class UiState(
    val startUri: Uri = Uri.EMPTY,
    val didNavigateToRedirectUri: Uri? = null
)