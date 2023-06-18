package app.eduroam.geteduroam.institutions

import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.models.Institution
import app.eduroam.geteduroam.models.Profile

sealed class Step {
    object Start : Step()
    data class DoConfig(val eapIdentityProviderList: EAPIdentityProviderList) : Step()
    data class DoOAuthFor(val profile: Profile, val authorizationUrl: String) : Step()
    data class PickProfileFrom(val institution: Institution) : Step()
}