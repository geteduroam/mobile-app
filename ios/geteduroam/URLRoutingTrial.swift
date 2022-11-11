import URLRouting
import Foundation
import SwiftUI

enum RoamRoute: Equatable {
    case discover
}

let router = OneOf {
    Route(.case(RoamRoute.discover)) {
        Path { "discovery.json" }
    }
}

struct InstitutionsResponse: Codable {
    let instances: [Institution]
}

struct Institution: Codable {
    let id: String
    let name: String
    let country: String
    let cat_idp: Int
    let profiles: [Profile]
    let geo: [Coordinate]
    
    var hasSingleProfile: Bool {
        profiles.count == 1
    }
    
    var requiresAuth: Bool {
        if hasSingleProfile {
            return profiles[0].oauth ?? false
        } else {
            return false
        }
    }
}

struct Profile: Codable {
    let id: String
    let name: String
    let `default`: Bool?
    let eapconfig_endpoint: URL?
    let oauth: Bool?
    let authorization_endpoint: URL?
    let token_endpoint: URL?
}

struct Coordinate: Codable {
    let lat: Double
    let lon: Double
}
