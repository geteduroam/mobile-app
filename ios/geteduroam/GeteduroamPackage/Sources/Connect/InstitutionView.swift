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
            VStack(alignment: .leading) {
                HStack(alignment: .firstTextBaseline) {
                    VStack(alignment: .leading) {
                        Text(viewStore.institution.name)
                            .font(Font.custom("OpenSans-Bold", size: 16, relativeTo: .body))
                        Text(viewStore.institution.country)
                            .font(Font.custom("OpenSans-Regular", size: 11, relativeTo: .footnote))
                    }
                    Spacer()
                    Button(action: {
                        viewStore.send(.dismissTapped)
                    }, label: {
                        Image(systemName: "xmark")
                    })
                    .buttonStyle(.plain)
                }
                .padding(20)
                
                if viewStore.isConnected == false {
                    List {
                        Section {
                            let selectedProfile = viewStore.selectedProfile
                            ForEach(viewStore.institution.profiles) { profile in
                                Button {
                                    viewStore.send(.select(profile.id))
                                } label: {
                                    ProfileRowView(profile: profile, isSelected: selectedProfile == profile)
                                }
                                .listRowSeparatorTint(Color("ListSeparator"))
                                .listRowBackground(Color("Background"))
                            }
                        } header: {
                            Text("Profiles")
                                .font(Font.custom("OpenSans-SemiBold", size: 12, relativeTo: .body))
                        }
                    }
                    .listStyle(.plain)
                    .disabled(viewStore.canSelectProfile == false)
                }
                
                HStack {
                    Spacer()
                    VStack(alignment: .center) {
                        if viewStore.isConnected {
                            Spacer()
                            Label("Connected", systemImage: "checkmark")
                                .font(Font.custom("OpenSans-Bold", size: 14, relativeTo: .body))
                        } else {
                            Button {
                                viewStore.send(.connect)
                            } label: {
                                Text("CONNECT")
                                    .multilineTextAlignment(.center)
                            }
                            .disabled(viewStore.isLoading)
                            .buttonStyle(ConnectButtonStyle())
                        }
                    }
                    Spacer()
                }
                Spacer()
            }
            .onAppear {
                viewStore.send(.onAppear)
            }
            .background {
                ZStack {
                    Color("Background")
                    VStack(spacing: 0) {
                        Image("Heart")
                            .resizable()
                            .frame(width: 200, height: 200)
                        Spacer()
                            .frame(width: 200, height: 200)
                    }
                }
                .edgesIgnoringSafeArea(.all)
            }
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .center)
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
                            token_endpoint: nil),
                        .init(
                            id: "3",
                            name: "Other Profile",
                            default: false,
                            eapconfig_endpoint: nil,
                            oauth: false,
                            authorization_endpoint: nil,
                            token_endpoint: nil)
                    ],
                    geo: [.init(lat: 0, lon: 0)])),
            reducer: Connect()))
    }
}
