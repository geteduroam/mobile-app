package app.eduroam.shared.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.Reader
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


class AndroidConfigParser : ConfigParser {
    private val xmlRootPath = "/EAPIdentityProviderList/EAPIdentityProvider"
    private val ieee80211path = "$xmlRootPath/CredentialApplicability/IEEE80211"
    private val serverSideCredentialPath =
        "$xmlRootPath/AuthenticationMethods/AuthenticationMethod/ServerSideCredential"
    private val clientSideCredentialPath =
        "$xmlRootPath/AuthenticationMethods/AuthenticationMethod/ClientSideCredential"

    override suspend fun parse(source: ByteArray): WifiConfigData = withContext(Dispatchers.IO) {
        val xPath: XPath = XPathFactory.newInstance().newXPath()

        WifiConfigData(
            ssids = xPath.nodeValues(
                reader = getReader(source),
                expression = "$ieee80211path/SSID/text()"
            ).distinct(),
            oids = xPath.nodeValues(
                reader = getReader(source),
                expression = "$ieee80211path/ConsortiumOID/text()"
            ).map { it.toLong(16) }.distinct(),
            clientCertificate = getClientCertificate(xPath, source),
            anonymousIdentity = xPath.nodeValues(
                reader = getReader(source),
                expression = "$clientSideCredentialPath/OuterIdentity/text()"
            ).firstOrNull(),
            caCertificates = xPath.nodeValues(
                reader = getReader(source),
                expression = "$serverSideCredentialPath/CA/text()"
            ).distinct(),
            serverNames = xPath.nodeValues(
                reader = getReader(source),
                expression = "$serverSideCredentialPath/ServerID/text()"
            ).distinct(),
            username = xPath.nodeValues(
                reader = getReader(source),
                expression = "$clientSideCredentialPath/UserName/text()"
            ).firstOrNull(),
            password = xPath.nodeValues(
                reader = getReader(source),
                expression = "$clientSideCredentialPath/Password/text()"
            ).firstOrNull()
        )
    }

    private fun getReader(source: ByteArray) = InputStreamReader(ByteArrayInputStream(source))

    private fun getClientCertificate(xPath: XPath, source: ByteArray) : ClientCertificate? {
        val passphrase = xPath.nodeValues(
            reader = getReader(source), expression = "$clientSideCredentialPath/Passphrase/text()"
        ).firstOrNull()
        val clientCertificate = xPath.nodeValues(
            reader = getReader(source),
            expression = "$clientSideCredentialPath/ClientCertificate/text()"
        ).firstOrNull()

        return if (passphrase.isNullOrEmpty() || clientCertificate.isNullOrEmpty())
            null
        else ClientCertificate(
            passphrase = passphrase,
            pkcs12StoreB64 = clientCertificate
        )
    }

    private fun XPath.nodeValues(reader: Reader, expression: String): List<String> = (this.evaluate(
        expression,
        InputSource(reader),
        XPathConstants.NODESET
    ) as NodeList).nodeValues()

    private fun NodeList.nodeValues(): List<String> {
        val list = mutableListOf<String>()

        for (i in 0 until this.length) {
            list.add(this.item(i).nodeValue)
        }

        return list
    }
}