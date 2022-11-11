import ComposableArchitecture

public struct Main: ReducerProtocol {
    public init() { }
    
    public struct State: Equatable {
        public init(query: String = "") {
            self.query = query
        }
        
        var query: String = ""
    }
    
    public enum Action: Equatable {
        case search(String)
        case onAppear
    }
    
    public var body: some ReducerProtocol<State, Action> {
        Reduce { state, action in
            switch action {
            case let .search(query):
                state.query = query
                return .none
                
            case .onAppear:
                // TODO: Discover
                return .none
            }
        }
    }
}
