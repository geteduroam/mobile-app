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
    case config(URL)
}


let router = OneOf {
    Route(.case(RoamRoute.discover)) {
        Path { "discovery.json" }
    }
//    Route(.case(RoamRoute.config(<#T##UUID#>))) {
//
//    }
}

let a = Task {
    let apiClient = URLRoutingClient.live(router: router.baseURL("https://discovery.eduroam.app/v1/"))
    
    let institutions = try await apiClient.decodedResponse(for: .discover, as: InstitutionsResponse.self).value.institutions
}

//extension InstitutionsResponse: Codable { }

struct InstitutionsResponse: Codable {
  let institutions: [Institution]
  struct Institution: Codable {
    let id: UUID
    let title: String
    let bookURL: URL
  }
}
