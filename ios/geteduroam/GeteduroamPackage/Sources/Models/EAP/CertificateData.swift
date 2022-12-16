import Foundation
import XMLCoder

public struct CertificateData: Codable, Equatable {
    public let value: String
    public let format: String
    public let encoding: String
    
    enum CodingKeys: String, CodingKey {
        case value = ""
        case format
        case encoding
    }
}
