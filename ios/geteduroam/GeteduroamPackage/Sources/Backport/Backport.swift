import SwiftUI

// Based on https://www.ralfebert.com/swiftui/new-ios-view-modifiers-with-older-deployment-target/

/// Allows usage of new API while supporting older OS versions.
///
/// Backport supplies a way to use newer APIs while maintaining support for older versions of the OS. This is especially useful for viewmodifiers which can't be wrapped with `@available`.
///
/// ## Sample Usage:
/// ```swift
/// view.backport.newFancyViewModifier()
/// ```
public struct Backport<Content> {
    let content: Content
}

public extension View {
    var backport: Backport<Self> { Backport(content: self) }
}
