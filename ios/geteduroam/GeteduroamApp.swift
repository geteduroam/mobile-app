import AuthClient
import ComposableArchitecture
import Main
import SwiftUI

@main
struct GeteduroamApp: App {
    
    @UIApplicationDelegateAdaptor private var appDelegate: GeteduroamAppDelegate
    
    var store: StoreOf<Main>!
    
    init() {
        store = .init(initialState: .init(), reducer: Main(authClient: appDelegate))
    }
    
	var body: some Scene {
		WindowGroup {
            MainView(store: store)
		}
	}
}
