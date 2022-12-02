import Foundation
import XMLCoder

#if canImport(UIKit)
import UIKit
typealias Image = UIImage
#elseif canImport(AppKit)
import AppKit
typealias Image = NSImage
#endif

struct EAPMethod: Codable, Equatable {
    let type: Int
    
    enum CodingKeys: String, CodingKey {
        case type = "Type"
    }
}

enum NonEAPAuthNumber: Int, Codable, Equatable {
    case PAP = 1
    case MSCHAP = 2
    case MSCHAPv2 = 3
}

enum IEEE80211RSNProtocol: String, Codable, Equatable {
    case TKIP
    case CCMP
}

struct HelpdeskDetails: Codable, Equatable {
    let emailAdress: LocalizedString?
    let webAddress: LocalizedString?
    let phone: LocalizedString?
    
    enum CodingKeys: String, CodingKey {
        case emailAdress = "EmailAdress"
        case webAddress = "WebAddress"
        case phone = "Phone"
    }
}

//enum LocalizedString: Codable, Equatable {
//    case simple(String)
//    case localized([LocalizedStringEntry])
//
//    init(string: String) {
//        self = .simple(string)
//    }
//
//    init(localized: [LocalizedStringEntry]) {
//        self = .localized(localized)
//    }
//
////    enum CodingKeys: String, XMLChoiceCodingKey {
////           case simp
////           case string
////       }
//
//}

typealias LocalizedString = String // [LocalizedStringEntry]

struct LocalizedStringEntry: Codable, Equatable {
    let language: String?
    let value: String
    
    enum CodingKeys: String, CodingKey {
        case language = "Language"
        case value
    }
}

struct Location: Codable, Equatable {
    let latitude: Double
    let longitude: Double
    
    enum CodingKeys: String, CodingKey {
        case latitude = "Latitude"
        case longitude = "Longitude"
    }
}

struct LogoData: Codable, Equatable {
    let value: String
    let mime: String
    let encoding: String
}

struct ProviderInfo: Codable, Equatable {
    let displayName: LocalizedString?
    let description: LocalizedString?
    let providerLocations: [Location]
    let providerLogo: LogoData?
    let termsOfUse: LocalizedString?
    let helpdesk: HelpdeskDetails?
    
    enum CodingKeys: String, CodingKey {
        case displayName = "DisplayName"
        case description = "Description"
        case providerLocations = "ProviderLocation"
        case providerLogo = "ProviderLogo"
        case termsOfUse = "TermsOfUse"
        case helpdesk = "Helpdesk"
    }
}

struct EEE80211Properties: Codable, Equatable {
    let ssid: String?
    let consortiumOID: String?
    let minRSNProto: IEEE80211RSNProtocol?
    
    enum CodingKeys: String, CodingKey {
        case ssid = "SSID"
        case consortiumOID = "ConsortiumOID"
        case minRSNProto = "MinRSNProto"
    }
}

struct IEEE8023Properties: Codable, Equatable {
    let networkID: String?
    
    enum CodingKeys: String, CodingKey {
        case networkID = "NetworkID"
    }
}

struct CredentialApplicability: Codable, Equatable {
    let IEEE80211: [EEE80211Properties]
    let IEEE8023: [IEEE8023Properties]
    
    enum CodingKeys: String, CodingKey {
        case IEEE80211
        case IEEE8023
    }
}

struct CertificateData: Codable, Equatable {
    let value: String
    let format: String
    let encoding: String
    
    enum CodingKeys: String, CodingKey {
        case value = ""
        case format
        case encoding
    }
}

struct ClientCredential: Codable, Equatable {
    let outerIdentity: String?
    let innerIdentityPrefix: String?
    let innerIdentitySuffix: String?
    let innerIdentityHint: Bool?
    let userName: String?
    let password: String?
    let clientCertificate: CertificateData?
    let intermediateCACertificates: [CertificateData]
    let passphrase: String?
    let PAC: String?
    let provisionPAC: Bool?
    let allowSave: Bool?
    
    enum CodingKeys: String, CodingKey {
        case outerIdentity = "OuterIdentity"
        case innerIdentityPrefix = "InnerIdentityPrefix"
        case innerIdentitySuffix = "InnerIdentitySuffix"
        case innerIdentityHint = "InnerIdentityHint"
        case userName = "UserName"
        case password = "Password"
        case clientCertificate = "ClientCertificate"
        case intermediateCACertificates = "IntermediateCACertificate"
        case passphrase = "Passphrase"
        case PAC
        case provisionPAC = "ProvisionPAC"
        case allowSave
    }
}

struct ServerCredential: Codable, Equatable {
    let certificates: [CertificateData]
    let serverIDs: [String]
    
    enum CodingKeys: String, CodingKey {
        case certificates = "CA"
        case serverIDs = "ServerID"
    }
}

struct EAPIdentityProvider: Codable, Equatable {
    let validUntil: Date?
    let authenticationMethods: AuthenticationMethodList // at least 1!
    let credentialApplicability: CredentialApplicability
    let providerInfo: ProviderInfo?

    enum CodingKeys: String, CodingKey {
        case validUntil = "ValidUntil"
        case authenticationMethods = "AuthenticationMethods"
        case credentialApplicability = "CredentialApplicability"
        case providerInfo = "ProviderInfo"
    }
}

struct AuthenticationMethod: Codable, Equatable {
    let EAPMethod: EAPMethod
    let serverSideCredential: ServerCredential?
    let clientSideCredential: ClientCredential?
    let innerAuthenticationMethods: [InnerAuthenticationMethod]
    
    enum CodingKeys: String, CodingKey {
        case EAPMethod
        case serverSideCredential = "ServerSideCredential"
        case clientSideCredential = "ClientSideCredential"
        case innerAuthenticationMethods = "InnerAuthenticationMethod"
    }
}

struct InnerAuthenticationMethod: Codable, Equatable {
    let EAPMethod: EAPMethod?
    let nonEAPAuthMethod: NonEAPAuthMethod?
    let serverSideCredential: ServerCredential?
    let clientSideCredential: ClientCredential?
    
    enum CodingKeys: String, CodingKey {
        case EAPMethod
        case nonEAPAuthMethod = "NonEAPAuthMethod"
        case serverSideCredential = "ServerSideCredential"
        case clientSideCredential = "ClientSideCredential"
    }
}

struct NonEAPAuthMethod: Codable, Equatable {
    let type: NonEAPAuthNumber
  
    enum CodingKeys: String, CodingKey {
        case type = "Type"
    }
}

struct EAPIdentityProviderList: Codable, Equatable {
    let providers: [EAPIdentityProvider]
    
    enum CodingKeys: String, CodingKey {
        case providers = "EAPIdentityProvider"
    }
}

struct AuthenticationMethodList: Codable, Equatable {
    let methods: [AuthenticationMethod]
    
    enum CodingKeys: String, CodingKey {
        case methods = "AuthenticationMethod"
    }
}
