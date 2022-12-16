import Foundation
import XMLCoder

public struct HelpdeskDetails: Codable, Equatable {
    public let emailAdress: LocalizedString?
    public let webAddress: LocalizedString?
    public let phone: LocalizedString?
    
    enum CodingKeys: String, CodingKey {
        case emailAdress = "EmailAdress"
        case webAddress = "WebAddress"
        case phone = "Phone"
    }
}
