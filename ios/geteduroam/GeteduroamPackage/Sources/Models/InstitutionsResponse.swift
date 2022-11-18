import Foundation

public struct InstitutionsResponse: Codable, Equatable {
    public init(instances: [Institution]) {
        self.instances = instances
    }
    
    public let instances: [Institution]
}
