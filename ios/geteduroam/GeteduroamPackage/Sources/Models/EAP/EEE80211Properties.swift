import Foundation
import XMLCoder

public struct EEE80211Properties: Codable, Equatable {
    public let ssid: String?
    public let consortiumOID: String?
    public let minRSNProto: IEEE80211RSNProtocol?
    
    enum CodingKeys: String, CodingKey {
        case ssid = "SSID"
        case consortiumOID = "ConsortiumOID"
        case minRSNProto = "MinRSNProto"
    }
}
