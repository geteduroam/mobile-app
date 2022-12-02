import CoreLocation
import Foundation
import Models
import NetworkExtension

public class EAPConfigurator {
    public init() { }
    
    /**
     @function getInnerAuthMethod
     @abstract Convert inner auth method integer to NEHotspotEAPSettings.TTLSInnerAuthenticationType enum
     @param innerAuthMethod Integer representing an auth method
     @result NEHotspotEAPSettings.TTLSInnerAuthenticationType representing the given auth method
     */
    class func getInnerAuthMethod(innerAuthMethod: Int?) -> NEHotspotEAPSettings.TTLSInnerAuthenticationType? {
        switch innerAuthMethod {
        case -1: // Non-EAP PAP
            return .eapttlsInnerAuthenticationPAP
        case -2: // Non-EAP MSCHAP
            return .eapttlsInnerAuthenticationMSCHAP
        case -3: // Non-EAP MSCHAPv2
            return .eapttlsInnerAuthenticationMSCHAPv2
            /*
             case _: // not in XSD
             return .eapttlsInnerAuthenticationCHAP
             */
        case 26: // EAP-MSCHAPv2 (Apple supports only this inner EAP type)
            return .eapttlsInnerAuthenticationEAP
        default:
            return nil
        }
    }
    
    /**
     @function getOuterEapType
     @abstract Convert outer EAP type integer to NEHotspotEAPSettings enum
     @param outerEapType Integer representing an EAP type
     @result NEHotspotEAPSettings.EAPType representing the given EAP type
     */
    class func getOuterEapType(outerEapType: Int) -> NEHotspotEAPSettings.EAPType? {
        switch outerEapType {
        case 13:
            return NEHotspotEAPSettings.EAPType.EAPTLS
        case 21:
            return NEHotspotEAPSettings.EAPType.EAPTTLS
        case 25:
            return NEHotspotEAPSettings.EAPType.EAPPEAP
        case 43:
            return NEHotspotEAPSettings.EAPType.EAPFAST
        default:
            return nil
        }
    }
  
    // MARK: - Configuring Access Points
    
    /// Configure Access Point
    /// - Parameter accessPoint: Parsed EAP configuration
    public func configure(accessPoint: AccessPoint) async throws {
        // At this point, we're not certain this configuration can work,
        // but we can't do this any step later, because createNetworkConfigurations will import things to the keychain.
        // TODO: only remove keychain items that match these networks
        removeNetwork(ssids: accessPoint.ssids, domains: [accessPoint.domain])
        resetKeychain()
        
        let configurations = try createNetworkConfigurations(accessPoint: accessPoint)
        
        try await NEHotspotConfigurationManager.shared.apply(configurations.last!)
//        for configuration in configurations {
//            // this line is needed in iOS 13 because there is a reported bug with iOS 13.0 until 13.1.0, where joinOnce was default true
//            // https://developer.apple.com/documentation/networkextension/nehotspotconfiguration/2887518-joinonce
//            configuration.joinOnce = false
//            // TODO: set to validity of client certificate
//            // config.lifeTimeInDays = NSNumber(integerLiteral: 825)
//
//            try await NEHotspotConfigurationManager.shared.apply(configurations.last)
//        }
    }
   
    ///  Create network configuration objects
    /// - Parameter accessPoint: Parsed EAP configuration
    /// - Returns:  Network configurations to apply
    func createNetworkConfigurations(accessPoint: AccessPoint) throws -> [NEHotspotConfiguration] {
        guard !accessPoint.oids.isEmpty || !accessPoint.ssids.isEmpty else {
            NSLog("☠️ createNetworkConfigurations: No OID or SSID in configuration")
            throw EAPConfiguratorError.noOIDOrSSID
        }
        
        let eapSettings = try buildSettings(accessPoint: accessPoint)
        
        if let outerIdentity = accessPoint.outerIdentity, outerIdentity != "" {
            // only works with EAP-TTLS, EAP-PEAP, and EAP-FAST
            // https://developer.apple.com/documentation/networkextension/nehotspoteapsettings/2866691-outeridentity
            eapSettings.outerIdentity = outerIdentity
        }
        
        if !accessPoint.serverNames.isEmpty {
            eapSettings.trustedServerNames = accessPoint.serverNames
        }
        if !accessPoint.caCertificates.isEmpty {
            var caImportStatus: Bool
            if #available(iOS 15, *) {
                // iOS 15.0.* and iOS 15.1.* have a bug where we cannot call setTrustedServerCertificates,
                // or the profile will be deemed invalid.
                // Not calling it makes the profile trust the CA bundle from the OS,
                // so only server name validation is performed.
                if #available(iOS 15.2, *) {
                    // The bug was fixed in iOS 15.2, business as usual:
                    caImportStatus = eapSettings.setTrustedServerCertificates(importCACertificates(certificateStrings: accessPoint.caCertificates))
                } else {
                    NSLog("😡 createNetworkConfigurations: iOS 15.0 and 15.1 do not accept setTrustedServerCertificates - continuing")
                    
                    // On iOS 15.0 and 15.1 we pretend everything went fine while in reality we don't even attempt; it would have crashed later on
                    caImportStatus = true
                }
            } else {
                // We are on iOS 14 or older.
                // The bug was not yet present prior to iOS 15, so business as usual:
                caImportStatus = eapSettings.setTrustedServerCertificates(importCACertificates(certificateStrings: accessPoint.caCertificates))
            }
            guard caImportStatus else {
                NSLog("☠️ createNetworkConfigurations: setTrustedServerCertificates: returned false")
                throw EAPConfiguratorError.failedToSetTrustedServerCertificates
            }
        }
        guard !accessPoint.serverNames.isEmpty || !accessPoint.caCertificates.isEmpty else {
            NSLog("😱 createNetworkConfigurations: No server names and no custom CAs set; there is no way to verify this network")
            throw EAPConfiguratorError.unableToVerifyNetwork
        }
        
        eapSettings.isTLSClientCertificateRequired = false
        
        var configurations: [NEHotspotConfiguration] = []
        
        if accessPoint.oids.count != 0 {
            let hs20 = NEHotspotHS20Settings(
                domainName: accessPoint.domain,
                roamingEnabled: true)
            hs20.roamingConsortiumOIs = accessPoint.oids
            configurations.append(NEHotspotConfiguration(hs20Settings: hs20, eapSettings: eapSettings))
        }
        
        for ssid in accessPoint.ssids {
            configurations.append(NEHotspotConfiguration(ssid: ssid, eapSettings: eapSettings))
        }
        
        guard !configurations.isEmpty else {
            throw EAPConfiguratorError.noConfigurations
        }
        
        return configurations
    }
    
    ///  Create NEHotspotEAPSettings object for client certificate authentication
    /// - Parameters:
    ///   - pkcs12: Base64 encoded client certificate
    ///   - passphrase: Passphrase to decrypt the pkcs12, Apple doesn't like password-less
    /// - Returns: NEHotspotEAPSettings configured with the provided credentials
    func buildSettingsWithClientCertificate(pkcs12: String, passphrase: String) throws -> NEHotspotEAPSettings {
        let eapSettings = NEHotspotEAPSettings()
        eapSettings.supportedEAPTypes = [NSNumber(value: NEHotspotEAPSettings.EAPType.EAPTLS.rawValue)]
        
        // TODO: certName should be the CN of the certificate,
        // but this works as long as we have only one (which we currently do)
        let identity = try addClientCertificate(certificate: pkcs12, passphrase: passphrase)
        
        guard eapSettings.setIdentity(identity) else {
            NSLog("☠️ configureAP: buildSettingsWithClientCertificate: cannot set identity")
            throw EAPConfiguratorError.cannotSetIdentity
        }
        
        return eapSettings
    }
    
    ///  Build a Hotspot EAP settings object
    /// - Parameter accessPoint:  Parsed EAP configuration
    /// - Returns: Hotspot EAP settings object
    func buildSettings(accessPoint: AccessPoint) throws -> NEHotspotEAPSettings {
        let outerEapTypes = accessPoint.outerEapTypes.compactMap(Self.getOuterEapType(outerEapType:))
        let innerAuthType = Self.getInnerAuthMethod(innerAuthMethod: accessPoint.innerAuthType)
        for outerEapType in outerEapTypes {
            switch outerEapType {
            case NEHotspotEAPSettings.EAPType.EAPTLS:
                guard let clientCertificate = accessPoint.clientCertificate, let passphrase = accessPoint.passphrase else {
                    NSLog("☠️ buildSettings: Failed precondition for EAPTLS")
                    throw EAPConfiguratorError.failedEAPTLSPrecondition
                }
                return try buildSettingsWithClientCertificate(
                    pkcs12: clientCertificate,
                    passphrase: passphrase
                )
                
            case NEHotspotEAPSettings.EAPType.EAPTTLS, NEHotspotEAPSettings.EAPType.EAPFAST, NEHotspotEAPSettings.EAPType.EAPPEAP:
                guard let username = accessPoint.username, let password = accessPoint.password else {
                    NSLog("☠️ buildSettings: Failed precondition for EAPPEAP/EAPFAST")
                    throw EAPConfiguratorError.failedEAPPEAPEAPFASTPrecondition
                }
                return try buildSettingsWithUsernamePassword(
                    outerEapTypes: outerEapTypes,
                    innerAuthType: innerAuthType,
                    username: username,
                    password: password
                )
                
            @unknown default:
                break
            }
        }
        throw EAPConfiguratorError.noOuterEAPType
    }
    
    /// Create NEHotspotEAPSettings object for username/pass authentication
    /// - Parameters:
    ///   - outerEapTypes: Outer identity
    ///   - innerAuthType: Inner auth types
    ///   - username: username for PEAP/TTLS authentication
    ///   - password: password for PEAP/TTLS authentication
    /// - Returns:  NEHotspotEAPSettings configured with the provided credentials
    func buildSettingsWithUsernamePassword(
        outerEapTypes: [NEHotspotEAPSettings.EAPType],
        innerAuthType: NEHotspotEAPSettings.TTLSInnerAuthenticationType?,
        username: String,
        password: String
    ) throws -> NEHotspotEAPSettings {
        let eapSettings = NEHotspotEAPSettings()
        
        guard username != "" && password != "" else{
            NSLog("☠️ buildSettingsWithUsernamePassword: empty user/pass")
            throw EAPConfiguratorError.emptyUsernameOrPassword
        }
        
        eapSettings.supportedEAPTypes = outerEapTypes.map() { outerEapType in NSNumber(value: outerEapType.rawValue) }
        // TODO: Default value is EAP, should we use that or MSCHAPv2?
        eapSettings.ttlsInnerAuthenticationType = innerAuthType ?? NEHotspotEAPSettings.TTLSInnerAuthenticationType.eapttlsInnerAuthenticationMSCHAPv2
        eapSettings.username = username
        eapSettings.password = password
        //NSLog("🦊 buildSettingsWithUsernamePassword: eapSettings.ttlsInnerAuthenticationType = " + String(eapSettings.ttlsInnerAuthenticationType.rawValue))
        return eapSettings
    }
    
    // MARK: - Managing Certificates and Keychain
    
    ///  Clear all items in this app's Keychain
    func resetKeychain() {
        deleteAllKeysForSecClass(kSecClassGenericPassword)
        deleteAllKeysForSecClass(kSecClassInternetPassword)
        deleteAllKeysForSecClass(kSecClassCertificate)
        deleteAllKeysForSecClass(kSecClassKey)
        deleteAllKeysForSecClass(kSecClassIdentity)
    }
    
    /**
     @function deleteAllKeysForSecClass
     @abstract Clear Keychain for given class
     @param secClass Class to clear
     */
    func deleteAllKeysForSecClass(_ secClass: CFTypeRef) {
        let dict: [NSString: CFTypeRef] = [kSecClass: secClass]
        let result = SecItemDelete(dict as CFDictionary)
        assert(result == noErr || result == errSecItemNotFound, "Error deleting keychain data (\(result))")
    }
    
    /**
     @function importCACertificates
     @abstract Import an array of Base64 encoded certificates and return an corresponding array of SecCertificate objects
     @param certificateStrings Array of Base64 CA certificates
     @result Array of SecCertificate certificates
     */
    func importCACertificates(certificateStrings: [String]) -> [SecCertificate] {
        // supporting multiple CAs
        var certificates = [SecCertificate]()
        //NSLog("🦊 configureAP: Start handling caCertificateStrings")
        for caCertificateString in certificateStrings {
            //NSLog("🦊 configureAP: caCertificateString " + caCertificateString)
            guard let certificate: SecCertificate = try? addCertificate(certificate: caCertificateString) else {
                NSLog("☠️ importCACertificates: CA certificate not added")
                continue
            }
            
            certificates.append(certificate)
        }
        
        if certificates.isEmpty {
            NSLog("☠️ importCACertificates: No certificates added")
        } else {
            //NSLog("🦊 configureAP: All caCertificateStrings handled")
        }
        
        return certificates
    }
    
    /**
     @function addCertificate
     @abstract Import Base64 encoded DER to keychain.
     @param certificate Base64 encoded DER encoded X.509 certificate
     @result Whether importing succeeded
     */
    func addCertificate(certificate: String) throws -> SecCertificate {
        guard let data = Data(base64Encoded: certificate) else {
            NSLog("☠️ Unable to base64 decode certificate data")
            throw EAPConfiguratorError.failedToBase64DecodeCertificate
        }
        guard let certificateRef = SecCertificateCreateWithData(kCFAllocatorDefault, data as CFData) else {
            NSLog("☠️ addCertificate: SecCertificateCreateWithData: false")
            throw EAPConfiguratorError.failedToCreateCertificateFromData
        }
        
        var commonNameRef: CFString?
        var status: OSStatus = SecCertificateCopyCommonName(certificateRef, &commonNameRef)
        guard status == errSecSuccess else {
            NSLog("☠️ addCertificate: unable to get common name")
            throw EAPConfiguratorError.failedToCopyCommonName
        }
        let commonName: String = commonNameRef! as String
        
        let addquery: [String: Any] = [
            kSecClass as String: kSecClassCertificate,
            kSecValueRef as String: certificateRef,
            kSecAttrLabel as String: commonName,
            kSecReturnRef as String: kCFBooleanTrue!,
            //kSecReturnPersistentRef as String: kCFBooleanTrue!, // Persistent refs cause an error (invalid EAP config) when installing the profile
            //kSecAttrAccessGroup as String: "ZYJ4TZX4UU.com.apple.networkextensionsharing", // Should be TEAMID.com.apple.networkextensionsharing, but works without?
        ]
        var item: CFTypeRef?
        status = SecItemAdd(addquery as CFDictionary, &item)
        
        // TODO: remove this, and always use the "failsafe"?
        if status == errSecSuccess && item != nil {
            return (item as! SecCertificate)
        }
        
        guard status == errSecSuccess || status == errSecDuplicateItem else {
            NSLog("☠️ addCertificate: SecItemAdd " + String(status))
            throw EAPConfiguratorError.failedSecItemAdd(status)
        }
        
        // FAILSAFE:
        // Instead of returning here, you can also run the code below
        // to make sure that the certificate was added to the KeyChain.
        // This is needed if errSecDuplicateItem was returned earlier.
        // TODO: should we use this flow always?
        
        let getquery: [String: Any] = [
            kSecClass as String: kSecClassCertificate,
            kSecAttrLabel as String: commonName,
            kSecReturnRef as String: kCFBooleanTrue!,
            //kSecReturnPersistentRef as String: kCFBooleanTrue!, // Persistent refs cause an error (invalid EAP config) when installing the profile
            //kSecAttrAccessGroup as String: "ZYJ4TZX4UU.com.apple.networkextensionsharing", // Should be TEAMID.com.apple.networkextensionsharing, but works without?
        ]
        status = SecItemCopyMatching(getquery as CFDictionary, &item)
        guard status == errSecSuccess && item != nil else {
            NSLog("☠️ addCertificate: item is nil after insert and retrieve")
            throw EAPConfiguratorError.failedSecItemCopyMatching(status)
        }
        
        return (item as! SecCertificate)
    }
    
    /// Import a PKCS12 to the keychain and return a handle to the imported item.
    /// - Parameters:
    ///   - certificate: Base64 encoded PKCS12
    ///   - passphrase: Passphrase needed to decrypt the PKCS12, required as Apple doesn't like password-less PKCS12s
    /// - Returns: SecIdentity for added item
    func addClientCertificate(certificate: String, passphrase: String) throws -> SecIdentity {
        // First we call SecPKCS12Import to read the P12,
        // then we call SecItemAdd to add items to the keychain
        // https://developer.apple.com/forums/thread/31711
        // https://developer.apple.com/documentation/security/certificate_key_and_trust_services/identities/importing_an_identity
        
        let options = [ kSecImportExportPassphrase as String: passphrase ]
        var rawItems: CFArray?
        let certificateData = Data(base64Encoded: certificate)!
        let statusImport = SecPKCS12Import(certificateData as CFData, options as CFDictionary, &rawItems)
        guard statusImport == errSecSuccess else {
            NSLog("☠️ addClientCertificate: SecPKCS12Import: " + String(statusImport))
            throw EAPConfiguratorError.failedSecPKCS12Import(String(statusImport))
        }
        let items = rawItems! as NSArray
        let item: Dictionary<String,Any> = items.firstObject as! Dictionary<String, Any>
        let identity: SecIdentity = item[kSecImportItemIdentity as String] as! SecIdentity
        let chain = item[kSecImportItemCertChain as String] as! [SecCertificate]
        if items.count > 1 {
            NSLog("😱 addClientCertificate: SecPKCS12Import: more than one result - using only first one")
        }
        
        // Import the identity to the keychain
        let addquery: [String: Any] = [
            //kSecClass as String: kSecClassIdentity, // Gives errSecInternal, according to Apple Developer Technical Support we should not specify this for client certs
            kSecValueRef as String: identity,
            kSecAttrLabel as String: "Identity", // No dual-profile support, so this name will always be unique because we will only have one client cert
            //kSecReturnRef as String: kCFBooleanTrue!, // We're not retrieving the reference at this point, 2nd argument to SecItemAdd is nil
            //kSecReturnPersistentRef as String: kCFBooleanTrue!, // Persistent refs cause an error (invalid EAP config) when installing the profile
            //kSecAttrAccessGroup as String: "ZYJ4TZX4UU.com.apple.networkextensionsharing", // Should be TEAMID.com.apple.networkextensionsharing, but works without?
        ]
        var status: OSStatus = SecItemAdd(addquery as CFDictionary, nil)
        guard status == errSecSuccess else {
            // -34018 = errSecMissingEntitlement
            // -26276 = errSecInternal
            NSLog("☠️ addClientCertificate: SecItemAdd: %d", status)
            throw EAPConfiguratorError.failedSecItemAdd(status)
        }
        
        // Import the certificate chain for this identity
        // If we don't do this, we get "failed to find the trust chain for the client certificate" when connecting
        for certificate in chain {
            let certificateRef: SecCertificate = certificate as SecCertificate
            var commonNameRef: CFString?
            var status: OSStatus = SecCertificateCopyCommonName(certificateRef, &commonNameRef)
            guard status == errSecSuccess else {
                NSLog("☠️ addClientCertificate: unable to get common name")
                continue
            }
            let commonName: String = commonNameRef! as String
            
            let addquery: [String: Any] = [
                kSecClass as String: kSecClassCertificate,
                kSecValueRef as String: certificate,
                kSecAttrLabel as String: commonName,
                //kSecReturnRef as String: kCFBooleanTrue!, // We're not retrieving the reference at this point, 2nd argument to SecItemAdd is nil
                //kSecReturnPersistentRef as String: kCFBooleanTrue!, // Persistent refs cause an error (invalid EAP config) when installing the profile
                kSecAttrAccessGroup as String: "ZYJ4TZX4UU.com.apple.networkextensionsharing", // TEAMID.com.apple.networkextensionsharing
            ]
            
            status = SecItemAdd(addquery as CFDictionary, nil)
            
            guard status == errSecSuccess || status == errSecDuplicateItem else {
                NSLog("☠️ addClientCertificate: SecItemAdd: %s: %d", commonName, status)
                throw EAPConfiguratorError.failedSecItemAdd(status, commonName: commonName)
            }
        }
        
        // Now we will retrieve the identity from the keychain again
        var newIdentity: SecIdentity
        let getquery: [String: Any] = [
            kSecClass as String: kSecClassIdentity,
            kSecAttrLabel as String: "Identity",
            kSecReturnRef as String: kCFBooleanTrue!,
            //kSecReturnPersistentRef as String: kCFBooleanTrue!, // Persistent refs cause an error (invalid EAP config) when installing the profile
            kSecAttrAccessGroup as String: "ZYJ4TZX4UU.com.apple.networkextensionsharing", // TEAMID.com.apple.networkextensionsharing
        ]
        var ref: CFTypeRef?
        status = SecItemCopyMatching(getquery as CFDictionary, &ref)
        guard status == errSecSuccess else {
            NSLog("☠️ addClientCertificate: SecItemCopyMatching: retrieving identity returned %d", status)
            throw EAPConfiguratorError.failedSecItemCopyMatching(status)
        }
        newIdentity = ref! as! SecIdentity
        
        return newIdentity
    }
    
    // MARK: - Managing Networks
    
    enum NetworkAssociationResult {
        case noNetworksFound
        case associated
        case missing
    }
    
    /**
    @function isNetworkAssociated
    @abstract Capacitor call to check if SSID is connect, doesn't work for HS20
    @param call Capacitor call object containing array "ssid"
    */
    func isNetworkAssociated(ssid: String) async -> NetworkAssociationResult {
        let configuredSSIDs = await NEHotspotConfigurationManager.shared.configuredSSIDs()
        
        guard !configuredSSIDs.isEmpty else { return .noNetworksFound }

        if configuredSSIDs.contains(ssid) {
            return .associated
        } else {
            return .missing
        }
    }

    /**
    @function removeConfiguration
    @abstract Capacitor call to remove a network
    @param call Capacitor call object containing array "ssid" and/or string "domain"
    */
    func removeNetwork(ssids: [String] = [], domains: [String] = []) {
        for ssid in ssids {
            NEHotspotConfigurationManager.shared.removeConfiguration(forSSID: ssid)
        }
        for domain in domains {
            NEHotspotConfigurationManager.shared.removeConfiguration(forHS20DomainName: domain)
        }
    }
}

public enum EAPConfiguratorError: Error {
    /// NSLog("☠️ createNetworkConfigurations: No OID or SSID in configuration")
    case noOIDOrSSID
    
    /// NSLog("☠️ createNetworkConfigurations: Unable to build a working NEHotspotEAPSettings")
    case noWorkingNEHotspotEAPSettings
    
    /// NSLog("☠️ createNetworkConfigurations: setTrustedServerCertificates: returned false")
    case failedToSetTrustedServerCertificates
    
    /// NSLog("😱 createNetworkConfigurations: No server names and no custom CAs set; there is no way to verify this network")
    case unableToVerifyNetwork
    
    /// NSLog("☠️ configureAP: buildSettingsWithClientCertificate: addClientCertificate: returned nil")
    case noClientCertificate
    
    /// NSLog("☠️ configureAP: buildSettingsWithClientCertificate: cannot set identity")
    case cannotSetIdentity
    
    // NSLog("☠️ buildSettingsWithUsernamePassword: empty user/pass")
    case emptyUsernameOrPassword
    
    case noOuterEAPType
    
    /// NSLog("☠️ buildSettings: Unknown EAPType")
    case unknownEAPType
    
    /// NSLog("☠️ buildSettings: Failed precondition for EAPTLS")
    case failedEAPTLSPrecondition
    
    /// NSLog("☠️ buildSettings: Failed precondition for EAPPEAP/EAPFAST")
    case failedEAPPEAPEAPFASTPrecondition
    
    /// NSLog("☠️ addClientCertificate: SecPKCS12Import: " + String(statusImport))
    case failedSecPKCS12Import(String)
    
    /// NSLog("☠️ addClientCertificate: SecItemAdd: %d", status)
    case failedSecItemAdd(OSStatus, commonName: String? = nil)
    
    /// NSLog("☠️ addClientCertificate: SecItemCopyMatching: retrieving identity returned %d", status)
    case failedSecItemCopyMatching(OSStatus)
    
    /// NSLog("☠️ Unable to base64 decode certificate data")
    case failedToBase64DecodeCertificate
    
    /// NSLog("☠️ addCertificate: SecCertificateCreateWithData: false")
    case failedToCreateCertificateFromData
    
    /// NSLog("☠️ addCertificate: unable to get common name")
    case failedToCopyCommonName
    
    case noConfigurations
}
