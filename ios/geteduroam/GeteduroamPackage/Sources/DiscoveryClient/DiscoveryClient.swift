import Dependencies
import Foundation
import URLRouting

extension DependencyValues {
    public var discoveryClient: URLRoutingClient<DiscoveryRoute> {
        get { self[DiscoveryClientKey.self] }
        set { self[DiscoveryClientKey.self] = newValue }
    }
    
    public enum DiscoveryClientKey: TestDependencyKey {
        public static var testValue = mockClient
    }
}

extension DependencyValues.DiscoveryClientKey: DependencyKey {
    public static let liveValue = liveClient
}

private let mockClient = URLRoutingClient<DiscoveryRoute>.failing
private let liveClient = URLRoutingClient.live(router: discoveryRouter.baseURL(Bundle.main.infoDictionary!["DiscoveryURL"] as! String))
