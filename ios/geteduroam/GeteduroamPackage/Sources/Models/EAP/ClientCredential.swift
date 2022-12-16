import Foundation
import XMLCoder

public struct ClientCredential: Codable, Equatable {
    public let outerIdentity: String?
    public let innerIdentityPrefix: String?
    public let innerIdentitySuffix: String?
    public let innerIdentityHint: Bool?
    public let userName: String?
    public let password: String?
    public let clientCertificate: CertificateData?
    public let intermediateCACertificates: [CertificateData]
    public let passphrase: String?
    public let PAC: String?
    public let provisionPAC: Bool?
    public let allowSave: Bool?
    
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
