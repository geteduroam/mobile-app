import Foundation

public enum EAPConfiguratorError: Error {
    /// NSLog("☠️ createNetworkConfigurations: No OID or SSID in configuration")
    case noOIDOrSSID
    
    /// NSLog("☠️ createNetworkConfigurations: setTrustedServerCertificates: returned false")
    case failedToSetTrustedServerCertificates
    
    /// NSLog("😱 createNetworkConfigurations: No server names and no custom CAs set; there is no way to verify this network")
    case unableToVerifyNetwork
    
    /// NSLog("☠️ configureAP: buildSettingsWithClientCertificate: cannot set identity")
    case cannotSetIdentity
    
    // NSLog("☠️ buildSettingsWithUsernamePassword: empty user/pass")
    case emptyUsernameOrPassword
    
    case noOuterEAPType
    
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
    
    case cannotCopySupportedInterfaces
}

extension EAPConfiguratorError: LocalizedError {
    public var errorDescription: String? {
        switch self {
        // NSLog("☠️ createNetworkConfigurations: No OID or SSID in configuration")
        case .noOIDOrSSID:
            return NSLocalizedString("No OID or SSID in configuration", comment: "No OID or SSID in configuration")
            
        // NSLog("☠️ createNetworkConfigurations: setTrustedServerCertificates: returned false")
        case .failedToSetTrustedServerCertificates:
            return NSLocalizedString("Unable to set server certificate as trusted", comment: "setTrustedServerCertificates: returned false")
            
        // NSLog("😱 createNetworkConfigurations: No server names and no custom CAs set; there is no way to verify this network")
        case .unableToVerifyNetwork:
            return NSLocalizedString("Unable to verify network because no server name or certificate set", comment: "No server names and no custom CAs set")
            
        // NSLog("☠️ configureAP: buildSettingsWithClientCertificate: cannot set identity")
        case .cannotSetIdentity:
            return NSLocalizedString("Unable to set identity for client certificate", comment: "ClientCertificate: cannot set identity")
            
        // NSLog("☠️ buildSettingsWithUsernamePassword: empty user/pass")
        case .emptyUsernameOrPassword:
            return NSLocalizedString("No credentials in configuration", comment: "empty user/pass")
            
        case .noOuterEAPType:
            return NSLocalizedString("No valid outer EAP type in configuration", comment: "noOuterEAPType")
            
        // NSLog("☠️ addClientCertificate: SecPKCS12Import: " + String(statusImport))
        case let .failedSecPKCS12Import(status):
            return String(format: NSLocalizedString("Unable to import certificate into keychain %@", comment: "addClientCertificate"), status)
            
        // NSLog("☠️ addClientCertificate: SecItemAdd: %d", status)
        case let .failedSecItemAdd(status, commonName):
            return String(format: NSLocalizedString("Unable to add certificate %@ to keychain (%d)", comment: "addClientCertificate: SecItemAdd"), commonName ?? NSLocalizedString("with unknown name", comment: "with unknown name"), status)
            
        // NSLog("☠️ addClientCertificate: SecItemCopyMatching: retrieving identity returned %d", status)
        case let .failedSecItemCopyMatching(status):
            return String(format: NSLocalizedString("Unable to copy from keychain (%d)", comment: "SecItemCopyMatching: retrieving identity returned"), status)
            
        // NSLog("☠️ Unable to base64 decode certificate data")
        case .failedToBase64DecodeCertificate:
            return NSLocalizedString("Unable to decode certificate data", comment: "Unable to base64 decode certificate data")
            
        // NSLog("☠️ addCertificate: SecCertificateCreateWithData: false")
        case .failedToCreateCertificateFromData:
            return NSLocalizedString("Unable to create certificate from data", comment: "SecCertificateCreateWithData: false")
            
        // NSLog("☠️ addCertificate: unable to get common name")
        case .failedToCopyCommonName:
            return NSLocalizedString("Unable to get common name from certificate", comment: "Certificate: unable to get common name")
            
        case .noConfigurations:
            return NSLocalizedString("No valid configuration found", comment: "noConfigurations")
            
        case .cannotCopySupportedInterfaces:
            return NSLocalizedString("Unable to read supported interfaces", comment: "cannotCopySupportedInterfaces")
            
        }
    }
}
