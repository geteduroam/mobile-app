package app.eduroam.geteduroam.oauth

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Base64
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.Route
import app.eduroam.geteduroam.di.assist.AuthenticationAssistant
import app.eduroam.geteduroam.di.repository.StorageRepository
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.ui.ErrorData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationManagementActivity
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.RegistrationRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenResponse
import timber.log.Timber
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject


@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val repository: StorageRepository,
    private val assistant: AuthenticationAssistant,
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var uiState by mutableStateOf(UiState(OAuthStep.Loading))
        private set
    private val usePKCE = true
    private var service: AuthorizationService? = null
    private var configuration: Configuration = Configuration.EMPTY
    init {
        val configurationArg = savedStateHandle.get<String>(Route.OAuth.configurationArg) ?: ""
        configuration = Route.OAuth.decodeUrlArgument(configurationArg)
        prepareAppAuth(context)
    }

    fun prepareAppAuth(context: Context) = viewModelScope.launch {
        uiState = UiState(OAuthStep.Loading)
        try {
            checkIfConfigurationChanged()
            initializeAppAuth(context)
            val authorizationIntent = createAuthorizationIntent(context)
            uiState = UiState(OAuthStep.Initialized(authorizationIntent))
        } catch (e: Exception) {
            Timber.w(e, "Unable to initialize AppAuth!")
            val argument = e.message ?: e.localizedMessage
            uiState = UiState(
                OAuthStep.Error, ErrorData(
                    titleId = R.string.err_title_auth_unexpected_fail,
                    messageId = if (argument == null) R.string.err_msg_auth_init_fail else R.string.err_msg_auth_init_fail_arg,
                    messageArg = argument
                )
            )
            Timber.w(e, "Failed to prepare AppAuth")
        }
    }

    private fun isCustomTabSupported(context: Context, url: Uri?): Boolean {
        return getCustomTabsPackages(context, url).isNotEmpty()
    }

    /**
     * Returns a list of packages that support Custom Tabs.
     */
    private fun getCustomTabsPackages(context: Context, url: Uri?): List<ResolveInfo> {
        val pm = context.packageManager
        val activityIntent = Intent(Intent.ACTION_VIEW, url)
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = mutableListOf<ResolveInfo>()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info)
            }
        }
        return packagesSupportingCustomTabs
    }


    private suspend fun createAuthorizationIntent(context: Context): Intent {
        val currentAuthRequest = repository.authRequest.first()
            ?: throw IllegalStateException("AuthorizationRequest not available when trying to create authorization request Intent")
        val availableService = service
            ?: throw IllegalStateException("AuthorizationService not available when trying to create authorization request Intent")

        val requestUri = currentAuthRequest.toUri()
        if (isCustomTabSupported(context, requestUri)) {
            val customTabIntent = warmupBrowser()
            return availableService.getAuthorizationRequestIntent(currentAuthRequest, customTabIntent)
        } else {
            val launchIntent = Intent(Intent.ACTION_VIEW, requestUri)
            return AuthorizationManagementActivity.createStartForResultIntent(context, currentAuthRequest, launchIntent)
        }
    }

    fun continueWithFetchToken(intent: Intent?) = viewModelScope.launch {
        Timber.d("Continue with fetch token")
        val currentAuthState = repository.authState.first()
        when {
            intent == null -> {
                uiState = UiState(
                    OAuthStep.Error, ErrorData(
                        titleId = R.string.err_title_auth_invalid,
                        messageId = R.string.err_msg_auth_invalid,
                    )
                )
            }

            currentAuthState == null -> {
                uiState = UiState(
                    OAuthStep.Error, ErrorData(
                        titleId = R.string.err_title_auth_failed,
                        messageId = R.string.err_msg_auth_failed,
                    )
                )
            }

            currentAuthState.isAuthorized && !currentAuthState.needsTokenRefresh -> {
                Timber.d("Current state is already authorized.")
                uiState = UiState(
                    oauthStep = OAuthStep.Authorized,
                    error = null,
                )
            }

            else -> {
                val response = AuthorizationResponse.fromIntent(intent)
                val ex = AuthorizationException.fromIntent(intent)
                Timber.d("Processing authorization response from intent.")
                if (response != null || ex != null) {
                    currentAuthState.update(response, ex)
                    repository.saveCurrentAuthState(currentAuthState)
                }
                if (response?.authorizationCode != null) {
                    try {
                        uiState = UiState(OAuthStep.ExchangingTokenRequest)
                        val tokenResponse = exchangeAuthorizationCode(response)
                        currentAuthState.update(tokenResponse, ex)
                        repository.saveCurrentAuthState(currentAuthState)
                        if (currentAuthState.isAuthorized) {
                            uiState = UiState(
                                oauthStep = OAuthStep.Authorized,
                                error = null,
                            )
                        } else {
                            uiState = UiState(
                                OAuthStep.Error, ErrorData(
                                    titleId = R.string.err_title_auth_failed,
                                    messageId = R.string.err_msg_auth_token_failed,
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to exchange authorization code.")
                        val arg = "${e.javaClass.simpleName}: ${e.message}"
                        uiState = UiState(
                            OAuthStep.Error, ErrorData(
                                titleId = R.string.err_title_auth_failed,
                                messageId = R.string.err_msg_auth_code_failed_arg,
                                messageArg = arg
                            )
                        )
                    }
                } else if (ex != null) {
                    val arg = "${ex.javaClass.simpleName}: ${ex.message}"
                    uiState = UiState(
                        OAuthStep.Error, ErrorData(
                            titleId = R.string.err_title_auth_failed,
                            messageId = R.string.err_msg_generic_unexpected_with_arg,
                            messageArg = arg
                        )
                    )
                } else {
                    uiState = UiState(
                        OAuthStep.Error, ErrorData(
                            titleId = R.string.err_title_auth_failed,
                            messageId = R.string.err_msg_auth_failed,
                        )
                    )
                }
            }
        }
    }

    private suspend fun checkIfConfigurationChanged() {
        val lastKnownHash = repository.lastKnownConfigHash.first()
        if (configuration.hashCode() != lastKnownHash) {
            Timber.d("Configuration change detected, discarding old state")
            repository.saveCurrentAuthState(AuthState())
            repository.acceptNewConfiguration(configuration.hashCode())
        }
    }

    private suspend fun exchangeAuthorizationCode(response: AuthorizationResponse): TokenResponse {
        val currentAuthState = repository.authState.first()
            ?: throw IllegalStateException("AuthState not available when trying to exchange authorization code.")
        val availableService = service
            ?: throw IllegalStateException("AuthenticationService not available when trying to exchange authorization code.")

        val clientAuthentication = try {
            currentAuthState.clientAuthentication
        } catch (e: ClientAuthentication.UnsupportedAuthenticationMethod) {
            throw IllegalStateException("AuthenticationService not available when trying to exchange authorization code.")
        }
        return assistant.exchangeAuthorizationCode(
            response, clientAuthentication, availableService
        )
    }

    fun dismissError() {
        uiState = uiState.copy(error = null)
    }

    private suspend fun recreateAuthorizationService(context: Context) {
        service?.dispose()
        service = AuthenticationAssistant.createAuthorizationService(context)
        repository.saveCurrentAuthRequest(null)
    }

    private suspend fun initializeAppAuth(context: Context) {
        Timber.d("Initializing AppAuth")
        recreateAuthorizationService(context)

        val currentAuthState = repository.authState.first()
        if (currentAuthState?.authorizationServiceConfiguration != null) {
            // configuration is already created, skip to client initialization
            Timber.d("authorization service configuration already established")
            initializeClient()
            return
        }
        // if we are not using discovery, build the authorization service configuration directly
        // from the static configuration values.
        if (configuration.discoveryUri != null) {
            Timber.d("Creating AuthorizationServiceConfiguration from statically known configuration")
            val serviceConfig = AuthorizationServiceConfiguration(
                configuration.authEndpointUri,
                configuration.tokenEndpointUri,
                configuration.registrationEndpointUri,
                configuration.endSessionEndpointUri
            )
            repository.saveCurrentAuthState(AuthState(serviceConfig))
            initializeClient()
            return
        }
        val serviceConfig = assistant.retrieveOpenIdDiscoveryDoc(configuration)
        repository.saveCurrentAuthState(AuthState(serviceConfig))
        initializeClient()
    }

    private suspend fun initializeClient() {
        val staticClientId = configuration.clientId
        if (staticClientId != null) {
            Timber.d("Using static client id: $staticClientId")
            repository.saveClientId(staticClientId)
            createAuthRequest()
            return
        }
        val currentAuthState = repository.authState.first() ?: return
        val lastRegistrationResponse = currentAuthState.lastRegistrationResponse
        if (lastRegistrationResponse != null) {
            Timber.d("Using dynamic client id learned from previous registration: ${lastRegistrationResponse.clientId}")
            repository.saveClientId(lastRegistrationResponse.clientId)
            createAuthRequest()
            return
        }
        Timber.d("Dynamically registering client")
        val serviceConfiguration = currentAuthState.authorizationServiceConfiguration ?: return
        val registrationRequest = RegistrationRequest.Builder(
            serviceConfiguration, listOf(configuration.redirectUri)
        ).setTokenEndpointAuthenticationMethod(ClientSecretBasic.NAME).build()
        if (service != null) {
            val registrationResponse =
                assistant.performRegistrationRequest(registrationRequest, service!!)
            repository.saveClientId(registrationResponse.clientId)
            createAuthRequest()
        }
    }

    private suspend fun warmupBrowser(): CustomTabsIntent {
        Timber.d("Warming up browser instance for auth request. Building custom tab intent")
        val currentAuthRequest = repository.authRequest.first()
            ?: throw IllegalStateException("AuthorizationRequest not available when trying to create CustomTabsIntent")
        val availableService = service
            ?: throw IllegalStateException("AuthorizationService not available when trying to create CustomTabsIntent")
        val intentBuilder =
            availableService.createCustomTabsIntentBuilder(currentAuthRequest.toUri())
        return intentBuilder.build()
    }

    private suspend fun createAuthRequest(loginHint: String? = null) {
        if (usePKCE) {
            createAuthRequestViaPKCE(loginHint)
        } else {
            createAuthRequestViaAuthenticationFlow(loginHint)
        }
    }

    private suspend fun createAuthRequestViaAuthenticationFlow(loginHint: String? = null) {
        val currentAuthState = repository.authState.first()
            ?: throw IllegalStateException("AuthState not available when trying to create AuthorizationRequest")
        val currentClientId = repository.clientId.first()
            ?: throw IllegalStateException("clientId not available when trying to create AuthorizationRequest")
        val currentConfiguration = currentAuthState.authorizationServiceConfiguration
            ?: throw IllegalStateException("AuthorizationServiceConfiguration not available when trying to create AuthorizationRequest")
        val authRequestBuilder = AuthorizationRequest.Builder(
            currentConfiguration,
            currentClientId,
            ResponseTypeValues.CODE,
            configuration.redirectUri
        ).setScopes(configuration.scope, "openid", "profile", "email")
        if (loginHint?.isEmpty() == false) {
            authRequestBuilder.setLoginHint(loginHint)
        }
        repository.saveCurrentAuthRequest(authRequestBuilder.build())
    }

    private suspend fun createAuthRequestViaPKCE(loginHint: String? = null) {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val codeVerifier = Base64.encodeToString(bytes, encoding)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = Base64.encodeToString(hash, encoding)

        val currentAuthState = repository.authState.first()
            ?: throw IllegalStateException("AuthState not available when trying to create AuthorizationRequest")
        val currentClientId = repository.clientId.first()
            ?: throw IllegalStateException("clientId not available when trying to create AuthorizationRequest")
        val currentConfiguration = currentAuthState.authorizationServiceConfiguration
            ?: throw IllegalStateException("AuthorizationServiceConfiguration not available when trying to create AuthorizationRequest")
        val authRequestBuilder = AuthorizationRequest.Builder(
            currentConfiguration,
            currentClientId,
            ResponseTypeValues.CODE,
            configuration.redirectUri
        ).setCodeVerifier(
            codeVerifier, codeChallenge, "S256"
        ).setScope(configuration.scope)
        repository.saveCurrentAuthRequest(authRequestBuilder.build())
    }

    override fun onCleared() {
        service?.dispose()
    }

    fun authorizationLaunched() {
        uiState = uiState.copy(oauthStep = OAuthStep.Launched)
    }

//     fun onNavigatedToRedirectUri(
//        code: String,
//        institutionId: String,
//        error: String,
//        profile: Profile,
//    ) {
//        viewModelScope.launch {
//            val token = institutionsRepository.postToken(
//                tokenUrl = profile.token_endpoint.orEmpty(),
//                code = code,
//                redirectUri = Screens.OAuth.redirectUrl,
//                clientId = Screens.OAuth.APP_ID,
//                codeVerifier = OAuth2Android.getCodeVerifier()
//            )
//            val eapData = institutionsRepository.getEapData(
//                id = institutionId,
//                profileId = profile.id,
//                eapconfigEndpoint = profile.eapconfig_endpoint.orEmpty(),
//                accessToken = token.access_token
//            )
//            configData.value = configParser.parse(eapData)
//        }
//    }
//
//    fun clearWifiConfigData() {
//        configData.value = null
//    }
//
//    private fun receivedToken(institutionId: String, profile: Profile, token: String) =
//        viewModelScope.launch {
//            val eapData = institutionsRepository.getEapData(
//                id = institutionId,
//                profileId = profile.id,
//                eapconfigEndpoint = profile.eapconfig_endpoint.orEmpty(),
//                accessToken = token
//            )
//            configData.value = configParser.parse(eapData)
//        }
//
//    private fun receivedIntentData(
//        intentData: Intent,
//        institutionId: String,
//        profile: Profile,
//    ) {
//        val response = AuthorizationResponse.fromIntent(intentData)
//        val exception = AuthorizationException.fromIntent(intentData)
//
//        if (response?.authorizationCode != null) {
//            viewModelScope.launch {
//                contract?.service?.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse: TokenResponse?, exception: AuthorizationException? ->
//                    Log.e(
//                        "OAuthContract",
//                        "performTokenRequest() called with: tokenResponse = $tokenResponse, exception = $exception"
//                    )
//                    receivedToken(institutionId, profile, tokenResponse?.accessToken.orEmpty())
//                }
//
//            }
//
//        } else if (exception != null) {
//            Log.e("OAuthContract", "Authorization flow failed: ", exception)
//        } else {
//            Log.e("OAuthContract", "No authorization state retained - reauthorization required")
//        }
//    }
//
//    fun generateContract(
//        url: String,
//        profile: Profile,
//        institutionId: String,
//    ): OAuthContract {
//        contract = OAuthContract(
//            url,
//            profile.token_endpoint.orEmpty(),
//        ) { intentData ->
//            receivedIntentData(intentData, institutionId, profile)
//        }
//        return contract!!
//    }
}