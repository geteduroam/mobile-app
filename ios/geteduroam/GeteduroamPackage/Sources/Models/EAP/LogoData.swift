import Foundation
import XMLCoder

public struct LogoData: Codable, Equatable {
    public let value: String
    public let mime: String
    public let encoding: String
    
    enum CodingKeys: String, CodingKey {
        case value = ""
        case mime
        case encoding
    }
}
