import Combine
import SwiftUI
import core

class SelectInstitutionViewModel: ObservableObject {
    private var viewModel: SelectInstitutionCallbackViewModel?

    @Published
    var loading = false

    @Published
    var institutions: [Institution] = []

    @Published
    var error: String?

    private var cancellables = [AnyCancellable]()

    func activate() {
        let viewModel = KotlinDependencies.shared.getSelectInstitutionViewModel()

        doPublish(viewModel.institutions) { [weak self] dataState in
            self?.loading = dataState.loading
            self?.institutions = dataState.data?.institutions ?? []
            self?.error = dataState.exception
        }.store(in: &cancellables)

        self.viewModel = viewModel
    }

    func deactivate() {
        cancellables.forEach { $0.cancel() }
        cancellables.removeAll()

        viewModel?.clear()
        viewModel = nil
    }
    
    func search(_ query: String) {
        viewModel?.onSearchTextChange(search: query)
    }
}

struct ContentView: View {
    @ObservedObject var observableModel = SelectInstitutionViewModel()
    
    @State private var query: String = ""

	var body: some View {
        NavigationView {
            List {
                ForEach(observableModel.institutions, id: \.self) { item in
                    NavigationLink(destination: InstitutionDetail(institution: item)) {
                        Text(item.name)
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("Eduroam ⚙️")
            .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: "Kies een organisatie")
            .onChange(of: query) { observableModel.search($0) }
        }
        .onAppear(perform: {
            observableModel.activate()
        })
        .onDisappear(perform: {
            observableModel.deactivate()
        })
	}
}

struct InstitutionDetail: View {
    var institution: Institution

    var body: some View {
        Text(institution.name)
            .frame(width: 200, height: 200)
            .navigationTitle(institution.name)
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
