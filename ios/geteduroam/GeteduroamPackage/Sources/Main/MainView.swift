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
                VStack(alignment: .leading, spacing: 4) {
                    if viewStore.loadingState == .success {
                        VStack(spacing: 8) {
                            HStack {
                                Image(systemName: "magnifyingglass")
                                TextField(
                                    "Search for your institution",
                                    text: viewStore.binding(get: \.searchQuery, send: Main.Action.searchQueryChanged))
                                .font(Font.custom("OpenSans-Regular", size: 20, relativeTo: .body))
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
                            Rectangle()
                                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 1, maxHeight: 1)
                                
                        }
                        .padding(16)
                    }
                    
                    if viewStore.loadingState == .failure {
                        HStack {
                            Image(systemName: "exclamationmark.triangle")
                            Text("Failed to load institutions")
                                .font(Font.custom("OpenSans-Bold", size: 16, relativeTo: .body))
                            Button {
                                viewStore.send(.tryAgainTapped)
                            } label: {
                                Image(systemName: "arrow.clockwise")
                            }
                        }
                        .padding(16)
                        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .top)
                        
                    } else {
                        List {
                            if viewStore.isSearching == false && viewStore.searchQuery.isEmpty == false && viewStore.searchResults.isEmpty {
                                Text("No matches found")
                                    .font(Font.custom("OpenSans-Regular", size: 16, relativeTo: .body))
                                    .listRowSeparatorTint(Color.clear)
                                    .listRowBackground(Color("Background"))
                            } else if viewStore.searchResults.isEmpty {
                                Text("")
                                    .listRowSeparatorTint(Color.clear)
                                    .listRowBackground(Color.clear)
                            } else {
                                ForEach(viewStore.searchResults) { institution in
                                    Button {
                                        viewStore.send(.select(institution))
                                    } label: {
                                        InstitutionRowView(institution: institution)
                                    }
                                    .listRowSeparatorTint(Color("ListSeparator"))
                                    .listRowBackground(Color("Background"))
                                }
                            }
                        }
                        .listStyle(.plain)
                    }
                }
                .background {
                    ZStack {
                        Color("Background")
                        
                        VStack(alignment: .trailing) {
                            Spacer()
                            Image("Eduroam")
                                .resizable()
                                .frame(width: 160, height: 74)
                                .padding(.bottom, 80)
                            
                        }
                        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .trailing)
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
                .task(id: viewStore.searchQuery) {
                    do {
                        try await Task.sleep(nanoseconds: NSEC_PER_SEC / 4)
                        await viewStore.send(.searchQueryChangeDebounced).finish()
                    } catch {}
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
                                ToolbarItem(placement: .navigationBarTrailing) {
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
