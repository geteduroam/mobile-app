import ComposableArchitecture
import SwiftUI
import AppAuth

public struct MainView: View {
    public init(store: StoreOf<Main>) {
        self.store = store
    }
    
    public let store: StoreOf<Main>
    
    public var body: some View {
        WithViewStore(store) { viewStore in
            NavigationView {
                List {
//                    ForEach(observableModel.institutions, id: \.self) { item in
//                        NavigationLink(destination: InstitutionView(institution: item)) {
//                            InstitutionRowView(institution: item)
//                        }
//                    }
                }
                .listStyle(.plain)
                .navigationTitle("Eduroam")
//                .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: "Kies een organisatie")
//                .onChange(of: query) { observableModel.search($0) }
            }
//            .navigationViewStyle(.stack)
            .onAppear {
                viewStore.send(.onAppear)
            }
        }
    }
}

//struct MainView_Previews: PreviewProvider {
//    static var previews: some View {
//        MainView(store: <#T##StoreOf<Main>#>)
//    }
//}
