package app.eduroam.geteduroam.config.model

import app.eduroam.geteduroam.util.serializer.DateSerializer
import kotlinx.serialization.Serializable
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import java.util.Date


@Root(name = "EAPIdentityProvider", strict = false)
@Serializable
data class EAPIdentityProvider(
    @field:ElementList(name = "AuthenticationMethods", entry = "AuthenticationMethod")
    var authenticationMethod: List<AuthenticationMethod>? = null,

    @field:ElementList(name = "CredentialApplicability", entry = "IEEE80211")
    var credentialApplicability: List<IEEE80211>? = null,

    @field:Element(name = "ValidUntil", required = false)
    @Serializable(with = DateSerializer::class)
    var validUntil: Date? = null,

    @field:Element(name = "ProviderInfo")
    var providerInfo: ProviderInfo? = null,

    @field:Attribute
    var ID: String? = null,

    @field:Attribute
    var namespace: String? = null,

    @field:Attribute
    var version: Int? = null,

    @field:Attribute(required = false)
    var lang: String? = null,
)