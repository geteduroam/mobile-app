import Algorithms
import Foundation
import Fuzi

class XMLTrial: NSObject {
    
    func parse(xmlString: String) throws -> WifiConfigData {
        let document = try XMLDocument(string: xmlString)
        
        let xmlRootPath = "/EAPIdentityProviderList/EAPIdentityProvider"
        let ieee80211Path = xmlRootPath + "/CredentialApplicability/IEEE80211"
        let serverSideCredentialPath = xmlRootPath + "/AuthenticationMethods/AuthenticationMethod/ServerSideCredential"
        let clientSideCredentialPath = xmlRootPath + "/AuthenticationMethods/AuthenticationMethod/ClientSideCredential"
        let ssidsPath = ieee80211Path + "/SSID/text()"
        let oidsPath = ieee80211Path + "/ConsortiumOID/text()"
        let clientCertificatePassphrasePath = clientSideCredentialPath + "/Passphrase/text()"
        let clientCertificateDataPath = clientSideCredentialPath + "/ClientCertificate/text()"
        let anonymousIdentityPath = clientSideCredentialPath + "/OuterIdentity/text()"
        let caCertificatesPath = serverSideCredentialPath + "/CA/text()"
        let serverNamesPath = serverSideCredentialPath + "/ServerID/text()"
        let usernamePath = clientSideCredentialPath + "/UserName/text()"
        let passwordPath = clientSideCredentialPath + "/Password/text()"
        
        let ssids = document.xpath(ssidsPath).compactMap { $0.stringValue }.uniqued { $0 }
        let oids = document.xpath(oidsPath).compactMap { $0.stringValue }.uniqued { $0 }
        let clientCertificatePassphrase = document.xpath(anonymousIdentityPath).compactMap { $0.stringValue }.first
        let clientCertificateData = document.xpath(clientCertificateDataPath).compactMap { $0.stringValue }.first
        let clientCertificate: ClientCertificate?
        if let clientCertificatePassphrase, let clientCertificateData {
            clientCertificate = .init(passphrase: clientCertificatePassphrase, pkcs12StoreB64: clientCertificateData)
        } else {
            clientCertificate = nil
        }
        let anonymousIdentity = document.xpath(anonymousIdentityPath).compactMap { $0.stringValue }.first
        let caCertificates = document.xpath(caCertificatesPath).compactMap { $0.stringValue }.uniqued { $0 }
        let enterpriseEAP = 0
        let serverNames = document.xpath(serverNamesPath).compactMap { $0.stringValue }.uniqued { $0 }
        let username = document.xpath(usernamePath).compactMap { $0.stringValue }.first
        let password = document.xpath(passwordPath).compactMap { $0.stringValue }.first
        let enterprisePhase2Auth = 0
        let fqdn: String? = nil
        
        return WifiConfigData(ssids: ssids, oids: oids, clientCertificate: clientCertificate, anonymousIdentity: anonymousIdentity, caCertificates: caCertificates, enterpriseEAP: enterpriseEAP, serverNames: serverNames, username: username, password: password, enterprisePhase2Auth: enterprisePhase2Auth, fqdn: fqdn)
    }
    
}

struct WifiConfigData: Codable {
    let ssids: [String]
    let oids: [String]
    var clientCertificate: ClientCertificate?
    let anonymousIdentity: String?
    // Working with certificate as base64 encoded strings, to be parsed by the platform into platform specific type.
    let caCertificates: [String]?
    let enterpriseEAP: Int
    let serverNames: [String]?
    let username: String?
    let password: String?
    let enterprisePhase2Auth: Int
    let fqdn: String?
}

struct ClientCertificate: Codable {
    let passphrase: String
    let pkcs12StoreB64: String
}
