import Foundation
import XMLCoder

public struct NonEAPAuthMethod: Codable, Equatable {
    public let type: NonEAPAuthNumber
  
    enum CodingKeys: String, CodingKey {
        case type = "Type"
    }
}
