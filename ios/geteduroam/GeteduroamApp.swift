import ComposableArchitecture
import Main
import SwiftUI
import AuthClient

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
		}
	}
}
