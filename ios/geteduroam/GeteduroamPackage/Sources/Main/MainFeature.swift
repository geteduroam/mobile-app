import AuthClient
import CacheClient
import ComposableArchitecture
import Connect
import DiscoveryClient
import Foundation
import Models

public struct Main: ReducerProtocol {
    public let authClient: AuthClient
    @Dependency(\.cacheClient) var cacheClient
    @Dependency(\.discoveryClient) var discoveryClient
    
    public init(authClient: AuthClient = FailingAuthClient()) {
        self.authClient = authClient
    }
    
    public struct State: Equatable {
        public init(searchQuery: String = "", institutions: IdentifiedArrayOf<Institution> = .init(uniqueElements: []), loadingState: LoadingState = .initial) {
            self.searchQuery = searchQuery
            self.institutions = institutions
            self.loadingState = loadingState
            self.searchResults = .init(uniqueElements: [])
        }
        
        var searchQuery: String
        var isSearching: Bool = false
        var institutions: IdentifiedArrayOf<Institution>
        
        var selectedInstitutionState: Connect.State?
        
        var searchResults: IdentifiedArrayOf<Institution>

        var isSheetVisible: Bool {
            selectedInstitutionState != nil
        }
        
        public enum LoadingState: Equatable {
            case initial
            case isLoading
            case success
            case failure
        }
        
        var loadingState: LoadingState
        var alert: AlertState<Action>?
    }
    
    public enum Action: Equatable {
        case discoveryResponse(TaskResult<InstitutionsResponse>)
        case onAppear
        case searchQueryChanged(String)
        case searchQueryChangeDebounced
        case searchResponse(TaskResult<IdentifiedArrayOf<Institution>>)
        case select(Institution)
        case institution(Connect.Action)
        case dismissSheet
        case tryAgainTapped
        case dismissErrorTapped
    }
    
    private enum SearchID {}
    
    func search(query: String, institutions: IdentifiedArrayOf<Institution>) -> IdentifiedArrayOf<Institution> {
        guard query.isEmpty == false else {
            return .init(uniqueElements: [])
        }
        return .init(uniqueElements: institutions
            // Apple recommends this, but that seems to be diacritic sensitive
            // .filter({ $0.name.localizedCaseInsensitiveContains(query) })
            .filter({ $0.name.range(of: query, options: [.caseInsensitive, .diacriticInsensitive], locale: Locale.current) != nil })
            .sorted(by: { $0.name < $1.name }))
    }

    public var body: some ReducerProtocol<State, Action> {
        Reduce { state, action in
            switch action {
            case .onAppear, .tryAgainTapped:
                state.loadingState = .isLoading
                return .task {
                    await .discoveryResponse(TaskResult {
                        do {
                            let (value, _) = try await discoveryClient.decodedResponse(for: .discover, as: InstitutionsResponse.self)
                            cacheClient.cacheInstitutions(value)
                            return value
                        } catch {
                            let restoredValue = try cacheClient.restoreInstitutions()
                            return restoredValue
                        }
                    })
                }

            case let .discoveryResponse(.success(response)):
                state.loadingState = .success
                state.institutions = .init(uniqueElements: response.instances)
                return .none
                
            case let .discoveryResponse(.failure(error)):
                state.loadingState = .failure
                state.alert = AlertState(
                    title: .init(NSLocalizedString("Failed to load institutions", bundle: .module, comment: "")),
                    message: .init((error as NSError).localizedDescription),
                    dismissButton: .default(.init(NSLocalizedString("OK", bundle: .module, comment: "OK")), action: .send(.dismissErrorTapped))
                )
                return .none
                
            case let .searchQueryChanged(query):
                state.searchQuery = query
                state.isSearching = true
                
                // When the query is cleared we can clear the search results, but we have to make sure to cancel
                // any in-flight search requests too, otherwise we may get data coming in later.
                guard !query.isEmpty else {
                    state.searchResults = []
                    state.isSearching = false
                    return .cancel(id: SearchID.self)
                }
                return .none
                
            case .searchQueryChangeDebounced:
                guard !state.searchQuery.isEmpty else {
                    return .none
                }
                return .task { [query = state.searchQuery, institutions = state.institutions] in
                    await .searchResponse(TaskResult { self.search(query: query, institutions: institutions) })
                }
                .cancellable(id: SearchID.self)
                
            case let .searchResponse(.success(searchResults)):
                state.searchResults = searchResults
                state.isSearching = false
                return .none
                
            case .searchResponse(.failure):
                state.isSearching = false
                return .none
                
            case let .select(institution):
                state.selectedInstitutionState = .init(institution: institution)
                return .none
                
            case .institution(.dismissTapped), .institution(.startAgainTapped), .dismissSheet:
                state.selectedInstitutionState = nil
                return .none
                
            case .institution:
                return .none
                
            case .dismissErrorTapped:
                state.alert = nil
                return .none
            }
        }
        .ifLet(\.selectedInstitutionState, action: /Action.institution) {
            Connect(authClient: authClient)
        }
    }
}
