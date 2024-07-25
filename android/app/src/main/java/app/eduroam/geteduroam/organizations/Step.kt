package app.eduroam.geteduroam.organizations

import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.models.Organization
import app.eduroam.geteduroam.models.Profile
import app.eduroam.geteduroam.status.ConfigSource

sealed class Step {
    object Start : Step()
    data class DoConfig(
        val source: ConfigSource,
        val organizationId: String,
        val organizationName: String,
        val eapIdentityProviderList: EAPIdentityProviderList
    ) : Step()
    data class DoOAuthFor(val configuration: Configuration) : Step()
    data class PickProfileFrom(val organization: Organization) : Step()
}