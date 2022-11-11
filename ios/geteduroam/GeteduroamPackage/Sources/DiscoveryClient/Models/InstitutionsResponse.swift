import Foundation

public struct InstitutionsResponse: Codable {
    public init(instances: [Institution]) {
        self.instances = instances
    }
    
    public let instances: [Institution]
}
