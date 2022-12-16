import Backport
import ComposableArchitecture
import Connect
import Models
import SwiftUI

public struct MainView: View {
    public init(store: StoreOf<Main>) {
        self.store = store
    }
    
    public let store: StoreOf<Main>
    
    @FocusState var searchFieldIsFocused: Bool
    
    public var body: some View {
        WithViewStore(store) { viewStore in
            NavigationView {
                switch viewStore.loadingState {
                case .initial:
                    EmptyView()
                    
                case .isLoading:
                    ProgressView()
                    
                case .success:
                    VStack {
                        HStack {
                            Image(systemName: "magnifyingglass")
                            
                            TextField(
                                "Choose an organisation",
                                text: viewStore.binding(get: \.searchQuery, send: Main.Action.searchQueryChanged))
                            .font(Font.custom("OpenSans-Regular", size: 16, relativeTo: .body))
                            .focused($searchFieldIsFocused)
                            .textInputAutocapitalization(.never)
                            
                            Button {
                                viewStore.send(.searchQueryChanged(""))
                            } label: {
                                Image(systemName: "xmark.circle.fill")
                            }
                            .buttonStyle(.plain)
                            .opacity(searchFieldIsFocused && viewStore.searchQuery.isEmpty == false ? 1 : 0)
                        }
                        .padding(16)
                        
                        if searchFieldIsFocused == false && viewStore.isSearching == false && viewStore.searchQuery.isEmpty && viewStore.searchResults.isEmpty {
                            Image("Launch Screen/Heart")
                                .resizable()
                                .frame(width: 200, height: 200)
                                .transition(.scale)
                        } else if viewStore.isSearching == false && viewStore.searchQuery.isEmpty == false && viewStore.searchResults.isEmpty {
                            Text("No matches found")
                        } else {
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
                        }
                       
                    }
                    .background(Color("Launch Screen/Background"))
                    .task(id: viewStore.searchQuery) {
                        do {
                            try await Task.sleep(nanoseconds: NSEC_PER_SEC / 4)
                            await viewStore.send(.searchQueryChangeDebounced).finish()
                        } catch {}
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
            .navigationViewStyle(.stack)
            .onAppear {
                // TODO: To focus or not to focus? searchFieldIsFocused = true
                viewStore.send(.onAppear)
            }
            .sheet(isPresented: viewStore.binding(get: \.isSheetVisible, send: Main.Action.dismissSheet)) {
                IfLetStore(store.scope(state: \.selectedInstitutionState, action: Main.Action.institution)) { store in
                    NavigationView {
                        ConnectView(store: store)
                            .toolbar {
                                ToolbarItem(placement: .navigationBarLeading) {
                                    Button {
                                        viewStore.send(.dismissSheet)
                                    } label: {
                                        Text("Cancel")
                                    }
                                }
                            }
                    }
                }
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
