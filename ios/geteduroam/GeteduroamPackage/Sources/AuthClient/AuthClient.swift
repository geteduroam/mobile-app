import AppAuth
import Dependencies
import Foundation

public protocol AuthClient {
    func startAuth(request: OIDAuthorizationRequest) async throws -> OIDAuthState
}
