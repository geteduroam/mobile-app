public typealias LocalizedString = [LocalizedStringEntry]

public extension LocalizedString {
    init(string: String) {
        self = [.init(language: nil, value: string)]
    }
}
