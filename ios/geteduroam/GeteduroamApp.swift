import AuthClient
import ComposableArchitecture
import Main
import Models
import SwiftUI

@main
struct GeteduroamApp: App {
    
    @UIApplicationDelegateAdaptor private var appDelegate: GeteduroamAppDelegate
    
    var store: StoreOf<Main>!
    
    init() {
        store = .init(initialState: .init(), reducer: Main(authClient: appDelegate))
    }
    
    @StateObject var theme = Theme(
        searchFont: .custom("OpenSans-Regular", size: 20, relativeTo: .body),
        errorFont: .custom("OpenSans-Regular", size: 16, relativeTo: .body),
        institutionNameFont: .custom("OpenSans-Bold", size: 16, relativeTo: .body),
        institutionCountryFont: .custom("OpenSans-Regular", size: 11, relativeTo: .footnote),
        profilesHeaderFont: .custom("OpenSans-SemiBold", size: 12, relativeTo: .body),
        profileNameFont: .custom("OpenSans-Regular", size: 16, relativeTo: .body),
        connectButtonFont: .custom("OpenSans-Bold", size: 20, relativeTo: .body),
        connectedFont: .custom("OpenSans-Bold", size: 14, relativeTo: .body))
    
	var body: some Scene {
		WindowGroup {
            MainView(store: store)
                .environmentObject(theme)
		}
      
	}
}
