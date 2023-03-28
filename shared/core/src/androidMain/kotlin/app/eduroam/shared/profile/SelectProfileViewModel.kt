package app.eduroam.shared.profile

import app.eduroam.shared.OAuth2
import app.eduroam.shared.config.AndroidConfigParser
import app.eduroam.shared.config.model.EAPIdentityProviderList
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.SelectProfileSummary
import app.eduroam.shared.models.ViewModel
import app.eduroam.shared.response.Institution
import app.eduroam.shared.response.Profile
import app.eduroam.shared.select.InstitutionsRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SelectProfileViewModel(
    private val institutionRepository: InstitutionsRepository,
    private val configParser: AndroidConfigParser,
    log: Logger,
) : ViewModel() {

    private val log = log.withTag("SelectProfileViewModel")

    var institutionId: String? = null
    val uiDataState: StateFlow<DataState<SelectProfileSummary>> = MutableStateFlow(
        DataState(loading = true)
    )
    val authorizationUrl: MutableStateFlow<String?> = MutableStateFlow(null)
    val eapIdentityProviderListMutableStateFlow: MutableStateFlow<EAPIdentityProviderList?> = MutableStateFlow(null)

    override fun onCleared() {
        log.v("Clearing SelectInstitutionViewModel")
    }

    fun profilesForInstitution(institution: Institution) {
        institutionId = institution.id
        updateDataState(
            DataState(
                SelectProfileSummary(institution.profiles, null), loading = false
            )
        )
    }

    fun onSelectProfile(selectedProfile: Profile, redirectUri: String, clientId: String) {
        val profiledata = uiDataState.value
        updateDataState(
            uiDataState.value.copy(
                loading = true,
                data = profiledata.data?.copy(selectedProfile = selectedProfile)
            )
        )
        if (institutionId != null) {
            if (selectedProfile.oauth) {
                log.d("Selected profile requires authentication")
                authorizationUrl.value = OAuth2().getAuthorizationUrl(
                    institutionId = institutionId!!,
                    authorizationEndpoint = selectedProfile.authorization_endpoint,
                    redirectUri = redirectUri,
                    clientId = clientId
                )
                updateDataState(uiDataState.value.copy(loading = false))
            } else {
                viewModelScope.launch {
                    log.d("Selected profile requires *no* authentication")
                    try {
                        val eapData = institutionRepository.getEapData(
                            id = institutionId!!,
                            profileId = selectedProfile.id,
                            eapconfigEndpoint = selectedProfile.eapconfig_endpoint.orEmpty()
                        )
                        log.d("Downloaded EAP file for profile with no authentication")
                        eapIdentityProviderListMutableStateFlow.emit(configParser.parse(eapData))
                    } catch (e: Exception) {
                        log.e("Failed to download anon EAP config file", e)
                    } finally {
                        updateDataState(uiDataState.value.copy(loading = false))
                    }
                }
            }
        }
    }

    private fun updateDataState(newValue: DataState<SelectProfileSummary>) {
        (uiDataState as MutableStateFlow).value = newValue
    }

    fun clearWifiConfigData() {
        eapIdentityProviderListMutableStateFlow.value = null
    }

    fun handledAuthorization() {
        authorizationUrl.value = null
    }

    fun currentSelectedProfile(): Profile? = uiDataState.value?.data?.selectedProfile
}
