import AppAuth

extension OIDAuthState {
    
    func tokens() async throws -> (accessToken: String, idToken: String?) {
        try await withCheckedThrowingContinuation { continuation in
            performAction() { accessToken, idToken, error in
                guard let accessToken else {
                    continuation.resume(throwing: error ?? NSError(domain: "Huh", code: 2))
                    return
                }
                continuation.resume(returning: (accessToken, idToken))
            }
        }
    }
    
}

