import Foundation
import XMLCoder

public struct EAPMethod: Codable, Equatable {
    public let type: Int
    
    enum CodingKeys: String, CodingKey {
        case type = "Type"
    }
}
