import SwiftUI

public class Theme: ObservableObject {
    @Published public var searchFont: Font
    @Published public var errorFont: Font
    @Published public var institutionNameFont: Font
    @Published public var institutionCountryFont: Font
    @Published public var profilesHeaderFont: Font
    @Published public var profileNameFont: Font
    @Published public var connectButtonFont: Font
    @Published public var connectedFont: Font
    
    public init(searchFont: Font, errorFont: Font, institutionNameFont: Font, institutionCountryFont: Font, profilesHeaderFont: Font, profileNameFont: Font, connectButtonFont: Font, connectedFont: Font) {
        self.searchFont = searchFont
        self.errorFont = errorFont
        self.institutionNameFont = institutionNameFont
        self.institutionCountryFont = institutionCountryFont
        self.profilesHeaderFont = profilesHeaderFont
        self.profileNameFont = profileNameFont
        self.connectButtonFont = connectButtonFont
        self.connectedFont = connectedFont
    }
}
