import URLRouting
import Foundation
import SwiftUI

//enum Route {
//    case discover
//}
//
//let router = OneOf {
//    Path {
//        "discover"
//    }
//}
//
//router.request(for: Route.discover)
// https://www.pointfree.co/episodes/ep189-tour-of-parser-printers-api-clients-for-free#downloads
enum RoamRoute: Equatable {
    case discover
//    case config(URL, String)
}


let router = OneOf {
    Route(.case(RoamRoute.discover)) {
        Path { "discovery.json" }
    }
//    Route(.case(RoamRoute.config(url, token))) {
//        Path { url.path }
//    }
//    .baseURL(url?.absoluteString)
}

let a = Task {
    let apiClient = URLRoutingClient.live(router: router.baseURL("https://discovery.eduroam.app/v1/"))
    
    let institutions = try await apiClient.decodedResponse(for: .discover, as: InstitutionsResponse.self).value.instances
    let url = URL(string: "")!
    
//    try await apiClient.data(for: .config(url)).
}

//extension InstitutionsResponse: Codable { }

struct InstitutionsResponse: Codable {
    let instances: [Institution2]
 
}


struct Institution2: Codable {
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
