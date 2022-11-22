import ComposableArchitecture
import Connect
import Models
import SwiftUI

public struct MainView: View {
    public init(store: StoreOf<Main>) {
        self.store = store
    }
    
    public let store: StoreOf<Main>
    
    public var body: some View {
        WithViewStore(store) { viewStore in
            NavigationView {
                switch viewStore.loadingState {
                case .initial:
                    EmptyView()
                    
                case .isLoading:
                    ProgressView()
                    
                case .success:
                    if #available(iOS 15.0, *) {
                        List {
                            ForEach(viewStore.searchResults) { institution in
                                Button {
                                    viewStore.send(.select(institution))
                                } label: {
                                    InstitutionRowView(institution: institution)
                                }
                            }
                        }
                        .listStyle(.plain)
                        .navigationTitle("Eduroam")
                        .searchable(text: viewStore.binding(get: \.query, send: Main.Action.search), placement: .navigationBarDrawer(displayMode: .always), prompt: "Kies een organisatie")
                    } else {
                        // Fallback on earlier versions
                    }
                    
                case .failure:
                    VStack {
                        Image(systemName: "exclamationmark.triangle")
                        Text("Failed to load institutions")
                        Button {
                            viewStore.send(.tryAgainTapped)
                        } label: {
                            Text("Try Again")
                        }
                    }
                }
               
            }
//            .navigationViewStyle(.stack)
            .onAppear {
                viewStore.send(.onAppear)
            }
            .sheet(isPresented: viewStore.binding(get: \.isSheetVisible, send: Main.Action.dismissSheet)) {
                IfLetStore(store.scope(state: \.selectedInstitutionState, action: Main.Action.institution)) { store in
                    NavigationView {
                        ConnectView(store: store)
                    }
                }
//                .interactiveDismissDisabled()
            }
            .alert(
              self.store.scope(state: \.alert),
              dismiss: .dismissErrorTapped
            )
        }
    }
}

struct MainView_Previews: PreviewProvider {
    static var previews: some View {
        MainView(store: .init(initialState: .init(), reducer: Main()))
    }
}
