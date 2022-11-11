import ComposableArchitecture
import Main
import SwiftUI

@main
struct GeteduroamApp: App {
    
    #if os(iOS)
    @UIApplicationDelegateAdaptor private var appDelegate: GeteduroamAppDelegate
    #elseif os(macOS)
    @NSApplicationDelegateAdaptor private var appDelegate: GeteduroamAppDelegate
    #endif
    
    let store: StoreOf<Main> = .init(initialState: .init(), reducer: Main())
    
	var body: some Scene {
		WindowGroup {
            MainView(store: store)
 /*               .task {
                    do {
                        let apiClient = URLRoutingClient.live(router: discoveryRouter.baseURL("https://discovery.eduroam.app/v1/"))
                        
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
  */
		}
	}
}
