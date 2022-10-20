package app.eduroam.geteduroam.oauth

import app.eduroam.geteduroam.Screens
import app.eduroam.shared.OAuth2Android
import app.eduroam.shared.config.ConfigParser
import app.eduroam.shared.config.WifiConfigData
import app.eduroam.shared.models.ViewModel
import app.eduroam.shared.response.Profile
import app.eduroam.shared.select.InstitutionsRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OAuthViewModel(
    private val institutionsRepository: InstitutionsRepository,
    private val configParser: ConfigParser,
    log: Logger,
) : ViewModel() {
    val configData: MutableStateFlow<WifiConfigData?> = MutableStateFlow(null)
    fun onNavigatedToRedirectUri(
        code: String,
        institutionId: String,
        error: String,
        profile: Profile
    ) {

        viewModelScope.launch {
            val token = institutionsRepository.postToken(
                tokenUrl = profile.token_endpoint.orEmpty(),
                code = code,
                redirectUri = Screens.OAuth.redirectUrl,
                clientId = Screens.OAuth.APP_ID,
                codeVerifier = OAuth2Android.getCodeVerifier()
            )
            val eapData = institutionsRepository.getEapData(
                id = institutionId,
                profileId = profile.id,
                eapconfigEndpoint = profile.eapconfig_endpoint.orEmpty(),
                accessToken = token.access_token
            )
            configData.value = configParser.parse(eapData)
        }
    }

    fun clearWifiConfigData() {
        configData.value = null
    }

}