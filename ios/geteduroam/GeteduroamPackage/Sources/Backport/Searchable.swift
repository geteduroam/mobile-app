import SwiftUI

///// Wraps SwiftUI's SearchFieldPlacement so it can be used on older OS versions.
public enum SearchFieldPlacement {
    /// Corresponds to [automatic](https://developer.apple.com/documentation/swiftui/searchfieldplacement/automatic)
    case automatic
    
    // TODO: Other cases

    @available(iOS 16.0, tvOS 16.0, watchOS 9.0, *)
    var original: SwiftUI.SearchFieldPlacement {
        switch self {
        case .automatic:
            return .automatic
        }
    }
}

public extension Backport where Content: View {
    
    /// Marks this view as searchable, which configures the display of a
    /// search field.
    ///
    /// For more information about using searchable modifiers, see
    /// <doc:Adding-Search-to-Your-App>.
    ///
    /// - Parameters:
    ///   - text: The text to display and edit in the search field.
    ///   - placement: The preferred placement of the search field within the
    ///     containing view hierarchy.
    ///   - prompt: A ``Text`` view representing the prompt of the search field
    ///     which provides users with guidance on what to search for.
    @ViewBuilder func searchable(text: Binding<String>,
                                 placement: SearchFieldPlacement = .automatic,
                                 prompt: Text? = nil) -> some View {
        if #available(iOS 16, tvOS 16.0, watchOS 9.0, *) {
            content.searchable(text: text, placement: placement.original, prompt: prompt)
        } else {
            VStack {
                TextField("Search", text: text)
                content
            }
        }
    }
    
    /// Marks this view as searchable, which configures the display of a
    /// search field.
    ///
    /// For more information about using searchable modifiers, see
    /// <doc:Adding-Search-to-Your-App>.
    ///
    /// - Parameters:
    ///   - text: The text to display and edit in the search field.
    ///   - placement: The preferred placement of the search field within the
    ///     containing view hierarchy.
    ///   - prompt: The key for the localized prompt of the search field
    ///     which provides users with guidance on what to search for.
    @ViewBuilder func searchable(text: Binding<String>, placement: SearchFieldPlacement = .automatic, prompt: LocalizedStringKey) -> some View {
        if #available(iOS 16, tvOS 16.0, watchOS 9.0, *) {
            content.searchable(text: text, placement: placement.original, prompt: prompt)
        } else {
            VStack {
                TextField(prompt, text: text)
                content
            }
        }
    }
    
    /// Marks this view as searchable, which configures the display of a
    /// search field.
    ///
    /// For more information about using searchable modifiers, see
    /// <doc:Adding-Search-to-Your-App>.
    ///
    /// - Parameters:
    ///   - text: The text to display and edit in the search field.
    ///   - placement: The preferred placement of the search field within the
    ///     containing view hierarchy.
    ///   - prompt: A string representing the prompt of the search field
    ///     which provides users with guidance on what to search for.
    @ViewBuilder func searchable<S>(text: Binding<String>, placement: SearchFieldPlacement = .automatic, prompt: S) -> some View where S : StringProtocol {
        if #available(iOS 16, tvOS 16.0, watchOS 9.0, *) {
            content.searchable(text: text, placement: placement.original, prompt: prompt)
        } else {
            VStack {
                TextField(prompt, text: text)
                content
            }
        }
    }

}
