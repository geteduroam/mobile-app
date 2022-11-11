import SwiftUI
import URLRouting
import AppAuth

@main
struct GeteduroamApp: App {
    
    #if os(iOS)
    @UIApplicationDelegateAdaptor private var appDelegate: GeteduroamAppDelegate
    #elseif os(macOS)
    @NSApplicationDelegateAdaptor private var appDelegate: GeteduroamAppDelegate
    #endif
    
	var body: some Scene {
		WindowGroup {
			Text("Placeholder")
                .task {
                    do {
                        let apiClient = URLRoutingClient.live(router: router.baseURL("https://discovery.eduroam.app/v1/"))
                        
                        let institutions = try await apiClient.decodedResponse(for: .discover, as: InstitutionsResponse.self).value.instances
                        
                        print("Found \(institutions.count) institutions")
                        
                        let someInstitutionToConnectTo = institutions.first(where: { $0.name.contains("Moreel") })
                        
                        print("Connecting to \(someInstitutionToConnectTo?.name ?? "?")")
                        
                        if let profile = someInstitutionToConnectTo?.profiles[0] {

                            let authorizationEndpoint = profile.authorization_endpoint!
                            let tokenEndpoint = profile.token_endpoint!
                            let configuration = OIDServiceConfiguration(authorizationEndpoint: authorizationEndpoint,
                                                                        tokenEndpoint: tokenEndpoint)

                            // perform the auth request...
                            
                            // builds authentication request
                            let clientId = "app.eduroam.geteduroam"
                            let scope = "eap-metadata"
                            let redirectURL = URL(string: "app.eduroam.geteduroam:/")!
                            let codeVerifier = OIDAuthorizationRequest.generateCodeVerifier()
                            let codeChallange = OIDAuthorizationRequest.codeChallengeS256(forVerifier: codeVerifier)
                            let state = UUID().uuidString
                            let nonce = UUID().uuidString
                         
                            let request = OIDAuthorizationRequest(configuration: configuration, clientId: clientId, clientSecret: nil, scope: scope, redirectURL: redirectURL, responseType: OIDResponseTypeCode, state: state, nonce: nonce, codeVerifier: codeVerifier, codeChallenge: codeChallange, codeChallengeMethod: OIDOAuthorizationRequestCodeChallengeMethodS256, additionalParameters: nil)

                            print("Initiating authorization request with scope: \(request.scope ?? "nil")")

                            let authState = try await appDelegate.startAuth(request: request)
                            
                            let (accessToken, _) = try await authState.tokens()
                            
                            var urlRequest = URLRequest(url: profile.eapconfig_endpoint!)
                            urlRequest.httpMethod = "POST"
                            urlRequest.allHTTPHeaderFields = ["Authorization": "Bearer \(accessToken)"]
                            
                            let (eapConfigData, _) = try await URLSession.shared.data(for: urlRequest)
                            
                            let wifi = try XMLTrial().parse(xmlData: eapConfigData)
                            
                            print("Found \(wifi)")

                            #if os(iOS)
                            let configurator = WifiEapConfigurator()

                            configurator.configureAP(wifi) { success, messages in
                                print("success \(success)")
                                
                                print("messages \(messages)")
                            }
                            #endif
                        } else {
                            
                        }
                    } catch {
                        print("Found \(error)")
                    }
                }
		}
	}
}

#if os(iOS)
enum StartAuthError: Error {
    case noWindow
    case noRootViewController
    case unknownError
}

class GeteduroamAppDelegate: NSObject, UIApplicationDelegate, ObservableObject {
    private var currentAuthorizationFlow: OIDExternalUserAgentSession?
  
    func startAuth(request: OIDAuthorizationRequest) async throws -> OIDAuthState {
        guard let window = UIApplication.shared.windows.first else {
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

    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        // Sends the URL to the current authorization flow (if any) which will process it if it relates to an authorization response.
        if let currentAuthorizationFlow, currentAuthorizationFlow.resumeExternalUserAgentFlow(with: url) {
            self.currentAuthorizationFlow = nil
            return true
        }
        
        return false
    }
}
#elseif os(macOS)
class GeteduroamAppDelegate: NSObject, NSApplicationDelegate, ObservableObject {

    func startAuth(request: OIDAuthorizationRequest) async throws -> OIDAuthState {
        let redirectHTTPHandler = OIDRedirectHTTPHandler(successURL: request.redirectURL)
        redirectHTTPHandler.startHTTPListener(nil) // TODO handle error!

        let window = await NSApplication.shared.mainWindow
        return try await withCheckedThrowingContinuation { continuation in
            redirectHTTPHandler.currentAuthorizationFlow =   OIDAuthState.authState(byPresenting: request, presenting: window!) { authState, error in
                if let authState {
                    continuation.resume(returning: authState)
                } else if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(throwing: NSError(domain: "Huh", code: 1))
                }
            }
        }
    }
}
#endif

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

