import ComposableArchitecture
import DiscoveryClient
import Models
import Institution
import Foundation

public struct Main: ReducerProtocol {
    public init() { }
    
    public struct State: Equatable {
        public init(query: String = "", institutions: IdentifiedArrayOf<Institution> = .init(uniqueElements: []), loadingState: LoadingState = .initial) {
            self.query = query
            self.institutions = institutions
            self.loadingState = loadingState
        }
        
        var query: String
        var institutions: IdentifiedArrayOf<Institution>
        
        var selectedInstitutionState: InstitutionSetup.State?
        
        var searchResults: IdentifiedArrayOf<Institution> {
            guard query.isEmpty == false else {
                return .init(uniqueElements: [])
            }
            return .init(uniqueElements: institutions
                .filter( { $0.name.localizedCaseInsensitiveContains(query) })
                .sorted(by: { $0.name < $1.name }))
        }
        
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
        case search(String)
        case select(Institution)
        case institution(InstitutionSetup.Action)
        case dismissSheet
        case tryAgainTapped
        case dismissErrorTapped
    }
    
    @Dependency(\.discoveryClient) var discoveryClient
    
    public var body: some ReducerProtocol<State, Action> {
        Reduce { state, action in
            switch action {
            case .onAppear, .tryAgainTapped:
                state.loadingState = .isLoading
                return .task {
                    await .discoveryResponse(TaskResult { try await discoveryClient.decodedResponse(for: .discover, as: InstitutionsResponse.self).value })
                }

            case let .discoveryResponse(.success(response)):
                state.loadingState = .success
                state.institutions = .init(uniqueElements: response.instances)
                return .none
                
            case let .discoveryResponse(.failure(error)):
                state.loadingState = .failure
                state.alert = AlertState(
                    title: .init("Failed to load institutions"),
                    message: .init((error as NSError).localizedDescription),
                    dismissButton: .default(.init("OK"), action: .send(.dismissErrorTapped))
                )
                return .none
                
            case let .search(query):
                state.query = query
                return .none
                
            case let .select(institution):
                state.selectedInstitutionState = .init(institution: institution)
                return .none
                
            case .institution(.startAgainTapped), .dismissSheet:
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
            InstitutionSetup()
        }
    }
}
