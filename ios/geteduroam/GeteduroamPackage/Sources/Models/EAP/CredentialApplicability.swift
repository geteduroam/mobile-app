import Foundation
import XMLCoder

public struct CredentialApplicability: Codable, Equatable {
    public let IEEE80211: [EEE80211Properties]
    public let IEEE8023: [IEEE8023Properties]
    
    enum CodingKeys: String, CodingKey {
        case IEEE80211
        case IEEE8023
    }
}

