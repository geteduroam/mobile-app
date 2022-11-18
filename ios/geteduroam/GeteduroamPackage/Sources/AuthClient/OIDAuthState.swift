import AppAuth

extension OIDAuthState {
    
    public func tokens() async throws -> (accessToken: String, idToken: String?) {
        try await withCheckedThrowingContinuation { continuation in
            performAction() { accessToken, idToken, error in
                guard let accessToken else {
                    continuation.resume(throwing: error ?? OIDAuthStateError.neitherAccessTokenNorErrorAvailable)
                    return
                }
                continuation.resume(returning: (accessToken, idToken))
            }
        }
    }
    
}

public enum OIDAuthStateError: Error, LocalizedError {
    case neitherAccessTokenNorErrorAvailable
    
    public var errorDescription: String? {
        switch self {
        case .neitherAccessTokenNorErrorAvailable:
            return NSLocalizedString("No access token obtained for unknown reason", comment: "OIDAuthStateError neitherAccessTokenNorErrorAvailable")
        }
    }
}
