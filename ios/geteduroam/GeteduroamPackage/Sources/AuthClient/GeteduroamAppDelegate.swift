import AppAuth
import Foundation

public enum StartAuthError: Error {
    case noWindow
    case noRootViewController
    case unknownError
}

public class GeteduroamAppDelegate: NSObject, UIApplicationDelegate, ObservableObject, AuthClient {
    private var currentAuthorizationFlow: OIDExternalUserAgentSession?
  
    public func startAuth(request: OIDAuthorizationRequest) async throws -> OIDAuthState {
        guard let window = UIApplication.shared.keyWindow else {
            throw StartAuthError.noWindow
        }
        guard let presenter = window.rootViewController else {
            throw StartAuthError.noRootViewController
        }
        return try await withCheckedThrowingContinuation { continuation in
            self.currentAuthorizationFlow = OIDAuthState.authState(byPresenting: request, presenting: presenter) { authState, error in
                if let authState {
                    continuation.resume(returning: authState)
                } else if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(throwing: StartAuthError.unknownError)
                }
            }
        }
    }

    public func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        // Sends the URL to the current authorization flow (if any) which will process it if it relates to an authorization response.
        if let currentAuthorizationFlow, currentAuthorizationFlow.resumeExternalUserAgentFlow(with: url) {
            self.currentAuthorizationFlow = nil
            return true
        }
        
        return false
    }
}

extension UIApplication {
    
    var keyWindow: UIWindow? {
        // Get connected scenes
        return UIApplication.shared.connectedScenes
            // Keep only active scenes, onscreen and visible to the user
            .filter { $0.activationState == .foregroundActive }
            // Keep only the first `UIWindowScene`
            .first(where: { $0 is UIWindowScene })
            // Get its associated windows
            .flatMap({ $0 as? UIWindowScene })?.windows
            // Finally, keep only the key window
            .first(where: \.isKeyWindow)
    }
    
}
