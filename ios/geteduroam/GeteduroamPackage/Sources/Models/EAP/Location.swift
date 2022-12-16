import Foundation
import XMLCoder

public struct Location: Codable, Equatable {
    public let latitude: Double
    public let longitude: Double
    
    enum CodingKeys: String, CodingKey {
        case latitude = "Latitude"
        case longitude = "Longitude"
    }
}
