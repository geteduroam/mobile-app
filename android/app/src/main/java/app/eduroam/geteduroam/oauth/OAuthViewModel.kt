package app.eduroam.geteduroam.oauth

import android.content.Intent
import android.util.Log
import app.eduroam.geteduroam.Screens
import app.eduroam.shared.OAuth2Android
import app.eduroam.shared.config.AndroidConfigParser
import app.eduroam.shared.config.model.EAPIdentityProviderList
import app.eduroam.shared.models.ViewModel
import app.eduroam.shared.response.Profile
import app.eduroam.shared.select.InstitutionsRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.*

class OAuthViewModel(
    private val institutionsRepository: InstitutionsRepository,
    private val configParser: AndroidConfigParser,
    private val log: Logger,
) : ViewModel() {
    val configData: MutableStateFlow<EAPIdentityProviderList?> = MutableStateFlow(null)
    private var contract: OAuthContract? = null

    fun onNavigatedToRedirectUri(
        code: String,
        institutionId: String,
        error: String,
        profile: Profile
    ) {
        log.d("Received code response $code for institution: $institutionId")
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

    private fun receivedToken(institutionId: String, profile: Profile, token: String) =
        viewModelScope.launch {
            val eapData = institutionsRepository.getEapData(
                id = institutionId,
                profileId = profile.id,
                eapconfigEndpoint = profile.eapconfig_endpoint.orEmpty(),
                accessToken = token
            )
            configData.value = configParser.parse(eapData)
        }

    private fun receivedIntentData(
        intentData: Intent,
        institutionId: String,
        profile: Profile
    ) {
        val response = AuthorizationResponse.fromIntent(intentData)
        val exception = AuthorizationException.fromIntent(intentData)

        if (response?.authorizationCode != null) {
            viewModelScope.launch {
                contract?.service?.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse: TokenResponse?, exception: AuthorizationException? ->
                    Log.e(
                        "OAuthContract",
                        "performTokenRequest() called with: tokenResponse = $tokenResponse, exception = $exception"
                    )
                    receivedToken(institutionId, profile, tokenResponse?.accessToken.orEmpty())
                }

            }

        } else if (exception != null) {
            Log.e("OAuthContract", "Authorization flow failed: ", exception)
        } else {
            Log.e("OAuthContract", "No authorization state retained - reauthorization required")
        }
    }

    fun generateContract(
        url: String,
        profile: Profile,
        institutionId: String
    ): OAuthContract {
        contract = OAuthContract(
            url,
            profile.token_endpoint.orEmpty(),
        ) { intentData ->
            receivedIntentData(intentData, institutionId, profile)
        }
        return contract!!
    }
}