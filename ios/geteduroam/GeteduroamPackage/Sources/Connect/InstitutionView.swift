import AuthClient
import ComposableArchitecture
import Models
import SwiftUI

public struct ConnectView: View {
    public init(store: StoreOf<Connect>) {
        self.store = store
    }
    
    let store: StoreOf<Connect>
    
    @EnvironmentObject private var appDelegate: GeteduroamAppDelegate
    
    public var body: some View {
        WithViewStore(store) { viewStore in
            Group {
                switch viewStore.loadingState {
                case .initial, .failure:
                    List {
                        Section {
                            let selectedProfile = viewStore.selectedProfile
                            ForEach(viewStore.institution.profiles) { profile in
                                Button {
                                    viewStore.send(.select(profile.id))
                                } label: {
                                    ProfileRowView(profile: profile, isSelected: selectedProfile == profile)
                                }
                            }
                        } header: {
                            Text("Profiles")
                        }
                        Button {
                            // Not very clean to insert the appDelegate this way, but couldn't find a way get proper access in another way
                            viewStore.send(.connect(appDelegate))
                        } label: {
                            Text("Connect")
                        }
                    }
                    
                case .isLoading:
                    ProgressView()
                    
                case .success:
                    VStack {
                        Image(systemName: "checkmark")
                        Text("Connected")
                        Button {
                            viewStore.send(.startAgainTapped)
                        } label: {
                            Text("Start Again")
                        }
                    }
                }
            }
            .onAppear {
                viewStore.send(.onAppear(appDelegate))
            }
            .navigationTitle(viewStore.institution.name)
            .navigationBarTitleDisplayMode(.inline)
            .alert(
              self.store.scope(state: \.alert),
              dismiss: .dismissErrorTapped
            )
        }
    }
}

/* Disabled, crashes due to unavailable appDelegate/authClient
struct ConnectView_Previews: PreviewProvider {
    static var previews: some View {
        ConnectView(store: .init(
            initialState: .init(
                institution: .init(
                    id: "1",
                    name: "My Institution",
                    country: "NL",
                    cat_idp: 1,
                    profiles: [
                        .init(
                            id: "2",
                            name: "My Profile",
                            default: true,
                            eapconfig_endpoint: nil,
                            oauth: false,
                            authorization_endpoint: nil,
                            token_endpoint: nil)],
                    geo: [.init(lat: 0, lon: 0)])),
            reducer: Connect()))
    }
}
*/
