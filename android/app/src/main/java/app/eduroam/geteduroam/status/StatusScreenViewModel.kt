package app.eduroam.geteduroam.status

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.eduroam.geteduroam.di.api.GetEduroamApi
import app.eduroam.geteduroam.di.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatusScreenViewModel @Inject constructor(
    val api: GetEduroamApi,
    private val repository: StorageRepository,
) : ViewModel() {

    val organizationId = repository.configuredOrganizationId
    val organizationName = repository.configuredOrganizationName
    val configSource = repository.configuredProfileSource
    val lastConfig = repository.configuredProfileLastConfig
    val expiryTimestampMs = repository.profileExpiryTimestampMs

    var uiState by mutableStateOf(UiState())

    data class UiState(
        val showDebugOptions: Boolean = false
    )


}