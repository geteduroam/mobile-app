import Foundation
import XMLCoder

public struct AuthenticationMethod: Codable, Equatable {
    public let EAPMethod: EAPMethod
    public let serverSideCredential: ServerCredential?
    public let clientSideCredential: ClientCredential?
    public let innerAuthenticationMethods: [InnerAuthenticationMethod]
    
    enum CodingKeys: String, CodingKey {
        case EAPMethod
        case serverSideCredential = "ServerSideCredential"
        case clientSideCredential = "ClientSideCredential"
        case innerAuthenticationMethods = "InnerAuthenticationMethod"
    }
}
