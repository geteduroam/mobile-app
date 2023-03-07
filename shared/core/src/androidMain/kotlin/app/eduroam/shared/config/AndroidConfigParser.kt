package app.eduroam.shared.config

import app.eduroam.shared.config.model.EAPIdentityProvider
import app.eduroam.shared.config.model.EAPIdentityProviderList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simpleframework.xml.core.Persister
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.Reader
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants


class AndroidConfigParser : ConfigParser {
    override suspend fun parse(source: ByteArray): WifiConfigData = withContext(Dispatchers.IO) {

        val persister = Persister()
        val eapIdentityProviderList = persister.read(EAPIdentityProviderList::class.java, source.inputStream())
        val eapIdentityProvider = eapIdentityProviderList?.eapIdentityProvider?.first()
        val clientCertificate = getClientCertificate(eapIdentityProvider)

        WifiConfigData(
            ssids = eapIdentityProvider?.credentialApplicability?.map { it.ssid } ?: listOf(),
            oids = eapIdentityProvider?.credentialApplicability?.map { it.consortiumOID?.toLong(16) }?.filterNotNull() ?: listOf(),
            clientCertificate = clientCertificate,
            anonymousIdentity = eapIdentityProvider?.authenticationMethod?.first()?.clientSideCredential?.outerIdentity,
            caCertificates = eapIdentityProvider?.authenticationMethod?.map { it.serverSideCredential?.cartData?.value }?.filterNotNull(),
            serverNames = eapIdentityProvider?.authenticationMethod?.map { it.serverSideCredential?.serverId }?.filterNotNull(),
            username = eapIdentityProvider?.authenticationMethod?.first()?.clientSideCredential?.userName,
            password = eapIdentityProvider?.authenticationMethod?.first()?.clientSideCredential?.password,
        )
    }

    private fun getClientCertificate(eapIdentityProvider: EAPIdentityProvider?): ClientCertificate? {
        val passphrase = eapIdentityProvider?.authenticationMethod?.first()?.clientSideCredential?.passphrase
        val clientCertificate = eapIdentityProvider?.authenticationMethod?.first()?.clientSideCredential?.clientCertificate?.value
        return if (passphrase.isNullOrEmpty() || clientCertificate.isNullOrEmpty())
            null
        else ClientCertificate(
            passphrase = passphrase,
            pkcs12StoreB64 = clientCertificate
        )
    }
}