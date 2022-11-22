import Foundation
import URLRouting

public enum DiscoveryRoute: Equatable {
    case discover
}

public let discoveryRouter = OneOf {
    Route(.case(DiscoveryRoute.discover)) {
        Path { "discovery.json" }
    }
}
