import Foundation
import XMLCoder

public struct AuthenticationMethodList: Codable, Equatable {
    public let methods: [AuthenticationMethod]
    
    enum CodingKeys: String, CodingKey {
        case methods = "AuthenticationMethod"
    }
}
