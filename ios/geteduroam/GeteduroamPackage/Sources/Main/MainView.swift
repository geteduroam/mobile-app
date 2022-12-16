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
                    VStack(alignment: .leading, spacing: 4) {

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
                                .background(Color.white)
                                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 1, maxHeight: 1)
                             
                        }
                        .padding(16)
                        
                        if searchFieldIsFocused == false && viewStore.isSearching == false && viewStore.searchQuery.isEmpty && viewStore.searchResults.isEmpty {
                            
                        } else {
                            if #available(iOS 16.0, *) {
                                List {
                                    if viewStore.isSearching == false && viewStore.searchQuery.isEmpty == false && viewStore.searchResults.isEmpty {
                                        Text("No matches found")
                                    } else {
                                        ForEach(viewStore.searchResults) { institution in
                                            Button {
                                                viewStore.send(.select(institution))
                                            } label: {
                                                InstitutionRowView(institution: institution)
                                            }
                                        }
                                    }
                                }
                                .scrollContentBackground(.hidden)
                                .background(Color.yellow)
                                .listStyle(.plain)
                            } else {
                                // Fallback on earlier versions
                            }
//                            .listRowBackground(Color("Launch Screen/Background"))
//                            .background(Color.clear)
                        }
                       Spacer()
                    }
                  
                    .background {
                        ZStack {
                            Color("Launch Screen/Background")
                                
                            
                            VStack(alignment: .trailing) {
                                Spacer()
                                Image("Launch Screen/Eduroam")
                                    .resizable()
                                    .frame(width: 160, height: 74)
                                    .padding(.bottom, 80)
                                
                            }
                             .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .trailing)
                            if searchFieldIsFocused == false && viewStore.isSearching == false && viewStore.searchQuery.isEmpty && viewStore.searchResults.isEmpty {
                                VStack(spacing: 0) {
                                    Image("Launch Screen/Heart")
                                        .resizable()
                                        .frame(width: 200, height: 200)
                                        .transition(.scale)
                                    Spacer()
                                        .frame(width: 200, height: 200)
                                }
                            
                                
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
