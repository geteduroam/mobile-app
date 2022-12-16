import Foundation
import XMLCoder

public struct ServerCredential: Codable, Equatable {
    public let certificates: [CertificateData]
    public let serverIDs: [String]
    
    enum CodingKeys: String, CodingKey {
        case certificates = "CA"
        case serverIDs = "ServerID"
    }
}
