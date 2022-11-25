import AppAuth
import Foundation

public struct FailingAuthClient: AuthClient {
    enum FailingAuthClientError: Error {
        case unimplemented
    }
    
    public init() { }
    
    public func startAuth(request: OIDAuthorizationRequest) async throws -> OIDAuthState {
        throw FailingAuthClientError.unimplemented
    }
}
