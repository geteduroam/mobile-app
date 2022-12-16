import Foundation
import XMLCoder

public struct InnerAuthenticationMethod: Codable, Equatable {
    public let EAPMethod: EAPMethod?
    public let nonEAPAuthMethod: NonEAPAuthMethod?
    public let serverSideCredential: ServerCredential?
    public let clientSideCredential: ClientCredential?
    
    enum CodingKeys: String, CodingKey {
        case EAPMethod
        case nonEAPAuthMethod = "NonEAPAuthMethod"
        case serverSideCredential = "ServerSideCredential"
        case clientSideCredential = "ClientSideCredential"
    }
}
