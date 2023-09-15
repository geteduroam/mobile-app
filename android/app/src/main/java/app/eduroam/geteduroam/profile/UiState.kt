package app.eduroam.geteduroam.profile

import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.config.model.Helpdesk
import app.eduroam.geteduroam.models.Credentials
import app.eduroam.geteduroam.models.Profile
import app.eduroam.geteduroam.ui.ErrorData

data class UiState(
    val profiles: List<PresentProfile> = emptyList(),
    val credentialsEnteredForProviderList: EAPIdentityProviderList? = null,
    val institution: PresentInstitution? = null,
    val inProgress: Boolean = false,
    val errorData: ErrorData? = null,
    val promptForOAuth: Unit? = null,
    val showTermsOfUseDialog: Boolean = false,
    val showUsernameDialog: Boolean = false,
    val goToConfigScreenWithProviderList: EAPIdentityProviderList? = null
)

data class PresentProfile(val profile: Profile, val isSelected: Boolean = false)

data class PresentInstitution(
    val name: String? = null,
    val location: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val logo: String? = null,
    val termsOfUse: String? = null,
    val helpDesk: Helpdesk? = null,
    val requiredSuffix: String? = null
)