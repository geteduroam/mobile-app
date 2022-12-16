import Foundation

public struct Institution: Codable, Identifiable, Equatable {
    public init(id: String, name: String, country: String, cat_idp: Int, profiles: [Profile], geo: [Coordinate]) {
        self.id = id
        self.name = name
        self.country = country
        self.cat_idp = cat_idp
        self.profiles = profiles
        self.geo = geo
    }
    
    public let id: String
    public let name: String
    public let country: String
    public let cat_idp: Int
    public let profiles: [Profile]
    public let geo: [Coordinate]
    
    public var hasSingleProfile: Bool {
        profiles.count == 1
    }
}
