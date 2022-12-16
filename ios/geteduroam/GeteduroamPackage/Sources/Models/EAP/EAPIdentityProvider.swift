import Foundation
import XMLCoder

public struct EAPIdentityProvider: Codable, Equatable {
    public let id: String
    public let validUntil: Date?
    public let authenticationMethods: AuthenticationMethodList // at least 1!
    public let credentialApplicability: CredentialApplicability
    public let providerInfo: ProviderInfo?

    enum CodingKeys: String, CodingKey {
        case id = "ID"
        case validUntil = "ValidUntil"
        case authenticationMethods = "AuthenticationMethods"
        case credentialApplicability = "CredentialApplicability"
        case providerInfo = "ProviderInfo"
    }
}
