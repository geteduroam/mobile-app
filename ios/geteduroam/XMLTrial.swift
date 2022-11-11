import Algorithms
import Foundation
import Fuzi

class XMLTrial: NSObject {
    
    func parse(xmlData: Data) throws -> WifiEapConfigurator.AccessPoint {
        let document = try XMLDocument(data: xmlData)
        
        let xmlRootPath = "/EAPIdentityProviderList/EAPIdentityProvider"
        let idPath = "/EAPIdentityProviderList/EAPIdentityProvider/@ID"
        let ieee80211Path = xmlRootPath + "/CredentialApplicability/IEEE80211"
        let authenticationMethodPath = xmlRootPath + "/AuthenticationMethods/AuthenticationMethod"
        let serverSideCredentialPath = authenticationMethodPath + "/ServerSideCredential"
        let clientSideCredentialPath = authenticationMethodPath + "/ClientSideCredential"
        let ssidsPath = ieee80211Path + "/SSID/text()"
        let oidsPath = ieee80211Path + "/ConsortiumOID/text()"
        let outerIdentityPath = clientSideCredentialPath + "/OuterIdentity/text()"
        let clientCertificatePassphrasePath = clientSideCredentialPath + "/Passphrase/text()"
        let clientCertificateDataPath = clientSideCredentialPath + "/ClientCertificate/text()"
//        let anonymousIdentityPath = clientSideCredentialPath + "/OuterIdentity/text()"
        let eapMethodsPath = authenticationMethodPath + "/EAPMethod/Type/text()"
        let caCertificatesPath = serverSideCredentialPath + "/CA/text()"
        let serverNamesPath = serverSideCredentialPath + "/ServerID/text()"
        let usernamePath = clientSideCredentialPath + "/UserName/text()"
        let passwordPath = clientSideCredentialPath + "/Password/text()"
        
        let id = document.xpath(idPath).compactMap { $0.stringValue }.first
        let ssids = document.xpath(ssidsPath).compactMap { $0.stringValue }.uniqued { $0 }
        let oids = document.xpath(oidsPath).compactMap { $0.stringValue }.uniqued { $0 }
        let outerIdentity = document.xpath(outerIdentityPath).compactMap { $0.stringValue }.first
        let clientCertificatePassphrase = document.xpath(clientCertificatePassphrasePath).compactMap { $0.stringValue }.first
        let clientCertificateData = document.xpath(clientCertificateDataPath).compactMap { $0.stringValue }.first
        let clientCertificate: ClientCertificate?
        if let clientCertificatePassphrase, let clientCertificateData {
            clientCertificate = .init(passphrase: clientCertificatePassphrase, pkcs12StoreB64: clientCertificateData)
        } else {
            clientCertificate = nil
        }
//        let anonymousIdentity = document.xpath(anonymousIdentityPath).compactMap { $0.stringValue }.first
        let eapMethods = document.xpath(eapMethodsPath).compactMap { Int($0.stringValue) }
        let caCertificates = document.xpath(caCertificatesPath).compactMap { $0.stringValue }.uniqued { $0 }
        let enterpriseEAP = 0
        let serverNames = document.xpath(serverNamesPath).compactMap { $0.stringValue }.uniqued { $0 }
        let outerEapTypes = eapMethods.compactMap(WifiEapConfigurator.getOuterEapType(outerEapType:))
        let username = document.xpath(usernamePath).compactMap { $0.stringValue }.first
        let password = document.xpath(passwordPath).compactMap { $0.stringValue }.first
        let enterprisePhase2Auth = 0
        let fqdn: String? = nil
        
        return WifiEapConfigurator.AccessPoint(
            id: id!,
            domain: id!,
            ssids: ssids,
            oids: oids,
            outerIdentity: outerIdentity!,
            serverNames: serverNames,
            outerEapTypes: outerEapTypes,
            innerAuthType: WifiEapConfigurator.getInnerAuthMethod(innerAuthMethod: 0),
            clientCertificate: clientCertificate?.pkcs12StoreB64,
            passphrase: clientCertificate?.passphrase,
            username: username,
            password: password,
            caCertificates: caCertificates)

//        return WifiConfigData(ssids: ssids, oids: oids, clientCertificate: clientCertificate, anonymousIdentity: outerIdentity, caCertificates: caCertificates, enterpriseEAP: enterpriseEAP, serverNames: serverNames, username: username, password: password, enterprisePhase2Auth: enterprisePhase2Auth, fqdn: fqdn)
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
