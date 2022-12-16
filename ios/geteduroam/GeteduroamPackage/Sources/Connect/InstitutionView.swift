import AuthClient
import ComposableArchitecture
import Models
import SwiftUI

public struct ConnectView: View {
    public init(store: StoreOf<Connect>) {
        self.store = store
    }
    
    let store: StoreOf<Connect>

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
                                .font(Font.custom("OpenSans-SemiBold", size: 12, relativeTo: .body))
                        }
                        
                        Button {
                            viewStore.send(.connect)
                        } label: {
                            Text("Connect")
                                .multilineTextAlignment(.center)
                                .font(Font.custom("OpenSans-Bold", size: 16, relativeTo: .body))
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
                viewStore.send(.onAppear)
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
