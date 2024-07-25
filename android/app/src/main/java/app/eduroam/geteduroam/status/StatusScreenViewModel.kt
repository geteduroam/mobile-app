package app.eduroam.geteduroam.status

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eduroam.geteduroam.di.api.GetEduroamApi
import app.eduroam.geteduroam.di.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatusScreenViewModel @Inject constructor(
    val api: GetEduroamApi,
    private val repository: StorageRepository,
) : ViewModel() {

    val organizationId = repository.configuredOrganizationId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val organizationName = repository.configuredOrganizationName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val configSource = repository.configuredProfileSource
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val lastConfig = repository.configuredProfileLastConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val expiryTimestampMs = repository.profileExpiryTimestampMs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var uiState by mutableStateOf(UiState())

    data class UiState(
        val showDebugOptions: Boolean = false
    )


}