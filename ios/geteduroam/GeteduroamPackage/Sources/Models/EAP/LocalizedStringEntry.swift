import Foundation
import XMLCoder

public struct LocalizedStringEntry: Codable, Equatable {
    public let language: String?
    public let value: String
    
    enum CodingKeys: String, CodingKey {
        case language = "lang"
        case value = ""
    }
}
