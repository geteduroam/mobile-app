import SwiftUI

/// Wraps SwiftUI's ScrollDismissesKeyboardMode so it can be used on older OS versions.
public enum ScrollDismissesKeyboardMode {
    /// Corresponds to [automatic](https://developer.apple.com/documentation/swiftui/scrolldismisseskeyboardmode/automatic)
    case automatic

    /// Corresponds to [immediately](https://developer.apple.com/documentation/swiftui/scrolldismisseskeyboardmode/immediately)
    case immediately

    /// Corresponds to [interactively](https://developer.apple.com/documentation/swiftui/scrolldismisseskeyboardmode/automatic)
    case interactively

    /// Corresponds to [never](https://developer.apple.com/documentation/swiftui/scrolldismisseskeyboardmode/never)
    case never

    @available(iOS 16.0, tvOS 16.0, watchOS 9.0, *)
    var original: SwiftUI.ScrollDismissesKeyboardMode {
        switch self {
        case .automatic:
            return .automatic
        case .immediately:
            return .immediately
        case .interactively:
            return .interactively
        case .never:
            return .never
        }
    }
}

public extension Backport where Content: View {
    /// Configures the behavior in which scrollable content interacts with the software keyboard.
    /// - Parameter mode: Mode to apply
    /// - Returns: View with dismiss keyboard mode applied if available
    @ViewBuilder func scrollDismissesKeyboard(_ mode: ScrollDismissesKeyboardMode) -> some View {
        if #available(iOS 16, tvOS 16.0, watchOS 9.0, *) {
            content.scrollDismissesKeyboard(mode.original)
        } else {
            content
        }
    }
}
