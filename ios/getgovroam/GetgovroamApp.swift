import AuthClient
import ComposableArchitecture
import Main
import Models
import SwiftUI

@main
struct GetgovroamApp: App {
    
    @UIApplicationDelegateAdaptor private var appDelegate: GeteduroamAppDelegate
    
    var store: StoreOf<Main>!
    
    init() {
        store = .init(initialState: .init(), reducer: Main(authClient: appDelegate))
    }
    
    @StateObject var theme = Theme(
        searchFont: .body,
        errorFont: .body,
        institutionNameFont: .headline,
        institutionCountryFont: .footnote,
        profilesHeaderFont: .caption,
        profileNameFont: .body,
        connectButtonFont: .body,
        connectedFont: .body)
    
	var body: some Scene {
		WindowGroup {
            MainView(store: store)
                .environmentObject(theme)
		}
	}
}
